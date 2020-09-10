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
	<script type="text/javascript" src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
	<script src="//code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
	<script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
	<script type="text/javascript">
		// Make sure that there is a date picker present for HTML5 
		// date input fields
		if (!Modernizr.inputtypes.date) {
		    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
		}
		// Load main form definition (for validation)
		loadFormDefinition("seasonTrapsiteForm","/formdefinitions/modules/barkbeetle/");
		function updateMarkerPosition(){
			console.info("TODO: updateMarkerPosition");
		}
	</script>
</#macro>
<#macro custom_css>
	<link href="//code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
</#macro>
<#macro page_contents>
	<#if messageKey?has_content>
		<div class="alert alert-success">Data ble lagret ${.now}</div>
	</#if>
	<h1><#if seasonTrapsite.seasonTrapsiteId?has_content>Rediger<#else>Ny</#if> fellelokalitet</h1>
	<p>
		<a href="/barkbeetle?season=${season}" class="btn btn-default back" role="button">${i18nBundle.back}</a>
		<#if seasonTrapsite.seasonTrapsiteId?has_content>
		<a href="/barkbeetle?action=registerData&seasonTrapsiteId=${seasonTrapsite.seasonTrapsiteId}" class="btn btn-default" role="button">Registrer data</a>
		</#if>
	</p>
<div class="singleBlockContainer">
	<div class="row">
	<div class="col-md-8">
<#assign formId = "seasonTrapsiteForm">
	<form id="${formId}" role="form" action="/barkbeetle?action=seasonTrapsiteFormSubmit" method="POST" onsubmit="return validateForm(this);"">
		<input type="hidden" name="seasonTrapsiteId" value="${seasonTrapsite.seasonTrapsiteId!"-1"}"/>
		<div class="form-group">
	    <label for="trapsiteType">Type fellelokalitet</label>
	    <select class="form-control" name="trapsiteTypeId" onblur="validateField(this);">
			<#list seasonTrapsiteTypes as trapsite>
				<option value="${trapsite.trapsiteTypeId}"
				<#if (seasonTrapsite.trapsiteType?has_content && seasonTrapsite.trapsiteType.trapsiteTypeId == trapsite.trapsiteTypeId) 
				||  (!(seasonTrapsite.trapsiteType?has_content) && trapsite.trapsiteTypeId == 1)>
				selected="selected"</#if>
				>${trapsite.title}</option>
			</#list>
		
	     </select>
	</div>
	<div class="form-group">
			<label for="season">Sesong</label>
			<input type="text" class="form-control" name="season" placeholder="Sesong" value="${season}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_season_validation"></span>
		  </div>
		<div class="form-group">
			<label for="gisGeom">Lokalitet (<a href="http://en.wikipedia.org/wiki/World_Geodetic_System#A_new_World_Geodetic_System:_WGS_84" target="new">WGS84</a>: lengdegrad, breddegrad. <a href="https://coordinates-converter.com/" target="new">Her kan du konvertere automatisk.</a>)</label>
			<input type="text" class="form-control" id="gisGeom" name="gisGeom" placeholder="Lokalitet" value="${(seasonTrapsite.longitude?c)!""},${(seasonTrapsite.latitude?c)!""}" onblur="validateField(this);" onchange="if(validateField(this)){updateMarkerPosition();}" />
			<span class="help-block" id="${formId}_location_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="altitude">Høyde over havet (m)</label>
			<input type="number" class="form-control" name="altitude" placeholder="Høyde over havet" value="${(seasonTrapsite.altitude?c)!""}" onblur="validateField(this);"/>
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
			<label for="ownerName">Eier av lokalitet</label>
			<input type="text" class="form-control" name="ownerName" placeholder="Eier av lokalitet" value="${(seasonTrapsite.ownerName)!""}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_ownerName_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="ownerName">Tlf til eier av lokalitet</label>
			<input type="text" class="form-control" name="ownerPhone" placeholder="Tlf til eier av lokalitet" value="${(seasonTrapsite.ownerPhone)!""}" onblur="validateField(this);"/>
			<span class="help-block" id="${formId}_ownerPhone_validation"></span>
		  </div>
		  <div class="form-group">
			<label for="userId">Registrant</label>
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
		  <div class="form-group">
				<label for="dateInstalled">Dato oppmontering</label>
				<input type="date" class="form-control" name="dateInstalled" placeholder="Dato oppmontering" value="${(seasonTrapsite.dateInstalled?date)!""}" onblur="validateField(this);" />
				<span class="help-block" id="${formId}_dateInstalled_validation"></span>
		  </div>
		  <div class="form-group">
				<label for="installationRemarks">Kommentar fellemontering</label>
				<textarea class="form-control" name="installationRemarks" placeholder="Kommentar fellemontering" onblur="validateField(this);" >${(seasonTrapsite.installationRemarks)!""}</textarea>
				<span class="help-block" id="${formId}_installationRemarks_validation"></span>
		  </div>
		  <button type="submit" class="btn btn-default">Lagre</button>
	</form>
	</div>
	<div class="col-md-4">
	Kartet kommer her!
	</div>
	</div>
</div>
</#macro>
<@page_html/>