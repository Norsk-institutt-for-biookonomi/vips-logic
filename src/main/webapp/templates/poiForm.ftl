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
        <title><#if poi.pointOfInterestId?has_content>${i18nBundle.editPoi}<#else>${i18nBundle.newPoi}</#if></title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/resourcebundle.js"></script>
	<script type="text/javascript" src="/js/validateForm.js"></script>
	<script type="text/javascript" src="/js/poiFormMap.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			// Load main form definition (for validation)
			loadFormDefinition("poiForm");
			
			// Initialize the map
			// If weather station already registered, center on location
			// Otherwise, center and zoom to organizations's default
			<#if poi.longitude?has_content && poi.latitude?has_content>
			initMap([${(poi.longitude?c)!""},${(poi.latitude?c)!""}],10,${user.organizationId.organizationId},${poi.pointOfInterestId!"null"});
			<#else>
			initMap([${defaultMapCenter.x?c},${defaultMapCenter.y?c}],${defaultMapZoom},${user.organizationId.organizationId},${poi.pointOfInterestId!"null"});
			</#if>
		});
                
                // POI name must be unique
                function checkPoiNameAvailability(poiNameField)
                {
                    var poiName = poiNameField.value;
                    if(poiName.length > 0)
                    {
                        $.getJSON( "/rest/poi/name/" + poiName)
                            .done(function(json, status, jx){
                                if(jx.status==200)
                                {
                                    var theForm = document.getElementById("poiForm");
                                    invalidizeField(poiNameField, theForm, "${i18nBundle.nameAlreadyExists}",null);
                                }
                            });
                    }
                }
	</script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
	<p><a href="${returnURL}" class="btn btn-default back" role="button">${i18nBundle.back}</a></p>
        <h1><#if poi.pointOfInterestId?has_content>${i18nBundle.editPoi}<#else>${i18nBundle.newPoi}</#if></h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<div class="row">
		<div class="col-md-6">
			<#assign formId = "poiForm">
			<form id="${formId}" role="form" action="/poi?action=poiFormSubmit" method="POST" onsubmit="return validateForm(this);">
			  <input type="hidden" name="returnIdCallback" value="${returnIdCallback!""}"/>
			  <input type="hidden" name="pointOfInterestId" value="${poi.pointOfInterestId!"-1"}"/>
			  <div class="form-group">
			    <label for="name">${i18nBundle.name}</label>
			    <input type="text" class="form-control" name="name" placeholder="${i18nBundle.name}" value="${(poi.name)!""}" onblur="validateField(this); checkPoiNameAvailability(this);"/>
			    <span class="help-block" id="${formId}_name_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="pointOfInterestTypeId">${i18nBundle.pointOfInterestType}</label>
			    <select class="form-control" name="pointOfInterestTypeId" onblur="validateField(this);">
				<#list [0,1,2,3,5,6,7] as pointOfInterestTypeId>
                                    <#if pointOfInterestTypeId != 1>
					<option value="${pointOfInterestTypeId}"<#if 
								poi.pointOfInterestTypeId?has_content && pointOfInterestTypeId == poi.pointOfInterestTypeId
								> selected="selected"</#if>>${i18nBundle["pointOfInterestType_" + pointOfInterestTypeId]}</option>
                                    </#if>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_pointOfInterestTypeId_validation"></span>
			  </div>
                          <#if user.isSuperUser() || user.isOrganizationAdmin()>
                          <div class="form-group">
			  	<div class="checkbox">
					<label>
						<input type="checkbox" name="isForecastLocation"<#if poi.isForecastLocation?has_content && poi.isForecastLocation == true> checked="checked"<#else></#if>/>
					</label>
					${i18nBundle.isForecastLocation}
			  	</div>
			  </div>
                          </#if>
			  <div class="form-group">
			    <label for="location">${i18nBundle.location} (<a href="http://en.wikipedia.org/wiki/World_Geodetic_System#A_new_World_Geodetic_System:_WGS_84" target="new">WGS84</a>: ${i18nBundle.longitude},${i18nBundle.latitude})</label>
			    <input type="text" class="form-control" id="location" name="location" placeholder="${i18nBundle.location}" value="${(poi.longitude?c)!""},${(poi.latitude?c)!""}" onblur="validateField(this);" onchange="if(validateField(this)){updateMarkerPosition();}" />
			    <span class="help-block" id="${formId}_location_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="altitude">${i18nBundle.altitude} (${i18nBundle.meter})</label>
			    <input type="number" class="form-control" name="altitude" placeholder="${i18nBundle.altitude}" value="${(poi.altitude?c)!""}" onblur="validateField(this);"/>
			    <span class="help-block" id="${formId}_altitude_validation"></span>
			  </div>
			   <div class="form-group">
			    <label for="timeZone">${i18nBundle.timeZone}</label>
			    <select class="form-control" name="timeZone" onblur="validateField(this);">
				<#list availableTimeZones as timeZoneId>
					<option value="${timeZoneId}"<#if 
								(poi.timeZone?has_content && timeZoneId == poi.timeZone)
								|| (!poi.timeZone?has_content && timeZoneId == defaultTimeZoneId)
								> selected="selected"</#if>>${timeZoneId}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_timeZone_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="countryCode">${i18nBundle.country}</label>
			    <select class="form-control" name="countryCode" onblur="validateField(this);">
				<#list availableCountries as country>
					<option value="${country.countryCode}"<#if 
								(poi.countryCode?has_content && country.countryCode == poi.countryCode.countryCode)
								|| (!poi.countryCode?has_content && country.countryCode == defaultCountryCode)
								> selected="selected"</#if>>${country.getCountryName(currentLocale)}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_countryCode_validation"></span>
			  </div>
                          <div class="form-group">
                            <label for="organizationGroupIds">${i18nBundle.availableFor} ${i18nBundle.organizationGroupList?lower_case}</label>
                            <select class="form-control chosen-select" name="organizationGroupIds" onblur="validateField(this);" multiple="multiple">
                                <#list groups as group>
                                        <option value="${group.organizationGroupId}" <#if poiGroupIds?seq_contains(group.organizationGroupId)> selected="selected"</#if>>${group.groupName}</option>
                                </#list>
                             </select>
                        </div>
			  <fieldset>
			  <#if user.isSuperUser() || user.isOrganizationAdmin()>
			<h3>${i18nBundle.externalResources}</h3>
			<#if poi.pointOfInterestExternalResourceSet?has_content>
				<#list poi.pointOfInterestExternalResourceSet as poiExternalResource>
				<div class="form-group">
				    <label for="externalResourceIdentifier_${poiExternalResource.externalResource.externalResourceId}">${i18nBundle.externalResourceIdentifier} - ${poiExternalResource.externalResource.name} (${poiExternalResource.externalResource.uri}${poiExternalResource.externalResource.identifierTemplate})</label>
				    <input type="text" class="form-control" name="externalResourceIdentifier_${poiExternalResource.externalResource.externalResourceId}" placeholder="${i18nBundle.externalResourceIdentifier}" value="${poiExternalResource.resourceIdentifier}" />
				</div>
				 </#list>
			 </#if>
			 <#list unreferencedExternalResources as externalResource>
			<div class="form-group">
			    <label for="externalResourceIdentifier_${externalResource.externalResourceId}">${i18nBundle.externalResourceIdentifier} - ${externalResource.name} (${externalResource.uri}${externalResource.identifierTemplate})</label>
			    <input type="text" class="form-control" name="externalResourceIdentifier_${externalResource.externalResourceId}" placeholder="${i18nBundle.externalResourceIdentifier}" value="" />
			</div>
			 </#list>
			 
			</fieldset>
			</#if>
			  <#if user.isSuperUser() || user.isOrganizationAdmin()>
			  <div class="form-group">
			    <label for="userId">${i18nBundle.vipsLogicUserId}</label>
			    <select class="form-control" name="userId" onblur="validateField(this);">
			    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.vipsLogicUserId?lower_case}</option>
				<#list users as user>
					<option value="${user.userId}"<#if poi.user?has_content && user.userId == poi.user.userId
								> selected="selected"</#if>>${user.lastName}, ${user.firstName} (${user.organizationId.organizationName})</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_userId_validation"></span>
			  </div>
                          <#else>
                            <input type="hidden" name="userId" value="${user.userId}"/>
			  </#if>
			  <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
			  <#if poi.pointOfInterestId?has_content>
			  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/poi?action=deletePoiPreview&pointOfInterestId=${poi.pointOfInterestId}';}">${i18nBundle.delete}</button>
			  </#if>
			</form>
		</div>
		<div class="col-md-6">
			<div id="poiFormMap" class="map">
				<div id="popover"></div>
			</div>
		</div>
	</div>
	<div style="display: none;"><div id="poiMarker" title="Marker"><img src="/public/images/anemometer_mono.png"/></div></div>
</div>
</#macro>
<@page_html/>
