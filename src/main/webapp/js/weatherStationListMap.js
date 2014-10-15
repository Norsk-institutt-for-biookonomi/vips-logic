/*
 * Copyright (c) 2014 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the Bioforsk Open Source License as published by 
 * Bioforsk, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Bioforsk Open Source License for more details.
 * 
 * You should have received a copy of the Bioforsk Open Source License
 * along with VIPSLogic.  If not, see <http://www.bioforsk.no/licenses/>.
 * 
 */

/*
 * KML layer map with weather station information
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 */

/**
 * 
 * @param {ol.Coordinate} center - coordinates for the map's center (WGS84)
 * @param {int} zoomLevel - the zoom level (1-15, 1 is world wide view, 15 is greatest zoom)
 * @param {int} organizationId - which organization to show maps from
 * @returns {void}
 */
function initMap(center, zoomLevel, organizationId)
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
        url: "/rest/weatherstations/kml/" + organizationId,
            projection: "EPSG:3857"
      })
    });
    
    // Layer for popup
    var popOverlay = new ol.Overlay({
      element: document.getElementById("popover")
    });
    
    // Creating the map
    var map = new ol.Map({
                    target: 'weatherStationListMap',
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
    
    // Using Bootstrap's popover plugin. See http://getbootstrap.com/javascript/#popovers
    var poiDetails = $("#popover");
    
    
    
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
                    title: "<a href='/weatherStation?pointOfInterestId=" + feature.getId() + "'>" + feature.get("name") + "</a>",
                    content: feature.get("description")
            });

            poiDetails.popover('show');


          } else {
              poiDetails.popover('destroy');
          }

    };
    

    // On click, display forecasts in popup
    map.on('singleclick', function(evt) {
            var pixel = map.getEventPixel(evt.originalEvent);
              displayFeatureDetails(pixel);
            });
}

