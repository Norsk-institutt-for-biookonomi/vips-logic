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
<#if message.getLocalMessage(messageLocale)?has_content>
<#assign localMessage = message.getLocalMessage(messageLocale)>
</#if>
<#macro page_head>
        	<title><#if message.messageId?has_content>${i18nBundle.editMessage} - ${(localMessage.heading)!""}<#else>${i18nBundle.newMessage}</#if></title>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
	<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
	<link href="http://code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
	<script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
	<script type="text/javascript">
		// Make sure that there is a date picker present for HTML5 
		// date input fields
		if (!Modernizr.inputtypes.date) {
		    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
		}
		
		// Load main form definition (for validation)
		loadFormDefinition("messageForm");
		// Adding selected file name to a box
		$(document)
			.on('change', '.btn-file :file', function() {
				var input = $(this),
				numFiles = input.get(0).files ? input.get(0).files.length : 1,
				label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
				input.trigger('fileselect', [numFiles, label]);
		});
		$(document).ready( function() {
			$('.btn-file :file').on('fileselect', function(event, numFiles, label) {
				
				var input = $(this).parents('.input-group').find(':text'),
					log = numFiles > 1 ? numFiles + ' files selected' : label;
				
				if( input.length ) {
					input.val(log);
				} else {
					if( log ) alert(log);
				}
				
			});
		});		
		// Reload with new locale
		function reloadWithNewLocale(localeSelectList)
		{
			if(confirm("${i18nBundle.confirmReloadForm}")) 
			{
				var messageLocale=localeSelectList.options[localeSelectList.selectedIndex].value;
				var href=
				
				window.location.href="/message?action="
				<#if message.messageId?has_content>
					+ "editMessageForm&messageId=" + ${message.messageId}
				<#else>
					+ "newMessageForm"
				</#if>
					+ "&messageLocale=" + messageLocale;
			}
		}
	</script>

