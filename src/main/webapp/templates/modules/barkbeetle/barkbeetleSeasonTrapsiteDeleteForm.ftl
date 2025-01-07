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
	<title>Barkbilleovervåking: Slett fellelokalitet for ${seasonTrapsite.season}</title>
</#macro>
<#macro custom_js>
	
</#macro>
<#macro custom_css>
	
</#macro>
<#macro page_contents>
	<h1>Barkbilleovervåking</h1>
	<h2>Slett fellelokalitet for sesongen ${seasonTrapsite.season}</h2>
	
	
	
<div class="singleBlockContainer">
	<div class="row">
	<div class="col-md-12">
		<h2>${seasonTrapsite.countyName}/${seasonTrapsite.municipalityName}</h2>
		<p>Denne lokaliteten har <a href="/barkbeetle?action=registerData&seasonTrapsiteId=${seasonTrapsite.seasonTrapsiteId}" target="new">${seasonTrapsite.trapsiteRegistrationCollection?size} registreringer</a></p>
		<#if userIsAdmin && seasonTrapsite.seasonTrapsiteId?has_content>
		  	<button type="button" class="btn btn-danger" onclick="window.location.href='/barkbeetle?action=seasonTrapsiteDelete&seasonTrapsiteId=${seasonTrapsite.seasonTrapsiteId}';">Jeg er ved mine fulle fem. Slett!</button>
		  	<button type="button" class="btn btn-primary" onclick="window.location.href='/barkbeetle?action=editSeasonTrapsite&seasonTrapsiteId=${seasonTrapsite.seasonTrapsiteId}';">Avbryt</button>
		  </#if>
	</div>

</div>
</#macro>
<@page_html/>