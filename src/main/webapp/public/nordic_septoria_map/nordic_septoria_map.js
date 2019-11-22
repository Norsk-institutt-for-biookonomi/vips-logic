/* 
 * Copyright (c) 2019 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */

/**
 * Self contained application for displaying several maps with risk assesments
 * @copyright 2019 NIBIO https://www.nibio.no/
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */

var hostName;
//var results;
//var localTimeZoneOffset = 120;
var todayAtMidnight; 
var views = ["observedDisease","yieldLoss","rainyDays","WHS","HM"];
var initialView = "WHS";
var featureOverlays = {};
for (var i in views)
{
    featureOverlays[views[i]] = {mainMap:null, subMap1: null, subMap2: null, subMap3: null, subMap4: null};
}
var maps = {mainMap:null, subMap1: null, subMap2: null, subMap3: null, subMap4: null};
var language = "en";

/**
 * After all libraries have been loaded (added to the containing page), this
 * function sets up all the maps and displays the initial view
 * @returns {undefined}
 */
var initMap = function ()
{
    todayAtMidnight = getTodayAtMidnight();
    var nordicSeptoriaMapContainer = document.getElementById("nordicSeptoriaMapContainer");
    // This is being used by dict, the translation tables
    language = nordicSeptoriaMapContainer.getAttribute("data-language") !== null ? nordicSeptoriaMapContainer.getAttribute("data-language") : language;
    // Giving the user the option to choose between the different views
    var viewRadioList = "";
    for (var i in views)
    {
        viewRadioList += "     <input type='radio' name='selectedlayer' " + (views[i] == initialView ? "checked " : "") + "value='" + views[i] + "' onclick='showLayer(this.value);'/> " + geti18nText(views[i]) + "<br/>"
    }
    // Model results should be calculated by VIPS from 2014 and forward
    var seasonList = "";
    var currentSeason = new Date().getFullYear();
    for(var season=2014;season<=currentSeason;season++)
    {
        seasonList += "     <option value='" + season + "'" + (season == currentSeason ? " selected='selected'" : "") + ">" + season + "</option>";
    }
    // This HTML is injected into the hosting web page. It contains all of the
    // maps and the controls
    nordicSeptoriaMapContainer.innerHTML = "<div id='mainMap'>"
                                    + " <div id='popupTooltip_mainMap' class='ol-popup'>"
                                    + "     <a href='#' class='ol-popup-closer' onclick='closeOverlay(this);'></a>"
                                    + "     <div></div>"
                                    + " </div>"
                                    + " <div class='dateField'>"
                                    + "     <div id='mainMapDateField'></div>" 
                                    + "     <div id='legend'>"
                                    + getLayerLegend[initialView]()
                                    + "     </div>"
                                    + " </div>"
                                    + " <div id='layersField'>"
                                    + viewRadioList
                                    + " </div>"
                                    + " <div id='seasonField'>"
                                    + " <select id='seasonList' name='season' onchange='changeSeason(this.options[this.options.selectedIndex].value);'>"
                                    + seasonList
                                    + " </select>"
                                    + " <input type='date' id='startDate' name='startDate' min='" + currentSeason + "-01-01' onchange='updateResults();'/> "
                                    + " <input type='date' id='endDate' name='endDate' max='" + currentSeason + "-12-31' onchange='updateResults();'/>"
                                    + " </div>"
                                    + " <div id='VIPSAttribution'>" + geti18nText("poweredBy") + " <a href='https://www.vips-landbruk.no/' target='new'><img id='VIPSLogo' src='" + hostName + "/public/nordic_septoria_map/logo_vips.png'/></a></div>"
                                    + "</div>"
                                    + "<div id='subMap1'>" 
                                    + " <div id='popupTooltip_subMap1' class='ol-popup'>"
                                    + "     <a href='#' class='ol-popup-closer' onclick='closeOverlay(this);'></a>"
                                    + "     <div></div>"
                                    + " </div>"
                                    + "<div id='subMap1DateField' class='dateField'></div></div>"
                                    + "<div id='subMap2'>" 
                                    + " <div id='popupTooltip_subMap2' class='ol-popup'>"
                                    + "     <a href='#' class='ol-popup-closer' onclick='closeOverlay(this);'></a>"
                                    + "     <div></div>"
                                    + " </div>"
                                    + "<div id='subMap2DateField' class='dateField'></div></div>"
                                    + "<div id='subMap3'>" 
                                    + " <div id='popupTooltip_subMap3' class='ol-popup'>"
                                    + "     <a href='#' class='ol-popup-closer' onclick='closeOverlay(this);'></a>"
                                    + "     <div></div>"
                                    + " </div>"
                                    + "<div id='subMap3DateField' class='dateField'></div></div>"
                                    + "<div id='subMap4'>" 
                                    + " <div id='popupTooltip_subMap4' class='ol-popup'>"
                                    + "     <a href='#' class='ol-popup-closer' onclick='closeOverlay(this);'></a>"
                                    + "     <div></div>"
                                    + " </div>"
                                    + "<div id='subMap4DateField' class='dateField'></div></div>"
                                    + "<div id='popup'></div>"
                                    + "</div>";
                                    
    // Initializing all the layers (one for each model result view) for all maps
    for (var i in views)
    {
        for(var mapName in featureOverlays[views[i]])
        {
            featureOverlays[views[i]][mapName] = new ol.layer.Vector({
                source: new ol.source.Vector({
                  features: new ol.Collection()
                }),
                style: getFeatureStyle[views[i]]
              });
        }
    }
    
    // Creating the 5 maps (present day + 4 days ahead)
    for(var mapName in maps)
    {
        var currentMap = currentMap;
        //console.info("mapName=" + mapName);
        maps[mapName] = new ol.Map({
            target: mapName,
            layers: [
              new ol.layer.Tile({
                source: new ol.source.OSM()
              })
            ],
            // This is the cartoon text bubble
            overlays: [new ol.Overlay({
                    element: document.getElementById('popupTooltip_' + mapName),
                    autoPan: true,
                    autoPanAnimation: {duration: 250}
            })],
            view: new ol.View({
              center: ol.proj.fromLonLat([15.41, 64.0]),
              zoom: mapName == "mainMap" ? 4 : 3
            })
          });
        // Adding all the layers (one per model/view) to the maps
        for (var i in views)
        {
            maps[mapName].addLayer(featureOverlays[views[i]][mapName]);
        }
        // When clicking on a feature - get the details and display
        maps[mapName].on('singleclick', function(evt) {
            var pixel = evt.map.getEventPixel(evt.originalEvent);
            var coordinate = evt.coordinate;
            displayFeatureDetails(evt.map,pixel, coordinate);
	});
    }
    showLayer(initialView);
};

