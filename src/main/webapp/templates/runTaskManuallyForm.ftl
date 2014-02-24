<#--
  Copyright (c) 2013 Bioforsk <http://www.bioforsk.no/>. 

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
        <title>${i18nBundle.configureAndRunManually}</title>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script src="/js/validateForm.js"></script>
	
	<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
	<link href="http://code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
	<script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
	<script type="text/javascript" src="/js/3rdparty/moment.min.js"></script>
	<script type="text/javascript">
		// Make sure that there is a date picker present for HTML5 
		// date input fields
		if (!Modernizr.inputtypes.date) {
		    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
		}
		
		formDefinitions["runTaskManuallyForm"] = ${vipsLogicTask.getConfigFormDefinition(currentLocale)};
		
	</script>
</#macro>
<#macro page_contents>
        <h1>${i18nBundle.configureAndRunManually}</h1>
        <h2>${vipsLogicTask.getName(currentLocale)}</h2>
        <form id="runTaskManuallyForm" role="form" action="/scheduling?action=runTaskManuallyFormSubmit" method="POST" onsubmit2="return false;" onsubmit="try{ return validateForm(this);}catch(err){alert(err);return false;}">
        ${vipsLogicTaskFormFields}
        <!--button type="button" onclick="console.log(validateForm(document.getElementById('runTaskManuallyForm')));">Validate</button-->
        <button type="submit" class="btn btn-default">${i18nBundle.runTask}</button>
	<button type="button" class="btn btn-warning" onclick="window.location.href='/scheduling?action=viewAllTasks'">${i18nBundle.cancel}</button>
	</form>
</#macro>
<@page_html/>
