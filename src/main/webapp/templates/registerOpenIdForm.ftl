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
	<div class="form-signin">
		<div class="alert alert-danger">We can't find this openId/GoogleId in our system. What do you want to do with it?</div>
		<div class="alert alert-info">
		<a href="/user?action=registerNewUserForm&userAuthenticationTypeId=${userAuthenticationTypeId}">I'm a new user to VIPSLogic, and want to register</a>
		</div>
	</div>
	<form class="form-signin" action="/user" method="POST">
		<input type="hidden" name="action" value="attachIdToExistingUser"/>
		<input type="hidden" name="userAuthenticationTypeId" value="${userAuthenticationTypeId}"/>
		<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
		</#if>
		<#if errorMessageKey?has_content>
		<div class="alert alert-danger">${i18nBundle(errorMessageKey)}</div>
		</#if>
		<h2 class="form-signin-heading">${i18nBundle.attachIdToExistingUser}</h2>
		 <input type="text" class="form-control" placeholder="${i18nBundle.username}" name="username" size="15" autofocus/>
		<input type="password" class="form-control" placeholder="${i18nBundle.password}" name="password" size="15"/>
		<button class="btn btn-lg btn-primary btn-block" type="submit">${i18nBundle.login}</button>
	</form>
</div>
</#macro>
<@page_html/>
