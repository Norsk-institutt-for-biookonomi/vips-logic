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
        <title><#if weatherStation.pointOfInterestId?has_content>${i18nBundle.editWeatherStation}<#else>${i18nBundle.newWeatherStation}</#if></title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/resourcebundle.js"></script>
	<script type="text/javascript" src="/js/validateForm.js"></script>
	<script type="text/javascript" src="/js/weatherStationFormMap.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			// Load main form definition (for validation)
			loadFormDefinition("weatherStationForm");
			
			// Initialize the map
			// If weather station already registered, center on location
			// Otherwise, center and zoom to organizations's default
			<#if weatherStation.longitude?has_content && weatherStation.latitude?has_content>
			initMap([${(weatherStation.longitude?c)!""},${(weatherStation.latitude?c)!""}],10,${user.organizationId.organizationId},${weatherStation.pointOfInterestId!"null"});
			<#else>
			initMap([${defaultMapCenter.x?c},${defaultMapCenter.y?c}],${defaultMapZoom},${user.organizationId.organizationId},${weatherStation.pointOfInterestId!"null"});
			</#if>
		});
	</script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
	<p><a href="${returnURL}" class="btn btn-default back" role="button">${i18nBundle.back}</a></p>
        <h1><#if weatherStation.pointOfInterestId?has_content>${i18nBundle.editWeatherStation}<#else>${i18nBundle.newWeatherStation}</#if></h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<div class="row">
		<div class="col-md-6">
			<#assign formId = "weatherStationForm">
			<form id="${formId}" role="form" action="/weatherStation?action=weatherStationFormSubmit" method="POST" onsubmit="return validateForm(this);">
			  <input type="hidden" name="pointOfInterestId" value="${weatherStation.pointOfInterestId!"-1"}"/>
			  <div class="form-group">
			    <label for="name">${i18nBundle.name}</label>
			    <input type="text" class="form-control" name="name" placeholder="${i18nBundle.name}" value="${(weatherStation.name)!""}" onblur="validateField(this);"/>
			    <span class="help-block" id="${formId}_name_validation"></span>
			  </div>
			  <div class="form-group">
				  <div class="checkbox">
					  <label>
					    <input type="checkbox" name="active" value="true"<#if (weatherStation.active?has_content && weatherStation.active == true)> checked="checked"</#if>/>
					    ${i18nBundle.active}
					  </label>
				  </div>
			  </div>
			  <div class="form-group">
			    <label for="location">${i18nBundle.location} (<a href="http://en.wikipedia.org/wiki/World_Geodetic_System#A_new_World_Geodetic_System:_WGS_84" target="new">WGS84</a>: ${i18nBundle.longitude},${i18nBundle.latitude})</label>
			    <input type="text" class="form-control" id="location" name="location" placeholder="${i18nBundle.location}" value="${(weatherStation.longitude?c)!""},${(weatherStation.latitude?c)!""}" onblur="validateField(this);" onchange="if(validateField(this)){updateMarkerPosition();}" />
			    <span class="help-block" id="${formId}_location_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="altitude">${i18nBundle.altitude} (${i18nBundle.meter})</label>
			    <input type="number" class="form-control" name="altitude" placeholder="${i18nBundle.altitude}" value="${(weatherStation.altitude?c)!""}" onblur="validateField(this);"/>
			    <span class="help-block" id="${formId}_altitude_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="weatherStationDataSourceId">${i18nBundle.dataSource}</label>
			    <select class="form-control" name="weatherStationDataSourceId" onblur="validateField(this);">
			    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.dataSource?lower_case}
				<#list dataSources as dataSource>
					<option value="${dataSource.weatherStationDataSourceId}"
						<#if weatherStation.weatherStationDataSourceId?has_content && weatherStation.weatherStationDataSourceId.weatherStationDataSourceId == dataSource.weatherStationDataSourceId>selected="selected"</#if>
					>${dataSource.name}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_weatherStationDataSourceId_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="weatherForecastProviderId">${i18nBundle.weatherForecastProvider}</label>
			    <select class="form-control" name="weatherForecastProviderId" onblur="validateField(this);">
			    	<option value="-1">${i18nBundle.none} ${i18nBundle.weatherForecastProvider?lower_case}
				<#list weatherForecastProviders as weatherForecastProvider>
					<option value="${weatherForecastProvider.weatherForecastProviderId}"
						<#if weatherStation.weatherForecastProviderId?has_content && weatherStation.weatherForecastProviderId.weatherForecastProviderId == weatherForecastProvider.weatherForecastProviderId>selected="selected"</#if>
					>${weatherForecastProvider.name}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_weatherForecastProviderId_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="weatherStationRemoteId">${i18nBundle.weatherStationRemoteId}</label>
			    <input type="text" class="form-control" name="weatherStationRemoteId" placeholder="${i18nBundle.weatherStationRemoteId}" value="${(weatherStation.weatherStationRemoteId)!""}" onblur="validateField(this);"/>
			    <span class="help-block" id="${formId}_weatherStationRemoteId_validation"></span>
			  </div>
			   <div class="form-group">
			    <label for="timeZone">${i18nBundle.timeZone}</label>
			    <select class="form-control" name="timeZone" onblur="validateField(this);">
				<#list availableTimeZones as timeZoneId>
					<option value="${timeZoneId}"<#if 
								(weatherStation.timeZone?has_content && timeZoneId == weatherStation.timeZone)
								|| (!weatherStation.timeZone?has_content && timeZoneId == defaultTimeZoneId)
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
								(weatherStation.countryCode?has_content && country.countryCode == weatherStation.countryCode.countryCode)
								|| (!weatherStation.countryCode?has_content && country.countryCode == defaultCountryCode)
								> selected="selected"</#if>>${country.getCountryName(currentLocale)}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_countryCode_validation"></span>
			  </div>
			  <fieldset>
			<h3>${i18nBundle.externalResources}</h3>
			<#if weatherStation.pointOfInterestExternalResourceSet?has_content>
				<#list weatherStation.pointOfInterestExternalResourceSet as poiExternalResource>
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
			  <#if user.isSuperUser()>
			  <div class="form-group">
			    <label for="userId">${i18nBundle.vipsLogicUserId}</label>
			    <select class="form-control" name="userId" onblur="validateField(this);">
			    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.vipsLogicUserId?lower_case}</option>
				<#list users as user>
					<option value="${user.userId}"<#if weatherStation.userId?has_content && user.userId == weatherStation.userId
								> selected="selected"</#if>>${user.lastName}, ${user.firstName} (${user.organizationId.organizationName})</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_userId_validation"></span>
			  </div>
                          <#else>
                          <input type="hidden" name="userId" value="<#if weatherStation.userId?has_content>${weatherStation.userId}<#else>${user.userId}</#if>"/>
			  </#if>
			  <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
			  <#if weatherStation.pointOfInterestId?has_content>
			  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/weatherStation?action=deleteWeatherStationPreview&pointOfInterestId=${weatherStation.pointOfInterestId}';}">${i18nBundle.delete}</button>
			  </#if>
			</form>
		</div>
		<div class="col-md-6">
			<div id="weatherStationFormMap" class="map">
				<div id="popover"></div>
			</div>
		</div>
	</div>
	<div style="display: none;"><div id="stationMarker" title="Marker"><img src="/public/images/anemometer_mono.png"/></div></div>
</div>
</#macro>
<@page_html/>
