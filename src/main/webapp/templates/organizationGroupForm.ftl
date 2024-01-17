<#-- 
    Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
  
  This file is part of VIPSLogic.
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
--><#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.edit} ${organizationGroup.groupName!i18nBundle.newGroup?lower_case}</title>
</#macro>
<#macro custom_css>
	<link href="/css/3rdparty/chosen.min.css" rel="stylesheet" />
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/3rdparty/chosen.jquery.min.js"></script>
	<script type="text/javascript">
		$(document).ready( function() {
			// Activating chosen plugin
			$(".chosen-select").chosen();
		});
	</script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
	<h1>${i18nBundle.edit} ${organizationGroup.groupName!i18nBundle.newGroup?lower_case}</h1>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<#assign formId = "organizationGroupForm">
	<form id="${formId}" role="form" action="/organizationgroup?action=organizationGroupFormSubmit" method="POST" onsubmit="return validateForm(this);">
		<input type="hidden" name="organizationGroupId" value="${organizationGroup.organizationGroupId!"-1"}"/>
		<div class="form-group">
		    <label for="groupName">${i18nBundle.name}</label>
		    <input type="text" class="form-control" name="groupName" placeholder="${i18nBundle.name}" value="${(organizationGroup.groupName)!""}" onblur="validateField(this);"/>
		    <span class="help-block" id="${formId}_groupName_validation"></span>
		</div>
		<div class="form-group">
		    <label for="organizationGroupUserIds">${i18nBundle.groupMembers}</label>
		    <select class="form-control chosen-select" name="organizationGroupUserIds" onblur="validateField(this);" multiple="multiple">
			<#list users as user>
				<option value="${user.userId}" <#if memberIds?seq_contains(user.userId)> selected="selected"</#if>>${user.lastName}, ${user.firstName}</option>
			</#list>
		     </select>
		</div>
		<button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
		<#if organizationGroup.organizationGroupId?has_content && (user.isOrganizationAdmin() || user.isSuperUser())>
                  <a href="/organizationgroup" class="btn btn-default" role="button">${i18nBundle.cancel}</a>
		  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/organizationgroup?action=deleteOrganizationGroup&organizationGroupId=${organizationGroup.organizationGroupId}';}">${i18nBundle.delete}</button>
                <#else>
                  <a href="?action=viewUserOrganizationGroups&userId=${user.userId}" class="btn btn-default" role="button">${i18nBundle.cancel}</a>
		</#if>
	</form>
</div>
</#macro>
<@page_html/>
