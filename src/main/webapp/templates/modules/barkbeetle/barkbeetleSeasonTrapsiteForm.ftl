<#--
    Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
  
  This file is part of VIPSLogic.
  VIPSLogic is free software: you can redistribute it and/or modify
  it under the terms of the NIBIO Open Source License as published by 
  NIBIO, either version 1 of the License, or (at your option) any
  later version.
  
  VIPSLogic is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  NIBIO Open Source License for more details.
  
  You should have received a copy of the NIBIO Open Source License
  along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
	
--><#include "../../master.ftl">
<#macro page_head>
	<title>Barkbilleregistrering: Rediger fellelokalitet for ${season}</title>
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
	<script type="text/javascript" src="/js/modules/barkbeetle/seasonTrapsiteFormMap.js"></script>
	<script type="text/javascript" src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
	<script src="//code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
	<script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
	<script type="text/javascript" src="/js/3rdparty/proj4.js"></script>
	<script type="text/javascript">
		var theForm = document.getElementById("${formId}"); 
		$(document).ready(function() {
			// Make sure that there is a date picker present for HTML5 
			// date input fields
			if (!Modernizr.inputtypes.date) {
				$('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
			}
			// Load main form definition (for validation)
			loadFormDefinition("seasonTrapsiteForm","/formdefinitions/modules/barkbeetle/");
			initMap(${season},${seasonTrapsite.seasonTrapsiteId!"null"},[${(seasonTrapsite.longitude?c)!"0"},${(seasonTrapsite.latitude?c)!"0"}]);
		});
		
		
		function handleLocationChanged(locationField){
			// Input control first
			var lonLat = locationField.value.split(",");
			if(lonLat.length != 2) return;
			var lon = parseFloat(lonLat[0]);
			var lat = parseFloat(lonLat[1]);
			if(isNaN(lon) || isNaN(lat)) return;
			
			// Get altitude from the startkart web service
			fetch("/corsproxy/http://openwps.statkart.no/skwms1/wps.elevation2?request=Execute&service=WPS&version=1.0.0&identifier=elevation&datainputs=lat=" + lat + ";lon=" + lon + ";epsg=4326")
			.then(response => response.text())
			.then(str => (new window.DOMParser()).parseFromString(str,"text/xml"))
			.then(xmlDoc => {
					var outputs = xmlDoc.getElementsByTagName("wps:Output");
					for(var i=0; i<outputs.length;i++)
					{
						if(outputs[i].getElementsByTagName("ows:Identifier")[0].textContent.trim() == "elevation")
						{
							var elevation = parseFloat(outputs[i].getElementsByTagName("wps:LiteralData")[0].textContent.trim());
							if(isNaN(elevation)) 
							{
								elevation = 0;
							}
							theForm["altitude"].value = elevation;
						}
					}
					
			});
			// Get County and municipality
			fetch("/corsproxy/https://proxy1.nibio.no/municipality_cache_ws/kommuneListe/N2000/4326/2019?lon=" + lon + "&lat=" + lat)
			.then(response => response.json())
			.then(municInfo =>{
					if(municInfo.length == 0)
					{
						alert("Posisjonen er utenfor Norges grenser. Vennligst korriger dette");
						theForm["countyNo"].value = 0;
						theForm["countyName"].value = "Utlandet";
						theForm["municipalityNo"].value = 0;
						theForm["municipalityName"].value = "Utlandet";
					}
					else
					{
						theForm["countyNo"].value=municInfo[0]["fylkenr"];
						theForm["countyName"].value=municInfo[0]["fylkenavn"];
						theForm["municipalityNo"].value=municInfo[0]["komnr"];
						theForm["municipalityName"].value=municInfo[0]["komnavn"];
					}
				}
			)
			.catch( error => console.info(error));;
			
		}
		
		proj4.defs([
			  [
				'EPSG:2077',
				'+proj=utm +zone=32 +ellps=intl +units=m +no_defs '],
			  [
				'EPSG:2078',
				'+proj=utm +zone=33 +ellps=intl +units=m +no_defs '
			  ]
			]);
		
		function convertProjection()
		{
			var projFromEPSG = document.getElementById("projFromEPSG").options[document.getElementById("projFromEPSG").selectedIndex].value;
			var wgs84coord = proj4(projFromEPSG, "WGS84", [document.getElementById("projectionConversionTool_lon").value, document.getElementById("projectionConversionTool_lat").value]);
			var gisGeomField = document.getElementById("gisGeom"); 
			gisGeomField.value = wgs84coord[0] + "," + wgs84coord[1];
			handleLocationChanged(gisGeomField);
			
		}
	</script>
</#macro>
<#macro custom_css>
	<link href="//code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
	<style type="text/css">
		#seasonTrapsiteFormMap {
			height: 600px;	
		}
	</style>