var updateResults = function() {
  console.info("updateResults: TODO");  
};

/**
 * 
 * @returns {undefined}All features are removed. Used at when switching season
 */
var clearAll = function()
{
    for (var i in views)
    {
        for(var mapName in featureOverlays[views[i]])
        {
            featureOverlays[views[i]][mapName].getSource().clear();
        }
    }
}

/**
 * This function does what you think! It clears all current features
 * and gets/displays results for the new selected season
 */
var changeSeason = function(selectedSeason)
{
  // Clear layer(s)
  clearAll();
  // Set date field limits
  var startDate = document.getElementById("startDate").value;
  var endDate = document.getElementById("endDate").value;
  document.getElementById("startDate").value = 
          document.getElementById("startDate").value != "" ? 
            selectedSeason + document.getElementById("startDate").value.substring(4) : 
            "";
  document.getElementById("endDate").value = 
          document.getElementById("endDate").value != "" ? 
            selectedSeason + document.getElementById("endDate").value.substring(4) : 
            "";
  // Get results for this season
  getResults[getVisibleLayerName()](getCurrentSeason());
  
};

/**
 * Which season is currently in use? Checking the season select list
 */
var getCurrentSeason = function()
{
    return parseInt(document.getElementById('seasonList').options[document.getElementById('seasonList').options.selectedIndex].value);
}

