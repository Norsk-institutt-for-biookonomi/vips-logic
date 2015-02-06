/*
 * Copyright (c) 2015 Bioforsk <http://www.bioforsk.no/>. 
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

package no.bioforsk.vips.logic.util;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @copyright 2015 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */
public class DOMUtils {

    public static synchronized String getNodeValue(String tagName, NodeList nodes ) {
        for ( int x = 0; x < nodes.getLength(); x++ ) {
            Node node = nodes.item(x);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                NodeList childNodes = node.getChildNodes();
                for (int y = 0; y < childNodes.getLength(); y++ ) {
                    Node data = childNodes.item(y);
                    if ( data.getNodeType() == Node.TEXT_NODE )
                        return data.getNodeValue();
                }
            }
        }
        return "";
    }
    
    public static synchronized Node getNode(String tagName, NodeList nodes) {
        for ( int x = 0; x < nodes.getLength(); x++ ) {
            Node node = nodes.item(x);
            if (node.getNodeName().equalsIgnoreCase(tagName)) {
                return node;
            }
        }

        return null;
    }
    
    public static synchronized String getNodeAttr(String tagName, String attrName, NodeList nodes ) {
       for ( int x = 0; x < nodes.getLength(); x++ ) {
           Node node = nodes.item(x);
           if (node.getNodeName().equalsIgnoreCase(tagName)) {
               return node.getAttributes().getNamedItem(attrName).getNodeValue();
               /*NodeList childNodes = node.getChildNodes();
               for (int y = 0; y < childNodes.getLength(); y++ ) {
                   Node data = childNodes.item(y);
                   if ( data.getNodeType() == Node.ATTRIBUTE_NODE ) {
                       if ( data.getNodeName().equalsIgnoreCase(attrName) )
                           return data.getNodeValue();
                   }
               }*/
           }
       }

       return "";
   }   
    
}
