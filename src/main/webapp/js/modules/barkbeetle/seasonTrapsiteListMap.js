/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
    var parser = new ol.format.WMTSCapabilities();
  fetch('https://opencache.statkart.no/gatekeeper/gk/gk.open_wmts?Version=1.0.0&service=wmts&request=getcapabilities').then(function(response) {
        return response.text();
      }).then(function(text) {
        var result = parser.read(text);
        var options = ol.source.WMTS.optionsFromCapabilities(result, {
          layer: 'topo4',
          matrixSet: 'EPSG:3857'
        });
        var topo4 = 
          new ol.layer.Tile({
            opacity: 1,
            source: new ol.source.WMTS(/** @type {!olx.source.WMTSOptions} */ (options))
          });
    
    // Creating the map
    map = new ol.Map({
                    target: 'seasonTrapsiteListMap',
                    layers: [topo4, trapLayer],
                    overlays: [popOverlay],
                    renderer: 'canvas'
    });
    
    // Setting zoom and center for the map (need to do this after creating map. so that we kan transform our
    // center to correct map projection)
    var view = new ol.View({
            center: ol.proj.transform([7.0,63.0], 'EPSG:4326', map.getView().getProjection().getCode()),
            zoom: 5
    });
    map.setView(view);
    
    // On click, display site details in popup
    map.on('singleclick', function(evt) {
            var pixel = map.getEventPixel(evt.originalEvent);
              displayFeatureDetails(pixel);
            });
    }); // END FETCH
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
    
 