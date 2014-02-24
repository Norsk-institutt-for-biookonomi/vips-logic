<#-- 
  Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 

  This file is part of VIPSLogic.
  VIPSLogic is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  VIPSLogic is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
--><#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.messages}</title>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.messages}</h1>
        <p>TODO: Add column for tags, add filters for tags and dates</p>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <div class="table-responsive">
	<table class="table table-striped">
		<thead>
			<th>${i18nBundle.datePub}</th>
			<th>${i18nBundle.heading}</th>
		</thead>
		<tbody>
		<#list messages as message>
		    <tr>
		    	<td>${(message.datePub?date)!""}</td>
		    	<td><a href="/message?action=editMessageForm&messageId=${message.messageId}">${message.getLocalMessageWithFallback(currentLocale,user.organizationId.defaultLocale).heading}</a></td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>
</#macro>
<@page_html/>
