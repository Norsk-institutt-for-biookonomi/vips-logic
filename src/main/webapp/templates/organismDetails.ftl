<#include "master.ftl">
<#macro page_head>
        <title>${organism.getLocalName(currentLocale.language)!""}<#if organism.tradeName?has_content> / ${organism.tradeName}</#if> (${organism.latinName!""})</title>
</#macro>

<#macro page_contents>
        <h1>${organism.getLocalName(currentLocale.language)!""}/${organism.tradeName!""}/${organism.latinName!""}</h1>
        <dl class="dl-horizontal">
        
          <#if parentOrganism?has_content>
          <dt>${i18nBundle.parentOrganismId}</dt>
	  <dd><a href="/organism?action=viewOrganism&organismId=${parentOrganism.organismId}">${parentOrganism.getLocalName(currentLocale.language)!""}/${parentOrganism.tradeName!""}/${parentOrganism.latinName!""}</a></dd>
	  </#if>
	  
	  
	  <dt>${i18nBundle.latinName}</dt>
	  <dd>${organism.latinName!""}</dd>
	  
	  <dt>${i18nBundle.tradeName}</dt>
	  <dd>${organism.tradeName!""}</dd>
	  
	  <dt>${i18nBundle.localName} (${currentLocale.getDisplayLanguage(currentLocale)})</dt>
	  <dd>${organism.getLocalName(currentLocale.language)!""}</dd>
	  
	  <dt>${i18nBundle.hierarchyCategoryId}</dt>
	  <dd>${hierarchyCategories.getName(organism.hierarchyCategoryId)}</dd>
	  
	  <dt>${i18nBundle.externalResources}</dt>
	  <dd>
	  <#list organism.organismExternalResourceSet as organismExternalResource>
	  <a href="${organismExternalResource.resourceUrl}" target="new">${organismExternalResource.externalResource.name}</a><br/>
	  </#list>
	  </dd>
	  
	</dl>
	<#if user.isSuperUser()>
	<a href="/organism?action=editOrganismForm&organismId=${organism.organismId}" class="btn btn-default" role="button">${i18nBundle.edit}</a>
	</#if>
	<a href="/organism?action=listChildOrganisms&organismId=${organism.parentOrganismId!"null"}" class="btn btn-default" role="button">${i18nBundle.back}</a>
        
</#macro>
<@page_html/>
