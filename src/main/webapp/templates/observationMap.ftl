<#-- 
  Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
  
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
<#setting time_zone=user.organizationId.defaultTimeZone!"UTC">
<#macro page_head>
        <title>${i18nBundle.observationMap}</title>
</#macro>
<#macro custom_css>
	<link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css"/>
        <link rel="stylesheet" type="text/css" href="/css/3rdparty/chosen.min.css"/>
        <style type="text/css">
            #progressBar{
                position: absolute;
                bottom: 5px;
                left: 10px;
                z-index: 1000;
                background-color: white;
                border-radius: 10px;
                padding: 10px;
            }
            #legend{
                position: absolute;
                bottom: 115px;
                left: 10px;
                z-index: 1000;
                background-color: white;
                border-radius: 10px;
                padding: 10px;
            }
            #legend ul {
                list-style: none;
                padding:0;
                margin:0;
            }
        </style>
</#macro>
<#macro custom_js>
        <script type="text/javascript" src="/js/3rdparty/ol.js"></script>
        <script type="text/javascript" src="/js/observationMap.js"></script>
        <script type="text/javascript" src="/js/3rdparty/moment.min.js"></script>
        <script type="text/javascript" src="/js/3rdparty/chosen.jquery.min.js"></script>
        <script type="text/javascript" src="/js/constants.js"></script>
        <script type="text/javascript" src="/js/util.js"></script>
        <script type="text/javascript" src="/js/environment.js"></script>
        <script type="text/javascript">
                var colors = [
                                [7, 'rgba(255, 0, 0, 1.0)'], // Red
                                [30, 'rgba(244, 206, 66, 1.0)'], // Orange
                                [60, 'rgba(0, 0, 255, 1.0)'], // Blue
                            ];
		$(document).ready(function() {
			initMap(
                            [${(defaultMapCenter.x?c)!"0"}, ${(defaultMapCenter.y?c)!"0"}],
                            ${defaultMapZoom!"1"},
                            ${organizationId!user.organizationId.organizationId},
                            "${from?date}",
                            "${to?date}",
                            ${pestId!"null"},
                            ${cropId!"null"},
                            ${cropCategoryId!"null"},
                            colors
                        );
                        
                        initMapLegend();
                        
                        initForm(${organizationId!user.organizationId.organizationId},
                            ${pestId!"null"},
                            ${cropId!"null"},
                            ${cropCategoryId!"null"},
                            function() {$(".chosen-select").chosen({allow_single_deselect: true});} // Must do this after select lists have been populated
                        );
                        
                        
                        
		});
                    
                    
                function initMapLegend()
                {
                    var lBox = document.getElementById("legend");
                    var html = "<ul>";
                    for(var i in colors)
                    {
                            html += '<li><i style="color: ' + colors[i][1] + ';" class="fa fa-square" aria-hidden="true"></i> ' + (i > 0 ? colors[i-1][0] + 1 : '0') + '-' + colors[i][0] + ' ${i18nBundle.days?lower_case}</li>';
                    }
                        html += '<li><i style="color: black;" class="fa fa-square" aria-hidden="true"></i> ${i18nBundle.older}</li>';
                    html += "</ul>";
                    lBox.innerHTML = html;
                }
                    
                var from = moment(document.getElementById("dateFrom").value);
                
                function updateCurrentDate(rangeBar){
                    currentDate = from.clone();
                    currentDate.add(parseInt(rangeBar.value)-1,"days");
                    document.getElementById("dayInPeriodDate").innerHTML=currentDate.format('YYYY-MM-DD');    
                    currentDateInMillis = currentDate.format("X") * 1000;
                    featureOverlay.getSource().changed();
                    //console.info(currentDate);
                }
                    
                function updateMap(rangeBar){
                    currentDate = from.clone();
                    currentDate.add(parseInt(rangeBar.value)-1,"days");
                    currentDateInMillis = currentDate.format("X") * 1000;
                    featureOverlay.getSource().changed();
                    /*featureOverlay.getSource().clear();
                    featureOverlay.getSource().addFeatures(drawnfeatures);*/
                }
                
                var moveCurrentDayForward = function(){
                    theRange = document.getElementById("dayInPeriod");
                    if(parseInt(theRange.value) < theRange.max)
                    {
                        theRange.value = parseInt(theRange.value) +1;
                        theRange.oninput();
                    }
                    else{
                        togglePlay(document.getElementById("playButton"));
                    }
                }
                var intervalId = null;
                function togglePlay(theButton){
                    if(intervalId == null)
                    {
                        intervalId = setInterval(moveCurrentDayForward, 250);
                        theButton.innerHTML = '<i class="fa fa-pause" aria-hidden="true"></i>';
                    }
                    else
                    {
                        clearInterval(intervalId);
                        intervalId = null;
                        theButton.innerHTML = '<i class="fa fa-play" aria-hidden="true"></i>';
                    }
                }
        </script>
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
        <h1>${i18nBundle.observationMap}</h1>
        
        <#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <div id="observationMap" class="map" style="position:relative;">
            <div id="popover"></div>
            <div class="form-group" id="progressBar">
                <label for="dayInPeriod">${i18nBundle.currentDate}: <span id="dayInPeriodDate">${to?date}</span></label><br/>
                <button type="button" id="playButton" class="btn" onclick="togglePlay(this);"><i class="fa fa-play" aria-hidden="true"></i></button>
                <input type="range" id="dayInPeriod" name="dayInPeriod" min="1" max="${periodDays}" step="1" value="${periodDays}" oninput="updateCurrentDate(this);" onchange="updateMap(this)"/>
                
            </div>
            <div id="legend"></div>
        </div>
        <form class="form-inline" method="get" action="/observation/map">
            <div class="form-group">
                <input class="form-control" type="date" id="dateFrom" name="from" value="${from?date}"/>
            </div>
            -
            <div class="form-group">
                <input class="form-control" type="date" id="dateTo" name="to" value="${to?date}"/>
            </div>
            <div class="form-group">
                    <select name="pestId" id="pestList" class="form-control chosen-select" data-placeholder="${i18nBundle.pestOrganismId}">
                    </select>
            </div>
            <div class="form-group">
                    <select name="cropId" id="cropList" class="form-control chosen-select" data-placeholder="${i18nBundle.cropOrganismId}">	
                    </select>
            </div>
            <div class="form-group">
                    <select name="cropCategoryId" id="cropCategoryList" class="form-control chosen-select" data-placeholder="${i18nBundle.cropCategoryIds}">	
                    </select>
            </div>
            <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
        </form>
        <div class="table-responsive">
            <table class="table table-striped" id="observationTable">
                    <thead>
                            <th>${i18nBundle.timeOfObservation}</th>
                            <th>${i18nBundle.organism}</th>
                            <th>${i18nBundle.cropOrganismId}</th>
                            <th>${i18nBundle.location}</th>
                            <th>${i18nBundle.observer}</th>
                            <th>${i18nBundle.heading}</th>
                            <th></th>
                    </thead>
                    <tbody id="observationTableBody">
                    </tbody>
            </table>
        </div>
</div>
</#macro>
<@page_html/>
