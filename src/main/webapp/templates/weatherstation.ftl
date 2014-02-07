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

<#macro custom_js>
<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
<link href="http://code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
<script type="text/javascript" src="/js/modernizr_custom.js"></script>
<script type="text/javascript">
	// Make sure that there is a date picker present for HTML5 
	// date input fields
	if (!Modernizr.inputtypes.date) {
	    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
	}
</script>
</#macro>

<#macro page_contents>
        <h1>${weatherStation.name}</h1>
        <h2>${i18nBundle.position}</h2>
	<ul>
		<li>${i18nBundle.latitude}: ${weatherStation.latitude!i18nBundle.missing}</li>
		<li>${i18nBundle.longitude}: ${weatherStation.longitude!i18nBundle.missing}</li>
		<li>${i18nBundle.altitude}: ${weatherStation.altitude!i18nBundle.missing}</li>
	</ul>
	<iframe width="300" height="200" frameborder="0" scrolling="no" marginheight="0" marginwidth="0" 
	src="http://www.openstreetmap.org/export/embed.html?bbox=${(weatherStation.longitude-0.037722588)?c}%2C${(weatherStation.latitude-0.016374048)?c}%2C${(weatherStation.longitude+0.037722588)?c}%2C${(weatherStation.latitude+0.016374048)?c}&amp;layer=mapnik&amp;marker=${weatherStation.latitude?c}%2C${weatherStation.longitude?c}" 
	style="border: 1px solid black"></iframe><br/>
	<a href="http://www.openstreetmap.org/?mlat=${weatherStation.latitude?c}&amp;mlon=${weatherStation.longitude?c}#map=14/${weatherStation.latitude?c}/${weatherStation.longitude?c}&amp;layers=N" target="new">${i18nBundle.viewFullScreen}</a>
	<h2>${i18nBundle.dataSource}</h2>
	<#if weatherStation.weatherStationDataSourceId??>
	<ul>
		<li>${i18nBundle.name}: ${weatherStation.weatherStationDataSourceId.name}</li>
		<li>URI: <a href="${weatherStation.weatherStationDataSourceId.uri}" target="new">${weatherStation.weatherStationDataSourceId.uri}</a></li>
		<li>${i18nBundle.weatherStationRemoteId}: ${weatherStation.weatherStationRemoteId!i18nBundle.weatherStationRemoteIdMissing}</li>
	</ul>
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
	<#else>
	<p>${i18nBundle.dataSourceMissing}</p>
	</#if>
</#macro>
<@page_html/>
