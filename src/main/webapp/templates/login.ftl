<#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.pleaselogin}</title>
</#macro>
<#macro custom_css>
        <!-- Custom styles for VIPSLogic -->
	<link href="css/signin.css" rel="stylesheet" media="screen" />
</#macro>
<#macro page_contentwrap_start></#macro>
<#macro page_contentwrap_end></#macro>
<#macro page_contents>
<div class="container">
	<form class="form-signin" action="/login" method="POST">
		<#if RequestParameters.nextPage?has_content>
		<input type="hidden" name="nextPage" value="${RequestParameters.nextPage}"/>
		</#if>
		<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
		</#if>
		<#if errorMessageKey?has_content>
		<div class="alert alert-danger">${i18nBundle(errorMessageKey)}</div>
		</#if>
		<h1 class="form-signin-heading">${i18nBundle.greeting} VIPSLogic</h1>
		<h2 class="form-signin-heading">${i18nBundle.pleaselogin}</h2>
		 <input type="text" class="form-control" placeholder="${i18nBundle.username}" name="username" size="15" autofocus/>
		<input type="password" class="form-control" placeholder="${i18nBundle.password}" name="password" size="15"/>
		<button class="btn btn-lg btn-primary btn-block" type="submit">${i18nBundle.login}</button>
	</form>
</div>
</#macro>
<@page_html/>
