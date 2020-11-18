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
	<title>Barkbilleregistrering: Fellelokaliteter for sesongen ${season}</title>
</#macro>
<#macro page_contents>
<#if messageKey?has_content && messageKey=="deleteOK">
		<div class="alert alert-success">Fellelokaliteten ble slettet</div>
	</#if>
<h1>Fellelokaliteter for sesongen ${season}</h1>
<div class="row">
	<div class="col-md-7">
	<p>Barkbilleovervåkingen er organisert gjennom «Barkbilleadministratorer» 
	ved Nibio, «Barkbillefylkeskontakter» og «Registranter». «Barkbillefylkeskontakter» 
	oppretter en liste over fellelokaliteter og tilordner «Registranter» til disse. 
	Etter dette kan <em>«Registrantene»</em> oppdatere informasjon (plassere riktig 
	i kart m.m.) og legge inn data for sin(e) lokalitet(er).<br/>
	Alle feller må være av typen BEKA (også kalt Novefella) (se bildet), og feromonet som brukes må 
	være av typen <a href="http://kjemikonsult.no/Norsk/Produkter.html" target="new">Ipslure</a>
	(=Norlure; tilsendt fra Nibio). Les instruksjoner som er aktuelle for deg nøye, slik som 
	<a href="/images/modules/barkbeetle/Instruks_Barkbillefylkeskontakter.pdf" target="new">Instruks
	Barkbillefylkeskontakter</a>,
	<a href="/images/modules/barkbeetle/Instruks_registranter_i_Barkbilleovervakingen.pdf" target="new">Instruks registranter</a>, og 
	<a href="/images/modules/barkbeetle/Montering_av_barkbillefelle.pdf">Monteringsanvisning
	Beka-felle</a> (kun noen få har i tillegg 
	<a href="/images/modules/barkbeetle/Instruks_utvidet_sesong.pdf" target="new">utvidet 
	sesong</a> med egen instruksjon).</p>
	
	<p><strong>Barkbillefylkeskontakt</strong> oppretter her lokaliteter for sitt fylke («Legg til 
	ny») og tildeler disse registranter som sørger for fellemontering, tømming 
	av feller og fortløpende innlegging av data gjennom sesongen. Det bør være 
	lokaliteter i alle kommuner som tidligere har vært med (<strong>NB</strong> kommuner før 
	sammenslåinger etter 2012). Liste over registranter, inkludert navn/telefon 
	og epost, sendes tor-einar.skog@nibio.no så	tidlig som mulig før sesong for 
	å opprette tilgang i systemet for registrantene. Etter dette får registrantene 
	innloggingsadgang tilsendt per epost. Se utdypende informasjon i 
	<a href="/images/modules/barkbeetle/Instruks_Barkbillefylkeskontakter.pdf" target="new">Instruks
	Barkbillefylkeskontakter</a> (se også 
	<a href="/images/modules/barkbeetle/Instruks_registranter_i_Barkbilleovervakingen.pdf" target="new">Instruks 
	Registranter</a> for å forstå deres oppgaver).</p>
	
	<p><strong>Registranter</strong> må oppdatere informasjon under «Endre lokalitetsinfo» for 
	sin(e) lokalitet(er) så snart som mulig og sørge for at feller blir montert 
	innen 20. april. Fellene i standardlokaliteter tømmes etter hver av fire 
	perioder, dvs. på mandag eller tirsdag i ukene 21, 24, 28 og 33. Data fra 
	hver tømming skal legges inn under «Registrer» innen mandag i påfølgende uke, 
	og blir dermed del av en fortløpende kartvisning av barkbillenivået gjennom 
	sesongen. Se utdypende informasjon i 
	<a href="/images/modules/barkbeetle/Instruks_registranter_i_Barkbilleovervakingen.pdf" target="new">Instruks 
	Registranter</a>, og 
	<a href="/images/modules/barkbeetle/Montering_av_barkbillefelle.pdf">Monteringsanvisning
	Beka-felle</a></p>
	<p>Merk at fylkesnr, fylkesnavn, kommunenr og kommunenavn som genereres er 
	   fra før sammenslåinger (2012).
	   </p>
	</div>
	<div class="col-md-1">
	<img src="/images/modules/barkbeetle/Bilde_BEKA_felle.jpg" style="height: 250px;"/>
	</div>
	<div class="col-md-4">
		<div id="seasonTrapsiteListMap" class="map" style="border: 1px solid black;">
				<div id="popover"></div>
		</div>
	</div>
</div>
<#if userIsAdmin || userIsCountyAdmin>
<p>
	<a href="/barkbeetle?action=editSeasonTrapsite&season=${season}" class="btn btn-default" role="button">${i18nBundle.addNew}</a>
</p>
</#if>
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
			<th></th>
			<th></th>
		</thead>
		<tbody>
		<#list seasonTrapsites as site>
			<tr>
				<td>${site.countyName!""}</td>
				<td>${site.municipalityName!""}</td>
				<td>${site.propertyNo!""}/${site.propertySectionNo!""}</td>
				<td>${site.altitude!""}</td>
				<td>${site.ownerName!""}</td>
				<td>${site.userId.firstName} ${site.userId.lastName}</td>
				<#if userIsAdmin || userIsCountyAdmin || user.userId == site.userId.userId>
				<td><a href="/barkbeetle?action=editSeasonTrapsite&season=${season}&seasonTrapsiteId=${site.seasonTrapsiteId}" class="btn btn-default" role="button">Endre lokalitetsinfo</a></td>
				<td<#if userIsAdmin || userIsCountyAdmin> class="status_${site.mostSevereRegistrationStatus}"</#if>><a href="/barkbeetle?action=registerData&seasonTrapsiteId=${site.seasonTrapsiteId}" class="btn btn-default" role="button">Registrer<#if userIsAdmin || userIsCountyAdmin>/godkjenn</#if> fangstdata</a></td>
				<#else>
				<td colspan="2"></td>
				</#if>
			</tr>
		</#list>
		</tbody>
	</table>
	</div>
</div>
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
	<script type="text/javascript" src="/js/modules/barkbeetle/seasonTrapsiteListMap.js"></script>
<script type="text/javascript">
$(document).ready(function() {
		initMap(${season});
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