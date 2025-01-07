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
        <title>${i18nBundle.tasks}</title>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
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
</div>
</#macro>
<@page_html/>