</#macro>
<#macro page_contents>
	<p><a href="/message" class="btn btn-default back" role="button">${i18nBundle.back}</a></p>
	<#if message.messageId?has_content>
	<h1>${i18nBundle.editMessage}</h1>
	<#else>
	<h1>${i18nBundle.newMessage}</h1>
	</#if>
        <#if messageKey?has_content>
	<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	
        <#assign formId = "messageForm">
        <form id="${formId}" role="form" action="/message?action=messageFormSubmit" enctype="multipart/form-data" method="POST" onsubmit="return true;" onsubmit2="try{ return validateForm(this);}catch(err){alert(err);return false;}">
        <input type="hidden" name="messageId" value="${message.messageId!"-1"}"/>
        <div class="form-group">
	    <label for="locale">${i18nBundle.language}</label>
	    <select class="form-control" name="locale" onchange="reloadWithNewLocale(this);">
	    	<option value="NOLANGUAGE">${i18nBundle.pleaseSelect} ${i18nBundle.language?lower_case}</option>
		<#list availableLocales?sort_by("displayLanguage") as locale>
			<option value="${locale.language}"<#if locale.language == messageLocale> selected="selected"</#if>>${locale.getDisplayLanguage(currentLocale)}</option>
		</#list>
	     </select>
	</div>
	<div class="form-group">
	    <label for="messageTagIds">${i18nBundle.messageTags}</label>
	    <select class="form-control" name="messageTagIds" onblur="validateField(this);" multiple="multiple">
	    	<#if message.messageTagSet?has_content >
			<#list message.messageTagSet as messageTag>
				<option value="${messageTag.messageTagId}" selected="selected">${(messageTag.getLocalName(currentLocale))!messageTag.defaultTagName}</option>
			</#list>
		</#if>
		<#list unusedTags as messageTag>
			<option value="${messageTag.messageTagId}">${(messageTag.getLocalName(currentLocale))!messageTag.defaultTagName}</option>
		</#list>
	     </select>
	</div>
	
       <div class="form-group">
	    <label for="heading">${i18nBundle.heading}</label>
	    <input type="text" class="form-control" name="heading" placeholder="${i18nBundle.heading}" value="${(localMessage.heading)!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_heading_validation"></span>
	</div>
	<div class="form-group">
	    <label for="leadParagraph">${i18nBundle.leadParagraph}</label>
	    <textarea class="form-control" name="leadParagraph" placeholder="${i18nBundle.leadParagraph}" onblur="validateField(this);" >${(localMessage.leadParagraph)!""}</textarea>
	    <span class="help-block" id="${formId}_leadParagraph_validation"></span>
	</div>
	<div class="form-group">
	    <label for="body">${i18nBundle.body}</label>
	    <textarea class="form-control" name="body" placeholder="${i18nBundle.body}" onblur="validateField(this);" >${(localMessage.body)!""}</textarea>
	    <span class="help-block" id="${formId}_body_validation"></span>
	</div>
	<div class="form-group">
	    <label for="datePub">${i18nBundle.datePub}</label>
	    <input type="date" class="form-control" name="datePub" placeholder="${i18nBundle.datePub}" value="${(message.datePub?date)!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_datePub_validation"></span>
	</div>
	<div class="form-group">
	    <label for="dateValidTo">${i18nBundle.dateValidTo}</label>
	    <input type="date" class="form-control" name="dateValidTo" placeholder="${i18nBundle.dateValidTo}" value="${(message.dateValidTo?date)!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_dateValidTo_validation"></span>
	</div>

	<#if message.messageIllustrationSet?has_content && message.messageIllustrationSet?size == 1>
		<#assign illustration = message.messageIllustrationSet?first>
		<#if illustration.getLocalMessageIllustrationCaption(messageLocale)?has_content>
			<#assign localMessageIllustrationCaption = illustration.getLocalMessageIllustrationCaption(messageLocale)>
		</#if>
		<img src="/static/images/messages/${message.organizationId}/${illustration.messageIllustrationPK.fileName}" alt="TODO: Add describing text" class="img-responsive"/>
		<div class="checkbox">
		  <label>
		    <input type="checkbox" name="deleteIllustration" value="true">
		    ${i18nBundle.deleteIllustration}
		  </label>
		</div>
	</#if>
	<div class="form-group">
		<div class="input-group">
			<label for="illustration"><#if message.messageIllustrationSet?has_content && message.messageIllustrationSet?size == 1>${i18nBundle.replaceIllustration}<#else>${i18nBundle.newIllustration}</#if></label><br/>
			<span class="btn btn-default btn-file">${i18nBundle.browse}<input type="file" name="illustration"></span>
			<input type="text" class="form-control" readonly>
		</div>
	</div>
	<div class="form-group">
	    <label for="messageIllustrationCaptionLocale">${i18nBundle.messageIllustrationCaptionLocale}</label>
	    <textarea class="form-control" name="messageIllustrationCaptionLocale" placeholder="${i18nBundle.messageIllustrationCaptionLocale}" onblur="validateField(this);" >${(localMessageIllustrationCaption.caption)!""}</textarea>
	    <span class="help-block" id="${formId}_messageIllustrationCaptionLocale_validation"></span>
	</div>
	<#if user.isSuperUser()>
		<div class="form-group">
			<label for="organizationId">${i18nBundle.organizationId}</label>
			<select name="organizationId" size="5" class="form-control" >
				<#list organizations?sort_by("organizationName") as organization>
				<option value="${organization.organizationId}" <#if message.organizationId?has_content && message.organizationId == organization.organizationId> selected="selected"</#if>>
				${organization.organizationName}
				</option>
				</#list>
			</select>
		</div>
	</#if>
        <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
        <#if message.messageId?has_content && user.isSuperUser()>
	  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/message?action=deleteMessage&messageId=${message.messageId}';}">${i18nBundle.delete}</button>
	  </#if>
        <a href="/message" class="btn btn-default" role="button">${i18nBundle.cancel}</a>
	</form>
</#macro>
<@page_html/>
