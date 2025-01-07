<#-- 
    Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
  
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
	<form class="form-signin" action="/loginsubmit" method="POST">
		<#if RequestParameters.nextPage?has_content>
		<input type="hidden" name="nextPage" value="${RequestParameters.nextPage}"/>
		</#if>
		<#if RequestParameters.returnUUID?has_content>
		<input type="hidden" name="returnUUID" value="${RequestParameters.returnUUID}"/>
		</#if>
		<input type="hidden" name="userAuthenticationTypeId" value="1"/>
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
		<input type="checkbox" name="rememberUser"<#if checkRemember?has_content && checkRemember> checked="checked"</#if>/> ${i18nBundle.rememberLogin}<br/>
		<a href="/user?action=registerNewUserForm&userAuthenticationTypeId=1">${i18nBundle.registerNewUser}</a><br/>
		<a href="/user?action=resetPasswordRequestForm">${i18nBundle.forgottenPassword}</a><br/>
                <a href="/public/documentation/${i18nBundle.privacyStatementFileName}" target="new">${i18nBundle.privacyStatement}</a>
	</form>
</div>
</#macro>
<@page_html/>
