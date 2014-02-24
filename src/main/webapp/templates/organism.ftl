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
        <title>${i18nBundle.organisms}</title>
        
</#macro>
<#macro custom_css>
        <link href="/css/3rdparty/selectize.bootstrap3.css" rel="stylesheet" media="screen" />
</#macro>
<#macro custom_js>
<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
<script src="/js/3rdparty/selectize.min.js"></script>
<script type="text/javascript">
$(document).ready(function() {
	$('#organismSearch').selectize({
		diacritics: true, // Support for international characters
		maxItems: 1,
		valueField: 'organismId',
		labelField: 'organismName',
		preload: true,
		onType: function(value){
			// Filter options on typed value 
			console.log("Type value:" + value);
		},
		load: function(query, callback) {
			// Load all options 
			// If options is empty, load all
			//if (!query.length) return callback();
			
			$.ajax({
			    url: '/rest/organism/list' + encodeURIComponent(query),
			    type: 'GET',
			    error: function() {
				callback();
			    },
			    success: function(res) {
				callback(res.repositories.slice(0, 10));
			    }
			});
			
			console.log("Load query: " + query);
		}
		// Render: add breadcrumb to search result
	});
});
</script>
</#macro>

<#macro page_contents>
        <h1>${i18nBundle.organisms}</h1>
        <div class="row">
		<div class="col-md-6">
			<select id="organismSearch">
			
			</select>
		Søkefelt og organismetre
		</div>
		<div class="col-md-6">
		Skjema for redigering av organisme
		</div>
	</div>
</#macro>
<@page_html/>