/**
 * Collects features at the point clicked on the map. Displays the feature
 * properties (differs between views/models) in a cartoon text bubble
 */
var displayFeatureDetails = function(map, pixel, coordinate)
{
    var features = [];
    map.forEachFeatureAtPixel(pixel, function(feature){
            features.push(feature)
    });
    if(features.length === 0)
    {
        return;
    }
    var currentOverlay = map.getOverlays().item(0);
    var contentElm = currentOverlay.getElement().getElementsByTagName("div").item(0);
    // Depending on which model we display, features have their own view    
    contentElm.innerHTML=getFeatureDetails[getVisibleLayerName()](features);
    currentOverlay.setPosition(coordinate);
}

/**
 * Returns a text representation of the feature, including specific properties
 */
var getFeatureDetails = {
    "WHS": function(features) { return "TODO"; },
    "rainyDays": function(features) {
        var properties = features[0].getProperties();
        return "GS 32 estimated at " + properties.GS32Date.format("YYYY-MM-DD") + ", GS 71 estimated at " + properties.GS71Date.format("YYYY-MM-DD");
    }
};

/**
 * Closing the cartoon text bubble (clicked feature details)
 */
var closeOverlay = function(theCloser)
{
    var currentOverlay = maps[theCloser.parentNode.id.split("_")[1]].getOverlays().item(0);
    currentOverlay.setPosition(undefined);
    theCloser.blur();
    return false;
};

/**
 * Makes the specified layer visible on all maps. Fetches data if that hasn't 
 * already been done.
 * @param {String} layerName
 * @returns {void}
 */
function showLayer(layerName)
{
    //console.info("Attempting to show " + layerName);
    // Has this layer been initialized already?
    if(featureOverlays[layerName]["mainMap"].getSource().getFeatures().length == 0)
    {
         //console.info("Layer " + layerName + " is new, need to get data ");
         getResults[layerName](getCurrentSeason());
    }
    for(var mapName in maps)
    {
        for(var i in views)
        {
            featureOverlays[views[i]][mapName].setVisible(views[i]==layerName);
        }
    }
    document.getElementById("legend").innerHTML = getLayerLegend[layerName]();
}

/**
 * Which layer is currently visible? I can tell you, using my immense powers
 * of deduction!
 * @returns {String} The name of the currently visible layer
 */
var getVisibleLayerName = function()
{
    for(var i in views)
    {
        if(featureOverlays[views[i]]["mainMap"].getVisible())
        {
            return views[i];
        }
    }
};

/**
 * Contains layer specific methods for fetching and displaying the data
 */
