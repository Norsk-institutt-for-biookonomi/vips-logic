<#-- 
    Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
  
  This file is part of VIPSLogic.
  VIPSLogic is free software: you can redistribute it and/or modify
  it under the terms of the Bioforsk Open Source License as published by 
  Bioforsk, either version 1 of the License, or (at your option) any
  later version.
  
  VIPSLogic is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  Bioforsk Open Source License for more details.
  
  You should have received a copy of the Bioforsk Open Source License
  along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
--><#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.forecasts}</title>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.forecasts}</h1>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<div class="panel panel-default">
		<div class="panel-body">
			<form class="form-inline" role="form" action="/forecastConfiguration" method="GET">
				<div class="form-group">
					<label class="sr-only" for="modelId">Model</label>
					<select name="modelId" multiple="multiple" size="3">
						<#list modelInformation?values?sort_by("defaultName") as modelInformationItem>
						<option value="${modelInformationItem.modelId}" ${selectedModelIds?seq_contains(modelInformationItem.modelId)?string(" selected=\"selected\"","")}>
						<#if i18nBundle.containsKey(modelInformationItem.modelId)>
							${i18nBundle[modelInformationItem.modelId]}
						<#else>
							${modelInformation[modelInformationItem.modelId].defaultName}
						</#if>
						</option>
						</#list>
					</select>
				</div>
				<#if user.isSuperUser() >
				<div class="form-group">
					<label class="sr-only" for="organizationId">Model</label>
					<select name="organizationId" size="3" multiple="multiple">
						<#list organizations?sort_by("organizationName") as organization>
						<option value="${organization.organizationId}" ${selectedOrganizationIds?seq_contains(organization.organizationId)?string(" selected=\"selected\"","")}>
						${organization.organizationName}
						</option>
						</#list>
					</select>
				</div>
				</#if>
				<button type="submit" class="btn btn-default">Filter</button>
			</form>
		</div>
	</div>
	<button type="button" class="btn btn-default" onclick="window.location.href='/forecastConfiguration?action=viewForecastConfiguration&forecastConfigurationId=-1'">${i18nBundle.addNew}</button>
        <div class="table-responsive">
	<table class="table table-striped">
		<thead>
			<th>${i18nBundle.modelId}</th>
			<th>${i18nBundle.poi}</th>
			<th>${i18nBundle.weatherStationPointOfInterestId}</th>
			<th>${i18nBundle.dateStart}</th>
			<th>${i18nBundle.dateEnd}</th>
		</thead>
		<tbody>
		<#list forecastConfigurations as forecastConfiguration>
		    <tr style="cursor: pointer;" onclick="window.location.href='/forecastConfiguration?action=viewForecastConfiguration&forecastConfigurationId=${forecastConfiguration.forecastConfigurationId}';">
		    	<td>
				<#if i18nBundle.containsKey(forecastConfiguration.modelId)>
					${i18nBundle[forecastConfiguration.modelId]}
				<#else>
					${modelInformation[forecastConfiguration.modelId].defaultName}
				</#if>
		    	</td>
		    	<td>${forecastConfiguration.locationPointOfInterestId.name}</td>
		    	<td>${forecastConfiguration.weatherStationPointOfInterestId.name}</td>
		    	<td>${forecastConfiguration.dateStart}</td>
		    	<td>${forecastConfiguration.dateEnd}</td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>
</#macro>
<@page_html/>
