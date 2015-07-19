<#--
  Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
  
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
        <title>${i18nBundle.organisms}<#if organism.organismId?has_content> - ${organism.localName!""}<#if organism.tradeName?has_content> / ${organism.tradeName}</#if> (${organism.latinName!""})</#if></title>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.organisms}</h1>
        <#if organism.organismId?has_content>
        <h2>${organism.getLocalName(currentLocale.language)!""}<#if organism.tradeName?has_content> / ${organism.tradeName}</#if> (<i>${organism.latinName!""}</i>)</h2>
        </#if>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<#if user.isSuperUser()>
	<a href="/organism?action=newOrganismForm&parentOrganismId=${organism.organismId!""}" class="btn btn-default" role="button">${i18nBundle.addNew}</a>
	</#if>
	<#if organism.organismId?has_content>
	<a href="/organism?action=listChildOrganisms&organismId=${organism.parentOrganismId!""}" class="btn btn-default" role="button">${i18nBundle.up}</a>
	<a href="/organism" class="btn btn-default" role="button">${i18nBundle.toTheTop}</a>
	</#if>
        <div class="table-responsive">
	<table class="table table-striped">
		<thead>
			<th>${i18nBundle.latinName}</th>
			<th>${i18nBundle.localName}</th>
			<#if tradeNamePresent>
			<th>${i18nBundle.tradeName}</th>
			</#if>
			<th></th>
		</thead>
		<tbody>
		<#list organism.childOrganisms as childOrganism>
		    <tr>
		    	<td>${childOrganism.latinName!""}</td>
		    	<td>${childOrganism.getLocalName(currentLocale.language)}</td>
		    	<#if tradeNamePresent>
		    	<td>${childOrganism.tradeName!""}</td>
		    	</#if>
		    	<td>
		    		<#if user.isSuperUser()>
		    			<a href="/organism?action=editOrganismForm&organismId=${childOrganism.organismId}&parentOrganismId=${organism.organismId!""}" class="btn btn-default" role="button">${i18nBundle.edit}</a>
		    			<#if ! childOrganism.childOrganisms?has_content || childOrganism.childOrganisms?size == 0>
		    			<button type="button" class="btn btn-danger" onclick="if(confirm('${childOrganism.getLocalName(currentLocale.language)!""}/${childOrganism.tradeName!""}/${childOrganism.latinName!""}: ${i18nBundle.confirmDelete}')) {window.location.href='/organism?action=deleteOrganism&organismId=${childOrganism.organismId}';}">${i18nBundle.delete}</button>
		    			</#if>
		    		</#if>
		    		<a href="/organism?action=viewOrganism&organismId=${childOrganism.organismId}" class="btn btn-default" role="button">${i18nBundle.details}</a>
		    		<a href="/organism?action=listChildOrganisms&organismId=${childOrganism.organismId}" class="btn btn-default" role="button">${i18nBundle.children}</a>
		    	</td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>
</#macro>
<@page_html/>