var getResults = {
    /**
     * TODO: This is old school
     */
    "WHS" : function(season){
        ajax(hostName + "/rest/forecastresults/-1000", function(e){
        //ajax("http://vipslogic-local.no/rest/forecastresults/-1000", function(e){
            var results = JSON.parse(e.target.responseText);
            //console.info(todayAtMidnight);
            var currentDay = todayAtMidnight;
            for(var mapName in maps)
            {
                //console.info("currentDay=" + currentDay.format());
                displayResults["WHS"](results,mapName, currentDay);
                //currentDay.setDate(currentDay.getDate()+1);
                currentDay.add(1,"days");
            }
            // This is here to fix an apparent bug in having Vector tiles
            // within the CSS grid system
            // THE FIRST LAYER THAT IS BEING SHOWN ON THE MAP MUST DO THIS. 
            // AT LEAST ONCE.
            window.dispatchEvent(new Event('resize'));
        });
    },
    "observedDisease": function() { console.info("NOT IMPLEMENTED");},
    "yieldLoss": function() { console.info("NOT IMPLEMENTED");},
    "rainyDays": function(season) { 
        // Retrieve the forecast ids first
        ajax(hostName + "/rest/forecastconfigurations/model/RAINYDAYSM/" + season, function(e){
            forecastsForSeason = JSON.parse(e.target.responseText);
            // For each forecast config, get the results for a given season 
            // and aggregate for a given period (GS32-GS71 or user selected)
            for(var i in forecastsForSeason)
            {
                ajax(hostName + "/rest/forecastresults/" + forecastsForSeason[i].forecastConfigurationId + "/" + season + "-01-01/" + season + "-12-31", function(e){
                    var forecastResults = JSON.parse(e.target.responseText);
                    //console.info(forecastResults);
                    var currentForecastId = forecastResults[0].forecastConfigurationId;
                    var currentForecast = null;
                    for(var j in forecastsForSeason)
                    {
                        if(forecastsForSeason[j].forecastConfigurationId == currentForecastId)
                        {
                            currentForecast = forecastsForSeason[j];
                            break;
                        }
                    }
                    ajax(hostName + "/rest/gs/date/32,71/25/?season=" + season + "&location=" + currentForecast.locationPointOfInterestId.longitude + "," + currentForecast.locationPointOfInterestId.latitude, function(e){
                        // This callback interprets the data returned from 
                        // the VIPSLogic GrowthStageService
                        // It then forwards results and GS dates to displayResults
                        var GSResults = JSON.parse(e.target.responseText);
                        var GS32Date, GS71Date;
                        
                        for(var i in GSResults)
                        {
                            if(GSResults[i]["32"] != null)
                            {
                                GS32Date = moment(GSResults[i]["32"]);
                            }
                            if(GSResults[i]["71"] != null)
                            {
                                GS71Date = moment(GSResults[i]["71"]);
                            }
                        }
                        
                        var startDate = document.getElementById("startDate").value != "" ? moment(document.getElementById("startDate").value) : GS32Date;
                        var endDate = document.getElementById("endDate").value != "" ? moment(document.getElementById("endDate").value) : GS71Date;
                        //console.info("startDate=" + startDate.format("YYYY-MM-DD"));
                        // We now have everything we need to calculate the sum
                        var rainyDaysSum = 0;
                        for(var i in forecastResults)
                        {
                            var validTimeStart = moment(forecastResults[i].validTimeStart);
                            if(
                                    validTimeStart.isSameOrAfter(startDate) 
                                    && validTimeStart.isSameOrBefore(endDate) 
                                    && forecastResults[i].allValues["RAINYDAYSM.RAINY_DAY"] == "true"
                            )
                            {
                                rainyDaysSum++;
                            }
                        }
                        // We have the sum and location, let's display it on the map!
                        // THIS IS THE LAST STEP IN THIS CHAIN!
                        displayResults["rainyDays"](rainyDaysSum, GS32Date, GS71Date, currentForecast);
                    });
                });
            }
        });
    },
    "HM": function() { console.info("NOT IMPLEMENTED");},
};

