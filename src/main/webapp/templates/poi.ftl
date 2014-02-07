<#-- 
  Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 

  This file is part of VIPSLogic.
  VIPSLogic is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  VIPSLogic is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
 
  You should have received a copy of the GNU Affero General Public License
  along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
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
