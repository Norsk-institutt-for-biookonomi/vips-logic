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
<div class="singleBlockContainer">
        <h1>${i18nBundle.observations}</h1>
        <p>TODO: Add map and filters for organism and time</p>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<a href="/observation?action=newObservationForm" class="btn btn-default" role="button">${i18nBundle.addNew}</a>

	<select class="form-control" name="statusTypeId" onchange="window.location.href='/observation?statusTypeId=' + this.options[this.options.selectedIndex].value;">
		<#list [-1,1,2,3] as statusTypeId>
		<option value="${statusTypeId}" <#if statusTypeId == selectedStatusTypeId>selected="selected"</#if>><#switch statusTypeId><#case -1>${i18nBundle.allCategories}<#break><#case 1>${i18nBundle.pending}<#break><#case 2>${i18nBundle.rejected}<#break><#case 3>${i18nBundle.approved}</#switch></option>
		</#list>
	</select>
        <div class="table-responsive">
	<table class="table table-striped">
		<thead>
			<th>${i18nBundle.timeOfObservation}</th>
			<th>${i18nBundle.organism}</th>
                        <th>${i18nBundle.cropOrganismId}</th>
                        <th>${i18nBundle.location}</th>
                        <th>${i18nBundle.observer}</th>
                        <th>${i18nBundle.heading}</th>
			<th>${i18nBundle.status}</th>
			<th></th>
		</thead>
		<tbody>
		<#list observations as observation>
		    <tr>
		    	<td>${observation.timeOfObservation?string("yyyy-MM-dd HH:mmZ")}</td>
		    	<td>${observation.organism.latinName!""}/${observation.organism.getLocalName(currentLocale.language)!""}</td>
                        <td>${observation.cropOrganism.latinName!""}/${observation.cropOrganism.getLocalName(currentLocale.language)!""}</td>
		    	<td><#if observation.location?has_content>${observation.location.name!""}</#if></td>
                        <td><#if observation.user?has_content>${observation.user.firstName!""} ${observation.user.lastName!""}</#if></td>
                        <td>${observation.observationHeading}</td>
		    	<td><#switch observation.statusTypeId><#case 1>${i18nBundle.pending}<#break><#case 2>${i18nBundle.rejected}<#break><#case 3>${i18nBundle.approved}</#switch></td>
		    	<td><#if userIsObservationAuthority || observation.userId == user.userId><a href="/observation?action=editObservationForm&observationId=${observation.observationId}" class="btn btn-default" role="button">${i18nBundle.edit}</a></#if></td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>
</div>
</#macro>
<@page_html/>
