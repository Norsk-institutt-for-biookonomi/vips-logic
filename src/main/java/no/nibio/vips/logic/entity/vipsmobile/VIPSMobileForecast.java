/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package no.nibio.vips.logic.entity.vipsmobile;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Rip-off from old VIPS VarselBO
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public final class VIPSMobileForecast implements Comparable
{
    private Integer DnmiSiteId;
    private Map<String, Object> props;

   /**
    * Lager et nytt varsel Business Object
    * @param varselId Unik identifikator for varsel
    * @param lovligVarselId Id for lovligVarsel
    * @param startDato Startdato for varselet
    * @param sluttDato Sluttdato for varselet
    * @param klimastasjonId Id for klimastasjon
    * @param feltId Id for felt
    * @param personlig Skiller offentlige og personlige varsel
    * @param inaktiv Angir om varselet er aktivt eller ikke
    * @param sistEndretAv Id til bruker som sist endret varselet
    * @param sistEndretDato Tid for siste endring
    * @param logiskSlettet Angir om varselet er logisk slettet
    */
   public VIPSMobileForecast
   (
      long varselId,
      long lovligVarselId,
      Date startDato,
      Date sluttDato,
      long klimastasjonId,
      String klimastasjonNavn,
      long feltId,
      String feltNavn,
      boolean personlig,
      boolean inaktiv,
      long sistEndretAv,
      Date sistEndretDato,
      boolean logiskSlettet,
      long organismeId,
      String organismeNavn,
      long kulturId,
      String kulturNavn,
      String modellId,
      String modellNavn
   )
   {
      setVarselId( varselId );
      setLovligVarselId( lovligVarselId );
      setStartDato( startDato );
      setSluttDato( sluttDato );
      setKlimastasjonId( klimastasjonId );
      setKlimastasjonNavn(klimastasjonNavn);
      setFeltId( feltId );
      setFeltNavn(feltNavn);
      setPersonlig( personlig );
      setInaktiv( inaktiv );
      setSistEndretAv( sistEndretAv );
      setSistEndretDato( sistEndretDato );
      setLogiskSlettet( logiskSlettet );
      setOrganismeId(organismeId);
      setOrganismeNavn(organismeNavn);
      setKulturId(kulturId);
      setKulturNavn(kulturNavn);
      setModellId(modellId);
      setModellNavn(modellNavn);
   }
   
   /**
    * @param varselId Unik identifikator for varsel
    */
   public final void setVarselId( long varselId )
   {
      setAttribute("varselId", varselId);
   }
   
   /**
    * @param lovligVarselId Id for lovligVarsel
    */
   public final void setLovligVarselId( long lovligVarselId )
   {
      setAttribute("lovligVarselId", lovligVarselId);
   }

   /**
    * @param startDato StartDato for varselet
    */
   public final void setStartDato( Date startDato )
   {
      setAttribute( "startDato", startDato );
   }
   
   /**
    * @param sluttDato SluttDato for varselet
    */
   public final void setSluttDato( Date sluttDato )
   {
      setAttribute( "sluttDato", sluttDato );
   }
   
   /**
    * @param klimastasjonId Id for klimastasjon
    */
   public final void setKlimastasjonId( long klimastasjonId )
   {
      setAttribute("klimastasjonId", klimastasjonId);
   }

   /**
    * @param feltId Id for felt
    */
   public final void setFeltId( long feltId )
   {
      setAttribute("feltId", feltId);
   }
   
   /**
    * @param personlig Personlig eller offentlig varsel
    */
   public final void setPersonlig( boolean personlig )
   {
      setAttribute("personlig", personlig);
   }

   /**
    * @param inaktiv Aktivt eller inaktivt varsel
    */
   public final void setInaktiv( boolean inaktiv )
   {
      setAttribute("inaktiv", inaktiv);
   }

   /**
    * @param sistEndretAv Id til bruker som sist endret varselet
    */
   public final void setSistEndretAv( long sistEndretAv )
   {
      setAttribute("sistEndretAv", sistEndretAv);
   }
   
   /**
    * @param sistEndretAvBrukernavn Brukernavn til bruker som sist endret varselet
    */
   public void setSistEndretAvBrukernavn( String sistEndretAvBrukernavn )
   {
      setAttribute( "sistEndretAvBrukernavn", sistEndretAvBrukernavn );
   }

   /**
    * @param sistEndretDato Tid for siste endring
    */
   public final void setSistEndretDato( Date sistEndretDato )
   {
      setAttribute( "sistEndretDato", sistEndretDato );
   }

   /**
    * @param logiskSlettet Slettet eller ikke slettet varsel
    */
   public final void setLogiskSlettet( boolean logiskSlettet )
   {
      setAttribute("logiskSlettet", logiskSlettet);
   }

   /**
    * @param klimastasjonNavn Navn for klimastasjon
    */
   public void setKlimastasjonNavn( String klimastasjonNavn )
   {
      setAttribute( "klimastasjonNavn", klimastasjonNavn );
   }
   
   /**
    * @param feltNavn Navn for felt
    */
   public void setFeltNavn( String feltNavn )
   {
      setAttribute( "feltNavn", feltNavn );
   }
   
   /**
    * @param gaardNavn Navn for gaard
    */
   public void setGaardNavn( String gaardNavn )
   {
      setAttribute( "gaardNavn", gaardNavn );
   }
   
   /**
    * @param skifteNavn Navn for skifte
    */
   public void setSkifteNavn( String skifteNavn )
   {
      setAttribute( "skifteNavn", skifteNavn );
   }

   /**
    * @param modellId ModellID
    */
   public void setModellId( String modellId )
   {
      setAttribute("modellId", modellId);
   }
   
   /**
    * @param modellNavn ModellNavn
    */
   public void setModellNavn( String modellNavn )
   {
      setAttribute( "modellNavn", modellNavn );
   }

   /**
    * @param modellWebNavn ModellNavn for VIPS-forsiden
    */
   public void setModellWebNavn( String modellWebNavn )
   {
      setAttribute( "modellWebNavn", modellWebNavn );
   }
   
   /**
    * @param kulturId KulturID
    */
   public void setKulturId( long kulturId )
   {
      setAttribute("kulturId", kulturId);
   }
   
   /**
    * @param skifteKulturId Id til den nyeste kultur som er registrert p� skifte
    */
   public void setSkifteKulturId( long skifteKulturId )
   {
      setAttribute("skifteKulturId", skifteKulturId);
   }
   
   /**
    * @param kulturNavn Kulturnavn
    */
   public void setKulturNavn( String kulturNavn )
   {
      setAttribute( "kulturNavn", kulturNavn );
   }

   /**
    * @param organismeId OrganismeID
    */
   public void setOrganismeId( long organismeId )
   {
      setAttribute("organismeId", organismeId);
   }

   /**
    * @param organismeNavn Organismenavn
    */
   public void setOrganismeNavn( String organismeNavn )
   {
      setAttribute( "organismeNavn", organismeNavn );
   }

      
   /**
    * @return Unik identifikator for varsel
    */
   public long getVarselId()
   {
      return ( ( Long )getAttribute( "varselId" ) );
   }
   
   /**
    * @return LovligVarsel Id
    */
   public long getLovligVarselId()
   {
      return ( ( Long )getAttribute( "lovligVarselId" ) );
   }

   /**
    * @return Startdato for varselet
    */
   public Date getStartDato()
   {
      return ( Date )getAttribute( "startDato" );
   }
   
      /**
    * @return Sluttdato for varselet
    */
   public Date getSluttDato()
   {
      return ( Date )getAttribute( "sluttDato" );
   }
   
   /**
    * @return Id for klimastasjon
    */
   public long getKlimastasjonId()
   {
      return ( ( Long )getAttribute( "klimastasjonId" ) );
   }

   /**
    * @return Id for felt
    */
   public long getFeltId()
   {
      return ( ( Long )getAttribute( "feltId" ) );
   }

   /**
    * @return personlig
    */
   public boolean getPersonlig()
   {
      return ( ( Boolean )getAttribute( "personlig" ) );
   }

   /**
    * @return inaktiv
    */
   public boolean getInaktiv()
   {
      return ( ( Boolean )getAttribute( "inaktiv" ) );
   }

   /**
    * @return sistEndretAv
    */
   public long getSistEndretAv()
   {
      return ( ( Long )getAttribute( "sistEndretAv" ) );
   }
   
   /**
    * @return sistEndretAvBrukernavn
    */
   public String getSistEndretAvBrukernavn()
   {
      return ( String )getAttribute( "sistEndretAvBrukernavn" );
   }

   /**
    * @return sistEndretDato
    */
   public Date getSistEndretDato()
   {
      return ( Date )getAttribute( "sistEndretDato" );
   }
   
   /**
    * @return True hvis varselet er slettet, false ellers
    */
   public boolean getLogiskSlettet()
   {
      if( getAttribute( "logiskSlettet" ) != null )
      {
         return ( ( Boolean )getAttribute( "logiskSlettet" ) );
      }
      else
      {
         return false;
      }
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getKlimastasjonNavn()
   {
      return ( String )getAttribute( "klimastasjonNavn" );
   }
   
   /**
    * @return Id for modell
    */
   public String getModellId()
   {
      return ( String )getAttribute( "modellId" ) ;
   }
   
   /**
    * @return Navn p� modell
    */
   public String getModellNavn()
   {
      return ( String )getAttribute( "modellNavn" );
   }
   
   /**
    * @return Navn paa modell til bruk p� VIPS-forsiden
    */
   public String getModellWebNavn()
   {
      return ( String )getAttribute( "modellWebNavn" );
   }
   
   /**
    * @return Id for kultur
    */
   public long getKulturId()
   {
      return ( ( Long )getAttribute( "kulturId" ) );
   }
   
   /**
    * @return Id for kultur
    */
   public long getSkifteKulturId()
   {
       Long retVal = ( Long )getAttribute( "skifteKulturId" );
       if(retVal == null) return -1;
       else return retVal;
   }
   
   /**
    * @return Navn på kultur
    */
   public String getKulturNavn()
   {
      return ( String )getAttribute( "kulturNavn" );
   }
   
   /**
    * @return Id for organisme
    */
   public long getOrganismeId()
   {
      return ( ( Long )getAttribute( "organismeId" ) );
   }
   
   /**
    * @return Navn p� organisme
    */
   public String getOrganismeNavn()
   {
      return ( String )getAttribute( "organismeNavn" );
   }

   
   /**
    * @return Navnet p� feltet varselet utf�res for
    */
   public String getFeltNavn()
   {
       return ( ( String )getAttribute( "feltNavn" ) );  
   }
   
   /**
    * @return Navnet p� g�rden varselet utf�res for
    */
   public String getGaardNavn()
   {
       return ( ( String )getAttribute( "gaardNavn" ) );  
   }
   
   /**
    * @return Navnet p� skiftet varselet utf�res for
    */
   public String getSkifteNavn()
   {
       return ( ( String )getAttribute( "skifteNavn" ) );  
   }
   
   

   /**
    * Sorterer alfabetisk p� klimastasjonNavn, hvis det er oppgitt
    * @param o
    * @return
    */
    @Override
    public int compareTo(Object o) {
        VIPSMobileForecast other = (VIPSMobileForecast) o;
        return this.compareTo(other);
    }

    /**
     * Sorterer alfabetisk p� G�rd, Skifte, Felt og klimastasjonNavn, hvis det er oppgitt. Deretter kronologisk p� startDato
     * @param other
     * @return
     */
    public int compareTo(VIPSMobileForecast other)
    {
        int retVal = 0;
        try
        {
            retVal = this.getGaardNavn().compareTo(other.getGaardNavn());
            if(retVal != 0) return retVal;

        }catch(NullPointerException npe){}

        try
        {
            retVal = this.getSkifteNavn().compareTo(other.getSkifteNavn());
            if(retVal != 0) return retVal;

        }catch(NullPointerException npe){}

        try
        {
            retVal = this.getFeltNavn().compareTo(other.getFeltNavn());
            if(retVal != 0) return retVal;

        }catch(NullPointerException npe){}

        try
        {
            retVal = this.getKlimastasjonNavn().compareTo(other.getKlimastasjonNavn());
            if(retVal != 0) return retVal;
        }catch(NullPointerException npe){}

        try
        {
            retVal = this.getStartDato().compareTo(other.getStartDato());
            if(retVal != 0) return retVal;
        }catch(NullPointerException npe){}
        
        
        return retVal;
    }

    /**
     * @return the DnmiSiteId
     */
    public Integer getDnmiSiteId() {
        return DnmiSiteId;
    }

    /**
     * @param DnmiSiteId the DnmiSiteId to set
     */
    public void setDnmiSiteId(Integer DnmiSiteId) {
        this.DnmiSiteId = DnmiSiteId;
    }

    
    
   private Object getAttribute(String key)
   {
       if(this.props != null)
       {
           return this.props.get(key);
       }
       else
       {
           return null;
       }
   }
   
   private void setAttribute(String key, Object value)
   {
       if(this.props == null)
       {
           this.props = new HashMap<>();
       }
       this.props.put(key, value);
   }
}
