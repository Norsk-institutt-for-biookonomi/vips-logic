<#--
  Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
  
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
	
--><#include "master.ftl">
<#macro page_head>
	
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
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
			<ul>
			<#list observationSites as observationSite>
			<li><a href="/applefruitmoth?action=viewObservationSite&observationSiteId=${observationSite.observationSiteId}">${observationSite.observationSiteName}</a></li>
			</#list>
			</ul>
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
