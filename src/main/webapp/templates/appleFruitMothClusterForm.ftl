<#--
  Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
  
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
	<title>Registrer klasetall for ${observationSite.observationSiteName} sesongen ${currentSeason}</title>
</#macro>
<#macro page_contents>
   <h2>Registrer klasetall for ${observationSite.observationSiteName} sesongen ${currentSeason}</h2>
   <#if messageKey?has_content>
            <div class="alert alert-success">${.now}: ${i18nBundle(messageKey)}</div>
    </#if>
   <div class="singleBlockContainer">
	
	<div class="row">
		<div class="col-md-8">
                    <button type="button" class="btn btn-default" onclick="window.location='/applefruitmoth';"><i class="fa fa-arrow-left" aria-hidden="true"></i> Tilbake</button>
                    <p>
                    Man trenger ikke registrere informasjon for alle trær (A-T) i 
                    lista, men har man lagt inn en verdi for et tre må alle felt for 
                    det treet være utfylt.
                    </p>
                    <#assign alphabet = ["A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T"]>
                    <form role="form" action="/applefruitmoth?action=clusterSubmit" method="POST" onsubmit="return validateForm(this)">
                    <input type="hidden" name="observationSiteId" value="${observationSite.observationSiteId}" />
                    <button type="submit" class="btn btn-default">Lagre</button>
                    <div class="form-group">
                        <label for="currentSeason">${i18nBundle.season}</label>
                        <select class="form-control" name="currentSeason" onchange="if(confirm('${i18nBundle.appleFruitMothChangeSeasonConfirm}')){window.location.href='/applefruitmoth?action=registerCluster&observationSiteId=${observationSite.observationSiteId}&currentSeason=' + this.options[this.options.selectedIndex].value;}">
                            <#list 2010..lastSeason as season>
                                    <option value="${season}"<#if season == currentSeason> selected="selected"</#if>>${season}</option>
                            </#list>
                         </select>
                    </div>
                    
                    
                    <table class="table table-striped">
                        <thead>
                            <tr>
                                <th>Tre</th>
                                <th>Høsttall</th>
                                <th>Høstdato (${currentSeason-1})</th>
                                <th>Vårtall</th>
                                <th>Vårdato (${currentSeason})</th>
                            </tr>
                        </thead>
                        <tbody>
                        <#list alphabet as stationName>
                            <tr>
                                 <td>${stationName}</td>
                                 <td><input type="number" class="form-control" name="autumnValue_${stationName}" value="<#if treeData[stationName]?has_content>${treeData[stationName].autumnValue!"0"}</#if>"/></td>
                                 <td><input type="date" class="form-control" name="autumnDate_${stationName}" value="<#if treeData[stationName]?has_content>${treeData[stationName].autumnDate!""}<#else>${currentSeason-1}-09-01</#if>"/></td>
                                 <td><input type="number" class="form-control" name="springValue_${stationName}" value="<#if treeData[stationName]?has_content>${treeData[stationName].springValue!"0"}</#if>"/></td>
                                 <td><input type="date" class="form-control" name="springDate_${stationName}" value="<#if treeData[stationName]?has_content>${treeData[stationName].springDate!""}<#else>${currentSeason}-04-01</#if>"/></td>
                            </tr>
                        </#list>
                        </tbody>
                    </table>
                    <button type="submit" class="btn btn-default">Lagre</button>
                    </form>
                </div>
                <div class="col-md-4">
			<div id="appleFruitMothObservationSiteMap" class="map" style="height: 400px;">
				<div id="popover"></div>
			</div>
		</div>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/>
</#macro>
<#macro custom_js>
	<script src="/js/resourcebundle.js"></script>
	<script type="text/javascript" src="/js/3rdparty/ol.js"></script>
	<script type="text/javascript" src="/js/constants.js"></script>
	<script type="text/javascript" src="/js/appleFruitMothObservationSiteMap.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			initMap([${(observationSite.gisId.gisGeom.x?c)!"0"},${(observationSite.gisId.gisGeom.y?c)!"0"}],${observationSite.observationSiteId}, ${currentSeason});
		});
                    
                /**
                 * For now: Check that dates are in the correct year
                 */
                var currentSeason = ${currentSeason};
                
                function validateForm(theForm){
                    var isFormValid = true;
                    try
                    {
                        var inputs = theForm.getElementsByTagName("input");
                        
                        var errorMessage = "";

                            // First run through: Check for date validity and index trees that have a count
                            var countedTrees = [];
                            for(var i=0;i<inputs.length;i++)
                            {
                                if((inputs[i].name.indexOf("autumnValue") == 0 && inputs[i].value.length > 0)
                                    ||(inputs[i].name.indexOf("springValue") == 0 && inputs[i].value.length > 0))
                                {
                                    countedTrees.push(inputs[i].name.split("_")[1]);
                                }
                                if(
                                    (inputs[i].name.indexOf("autumnDate") == 0 && inputs[i].value.length > 0 && inputs[i].value.substr(0,4) != currentSeason -1)
                                    || (inputs[i].name.indexOf("springDate") == 0 && inputs[i].value.length > 0 && inputs[i].value.substr(0,4) != currentSeason)
                                    )
                                {
                                    $(inputs[i].parentNode).addClass("has-error");
                                    isFormValid = false;
                                    errorMessage = "Feil med datoer. Sjekk før du lagrer igjen."
                                }
                                else{
                                    $(inputs[i].parentNode).removeClass("has-error");
                                }
                                //console.info(inputs[i].name + ": " + inputs[i].value.substr(0,4));
                            }

                        // Reducing array to contain only unique values
                        // This is from EcmaScript 6, and a shorthand for Array.from();
                        countedTrees = [...new Set(countedTrees)];

                        // Checking validity of each row with tree counts
                        var fieldNames = ["autumnValue","autumnDate", "springValue", "springDate"];
                        for(var i=0;i<countedTrees.length;i++)
                        {
                            // If counted, all fields must contain values
                            var treeName=countedTrees[i];
                            var fieldsWithValue = 0;
                            for(var j=0;j<fieldNames.length;j++)
                            {
                                    if(theForm[fieldNames[j] + "_" + treeName].value.length == 0)
                                    {
                                        $(theForm[fieldNames[j] + "_" + treeName].parentNode).addClass("has-error");
                                        isFormValid = false;
                                        errorMessage = "Alle felt i en rad må fylles ut. Sjekk før du lagrer igjen."
                                    }
                                    else
                                    {
                                        $(theForm[fieldNames[j] + "_" + treeName].parentNode).removeClass("has-error");
                                    }
                            }
                        }

                        if(!isFormValid)
                        {
                                alert(errorMessage);
                        }
                    } catch(err)
                    {
                        console.info(err.message);
                    }
                    return isFormValid;
                    //return false; // For testing only
                }
	</script>
</#macro>
<@page_html/>
