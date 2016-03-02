<#-- 
  Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
  
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
        <title>${i18nBundle.user}</title>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
	<script type="text/javascript" src="/js/3rdparty/chosen.jquery.min.js"></script>
	<script type="text/javascript">
		// Activating chosen plugin
		$(".chosen-select").chosen();
		// Load main form definition (for validation)
		//loadFormDefinition("userForm");
	</script>
</#macro>
<#macro custom_css>
	<link href="/css/3rdparty/chosen.min.css" rel="stylesheet" />
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
	<p><a href="/user" class="btn btn-default back" role="button">${i18nBundle.back}</a></p>
        <h1><#if viewUser.userId?has_content>${viewUser.firstName!""} ${viewUser.lastName}<#else>${i18nBundle.newUser}</#if></h1>
        <a href="/user?action=viewUser&userId=${viewUser.userId}" class="btn btn-default" role="button">${i18nBundle.userAccountInformation}</a>
        <button type="button" class="btn btn-primary">${i18nBundle.notificationSubscriptions}</button>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<p>${i18nBundle.notificationSubscriptionDescription}</p>
        <#assign formId = "notificationSubscriptionForm">
        <form id="${formId}" role="form" action="/user/notificationsubscription" method="POST" onsubmit="return validateForm(this);">
        <input type="hidden" name="action" value="notificationSubscriptionFormSubmit"/>
        <input type="hidden" name="userId" value="${viewUser.userId}"/>
          
          <h2>${i18nBundle.forecastNotifications}</h2>
          <div class="form-group">
		<label for="forecastN_universalMessageFormatId">${i18nBundle.messageFormat}</label>
		<select name="forecastN_universalMessageFormatId" onblur="validateField(this);">
			<#list universalMessageFormats as format>
			<option value="${format.universalMessageFormatId}"
			<#if messageNotificationSubscription?has_content && messageNotificationSubscription.universalMessageFormatId == format.universalMessageFormatId> selected="selected"</#if>
			>${i18nBundle(format.formatName)}</option>
		</#list>
		</select>
	</div>
          <div class="form-group">
		<label for="forecastN_cropCategoryIds">${i18nBundle.cropCategoryIds}</label>
		<select class="form-control chosen-select" name="forecastN_cropCategoryIds" multiple="multiple">
			<#list allCropCategories as cropCategory>
			<option value="${cropCategory.cropCategoryId}"
			<#if forecastEventNotificationSubscription?has_content && forecastEventNotificationSubscription.cropCategoryIds?seq_contains(cropCategory.cropCategoryId)> selected="selected"</#if>
			>${cropCategory.getLocalName(currentLocale.language)!cropCategory.defaultName}</option>
		</#list>
		</select>
	</div>
	<div class="form-group">
	    <label for="forecastN_weatherStationIds">${i18nBundle.weatherStations}</label>
	    <select class="form-control chosen-select" name="forecastN_weatherStationIds" multiple="multiple">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.weatherStations?lower_case}</option>
	    	<#list weatherStationIds?sort_by("name") as poi>
	    	<option value="${poi.pointOfInterestId}"<#if forecastEventNotificationSubscription?has_content && forecastEventNotificationSubscription.weatherStationIds?seq_contains(poi.pointOfInterestId)> selected="selected"</#if>>${poi.name}</option>
	    	</#list>
	    </select>
	    <span class="help-block" id="${formId}_weatherStationPointOfInterestId_validation"></span>
	  </div>
	  <h2>${i18nBundle.observationNotifications}</h2>
          <div class="form-group">
		<label for="observationN_universalMessageFormatId">${i18nBundle.messageFormat}</label>
		<select name="observationN_universalMessageFormatId" onblur="validateField(this);">
			<#list universalMessageFormats as format>
			<option value="${format.universalMessageFormatId}"
			<#if observationNotificationSubscription?has_content && observationNotificationSubscription.universalMessageFormatId == format.universalMessageFormatId> selected="selected"</#if>
			>${i18nBundle(format.formatName)}</option>
		</#list>
		</select>
	</div>
          <div class="form-group">
		<label for="observationN_cropCategoryIds">${i18nBundle.cropCategoryIds}</label>
		<select class="form-control chosen-select" name="observationN_cropCategoryIds" onblur="validateField(this);" multiple="multiple">
			<#list allCropCategories as cropCategory>
			<option value="${cropCategory.cropCategoryId}"
			<#if observationNotificationSubscription?has_content && observationNotificationSubscription.cropCategoryIds?seq_contains(cropCategory.cropCategoryId)> selected="selected"</#if>
			>${cropCategory.getLocalName(currentLocale.language)!cropCategory.defaultName}</option>
		</#list>
		</select>
	</div>
          <h2>${i18nBundle.messageNotifications}</h2>
          <div class="form-group">
		<label for="messageN_universalMessageFormatId">${i18nBundle.messageFormat}</label>
		<select name="messageN_universalMessageFormatId" onblur="validateField(this);">
			<#list universalMessageFormats as format>
			<option value="${format.universalMessageFormatId}"
			<#if messageNotificationSubscription?has_content && messageNotificationSubscription.universalMessageFormatId == format.universalMessageFormatId> selected="selected"</#if>
			>${i18nBundle(format.formatName)}</option>
		</#list>
		</select>
	</div>
          <div class="form-group">
		<label for="messageN_cropCategoryIds">${i18nBundle.cropCategoryIds}</label>
		<select class="form-control chosen-select" name="messageN_cropCategoryIds" onblur="validateField(this);" multiple="multiple">
			<#list allCropCategories as cropCategory>
			<option value="${cropCategory.cropCategoryId}"
			<#if messageNotificationSubscription?has_content && messageNotificationSubscription.cropCategoryIds?seq_contains(cropCategory.cropCategoryId)> selected="selected"</#if>
			>${cropCategory.getLocalName(currentLocale.language)!cropCategory.defaultName}</option>
		</#list>
		</select>
	</div>
	<div class="form-group">
	    <label for="messageN_messageTagIds">${i18nBundle.messageTags}</label>
	    <select class="form-control chosen-select" name="messageN_messageTagIds" onblur="validateField(this);" multiple="multiple">
		<#list messageTagSet as messageTag>
			<option value="${messageTag.messageTagId}"
				<#if messageNotificationSubscription?has_content && messageNotificationSubscription.messageTagIds?seq_contains(messageTag.messageTagId)> selected="selected"</#if>
			>${(messageTag.getLocalName(currentLocale))!messageTag.defaultTagName}</option>
		</#list>
	     </select>
	</div>
          <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
	</form>
</div>
</#macro>
<@page_html/>
