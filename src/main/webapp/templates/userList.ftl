<#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.users}</title>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.users}</h1>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <div class="table-responsive">
	<table class="table table-striped">
		<thead>
			<th>Name</th>
			<th>Status</th>
		</thead>
		<tbody>
		<#list users?sort_by("lastName") as user>
		    <tr>
		    	<td><a href="/user?action=viewUser&userId=${user.userId}">${user.firstName} ${user.lastName}</a></td>
		    	<td>${i18nBundle["userStatus_" + user.userStatusId]}</td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>
</#macro>
<@page_html/>
