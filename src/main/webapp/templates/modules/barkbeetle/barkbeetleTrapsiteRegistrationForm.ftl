<#--
    Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
  
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
	
--><#include "../../master.ftl">
<#macro page_head>
	<title>Barkbilleregistrering: Registrer data for lokalitet X, sesong ${seasonTrapsite.season}</title>
</#macro>
<#macro page_contents>
	<#if messageKey?has_content>
		<div class="alert alert-success">Data ble lagret ${.now}</div>
	</#if>
	<h1>Registrer data for lokalitet X, sesong ${seasonTrapsite.season}</h1>
	<p>
		<a href="/barkbeetle?action=editSeasonTrapsite&season=${seasonTrapsite.season}&seasonTrapsiteId=${seasonTrapsite.seasonTrapsiteId}" class="btn btn-default back" role="button">${i18nBundle.back}</a>
	</p>
	<div class="singleBlockContainer">
		<div class="row">
		<form id="trapsiteRegistrationForm" action="/barkbeetle?action=trapsiteRegistrationFormSubmit" method="post">
		<input type="hidden" name="seasonTrapsiteId" value="${seasonTrapsite.seasonTrapsiteId}"/>
		<table class="table table-striped">
			<thead>
				<th></th>
				<th></th>
				<th colspan="2">BEKAFelle 1</th>
				<th colspan="2">BEKAFelle 2</th>
				<th colspan="2">BEKAFelle 3</th>
				<th colspan="2">BEKAFelle 4</th>
				<th></th>
				<th></th>
				<th></th>
				<th></th>
			</thead>
			<thead>
				<th>Uke</th>
				<th>Dato</th>
				<th>ml</th>
				<th>antall</th>
				<th>ml</th>
				<th>antall</th>
				<th>ml</th>
				<th>antall</th>
				<th>ml</th>
				<th>antall</th>
				<th>Status</th>
			</thead>
			
			<tbody>
			<#list trapsiteRegistrations as reg>
				<tr>
					<td rowspan="2">${reg.trapsiteRegistrationPK.week}</td>
					<td rowspan="2"><input class="form-control" type="date" name="${reg.trapsiteRegistrationPK.week}_dateRegistration" value="${reg.dateRegistration!""}"<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"<#else> required="required"</#if>/></td>
					<td><input class="form-control" type="number" name="${reg.trapsiteRegistrationPK.week}_trap1_ml" onblur="setCalculatedAmount(this);" value=""<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>
					<td><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap1" value="${reg.trap1!"M"}"<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>
					<td><input class="form-control" type="number" name="${reg.trapsiteRegistrationPK.week}_trap2_ml" onblur="setCalculatedAmount(this);" value=""<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>
					<td><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap2" value="${reg.trap2!"M"}"<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>
					<td><input class="form-control" type="number" name="${reg.trapsiteRegistrationPK.week}_trap3_ml" onblur="setCalculatedAmount(this);" value=""<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>
					<td><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap3" value="${reg.trap3!"M"}"<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>
					<td><input class="form-control" type="number" name="${reg.trapsiteRegistrationPK.week}_trap4_ml" onblur="setCalculatedAmount(this);" value=""<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>
					<td><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap4" value="${reg.trap4!"M"}"<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>
					
					<td rowspan="2"><#if reg.dateRegistration?has_content>
						<select class="form-control status_${reg.registrationStatusTypeId}" name="${reg.trapsiteRegistrationPK.week}_registrationStatusTypeId" onchange="this.className='form-control status_' + this.options[this.selectedIndex].value;">
						<#list registrationStatusTypes as registrationStatusType>
							<option class="status_${registrationStatusType.registrationStatusTypeId}" value="${registrationStatusType.registrationStatusTypeId}"
							<#if reg.registrationStatusTypeId?has_content && reg.registrationStatusTypeId==registrationStatusType.registrationStatusTypeId>selected="selected"</#if>
							>${registrationStatusType.title}</option>
						</#list>
						</select>
					</#if></td>
				</tr>
				<tr>
					<td colspan="2"><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap1Remarks" placeholder="Kommentar felle 1" value="${reg.trap1Remarks!""}"<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>
					<td colspan="2"><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap2Remarks" placeholder="Kommentar felle 2" value="${reg.trap2Remarks!""}"<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>
					<td colspan="2"><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap3Remarks" placeholder="Kommentar felle 3" value="${reg.trap3Remarks!""}"<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>
					<td colspan="2"><input class="form-control" type="text" name="${reg.trapsiteRegistrationPK.week}_trap4Remarks" placeholder="Kommentar felle 4" value="${reg.trap4Remarks!""}"<#if reg.trapsiteRegistrationPK.week gt currentWeek> disabled="disabled"</#if>/></td>

				</tr>
			</#list>
			</tbody>
		</table>
		<p>
			<button type="submit" class="btn btn-default">Lagre</button>
		</p>
		</form>
		</div>
	</div>
</#macro>
<#macro custom_js>
<script type="text/javascript" src="/js/resourcebundle.js"></script>
<script src="//code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
<script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
<script type="text/javascript">
		var theForm = document.getElementById("trapsiteRegistrationForm");
		// Make sure that there is a date picker present for HTML5 
		// date input fields
		if (!Modernizr.inputtypes.date) {
		    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
		}
		
		function setCalculatedAmount(sourceField)
		{

			var volumeValue = parseFloat(sourceField.value);
			if(!isNaN(volumeValue))
			{
				theForm[sourceField.name.substring(0,sourceField.name.indexOf("_ml"))].value = volumeValue * 50;
			}
		}
		
</script>
</#macro>
<#macro custom_css>
	<link href="//code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
	<style type="text/css">
		select option[value="1"], select.status_1 {
			background-color: #ffe066 !important;
		}
		select option[value="2"], select.status_2 {
			background-color: #1aff88 !important;
		}
		select option[value="3"], select.status_3 {
			background-color: #ff6e66 !important;
		}

			
	</style>
</#macro>
<@page_html/>