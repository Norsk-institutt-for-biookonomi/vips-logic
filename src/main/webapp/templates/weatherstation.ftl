<#include "master.ftl">
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
	<h2>${i18nBundle.dataSource}</h2>
	<#if weatherStation.weatherStationDataSourceId??>
	<ul>
		<li>${i18nBundle.name}: ${weatherStation.weatherStationDataSourceId.name}</li>
		<li>URI: <a href="${weatherStation.weatherStationDataSourceId.uri}" target="new">${weatherStation.weatherStationDataSourceId.uri}</a></li>
		<li>${i18nBundle.weatherStationRemoteId}: ${weatherStation.weatherStationRemoteId!i18nBundle.weatherStationRemoteIdMissing}</li>
	</ul>
	<h3>${i18nBundle.test} ${i18nBundle.dataSource?lower_case}</h3>
	<form action="${dataRequestUri}" method="POST" target="new" class="form-horizontal" role="form">
		<div class="form-group">
			<label class="col-lg-4 control-label" for="elementMeasurementTypes[]">${i18nBundle.elementMeasurementTypes}</label>
			<div class="col-lg-8">
				<select name="elementMeasurementTypes[]" multiple="multiple" cols="10" size="5">
					<option value="TM">TM</option>
					<option value="RR">RR</option>
					<option value="BT">BT</option>
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
				<option value="${timeZoneId}"<#if timeZoneId == defaultTimeZoneId> selected="selected"</#if>>${timeZoneId}</option>
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
