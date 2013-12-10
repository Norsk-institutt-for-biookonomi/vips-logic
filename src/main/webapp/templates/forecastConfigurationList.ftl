<#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.forecasts}</title>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.forecasts}</h1>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<button type="button" class="btn btn-default" onclick="window.location.href='/forecastConfiguration?action=viewForecastConfiguration&forecastConfigurationId=-1'">${i18nBundle.addNew}</button>
        <div class="table-responsive">
	<table class="table table-striped">
		<thead>
			<th>${i18nBundle.modelId}</th>
			<th>${i18nBundle.poi}</th>
			<th>${i18nBundle.dateStart}</th>
			<th>${i18nBundle.dateEnd}</th>
		</thead>
		<tbody>
		<#list forecastConfigurations?sort_by("dateStart")?reverse as forecastConfiguration>
		    <tr style="cursor: pointer;" onclick="window.location.href='/forecastConfiguration?action=viewForecastConfiguration&forecastConfigurationId=${forecastConfiguration.forecastConfigurationId}';">
		    	<td>
				<#if i18nBundle.containsKey(forecastConfiguration.modelId)>
					${i18nBundle[forecastConfiguration.modelId]}
				<#else>
					${modelInformation[forecastConfiguration.modelId].defaultName}
				</#if>
		    	</td>
		    	<td>${forecastConfiguration.locationPointOfInterestId.name}</td>
		    	<td>${forecastConfiguration.dateStart}</td>
		    	<td>${forecastConfiguration.dateEnd}</td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>
</#macro>
<@page_html/>
