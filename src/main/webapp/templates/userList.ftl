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
        <title>${i18nBundle.users}</title>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
        <h1>${i18nBundle.users}</h1>
		<p>
		<a href="/user?action=newUser" class="btn btn-default" role="button">${i18nBundle.addNew}</a>
	</p>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <div class="table-responsive">
	<table class="table table-striped">
		<thead>
			<th>${i18nBundle.lastName}</th>
                        <th>${i18nBundle.firstName}</th>
			<th>${i18nBundle.status}</th>
		</thead>
		<tbody>
                <!-- The ones that need treatment first -->
                <#assign userStatusOrdering = [2,1,3,4,5]>
                <#list userStatusOrdering as currentUserStatusId>
                    <#list users?sort_by("lastName") as user>
                        <#if user.userStatusId == currentUserStatusId>
                        <tr>
                            <td><a href="/user?action=viewUser&userId=${user.userId}">${user.lastName}</a></td>
                            <td><a href="/user?action=viewUser&userId=${user.userId}">${user.firstName}</a></td>
                            <td>${i18nBundle["userStatus_" + user.userStatusId]}</td>
                        </tr>
                        </#if>
                    </#list>
                </#list>
		</tbody>
        </table>
        </div>
</div>
</#macro>
<@page_html/>
