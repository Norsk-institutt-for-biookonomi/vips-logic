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
  fetch('https://cache.kartverket.no/v1/wmts/1.0.0/WMTSCapabilities.xml').then(function(response) {
        return response.text();
      }).then(function(text) {
        var result = parser.read(text);
        var options = ol.source.WMTS.optionsFromCapabilities(result, {
          layer: 'topo',
          matrixSet: 'webmercator'
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
                    renderer: 'canvas',
                    view: new ol.View({
                        center: ol.proj.fromLonLat([7.0,63.0], 'EPSG:3857'),
                        zoom: 5
                    })
    });

    // On click, display site details in popup
    map.on('singleclick', (evt) => {
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
    
