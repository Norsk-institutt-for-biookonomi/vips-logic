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
	<title>${i18nBundle.cropCategoriesFor} ${organization.organizationName}</title>
</#macro>
<#macro custom_css>
	<link href="/css/3rdparty/chosen.min.css" rel="stylesheet" />
</#macro>
<#macro custom_js>
    <script src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
    <script type="text/javascript" src="/js/3rdparty/chosen.jquery.min.js"></script>
    <script type="text/javascript">
            $(document).ready( function() {
                // Activating chosen plugin
                $(".chosen-select").chosen();
            });
            
            var allCrops = [
                <#list allCrops?keys as cropId>
                    new Option("${allCrops[cropId?string].getLocalName(currentLocale.language)!""} (${allCrops[cropId?string].latinName})", "${cropId}"),
                </#list>
            ];
                
            function prepareEdit(cropCategoryId)
            {
                var theForm = document.getElementById("form_" + cropCategoryId);
                // Disable all other edit buttons
                var allForms = document.querySelectorAll('[id^="form_"]')
                for(var i=0;i<allForms.length;i++)
                {
                    var aForm = allForms[i];
                    var formId = aForm.id.split("_")[1];
                    if(formId != cropCategoryId)
                    {
                        document.getElementById("editButton_" + formId).disabled = true;
                    }
                    else
                    {
                        document.getElementById("editButton_" + formId).style.display = "none";
                        document.getElementById("cancelButton_" + formId).style.display = "inline";
                        document.getElementById("submitButton_" + formId).style.display = "inline";
                        aForm.cropCategoryCropOrganismIds.disabled = false;
                        addUnselectedCrops(aForm.cropCategoryCropOrganismIds);
                        
                        $(aForm.cropCategoryCropOrganismIds).trigger("chosen:updated");
                    }
                }
            }
                
            function addUnselectedCrops(theSelect)
            {
                // Make a set of the options currently in the list
                var selOpts = theSelect.options;
                var selOptIds = [];
                for(var i=0;i<selOpts.length;i++)
                {
                    selOptIds.push(selOpts[i].value);
                }

                for(var i=0;i<allCrops.length;i++)
                {
                    if(selOptIds.indexOf(allCrops[i].id) < 0)
                    {
                        selOpts[selOpts.length] = allCrops[i];
                    }       
                }
            }
                
            function cancelEdit()
            {
                if(!confirm("Are you sure you want to cancel?"))
                {
                    return;
                }
                location.reload();
            }
    </script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
        <h1>${i18nBundle.cropCategoriesFor} ${organization.organizationName}</h1>
        <#if user.isSuperUser()>
            <div class="form-group">
                <label for="organizationId">${i18nBundle.organizationId}</label>
                <select class="form-control chosen-select" name="organizationId" onchange="window.location.href='/organism/cropcategory?organizationId=' + this.options[this.options.selectedIndex].value;">
                    <option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.organizationId?lower_case}</option>
                    <#list allOrganizations?sort_by("organizationName") as oneOrg>
                        <option value="${oneOrg.organizationId}"
                            <#if (organization?has_content && organization.organizationId == oneOrg.organizationId)>selected="selected"</#if>
                                    >${oneOrg.organizationName}</option>
                    </#list>
                </select>
            </div>
        </#if>
        <div class="table-responsive">
	<table class="table table-striped">
		<thead>
			<th>${i18nBundle.name}</th>
                        <th>${i18nBundle.cropOrganismIds}</th>
                        <th></th>
                </thead>
                <tbody>
		<#list cropCategories as cropCategory>
		    <tr>
                        <form id="form_${cropCategory.cropCategoryId}" action="/organism/cropcategory" method="post">
                        <input type="hidden" name="action" value="submit"/>
                        <input type="hidden" name="cropCategoryId" value="${cropCategory.cropCategoryId}"/>
		    	<td>${(cropCategory.getLocalName(currentLocale))!cropCategory.defaultName}</td>
                        <td>
                            <select class="form-control chosen-select" name="cropCategoryCropOrganismIds" multiple="multiple" disabled="disabled">
                            <#list cropCategory.cropOrganismIds as cropId>
                            <option value="${cropId}" selected="selected">${allCrops[cropId?string].getLocalName(currentLocale.language)!""} (${allCrops[cropId?string].latinName})</option>
                            </#list>
                            </select>
                        </td>
                        <td>
                            <button id="editButton_${cropCategory.cropCategoryId}" type="button" onclick="prepareEdit(${cropCategory.cropCategoryId});">Edit</button>
                            <button id="cancelButton_${cropCategory.cropCategoryId}" type="button" onclick="cancelEdit(${cropCategory.cropCategoryId});" style="display: none;">Cancel</button>
                            <input id="submitButton_${cropCategory.cropCategoryId}" type="submit" value="Submit" style="display: none;"/>
                        </td>
                        </form>
                    </tr>
                </#list>
                </tbody>
        </table>
</div>
</#macro>
<@page_html/>