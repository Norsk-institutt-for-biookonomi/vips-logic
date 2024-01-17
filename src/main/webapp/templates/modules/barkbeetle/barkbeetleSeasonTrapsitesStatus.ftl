<#--
    Copyright (c) 2021 NIBIO <http://www.nibio.no/>. 
  
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
	
--><#include "../../master.ftl">
<#macro page_head>
	<title>Barkbilleovervåking: Fangststatus for fellelokaliteter for sesongen ${season}</title>
</#macro>
<#macro page_contents>
<p>
	<a href="/barkbeetle?season=${season}" class="btn btn-default back" role="button">${i18nBundle.back}</a>
</p>
<h1>Barkbilleovervåking</h1>
<h2>Fangststatus for fellelokaliteter for sesongen ${season}</h2>
<p>
	Ved å huke av på "Purr opp" for en fellelokalitet, vil registrantens e-postadresse legges til i tekstfeltet 
	over tabellen. Herfra kan du kopiere (f.eks. merk alt med [CTRL]-A og kopier til utklippstavlen med 
	[CTRL]-C) og lime inn i	e-postprogrammet ditt.
</p>
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
			<th colspan="2">
				<div class="input-group">
		      		<div class="input-group-addon"><span class="fa fa-search"></span></div>
					<input class="form-control" type="text" id="textSearchField" placeholder="Filtrer på fylke" oninput="filter(this.value);"/>
				</div>
			</th>
			<th></th>
			<th colspan="4">
				<input class="form-control" type="text" id="emails" placeholder="E-poster til purring"/>
			</th>
		</thead>
		<thead>
			<th>Fylke</th>
			<th>Kommune</th>
			<th>Gårdsnr/bruksnr</th>
			<th>Høyde o.h.</th>
			<th>Eier</th>
			<th>Registrant</th>
			<th>Purr opp</th>
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
				<td><input type="checkbox" name="followUp_${site.seasonTrapsiteId}" onclick="updateEmailList();"/></td>
			</tr>
			<tr>
				<td colspan="3"></td>
				<td>Tømmerunde (ukenr)</td>
				<td>Tømmedato</td>
				<td colspan="2">Snittverdi</td>
			</tr>
			<#list site.trapsiteRegistrationCollectionSortedWithBlanks as tReg>
			<tr>
				<td colspan="3"></td>
				<td>${tReg.trapsiteRegistrationPK.week}</td>
				<td>${tReg.dateRegistration!""}</td>
				<td colspan="2">${tReg.registrationAverage!"IKKE UTFYLT"}</td>
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
	
	var siteEmails = {
		<#list seasonTrapsites as site>
		${site.seasonTrapsiteId}: "${site.userId.email}",
		</#list>
	};
	
	function updateEmailList(){
		var emails = "";
		var checkedBoxes = document.getElementById("seasonTrapsitesTable").querySelectorAll("input[type=checkbox]:checked");
		for(var i=0; i<checkedBoxes.length;i++)
		{
			var cb = checkedBoxes[i];
			console.info(cb);
			var sti = parseInt(cb.name.split("_")[1]);
			emails += siteEmails[sti] + "; ";
		}
		console.info(emails);
		document.getElementById("emails").value=emails;
		//console.info(checkedBoxes);
	}
</script>
</#macro>
<#macro custom_css>
	
</#macro>
<@page_html/>