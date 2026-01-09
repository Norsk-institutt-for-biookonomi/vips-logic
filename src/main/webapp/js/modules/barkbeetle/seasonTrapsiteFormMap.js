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
 var trapMarker;
 
 function initMap(season, currentSeasonTrapsiteId, currentSeasonTrapsiteCoordinate){
 	 // Background layer is OpenStreetMap
    /*var backgroundLayer = new ol.layer.Tile({
                    source: new ol.source.OSM({
                        attributions: [mapConstants.MAP_ATTRIBUTION]
                    })
    });*/
    
    // The trapsite layer
    var trapLayer = new ol.layer.Vector({
    source: new ol.source.Vector({
        url: "/rest/barkbeetle/seasontrapsites/" + season + "/kml/" + (currentSeasonTrapsiteId != null ? "?excludeSeasonTrapsiteId=" + currentSeasonTrapsiteId : ""),
		projection: "EPSG:3857",
		format: new ol.format.KML({showPointNames:false})
      })
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
                        target: 'seasonTrapsiteFormMap',
                        layers: [topo4, trapLayer],
                        //overlays: [popOverlay],
                        renderer: 'canvas',
                        view: new ol.View({
                            center: ol.proj.fromLonLat([10.0,63.0], 'EPSG:3857'),
                            zoom: 5
                        })
        });

        // Marker overlay
        trapMarker = new ol.Overlay({
          position: currentSeasonTrapsiteId !== null ? ol.proj.transform(currentSeasonTrapsiteCoordinate, 'EPSG:4326', map.getView().getProjection().getCode()) : undefined,
          positioning: 'bottom-center',
          element: document.getElementById('trapMarker'),
          stopEvent: false
        });

        map.addOverlay(trapMarker);

        // Listening for single clicks, position observation pin and updating form element
        map.on(['singleclick'], (evt) => {
            updateLocationPosition(evt.coordinate);
        });
    }); // END FETCH
 }
 
 function updateLocationPosition(coordinate)
{
    var locationPosition = ol.coordinate.toStringXY(ol.proj.transform(coordinate, map.getView().getProjection().getCode(), 'EPSG:4326'),5);
    // Set/move location pin
    trapMarker.setPosition(coordinate);
    // Update form field "location"
    var locationEl = document.getElementById("gisGeom");
    //console.log(locationEl);
    locationEl.value=locationPosition;
	showGisGeom();

    // Adding a little animation
    $("#gisGeom").animate({borderWidth: "4"},500, function(){
        $("#gisGeom").animate({borderWidth: "1"},500, function(){});
    });
    
    handleLocationChanged(locationEl);
}

/**
 * Places the station marker on the coordinates given in Location input field,
 * and centers the map around these coordinates
 */
function updateMarkerPosition()
{
    var locationEl = document.getElementById("gisGeom");
   
   var coordinate = locationEl.value.split(",");
   coordinate[0] = parseFloat(coordinate[0]);
   coordinate[1] = parseFloat(coordinate[1]);
   //console.log(coordinate);
   var markerPosition = ol.proj.transform(coordinate, 'EPSG:4326', map.getView().getProjection().getCode());
   
   trapMarker.setPosition(markerPosition);
}
