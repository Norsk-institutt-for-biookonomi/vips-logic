/* 
 * Copyright (c) 2018 NIBIO <http://www.nibio.no/>. 
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

var hostName;
var results;
//var localTimeZoneOffset = 120;
var todayAtMidnight = getTodayAtMidnight();
var featureOverlays = {mainMap:null, subMap1: null, subMap2: null, subMap3: null, subMap4: null}; 
var maps = {mainMap:null, subMap1: null, subMap2: null, subMap3: null, subMap4: null};

var initMap = function ()
{
    var zymoGridMapContainer = document.getElementById("zymoGridMapContainer");
    zymoGridMapContainer.innerHTML = "<div id='mainMap'><div id='mainMapDateField' class='dateField'></div><div id='VIPSAttribution'>Powered by <a href='https://www.vips-landbruk.no/' target='new'><img id='VIPSLogo' src='" + hostName + "/public/nordic_septoria_whs/logo_vips.png'/></a></div></div>"
                                    + "<div id='subMap1'><div id='subMap1DateField' class='dateField'></div></div>"
                                    + "<div id='subMap2'><div id='subMap2DateField' class='dateField'></div></div>"
                                    + "<div id='subMap3'><div id='subMap3DateField' class='dateField'></div></div>"
                                    + "<div id='subMap4'><div id='subMap4DateField' class='dateField'></div></div>";
    
    for(var mapName in featureOverlays)
    {
        featureOverlays[mapName] = new ol.layer.Vector({
            source: new ol.source.Vector({
              features: new ol.Collection()
            }),
            style: getFeatureStyle
          });
    }
    
    for(var mapName in maps)
    {
        maps[mapName] = new ol.Map({
            target: mapName,
            layers: [
              new ol.layer.Tile({
                source: new ol.source.OSM()
              }),
              featureOverlays[mapName]
            ],
            view: new ol.View({
              center: ol.proj.fromLonLat([15.41, 64.0]),
              zoom: mapName == "mainMap" ? 4 : 3
            })
          });
    }
    ajax(hostName + "/rest/forecastresults/-1000", function(e){
    //ajax("http://vipslogic-local.no/rest/forecastresults/-1000", function(e){
        results = JSON.parse(e.target.responseText);
        var currentDay = todayAtMidnight;
        for(var mapName in maps)
        {
            displayResults(mapName, currentDay);
            currentDay.setDate(currentDay.getDate()+1);
        }
        // This is here to fix an apparent bug in having Vector tiles
        // within the CSS grid system
        window.dispatchEvent(new Event('resize'));
    });
};

var featureZIndex = 10;

var getFeatureStyle = function(feature)
{
    return [
            new ol.style.Style({
                image: new ol.style.Circle({
                    fill: new ol.style.Fill({ color: [parseInt(feature.get("WHS")) * 255/72, 255 - parseInt(feature.get("WHS")) * 255/72,0,1] }),
                    stroke: new ol.style.Stroke({ color: [0,0,0,1], width: 3, lineDash: [2,2] }),
                    radius: 20
                }),
                text: new ol.style.Text({
                    text: feature.get("WHS"),
                    font: 'bold 15px Times New Roman',
                }),
                zIndex: featureZIndex++
            })
    ];
}

var displayResults = function(mapName, date){
    var features = [];
    for(var i in results)
    {
        //console.info(parseJSONDate(results[i].validTimeStart).getTime() + "==" + date.getTime());
        if(parseJSONDate(results[i].validTimeStart).getTime() == date.getTime()){
            //console.info(results[i].validGeometry.coordinates);
            var feature = new ol.Feature({
                geometry:new ol.geom.Point(ol.proj.fromLonLat(results[i].validGeometry.coordinates)),
                WHS: results[i].allValues["GRIDZYMOSE.WHS"]
            });
            //console.info(feature.getGeometry());
            features.push(feature);
        }
    }
    //var featureSource = new ol.source.Vector({features:features});
    //console.info(features);
    featureOverlays[mapName].getSource().clear();
    featureOverlays[mapName].getSource().addFeatures(features);
    //console.info(featureOverlays.mainMap.getSource().getFeatures());
    // Setting the results date
    document.getElementById(mapName + "DateField").innerHTML = date.getFullYear() + "-" + ("0" + (date.getMonth()+1)).slice(-2) + "-" + ("0" + date.getDate()).slice(-2);
};

var ajax = function(url, callback)
{
    var xhr = new XMLHttpRequest();
    xhr.open("GET", url);
    xhr.onload = callback;
    xhr.send();
};

function parseJSONDate(theDateString)
{
    // This actually does the trick pretty well
    return new Date(theDateString);
}

function getTodayAtMidnight()
{
    var retVal = new Date();
    retVal.setHours(0);
    retVal.setMinutes(0);
    retVal.setSeconds(0);
    retVal.setMilliseconds(0);
    
    //return retVal;
    // OR RETURN A FIXED DATE FOR TESTING
     return new Date("2017-09-09T22:00:00.000+0000");
}


// ###########################################################################
// All the stuff below is for dynamically loading all JavaScript Libs that are
// needed to run the application

// After the client document has finished loading, we download OpenLayers and subsequently
// initialize the map.
document.addEventListener("DOMContentLoaded", function() {
  // Some introspection here
  var me = document.getElementById("zymoGridMapScript");
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
  // Creating a chain here: First we load ol.css, then nordic_septoria_whs.css 
  // and finally ol.js. Then we initialize the Map
  loadHeadElement("link", 
    {
        href: hostName + "/public/nordic_septoria_whs/ol.css",
        rel: "stylesheet",
        media: "screen"
    }, 
    function() {
        loadHeadElement("link", 
            {
                href: hostName + "/public/nordic_septoria_whs/nordic_septoria_whs.css",
                rel: "stylesheet",
                media: "screen"
            },
            function(){ loadHeadElement("script", { src: hostName + "/public/nordic_septoria_whs/ol.js" }, initMap); }
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

