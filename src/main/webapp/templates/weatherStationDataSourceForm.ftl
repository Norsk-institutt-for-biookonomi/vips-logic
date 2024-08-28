<#-- 
  Copyright (c) 2024 NIBIO <http://www.nibio.no/>. 
  
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
<#assign formId = "weatherStationDataSourceForm">
<#macro page_head>
        <title><#if weatherStationDataSource.weatherStationDataSourceId?has_content>${i18nBundle.editWeatherStationDataSource} ${weatherStationDataSource.name}<#else>${i18nBundle.newWeatherStationDataSource}</#if></title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/ >
</#macro>
<#macro custom_js>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/resourcebundle.js"></script>
	<script type="text/javascript" src="/js/validateForm.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			// Load main form definition (for validation)
			loadFormDefinition("${formId}");
			
		});
        function handleWeatherStationDataSourceDelete(weatherStationDataSourceId){  
                window.location.href="/weatherstationdatasource?action=deleteWeatherStationDataSource&weatherStationDataSourceId=" + weatherStationDataSourceId;
        }
                
	</script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
	<p><a href="/weatherstationdatasource" class="btn btn-default back" role="button">${i18nBundle.back}</a></p>
    <h1><#if weatherStationDataSource.weatherStationDataSourceId?has_content>${i18nBundle.editWeatherStationDataSource} ${weatherStationDataSource.name}<#else>${i18nBundle.newWeatherStationDataSource}</#if></h1>
    <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
		<#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
    <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
    <div class="row">
		<div class="col-md-12">
			<form id="${formId}" role="form" action="/weatherstationdatasource?action=weatherStationDataSourceFormSubmit" method="POST" onsubmit="return validateForm(this);">
                <input type="hidden" name="weatherStationDataSourceId" value="${weatherStationDataSource.weatherStationDataSourceId!"-1"}"/>
                <div class="form-group">
                    <label for="name">${i18nBundle.name}</label>
                    <input type="text" class="form-control" name="name" placeholder="${i18nBundle.name}" value="${(weatherStationDataSource.name)!""}" onblur="validateField(this); "/>
			        <span class="help-block" id="${formId}_name_validation"></span>
			    </div>
                <div class="form-group">
                    <label for="defaultDescription">${i18nBundle.description}</label>
                    <input type="text" class="form-control" name="defaultDescription" placeholder="${i18nBundle.description}" value="${(weatherStationDataSource.defaultDescription)!""}" onblur="validateField(this); "/>
			        <span class="help-block" id="${formId}_defaultDescription_validation"></span>
			    </div>
                <div class="form-group">
                    <label for="uri">URI</label>
                    <input type="url" class="form-control" name="uri" placeholder="URI" value="${(weatherStationDataSource.uri)!""}" onblur="validateField(this); "/>
			        <span class="help-block" id="${formId}_uri_validation"></span>
			    </div>
                <div class="form-group">
                    <label for="datafetchUriExpression">${i18nBundle.datafetchUriExpression}</label>
                    <input type="url" class="form-control" name="datafetchUriExpression" placeholder="${i18nBundle.datafetchUriExpression}" value="${(weatherStationDataSource.datafetchUriExpression)!""}" onblur="validateField(this); "/>
			        <span class="help-block" id="${formId}_datafetchUriExpression_validation"></span>
			    </div>
                <div class="form-group">
                    <label for="infoUriExpression">${i18nBundle.infoUriExpression}</label>
                    <input type="url" class="form-control" name="infoUriExpression" placeholder="${i18nBundle.infoUriExpression}" value="${(weatherStationDataSource.infoUriExpression)!""}" onblur="validateField(this); "/>
			        <span class="help-block" id="${formId}_infoUriExpression_validation"></span>
			    </div>
                <div class="form-group">
                <div class="checkbox">
                    <label>
                    <input type="checkbox" id="isGrid" name="isGrid" value="true"<#if weatherStationDataSource.isGrid?has_content && weatherStationDataSource.isGrid == true> checked="checked"</#if>/>
                    </label>
                    ${i18nBundle.isGridWeatherDataSource}
                    <span class="help-block" id="${formId}_isGrid_validation"></span>
                </div>
                <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
			  <#if weatherStationDataSource.weatherStationDataSourceId?has_content>
			  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){handleWeatherStationDataSourceDelete(${weatherStationDataSource.weatherStationDataSourceId});}">${i18nBundle.delete}</button>
			  </#if>
            </form>
        </div>
    </div>
</div>
</#macro>
<@page_html/>