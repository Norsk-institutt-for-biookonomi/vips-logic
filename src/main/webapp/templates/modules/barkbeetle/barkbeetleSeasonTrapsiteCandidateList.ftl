<#--
    Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
  
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
	<title>Barkbilleovervåking: Fellelokalitet-kandidater for sesongen ${season}</title>
</#macro>
<#macro page_contents>
<#if messageKey?has_content && messageKey=="activateOK">
	<div class="alert alert-success">Fellelokaliteten ble aktivert ${.now}, og ble dermed flyttet over til <a href="/barkbeetle?season=${season}">lista over sesongens fellelokaliteter</a></div>
</#if>
<#if messageKey?has_content && messageKey=="deleteOK">
	<div class="alert alert-success">Fellelokaliteten ble slettet ${.now}</div>
</#if>
<p>
	<a href="/barkbeetle?season=${season}" class="btn btn-default back" role="button">${i18nBundle.back}</a>
</p>
<h1>Barkbilleovervåking</h1>
<h2>Fellelokalitet-kandidater for sesongen ${season}</h2>
<div class="form-inline">
	<div class="form-group">
	Velg sesong <select class="form-control" onchange="window.location.href='/barkbeetle?action=listSeasonTrapsiteCandidates&season=' + this.options[this.selectedIndex].value;">
		<#list firstAvailableSeason..lastAvailableSeason as availableSeason>
		<option value="${availableSeason}" <#if availableSeason==season>selected</#if>>${availableSeason}</option>
		</#list>
	</select>
	<div class="alert alert-warning"><i class="fa fa-arrow-up"></i> <span style="font-weight: bold; font-style: italic;">Pass på at du har valgt rett sesong!</span></div>
	</div>
</div>
<div class="row">
	<div class="col-md-12">
	<p>
	Første steg er å <u>stille inn på riktig sesong som</u> det planlegges for under 
	«Velg sesong» øverst på siden. I en ny sesong mangler tabellen over 
	lokaliteter. For å lette forberedelsene videre kan du nå velge å <u>kopiere 
	fjorårets liste som et utkast ved å trykke på knappen «Snu bunken»</u>. Det kommer 
	nå opp en kandidatliste, men informasjon for hver ny lokalitet må oppdateres. 
	Dessuten kan det være aktuelt å legge til nye lokaliteter (knapp over listen), 
	eller slette lokaliteter som ikke skal være med (knapp til høyre ved hver 
	lokalitet i tabellen). Du går nå inn på <u>«Endre lokalitetsinfo»</u> for hver lokalitet 
	i listen som skal oppdateres. Her må du panorere og forstørre kart for å plassere 
	lokaliteten med et <u>trykk i kartet</u>. Dermed genereres høyde over havet og 
	navn og numre for fylke og kommune automatisk (de genererte feltene skal ikke 
	redigeres etterpå, men den kan omplasseres med et nytt trykk i kartet). Bortsett 
	fra at lokaliteten er plassert i riktig kommune, behøver den ikke være helt riktig 
	plassert i første omgang. Riktig plassering gjøres gjerne senere av Registrant når 
	han/hun oppdaterer informasjon og legger inn data. Du må også <u>fylle inn alle felter 
	med stjerne</u>, før du <u>lagrer med lagreknappen</u> i bunnen av skjemaet. Det kommer nå en 
	melding øverst på siden at lagringen har vært vellykket. Etter dette trykker du «Tilbake» 
	over skjemaet for å komme tilbake til kandidatlisten. I kandidatlisten leter du opp den 
	samme lokaliteten (alfabetisk plassering) og trykker på <u>«Aktiver fellelokalitet»</u>. 
	Først nå er lokaliteten aktivert og i bruk. Den forsvinner nå fra kopieringslisten hvor du 
	er, og den legger seg til i den oppdaterte listen for året du planlegger (for å se den 
	oppdaterte listen trykker du «Tilbake» over kopieringslisten som du står i).
	</p>
 	<p>
	Om oppdatering av listen: Det bør være lokaliteter i alle kommuner som tidligere har vært 
	med. For kommuner som har vært slått sammen, bør det fortsatt være lokaliteter i hver av 
	de tidligere delkommunene (dvs. flere lokaliteter innenfor den sammenslåtte kommunen). 
	Fellestedet skal være på en <u>hogstflate på minst 2 dekar hvor det i høst-/vintersesongen 
	før denne sommersesongen ble avvirket gran</u>. Det innebærer at lokaliteten ikke kan 
	ligge på samme hogstflate flere år. For alle lokaliteter i listen gjelder det at årets 
	fellested bør om mulig ligge nær fjorårets fellested, og på tilnærmet samme skogtype 
	(ønske om nærhet og skogtype kan avvikes når det ikke er mulig). Både Registrant og 
	Barkbille­fylkeskontakt kan endre lokalitetsinformasjon på et senere tidspunkt for å gjøre 
	den mer korrekt.
	</p>

	</div>
