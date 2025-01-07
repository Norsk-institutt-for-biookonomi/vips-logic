<#--
  Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
  
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
	
--><#include "master.ftl">
<#macro page_head>
	<title>Rognebærmøllstasjoner</title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/appleFruitMothObservationSitesMap.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			initMap("appleFruitMothForecastMap","");
		});
	</script>
</#macro>
<#macro page_contents>
<h1>${i18nBundle.appleFruitMoth}</h1>
<div class="singleBlockContainer">
	<div class="row">
		<div class="col-md-6">
			<table class="table table-striped">
                            <tr>
                                <td colspan="3"></td>
                                <th>Klasetall sist oppdatert</th>
                            </tr>
                            <#list observationSites as observationSite>
                            <tr>
                                <td>${observationSite.observationSiteName}</td>
                                <td><#if user.isSuperUser() || user.isAppleFruitMothAdministrator() ><button class="btn btn-default" onclick="window.location='/applefruitmoth?action=viewObservationSite&observationSiteId=${observationSite.observationSiteId}';">Rediger</button></#if></td>
                                <td><button class="btn btn-default" onclick="window.location='/applefruitmoth?action=registerCluster&observationSiteId=${observationSite.observationSiteId}';">Registrer klasetall</button></td>
                                <td>${observationSite.lastUpdatedClusterCount!"Ukjent"}</td>
                            </tr>
                            </#list>
			</table>
		</div>
		<div class="col-md-6">
			<div id="appleFruitMothForecastMap" class="map" style="height: 600px;">
				<div id="popover"></div>
			    </div>
			</div>
		</div>
	</div>
</div>	
</#macro>
<@page_html/>
