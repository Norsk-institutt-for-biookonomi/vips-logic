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
        <title>${i18nBundle.allCrops}</title>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
        <h1>${i18nBundle.allCrops}</h1>
        <a href="/organism" class="btn btn-default" role="button">${i18nBundle.organisms}</a>
	<a href="/organism?action=listPests" class="btn btn-default" role="button">${i18nBundle.listPests}</a>
        <table class="table table-striped">
            <thead>
                <tr>
                    <th>${i18nBundle.crops}</th>
                    <th>${i18nBundle.pestOrganismIds}</th>
                </tr>
            </thead>
            <tbody> 
            <#list allCrops as cropOrganism>
                <tr>
                    <td><a href="/organism?action=viewOrganism&organismId=${cropOrganism.organismId}">${cropOrganism.getLocalName(currentLocale.language)!""} (${cropOrganism.latinName!""}) ${hierarchyCategories.getName(cropOrganism.hierarchyCategoryId)?upper_case}</a></td>
                    <td>
                    
                        <#if cropPestsMap[cropOrganism.organismId?string]?has_content>
                            <#list cropPestsMap[cropOrganism.organismId?string].pestOrganismIds as pestOrganismId>
                                <#if allPestsMapped[pestOrganismId?string]?has_content>
                        <a href="/organism?action=viewOrganism&organismId=${pestOrganismId}">${allPestsMapped[pestOrganismId?string].getLocalName(currentLocale.language)}</a>,
                                <#else>
                                ${i18nBundle.unknownOrganismId}: ${pestOrganismId}
                                </#if>
                            </#list>
                        </#if>
                    </td>
                </tr>
            </#list>
            </tbody>
        </table>
</div>
</#macro>
<@page_html/>
