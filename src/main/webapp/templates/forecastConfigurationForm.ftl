<#include "master.ftl">
<#macro page_head>
        <title></title>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
	<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
	<link href="http://code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
	<script type="text/javascript" src="/js/modernizr_custom.js"></script>
	<script type="text/javascript" src="/js/moment.min.js"></script>
	<script type="text/javascript">
		// Make sure that there is a date picker present for HTML5 
		// date input fields
		if (!Modernizr.inputtypes.date) {
		    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
		}
	</script>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.viewForecastConfiguration}</h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <#assign formId = "forecastConfigurationForm">
        <form id="${formId}" role="form" action="/forecastConfiguration?action=forecastConfigurationFormSubmit" method="POST" onsubmit="try{return validateForm(this);}catch(err){alert(err);return false;}">
          <input type="hidden" name="forecastConfigurationId" value="${forecastConfiguration.forecastConfigurationId!"-1"}"/>
          <div class="form-group">
	    <label for="modelId">${i18nBundle.modelId}</label>
	    <select class="form-control" name="modelId" onblur="validateField(this);">
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
          <div class="form-group">
	    <label for="dateStart">${i18nBundle.dateStart}</label>
	    <input type="date" class="form-control" name="dateStart" placeholder="${i18nBundle.dateStart}" value="${(forecastConfiguration.dateStart?string(dateStart_dateFormat))!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_dateStart_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="dateEnd">${i18nBundle.dateEnd}</label>
	    <input type="date" class="form-control" name="dateEnd" placeholder="${i18nBundle.dateEnd}" value="${(forecastConfiguration.dateEnd?string(dateEnd_dateFormat))!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_dateEnd_validation"></span>
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
	  <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
	  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){alert('Not implemented');}">${i18nBundle.delete}</button>
        </form>
</#macro>
<@page_html/>