var featureZIndex = 10;
// This is used by OpenLayers to define the features' looks
// The different models have varying thresholds, colors etc
var getFeatureStyle = {
    "WHS": function(feature)
    {
        var WHS = parseInt(feature.get("WHS"));
        var color = WHS === 0 ? "#777777" 
                    : WHS < 20 ? "yellow"
                    : WHS < 40 ? "orange"
                    : "red";

        return [
                new ol.style.Style({
                    image: new ol.style.Circle({
                        fill: new ol.style.Fill({ color: color }),
                        stroke: new ol.style.Stroke({ color: [0,0,0,0], width: 0 }),
                        radius: 11
                    }),
                    text: new ol.style.Text({
                        text: feature.get("WHS"),
                        font: 'bold 12px sans-serif',
                        fill: new ol.style.Fill({ color: "white" }),
                        stroke: new ol.style.Stroke({ color: [90,90,90,1], width: 1 })
                    }),
                    zIndex: featureZIndex++
                })
        ];
    },
    "rainyDays": function(feature)
    {
        var rainyDays = parseInt(feature.get("rainyDays"));
        var color = rainyDays < 4 ? "green" : "red";
        
        return [
                new ol.style.Style({
                    image: new ol.style.Circle({
                        fill: new ol.style.Fill({ color: color }),
                        stroke: new ol.style.Stroke({ color: [0,0,0,0], width: 0 }),
                        radius: 11
                    }),
                    text: new ol.style.Text({
                        text: feature.get("rainyDays"),
                        font: 'bold 12px sans-serif',
                        fill: new ol.style.Fill({ color: "white" }),
                        stroke: new ol.style.Stroke({ color: [90,90,90,1], width: 1 })
                    }),
                    zIndex: featureZIndex++
                })
        ];
    }
};

/**
 * 
 * Creating OpenLayers features from the model results from VIPSLogiv and 
 * displaying them on the map
 */
var displayResults = {
    "WHS" : function(results,mapName, date){
        var features = [];
        //var momentDate = moment(date);
        for(var i in results)
        {
            if(moment(results[i].validTimeStart).isSame(date)){
                //console.info(results[i].validGeometry.coordinates);
                var feature = new ol.Feature({
                    geometry:new ol.geom.Point(ol.proj.fromLonLat(results[i].validGeometry.coordinates)),
                    WHS: results[i].allValues["GRIDZYMOSE.WHS"]
                });
                //console.info(feature.getGeometry());
                features.push(feature);
            }
        }
        featureOverlays["WHS"][mapName].getSource().clear();
        featureOverlays["WHS"][mapName].getSource().addFeatures(features);
        
        //console.info(featureOverlays["WHS"].mainMap.getSource().getFeatures());
        // Setting the results date
        document.getElementById(mapName + "DateField").innerHTML = date.format("YYYY-MM-DD");//date.getFullYear() + "-" + ("0" + (date.getMonth()+1)).slice(-2) + "-" + ("0" + date.getDate()).slice(-2);
    },
    // This may be called several times, make sure we don't remove features from 
    // the layer
    "rainyDays" : function(rainyDaysSum, GS32Date, GS71Date, currentForecast){
        var feature = new ol.Feature({
                    geometry:new ol.geom.Point(ol.proj.fromLonLat([currentForecast.locationPointOfInterestId.longitude,currentForecast.locationPointOfInterestId.latitude])),
                    rainyDays: rainyDaysSum.toString(),
                    GS32Date: GS32Date,
                    GS71Date: GS71Date
                });
        var features = [];
        features.push(feature);
        featureOverlays["rainyDays"]["mainMap"].getSource().addFeatures(features);
        
    }
};

/**
 * 
 * @param {type} url
 * @param {type} callback
 * @returns {undefined}General XMLHttpRequest utility function. To avoid using JQuery or similarly
 * bloated framework
 */
var ajax = function(url, callback)
{
    var xhr = new XMLHttpRequest();
    xhr.open("GET", url);
    xhr.onload = callback;
    xhr.send();
};

/**
 * Simple popup with detailed description of each model
 */
var showModelInfo = {
    "WHS": function()
    {
        ajax(hostName + "/rest/model/GRIDZYMOSE", function(e){
            var modelInfo = JSON.parse(e.target.responseText);
            document.getElementById('popup').innerHTML= "<p><button type='button' onclick='hideModelInfo();'>" + geti18nText("close") + "</button></p>" 
                    + refHumidityModelMapExplanation;
                    //+ "<h3>Background</h3>"
                    //+ modelInfo.defaultDescription.replace("\n","<br/><br/>");
            document.getElementById('popup').style.display="block";

        });
    },
    "rainyDays": function(){
        document.getElementById('popup').innerHTML= "<p><button type='button' onclick='hideModelInfo();'>" + geti18nText("close") + "</button></p>" 
                    + "TODO";
                    //+ "<h3>Background</h3>"
                    //+ modelInfo.defaultDescription.replace("\n","<br/><br/>");
            document.getElementById('popup').style.display="block";
    }
};

