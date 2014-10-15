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
        <title>${i18nBundle.messages}</title>
</#macro>
<#macro custom_js>
	<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
	<link href="http://code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
	<script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
	<script type="text/javascript">
		// Make sure that there is a date picker present for HTML5 
		// date input fields
		if (!Modernizr.inputtypes.date) {
		    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
		}
	</script>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.messages}</h1>
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
	<a href="/message?action=newMessageForm" class="btn btn-default" role="button">${i18nBundle.addNew}</a>
	<h2>${i18nBundle.filter}</h2>
	<form action="/message" method="GET" class="form-inline" role="form">
	<input type="hidden" name="action" value="filter"/>
	<div class="form-group">
	    <label class="sr-only" for="datePubStart">${i18nBundle.from}</label>
	    <input type="date" class="form-control" name="datePubStart" placeholder="${i18nBundle.from}" size="10" value="${datePubStart?date}">
	</div>-
	<div class="form-group">
	    <label class="sr-only" for="datePubEnd">${i18nBundle.to}</label>
	    <input type="date" class="form-control" name="datePubEnd" placeholder="${i18nBundle.to}" size="10" value="${datePubEnd?date}">
	</div>
	
	<div class="form-group">
	    <label class="sr-only" for="filterMessageTags">${i18nBundle.messageTags}</label>
	    <select class="form-control" name="filterMessageTags" multiple="multiple" size="3">
		<#list selectedMessageTags as selectedMessageTag>
			<option value="${selectedMessageTag.messageTagId}" selected="selected">${(selectedMessageTag.getLocalName(currentLocale))!selectedMessageTag.defaultTagName}</option>
		</#list>
		<#list notSelectedMessageTags as notSelectedMessageTag>
			<option value="${notSelectedMessageTag.messageTagId}" >${(notSelectedMessageTag.getLocalName(currentLocale))!notSelectedMessageTag.defaultTagName}</option>
		</#list>
	     </select>
	  </div>
	  <button type="submit" class="btn btn-default">${i18nBundle.filterMessages}</button>
	  <a href="/message" class="btn btn-default" role="button">${i18nBundle.reset}</a>
	</form>
        <div class="table-responsive">
	<table class="table table-striped">
		<thead>
			<th>${i18nBundle.datePub}</th>
			<th>${i18nBundle.dateValidTo}</th>
			<th>${i18nBundle.messageTags}</th>
			<th>${i18nBundle.heading}</th>
		</thead>
		<tbody>
		<#list messages?sort_by("datePub")?reverse as message>
		    <tr>
		    	<td>${(message.datePub?date)!""}</td>
		    	<td>${(message.dateValidTo?date)!""}</td>
		    	<td>
		    	<#if message.messageTagSet?has_content >
				<#list message.messageTagSet as messageTag>
					${(messageTag.getLocalName(currentLocale))!messageTag.defaultTagName},
				</#list>
			</#if>
		    	</td>
		    	<td><a href="/message?action=editMessageForm&messageId=${message.messageId}">${message.getLocalMessageWithFallback(currentLocale,user.organizationId.defaultLocale).heading}</a></td>
		    </tr>
		</#list>
		</tbody>
        </table>
        </div>
</#macro>
<@page_html/>
