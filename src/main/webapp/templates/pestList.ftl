<#--
  Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
  
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
        <title>${i18nBundle.allPests}</title>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
        <h1>${i18nBundle.allPests}</h1>
        <a href="/organism" class="btn btn-default" role="button">${i18nBundle.organisms}</a>
        <a href="/organism?action=listCrops" class="btn btn-default" role="button">${i18nBundle.listCrops}</a>
        <ul>
        <#list allPests as pestOrganism>
        <li><a href="/organism?action=viewOrganism&organismId=${pestOrganism.organismId}">${pestOrganism.getLocalName(currentLocale.language)!""} (${pestOrganism.latinName!""}) ${hierarchyCategories.getName(pestOrganism.hierarchyCategoryId)?upper_case}</a></li>
        </#list>
        </ul>
</div>
</#macro>
<@page_html/>
