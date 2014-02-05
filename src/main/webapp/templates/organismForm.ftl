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
	<#if organism.organismId?has_content>
		<title>${i18nBundle.edit} - ${organism.latinName!organism.tradeName}</title>
	<#else>
        	<title>${i18nBundle.newOrganism}</title>
        </#if>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
	<script type="text/javascript">
		// Load main form definition (for validation)
		loadFormDefinition("organismForm");
	</script>

</#macro>
<#macro page_contents>
	<#if organism.organismId?has_content>
        <h1>${i18nBundle.edit} - ${organism.latinName!organism.tradeName}</h1>
        <#else>
        <h1>${i18nBundle.newOrganism}</h1>
        </#if>
        <#if messageKey?has_content>
	<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
        <#assign formId = "organismForm">
        <form id="${formId}" role="form" action="/organism?action=organismFormSubmit" method="POST" onsubmit="return true;" onsubmit2="try{ return validateForm(this);}catch(err){alert(err);return false;}">
        <input type="hidden" name="organismId" value="${organism.organismId!"-1"}"/>
        <div class="form-group">
	    <label for="parentOrganismId">${i18nBundle.parentOrganismId}</label>
	    <select class="form-control" name="parentOrganismId" onblur="validateField(this);">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.parentOrganismId?lower_case}</option>
		<#list allOrganisms?sort_by("latinName") as parentOrganism>
			<option value="${parentOrganism.organismId}"
				<#if (organism.parentOrganismId?has_content && organism.parentOrganismId == parentOrganism.organismId)
					|| (parentOrganismId?has_content && parentOrganismId == parentOrganism.organismId)
				>
					selected="selected"
				</#if>
			>${parentOrganism.latinName!""}/${parentOrganism.tradeName!""}/${parentOrganism.getLocalName(currentLocale.language)!""} (${hierarchyCategories.getName(parentOrganism.hierarchyCategoryId)})</option>
		</#list>
	     </select>
	</div>
        <div class="form-group">
	    <label for="latinName">${i18nBundle.latinName}</label>
	    <input type="text" class="form-control" name="latinName" placeholder="${i18nBundle.latinName}" value="${organism.latinName!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_latinName_validation"></span>
	</div>
	<div class="form-group">
	    <label for="tradeName">${i18nBundle.tradeName}</label>
	    <input type="text" class="form-control" name="tradeName" placeholder="${i18nBundle.tradeName}" value="${organism.tradeName!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_tradeName_validation"></span>
	</div>
	<div class="form-group">
	    <label for="Name">${i18nBundle.localName} (${currentLocale.getDisplayLanguage(currentLocale)})</label>
	    <input type="text" class="form-control" name="localName" placeholder="${i18nBundle.localName}" value="<#if organism.organismId?has_content>${organism.getLocalName(currentLocale.language)!""}</#if>" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_localName_validation"></span>
	</div>
	<div class="form-group">
	    <label for="hierarchyCategoryId">${i18nBundle.hierarchyCategoryId}</label>
	    <select class="form-control" name="hierarchyCategoryId" onblur="validateField(this);">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.hierarchyCategoryId?lower_case}</option>
		<#list hierarchyCategories.hierarchyCategoryIds as hierarchyCategoryId>
			<option value="${hierarchyCategoryId}"
				<#if (organism.hierarchyCategoryId?has_content && organism.hierarchyCategoryId == hierarchyCategoryId)>
					selected="selected"
				</#if>
			>${hierarchyCategories.getName(hierarchyCategoryId)}</option>
		</#list>
	     </select>
	</div>
	<fieldset>
	<h3>${i18nBundle.externalResources}</h3>
	<#if organism.organismExternalResourceSet?has_content>
		<#list organism.organismExternalResourceSet as organismExternalResource>
		<div class="form-group">
		    <label for="externalResourceIdentifier_${organismExternalResource.externalResource.externalResourceId}">${i18nBundle.externalResourceIdentifier} - ${organismExternalResource.externalResource.name} (${organismExternalResource.externalResource.uri}${organismExternalResource.externalResource.identifierTemplate})</label>
		    <input type="text" class="form-control" name="externalResourceIdentifier_${organismExternalResource.externalResource.externalResourceId}" placeholder="${i18nBundle.externalResourceIdentifier}" value="${organismExternalResource.resourceIdentifier}" />
		</div>
		 </#list>
	 </#if>
	 <#list unreferencedExternalResources as externalResource>
	<div class="form-group">
	    <label for="externalResourceIdentifier_${externalResource.externalResourceId}">${i18nBundle.externalResourceIdentifier} - ${externalResource.name} (${externalResource.uri}${externalResource.identifierTemplate})</label>
	    <input type="text" class="form-control" name="externalResourceIdentifier_${externalResource.externalResourceId}" placeholder="${i18nBundle.externalResourceIdentifier}" value="" />
	</div>
	 </#list>
	 
	</fieldset>
        <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
        <a href="/organism?action=listChildOrganisms&organismId=${organism.parentOrganismId!"null"}" class="btn btn-default" role="button">${i18nBundle.cancel}</a>
	</form>
</#macro>
<@page_html/>
