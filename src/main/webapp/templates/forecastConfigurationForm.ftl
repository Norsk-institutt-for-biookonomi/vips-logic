<#-- 
    Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
  
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
--><#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.viewForecastConfiguration}</title>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script src="/js/forecastConfigurationForm.js"></script>
	<script src="/js/validateForm.js"></script>
	<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
	<link href="http://code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
	<script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
	<script type="text/javascript" src="/js/3rdparty/moment.min.js"></script>
	<script type="text/javascript">
		// Make sure that there is a date picker present for HTML5 
		// date input fields
		if (!Modernizr.inputtypes.date) {
		    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
		}
		
		// Load main form definition (for validation)
		loadFormDefinition("${formId}");
		
		// Show specific fields for model
		if(document.getElementById('modelId').options.selectedIndex > 0)
		{
			renderModelSpecificFields("${formId}");
		}
	</script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
	<p><a href="/forecastConfiguration" class="btn btn-default back" role="button">${i18nBundle.back}</a></p>
        <h1>${i18nBundle.viewForecastConfiguration}</h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <form id="${formId}" role="form" action="/forecastConfiguration?action=forecastConfigurationFormSubmit" method="POST" onsubmit2="return false;" onsubmit="try{ return (validateForm(this) && validateForm(this, this.modelId.options[this.modelId.selectedIndex].value));}catch(err){alert(err);return false;}">
          <input type="hidden" id="forecastConfigurationId" name="forecastConfigurationId" value="${forecastConfiguration.forecastConfigurationId!"-1"}"/>
          <div class="form-group">
	    <label for="modelId">${i18nBundle.modelId}</label>
	    <select class="form-control" id="modelId" name="modelId" onblur="validateField(this);" onchange="renderModelSpecificFields('${formId}');">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.modelId?lower_case}</option>
	    	<#list modelInformations as modelInformation>
	    	<option value="${modelInformation.modelId}"<#if forecastConfiguration.modelId?has_content && modelInformation.modelId == forecastConfiguration.modelId> selected="selected"</#if>>
	    		<#if i18nBundle.containsKey(modelInformation.modelId)>
				${i18nBundle[modelInformation.modelId]}
			<#else>
				${modelInformation.defaultName}
			</#if>
		</option>
	    	</#list>
	    </select>
	    <span class="help-block" id="${formId}_modelId_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="cropOrganismId">${i18nBundle.cropOrganismId}</label>
	    <select class="form-control" id="cropOrganismId" name="cropOrganismId" onblur="validateField(this);" onchange="renderModelSpecificFields('${formId}');">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.cropOrganismId?lower_case}</option>
	    	<#list allCrops?sort_by("latinName") as organism>
			<option value="${organism.organismId}"
				<#if (forecastConfiguration.cropOrganismId?has_content && forecastConfiguration.cropOrganismId.organismId == organism.organismId)>
					selected="selected"
				</#if>
			>${organism.latinName!""}/${organism.tradeName!""}/${organism.getLocalName(currentLocale.language)!""} (${hierarchyCategories.getName(organism.hierarchyCategoryId)})</option>
		</#list>
	    </select>
	    <span class="help-block" id="${formId}_cropOrganismId_validation"></span>
	  </div>
	  <div class="form-group">
		  <div class="checkbox">
			<label>
				<input type="checkbox" name="isPrivate"<#if forecastConfiguration.isPrivate?has_content && forecastConfiguration.isPrivate == true> checked="checked"</#if>/>
			</label>
			${i18nBundle.isPrivate}
		</div>
	  </div>
	  <div class="form-group">
	    <label for="pestOrganismId">${i18nBundle.pestOrganismId}</label>
	    <select class="form-control" id="pestOrganismId" name="pestOrganismId" onblur="validateField(this);" onchange="renderModelSpecificFields('${formId}');">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.pestOrganismId?lower_case}</option>
	    	<#list allPests?sort_by("latinName") as organism>
			<option value="${organism.organismId}"
				<#if (forecastConfiguration.pestOrganismId?has_content && forecastConfiguration.pestOrganismId.organismId == organism.organismId)>
					selected="selected"
				</#if>
			>${organism.latinName!""}/${organism.tradeName!""}/${organism.getLocalName(currentLocale.language)!""} (${hierarchyCategories.getName(organism.hierarchyCategoryId)})</option>
		</#list>
	    </select>
	    <span class="help-block" id="${formId}_pestOrganismId_validation"></span>
	  </div>
	  <#if !multipleNew>
	  <div class="form-group">
	    <label for="locationPointOfInterestId">${i18nBundle.locationPointOfInterestId}</label>
	    <select class="form-control" name="locationPointOfInterestId" onblur="validateField(this);">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.locationPointOfInterestId?lower_case}</option>
	    	<#list locationPointOfInterests?sort_by("name") as poi>
	    	<option value="${poi.pointOfInterestId}"<#if forecastConfiguration.locationPointOfInterestId?has_content && poi.pointOfInterestId == forecastConfiguration.locationPointOfInterestId.pointOfInterestId> selected="selected"</#if>>${poi.name}</option>
	    	</#list>
	    </select>
	    <span class="help-block" id="${formId}_locationPointOfInterestId_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="weatherStationPointOfInterestId">${i18nBundle.weatherStationPointOfInterestId}</label>
	    <select class="form-control" name="weatherStationPointOfInterestId" onblur="validateField(this);">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.weatherStationPointOfInterestId?lower_case}</option>
	    	<#list weatherStationPointOfInterests?sort_by("name") as poi>
	    	<option value="${poi.pointOfInterestId}"<#if forecastConfiguration.weatherStationPointOfInterestId?has_content && poi.pointOfInterestId == forecastConfiguration.weatherStationPointOfInterestId.pointOfInterestId> selected="selected"</#if>>${poi.name}</option>
	    	</#list>
	    </select>
	    <span class="help-block" id="${formId}_weatherStationPointOfInterestId_validation"></span>
	  </div>
	  <#else>
	  <input type="hidden" name="multipleNew" value="true"/>
	  <div class="form-group">
	    <label for="weatherStationPointOfInterestIds">${i18nBundle.weatherStationPointOfInterestIds}</label>
	    <select class="form-control" name="weatherStationPointOfInterestIds" onblur="validateField(this);" multiple="multiple" size="5">
	    	<#list weatherStationPointOfInterests?sort_by("name") as poi>
	    	<option value="${poi.pointOfInterestId}">${poi.name}</option>
	    	</#list>
	    </select>
	    <span class="help-block" id="${formId}_weatherStationPointOfInterestIds_validation"></span>
	  </div>
	  </#if>
          <div class="form-group">
	    <label for="dateStart">${i18nBundle.dateStart}</label>
	    <input type="date" class="form-control" name="dateStart" placeholder="${i18nBundle.dateStart}" value="${(forecastConfiguration.dateStart)!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_dateStart_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="dateEnd">${i18nBundle.dateEnd}</label>
	    <input type="date" class="form-control" name="dateEnd" placeholder="${i18nBundle.dateEnd}" value="${(forecastConfiguration.dateEnd)!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_dateEnd_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="timeZone">${i18nBundle.timeZone}</label>
	    <select class="form-control" name="timeZone" onblur="validateField(this);">
		<#list availableTimeZones as timeZoneId>
			<option value="${timeZoneId}"<#if 
						(forecastConfiguration.timeZone?has_content && timeZoneId == forecastConfiguration.timeZone)
						|| (!forecastConfiguration.timeZone?has_content && timeZoneId == defaultTimeZoneId)
						> selected="selected"</#if>>${timeZoneId}</option>
		</#list>
	     </select>
	     <span class="help-block" id="${formId}_timeZone_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="vipsLogicUserId">${i18nBundle.vipsLogicUserId}</label>
	    <select class="form-control" name="vipsLogicUserId" onblur="validateField(this);">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.vipsLogicUserId?lower_case}</option>
	    	<#list vipsLogicUsers?sort_by("lastName") as user>
	    	<option value="${user.userId}"<#if forecastConfiguration.vipsLogicUserId?has_content && user.userId == forecastConfiguration.vipsLogicUserId.userId> selected="selected"</#if>>${user.lastName}, ${user.firstName}</option>
	    	</#list>
	    </select>
	    <span class="help-block" id="${formId}_vipsLogicUserId_validation"></span>
	  </div>
	  <fieldset id="modelSpecificFields">
	  	
	  </fieldset>
	  <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
	  <!--button type="button" onclick="var theForm=document.getElementById('${formId}');validateForm(theForm);validateForm(theForm, theForm.modelId.options[theForm.modelId.selectedIndex].value); ">Test</button-->
	  <button type="button" class="btn btn-warning" onclick="if(confirm('${i18nBundle.confirmLeaveForm}')){window.location.href='/forecastConfiguration';}">${i18nBundle.leaveForm}</button>
	  <#if forecastConfiguration.forecastConfigurationId?has_content>
	  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/forecastConfiguration?action=deleteForecastConfiguration&forecastConfigurationId=${forecastConfiguration.forecastConfigurationId}';}">${i18nBundle.delete}</button>
	  </#if>
        </form>
</div>
</#macro>
<@page_html/>
