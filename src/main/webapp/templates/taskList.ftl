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
