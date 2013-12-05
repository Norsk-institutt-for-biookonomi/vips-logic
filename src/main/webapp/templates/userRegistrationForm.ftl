<#include "master.ftl">
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
	<#assign formId = "userRegistrationForm">
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
		<div><input type="text" class="form-control" placeholder="${i18nBundle.username}" name="username" size="15" value="<#if formValidation?has_content>${formValidation.getFormField("username").webValue}</#if>" onblur="validateField(this);" autofocus/></div>
		<span class="help-block" id="${formId}_username_validation"></span>
		<div><input type="password" class="form-control" placeholder="${i18nBundle.password}" name="password" size="15" onblur="evaluatePassword(this);" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_password_validation"></span>
		<div><input type="password" class="form-control" placeholder="${i18nBundle.password2}" name="password2" size="15" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_password2_validation"></span>
		<div><input type="text" class="form-control" placeholder="${i18nBundle.email}" name="email" value="<#if formValidation?has_content>${formValidation.getFormField("email").webValue}</#if>" size="15" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_email_validation"></span>
		<div><input type="text" class="form-control" placeholder="${i18nBundle.firstName}" name="firstName" size="15" value="<#if formValidation?has_content>${formValidation.getFormField("firstName").webValue}</#if>" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_firstName_validation"></span>
		<div><input type="text" class="form-control" placeholder="${i18nBundle.lastName}" name="lastName" size="15" value="<#if formValidation?has_content>${formValidation.getFormField("lastName").webValue}</#if>" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_lastName_validation"></span>
		<div>
			<select class="form-control" placeholder="test" name="organizationId" onblur="validateField(this);">
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
		<div><textarea class="form-control" placeholder="${i18nBundle.approvalApplication}" name="approvalApplication" cols="15" rows="5" onblur="validateField(this);"><#if formValidation?has_content>${formValidation.getFormField("approvalApplication").webValue}</#if></textarea></div>
		<span class="help-block" id="${formId}_approvalApplication_validation"></span>
		<button class="btn btn-lg btn-primary btn-block" type="submit">${i18nBundle.login}</button>
	</form>

</div>
</#macro>
<@page_html/>
