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
 var trapMarker;
 
 function initMap(season, currentSeasonTrapsiteId, currentSeasonTrapsiteCoordinate){
 	 // Background layer is OpenStreetMap
    /*var backgroundLayer = new ol.layer.Tile({
                    source: new ol.source.OSM({
                        attributions: [
                            new ol.Attribution({
                              html: mapConstants.MAP_ATTRIBUTION
                            })
                          ]
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
                        target: 'seasonTrapsiteFormMap',
                        layers: [topo4, trapLayer],
                        //overlays: [popOverlay],
                        renderer: 'canvas'
        });

        // Setting zoom and center for the map (need to do this after creating map. so that we kan transform our
        // center to correct map projection)
        var view = new ol.View({
                center: ol.proj.transform([10.0,63.0], 'EPSG:4326', map.getView().getProjection().getCode()),
                zoom: 5
        });
        map.setView(view);

        // Marker overlay
        trapMarker = new ol.Overlay({
          position: currentSeasonTrapsiteId !== null ? ol.proj.transform(currentSeasonTrapsiteCoordinate, 'EPSG:4326', map.getView().getProjection().getCode()) : undefined,
          positioning: 'bottom-center',
          element: document.getElementById('trapMarker'),
          stopEvent: false
        });

        map.addOverlay(trapMarker);

        // Listening for single clicks, position observation pin and updating form element
        map.on(['singleclick'], function(evt) {
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