var hideModelInfo = function()
{
    document.getElementById('popup').style.display="none";
}

/**
 * TODO: this is using Europe/Oslo as default. What about Finland/the Baltics?
 * @returns {getTodayAtMidnight.today}
 */
function getTodayAtMidnight()
{
    var timeZone="Europe/Oslo";
    var today = moment().tz(timeZone)
    today.set("hour",0);
    today.set("minute",0);
    today.set("second",0);
    today.set("millisecond",0);
    return today;
}

var getLayerLegend = {
    "WHS": function(){
        return "         <table>"
            + "             <tr><td>0</td><td style='color: #777777; font-weight: bold;'>" + geti18nText("risk_no") + "</td></tr>"
            + "             <tr><td>0-20</td><td style='color: yellow; background-color: #777777; font-weight: bold;'>" + geti18nText("risk_low") + "</td></tr>"
            + "             <tr><td>20-40</td><td style='color: orange; font-weight: bold; '>" + geti18nText("risk_medium") + "</td></tr>"
            + "             <tr><td>" + geti18nText("over") + " 40</td><td style='color: red; font-weight: bold;'>" + geti18nText("risk_high") + "</td></tr>"
            + "             <tr><td><span style='cursor:pointer; color: green;' onclick='showModelInfo[\"WHS\"]();'><i class='fas fa-info-circle'></i></span></td><td></td></tr>"
            + "         </table>";
    },
    "rainyDays": function(){
        return "         <table>"
            + "             <tr><td>&lt; 4</td><td style='color: #777777; font-weight: bold;'>" + geti18nText("risk_no") + "</td></tr>"
            + "             <tr><td>&gt;= 4</td><td style='color: red; background-color: #777777; font-weight: bold;'>" + geti18nText("risk_high") + "</td></tr>"
            + "             <tr><td><span style='cursor:pointer; color: green;' onclick='showModelInfo[\"rainyDays\"]();'><i class='fas fa-info-circle'></i></span></td><td></td></tr>"
            + "         </table>";
    }
};


// ###########################################################################
// All the stuff below is for dynamically loading all JavaScript Libs that are
// needed to run the application

// After the client document has finished loading, we download OpenLayers, MomentJS and subsequently
// initialize the map.
document.addEventListener("DOMContentLoaded", function() {
  // Some introspection here
  var me = document.getElementById("nordicSeptoriaMapScript");
  if(me == null || me == undefined)
  {
      me = document.currentScript;
      if(me == null || me == undefined)
      {
          failGracefully();
          return;
      }
  }
  hostName = me.src.substring(0,me.src.indexOf("/public"));
  // Creating a chain here: First we load ol.css, then nordic_septoria_map.css, 
  // ol.js and finally moment.min.js. Then we initialize the Map
  loadHeadElement("link", 
    {
        href: hostName + "/public/nordic_septoria_map/ol.css",
        rel: "stylesheet",
        media: "screen"
    }, 
    function() {
        loadHeadElement("link", 
            {
                href: hostName + "/public/nordic_septoria_map/nordic_septoria_map.css",
                rel: "stylesheet",
                media: "screen"
            },
            function(){ 
                loadHeadElement("script", 
                    { 
                        src: hostName + "/public/nordic_septoria_map/ol-debug.js" 
                    }, 
                    function() {
                        loadHeadElement("script", 
                            { 
                                src: hostName + "/public/nordic_septoria_map/moment.min.js" 
                            }, 
                            function() {
                                loadHeadElement("script", 
                                    { 
                                        src: hostName + "/public/nordic_septoria_map/moment-timezone-with-data.min.js" 
                                    }, 
                                    function() {
                                        loadHeadElement("script", 
                                            { 
                                                src: "https://kit.fontawesome.com/567c8b1eb5.js" 
                                            }, 
                                            initMap
                                        ); 
                                    }
                                ); 
                            }
                        ); 
                    }
                );
            }
        );
    }
    );
});
/**
 * Adds a script to the HTML, performs an action AFTER it's been confirmed
 * loaded (which normally happens asynchronously) and ready
 * @param {String} url
 * @param {function} callback
 */
