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
<h1>Fellelokaliteter for sesongen ${season}</h1>
<#if userIsAdmin>
<p>
	<a href="/barkbeetle?action=editSeasonTrapsite&season=${season}" class="btn btn-default" role="button">${i18nBundle.addNew}</a>
</p>
</#if>
<div class="singleBlockContainer">
	<div class="row">
	<div class="col-md-12">
	<h1>Kartet kommer her!</h1>
	</div>
	</div>
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
				<#if userIsAdmin || user.userId == site.userId.userId>
				<td><a href="/barkbeetle?action=editSeasonTrapsite&season=${season}&seasonTrapsiteId=${site.seasonTrapsiteId}" class="btn btn-default" role="button">Endre lokalitetsinfo</a></td>
				<td><a href="/barkbeetle?action=registerData&seasonTrapsiteId=${site.seasonTrapsiteId}" class="btn btn-default" role="button">Registrer data</a></td>
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
<@page_html/>