<#-- 
  Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
  
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
        <title>${i18nBundle.deletePoi}</title>
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
        <h1>${i18nBundle.deletePoi} ${poi.name}</h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<p>${i18nBundle.deletePoiPreviewExplanation}</p>
	<h2>${i18nBundle.forecasts}</h2>
	<#if forecastConfigurations?has_content>
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
	<#else>
		<p>${i18nBundle.noForecasts}</p>
	</#if>
	<h2>${i18nBundle.timeSeriesList}</h2>
	<#if observationTimeSeries?has_content>
		<table class="table table-striped">
			<thead>
			<th>${i18nBundle.observationTimeSeriesName}</th>
			<th>${i18nBundle.year}</th>
			<th>${i18nBundle.cropOrganismId}</th>
			<th>${i18nBundle.organism}</th>
			<th></th>
			</thead>
			<tbody>
			<#list observationTimeSeries as ots>
				<tr>
					<td>${ots.name}</td>
					<td>${ots.year}</td>
					<td>${ots.cropOrganism.latinName}</td>
					<td>${ots.organism.latinName}</td>
					<td><a href="/observationTimeSeries?action=editObservationTimeSeriesForm&observationTimeSeriesId=${ots.observationTimeSeriesId}" target="new">${i18nBundle.edit}</a></td>
				</tr>
			</#list>
			</tbody>
		</table>
	<#else>
		<p>${i18nBundle.noTimeSeries}</p>
	</#if>
    <h2>${i18nBundle.observations}</h2>
	<#if observations?has_content>
		<table class="table table-striped">
			<thead>
			<th>${i18nBundle.timeOfObservation}</th>
			<th>${i18nBundle.cropOrganismId}</th>
			<th>${i18nBundle.pestOrganismId}</th>
			<th>${i18nBundle.observationHeading}</th>
			<th></th>
			</thead>
			<tbody>
			<#list observations as observation>
				<tr>
					<td>${observation.timeOfObservation}</td>
					<td>${observation.cropOrganism.latinName}</td>
					<td>${observation.organism.latinName}</td>
					<td>${observation.observationHeading}</td>
					<td><a href="/observation?action=editObservationForm&observationId=${observation.observationId}" target="new">${i18nBundle.edit}</a></td>
				</tr>
			</#list>
			</tbody>
		</table>
	<#else>
		<p>${i18nBundle.noObservations}</p>
	</#if>

	<p>
		<a href="${returnURL}" class="btn btn-default cancel" role="button">${i18nBundle.cancel}</a>
		<button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/poi?action=deletePoi&pointOfInterestId=${poi.pointOfInterestId}';}">${i18nBundle.deletePoi}</button>
	</p>
</div>
</#macro>
<@page_html/>
