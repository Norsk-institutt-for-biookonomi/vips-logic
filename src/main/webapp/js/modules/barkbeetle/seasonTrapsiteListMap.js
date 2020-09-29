/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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
 
 /*
 * Map with barkbeetle trap information
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
 
 var map;
 var popOverlay;
 var poiDetails;
 
 function initMap(season){
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
    
    // The trapsite layer
    var trapLayer = new ol.layer.Vector({
    source: new ol.source.Vector({
        url: "/rest/barkbeetle/seasontrapsites/" + season + "/kml/status",
		projection: "EPSG:3857",
		format: new ol.format.KML({showPointNames:false})
      })
    });
    
    // Using Bootstrap's popover plugin. See http://getbootstrap.com/javascript/#popovers
    poiDetails = $("#popover");
    
    // Layer for popup
    popOverlay = new ol.Overlay({
      element: document.getElementById("popover")
    });
    
    // Creating the map
    map = new ol.Map({
                    target: 'seasonTrapsiteListMap',
                    layers: [backgroundLayer, trapLayer],
                    overlays: [popOverlay],
                    renderer: 'canvas'
    });
    
    // Setting zoom and center for the map (need to do this after creating map. so that we kan transform our
    // center to correct map projection)
    var view = new ol.View({
            center: ol.proj.transform([10.0,63.0], 'EPSG:4326', map.getView().getProjection().getCode()),
            zoom: 5
    });
    map.setView(view);
    
    // On click, display site details in popup
    map.on('singleclick', function(evt) {
            var pixel = map.getEventPixel(evt.originalEvent);
              displayFeatureDetails(pixel);
            });
 }
 
 // Displays popup with forecasts for a given station
    // (if there is a station where the click event is fired)
    var displayFeatureDetails = function(pixel, coordinate) {
          var feature = map.forEachFeatureAtPixel(pixel, function(feature,layer){
             return feature; 
          });

          if (feature) {
              // Position the popup, and hiding it
              // Resetting information from (possible) former popups
              var geometry = feature.getGeometry();
              popOverlay.setPosition(geometry.getCoordinates());
              poiDetails.popover('destroy');
              // Create the popup, showing it
              poiDetails.popover({
                    animation: true,
                    trigger: 'manual',
                    html: true,
                    placement: "auto top",
                    title: feature.get("name"),
                    content: feature.get("description")
            });

            poiDetails.popover('show');


          } else {
              poiDetails.popover('destroy');
          }

    };
    
 