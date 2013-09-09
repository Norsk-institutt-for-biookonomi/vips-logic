<#include "master.ftl">
<#macro page_head>
        <title>POI</title>
</#macro>
<#macro page_contents>
        <h1>POI</h1>
	<ul>
	<#list weatherstations?sort_by("name") as weatherstation>
	    <li>${weatherstation.name}<#if weatherstation.weatherStationDataSourceId??>, ${weatherstation.weatherStationDataSourceId.uri!}<#else></#if></li>
	</#list>
        </ul>
</#macro>
<@page_html/>
