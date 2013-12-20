<#include "master.ftl">
<#macro page_head>
	<meta http-equiv="refresh" content="10;url=/scheduling" />
        <title>${i18nBundle.schedulingOverview}</title>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.schedulingOverview}</h1>
        <#if user.isSuperUser()>
        <p><a href="/scheduling?action=viewAllTasks">${i18nBundle.viewAllTasks}</a></p>
        </#if>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<#if message?has_content>
		<div class="alert alert-success">${message}</div>
	</#if>
	<#if taskExecutors?has_content>
		<h2>${i18nBundle.runningTasks}</h2>
		<div class="table-responsive">
		<table class="table table-striped">
			<thead>
				<th>${i18nBundle.name}</th>
				<th>${i18nBundle.progress}</th>
			</thead>
			<tbody>
			<#list taskExecutors as taskExecutor>
			    <tr>
				<td>${taskExecutor.getTask().getName(currentLocale)}</td>
				<td><progress max="1" value="${taskExecutor.completeness}">Progress</progress></td>
			    </tr>
			</#list>
			</tbody>
		</table>
		</div>
	<#else>
		<div class="alert alert-info">${i18nBundle.noTasksRunning}</div>
        </#if>
        <#if user.isSuperUser()>
        	<#if schedulingStarted>
        	<button type="button" class="btn btn-danger" onclick="window.location.href='/scheduling?action=stopScheduling'">${i18nBundle.stopScheduling}</button>
        	<#else>
        	<div class="alert alert-info">${i18nBundle.schedulingStoppedExplanation}</div>
        	<button type="button" class="btn btn-success" onclick="window.location.href='/scheduling?action=startScheduling'">${i18nBundle.startScheduling}</button>
        	</#if>
        </#if>
        <h2>${i18nBundle.scheduledTasks}</h2>
        <div class="table-responsive">
        <table class="table table-striped">
		<thead>
			<th>${i18nBundle.timestamp}</th>
			<th>${i18nBundle.name}</th>
		</thead>
		<tbody>
		<#list orderedScheduledTasks?keys as executionTime>
			<#assign tasksAtSameTime = orderedScheduledTasks[executionTime]>
			<#list tasksAtSameTime as task>
			<tr>
				<td>${executionTime}</td>
				<td>${task.getName(currentLocale)}</td>
			</tr>
			</#list>
		</#list>
		</tbody>
        </table>
        </div>
        <h2>${i18nBundle.taskHistory}</h2>
         <div class="table-responsive">
        <table class="table table-striped">
		<thead>
			<th>${i18nBundle.startTime}</th>
			<th>${i18nBundle.endTime}</th>
			<th>${i18nBundle.name}</th>
			<th>${i18nBundle.status}</th>
		</thead>
		<tbody>
		<#list taskHistory?sort_by("startTime")?reverse as taskHistoryItem>
			<tr>
				
				<td>${taskHistoryItem.startTime}</td>
				<td>${taskHistoryItem.finishTime}</td>
				<td>${allTasksMap[taskHistoryItem.taskFactoryId?string].name}</td>
				<td>${taskHistoryItem.taskHistoryStatusId.defaultTitle}</td>
			</tr>
		</#list>
		</tbody>
        </table>
        </div>
        <#list allTasksMap?keys as key>
        ${allTasksMap[key].name}
        </#list>
</#macro>
<@page_html/>
