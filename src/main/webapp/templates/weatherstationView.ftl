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
        <title>${weatherStation.name}</title>
        
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
</#macro>
<#macro custom_js>
<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
<script type="text/javascript" src="/js/constants.js"></script>
<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
<link href="http://code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
<script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
<script type="text/javascript" src="/js/weatherStationViewMap.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		
		// Initialize the map
		// If weather station already registered, center on location
		// Otherwise, center and zoom to organizations's default
		<#if weatherStation.longitude?has_content && weatherStation.latitude?has_content>
		initMap([${(weatherStation.longitude?c)!""},${(weatherStation.latitude?c)!""}],10,${weatherStation.pointOfInterestId!"null"});
		<#else>
		initMap([${defaultMapCenter.x?c},${defaultMapCenter.y?c}],${defaultMapZoom},${weatherStation.pointOfInterestId!"null"});
		</#if>
	});
	// Make sure that there is a date picker present for HTML5 
	// date input fields
	if (!Modernizr.inputtypes.date) {
	    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
	}
</script>
</#macro>

<#macro page_contents>
	<p><a href="/weatherStation" class="btn btn-default back" role="button">${i18nBundle.back}</a></p>
        <h1>${weatherStation.name}</h1>
        <div class="row">
		<div class="col-md-6">
			<dl class="dl-horizontal">
				<dt>${i18nBundle.location}</dt>
				<dd>${(weatherStation.longitude?c)!""},${(weatherStation.latitude?c)!""}</dd>
				
				<dt>${i18nBundle.altitude} (${i18nBundle.meter})</dt>
				<dd>${(weatherStation.altitude?c)!""}</dd>
				
				<dt>${i18nBundle.dataSource}</dt>
				<dd>
					<#if weatherStation.weatherStationDataSourceId?has_content> 
						<a href="${weatherStation.weatherStationDataSourceId.uri}">${weatherStation.weatherStationDataSourceId.name}</a>
					<#else>
						${i18nBundle.dataSourceMissing}
					</#if>
				</dd>
				
				<dt>${i18nBundle.weatherStationRemoteId}</dt>
				<dd>${(weatherStation.weatherStationRemoteId)!i18nBundle.missing}</dd>
				
				<dt>${i18nBundle.timeZone}</dt>
				<dd>${(weatherStation.timeZone)!""}</dd>
				
				<dt>${i18nBundle.country}</dt>
				<dd>${weatherStation.countryCode.getCountryName(currentLocale)}</dd>
				
				<dt>${i18nBundle.vipsLogicUserId}</dt>
				<dd>${user.lastName}, ${user.firstName} (${user.organizationId.organizationName})</dd>
				
			</dl>
			<#if weatherStation.weatherStationDataSourceId?has_content>
			<h3>${i18nBundle.test} ${i18nBundle.dataSource?lower_case}</h3>
			<form action="${weatherStation.dataFetchUri}" method="POST" target="new" class="form-horizontal" role="form">
				<div class="form-group">
					<label class="col-lg-4 control-label" for="elementMeasurementTypes[]">${i18nBundle.elementMeasurementTypes}</label>
					<div class="col-lg-8">
						<select name="elementMeasurementTypes[]" multiple="multiple" cols="10" size="5">
							<option value="TM">TM</option>
							<option value="RR">RR</option>
							<option value="BT">BT</option>
							<option value="Q0">Q0</option>
							<option value="UM">UM</option>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="col-lg-4 control-label" for="logInterval">${i18nBundle.logInterval}</label>
					<div class="col-lg-8">
						<select name="logInterval">
							<option value="1h" selected="selected">${i18nBundle.logInterval1h}</option>
							<option value="1d">${i18nBundle.logInterval1d}</option>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="col-lg-4 control-label" for="startDate">${i18nBundle.startTime}</label>
					<div class="col-lg-8">
						<input type="date" name="startDate" size="15"/>
						<select name="startTime">
						<#list 0..23 as hour>
							<option value="${hour?string("00")}"<#if hour == 0> selected="selected"</#if>>${hour?string("00")}</option>
						</#list>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="col-lg-4 control-label" for="endDate">${i18nBundle.endTime}</label>
					<div class="col-lg-8">
						<input type="date" name="endDate" size="15"/>
						<select name="endTime">
						<#list 0..23 as hour>
							<option value="${hour?string("00")}"<#if hour == 0> selected="selected"</#if>>${hour?string("00")}</option>
						</#list>
						</select>
					</div>
				</div>
				<div class="form-group">
					<label class="col-lg-4 control-label" for="endDate">${i18nBundle.timeZone}</label>
					<div class="col-lg-8">
						<select name="timeZone">
						<!--
						<#list -12..12 as offset>
							<option value="<#if (offset >= 0)>+</#if>${offset?string("00")}"<#if offset == 0> selected="selected"</#if>><#if (offset >= 0)>+</#if>${offset?string("00")}</option>
						</#list>
						-->
						<#list availableTimeZones as timeZoneId>
						<option value="${timeZoneId}"<#if timeZoneId == weatherStation.timeZone> selected="selected"</#if>>${timeZoneId}</option>
						</#list>
						</select>
					</div>
				</div>
				<div class="form-group">
					<div class="col-lg-offset-4 col-lg-8">
						<button type="submit" class="btn btn-default">Test</button>
					</div>
				</div>
			
			</form>
			</#if>
		</div>
		<div class="col-md-6">
			<div id="weatherStationViewMap" class="map">
			</div>
		</div>
	</div>
        
	
	
</#macro>
<@page_html/>
