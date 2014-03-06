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
        <title>${i18nBundle.weatherStations}</title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/weatherStationListMap.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			initMap([${defaultMapCenter.x?c},${defaultMapCenter.y?c}],${defaultMapZoom});
		});
	</script>
</#macro>
<#macro page_contents>
<h1>${i18nBundle.weatherStations}</h1>
<p>
	<a href="/weatherStation?action=newWeatherStationForm" class="btn btn-default" role="button">${i18nBundle.addNew}</a>
</p>
<div class="row">
	<div class="col-md-8">
		<div id="weatherStationListMap" class="map">
			<div id="popover"></div>
		</div>
	</div>
	<div class="col-md-4">
		 <div class="table-responsive">
			 <table class="table table-striped">
				<thead>
					<th>${i18nBundle.name}</th>
					<#if user.isOrganizationAdmin() || user.isSuperUser() >
					<th></th>
					</#if>
					<th>${i18nBundle.dataSourceName}</th>
				</thead>
				<tbody>
				<#list weatherStations?sort_by("name") as weatherStation>
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
</#macro>
<@page_html/>
