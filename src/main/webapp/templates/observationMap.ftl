<#-- 
  Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
  
  This file is part of VIPSLogic.
  VIPSLogic is free software: you can redistribute it and/or modify
  it under the terms of the NIBIO Open Source License as published by 
  NIBIO, either version 1 of the License, or (at your option) any
  later version.
  
  VIPSLogic is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  NIBIO Open Source License for more details.
  
  You should have received a copy of the NIBIO Open Source License
  along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
--><#include "master.ftl">
<#setting time_zone=user.organizationId.defaultTimeZone!"UTC">
<#macro page_head>
        <title>${i18nBundle.observationMap}</title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/>
        <link rel="stylesheet" type="text/css" href="/css/3rdparty/chosen.min.css"/>
</#macro>
<#macro custom_js>
        <script type="text/javascript" src="/js/3rdparty/ol.js"></script>
        <script type="text/javascript" src="/js/observationMap.js"></script>
        <script type="text/javascript" src="/js/3rdparty/moment.min.js"></script>
        <script type="text/javascript" src="/js/3rdparty/chosen.jquery.min.js"></script>
        <script type="text/javascript" src="/js/constants.js"></script>
        <script type="text/javascript" src="/js/util.js"></script>
        <script type="text/javascript" src="/js/environment.js"></script>
        <script type="text/javascript">
		$(document).ready(function() {
			initMap(
                            [${(defaultMapCenter.x?c)!"0"}, ${(defaultMapCenter.y?c)!"0"}],
                            ${defaultMapZoom!"1"},
                            ${organizationId!user.organizationId.organizationId},
                            "${from?date}",
                            "${to?date}",
                            ${pestId!"null"},
                            ${cropId!"null"},
                            ${cropCategoryId!"null"}
                        );
                        
                        
                        
                        initForm(${organizationId!user.organizationId.organizationId},
                            ${pestId!"null"},
                            ${cropId!"null"},
                            ${cropCategoryId!"null"},
                            function() {$(".chosen-select").chosen({allow_single_deselect: true});} // Must do this after select lists have been populated
                        );
                        
                        
                        
		});
        </script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
        <h1>${i18nBundle.observationMap}</h1>
        
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <div id="observationMap" class="map">
            <div id="popover"></div>
        </div>
        <form class="form-inline" method="get" action="/observation/map">
            <div class="form-group">
                <input class="form-control" type="date" name="from" value="${from?date}"/>
            </div>
            -
            <div class="form-group">
                <input class="form-control" type="date" name="to" value="${to?date}"/>
            </div>
            <div class="form-group">
                    <select name="pestId" id="pestList" class="form-control chosen-select" data-placeholder="${i18nBundle.pestOrganismId}">
                    </select>
            </div>
            <div class="form-group">
                    <select name="cropId" id="cropList" class="form-control chosen-select" data-placeholder="${i18nBundle.cropOrganismId}">	
                    </select>
            </div>
            <div class="form-group">
                    <select name="cropCategoryId" id="cropCategoryList" class="form-control chosen-select" data-placeholder="${i18nBundle.cropCategoryIds}">	
                    </select>
            </div>
            <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
        </form>
        <div class="table-responsive">
            <table class="table table-striped" id="observationTable">
                    <thead>
                            <th>${i18nBundle.timeOfObservation}</th>
                            <th>${i18nBundle.organism}</th>
                            <th>${i18nBundle.cropOrganismId}</th>
                            <th>${i18nBundle.location}</th>
                            <th>${i18nBundle.observer}</th>
                            <th>${i18nBundle.heading}</th>
                            <th></th>
                    </thead>
                    <tbody id="observationTableBody">
                    </tbody>
            </table>
        </div>
</div>
</#macro>
<@page_html/>
