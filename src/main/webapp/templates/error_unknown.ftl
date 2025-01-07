<#-- 
  Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
  
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
<#macro navbar_collapse>
</#macro>
<#macro page_head>
        <title>ERROR ${Request["jakarta.servlet.error.status_code"]}</title>
</#macro>
<#macro page_contentwrap_start></#macro>
<#macro page_contentwrap_end></#macro>
<#macro page_contents>
 <h1>ERROR ${Request["jakarta.servlet.error.status_code"]}</h1>
  <p>${message}</p>
  <p><a href="/">To front page</a></p>
</#macro>
<@page_html/>

