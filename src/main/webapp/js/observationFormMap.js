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

var featureOverlay, map;

/**
 *
 * @param {ol.Coordinate} center - coordinates for the map's center (WGS84)
 * @param {int} zoomLevel - the zoom level (1-15, 1 is world wide view, 15 is greatest zoom)
 * @param {boolean} displayMarker - show observation marker in center location
 * @param {string} drawnObjs - GeoJSON with geometries to display
 * @returns {void}
 */
function initMap(center, zoomLevel, displayMarker, drawnObjs, chooseLayersObj) {


  var osm = new ol.layer.Tile({
    'title': 'OSM',
    type: 'base',
    visible: true,
    source: new ol.source.OSM({
      attributions: [
        new ol.Attribution({
          html: mapConstants.MAP_ATTRIBUTION
        })
      ]
    })
  });

  var bingArial = new ol.layer.Tile({
    title: 'Bing Arial',
    type: 'base',
    visible: false,
    source: new ol.source.BingMaps({
      imagerySet: 'Aerial',
      key: 'Ak-dzM4wZjSqTlzveKz5u0d4IQ4bRzVI309GxmkgSVr1ewS6iPSrOvOKhA-CJlm3'
    })
  });


  var topo2graatone = new ol.layer.Tile({
    title: "Gråtone",
    type: 'base',
    visible: false,
    source: new ol.source.TileWMS({
      url: "http://opencache.statkart.no/gatekeeper/gk/gk.open?",
      params: {
        LAYERS: 'topo2graatone',
        VERSION: '1.1.1'
      }
    })
  });
 
  //-----------------Get maplayers you can choose from-----------------------------

  //var layersObj = eval ("(" + chooseLayersObj + ")");
  var allLayers = [osm, bingArial, topo2graatone] ;
  var chooseLayers = [];
  var choosenLayer;
  var hoverAttribute = '';

  var typeSelect = document.getElementById('type');

    if (chooseLayersObj.chooseFromMapLayers.length > 0){
      for (i = 0; i < chooseLayersObj.chooseFromMapLayers.length; i++) { 
        var layer = chooseLayersObj.chooseFromMapLayers[i];
        var opt = document.createElement("option");
        opt.value = layer.id;
        opt.text=layer.title;
        typeSelect.appendChild(opt);
        chooseLayers.push(new ol.layer.Vector({
          id: layer.id,
          title: layer.title,
          type: layer.type,
          visible: layer.visible, 
          hoverAttribute:layer.hoverAttribute,          
          source: new ol.source.Vector({
            projection: layer.projection,
            format: new ol.format.GeoJSON(),
            url: layer.url
          })
        }));
      }
    }

    allLayers.push.apply(allLayers, chooseLayers);
 

  // Creating the map
  map = new ol.Map({
    target: 'observationFormMap',
    layers: allLayers,
    renderer: 'canvas'
  });

  var centerPosition = ol.proj.transform(center, 'EPSG:4326', 'EPSG:3857');

  // Setting zoom and center for the map (need to do this after creating map. so that we kan transform our
  // center to correct map projection)
  view = new ol.View({
    center: centerPosition,
    zoom: zoomLevel
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

  var layerSwitcher = new ol.control.LayerSwitcher({
    tipLabel: 'Layerswitcher' // Optional label for button
  });
  map.addControl(layerSwitcher);


  //###################### DRAWING ########################################
  // make interactions global so they can later be removed
  // The features are not added to a regular vector layer/source,
  // but to a feature overlay which holds a collection of features.
  // This collection is passed to the modify and also the draw
  // interaction, so that both can add or modify features.
  var features = new ol.Collection();
  featureOverlay = new ol.layer.Vector({
    source: new ol.source.Vector({
      features: features
    }),
    style: new ol.style.Style({
      fill: new ol.style.Fill({
        color: 'rgba(255, 0, 255, 0.2)'
      }),
      stroke: new ol.style.Stroke({
        color: '#ff00ff',
        width: 2
      }),
      image: new ol.style.Circle({
        radius: 7,
        fill: new ol.style.Fill({
          color: '#ff00ff'
        })
      })
    })
  });


  function computeFeatureStyle(feature, resolution) {
    return [new ol.style.Style({
      fill: new ol.style.Fill({
        color: 'rgba(0, 0, 255, 0.2)'
      }),
      stroke: new ol.style.Stroke({
        color: 'rgba(0, 0, 255, 0.8)',
        width: 1
      }),
      text: new ol.style.Text({
        font: '12px helvetica,sans-serif',
        text: feature.get(hoverAttribute),
        //rotation: 360 * rnd * Math.PI / 180,
        fill: new ol.style.Fill({
          color: '#000'
        }),
        stroke: new ol.style.Stroke({
          color: '#fff',
          width: 2
        })
      })
    })];
  }


  var mouseFeatures = new ol.Collection();
  mouseOverlay = new ol.layer.Vector({
    source: new ol.source.Vector({
      features: mouseFeatures
    }),
    style: computeFeatureStyle
  });

  mouseOverlay.setMap(map);

  if (drawnObjs) { // Get drawn objs param from initMap
    var format = new ol.format.GeoJSON();
    var drawnfeatures = format.readFeatures(drawnObjs, {
      dataProjection: 'EPSG:4326',
      featureProjection: map.getView().getProjection().getCode()
    });
    //featureOverlay.clear(true);
    featureOverlay.getSource().addFeatures(drawnfeatures);
  }

  featureOverlay.setMap(map);

  var modify = new ol.interaction.Modify({
    features: features,
    // the SHIFT key must be pressed to delete vertices, so
    // that new vertices can be drawn at the same position
    // of existing vertices
    deleteCondition: function(event) {
      return ol.events.condition.shiftKeyOnly(event) &&
        ol.events.condition.singleClick(event);
    }
  });


  var draw; // global so we can remove it later
  function addDrawInteraction() {
    draw = new ol.interaction.Draw({
      features: features,
      type: /** @type {ol.geom.GeometryType} */ (typeSelect.value)
    });
    map.addInteraction(draw);
  }


  function chooseFromLayer(chooseLayer) {
      map.removeInteraction(draw);
     chooseLayer.setVisible(true);
      //map.addInteraction(selectInteraction);
    var clickInfo = function(pixel) {
      var features = [];
      map.forEachFeatureAtPixel(pixel, function(feature, layer) {
        featureOverlay.getSource().addFeature(feature);
      });
    };

    map.on('click', function(evt) {
      var pixel = evt.pixel;
      clickInfo(pixel);
    });

    var mouseInfo = function(pixel) {
      var features = [];
      map.forEachFeatureAtPixel(pixel, function(feature, layer) {
        mouseOverlay.getSource().clear(true);
        mouseOverlay.getSource().addFeature(feature);
      });
    };

    map.on('pointermove', function(evt) {
      var pixel = evt.pixel;
      mouseInfo(pixel);
    });
  }



  /**
   * Let user change the geometry type.
   * @param {Event} e Change event.
   */
  typeSelect.onchange = function(e) {
    map.removeInteraction(draw);

    for (i = 0; i < chooseLayers.length; i++) {
      if (e.target.value === chooseLayers[i].getProperties().id) {
        choosenLayer = chooseLayers[i];
        hoverAttribute = choosenLayer.getProperties().hoverAttribute;
        choosenLayer.setVisible(true);
        chooseFromLayer(choosenLayer);
      } else {
        chooseLayers[i].setVisible(false);
      }
    }
    if (e.target.value === 'Point' || e.target.value === 'Polygon'){
      map.addInteraction(modify);
        addDrawInteraction();
    }
  };

  addDrawInteraction();



  $('#save-button').click(function() {
    // get the features drawn on the map
    var features = featureOverlay.getSource().getFeatures();
    var format = new ol.format.GeoJSON();
    // write features to GeoJSON format using projection EPSG:4326

    var result = format.writeFeatures(features, {
      dataProjection: 'EPSG:4326',
      featureProjection: map.getView().getProjection().getCode()
    });

    alert(result);
  });





  // clear map when user clicks on 'Delete all features'
  $("#delete").click(function() {
    clearMap();
  });

  // clears the map and the output of the data
  function clearMap() {
    featureOverlay.getSource().clear(true);
  }

  // creates unique id's
  function uid() {
    var id = 0;
    return function() {
      if (arguments[0] === 0) {
        id = 0;
      }
      return id++;
    }
  }

  //######################## GEOLOCATION  #############


  var geolocation = new ol.Geolocation({
    projection: view.getProjection()
  });

  function el(id) {
    return document.getElementById(id);
  }

  el('track').addEventListener('change', function() {
    geolocation.setTracking(this.checked);
    $('#type').val('Point');
    for (i = 0; i < chooseLayers.length; i++) {
      chooseLayers[i].setVisible(false);
    }
  });


  // handle geolocation error.
  geolocation.on('error', function(error) {
    alert(error.message);
  });



  geolocation.on('change:position', function() {
    var coordinates = geolocation.getPosition();

    var posFeature = new ol.Feature({
      geometry: new ol.geom.Point(coordinates)
    });

    featureOverlay.getSource().addFeature(posFeature);

    var numFeatures = featureOverlay.getSource().getFeatures().length;
    if (numFeatures > 1) { //Zoom to all features
      var extent = featureOverlay.getSource().getExtent();
      map.getView().fit(extent, map.getSize());
    } else { //Zoom to geolocation
      map.getView().setCenter(coordinates);
      map.getView().setZoom(15);
    }

    map.addInteraction(modify);
    addDrawInteraction();


  });



  /*function updateLocationPosition(coordinate) {
    var locationPosition = ol.coordinate.toStringXY(ol.proj.transform(coordinate, 'EPSG:3857', 'EPSG:4326'), 8);
    // Set/move location pin
    marker.setPosition(coordinate);
    // Update form field "location"
    var locationEl = document.getElementById("location");
    locationEl.setAttribute("value", locationPosition);
    // Adding a little animation
    $("#location").animate({
      borderWidth: "4"
    }, 500, function() {
      $("#location").animate({
        borderWidth: "1"
      }, 500, function() {});
    });
  }*/
}

function getFeatures() {
  var features = featureOverlay.getSource().getFeatures();
  var format = new ol.format.GeoJSON();
  // write features to GeoJSON format using projection EPSG:4326

  var result = format.writeFeatures(features, {
    dataProjection: 'EPSG:4326',
    featureProjection: map.getView().getProjection().getCode()
  });

  return result;
}