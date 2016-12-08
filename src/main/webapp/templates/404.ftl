<#include "master.ftl">
<#macro navbar_collapse>
</#macro>
<#macro page_head>
        <title>ERROR 404</title>
</#macro>
<#macro page_contentwrap_start></#macro>
<#macro page_contentwrap_end></#macro>
<#macro page_contents>
 <h1>ERROR 404 - page not found</h1>
  <p>${message}</p>
  <p><a href="/">To front page</a></p>
</#macro>
<@page_html/>
