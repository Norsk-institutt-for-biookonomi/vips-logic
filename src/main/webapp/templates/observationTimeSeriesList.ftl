<#include "master.ftl">
<#setting time_zone=user.organizationId.defaultTimeZone!"UTC">
<#macro page_head>
    <title>${i18nBundle.timeSeriesList}</title>
</#macro>
<#macro custom_css>
    <link href="/css/3rdparty/chosen.min.css" rel="stylesheet"/>
</#macro>
<#macro custom_js>
    <script type="text/javascript" src="/js/3rdparty/chosen.jquery.min.js"></script>
    <script type="text/javascript" src="/js/3rdparty/jquery.datetimepicker.js"></script>
</#macro>
<#macro page_contents>
    <div class="singleBlockContainer">
        <h1>${i18nBundle.timeSeriesList}</h1>
        <#if messageKey?has_content>
            <div class="alert alert-success">${i18nBundle(messageKey)}</div>
        </#if>
        <a href="/observationTimeSeries?action=newObservationTimeSeriesForm" class="btn btn-default" role="button">${i18nBundle.addNew}</a>

        <div class="table-responsive">
            <table class="table table-striped">
                <thead>
                <th>Tittel</th>
                <th>År</th>
                <th>${i18nBundle.cropOrganismId}</th>
                <th>${i18nBundle.organism}</th>
                <th>${i18nBundle.location}</th>
                <th>${i18nBundle.observer}</th>
                <th>${i18nBundle.observationCount}</th>
                <th></th>
                </thead>
                <tbody>
                <#list observationTimeSeriesList as timeSeries>
                    <tr>
                        <td>${timeSeries.name}</td>
                        <td>${timeSeries.year}</td>
                        <td>${timeSeries.cropOrganism.latinName!""}
                            /${timeSeries.cropOrganism.getLocalName(currentLocale.language)!""}</td>
                        <td>${timeSeries.organism.latinName!""}
                            /${timeSeries.organism.getLocalName(currentLocale.language)!""}</td>
                        <td><#if timeSeries.locationPointOfInterest?has_content>${timeSeries.locationPointOfInterest.name!""}</#if></td>
                        <td><#if timeSeries.user?has_content>${timeSeries.user.firstName!""} ${timeSeries.user.lastName!""}</#if></td>
                        <td>${observationCounts[timeSeries.observationTimeSeriesId?string]}</td>
                        <td><#if userIsObservationAuthority || timeSeries.userId == user.userId>
                                <a
                                href="/observationTimeSeries?action=editObservationTimeSeriesForm&observationTimeSeriesId=${timeSeries.observationTimeSeriesId}"
                                class="btn btn-default" role="button">${i18nBundle.edit}</a></#if></td>
                    </tr>
                </#list>
                </tbody>
            </table>
        </div>

    </div>
</#macro>
<@page_html/>
