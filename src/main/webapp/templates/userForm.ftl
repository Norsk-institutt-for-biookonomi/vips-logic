<#-- 
  Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
  
  This file is part of VIPSLogic.
  VIPSLogic is free software: you can redistribute it and/or modify
  it under the terms of the Bioforsk Open Source License as published by 
  Bioforsk, either version 1 of the License, or (at your option) any
  later version.
  
  VIPSLogic is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  Bioforsk Open Source License for more details.
  
  You should have received a copy of the Bioforsk Open Source License
  along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
--><#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.user}</title>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
	<script type="text/javascript">
		// Load main form definition (for validation)
		loadFormDefinition("userForm");
	</script>
</#macro>
<#macro page_contents>
	<p><a href="/user" class="btn btn-default back" role="button">${i18nBundle.back}</a></p>
        <h1><#if viewUser.userId?has_content>${viewUser.firstName!""} ${viewUser.lastName}<#else>${i18nBundle.newUser}</#if></h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <#assign formId = "userForm">
        <form id="${formId}" role="form" action="/user?action=userFormSubmit" method="POST" onsubmit="return validateForm(this);">
          <input type="hidden" name="userId" value="${viewUser.userId!"-1"}"/>
	  <!-- Authentication info should be dealt with outside this form -->
	  <div class="form-group">
	    <label for="email">${i18nBundle.email}</label>
	    <input type="email" class="form-control" name="email" placeholder="${i18nBundle.email}" value="${viewUser.email!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_email_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="firstName">${i18nBundle.firstName}</label>
	    <input type="text" class="form-control" name="firstName" placeholder="${i18nBundle.firstName}" value="${viewUser.firstName!""}" onblur="validateField(this);"/>
	    <span class="help-block" id="${formId}_firstName_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="lastName">${i18nBundle.lastName}</label>
	    <input type="text" class="form-control" name="lastName" placeholder="${i18nBundle.lastName}" value="${viewUser.lastName!""}" onblur="validateField(this);"/>
	    <span class="help-block" id="${formId}_lastName_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="organizationId">${i18nBundle.organizationId}</label>
	    <select class="form-control" name="organizationId" onblur="validateField(this);">
		<#list organizations?sort_by("organizationName") as organization>
			<option value="${organization.organizationId}"
				<#if viewUser.organizationId.organizationId == organization.organizationId>
					selected="selected"
				</#if>
			>${organization.organizationName}</option>
		</#list>
	     </select>
	     <span class="help-block" id="${formId}_organizationId_validation"></span>
	  </div>
	  <div class="form-group">
	  	<label for="vipsLogicRoles">${i18nBundle.vipsLogicRoles}</label>
	  	<select class="form-control" name="vipsLogicRoles" multiple="multiple" size="3" onblur="validateField(this);">
	  	<#list vipsLogicRoles as vipsLogicRole>
	  	<option value="${vipsLogicRole.vipsLogicRoleId}"
	  		<#if viewUser.hasRole(vipsLogicRole)>
				selected="selected"
			</#if>
	  	>${i18nBundle["vipsLogicRole_" + vipsLogicRole.vipsLogicRoleId]}</option>
	  	</#list>
	  	</select>
	  	<span class="help-block" id="${formId}_vipsLogicRoles_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="userStatusId">${i18nBundle.userStatusId}</label>
	    <select class="form-control" name="userStatusId" onblur="validateField(this);">
	    <#list userStatusIds as userStatusId>
	    	<option value="${userStatusId}"
	    		<#if viewUser.userStatusId?has_content && viewUser.userStatusId == userStatusId>
				selected="selected"
			</#if>
		>${i18nBundle["userStatus_" + userStatusId]}</option>
	    </#list>
	    </select>
	    <span class="help-block" id="${formId}_userStatusId_validation"></span>
	  </div>
	  <div class="form-group">
	    <label>${i18nBundle.approvalApplication}</label>
	    <p>${viewUser.approvalApplication!""}</p>
	  </div>
	  <div class="form-group">
	  	<label for="remarks">${i18nBundle.remarks}</label>
	  	<textarea class="form-control" placeholder="${i18nBundle.remarks}" name="remarks" rows="5" onblur="validateField(this);"><#if viewUser.remarks?has_content>${viewUser.remarks}</#if></textarea>
	  	<span class="help-block" id="${formId}_remarks_validation"></span>
	  </div>
	  <#if ! viewUser.userId?has_content>
	  <div>${i18nBundle.newUserCredentialsHelpText}</div>
	  <div class="form-group">
	    <label for="username">${i18nBundle.username}</label>
	    <input type="text" class="form-control" name="username" placeholder="${i18nBundle.username}" value="${viewUser.firstName!""}" onblur="validateField(this);"/>
	    <span class="help-block" id="${formId}_username_validation"></span>
	  </div>
	  </#if>
	  <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
	  <#if viewUser.userId?has_content>
	  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/user?action=deleteUser&userId=${viewUser.userId}';}">${i18nBundle.delete}</button>
	  </#if>
	</form>

</#macro>
<@page_html/>
