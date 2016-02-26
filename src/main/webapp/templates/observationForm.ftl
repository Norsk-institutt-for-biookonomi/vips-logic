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
	<script type="text/javascript" src="/js/util.js"></script>
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
			
			// Activating file selection
			$('.btn-file :file').on('fileselect', function(event, numFiles, label) {
				
				var input = $(this).parents('.input-group').find(':text'),
					log = numFiles > 1 ? numFiles + ' files selected' : label;
				
				if( input.length ) {
					input.val(log);
				} else {
					if( log ) alert(log);
				}
				
			});
		});
		
		// Adding selected file name to a box
		$(document)
			.on('change', '.btn-file :file', function() {
				var input = $(this),
				numFiles = input.get(0).files ? input.get(0).files.length : 1,
				label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
				input.trigger('fileselect', [numFiles, label]);
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
		
		var updateCropPests = function(){
			var theForm = document.getElementById('observationForm');
			var selectedCropId = theForm["cropOrganismId"].options[theForm["cropOrganismId"].options.selectedIndex].value;
			// If this is not a new observation, or the selected crop is not in the database, we keep calm
			if(theForm["observationId"].value !== "-1" ||  selectedCropId == "-10")
			{
				return;
			}
			$.getJSON( "/rest/organism/croppest/" + selectedCropId, function( json ) {
				updateCropPestsCallback(json);
			})
			.fail(function() {
			    alert( "Error getting pests for this crop. Please contact system administrator" );
			  });
		};
		
		var updateCropPestsCallback = function(cropPest) {
			var pestList = document.getElementById('observationForm')["organismId"];
			if(cropPest == null)
			{
				// Need to reorganize pests back to default
				var allPests = [];
				for(var i=2;i<pestList.options.length;i++)
				{
					allPests.push(pestList.options[i]);
				}
				allPests.sort(compareSelectListOptions);
				pestList.options.length=2; // Keeping the top two
				for(var i=0;i<allPests.length;i++)
				{
					pestList.options[pestList.options.length] = allPests[i];
				}
			}
			else
			{
				var prioritatedPests = [];
				var unprioritatedPests = []
				for(var i=2;i<pestList.options.length;i++)
				{
					if(cropPest.pestOrganismIds.indexOf(parseInt(pestList.options[i].value)) >= 0)
					{
						//console.log(pestList.options[i].value + " is prioritated");
						prioritatedPests.push(pestList.options[i]);
					}
					else if(pestList.options[i].value != "-1") // Avoiding the "---" option
					{
						//console.log(pestList.options[i].value + " is unprioritated");
						unprioritatedPests.push(pestList.options[i]);
					}
					
				}
				pestList.options.length=2; // Keeping the top two
				for(var i=0;i<prioritatedPests.length;i++)
				{
					pestList.options[pestList.options.length] = prioritatedPests[i];
				}
				pestList.options[pestList.options.length] = new Option("---","-1");
				for(var i=0;i<unprioritatedPests.length;i++)
				{
					pestList.options[pestList.options.length] = unprioritatedPests[i];
				}
			}
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
			<form id="${formId}" role="form" action="/observation?action=observationFormSubmit" enctype="multipart/form-data" method="POST" onsubmit="this['geoInfo'].value=getFeatures();try{mw.save();this['observationData'].value=JSON.stringify(mw.toInspect);return validateForm(this);}catch(e){console.log(e.message);console.log(e);return false;}">
			<!--form id="${formId}" role="form" action="/observation?action=observationFormSubmit" method="POST" onsubmit="this['geoInfo'].value=getFeatures();mw.save();console.log(this['observationData']);this['observationData'].value=JSON.stringify(mw.toInspect);return validateForm(this);"-->
			<!--form id="${formId}" role="form" action="/observation?action=observationFormSubmit" method="POST" onsubmit="this['geoInfo'].value=getFeatures();mw.save();console.log(this['observationData']);this['observationData'].value=JSON.stringify(mw.toInspect);validateForm(this);return false;"-->
			  <input type="hidden" name="geoInfo" value=""/>
			  <input type="hidden" name="observationData" value=""/>
			  <input type="hidden" name="observationId" value="${observation.observationId!"-1"}"/>
			  <!--button type="button" onclick="var theForm=document.getElementById('${formId}');console.log(theForm['geoInfo']);console.log(theForm['observationData']);theForm['geoInfo'].value=getFeatures();mw.save();console.log(theForm['observationData']);theForm['observationData'].value=JSON.stringify(mw.toInspect);validateForm(theForm);">Test</button-->
			  
			   <div class="form-group">
			    <label for="cropOrganismId">${i18nBundle.cropOrganismId}</label>
			    <select class="form-control" name="cropOrganismId" <#if observation.organism?has_content>readonly="readonly"<#else>onblur="validateField(this);"</#if> onchange="updateCropPests();">
				<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.cropOrganismId?lower_case}</option>
				<option value="-10"<#if (observation.cropOrganism?has_content && observation.cropOrganism.organismId == -10)>selected="selected"</#if>>${i18nBundle.missingInDatabase}</option>
				<#list allCrops?sort_by("latinName") as cropOrganism>
					<option value="${cropOrganism.organismId}"
						<#if (observation.cropOrganism?has_content && observation.cropOrganism.organismId == cropOrganism.organismId)>selected="selected"</#if>
					>${cropOrganism.getLocalName(currentLocale.language)!""} (${cropOrganism.latinName!""}) ${hierarchyCategories.getName(cropOrganism.hierarchyCategoryId)?upper_case}</option>
				</#list>
			    </select>
			    <span class="help-block" id="${formId}_cropOrganismId_validation"></span>
			    
			    
			  </div>
			  <div class="form-group">
			    <label for="organismId">${i18nBundle.organism}</label>
			    
			    <select class="form-control" name="organismId" <#if observation.organism?has_content>readonly="readonly"<#else>onchange="initObservationData(this.options[this.options.selectedIndex].value,organizationId);" onblur="validateField(this);"</#if>>
				<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.organism?lower_case}</option>
				<option value="-10"<#if (observation.organism?has_content && observation.organism.organismId == -10)>selected="selected"</#if>>${i18nBundle.missingInDatabase}</option>
				<#list allPests?sort_by("latinName") as organism>
					<option value="${organism.organismId}"
						<#if (observation.organism?has_content && observation.organism.organismId == organism.organismId)>selected="selected"</#if>
					>${organism.getLocalName(currentLocale.language)!""} (${organism.latinName!""}) ${hierarchyCategories.getName(organism.hierarchyCategoryId)?upper_case}</option>
				</#list>
			    </select>
			    <span class="help-block" id="${formId}_organismId_validation"></span>
			    
			  </div>
			  
			  <div class="form-group">
			    <label for="timeOfObservation">${i18nBundle.timeOfObservation}</label>
			    <input type="datetime" class="form-control" name="timeOfObservation" placeholder="${i18nBundle.timeOfObservation}" value="${(observation.timeOfObservation?string("yyyy-MM-dd HH:mmZ"))!.now?string("yyyy-MM-dd HH:mmZ")}" onblur="validateField(this);" />
			    <span class="help-block" id="${formId}_timeOfObservation_validation"></span>
			  </div>
			  <div class="form-group">
			  	<div class="checkbox">
					<label>
						<input type="checkbox" name="isQuantified"<#if observation.isQuantified?has_content && observation.isQuantified == false><#else>checked="checked"</#if>/>
					</label>
					${i18nBundle.isQuantified}
			  	</div>
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
			  <!--div class="form-group">
			    <label for="observationMethodId">${i18nBundle.observationMethodId}</label>
			    <select class="form-control" name="observationMethodId" onblur="validateField(this);">
				<#list observationMethods as observationMethod>
					<option value="${observationMethod.observationMethodId}"
						<#if observation.observationMethodId?has_content && observation.observationMethodId.observationMethodId == observationMethod.observationMethodId>selected="selected"</#if>
					>${i18nBundle("observationMethodTitle_" + observationMethod.observationMethodId)}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_observationMethodId_validation"></span>
			  </div-->
			  <div class="form-group">
			  	<div class="checkbox">
					<label>
						<input type="checkbox" name="broadcastMessage"<#if observation.broadcastMessage?has_content && observation.broadcastMessage == false><#else>checked="checked"</#if>/>
					</label>
					${i18nBundle.broadcastMessage}
			  	</div>
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
			  <#if observation.observationIllustrationSet?has_content && observation.observationIllustrationSet?size == 1>
				<#assign illustration = observation.observationIllustrationSet?first>
				<img src="/static/images/observations/${observation.organismId}/${illustration.observationIllustrationPK.fileName}" alt="TODO: Add describing text" class="img-responsive"/>
				<div class="checkbox">
				  <label>
				    <input type="checkbox" name="deleteIllustration" value="true">
				    ${i18nBundle.deleteIllustration}
				  </label>
				</div>
			</#if>
			  <div class="form-group">
				<div class="input-group">
					<label for="illustration"><#if observation.observationIllustrationSet?has_content && observation.observationIllustrationSet?size == 1>${i18nBundle.replaceIllustration}<#else>${i18nBundle.newIllustration}</#if></label><br/>
					<span class="btn btn-default btn-file">${i18nBundle.browse}<input type="file" name="illustration"></span>
					<input type="text" class="form-control" readonly>
				</div>
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
