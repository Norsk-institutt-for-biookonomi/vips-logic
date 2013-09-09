<#include "master.ftl">
<#macro page_head>
        <title>${weatherStation.name}</title>
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
	<#else>
	<p>${i18nBundle.dataSourceMissing}</p>
	</#if>
	
</#macro>
<@page_html/>
