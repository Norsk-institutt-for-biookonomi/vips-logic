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
	<title>Barkbilleovervåking: Vedlikehold av fellelokaliteter for sesongen ${season}</title>
</#macro>
<#macro page_contents>
	<h1>Barkbilleovervåking</h1>
<h2>Vedlikehold av fellelokaliteter for sesongen ${season}</h2>
<div class="form-inline">
	<div class="form-group">
	Velg sesong <select class="form-control" onchange="window.location.href='/barkbeetle?action=seasonTrapsiteMaintenanceList&season=' + this.options[this.selectedIndex].value;">
		<#list firstAvailableSeason..lastAvailableSeason as availableSeason>
		<option value="${availableSeason}" <#if availableSeason==season>selected</#if>>${availableSeason}</option>
		</#list>
	</select>
	<div class="alert alert-warning"><i class="fa fa-arrow-up"></i> <span style="font-weight: bold; font-style: italic;">Pass på at du har valgt rett sesong!</span></div>
	</div>
</div>
<div class="row">
	<div class="col-md-12">
	Vedlikeholdsansvarlig behandler vedlikeholdsbehov oppgitt nedenfor. Kryss av for "Sak behandlet".
	</div>
</div>
<div class="singleBlockContainer">
	<div class="row">
	<table class="table table-striped">
		<thead>
			<th>Fylke</th>
			<th>Kommune</th>
			<th>Gårdsnr/bruksnr</th>
			<th>Høyde o.h.</th>
			<th>Eier</th>
			<th>Registrant</th>
			<th>Vedlikeholdsbehov</th>
			<th>Leveringsadresse</th>
			<th>Sak behandlet</th>
		</thead>
		<tbody id="seasonTrapsiteTBody">
		<#list seasonTrapsites as site>
			<#if !site.maintenanceCompleted && site.maintenanceDescription?has_content>
			<tr class="maintenanceNeeded">
				<td>${site.countyName!""}</td>
				<td>${site.municipalityName!""}</td>
				<td>${site.propertyNo!""}/${site.propertySectionNo!""}</td>
				<td>${site.altitude!""}</td>
				<td>${site.ownerName!""}</td>
				<td>${site.userId.firstName} ${site.userId.lastName}</td>
				<td>${site.maintenanceDescription!""}</td>
				<td>${site.maintenanceAddress!""}</td>
				<td><input type="checkbox" name="maintenanceCompleted" onclick="handleMaintenanceCompletedClick(${site.seasonTrapsiteId},this);" <#if site.maintenanceCompleted>checked</#if>></td>
			</tr>
			</#if>
		</#list>
		<#list seasonTrapsites as site>
			<#if site.maintenanceCompleted || ! site.maintenanceDescription?has_content>
			<tr class="maintenanceNotNeeded">
				<td>${site.countyName!""}</td>
				<td>${site.municipalityName!""}</td>
				<td>${site.propertyNo!""}/${site.propertySectionNo!""}</td>
				<td>${site.altitude!""}</td>
				<td>${site.ownerName!""}</td>
				<td>${site.userId.firstName} ${site.userId.lastName}</td>
				<td>${site.maintenanceDescription!""}</td>
				<td><input type="checkbox" name="maintenanceCompleted" onclick="handleMaintenanceCompletedClick(${site.seasonTrapsiteId},this);" <#if site.maintenanceCompleted>checked</#if>></td>
			</tr>
			</#if>
		</#list>
		</tbody>
	</table>
	</div>
</div>
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
<script type="text/javascript">
$(document).ready(function() {
		// Check for Internet Explorer
		var ua = window.navigator.userAgent;
		//console.info(ua);
		if(ua.indexOf("MSIE ") >= 0 || navigator.userAgent.match(/Trident.*rv\:11\./))
		{
			alert("Hei! Det ser ut som du bruker Internet Explorer. Vi anbefaler Firefox, Edge eller Chrome når du er på denne nettsiden.");
			return;
		}

});

/**
 * Using AJAX to update the backend system. Changing status color.
 */
function handleMaintenanceCompletedClick(seasonTrapsiteId, clickedCheckBox){
	var maintenanceCompleted = clickedCheckBox.checked;
	fetch(
		"/rest/barkbeetle/seasontrapsite/" + seasonTrapsiteId + "/maintenancecompleted/" + (maintenanceCompleted ? "true" : "false"),
		{
			method: "PATCH"
		}
	)
	.then(response => {
		if(response.status == 200)
		{
			var parentRow = clickedCheckBox.parentNode.parentNode;
			var maintenanceDesc = parentRow.getElementsByTagName("td")[6].textContent;
			var maintenanceNeeded = !clickedCheckBox.checked && maintenanceDesc.trim() != "";
			parentRow.classList.remove(maintenanceNeeded ? "maintenanceNotNeeded" : "maintenanceNeeded");
			parentRow.classList.add(maintenanceNeeded ? "maintenanceNeeded" : "maintenanceNotNeeded");
		}
	});
}

</script>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
	<style type="text/css">
		tr.maintenanceNotNeeded td {
			background-color: #1aff88 !important;
		}
		tr.maintenanceNeeded td {
			background-color: #ff6e66 !important;
		}
	</style>
</#macro>
<@page_html/>