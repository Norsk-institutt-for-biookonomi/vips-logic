<#-- 
    Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
  
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
        <title>${i18nBundle.forecasts}</title>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
        <h1>${i18nBundle.forecasts}</h1>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<button type="button" class="btn btn-default" onclick="window.location.href='/forecastConfiguration?action=viewForecastConfiguration&forecastConfigurationId=-1'">${i18nBundle.addNew}</button>
        
        <#if user.isSuperUser() || user.isOrganizationAdmin()>
	<div class="panel panel-default">
		<div class="panel-body">
			<form class="form-inline" role="form" action="/forecastConfiguration" method="GET">
				<div class="form-group">
					<label class="sr-only" for="modelId">Model</label>
					<select name="modelId" id="modelId" multiple="multiple" size="6" class="form-control">
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
					<label class="sr-only" for="organizationId">Organization</label>
					<select name="organizationId" size="6" multiple="multiple" class="form-control">
						<#list organizations?sort_by("organizationName") as organization>
						<option value="${organization.organizationId}" ${selectedOrganizationIds?seq_contains(organization.organizationId)?string(" selected=\"selected\"","")}>
						${organization.organizationName}
						</option>
						</#list>
					</select>
				</div>
				</#if>
				<div class="form-group">
					<input type="date" class="form-control" name="from" placeholder="${i18nBundle.from}" value="${(from?date)!""}"/>
				</div>
				-
				<div class="form-group">
					<input type="date" class="form-control" name="to" placeholder="${i18nBundle.to}" value="${(to?date)!""}"/>
				</div>
				
				<button type="submit" class="btn btn-default">Filter</button>
			</form>
		</div>
	</div>
	<button type="button" class="btn btn-default" onclick="window.location.href='/forecastConfiguration?action=viewForecastConfiguration&forecastConfigurationId=-1&multipleNew=true'">${i18nBundle.addMultipleNew}</button>
        <h2>${i18nBundle.privateForecasts}</h2>
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
		<#list privateForecastConfigurations as forecastConfiguration>
		    <tr style="cursor: pointer;" onclick="window.location.href='/forecastConfiguration?action=viewForecastConfiguration&forecastConfigurationId=${forecastConfiguration.forecastConfigurationId}';">
		    	<td>
				<#if i18nBundle.containsKey(forecastConfiguration.modelId)>
					${i18nBundle[forecastConfiguration.modelId]}
				<#else>
					${modelInformation[forecastConfiguration.modelId].defaultName}
				</#if>
		    	</td>
		    	<td>${forecastConfiguration.locationPointOfInterestId.name}</td>
		    	<td><#if forecastConfiguration.weatherStationPointOfInterestId?has_content>${forecastConfiguration.weatherStationPointOfInterestId.name}</#if></td>
		    	<td>${forecastConfiguration.dateStart}</td>
		    	<td>${forecastConfiguration.dateEnd}</td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>	
	<h2>${i18nBundle.publicForecasts}</h2>
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
		    	<td><#if forecastConfiguration.weatherStationPointOfInterestId?has_content>${forecastConfiguration.weatherStationPointOfInterestId.name}</#if></td>
		    	<td>${forecastConfiguration.dateStart}</td>
		    	<td>${forecastConfiguration.dateEnd}</td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>
        </#if>
        
        <#if user.isSuperUser() >
        <h2>${i18nBundle.privateForecastsForOtherUser}</h2>
        <div class="form-group">
	    <label for="otherUserId">${i18nBundle.user}</label>
	    <select class="form-control" name="otherUserId" onchange="window.location.href='/forecastConfiguration?otherUserId=' + this.options[this.options.selectedIndex].value;">
		<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.user?lower_case}</option>
		<#list allUsers?sort_by("lastName") as otherUser>
			<#if otherUser.userId != user.userId>
				<option value="${otherUser.userId}"
					<#if (otherUserId?has_content && otherUserId == otherUser.userId)>selected="selected"</#if>
				>${otherUser.lastName}, ${otherUser.firstName}</option>
			</#if>
		</#list>
	    </select>
	 </div>
		<#if privateForecastConfigurationsForOtherUser?has_content>
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
			<#list privateForecastConfigurations as forecastConfiguration>
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
		<#elseif otherUserId?has_content>
		<div class="alert alert-warning">${i18nBundle.noPrivateForecastsFoundForUser}</div>
		</#if>
        </#if>
</div>
</#macro>
<#macro custom_js>
        <script type="text/javascript" src="/js/util.js"></script>
	<script type="text/javascript">
            $(document).ready(function() {
               sortListAlphabetically(document.getElementById("modelId")); 
            });
        </script>
</#macro>        
<@page_html/>
