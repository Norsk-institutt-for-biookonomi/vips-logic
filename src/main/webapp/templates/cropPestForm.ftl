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
        	<title>${i18nBundle.editCropPests}</title>
</#macro>
<#macro custom_css>
	<link href="/css/3rdparty/chosen.min.css" rel="stylesheet" />
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script type="text/javascript" src="/js/3rdparty/chosen.jquery.min.js"></script>
	<script src="/js/validateForm.js"></script>
	<script type="text/javascript">
		// Load main form definition (for validation)
		loadFormDefinition("cropPestForm");
		$(document).ready( function() {
			
			// Activating chosen plugin
			$(".chosen-select").chosen();
		});	
	</script>

</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
	<h1>${i18nBundle.editCropPests}</h1>
	<#assign formId = "cropPestForm">
	<form id="${formId}" action="/organism?action=submitCropPest" method="POST" role="form" onsubmit="return validateForm(this);">
	<div class="form-group">
		<label for="cropOrganismId">${i18nBundle.cropOrganismId}</label>
		<select name="cropOrganismId" class="form-control chosen-select" onchange="window.location.href='/organism?action=editCropPest&cropOrganismId='+this.options[this.options.selectedIndex].value;">
			<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.cropOrganismId?lower_case}</option>
			<#list allCrops as cropOrganism>
			<option value="${cropOrganism.organismId}"<#if cropOrganismId?has_content && cropOrganismId == cropOrganism.organismId> selected="selected"</#if>>${cropOrganism.latinName!""}/${cropOrganism.getLocalName(currentLocale.language)!""} (${hierarchyCategories.getName(cropOrganism.hierarchyCategoryId)})</option>
			</#list>
		</select>
	</div>
	<#if cropOrganismId?has_content>
		<div class="form-group">
			<div class="checkbox">
				<label>
					<input type="checkbox" name="includeAllChildCrops"<#if cropPest?has_content && cropPest.includeAllChildCrops == true> checked="checked"</#if>/>
				</label>
				${i18nBundle.includeAllChildCrops}
			</div>
		  </div>
		
		<div class="form-group">
			<label for="pestOrganismIds">${i18nBundle.pestOrganismIds}</label>
			<select name="pestOrganismIds" class="form-control chosen-select" multiple="multiple">
			<#list allPests as pestOrganism>
			<option value="${pestOrganism.organismId}"
			<#if cropPest?has_content && cropPest.pestOrganismIds?has_content>
				<#list cropPest.pestOrganismIds as pestOrganismId>
					<#if pestOrganismId == pestOrganism.organismId>selected="selected"</#if>
				</#list>
			</#if>
			>${pestOrganism.latinName!""}/${pestOrganism.getLocalName(currentLocale.language)!""} (${hierarchyCategories.getName(pestOrganism.hierarchyCategoryId)})</option>
			</#list>
			</select>
		</div>
		<button class="btn btn-default" type="submit">${i18nBundle.submit}</button>
	</#if>
	</form>
</div>	
</#macro>
<@page_html/>
