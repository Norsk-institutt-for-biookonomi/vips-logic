<#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.user}</title>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
</#macro>
<#macro page_contents>
        <h1>${viewUser.firstName} ${viewUser.lastName}</h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <#assign formId = "userForm">
        <form id="${formId}" role="form" action="/user?action=userFormSubmit" method="POST" onsubmit="return validateForm(this);" accept-charset="UTF-8">
          <input type="hidden" name="userId" value="${viewUser.userId}"/>
	  <!-- Authentication info should be dealt with outside this form -->
	  <div class="form-group">
	    <label for="email">${i18nBundle.email}</label>
	    <input type="email" class="form-control" name="email" placeholder="${i18nBundle.email}" value="${viewUser.email}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_email_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="firstName">${i18nBundle.firstName}</label>
	    <input type="firstName" class="form-control" name="firstName" placeholder="${i18nBundle.firstName}" <#if viewUser.remarks?has_content>value="${viewUser.firstName}"</#if> onblur="validateField(this);"/>
	    <span class="help-block" id="${formId}_firstName_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="lastName">${i18nBundle.lastName}</label>
	    <input type="lastName" class="form-control" name="lastName" placeholder="${i18nBundle.lastName}" value="${viewUser.lastName}" onblur="validateField(this);"/>
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
	    		<#if viewUser.userStatusId == userStatusId>
				selected="selected"
			</#if>
		>${i18nBundle["userStatus_" + userStatusId]}</option>
	    </#list>
	    </select>
	    <span class="help-block" id="${formId}_userStatusId_validation"></span>
	  </div>
	  <div class="form-group">
	    <label>${i18nBundle.approvalApplication}</label>
	    <p><#if viewUser.approvalApplication?has_content>${viewUser.approvalApplication}</#if></p>
	  </div>
	  <div class="form-group">
	  	<label for="remarks">${i18nBundle.remarks}</label>
	  	<textarea class="form-control" placeholder="${i18nBundle.remarks}" name="remarks" rows="5" onblur="validateField(this);"><#if viewUser.remarks?has_content>${viewUser.remarks}</#if></textarea>
	  	<span class="help-block" id="${formId}_remarks_validation"></span>
	  </div>
	  <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
	  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/user?action=deleteUser&userId=${viewUser.userId}';}">${i18nBundle.delete}</button>
	</form>

</#macro>
<@page_html/>