</#macro>
<#macro page_contents>
	<#if messageKey?has_content>
		<div class="alert alert-success">Data ble lagret ${.now}</div>
	</#if>
	<p>
		<a href="/barkbeetle?season=${season}" class="btn btn-default back" role="button">${i18nBundle.back}</a>
	</p>
	<h1><#if seasonTrapsite.seasonTrapsiteId?has_content>Rediger<#else>Ny</#if> fellelokalitet</h1>
	<div class="row">
		<div class="col-md-11">
		<p>Alle feller må være av typen BEKA (=Nove, se bildet) og feromon av typen 
		<a href="http://kjemikonsult.no/Norsk/Produkter.html" target="new">Ipslure</a>
		(=Norlure; tilsendt fra Nibio). Plasser lokaliteten med et trykk 
		på kartet, så genereres koordinater og flere av feltene automatisk. Alle 
		øvrige felt med stjerne må fylles inn. Fyll også inn dato og kommentar 
		nederst i skjemaet dersom montering av feller skjer senere enn 20. april.
		Se utdypende informasjon i 
		<a href="/images/modules/barkbeetle/Instruks_registranter_i_Barkbilleovervakingen.pdf" target="new">Instruks registranter</a> 
		og <a href="/images/modules/barkbeetle/Montering_av_barkbillefelle.pdf">Monteringsanvisning
	Beka-felle</a>
	   </p>
		<p>
			<#if seasonTrapsite.seasonTrapsiteId?has_content>
			<a href="/barkbeetle?action=registerData&seasonTrapsiteId=${seasonTrapsite.seasonTrapsiteId}" class="btn btn-default" role="button">Registrer data</a>
			</#if>
		</p>
		</div>
		<div class="col-md-1">
		<img src="/images/modules/barkbeetle/Bilde_BEKA_felle.jpg" style="height: 150px;"/>
		</div>
	</div>
	
<div class="singleBlockContainer">
	<div class="row">
	<div class="col-md-8">
