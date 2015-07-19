<#-- 
  Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
  
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
        <title>POI</title>
</#macro>
<#macro page_contents>
        <h1>POI</h1>
	<ul>
	<#list weatherstations?sort_by("name") as weatherstation>
	    <li>${weatherstation.name}<#if weatherstation.weatherStationDataSourceId??>, ${weatherstation.weatherStationDataSourceId.uri!}<#else></#if></li>
	</#list>
        </ul>
</#macro>
<@page_html/>
