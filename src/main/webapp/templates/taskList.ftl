<#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.tasks}</title>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.tasks}</h1>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <div class="table-responsive">
	<table class="table table-striped">
		<thead>
			<th>${i18nBundle.name}</th>
			<th>${i18nBundle.description}</th>
			<th></th>
		</thead>
		<tbody>
		<#list tasks as task>
		    <tr>
		    	<td>${task.getName(currentLocale)}</td>
		    	<td>${task.getDescription(currentLocale)}</td>
		    	<td><a href="/scheduling?action=runTaskManuallyForm&factoryId=${task.factoryId}">${i18nBundle.configureAndRunManually}</a></td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>
</#macro>
<@page_html/>