var loadScript = function(url, callback)
{
    var script = document.createElement("script"); // Make a script DOM node
    script.src = url; // Set it's src to the provided URL
    
    script.onreadystatechange = callback;
    script.onload = callback;
    console.info(script);
    
    document.head.appendChild(script);
};

/**
 * Loads elements (e.g. script elements) into the parent page
 * @param {type} type the type of element
 * @param {type} attributes the attributes (like src) of the element
 * @param {type} callback What do do after the loading is done
 */
var loadHeadElement = function(type, attributes, callback)
{
    var elm = document.createElement(type);
    
    for(var attribute in attributes)
    {
        if(attributes.hasOwnProperty(attribute))
        {
            elm.setAttribute(attribute, attributes[attribute]);
        }
    }
    
    elm.onreadystatechange = callback;
    elm.onload = callback;
    
    document.head.appendChild(elm);
};


var failGracefully = function() {
    alert("ERROR: Your current browser does not support the map application.");
};


var dict = {
    "risk_no"           : {     "no": "Ingen risiko",           "dk": "Ingen risiko",           "en": "No risk"},
    "risk_low"          : {     "no": "Lav risiko",             "dk": "Lav risiko",             "en": "Low risk"},
    "risk_medium"       : {     "no": "Middels risiko",         "dk": "Middel risiko",          "en": "Medium risk"},
    "risk_high"         : {     "no": "Høy risiko",             "dk": "Høj risiko",             "en": "High risk"},
    "over"              : {     "no": "Over",                   "dk": "Over",                   "en": "Over"},
    
    "poweredBy"         : {     "no": "Drevet av",              "dk": "Drevet af",              "en": "Powered by"},
    "close"             : {     "no": "Lukk",                   "dk": "Lukk",                   "en": "Close"},
    "observedDisease"   : {     "no": "Observert sykdom",       "dk": "Observeret sykdom",      "en": "Observed disease"},
    "yieldLoss"         : {     "no": "Avlingstap",             "dk": "Tap af grøde",           "en": "Yield loss"},
    "rainyDays"         : {     "no": "Dager med regn",         "dk": "Dager med regn",         "en": "Rainy days"},
    "WHS"               : {     "no": "Referansefuktmodellen",  "dk": "Referansefuktmodellen",  "en": "Reference humidity model"},
    "HM"                : {     "no": "Fuktmodellen",           "dk": "Fuktighetsmodellen",     "en": "Humidity model"}
};

var geti18nText = function(keyword)
{
    return dict[keyword][language];
};

var refHumidityModelMapExplanation = "<p>The map shows the risk for new infections of Septoria SPP. in wheat. The reference humidity model calculates the total number of humid hours as a running sum of 36 hours (Yesterday + today + tomorrow). 0 = no risk; 1-20= low risk; 21-40 is medium risk and >40 is high risk. A humid hour is calculated if: Rh>=88% or, Leaf wetness >=30 minutes per hour or rain >=0,2mm. Only hours where the temperature >=8°C </p>"
                                     + "<p>This risk index was developed as a supplement to the Humidity model. It should be considered as a weather based submodel for Septoria risk, easy to show on maps (risk across regions) as well as on a graph (curve) with risk across time. </p>";