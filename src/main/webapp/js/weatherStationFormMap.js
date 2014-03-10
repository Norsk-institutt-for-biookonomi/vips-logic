/* 
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

/*
 * KML layer map with weather station information
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */

// Keeping the map and station marker globally available
var map;
var stationMarker;

/**
 * 
 * @param {ol.Coordinate} center - coordinates for the map's center (WGS84)
 * @param {int} zoomLevel - the zoom level (1-15, 1 is world wide view, 15 is greatest zoom)
 * @param {int} organizationId
 * @param {int} currentWeatherStationId - the current weather station id
 * @returns {void}
 */
function initMap(center, zoomLevel, organizationId, currentWeatherStationId)
{
    // Background layer is OpenStreetMap
    var backgroundLayer = new ol.layer.Tile({
                    source: new ol.source.OSM({
                        attributions: [
                            new ol.Attribution({
                              html: mapConstants.MAP_ATTRIBUTION
                            })
                          ]
                    })
    });
    
    // The weather station layer
    var weatherStationLayer = new ol.layer.Vector({
    source: new ol.source.KML({
        url: "/rest/weatherstations/kml/" + organizationId + (currentWeatherStationId > 0 ? "?excludeWeatherStationId=" + currentWeatherStationId : ""),
            projection: "EPSG:3857"
      })
    });
    
   
    // Layer for popup
    var popOverlay = new ol.Overlay({
      element: document.getElementById("popover")
    });
    
    // Creating the map
    map = new ol.Map({
                    target: 'weatherStationFormMap',
                    layers: [backgroundLayer,weatherStationLayer],
                    overlays: [popOverlay],
                    renderer: 'canvas'
    });

    var centerPosition = ol.proj.transform(center, 'EPSG:4326', map.getView().getProjection().getCode());
    
    

    // Setting zoom and center for the map (need to do this after creating map. so that we kan transform our
    // center to correct map projection)
    var view = new ol.View2D({
            center: centerPosition,
            zoom:zoomLevel
    });
    map.setView(view);
    
    // Marker overlay
    stationMarker = new ol.Overlay({
      position: currentWeatherStationId !== null ? centerPosition : undefined,
      positioning: 'bottom-center',
      element: document.getElementById('stationMarker'),
      stopEvent: false
    });
    
    map.addOverlay(stationMarker);
    
    
    // Listening for single clicks, position observation pin and updating form element
    map.on(['singleclick'], function(evt) {
        updateLocationPosition(evt.coordinate);
    });
    
    
    
    
    
    
   
   
}

function updateLocationPosition(coordinate)
{
    var locationPosition = ol.coordinate.toStringXY(ol.proj.transform(coordinate, map.getView().getProjection().getCode(), 'EPSG:4326'),4);
    // Set/move location pin
    stationMarker.setPosition(coordinate);
    // Update form field "location"
    var locationEl = document.getElementById("location");
    //console.log(locationEl);
    locationEl.value=locationPosition;

    // Adding a little animation
    $("#location").animate({borderWidth: "4"},500, function(){
        $("#location").animate({borderWidth: "1"},500, function(){});
    });
}

/**
 * Places the station marker on the coordinates given in Location input field,
 * and centers the map around these coordinates
 */
function updateMarkerPosition()
{
    var locationEl = document.getElementById("location");
   
   var coordinate = locationEl.value.split(",");
   coordinate[0] = parseFloat(coordinate[0]);
   coordinate[1] = parseFloat(coordinate[1]);
   //console.log(coordinate);
   var centerPosition = ol.proj.transform(coordinate, 'EPSG:4326', map.getView().getProjection().getCode());
   
   stationMarker.setPosition(centerPosition);

   // Setting zoom and center for the map (need to do this after creating map. so that we kan transform our
   // center to correct map projection)
   var view = new ol.View2D({
           center: centerPosition,
           zoom:10
   });
   map.setView(view);
}

