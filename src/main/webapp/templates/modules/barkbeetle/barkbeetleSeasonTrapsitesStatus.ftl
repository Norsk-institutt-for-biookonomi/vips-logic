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
	<title>Barkbilleovervåking: Fangststatus for fellelokaliteter for sesongen</title>
</#macro>
<#macro page_contents>

	<h1>Barkbilleovervåking</h1>
<h2>Fangststatus for fellelokaliteter for sesongen</h2>
<div class="form-inline">
	<div class="form-group">
	Velg sesong <select class="form-control" onchange="window.location.href='/barkbeetle?action=listSeasonTrapsitesStatus&season=' + this.options[this.selectedIndex].value;">
		<#list firstAvailableSeason..lastAvailableSeason as availableSeason>
		<option value="${availableSeason}" <#if availableSeason==season>selected</#if>>${availableSeason}</option>
		</#list>
	</select>
	<div class="alert alert-warning"><i class="fa fa-arrow-up"></i> <span style="font-weight: bold; font-style: italic;">Pass på at du har valgt rett sesong!</span></div>
	</div>
</div>
<div class="singleBlockContainer">
	<div class="row table-responsive">
	<table id="seasonTrapsitesTable" class="table table-striped">
		<thead>
			<th><div class="input-group">
	      	<div class="input-group-addon"><span class="fa fa-search"></span></div>
			<input class="form-control" type="text" id="textSearchField" placeholder="Filtrer på fylke" oninput="filter(this.value);"/>
		</div></th>
			<th colspan="7"></th>
		</thead>
		<thead>
			<th>Fylke</th>
			<th>Kommune</th>
			<th>Gårdsnr/bruksnr</th>
			<th>Høyde o.h.</th>
			<th>Eier</th>
			<th>Registrant</th>
		</thead>
		<#list seasonTrapsites as site>
		<tbody id="seasonTrapsiteTBody_${site.seasonTrapsiteId}">
			<tr>
				<td>${site.countyName!""}</td>
				<td>${site.municipalityName!""}</td>
				<td>${site.propertyNo!""}/${site.propertySectionNo!""}</td>
				<td>${site.altitude!""}</td>
				<td>${site.ownerName!""}</td>
				<td>${site.userId.firstName} ${site.userId.lastName}</td>
			</tr>
			<tr>
				<td colspan="3"></td>
				<td>Tømmerunde (ukenr)</td>
				<td>Tømmedato</td>
				<td>Snittverdi</td>
			</tr>
			<#list site.trapsiteRegistrationCollection as tr>
			<tr>
				<td colspan="3"></td>
				<td>${tr.trapsiteRegistrationPK.week}</td>
				<td>${tr.dateRegistration}</td>
				<td>${tr.registrationAverage!"IKKE UTFYLT"}</td>
			</tr>
			</#list>
		</tbody>
		</#list>
	</table>
	</div>
</div>
</#macro>
<#macro custom_js>
<script type="text/javascript">
	function filter(searchTxt){
		var rowSets = document.getElementById("seasonTrapsitesTable").getElementsByTagName("tbody");
		for(var i=0;i<rowSets.length;i++)
		{
			var countyCell = rowSets[i].getElementsByTagName("td")[0];
			var visible = searchTxt.trim() == "" ? true 
			: countyCell.textContent.toLowerCase().indexOf(searchTxt.toLowerCase()) > -1;
			rowSets[i].setAttribute("style","display:" + (visible ? "table-row-group" : "none"));
		}
	}
</script>
</#macro>
<#macro custom_css>
	
</#macro>
<@page_html/>