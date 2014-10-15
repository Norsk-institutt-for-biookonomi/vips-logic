/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

package no.bioforsk.vips.logic.authenticate;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import no.bioforsk.vips.util.ServletUtil;
import org.openid4java.OpenIDException;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.InMemoryConsumerAssociationStore;
import org.openid4java.consumer.InMemoryNonceVerifier;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.AxMessage;
import org.openid4java.message.ax.FetchRequest;
import org.openid4java.message.ax.FetchResponse;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;

/**
 * Contains all methods needed to perform authenticating sessions with OpenId
 * Resources:
 * <ul>
 *  <li>Attributes: http://openid.net/specs/openid-attribute-properties-list-1_0-01.html</li>
 * </ul>
 * Relies on OpenId4Java.
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class OpenIdRelyingParty {
    private static ConsumerManager consumerManager;

    public OpenIdRelyingParty() throws ConsumerException
    {
        // instantiate a ConsumerManager object
        consumerManager = new ConsumerManager();
    }
    
    private static ConsumerManager getConsumerManager()
    {
        if (consumerManager == null) {
                consumerManager = new ConsumerManager();
                consumerManager.setAssociations(new InMemoryConsumerAssociationStore());
                consumerManager.setNonceVerifier(new InMemoryNonceVerifier(10000));
        }
        return consumerManager;
    }

    // --- placing the authentication request for first time registration ---
    public String authRequest(String userSuppliedString,
                              String returnToUrl,
                              HttpServletRequest httpReq,
                              HttpServletResponse httpResp)
            throws IOException
    {
        try
        {
            // perform discovery on the user-supplied identifier
            // The list is a list of DiscoveryInformation objects
            List discoveries = getConsumerManager().discover(userSuppliedString);

            // attempt to associate with the OpenID provider
            // and retrieve one service endpoint for authentication
            DiscoveryInformation discovered = getConsumerManager().associate(discoveries);
            
            //System.out.println("DiscoveryInformation=" + discovered.toString());
            
            // store the discovery information in the user's session
            httpReq.getSession().setAttribute("openid-disc", discovered);

            // obtain a AuthRequest message to be sent to the OpenID provider
            AuthRequest authReq = getConsumerManager().authenticate(discovered, returnToUrl);
            authReq.setHandle("");
            
            /**
             * OpenId Providers are VERY diverse when it comes to attributes
             * We attempt to get email address (and other attributes) in diverse ways
             */
            /*
            
            We skip this for now... (2013-11-27)
            // This works with Symantec Personal Identity Portal
            SRegRequest sRegRequest = SRegRequest.createFetchRequest();
            sRegRequest.addAttribute("email", true);
            // attach the extension to the authentication request
            authReq.addExtension(sRegRequest);
            
            // This works with Google
            FetchRequest fetch = FetchRequest.createFetchRequest();
            fetch.addAttribute("email1","http://openid.net/schema/contact/internet/email", true);                                      // required
            fetch.addAttribute("email2", "http://schema.openid.net/contact/email", true);
            fetch.addAttribute("email3", "http://axschema.org/contact/email", true);
            // attach the extension to the authentication request
            authReq.addExtension(fetch);
            */

            httpResp.sendRedirect(authReq.getDestinationUrl(true));
            return null;
        }
        catch (OpenIDException e)
        {
            // present error to the user
        }

        return null;
    }

    // --- processing the authentication response ---
    public OpenIdAuthAttributes verifyResponse(HttpServletRequest httpReq)
    {
        try
        {
            
            // extract the parameters from the authentication response
            // (which comes in as a HTTP request from the OpenID provider)
            ParameterList response =
                    new ParameterList(httpReq.getParameterMap());

            // retrieve the previously stored discovery information
            DiscoveryInformation discovered = (DiscoveryInformation)
                    httpReq.getSession().getAttribute("openid-disc");
            //System.out.println("discovered = " + discovered.toString());

            // verify the response; ConsumerManager needs to be the same
            // (static) instance used to place the authentication request
            VerificationResult verification = getConsumerManager().verify(
                    ServletUtil.getCompleteRequestURL(httpReq),
                    response, discovered);

            //System.out.println("Verification finished. Verification = " + verification.toString());
            
            // examine the verification result and extract the verified identifier
            Identifier verified = verification.getVerifiedId();
            if (verified != null)
            {
                //System.out.println("Verified = " + verified.getIdentifier());
                
                AuthSuccess authSuccess =
                        (AuthSuccess) verification.getAuthResponse();
                
                OpenIdAuthAttributes attributes = this.getOpenIdAuthAttributes(verified.getIdentifier(), authSuccess);

                

                return attributes;  // success
            }
        }
        catch (OpenIDException e)
        {
            e.printStackTrace();// present error to the user
        }

        return null;
    }
    
    private OpenIdAuthAttributes getOpenIdAuthAttributes(String identifier, AuthSuccess authSuccess) throws MessageException
    {
        OpenIdAuthAttributes retVal = new OpenIdAuthAttributes();
        retVal.setOpenId(identifier);
        if (authSuccess.hasExtension(SRegMessage.OPENID_NS_SREG))
        {
            MessageExtension extension = authSuccess.getExtension(SRegMessage.OPENID_NS_SREG);
            if(extension instanceof SRegResponse)
            {
                SRegResponse sRegResponse = (SRegResponse) extension;
                String value = sRegResponse.getAttributeValue("email");
                if(value !=  null)
                {
                    retVal.setEmail(value);
                }
                value = sRegResponse.getAttributeValue("firstname");
                if(value != null)
                {
                    retVal.setFirstName(value);
                }
                value = sRegResponse.getAttributeValue("lastname");
                if(value != null)
                {
                    retVal.setLastName(value);
                }
                value = sRegResponse.getAttributeValue("fullname");
                if(value != null)
                {
                    retVal.setFullName(value);
                }
                value = sRegResponse.getAttributeValue("countrycode");
                if(value != null)
                {
                    retVal.setCountryCode(value);
                }
            }

        }
        else if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX))
        {
            FetchResponse fetchResp = (FetchResponse) authSuccess
                    .getExtension(AxMessage.OPENID_NS_AX);

            //String emails = fetchResp.getAttributeValues("email1");
            String email = fetchResp.getAttributeValue("email1");
            if(email ==  null)
            {
                email = fetchResp.getAttributeValue("email2");
            }
            if(email == null)
            {
                email = fetchResp.getAttributeValue("email3");
            }
            retVal.setEmail(email);
        }

        
        return retVal;
    }

}

