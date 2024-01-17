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
        <title>${i18nBundle.resetPasswordRequest}</title>
</#macro>
<#macro custom_css>
        <!-- Custom styles for VIPSLogic -->
	<link href="css/signin.css" rel="stylesheet" media="screen" />
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
</#macro>
<#macro page_contentwrap_start></#macro>
<#macro page_contentwrap_end></#macro>
<#macro page_contents>
<div class="container">
	<#assign formId = "resetPasswordRequestForm">
	<form id="${formId}" class="form-signin" action="/user?action=resetPasswordRequestFormSubmit" method="POST" onsubmit="return validateForm(this);">
		<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
		</#if>
		<#if errorMessageKey?has_content>
		<div class="alert alert-danger">${i18nBundle(errorMessageKey)}</div>
		</#if>
		<div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
			<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
		</div>
		<h1 class="form-signin-heading">${i18nBundle.resetPasswordRequest}</h1>
		<p>${i18nBundle.resetPasswordRequestFormExplanation}</p>
		
		<div><label for="email">${i18nBundle.email}</label><input type="text" class="form-control" placeholder="${i18nBundle.email}" name="email" value="<#if formValidation?has_content>${formValidation.getFormField("email").webValue}</#if>" size="15" onblur="validateField(this);"/></div>
		<span class="help-block" id="${formId}_email_validation"></span>
		
		<button class="btn btn-lg btn-primary btn-block" type="submit">${i18nBundle.submit}</button>
	</form>

</div>
</#macro>
<@page_html/>
