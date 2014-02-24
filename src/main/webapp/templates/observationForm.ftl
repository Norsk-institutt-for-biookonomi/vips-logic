<#-- 
  Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 

  This file is part of VIPSLogic.
  VIPSLogic is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  VIPSLogic is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
--><#include "master.ftl">
<#macro page_head>
        <title><#if observation.observationId?has_content>${i18nBundle.editObservation}<#else>${i18nBundle.newObservation}</#if></title>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/jquery.datetimepicker.css"/ >
	<script src="/js/3rdparty/jquery.datetimepicker.js"></script>
	<script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
	<script type="text/javascript" src="/js/3rdparty/moment.min.js"></script>
	<script type="text/javascript">
		// Make sure that there is a datetime picker present for HTML5 
		// date input fields
		if (!Modernizr.inputtypes.datetime) {
		    $('input[type=datetime]').datetimepicker({
		    		lang:"no",
		    		dayOfWeekStart: 1,
		    		format: "Y-m-d G:iO",
		    		step: 30
		    	});
		}
		// Load main form definition (for validation)
		loadFormDefinition("observationForm");
	</script>
</#macro>
<#macro page_contents>
        <h1><#if observation.observationId?has_content>${i18nBundle.editObservation}<#else>${i18nBundle.newObservation}</#if></h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<div class="row">
		<div class="col-md-6">
			<#assign formId = "observationForm">
			<form id="${formId}" role="form" action="/observation?action=observationFormSubmit" method="POST" onsubmit="return validateForm(this);">
			  <input type="hidden" name="observationId" value="${observation.observationId!"-1"}"/>
			  <div class="form-group">
			    <label for="organismId">${i18nBundle.organismId}</label>
			    <select class="form-control" name="organismId" onblur="validateField(this);">
				<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.organismId?lower_case}</option>
				<#list allOrganisms?sort_by("latinName") as organism>
					<option value="${organism.organismId}"
						<#if (observation.organism?has_content && observation.organism.organismId == organism.organismId)>selected="selected"</#if>
					>${organism.latinName!""}/${organism.getLocalName(currentLocale.language)!""} (${hierarchyCategories.getName(organism.hierarchyCategoryId)})</option>
				</#list>
			    </select>
			    <span class="help-block" id="${formId}_organismId_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="location">${i18nBundle.location}</label>
			    <input type="text" class="form-control" name="location" placeholder="${i18nBundle.location}" value="${(observation.location.x)!""},${(observation.location.y)!""}" onblur="validateField(this);" />
			    <span class="help-block" id="${formId}_location_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="timeOfObservation">${i18nBundle.timeOfObservation}</label>
			    <input type="datetime" class="form-control" name="timeOfObservation" placeholder="${i18nBundle.timeOfObservation}" value="${observation.timeOfObservation?string("yyyy-MM-dd HH:mmZ")!.now?string("yyyy-MM-dd HH:mmZ")}" onblur="validateField(this);" />
			    <span class="help-block" id="${formId}_timeOfObservation_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="observedValue">${i18nBundle.observedValue}</label>
			    <input type="number" class="form-control" name="observedValue" placeholder="${i18nBundle.observedValue}" value="${observation.observedValue!""}" onblur="validateField(this);"/>
			    <span class="help-block" id="${formId}_observedValue_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="denominator">${i18nBundle.denominator}</label>
			    <input type="number" class="form-control" name="denominator" placeholder="${i18nBundle.denominator}" value="${observation.denominator!"1"}" onblur="validateField(this);"/>
			    <span class="help-block" id="${formId}_denominator_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="observationMethodId">${i18nBundle.observationMethodId}</label>
			    <select class="form-control" name="observationMethodId" onblur="validateField(this);">
				<#list observationMethods as observationMethod>
					<option value="${observationMethod.observationMethodId}"
						<#if observation.observationMethodId?has_content && observation.observationMethodId.observationMethodId == observationMethod.observationMethodId>selected="selected"</#if>
					>${i18nBundle("observationMethodTitle_" + observationMethod.observationMethodId)}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_observationMethodId_validation"></span>
			  </div>
			  
			  <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
			  <#if observation.observationId?has_content>
			  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/observation?action=deleteObservation&observationId=${observation.observationId}';}">${i18nBundle.delete}</button>
			  </#if>
			</form>
		</div>
		<div class="col-md-6">
		MAPPP!
		</div>
	</div>
</#macro>
<@page_html/>
