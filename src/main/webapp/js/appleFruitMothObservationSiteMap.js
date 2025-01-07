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
 * KML layer map with information about an apple fruit moth observation site
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
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
function initMap(center, observationSiteId, currentSeason)
{
    // Background layer is OpenStreetMap
    var backgroundLayer = new ol.layer.Tile({
                    source: new ol.source.OSM({
                        attributions: [
                            new ol.Attribution({
                              html: ""
                            })
                          ]
                    })
    });
    
    // Layer for popup
    var popOverlay = new ol.Overlay({
      element: document.getElementById("popover")
    });
    
    var sitePointsLayer = new ol.layer.Vector({
            source: new ol.source.Vector({
                    url: "/rest/applefruitmoth/observationsites/kml/" + currentSeason + "/?onlyPubliclyAvailable=false&selectedObservationSiteId=" + observationSiteId,
                    format: new ol.format.KML({"extractAttributes":true}),
                    projection: ol.proj.get('EPSG:3857')
            })
    });
    
    // Creating the map
    map = new ol.Map({
                    target: 'appleFruitMothObservationSiteMap',
                    layers: [backgroundLayer,sitePointsLayer],
                    overlays: [popOverlay],
                    renderer: 'canvas'
    });

    var centerPosition = ol.proj.transform(center, 'EPSG:4326', map.getView().getProjection().getCode());
    
    // Setting zoom and center for the map (need to do this after creating map. so that we kan transform our
    // center to correct map projection)
    var view = new ol.View({
            center: centerPosition,
            zoom:9
    });
    map.setView(view);
       
}


