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
	<title>${observationSite.observationSiteName!i18nBundle.newObservationSite}</title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
	<script type="text/javascript" src="/js/constants.js"></script>
        <script type="text/javascript" src="/js/validateForm.js"></script>
	<script type="text/javascript" src="/js/appleFruitMothObservationSiteMap.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			initMap([${(observationSite.gisId.gisGeom.x?c)!"0"},${(observationSite.gisId.gisGeom.y?c)!"0"}],${observationSite.observationSiteId}, ${currentSeason});
		});
	</script>
</#macro>
<#macro page_contents>
<h1>${observationSite.observationSiteName!i18nBundle.newObservationSite} ${currentSeason}</h1>
<button type="button" class="btn btn-default" onclick="window.location='/applefruitmoth';"><i class="fa fa-arrow-left" aria-hidden="true"></i> Tilbake til stasjonsliste</button>
<button type="button" class="btn btn-default" onclick="window.location='/applefruitmoth?action=registerCluster&observationSiteId=${observationSite.observationSiteId}&currentSeason=${currentSeason}';">Registrer klasetall</button>
<div class="singleBlockContainer">
	
	<div class="row">
		<div class="col-md-6">
			<#assign formId = "appleFruitMothStationForm">
			<#if messageKey?has_content>
				<div class="alert alert-success">${.now}: ${i18nBundle(messageKey)}</div>
			</#if>
                        <#if observationSiteSeasonCommonData??><div class="alert alert-info">${i18nBundle.lastUpdated}: ${observationSiteSeasonCommonData.lastUpdated}</div></#if>
			<form id="${formId}" role="form" action="/applefruitmoth?action=observationSiteSubmit" method="POST">
				<input type="hidden" name="observationSiteId" value="${observationSite.observationSiteId}"/>
				<div class="form-group">
				    <label for="observationSiteName">${i18nBundle.observationSiteName}</label>
				    <input type="text" class="form-control" name="observationSiteName" placeholder="${i18nBundle.observationSiteName}" value="${observationSite.observationSiteName!""}" onblur="validateField(this);" />
				    <span class="help-block" id="${formId}_observationSiteName_validation"></span>
				  </div>
				  <div class="form-group">
				    <label for="currentSeason">${i18nBundle.season}</label>
				    <select class="form-control" name="currentSeason" onchange="if(confirm('${i18nBundle.appleFruitMothChangeSeasonConfirm}')){window.location.href='/applefruitmoth?action=viewObservationSite&observationSiteId=${observationSite.observationSiteId}&currentSeason=' + this.options[this.options.selectedIndex].value;}">
					<#list 2010..lastSeason as season>
						<option value="${season}"<#if season == currentSeason> selected="selected"</#if>>${season}</option>
					</#list>
				     </select>
				     <span class="help-block" id="${formId}_currentSeason_validation"></span>
				  </div>
				  <div class="form-group">
					<label for="warningStatus">${i18nBundle.warningStatus}</label>
					<select class="form-control" name="warningStatus">
						<#list 0..4 as warningStatus>
							<#if warningStatus != 1>
							<option value="${warningStatus}"<#if observationSiteSeasonCommonData?has_content && observationSiteSeasonCommonData.warningStatus == warningStatus> selected="selected"</#if>>${i18nBundle["warningStatus_" + warningStatus]}</option>
							</#if>
						</#list>
					</select>
					<span class="help-block" id="${formId}_warningStatus_validation"></span>
				  </div>
				<div class="form-group">
				    <label for="degreeOfParasitation">${i18nBundle.degreeOfParasitation} (${i18nBundle.internal})</label>
				    <input type="number" step="any" class="form-control" name="degreeOfParasitation" placeholder="${i18nBundle.degreeOfParasitation}" value="<#if observationSiteSeasonCommonData??>${observationSiteSeasonCommonData.degreeOfParasitation?c!""}</#if>" onblur="validateField(this);" />
				    <span class="help-block" id="${formId}_degreeOfParasitation_validation"></span>
				</div>
				<div class="form-group">
				    <label for="thousandBerrySample">${i18nBundle.thousandBerrySample} (${i18nBundle.internal})</label>
				    <input type="number" step="any" class="form-control" name="thousandBerrySample" placeholder="${i18nBundle.thousandBerrySample}" value="<#if observationSiteSeasonCommonData??>${observationSiteSeasonCommonData.thousandBerrySample?c!""}</#if>" onblur="validateField(this);" />
				    <span class="help-block" id="${formId}_thousandBerrySample_validation"></span>
				</div>
                                <div class="form-group">
					<label for="remarks">${i18nBundle.remarks} (${i18nBundle.internal})</label>
					<textarea class="form-control" placeholder="${i18nBundle.remarks}" name="remarks" rows="5" onblur="validateField(this);"><#if observationSiteSeasonCommonData??>${observationSiteSeasonCommonData.remarks!""}</#if></textarea>
					<span class="help-block" id="${formId}_remarks_validation"></span>
				  </div>
				<button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
			</form>
		</div>
		<div class="col-md-6">
			<div id="appleFruitMothObservationSiteMap" class="map" style="height: 400px;">
				<div id="popover"></div>
			</div>
                        <h2>Beregninger/resultater</h2>
                        <table class="table table-striped">
                        <tbody>
                            <tr><td>Netto-klasetall</td><td>Høst: ${netClusterAutumnSum}, vår: ${netClusterSpringSum}</td></tr>
                            <tr><td>Antall trær registrert</td><td>Høst: ${treesWithAutumnValues}, vår: ${treesWithSpringValues}</td></tr>
                            <tr><td>Antall trær med økning i klasetall over vinteren</td><td>${treesWithIncreasingValues}</td></tr>
                            <tr><td>Antall trær med nedgang i klasetall over vinteren</td><td>${treesWithDecreasingValues}</td></tr>
                            <tr><td>Antall trær med uendret klasetall over vinteren</td><td>${treesWithUnchangedValues}</td></tr>
                            <tr><td>Angrepstall</td><td><#if attackNumber??>${attackNumber?c}<#else>[Data mangler]</#if></td></tr>
                            <tr><td>Nødvendig antall klaser ${currentSeason}</td><td>${neededClusters!"[Data mangler]"}</td></tr>
                            <tr><td>Prognosetall ${currentSeason}</td><td><#if forecastNumber??>${forecastNumber}<#else>[Data mangler]</#if></td></tr>
                            <tr><td>Klasebrøk</td><td><#if clusterFraction??>${clusterFraction?c}<#else>[Data mangler]</#if></td></tr>
                            
                        </tbody>
                        </table>
		</div>
	</div>
</div>	
</#macro>
<@page_html/>
