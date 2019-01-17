<#-- 
    Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
  
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
        <title>${i18nBundle.organizations}</title>
</#macro>
<#macro custom_css>
</#macro>
<#macro custom_js>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
<h1>${i18nBundle.organizations}</h1>
<#if messageKey?has_content>
        <div class="alert alert-success">${i18nBundle(messageKey)}</div>
</#if>
<ul>
<#list organizations as organization>
<li><a href="/organization?action=editOrganization&organizationId=${organization.organizationId}">${organization.organizationName}</a></li>
</#list>
</ul>
<p><a href="/organization?action=newOrganization" class="btn btn-default back" role="button">${i18nBundle.newOrganization}</a></p>
</div>
</#macro>
<@page_html/>
