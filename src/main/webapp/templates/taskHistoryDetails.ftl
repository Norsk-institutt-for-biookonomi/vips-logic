<#-- 
  Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
  
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
        <title>${i18nBundle.taskHistoryDetails}</title>
</#macro>

<#macro page_contents>
<div class="singleBlockContainer">
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
</div>
</#macro>
<@page_html/>
