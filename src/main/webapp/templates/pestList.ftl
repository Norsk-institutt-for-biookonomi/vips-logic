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
