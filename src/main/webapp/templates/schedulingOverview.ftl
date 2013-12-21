<#include "master.ftl">
<#macro page_head>
	<!--meta http-equiv="refresh" content="10;url=/scheduling<#if taskHistoryDate?has_content>?taskHistoryDate=${taskHistoryDate?date}</#if>" /-->
        <title>${i18nBundle.schedulingOverview}</title>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
	
	<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
	<link href="http://code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
	<script type="text/javascript" src="/js/modernizr_custom.js"></script>
	<script type="text/javascript" src="/js/moment.min.js"></script>
	<script type="text/javascript">
		// Make sure that there is a date picker present for HTML5 
		// date input fields
		if (!Modernizr.inputtypes.date) {
		    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
		}
		// Load the form definition for validation
		loadFormDefinition("taskHistoryDateForm");
	</script>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.schedulingOverview}</h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
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
        <form class="form-inline" id="taskHistoryDateForm" action="/scheduling" method="GET" onsubmit="return validateForm(this);">
		<div class="form-group">
		    <label class="sr-only" for="taskHistoryDate">${i18nBundle.taskHistoryDate}</label>
		    <input type="date" class="form-control" name="taskHistoryDate" placeholder="${i18nBundle.taskHistoryDate}" value="${taskHistoryDate?date}" />
		</div>
		<button type="submit" class="btn btn-default">${i18nBundle.changeDate}</button>
		<span class="help-block" id="taskHistoryDateForm_taskHistoryDate_validation"></span>
        </form>
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
				<td>${allTasksMap[taskHistoryItem.taskFactoryId?string].getName()}</td>
				<td>${taskHistoryItem.taskHistoryStatusId.defaultTitle}</td>
			</tr>
		</#list>
		</tbody>
        </table>
        </div>
        
</#macro>
<@page_html/>
