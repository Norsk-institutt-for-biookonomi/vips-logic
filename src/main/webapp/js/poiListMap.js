/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
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
 * KML layer map with weather station information
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
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
                        attributions: [mapConstants.MAP_ATTRIBUTION]
                    })
    });
    
    // The weather station layer
    var poiLayer = new ol.layer.Vector({
    source: new ol.source.Vector({
        url: "/rest/pois/kml/" + organizationId,
        format: new ol.format.KML(),
        projection: ol.proj.get("EPSG:3857")
      })
    });
    
    // Layer for popup
    var popOverlay = new ol.Overlay({
      element: document.getElementById("popover")
    });

    // Creating the map
    let map = new ol.Map({
                    target: 'poiListMap',
                    layers: [backgroundLayer,poiLayer],
                    overlays: [popOverlay],
                    renderer: 'canvas',
                    view: new ol.View({
                        center: ol.proj.fromLonLat(center, 'EPSG:3857'),
                        zoom: zoomLevel
                    })
    });

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
                    title: userPoiIds.indexOf(feature.getId()) >= 0 ? "<a href='/poi?pointOfInterestId=" + feature.getId() + "'>" + feature.get("name") + "</a>" : feature.get("name"),
                    content: feature.get("description")
            });

            poiDetails.popover('show');


          } else {
              poiDetails.popover('destroy');
          }

    };
    

    // On click, display forecasts in popup
    map.on('singleclick', (evt) => {
            var pixel = map.getEventPixel(evt.originalEvent);
              displayFeatureDetails(pixel);
            });
}

