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

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Rip off from old VIPS
 * &copy; 2015 NIBIO
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class VIPSMobilePresentation 
{
    
    private Map<String, Object> props;
   /**
    * Lager et nytt presentasjon Business Object
    * @param 
    */
   public VIPSMobilePresentation
   (
      long presentasjonId,
      Date dagensDato, 
      int nivaa,
      int varselFarge1,
      int varselFarge2,
      int varselFarge3,
      int varselFarge4,
      int varselFarge5,
      int varselFarge6,
      int varselFarge7,
      int varselFarge8,
      int varselFarge9,
      int varselFarge10,
      int varselFarge11,
      String varselTekst1,
      String varselTekst2,
      String varselTekst3,
      String varselTekst4,
      String varselTekst5,
      String varselTekst6,
      String varselTekst7,
      String varselTekst8,
      String varselTekst9,
      String varselTekst10,
      String varselTekst11,
      long klimastasjonId,
      long organismeId,
      long kulturId,
      long feltId,
      String modellId,
		long varselId,
      String klimastasjonNavn,
      String organismeNavn,
      String kulturNavn,
      String feltNavn,
      String modellNavn,
      String klimastasjonHjelpUrl,
      String modellHjelpUrl,
		boolean personlig ) {
		   this (
	       presentasjonId,
	       dagensDato, 
	       nivaa,
	       varselFarge1,
	       varselFarge2,
	       varselFarge3,
	       varselFarge4,
	       varselFarge5,
	       varselFarge6,
	       varselFarge7,
	       varselFarge8,
	       varselFarge9,
	       varselFarge10,
	       varselFarge11,
	       varselTekst1,
	       varselTekst2,
	       varselTekst3,
	       varselTekst4,
	       varselTekst5,
	       varselTekst6,
	       varselTekst7,
	       varselTekst8,
	       varselTekst9,
	       varselTekst10,
	       varselTekst11,
	       klimastasjonId,
	       organismeId,
	       kulturId,
	       feltId,
	       modellId,
	       klimastasjonNavn,
	       organismeNavn,
	       kulturNavn,
	       feltNavn,
	       modellNavn,
	       klimastasjonHjelpUrl,
	       modellHjelpUrl );
	       setVarselId( varselId );
			 setPersonlig( personlig );
		}
	
/**
    * Lager et nytt presentasjon Business Object
    * @param 
    */
   public VIPSMobilePresentation
   (
      long presentasjonId,
      Date dagensDato, 
      int nivaa,
      int varselFarge1,
      int varselFarge2,
      int varselFarge3,
      int varselFarge4,
      int varselFarge5,
      int varselFarge6,
      int varselFarge7,
      int varselFarge8,
      int varselFarge9,
      int varselFarge10,
      int varselFarge11,
      String varselTekst1,
      String varselTekst2,
      String varselTekst3,
      String varselTekst4,
      String varselTekst5,
      String varselTekst6,
      String varselTekst7,
      String varselTekst8,
      String varselTekst9,
      String varselTekst10,
      String varselTekst11,
      long klimastasjonId,
      long organismeId,
      long kulturId,
      long feltId,
      String modellId,
      String klimastasjonNavn,
      String organismeNavn,
      String kulturNavn,
      String feltNavn,
      String modellNavn,
      String klimastasjonHjelpUrl,
      String modellHjelpUrl
   )
   {
      setPresentasjonId( presentasjonId );
      setDagensDato( dagensDato );
      setNivaa( nivaa );
      setVarselFarge1( varselFarge1 );
      setVarselFarge2( varselFarge2 );
      setVarselFarge3( varselFarge3 );
      setVarselFarge4( varselFarge4 );
      setVarselFarge5( varselFarge5 );
      setVarselFarge6( varselFarge6 );
      setVarselFarge7( varselFarge7 );
      setVarselFarge8( varselFarge8 );
      setVarselFarge9( varselFarge9 );
      setVarselFarge10( varselFarge10 );
      setVarselFarge11( varselFarge11 );
      setVarselTekst1( varselTekst1 );
      setVarselTekst2( varselTekst2 );
      setVarselTekst3( varselTekst3 );
      setVarselTekst4( varselTekst4 );
      setVarselTekst5( varselTekst5 );
      setVarselTekst6( varselTekst6 );
      setVarselTekst7( varselTekst7 );
      setVarselTekst8( varselTekst8 );
      setVarselTekst9( varselTekst9 );
      setVarselTekst10( varselTekst10 );
      setVarselTekst11( varselTekst11 );
      setKlimastasjonId( klimastasjonId );
      setOrganismeId( organismeId );
      setKulturId( kulturId );
      setFeltId( feltId );
      setModellId( modellId );
      setKlimastasjonNavn( klimastasjonNavn );
      setOrganismeNavn( organismeNavn );
      setKulturNavn( kulturNavn );
      setFeltNavn( feltNavn );
      setModellNavn( modellNavn );
      setKlimastasjonHjelpUrl( klimastasjonHjelpUrl );
      setModellHjelpUrl( modellHjelpUrl );
		setVarselId( -1 );
		setPersonlig( false );
   }

   /**
    * @param varselId Unik identifikator for presentasjon
    */
   public final void setPresentasjonId( long presentasjonId )
   {
      setAttribute("presentasjonId", presentasjonId);
   }

   /**
    * @param varselId Unik identifikator for presentasjon
    */
   public final void setPersonlig( boolean personlig )
   {
      setAttribute("personlig", personlig);
   }

   /**
    * @param dagensDato Dato presentasjonen gjelder for
    */
   public final void setDagensDato( Date dagensDato )
   {
      setAttribute( "dagensDato", dagensDato );
   }   
   
   /**
    * @param nivaa Nivaa for presentasjon
    */
   public final void setNivaa( int nivaa )
   {
      setAttribute("nivaa", nivaa);
   }

   /**
    * @param varselFarge1 Status for farge
    */
   public final void setVarselFarge1( int varselFarge1 )
   {
      setAttribute("varselFarge1", varselFarge1);
   }

   /**
    * @param varselFarge2 Status for farge
    */
   public final void setVarselFarge2( int varselFarge2 )
   {
      setAttribute("varselFarge2", varselFarge2);
   }

   /**
    * @param varselFarge3 Status for farge
    */
   public final void setVarselFarge3( int varselFarge3 )
   {
      setAttribute("varselFarge3", varselFarge3);
   }

   /**
    * @param varselFarge4 Status for farge
    */
   public final void setVarselFarge4( int varselFarge4 )
   {
      setAttribute("varselFarge4", varselFarge4);
   }

   /**
    * @param varselFarge5 Status for farge
    */
   public final void setVarselFarge5( int varselFarge5 )
   {
      setAttribute("varselFarge5", varselFarge5);
   }

   /**
    * @param varselFarge6 Status for farge
    */
   public final void setVarselFarge6( int varselFarge6 )
   {
      setAttribute("varselFarge6", varselFarge6);
   }

   /**
    * @param varselFarge7 Status for farge
    */
   public final void setVarselFarge7( int varselFarge7 )
   {
      setAttribute("varselFarge7", varselFarge7);
   }

   /**
    * @param varselFarge8 Status for farge
    */
   public final void setVarselFarge8( int varselFarge8 )
   {
      setAttribute("varselFarge8", varselFarge8);
   }

   /**
    * @param varselFarge9 Status for farge
    */
   public final void setVarselFarge9( int varselFarge9 )
   {
      setAttribute("varselFarge9", varselFarge9);
   }

   /**
    * @param varselFarge10 Status for farge
    */
   public final void setVarselFarge10( int varselFarge10 )
   {
      setAttribute("varselFarge10", varselFarge10);
   }

   /**
    * @param varselFarge11 Status for farge
    */
   public final void setVarselFarge11( int varselFarge11 )
   {
      setAttribute("varselFarge11", varselFarge11);
   }

   /**
    * @param varselTekst1 Tekst for varsel rute
    */
   public final void setVarselTekst1( String varselTekst1 )
   {
      setAttribute( "varselTekst1", varselTekst1 );
   }


   /**
    * @param varselTekst2 Tekst for varsel rute
    */
   public final void setVarselTekst2( String varselTekst2 )
   {
      setAttribute( "varselTekst2", varselTekst2 );
   }

   /**
    * @param varselTekst3 Tekst for varsel rute
    */
   public final void setVarselTekst3( String varselTekst3 )
   {
      setAttribute( "varselTekst3", varselTekst3 );
   }

   /**
    * @param varselTekst4 Tekst for varsel rute
    */
   public final void setVarselTekst4( String varselTekst4 )
   {
      setAttribute( "varselTekst4", varselTekst4 );
   }

   /**
    * @param varselTekst5 Tekst for varsel rute
    */
   public final void setVarselTekst5( String varselTekst5 )
   {
      setAttribute( "varselTekst5", varselTekst5 );
   }

   /**
    * @param varselTekst6 Tekst for varsel rute
    */
   public final void setVarselTekst6( String varselTekst6 )
   {
      setAttribute( "varselTekst6", varselTekst6 );
   }

   /**
    * @param varselTekst7 Tekst for varsel rute
    */
   public final void setVarselTekst7( String varselTekst7 )
   {
      setAttribute( "varselTekst7", varselTekst7 );
   }

   /**
    * @param varselTekst8 Tekst for varsel rute
    */
   public final void setVarselTekst8( String varselTekst8 )
   {
      setAttribute( "varselTekst8", varselTekst8 );
   }

   /**
    * @param varselTekst9 Tekst for varsel rute
    */
   public final void setVarselTekst9( String varselTekst9 )
   {
      setAttribute( "varselTekst9", varselTekst9 );
   }

   /**
    * @param varselTekst10 Tekst for varsel rute
    */
   public final void setVarselTekst10( String varselTekst10 )
   {
      setAttribute( "varselTekst10", varselTekst10 );
   }

   /**
    * @param varselTekst11 Tekst for varsel rute
    */
   public final void setVarselTekst11( String varselTekst11 )
   {
      setAttribute( "varselTekst11", varselTekst11 );
   }

   /**
    * @param klimastasjonId Id for klimastasjon
    */
   public final void setKlimastasjonId( long klimastasjonId )
   {
      setAttribute("klimastasjonId", klimastasjonId);
   }

   /**
    * @param organismeId Id for organisme
    */
   public final void setOrganismeId( long organismeId )
   {
      setAttribute("organismeId", organismeId);
   }

   /**
    * @param kulturId Id for kultur
    */
   public final void setKulturId( long kulturId )
   {
      setAttribute("kulturId", kulturId);
   }

   /**
    * @param feltId Id for felt
    */
   public final void setFeltId( long feltId )
   {
      setAttribute("feltId", feltId);
   }

   /**
    * @param modellId Id for modell
    */
   public final void setModellId( String modellId )
   {
      setAttribute( "modellId", modellId );
   }

	/**
    * @param modellId Id for modell
    */
   public final void setVarselId( long varselId )
   {
      setAttribute("varselId", varselId);
   }

   /**
    * @param klimastasjonNavn Navn for klimastasjon
    */
   public final void setKlimastasjonNavn( String klimastasjonNavn )
   {
      setAttribute( "klimastasjonNavn", klimastasjonNavn );
   }

   /**
    * @param organismeNavn Navn for organisme
    */
   public final void setOrganismeNavn( String organismeNavn )
   {
      setAttribute( "organismeNavn", organismeNavn );
   }

   /**
    * @param kulturNavn Navn for kultur
    */
   public final void setKulturNavn( String kulturNavn )
   {
      setAttribute( "kulturNavn", kulturNavn );
   }

   /**
    * @param feltNavn Navn for felt
    */
   public final void setFeltNavn( String feltNavn )
   {
      setAttribute( "feltNavn", feltNavn );
   }

   /**
    * @param modellNavn Navn for modell
    */
   public final void setModellNavn( String modellNavn )
   {
      setAttribute( "modellNavn", modellNavn );
   }
   
   /**
    * @param modellNavn Navn for modell til bruk p� VIPS-forsiden
    */
   public void setModellWebNavn( String modellWebNavn )
   {
      setAttribute( "modellWebNavn", modellWebNavn );
   }

   /**
    * @param klimastasjonHjelpUrl Hjelp url for klimastasjon
    */
   public final void setKlimastasjonHjelpUrl( String klimastasjonHjelpUrl )
   {
      setAttribute( "klimastasjonHjelpUrl", klimastasjonHjelpUrl );
   }

   /**
    * @param modellHjelpUrl Hjelp url for modell
    */
   public final void setModellHjelpUrl( String modellHjelpUrl )
   {
      setAttribute( "modellHjelpUrl", modellHjelpUrl );
   }

      
   /**
    * @return Unik identifikator for varsel
    */
   public long getPresentasjonId()
   {
      return ( ( Long )getAttribute( "presentasjonId" ) );
   }

	/**
    * @return Unik identifikator for varsel
    */
   public boolean getPersonlig()
   {
      return ( ( Boolean )getAttribute( "personlig" ) );
   }

   /**
    * @return  dagensDato 
    */
//   @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd")
   @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ssXXX", timezone="UTC")
   //@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="dd.MM.yyyy", timezone="UTC")
   public Date getDagensDato()
   {
       return ( (Date)getAttribute( "dagensDato" ) );  
   }   
   
   /**
    * @return Nivaa for presentasjon
    */
   public int getNivaa()
   {
      return ( ( Integer )getAttribute( "nivaa" ) );
   }

   /**
    * @return Farge for varsel
    */
   public int getVarselFarge1()
   {
      return ( ( Integer )getAttribute( "varselFarge1" ) );
   }

   /**
    * @return Farge for varsel
    */
   public int getVarselFarge2()
   {
      return ( ( Integer )getAttribute( "varselFarge2" ) );
   }

   /**
    * @return Farge for varsel
    */
   public int getVarselFarge3()
   {
      return ( ( Integer )getAttribute( "varselFarge3" ) );
   }

   /**
    * @return Farge for varsel
    */
   public int getVarselFarge4()
   {
      return ( ( Integer )getAttribute( "varselFarge4" ) );
   }

   /**
    * @return Farge for varsel
    */
   public int getVarselFarge5()
   {
      return ( ( Integer )getAttribute( "varselFarge5" ) );
   }

   /**
    * @return Farge for varsel
    */
   public int getVarselFarge6()
   {
      return ( ( Integer )getAttribute( "varselFarge6" ) );
   }

   /**
    * @return Farge for varsel
    */
   public int getVarselFarge7()
   {
      return ( ( Integer )getAttribute( "varselFarge7" ) );
   }

   /**
    * @return Farge for varsel
    */
   public int getVarselFarge8()
   {
      return ( ( Integer )getAttribute( "varselFarge8" ) );
   }

   /**
    * @return Farge for varsel
    */
   public int getVarselFarge9()
   {
      return ( ( Integer )getAttribute( "varselFarge9" ) );
   }

   /**
    * @return Farge for varsel
    */
   public int getVarselFarge10()
   {
      return ( ( Integer )getAttribute( "varselFarge10" ) );
   }

   /**
    * @return Farge for varsel
    */
   public int getVarselFarge11()
   {
      return ( ( Integer )getAttribute( "varselFarge11" ) );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getVarselTekst1()
   {
      return ( String )getAttribute( "varselTekst1" );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getVarselTekst2()
   {
      return ( String )getAttribute( "varselTekst2" );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getVarselTekst3()
   {
      return ( String )getAttribute( "varselTekst3" );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getVarselTekst4()
   {
      return ( String )getAttribute( "varselTekst4" );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getVarselTekst5()
   {
      return ( String )getAttribute( "varselTekst5" );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getVarselTekst6()
   {
      return ( String )getAttribute( "varselTekst6" );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getVarselTekst7()
   {
      return ( String )getAttribute( "varselTekst7" );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getVarselTekst8()
   {
      return ( String )getAttribute( "varselTekst8" );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getVarselTekst9()
   {
      return ( String )getAttribute( "varselTekst9" );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getVarselTekst10()
   {
      return ( String )getAttribute( "varselTekst10" );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getVarselTekst11()
   {
      return ( String )getAttribute( "varselTekst11" );
   }

   /**
    * @return Id for klimastasjon
    */
   public long getKlimastasjonId()
   {
      return ( ( Long )getAttribute( "klimastasjonId" ) );
   }

   /**
    * @return Id for organisme
    */
   public long getOrganismeId()
   {
      return ( ( Long )getAttribute( "organismeId" ) );
   }

   /**
    * @return Id for kultur
    */
   public long getKulturId()
   {
      return ( ( Long )getAttribute( "kulturId" ) );
   }

   /**
    * @return Id for felt
    */
   public long getFeltId()
   {
      return ( ( Long )getAttribute( "feltId" ) );
   }

   /**
    * @return Id for modell
    */
   public String getModellId()
   {
      return ( String )getAttribute( "modellId" );
   }

	/**
    * @return Id for modell
    */
   public long getVarselId()
   {
      return ( ( Long )getAttribute( "varselId" ) );
   }

   /**
    * @return Navn for klimastasjon
    */
   public String getKlimastasjonNavn()
   {
      return ( String )getAttribute( "klimastasjonNavn" );
   }

   /**
    * @return Navn for organisme
    */
   public String getOrganismeNavn()
   {
      return ( String )getAttribute( "organismeNavn" );
   }

   /**
    * @return Navn for kultur
    */
   public String getKulturNavn()
   {
      return ( String )getAttribute( "kulturNavn" );
   }

   /**
    * @return Navn for modell
    */
   public String getFeltNavn()
   {
      return ( String )getAttribute( "feltNavn" );
   }

   /**
    * @return Navn for modell
    */
   public String getModellNavn()
   {
      return ( String )getAttribute( "modellNavn" );
   }
   
   /**
    * @return Navn for modell til bruk paa VIPS-forsiden
    */
   public String getModellWebNavn()
   {
      return ( String )getAttribute( "modellWebNavn" );
   }

   /**
    * @return Hjelp url for klimastasjon
    */
   public String getKlimastasjonHjelpUrl()
   {
      return ( String )getAttribute( "klimastasjonHjelpUrl" );
   }

   /**
    * @return Hjelp url for modell
    */
   public String getModellHjelpUrl()
   {
      return ( String )getAttribute( "modellHjelpUrl" );
   }
   
 
   public boolean getLeaf()
   {
       return true;
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
