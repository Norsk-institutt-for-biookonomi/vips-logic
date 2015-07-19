<#-- 
  Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
  
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
<#macro page_head>
        <title>${i18nBundle.observations}</title>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.observations}</h1>
        <p>TODO: Add map and filters for organism and time</p>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<a href="/observation?action=newObservationForm" class="btn btn-default" role="button">${i18nBundle.addNew}</a>
        <div class="table-responsive">
	<table class="table table-striped">
		<thead>
			<th>${i18nBundle.timeOfObservation}</th>
			<th>${i18nBundle.organism}</th>
			<th></th>
		</thead>
		<tbody>
		<#list observations as observation>
		    <tr>
		    	<td>${observation.timeOfObservation?string("yyyy-MM-dd HH:mmZ")}</td>
		    	<td>${observation.organism.latinName!""}/${observation.organism.getLocalName(currentLocale.language)!""}</td>
		    	<td><#if userHasObserverPrivilege><a href="/observation?action=editObservationForm&observationId=${observation.observationId}" class="btn btn-default" role="button">${i18nBundle.edit}</a></#if></td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>
</#macro>
<@page_html/>
