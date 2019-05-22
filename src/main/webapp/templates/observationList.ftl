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
<#setting time_zone=user.organizationId.defaultTimeZone!"UTC">
<#macro page_head>
        <title>${i18nBundle.observations}</title>
</#macro>
<#macro custom_css>
	<link href="/css/3rdparty/chosen.min.css" rel="stylesheet" />
        <link rel="stylesheet" type="text/css" href="/css/3rdparty/jquery.datetimepicker.css"/>
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/3rdparty/chosen.jquery.min.js"></script>
        <script type="text/javascript" src="/js/3rdparty/jquery.datetimepicker.js"></script>
        <script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
	<script type="text/javascript">
		$(document).ready( function() {
                    // Make sure that there is a date picker present for HTML5 
                    // date input fields
                    if (!Modernizr.inputtypes.date) {
                        $('input[type=date]').datetimepicker({
                                    timepicker: false,
                                    lang:"no",
                                    dayOfWeekStart: 1,
                                    format: "Y-m-d"
                            });
                    }
                    // Activating chosen plugin
                    $(".chosen-select").chosen();
		});
	</script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
        <h1>${i18nBundle.observations}</h1>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<a href="/observation?action=newObservationForm" class="btn btn-default" role="button">${i18nBundle.addNew}</a>
        <#if shortcuts?has_content>
            <h4>${i18nBundle.registrationShortcuts}</h4>
            <#list shortcuts as shortcut>
            <a href="/observation?action=newObservationForm${shortcut.formQueryParamString}&observationFormShortcutId=${shortcut.observationFormShortcutId}" class="btn btn-default" role="button">${shortcut.getLocalLabel(currentLocale.language)}</a>
            </#list>
            <h4>${i18nBundle.filterList}</h4>
        </#if>
        
        <form action="/observation" method="get">
            <div class="form-inline">
                <div class="form-group">
                    <select class="form-control" name="statusTypeId">
                            <option value="-1" <#if 1 == selectedStatusTypeId>selected="selected"</#if>>${i18nBundle.allCategories}</option>
                            <#list [1,2,3] as statusTypeId>
                            <option value="${statusTypeId}" <#if statusTypeId == selectedStatusTypeId>selected="selected"</#if>><#switch statusTypeId><#case 1>${i18nBundle.pending}<#break><#case 2>${i18nBundle.rejected}<#break><#case 3>${i18nBundle.approved}</#switch></option>
                            </#list>
                    </select>
                </div>
                <div class="form-group">
                    <select class="form-control" name="pestOrganismId">
                        <option value="-1">${i18nBundle.allPests}</option>
                        <#list allPests as pestOrganism>
                            <option value="${pestOrganism.organismId}" <#if selectedPestOrganismId?has_content && pestOrganism.organismId == selectedPestOrganismId> selected="selected"</#if>>
                                ${pestOrganism.getLocalName(currentLocale.language)!""} (${pestOrganism.latinName!""}) ${hierarchyCategories.getName(pestOrganism.hierarchyCategoryId)?upper_case}
                            </option>
                        </#list>
                    </select>
                </div>
                <div class="form-group">
                    <input type="date" class="form-control" name="timeOfObservationFrom" value="${(timeOfObservationFrom?date)!""}" placeholder="${i18nBundle.dateStart}"/>
                </div> &nbsp;&ndash;&nbsp;
                <div class="form-group">
                    <input type="date" class="form-control" name="timeOfObservationTo" value="${(timeOfObservationTo?date)!""}" placeholder="${i18nBundle.dateEnd}"/>
                </div>
            </div>
            <div class="form-inline">
                <div class="form-group">
                    <select class="form-control chosen-select" name="organizationGroupId" multiple="multiple" data-placeholder="${i18nBundle.organizationGroupList}">
                        <#list organizationGroups as group>
                        <option value="${group.organizationGroupId}" <#if organizationGroupId?has_content && organizationGroupId?seq_contains(group.organizationGroupId)> selected="selected"</#if>> ${group.groupName}</option>
                        </#list>
                    </select>
                </div>
            </div>
            <#if user.isSuperUser() || user.isOrganizationAdmin() || userIsObservationAuthority>
            <div class="form-group">
                <div class="checkbox">
                        <label>
                            <input type="checkbox" value="true" name="viewOthersObservations"
                                   <#if viewOthersObservations >checked="checked"</#if>
                                   />
                        </label>
                        ${i18nBundle.viewOthersObservations}
                </div>
            </div>
            </#if>
            <button type="submit" class="btn btn-default">Bruk filter</button>
        </form>
        <#if observations?size == 0>
        <div class="alert alert-warning">${i18nBundle.noResultsFoundForCriteria}</div>
        </#if>
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
		    	<td><#if user.isSuperUser() || user.isOrganizationAdmin() || userIsObservationAuthority || observation.userId == user.userId><a href="/observation?action=editObservationForm&observationId=${observation.observationId}" class="btn btn-default" role="button">${i18nBundle.edit}</a></#if></td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>
</div>
</#macro>
<@page_html/>