</div>
<div class="singleBlockContainer">
	<div class="row">
	<#if seasonTrapsites?size == 0>
		<div class="alert alert-warning">
			Det finnes for øyeblikket ingen fjorårskopier tilgjengelig. Dette kan enten skyldes at administrator
			ikke har "snudd bunken", eller at alle kopiene er ferdig revidert.
		</div>
		<#if userIsAdmin>
			<p>
			<button type="button" onclick="if(confirm('Ønsker du virkelig å kopiere fellelokaliteter fra ${season-1} til ${season}?')){window.location.href='/barkbeetle?action=copySeasonTrapsites&fromSeason=${season-1}&toSeason=${season}';}" class="btn btn-primary" role="button">Snu bunken</button>
			</p>
		</#if>
	</#if>
	<table class="table table-striped">
		<thead>
			<th>Fylke</th>
			<th>Kommune</th>
			<th>Eier</th>
			<th>Lokalitet oppdatert</th>
			<th>Registrant</th>
			<th></th>
			<th></th>
			<th></th>
		</thead>
		<tbody>
		<#list seasonTrapsites as site>
			<tr>
				<td>${site.countyName!""}</td>
				<td>${site.municipalityName!""}</td>
				<td><span class="alert-${site.ownerName?has_content?string("success","danger")}">${site.ownerName!"Ikke oppgitt"}</span></td>
				<td><span class="alert-${site.locationUpdated?string("success","danger")}">${site.locationUpdated?string("Ja","Nei")}</span></td>
				<td>${site.userId.firstName} ${site.userId.lastName}</td>
				<#if userIsAdmin || userIsCountyAdmin || user.userId == site.userId.userId>
				<td><a href="/barkbeetle?action=editSeasonTrapsite&season=${season}&seasonTrapsiteId=${site.seasonTrapsiteId}" class="btn btn-default" role="button">Endre lokalitetsinfo</a></td>
				<td><#if site.ownerName?has_content && site.locationUpdated><a href="/barkbeetle?action=activateSeasonTrapsite&seasonTrapsiteId=${site.seasonTrapsiteId}" class="btn btn-default" role="button">Aktiver fellelokalitet</a></#if></td>
				<#else>
				<td colspan="2"></td>
				</#if>
				<td><button type="button" class="btn btn-danger" onclick="if(confirm('Ønsker du virkelig å slette?')){window.location.href='/barkbeetle?action=seasonTrapsiteDelete&redirectAction=listSeasonTrapsiteCandidates&seasonTrapsiteId=${site.seasonTrapsiteId}';}">Slett</button></td>
			</tr>
		</#list>
		</tbody>
	</table>
	</div>
</div>
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/constants.js"></script>
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
</script>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
	<style type="text/css">
		td.status_1 {
			background-color: #ffe066 !important;
		}
		td.status_2 {
			background-color: #1aff88 !important;
		}
		td.status_3 {
			background-color: #ff6e66 !important;
		}

		#seasonTrapsiteListMap {
			height: 500px;	
		}	
		
		div.popover {
			min-width: 250px !important;
		}
	</style>
</#macro>
<@page_html/>