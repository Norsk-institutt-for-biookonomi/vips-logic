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
        <title>${i18nBundle.weatherStations}</title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/>
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/weatherStationListMap.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			initMap([${(defaultMapCenter.x?c)!"0"},${(defaultMapCenter.y?c)!"0"}],${defaultMapZoom!"1"},${organizationId!user.organizationId.organizationId});
		});
		
		function changeOrganization(selectList)
		{
			var organizationId = selectList.options[selectList.selectedIndex].value;
			window.location="/weatherStation?organizationId=" + organizationId;
		}
	</script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
<h1>${i18nBundle.weatherStations}</h1>
<#if messageKey?has_content>
	<div class="alert alert-success">${i18nBundle(messageKey)}</div>
</#if>
<p>
	<a href="/weatherStation?action=newWeatherStationForm&organizationId=${organizationId}" class="btn btn-default" role="button">${i18nBundle.addNew}</a>
</p>
<div class="row">
	<div class="col-md-8">
		<div id="weatherStationListMap" class="map">
			<div id="popover"></div>
		</div>
	</div>
	<div class="col-md-4">
		 <#if user.isSuperUser() >
		<div class="form-group">
			<label for="organizationId">${i18nBundle.organizationId}</label>
			<select name="organizationId" class="form-control" onchange="changeOrganization(this);">
				<option value="-1">${i18nBundle.allOrganizations}</option>
				<#list organizations?sort_by("organizationName") as organization>
				<option value="${organization.organizationId}" <#if organizationId == organization.organizationId> selected="selected"</#if>>
				${organization.organizationName}
				</option>
				</#list>
			</select>
		</div>
		 </#if>
		 <div class="table-responsive">
		 	<h3>${i18nBundle.activeWeatherStations}</h3>
			 <table class="table table-striped">
				<thead>
					<th>${i18nBundle.name}</th>
					<#if user.isOrganizationAdmin() || user.isSuperUser() >
					<th></th>
					</#if>
					<th>${i18nBundle.dataSourceName}</th>
				</thead>
				<tbody>
				<#list activeWeatherStations?sort_by("name") as weatherStation>
				    <tr>
					<td>
						<a href="weatherStation?pointOfInterestId=${weatherStation.pointOfInterestId}">${weatherStation.name!""}</a>
					</td>
					<#if user.isOrganizationAdmin() || user.isSuperUser() >
					<td>
						<a href="/weatherStation?action=editWeatherStationForm&pointOfInterestId=${weatherStation.pointOfInterestId}" class="btn btn-default" role="button">${i18nBundle.edit}</a>
					</td>
					</#if>
					<td>
					<#if weatherStation.weatherStationDataSourceId?has_content>
						<a href="${weatherStation.weatherStationDataSourceId.uri}">${weatherStation.weatherStationDataSourceId.name}</a>
					</#if>
					</td>
				    </tr>
				</#list>
				</tbody>
			</table>
			<h3>${i18nBundle.inactiveWeatherStations}</h3>
			 <table class="table table-striped">
				<thead>
					<th>${i18nBundle.name}</th>
					<#if user.isOrganizationAdmin() || user.isSuperUser() >
					<th></th>
					</#if>
					<th>${i18nBundle.dataSourceName}</th>
				</thead>
				<tbody>
				<#list inactiveWeatherStations?sort_by("name") as weatherStation>
				    <tr>
					<td>
						<a href="weatherStation?pointOfInterestId=${weatherStation.pointOfInterestId}">${weatherStation.name!""}</a>
					</td>
					<#if user.isOrganizationAdmin() || user.isSuperUser() >
					<td>
						<a href="/weatherStation?action=editWeatherStationForm&pointOfInterestId=${weatherStation.pointOfInterestId}" class="btn btn-default" role="button">${i18nBundle.edit}</a>
					</td>
					</#if>
					<td>
					<#if weatherStation.weatherStationDataSourceId?has_content>
						<a href="${weatherStation.weatherStationDataSourceId.uri}">${weatherStation.weatherStationDataSourceId.name}</a>
					</#if>
					</td>
				    </tr>
				</#list>
				</tbody>
			</table>
		</div>
	</div>
</div>
</div>
</#macro>
<@page_html/>
