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
        <title>${i18nBundle.organizationGroupList}</title>
</#macro>
<#macro custom_css>
</#macro>
<#macro custom_js>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
<h1>${i18nBundle.organizationGroupList}</h1>
<#if user.isSuperUser() || user.isOrganizationAdmin()>
    <a href="/organizationgroup?action=editOrganizationGroup&organizationGroupId=-1" class="btn btn-default" role="button">${i18nBundle.addNew}</a>
</#if>
<#if messageKey?has_content>
        <div class="alert alert-success">${i18nBundle(messageKey)}</div>
</#if>
<ul>
<#list organizationGroups as group>
<li><a href="/organizationgroup?action=editOrganizationGroup&organizationGroupId=${group.organizationGroupId}">${group.groupName}</a></li>
</#list>
</ul>
</div>
</#macro>
<@page_html/>