<#assign formId = "seasonTrapsiteForm">
	<p>Felt merket med * må være utfylt</p>
	<form id="${formId}" role="form" action="/barkbeetle?action=seasonTrapsiteFormSubmit" method="POST" onsubmit="return validateForm(this);"">
		<input type="hidden" name="seasonTrapsiteId" value="${seasonTrapsite.seasonTrapsiteId!"-1"}"/>
		<div class="form-group">
	    <label for="trapsiteType">Type fellelokalitet *</label>
	    <select class="form-control" name="trapsiteTypeId" onblur="validateField(this);">
			<#list seasonTrapsiteTypes as trapsite>
				<option value="${trapsite.trapsiteTypeId}"
				<#if (seasonTrapsite.trapsiteType?has_content && seasonTrapsite.trapsiteType.trapsiteTypeId == trapsite.trapsiteTypeId) 
				||  (!(seasonTrapsite.trapsiteType?has_content) && trapsite.trapsiteTypeId == 1)>
				selected="selected"</#if>
				>${trapsite.description}</option>
			</#list>
		
	     </select>
	</div>
	<div class="form-group">
			<label for="season">Sesong *</label>
			<input type="text" class="form-control" name="season" placeholder="Sesong" value="${season}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_season_validation"></span>
		  </div>
		<div class="form-group">
			<label for="gisGeom">
			Lokalitet * angitt i <a href="http://en.wikipedia.org/wiki/World_Geodetic_System#A_new_World_Geodetic_System:_WGS_84" target="new">WGS84</a>: lengdegrad, breddegrad. 
			<button class="btn btn-default" type="button" onclick="document.getElementById('projectionConversionTool').style.display='inline';">Vis konverteringsverktøy</button><br/>
			<div id="projectionConversionTool" style="display: none;">
				<select name="projection" id="projFromEPSG">
					<option value="EPSG:2077">UTM Sone 32N</option>
					<option value="EPSG:2078">UTM Sone 33N</option>
				</select>
				<input type="number" name="projectionConversionTool_lon" id="projectionConversionTool_lon" value="Lengdegrad/E" placeholder="Lengdegrad/E"/><input type="number" name="projectionConversionTool_lat" id="projectionConversionTool_lat" value="Breddegrad/N" placeholder="Breddegrad/N"/>
				<button class="btn btn-default" type="button" onclick="convertProjection();">Konverter til WGS84</button>
			</div>
			</label>
			<input type="text" class="form-control" id="gisGeom" name="gisGeom" placeholder="Lokalitet" value="${(seasonTrapsite.longitude?c)!""},${(seasonTrapsite.latitude?c)!""}" onblur="validateField(this);" onchange="if(validateField(this)){handleLocationChanged(this);updateMarkerPosition();}" />
			<span class="help-block" id="${formId}_gisGeom_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="altitude">Høyde over havet (m)</label>
			<input type="number" class="form-control" name="altitude" step="0.1" placeholder="Høyde over havet" value="${(seasonTrapsite.altitude?c)!""}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_altitude_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="countyNo">Fylkesnr</label>
			<input type="number" class="form-control" name="countyNo" placeholder="Fylkesnr" value="${(seasonTrapsite.countyNo)!""}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_countyNo_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="countyName">Fylkesnavn</label>
			<input type="text" class="form-control" name="countyName" placeholder="Fylkesnavn" value="${(seasonTrapsite.countyName)!""}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_countyName_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="municipalityNo">Kommunenr</label>
			<input type="number" class="form-control" name="municipalityNo" placeholder="Kommunenr" value="${(seasonTrapsite.municipalityNo)!""}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_municipalityNo_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="municipalityName">Kommunenavn</label>
			<input type="text" class="form-control" name="municipalityName" placeholder="Kommunenavn" value="${(seasonTrapsite.municipalityName)!""}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_municipalityName_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="propertyNo">Gårdsnr</label>
			<input type="number" class="form-control" name="propertyNo" placeholder="Gårdsnr" value="${(seasonTrapsite.propertyNo)!""}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_propertyNo_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="propertySectionNo">Bruksnr</label>
			<input type="number" class="form-control" name="propertySectionNo" placeholder="Bruksnr" value="${(seasonTrapsite.propertySectionNo)!""}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_propertySectionNo_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="ownerName">Eier av lokalitet*</label>
			<input type="text" class="form-control" name="ownerName" placeholder="Eier av lokalitet" value="${(seasonTrapsite.ownerName)!""}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_ownerName_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="ownerName">Tlf til eier av lokalitet</label>
			<input type="text" class="form-control" name="ownerPhone" placeholder="Tlf til eier av lokalitet" value="${(seasonTrapsite.ownerPhone)!""}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_ownerPhone_validation"></span>
		  </div>
		  <#if userIsAdmin>
		  <div class="form-group">
			<label for="userId">Registrant*</label>
			<select class="form-control" name="userId">
			<option value="-1">-- Velg registrant --</option>
			<#list trapsiteRegistrators as registrator>
				<option value="${registrator.userId}"
				<#if seasonTrapsite.userId?has_content>
					<#if seasonTrapsite.userId.userId == registrator.userId>
					selected="selected"
					</#if>
				<#elseif user.userId == registrator.userId>
					selected="selected"
				</#if>
				>${registrator.lastName}, ${registrator.firstName}</option>
			</#list>
			</select>
		  </div>
		  <#else>
		  <input type="hidden" name="userId" value="${user.userId}"/>
		  </#if>
		  <div class="form-group">
				<label for="dateInstalled">Dato oppmontering (innen 20.04)</label>
				<input type="date" class="form-control" name="dateInstalled" placeholder="Dato oppmontering" value="${(seasonTrapsite.dateInstalled?date)!""}" onblur="validateField(this);" />
				<span class="help-block" id="${formId}_dateInstalled_validation"></span>
		  </div>
		  <div class="form-group">
				<label for="installationRemarks">Kommentar fellemontering (for eksempel avvik grunnet værforhold)</label>
				<textarea class="form-control" name="installationRemarks" placeholder="Kommentar fellemontering" onblur="validateField(this);" >${(seasonTrapsite.installationRemarks)!""}</textarea>
				<span class="help-block" id="${formId}_installationRemarks_validation"></span>
		  </div>
		  <button type="submit" class="btn btn-default">Lagre</button>
	</form>
	</div>
	<div class="col-md-4">
		<div id="seasonTrapsiteFormMap" class="map">
				<div id="popover"></div>
			</div>
	</div>
	</div>
	<div style="display: none;"><div id="trapMarker" title="Marker"><img src="/images/modules/barkbeetle/trapsite.png"/></div></div>
</div>
</#macro>
<@page_html/>