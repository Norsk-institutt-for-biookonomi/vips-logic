<#-- 
  Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
  
  This file is part of VIPSLogic.
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
--><#include "master.ftl">
<#macro page_head>
        <title><#if organization.organizationId?has_content>${i18nBundle.editOrganization} ${organization.organizationName}<#else>${i18nBundle.newOrganization}</#if></title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/resourcebundle.js"></script>
	<script type="text/javascript" src="/js/validateForm.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			// Load main form definition (for validation)
			loadFormDefinition("organizationForm");
			
		});
                
                
	</script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
        <#if user.isSuperUser() >
	<p><a href="/organization" class="btn btn-default back" role="button">${i18nBundle.back}</a></p>
        </#if>
        <h1><#if organization.organizationId?has_content>${i18nBundle.editOrganization} ${organization.organizationName}<#else>${i18nBundle.newOrganization}</#if></h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<div class="row">
		<div class="col-md-12">
			<#assign formId = "organizationForm">
			<form id="${formId}" role="form" action="/organization?action=organizationFormSubmit" method="POST" onsubmit="return validateForm(this);">
			  
			  <input type="hidden" name="organizationId" value="${organization.organizationId!"-1"}"/>
			  <div class="form-group">
			    <label for="organizationName">${i18nBundle.name}</label>
			    <input type="text" class="form-control" name="organizationName" placeholder="${i18nBundle.name}" value="${(organization.organizationName)!""}" onblur="validateField(this); "/>
			    <span class="help-block" id="${formId}_organizationName_validation"></span>
			  </div>
                          <div class="form-group">
			    <label for="address1">${i18nBundle.address} 1</label>
			    <input type="text" class="form-control" name="address1" placeholder="${i18nBundle.address} 1" value="${(organization.address1)!""}" onblur="validateField(this); "/>
			    <span class="help-block" id="${formId}_address1_validation"></span>
			  </div>
                          <div class="form-group">
			    <label for="address2">${i18nBundle.address} 2</label>
			    <input type="text" class="form-control" name="address2" placeholder="${i18nBundle.address} 2" value="${(organization.address2)!""}" onblur="validateField(this); "/>
			    <span class="help-block" id="${formId}_address2_validation"></span>
			  </div>
                          <div class="form-group">
			    <label for="postalCode">${i18nBundle.postalCode}</label>
			    <input type="text" class="form-control" name="postalCode" placeholder="${i18nBundle.postalCode}" value="${(organization.postalCode)!""}" onblur="validateField(this); "/>
			    <span class="help-block" id="${formId}_postalCode_validation"></span>
			  </div>
                          <div class="form-group">
			    <label for="city">${i18nBundle.city}</label>
			    <input type="text" class="form-control" name="city" placeholder="${i18nBundle.city}" value="${(organization.city)!""}" onblur="validateField(this); "/>
			    <span class="help-block" id="${formId}_city_validation"></span>
			  </div>
                          <div class="form-group">
			    <label for="countryCode">${i18nBundle.country}</label>
			    <select class="form-control" name="countryCode" onblur="validateField(this);">
			    	<option value="">${i18nBundle.pleaseSelect} ${i18nBundle.country?lower_case}
				<#list countries as country>
					<option value="${country.countryCode}"
						<#if organization.countryCode?has_content && organization.countryCode.countryCode == country.countryCode>selected="selected"</#if>
					>${country.getCountryName(currentLocale.language)}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_countryCode_validation"></span>
			  </div>
                          <div class="form-group">
			    <label for="defaultLocale">${i18nBundle.defaultLocale}</label>
			    <select class="form-control" name="defaultLocale" onblur="validateField(this);">
			    	<option value="">${i18nBundle.pleaseSelect} ${i18nBundle.defaultLocale?lower_case}
				<#list currentLocale.getAvailableLocales() as locale>
					<option value="${locale.language}"
						<#if organization.defaultLocale?has_content && organization.defaultLocale == locale.language && organization.countryCode.countryCode == locale.country>selected="selected"</#if>
					>${locale.getDisplayLanguage(currentLocale)} / ${locale.getDisplayCountry(currentLocale)}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_defaultLocale_validation"></span>
			  </div>
                          <div class="form-group">
			    <label for="defaultTimeZone">${i18nBundle.defaultTimeZone}</label>
			    <select class="form-control" name="defaultTimeZone" onblur="validateField(this);">
			    	<option value="">${i18nBundle.pleaseSelect} ${i18nBundle.defaultTimeZone?lower_case}
				<#list timeZones as timeZone>
					<option value="${timeZone}"
						<#if organization.defaultTimeZone?has_content && organization.defaultTimeZone == timeZone>selected="selected"</#if>
					>${timeZone}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_defaultTimeZone_validation"></span>
			  </div>
                          <div class="form-group">
			    <label for="vipswebUrl">${i18nBundle.vipswebUrl}</label>
			    <input type="text" class="form-control" name="vipswebUrl" placeholder="${i18nBundle.vipswebUrl}" value="${(organization.vipswebUrl)!""}" onblur="validateField(this); "/>
			    <span class="help-block" id="${formId}_vipswebUrl_validation"></span>
			  </div>
                <div class="form-group">
			    <label for="archiveUserId">${i18nBundle.archiveUserId}</label>
			    <select class="form-control" name="archiveUserId" onblur="validateField(this);">
			    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.archiveUserId?lower_case}
				<#list organizationUsers as oUser>
					<option value="${oUser.userId}"
						<#if organization.archiveUser?has_content && organization.archiveUser.userId == oUser.userId>selected="selected"</#if>
					>${oUser.lastName}, ${oUser.firstName}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_archiveUserId_validation"></span>
			  </div>
                <div class="form-group">
			    <label for="defaultVipsCoreUserId">${i18nBundle.defaultVipsCoreUserId}</label>
			    <input type="number" class="form-control" name="defaultVipsCoreUserId" placeholder="${i18nBundle.defaultVipsCoreUserId}" value="${(organization.defaultVipsCoreUserId)!""}" onblur="validateField(this); "/>
			    <span class="help-block" id="${formId}_defaultVipsCoreUserId_validation"></span>
			  </div>
                <div class="form-group">
			    <label for="defaultMapZoom">${i18nBundle.defaultMapZoom}</label>
			    <input type="number" class="form-control" name="defaultMapZoom" placeholder="${i18nBundle.defaultMapZoom}" value="${(organization.defaultMapZoom)!""}" onblur="validateField(this); "/>
			    <span class="help-block" id="${formId}_defaultMapZoom_validation"></span>
			  </div>
                <div class="form-group">
			    <label for="defaultMapCenter">${i18nBundle.defaultMapCenter} (WGS84 longitude,latitude)</label>
			    <input type="text" class="form-control" name="defaultMapCenter" placeholder="${i18nBundle.defaultMapCenter}" value="${(defaultMapCenterLon?c)!""},${(defaultMapCenterLat?c)!""}" onblur="validateField(this); "/>
			    <span class="help-block" id="${formId}_defaultMapCenter_validation"></span>
			  </div>
			  <div class="form-group">
			    <label for="defaultGridWeatherStationDataSourceId">${i18nBundle.defaultGridWeatherStationDataSource}</label>
			    <select class="form-control" name="defaultGridWeatherStationDataSourceId" onblur="validateField(this);">
			    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.defaultGridWeatherStationDataSource?lower_case}
				<#list gridWeatherDataSources as dSource>
					<option value="${dSource.weatherStationDataSourceId}"
						<#if organization.defaultGridWeatherStationDataSource?has_content && organization.defaultGridWeatherStationDataSource.weatherStationDataSourceId == dSource.weatherStationDataSourceId>selected="selected"</#if>
					>${dSource.name}</option>
				</#list>
			     </select>
			     <span class="help-block" id="${formId}_defaultGridWeatherStationDataSourceId_validation"></span>
			  </div>
			  <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
			  <#if organization.organizationId?has_content>
			  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){alert('Sorry, but this functionality has not yet been implemented');}">${i18nBundle.delete}</button>
			  </#if>
			</form>
		</div>
	</div>
</div>
</#macro>
<@page_html/>
