<#-- 
  Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
  
  This file is part of VIPSLogic.
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
        <title>${i18nBundle.user}: ${i18nBundle.organizationGroupList?lower_case}</title>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
    <h1>${viewUser.firstName!""} ${viewUser.lastName}</h1>
    <#if viewUser.userId?has_content>
            <a href="user?action=viewUser&userId=${viewUser.userId}" class="btn btn-default" role="button">${i18nBundle.userAccountInformation}</a>
            <a href="/user/notificationsubscription?userId=${viewUser.userId}" class="btn btn-default" role="button">${i18nBundle.notificationSubscriptions}</a>
            <button type="button" class="btn btn-primary">${i18nBundle.organizationGroupList}</button>
    </#if>
    <p>
        ${i18nBundle.userOrganizationGroupsListDescription}
    </p>
    <ul>
    <#list userGroups as group>
        <li><a href="/organizationgroup?action=editOrganizationGroup&organizationGroupId=${group.organizationGroupId}">${group.groupName}</a></li>
    </#list>
    </ul>
</div>
</#macro>
<@page_html/>