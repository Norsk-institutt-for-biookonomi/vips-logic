<#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.taskHistoryDetails}</title>
</#macro>

<#macro page_contents>
        <h1>${i18nBundle.taskHistoryDetails}</h1>
        <dl class="dl-horizontal">
	  <dt>${i18nBundle.startTime}</dt>
	  <dd>${taskHistory.startTime}</dd>
	  
	  <dt>${i18nBundle.endTime}</dt>
	  <dd>${taskHistory.finishTime}</dd>
	  
	  <dt>${i18nBundle.name}</dt>
	  <dd>${allTasksMap[taskHistory.taskFactoryId?string].getName(currentLocale)}</dd>
	  
	  <dt>${i18nBundle.status}</dt>
	  <dd>${i18nBundle[taskHistory.taskHistoryStatusId.taskHistoryStatusId]}</dd>
	  
	  <dt>${i18nBundle.completenessAtFinish}</dt>
	  <dd>${taskHistory.completenessAtFinish * 100}%</dd>
	  
	  <dt>${i18nBundle.details}</dt>
	  <dd>${taskHistory.message!""}</dd>
	</dl>
	<button type="button" class="btn btn-default" onclick="window.location.href='/scheduling'">&lt;&lt; ${i18nBundle.back}</button>
        
</#macro>
<@page_html/>
