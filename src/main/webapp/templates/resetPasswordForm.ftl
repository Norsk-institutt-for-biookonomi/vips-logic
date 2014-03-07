<#-- 
  Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 

  This file is part of VIPSLogic.
  VIPSLogic is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  VIPSLogic is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
--><#include "master.ftl">
<#macro page_head>
        <title>${i18nBundle.resetPassword}</title>
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
	<#assign formId = "resetPasswordForm">
	<form id="${formId}" class="form-signin" action="/user?action=resetPasswordFormSubmit" method="POST" onsubmit="return validateForm(this);">
		<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
		</#if>
		<#if errorMessageKey?has_content>
		<div class="alert alert-danger">${i18nBundle(errorMessageKey)}</div>
		</#if>
		<div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
			<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
		</div>
		<h1 class="form-signin-heading">${i18nBundle.resetPassword}</h1>
		<p>${i18nBundle.resetPasswordFormExplanation}</p>
		<input type="hidden" name="passwordResetCode" value="${passwordResetCode!""}"/>
		<div><label for="password">${i18nBundle.password}</label><input type="password" class="form-control" placeholder="${i18nBundle.password}" name="password" size="15" onblur="evaluatePassword(this);" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_password_validation"></span>
		<div><label for="password2">${i18nBundle.password2}</label><input type="password" class="form-control" placeholder="${i18nBundle.password2}" name="password2" size="15" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_password2_validation"></span>
		
		<button class="btn btn-lg btn-primary btn-block" type="submit">${i18nBundle.submit}</button>
	</form>

</div>
</#macro>
<@page_html/>
