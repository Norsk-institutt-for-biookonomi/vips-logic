<#-- 
  Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
  
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
        <title>${i18nBundle.deleteWeatherStation}</title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/resourcebundle.js"></script>
	<script type="text/javascript" src="/js/validateForm.js"></script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
	<p><a href="${returnURL}" class="btn btn-default cancel" role="button">${i18nBundle.cancel}</a></p>
        <h1>${i18nBundle.deleteWeatherStation} ${weatherStation.name}</h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<p>${i18nBundle.deleteWeatherStationPreviewExplanation}</p>
	<h2>${i18nBundle.weatherStationForecastConfigurations}</h2>
	<table class="table table-striped">
		<thead>
			<th>${i18nBundle.modelId}</th>
			<th>${i18nBundle.dateStart}</th>
			<th>${i18nBundle.dateEnd}</th>
			<th></th>
		</thead>
		<tbody>
			<#list forecastConfigurations as forecastConfiguration>
			<tr>
				<td>
				<#if i18nBundle.containsKey(forecastConfiguration.modelId)>
					${i18nBundle[forecastConfiguration.modelId]}
				<#else>
					${modelInformation[forecastConfiguration.modelId].defaultName}
				</#if>
				</td>
				<td>${forecastConfiguration.dateStart}</td>
				<td>${forecastConfiguration.dateEnd}</td>
				<td><a href="/forecastConfiguration?action=viewForecastConfiguration&forecastConfigurationId=${forecastConfiguration.forecastConfigurationId}" target="new">${i18nBundle.edit}</a></td>
			</tr>
			</#list>
		</tbody>
	</table>
	<p>
		<a href="${returnURL}" class="btn btn-default cancel" role="button">${i18nBundle.cancel}</a>
		<button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/weatherStation?action=deleteWeatherStation&pointOfInterestId=${weatherStation.pointOfInterestId}';}">${i18nBundle.deleteWeatherStation}</button>
	</p>
</div>
</#macro>
<@page_html/>
