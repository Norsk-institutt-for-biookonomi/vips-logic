<#-- 
  Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
  
  This file is part of VIPSLogic.
  VIPSLogic is free software: you can redistribute it and/or modify
  it under the terms of the NIBIO Open Source License as published by 
  NIBIO, either version 1 of the License, or (at your option) any
  later version.
  
  NIBIO is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  NIBIO Open Source License for more details.
  
  You should have received a copy of the NIBIO Open Source License
  along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
--><#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.deleteUser}</title>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
        <h1>${i18nBundle.deleteUser} <#if viewUser.userId?has_content>${viewUser.firstName!""} ${viewUser.lastName}<#else>${i18nBundle.newUser}</#if></h1>
        <#if errorMsg?has_content>
        <div id="errorMsgEl" class="alert alert-danger">
		${errorMsg}
	</div>
	</#if>
	<p>${i18nBundle.deleteUserDescription}</p>
	<h2>${i18nBundle.userResources}</h2>
	<ul>
		<li>${i18nBundle.weatherStations}: ${userResources.weatherStations?size}</li>
		<li>${i18nBundle.messages}: ${userResources.messageLocales?size}</li>
		<li>${i18nBundle.forecasts}: ${userResources.forecastConfigurations?size}</li>
	</ul>
	<h2>${i18nBundle.transferResources}</h2>
	<form action="/user?action=deleteUser" method="POST" onsubmit="return confirm('${i18nBundle.confirmDelete}');"/>
	<input type="hidden" name="userId" value="${viewUser.userId!"-1"}"/>
	<p>
		<select class="form-control" name="transferToUserId">
			<option value="-1">-- ${i18nBundle.select} --</option>
		<#list users as user>
			<#if user.userId != viewUser.userId>
			<option value="${user.userId}">${user.firstName!""} ${user.lastName}</option>
			</#if>
		</#list>
		</select>
	</p>
	<button type="button" class="btn btn-default" onclick="window.location.href='/user?action=viewUser&userId=${viewUser.userId}';">${i18nBundle.cancel}</button>
	<#if viewUser.userId?has_content>
	<button type="submit" class="btn btn-danger">${i18nBundle.transferAndDelete}</button>
	</#if>

</div>
</#macro>
<@page_html/>
