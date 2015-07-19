/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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
 * Logic for registering location of an observation using an OpenLayers v3 map
 */

/**
 * 
 * @param {ol.Coordinate} center - coordinates for the map's center (WGS84)
 * @param {int} zoomLevel - the zoom level (1-15, 1 is world wide view, 15 is greatest zoom)
 * @param {boolean} displayMarker - show observation marker in center location
 * @returns {void}
 */
function initMap(center, zoomLevel, displayMarker)
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
    
    

    
    // Creating the map
    var map = new ol.Map({
                    target: 'observationFormMap',
                    layers: [backgroundLayer],
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
    var marker = new ol.Overlay({
      position: displayMarker ? centerPosition : undefined,
      positioning: 'center-center',
      element: document.getElementById('marker'),
      stopEvent: false
    });

    map.addOverlay(marker);
    
    // Adding the mouse position control
    var mousePositionControl = new ol.control.MousePosition({
		  coordinateFormat: ol.coordinate.createStringXY(8),
		  projection: 'EPSG:4326',
		  undefinedHTML: '&nbsp;'
		});
    map.addControl(mousePositionControl);
    
    // Listening for single clicks, position observation pin and updating form element
    map.on(['singleclick'], function(evt) {
        updateLocationPosition(evt.coordinate);
    });
    
    
    
    function updateLocationPosition(coordinate)
    {
        var locationPosition = ol.coordinate.toStringXY(ol.proj.transform(coordinate, map.getView().getProjection().getCode(), 'EPSG:4326'),8);
        // Set/move location pin
        marker.setPosition(coordinate);
        // Update form field "location"
        var locationEl = document.getElementById("location");
        locationEl.setAttribute("value", locationPosition);
        // Adding a little animation
        $("#location").animate({borderWidth: "4"},500, function(){
            $("#location").animate({borderWidth: "1"},500, function(){});
        });
    }
    
}

