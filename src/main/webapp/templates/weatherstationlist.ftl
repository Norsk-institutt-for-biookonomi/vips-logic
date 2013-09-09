<#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.weatherStations}</title>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.weatherStations}</h1>
	<ul>
	<#list weatherStations?sort_by("name") as weatherStation>
	    <li><a href="weatherStation?pointOfInterestId=${weatherStation.pointOfInterestId}">${weatherStation.name}</a><#if weatherStation.weatherStationDataSourceId??>, ${weatherStation.weatherStationDataSourceId.uri!}<#else></#if></li>
	</#list>
        </ul>
</#macro>
<@page_html/>
