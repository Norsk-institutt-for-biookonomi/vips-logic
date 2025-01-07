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
	<title>Barkbilleovervåking: Beregn fylkesvise fangsttall for sesongen ${season}</title>
</#macro>
<#macro page_contents>
<#if messageKey?has_content && messageKey=="storeOK">
		<div class="alert alert-success">Fangsttall for sesongen ${season} ble lagret og publisert</div>
	</#if>
	<h1>Barkbilleovervåking</h1>
<h2>Beregn fylkesvise fangsttall for sesongen ${season}</h2>
<div class="form-inline">
	<div class="form-group">
	Velg sesong <select class="form-control" onchange="window.location.href='/barkbeetle?season=' + this.options[this.selectedIndex].value;">
		<#list firstAvailableSeason..lastAvailableSeason as availableSeason>
		<option value="${availableSeason}" <#if availableSeason==season>selected</#if>>${availableSeason}</option>
		</#list>
	</select>
	<div class="alert alert-warning"><i class="fa fa-arrow-up"></i> <span style="font-weight: bold; font-style: italic;">Pass på at du har valgt rett sesong!</span></div>
	</div>
</div>
<div class="row">
	<div class="col-md-12">
	Si noe fornuftig her
	</div>
</div>
<div class="singleBlockContainer">
	<div class="row">
	<table class="table table-striped">
		
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
	<style type="text/css">
		
	</style>
</#macro>
<@page_html/>