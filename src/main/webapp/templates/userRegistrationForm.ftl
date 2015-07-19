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
        <title>${i18nBundle.registerNewUser}</title>
</#macro>
<#macro custom_css>
        <!-- Custom styles for VIPSLogic -->
	<link href="css/signin.css" rel="stylesheet" media="screen" />
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
</#macro>
<#macro page_contentwrap_start></#macro>
<#macro page_contentwrap_end></#macro>
<#macro page_contents>
<div class="container">
	<#assign formId = "userRegistrationFormType" + userAuthenticationTypeId>
	<form id="${formId}" class="form-signin" action="/user?action=registerNewUserFormSubmit" method="POST" onsubmit="return validateForm(this);/*try{validateForm(this);}catch(err){console.log(err);}finally{return false;}*/">
		<input type="hidden" name="userAuthenticationTypeId" value="${userAuthenticationTypeId}"/>
		<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
		</#if>
		<div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
			<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
		</div>
		<h1 class="form-signin-heading">${i18nBundle.registerNewUser}</h1>
		<p>${i18nBundle.registerNewUserExplanation}</p>
		<#if userAuthenticationTypeId == 1>
		<div><label for="username">${i18nBundle.username}</label><input type="text" class="form-control" placeholder="${i18nBundle.username}" name="username" size="15" value="<#if formValidation?has_content>${formValidation.getFormField("username").webValue}</#if>" onblur="validateField(this);" autofocus/></div>
		<span class="help-block" id="${formId}_username_validation"></span>
		<div><label for="password">${i18nBundle.password}</label><input type="password" class="form-control" placeholder="${i18nBundle.password}" name="password" size="15" onblur="evaluatePassword(this);" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_password_validation"></span>
		<div><label for="password2">${i18nBundle.password2}</label><input type="password" class="form-control" placeholder="${i18nBundle.password2}" name="password2" size="15" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_password2_validation"></span>
		</#if>
		<div><label for="email">${i18nBundle.email}</label><input type="text" class="form-control" placeholder="${i18nBundle.email}" name="email" value="<#if formValidation?has_content>${formValidation.getFormField("email").webValue}</#if>" size="15" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_email_validation"></span>
		<div><label for="firstName">${i18nBundle.firstName}</label><input type="text" class="form-control" placeholder="${i18nBundle.firstName}" name="firstName" size="15" value="<#if formValidation?has_content>${formValidation.getFormField("firstName").webValue}</#if>" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_firstName_validation"></span>
		<div><label for="lastName">${i18nBundle.lastName}</label><input type="text" class="form-control" placeholder="${i18nBundle.lastName}" name="lastName" size="15" value="<#if formValidation?has_content>${formValidation.getFormField("lastName").webValue}</#if>" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_lastName_validation"></span>
		<div>
			<label for="organizationId">${i18nBundle.organizationId}</label>
			<select class="form-control" name="organizationId" onblur="validateField(this);">
				<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.organizationId?lower_case}</option>
			<#list organizations?sort_by("organizationName") as organization>
				<option value="${organization.organizationId}"
					<#if formValidation?has_content && formValidation.getFormField("organizationId").valueAsInteger == organization.organizationId>
						selected="selected"
					</#if>
				>${organization.organizationName}</option>
			</#list>
			</select>
		</div>
		<span class="help-block" id="${formId}_organizationId_validation"></span>
		<div><label for="approvalApplication">${i18nBundle.approvalApplication}</label><textarea class="form-control" placeholder="${i18nBundle.approvalApplication}" name="approvalApplication" cols="15" rows="5" onblur="validateField(this);"><#if formValidation?has_content>${formValidation.getFormField("approvalApplication").webValue}</#if></textarea></div>
		<span class="help-block" id="${formId}_approvalApplication_validation"></span>
		<button class="btn btn-lg btn-primary btn-block" type="submit">${i18nBundle.submit}</button>
	</form>

</div>
</#macro>
<@page_html/>
