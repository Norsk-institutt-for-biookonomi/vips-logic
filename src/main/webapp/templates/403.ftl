<#include "master.ftl">
<#macro navbar_collapse>
</#macro>
<#macro page_head>
        <title>ERROR 403 - Forbidden</title>
</#macro>
<#macro page_contentwrap_start></#macro>
<#macro page_contentwrap_end></#macro>
<#macro page_contents>
 <h1>ERROR 403 - Forbidden</h1>
  <p>${message}</p>
  
  <#assign nextPage= Request["jakarta.servlet.forward.servlet_path"] + "?" + Request["jakarta.servlet.forward.query_string"]!"">
  <p><a href="/login?nextPage=${nextPage?url}">To login page</a></p>
</#macro>
<@page_html/>

