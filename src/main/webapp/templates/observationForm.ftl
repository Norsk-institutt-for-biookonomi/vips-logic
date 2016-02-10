<#-- 
    Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
  
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
        <title><#if observation.observationId?has_content>${i18nBundle.editObservation}<#else>${i18nBundle.newObservation}</#if></title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/jquery.datetimepicker.css"/ >
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol3-layerswitcher.css"/ >
	<link rel="stylesheet" type="text/css" href="/css/map.css"/ >
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/3rdparty/jquery.datetimepicker.js"></script>
	<script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
	<script type="text/javascript" src="/js/3rdparty/moment.min.js"></script>
	<script src="/js/3rdparty/metawidget/core/metawidget-core.min.js" type="text/javascript"></script>
	<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
        <script type="text/javascript" src="js/3rdparty/ol3-layerswitcher.js"></script>
        
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/resourcebundle.js"></script>
	<script type="text/javascript" src="/js/validateForm.js"></script>
	<script type="text/javascript" src="/js/objectGISInfoMap.js"></script>
	<script type="text/javascript">
		var organizationId = ${user.organizationId.organizationId};
		
		$(document).ready(function() {
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
			
			// Initialize the map
			// If observation already registered center on location
			// Otherwise, center and zoom to organizations's default
			<#if observation.location?has_content>
			initMap([${(observation.location.x?c)!""},${(observation.location.y?c)!""}],10,true);
			<#else>
			var geoInfo = <#if observation.geoinfo?has_content>${observation.geoinfo}<#else>{}</#if>;
			var chooseFromMapLayers = {"chooseFromMapLayers":<#if mapLayers?has_content>${mapLayers}<#else>[]</#if>};
			initMap("observationFormMap",[${defaultMapCenter.x?c},${defaultMapCenter.y?c}],${defaultMapZoom},false, geoInfo, chooseFromMapLayers);
			</#if>
			
			<#if observation.observationData?has_content>
				observationData = ${observation.observationData};
				getDataSchema(${observation.organism.organismId}, organizationId);
			<#else>
			// Setting 
				
			</#if>
		});
		
		// Global for observationData metawidgets
		var mw = null;
		var observationData = null;
		function initObservationData(organismId, organizationId) {
			$.getJSON( "/rest/observationdata/model/" + organizationId + "/" + organismId, function( json ) {
				observationData = json;
				getDataSchema(organismId, organizationId);
			});
		}
		
		function getDataSchema(organismId, organizationId)
		{
			$.getJSON( "/rest/observationdata/schema/" + organizationId + "/" + organismId, buildForm);
		}
		
		var buildForm = function(schemaProperties)
		{
			// Erasing whatever was there before
			var metawidgetC = document.getElementById( 'metawidget' );
			metawidgetC.innerHTML = "";
			mw = new metawidget.Metawidget(metawidgetC , {
					inspector: new metawidget.inspector.CompositeInspector( [ new metawidget.inspector.PropertyTypeInspector(),
						function( toInspect, type, names ) {
								return {
									properties: schemaProperties
								};
							}
					] )
				}
			);
			mw.toInspect = observationData;
			mw.buildWidgets();
		};
		
	</script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
	<p><a href="/observation" class="btn btn-default back" role="button">${i18nBundle.back}</a></p>
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
			<form id="${formId}" role="form" action="/observation?action=observationFormSubmit" method="POST" onsubmit="this['geoInfo'].value=getFeatures();try{mw.save();console.log(this['observationData']);this['observationData'].value=JSON.stringify(mw.toInspect);return validateForm(this);}catch(e){console.log(e.message);console.log(e);return false;}">
			<!--form id="${formId}" role="form" action="/observation?action=observationFormSubmit" method="POST" onsubmit="this['geoInfo'].value=getFeatures();mw.save();console.log(this['observationData']);this['observationData'].value=JSON.stringify(mw.toInspect);return validateForm(this);"-->
			<!--form id="${formId}" role="form" action="/observation?action=observationFormSubmit" method="POST" onsubmit="this['geoInfo'].value=getFeatures();mw.save();console.log(this['observationData']);this['observationData'].value=JSON.stringify(mw.toInspect);validateForm(this);return false;"-->
			  <input type="hidden" name="geoInfo" value=""/>
			  <input type="hidden" name="observationData" value=""/>
			  <input type="hidden" name="observationId" value="${observation.observationId!"-1"}"/>
			  <!--button type="button" onclick="var theForm=document.getElementById('${formId}');console.log(theForm['geoInfo']);console.log(theForm['observationData']);theForm['geoInfo'].value=getFeatures();mw.save();console.log(theForm['observationData']);theForm['observationData'].value=JSON.stringify(mw.toInspect);validateForm(theForm);">Test</button-->
			  <div class="form-group">
			    <label for="organismId">${i18nBundle.organism}</label>
			    
			    <select class="form-control" name="organismId" <#if observation.organism?has_content>readonly="readonly"<#else>onchange="initObservationData(this.options[this.options.selectedIndex].value,organizationId);" onblur="validateField(this);"</#if>>
				<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.organism?lower_case}</option>
				<#list allOrganisms?sort_by("latinName") as organism>
					<option value="${organism.organismId}"
						<#if (observation.organism?has_content && observation.organism.organismId == organism.organismId)>selected="selected"</#if>
					>${organism.latinName!""}/${organism.getLocalName(currentLocale.language)!""} (${hierarchyCategories.getName(organism.hierarchyCategoryId)})</option>
				</#list>
			    </select>
			    <span class="help-block" id="${formId}_organismId_validation"></span>
			    
			  </div>
			  <!--div class="form-group">
			    <label for="location">${i18nBundle.location}</label>
			    <input type="text" class="form-control" readonly="readonly" id="location" name="location" placeholder="${i18nBundle.location}" value="${(observation.location.x?c)!""},${(observation.location.y?c)!""}" onblur="validateField(this);" />
			    <span class="help-block" id="${formId}_location_validation"></span>
			  </div-->
			  <div class="form-group">
			    <label for="timeOfObservation">${i18nBundle.timeOfObservation}</label>
			    <input type="datetime" class="form-control" name="timeOfObservation" placeholder="${i18nBundle.timeOfObservation}" value="${(observation.timeOfObservation?string("yyyy-MM-dd HH:mmZ"))!.now?string("yyyy-MM-dd HH:mmZ")}" onblur="validateField(this);" />
			    <span class="help-block" id="${formId}_timeOfObservation_validation"></span>
			  </div>
			  <div class="form-group">
			  <fieldset>
			  	<legend>Observation data</legend>
			  	<div id="metawidget"></div>
			  </fieldset>
			  </div>
			  <!--div class="form-group">
			    <label for="observedValue">${i18nBundle.observedValue}</label>
			    <input type="number" class="form-control" name="observedValue" placeholder="${i18nBundle.observedValue}" value="${(observation.observedValue?c)!""}" onblur="validateField(this);"/>
			    <span class="help-block" id="${formId}_observedValue_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="denominator">${i18nBundle.denominator}</label>
			    <input type="number" class="form-control" name="denominator" placeholder="${i18nBundle.denominator}" value="${(observation.denominator?c)!"1"}" onblur="validateField(this);"/>
			    <span class="help-block" id="${formId}_denominator_validation"></span>
			  </div-->
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
			  <div class="form-group">
			    <label for="observationHeading">${i18nBundle.observationHeading}</label>
			    <input type="text" class="form-control" name="observationHeading" placeholder="" value="${observation.observationHeading!""}" onblur="validateField(this);"/>
			    <span class="help-block" id="${formId}_observationHeading_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="observationText">${i18nBundle.observationText}</label>
			    <textarea class="form-control" name="observationText" placeholder="" >${observation.observationText!""}</textarea>
			    <span class="help-block" id="${formId}_observationText_validation"></span>
			  </div>
			  <#if user.isObservationAuthority() || user.isSuperUser()>
				  <div class="form-group">
				    <label for="statusTypeId">${i18nBundle.statusTypeId}</label>
				    <select class="form-control" name="statusTypeId" onblur="validateField(this);">
					<#list statusTypeIds as statusTypeId>
						<option value="${statusTypeId.statusTypeId}"
							<#if observation.statusTypeId?has_content && observation.statusTypeId == statusTypeId.statusTypeId>selected="selected"</#if>
							<#if !observation.statusTypeId?has_content && 3 == statusTypeId.statusTypeId>selected="selected"</#if>
						>${i18nBundle("statusTypeIdTitle_" + statusTypeId.statusTypeId)}</option>
					</#list>
				     </select>
				     <span class="help-block" id="${formId}_statusTypeId_validation"></span>
				  </div>
				  <div class="form-group">
				    <label for="statusRemarks">${i18nBundle.statusRemarks}</label>
				    <textarea class="form-control" name="statusRemarks" placeholder="" >${observation.statusRemarks!""}</textarea>
				    <span class="help-block" id="${formId}_statusRemarks_validation"></span>
				  </div>
			  </#if>
			  <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
			  <#if observation.observationId?has_content>
			  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/observation?action=deleteObservation&observationId=${observation.observationId}';}">${i18nBundle.delete}</button>
			  </#if>
			</form>
		</div>
		<div class="col-md-6">
			<div id="observationFormMap"></div> <!-- Contents is added dynamically by objectGISInfoMap -->
		</div>
	</div>
	<!--div style="display: none;"><div id="marker" title="Marker"><img src="/images/bug_medium.png"/></div></div-->
</div>
</#macro>
<@page_html/>
