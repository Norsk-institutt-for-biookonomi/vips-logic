/*
 * Copyright (c) 2015-2016 NIBIO <http://www.nibio.no/>.
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
 * Logic for GIS information about an object
 * 
 * To get the current registered GIS as GEOJson, call getFeatures();
 * 
 * @author Viggo Lunde <viggo.lunde@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */

var featureOverlay, GISInfoMap;
var draw; // global so we can remove it later
var olMapDivId = "objectGISInfoMap";

/**
 * @param {string} containerId - DOM id of the div where we can intialize the map
 * @param {ol.Coordinate} center - coordinates for the map's center (WGS84)
 * @param {int} zoomLevel - the zoom level (1-15, 1 is world wide view, 15 is greatest zoom)
 * @param {boolean} displayMarker - show observation marker in center location
 * @param {string} drawnObjs - GeoJSON with geometries to display
 * @param {string} chooseLayersObj Which polygon layers to choose from. Sample: 
 * <pre>
 *  {
 *      "chooseFromMapLayers":[
 *          {
 *              "mapLayerId":1,
 *              "mapLayerTitle":"Kommunegrenser",
 *              "description":"Norske kommunegrenser",
 *              "mapLayerType":"overlay",
 *              "defaultVisible":false,
 *              "hoverAttribute":"navn",
 *              "url":"http://kart13.skogoglandskap.no/geoserver/ows?srsname=EPSG:3857&format_options=decimals:0&service=WFS&version=1.0.0&outputFormat=json&request=GetFeature&typeName=sl:n2000_komm_flate&"
 *          },
 *          {
 *              "mapLayerId":2,
 *              "mapLayerTitle":"Fylkesgrenser",
 *              "description":"Norske fylkesgrenser",
 *              "mapLayerType":"overlay",
 *              "defaultVisible":false,
 *              "hoverAttribute":null,
 *              "url":"http://kart13.skogoglandskap.no/geoserver/ows?srsname=EPSG:3857&format_options=decimals:0&service=WFS&version=1.0.0&outputFormat=json&request=GetFeature&typeName=sl:n5_forv_fylke_mv&"
 *          }
 *      ]
 *  }
 *  </pre>
 *  
 * @returns {void}
 */
function initGisInfoMap(containerId, center, zoomLevel, displayMarker, drawnObjs, chooseLayersObj) {

  // Create the DOM structure
  var theContainer = el(containerId);
  theContainer.innerHTML = [
      "<div class='form-group' id='drawOptions'>",
      "     <label>",getI18nMsg("mapDrawTypeLabel"),"</label>",
      "     <select class='form-control' id='type' style='width: auto; display: inline;'>",
      "         <option value='Point'>",getI18nMsg("point"),"</option>",
      "         <option value='Polygon'>",getI18nMsg("polygon"),"</option>",
      "     </select>",
      "     <button type='button' id='delete' class='btn btn-danger' type='button'>",getI18nMsg("clearAll"),"</button>",
      "     <button type='button' id='deleteOne' class='btn btn-warning' type='button'>",getI18nMsg("clearOne"),"</button>",
      "</div>",
      "<div id='", olMapDivId ,"' class='map'></div>",
      "<div id='alert_placeholder'></div>"
  ].join("");
  
  

  // Adding standard base layers
  // 
  // TODO: Make this configurable per organization (input base layers as function parameter)

  // OpenStreetMap
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

  // Satellite photo map from Microsoft/Bing
  /*var bingArial = new ol.layer.Tile({
    title: 'Bing Arial',
    type: 'base',
    visible: false,
    source: new ol.source.BingMaps({
      imagerySet: 'Aerial',
      key: 'Ak-dzM4wZjSqTlzveKz5u0d4IQ4bRzVI309GxmkgSVr1ewS6iPSrOvOKhA-CJlm3'
    })
  });*/

  // Detailed map of Norway in shades of grey
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
  
  

  //########################Get maplayers you can choose from########################

  //var layersObj = eval ("(" + chooseLayersObj + ")");
  var allLayers = [osm, topo2graatone] ;
  var chooseLayers = [];
  var choosenLayer;
  var hoverAttribute = '';
  var deleteObj = false;

  var typeSelect = document.getElementById('type');

    if (chooseLayersObj.chooseFromMapLayers.length > 0){
      for (i = 0; i < chooseLayersObj.chooseFromMapLayers.length; i++) { 
        var layer = chooseLayersObj.chooseFromMapLayers[i];
        var opt = document.createElement("option");
        opt.value = layer.mapLayerId;
        opt.text = layer.mapLayerTitle;
        typeSelect.appendChild(opt);
        chooseLayers.push(new ol.layer.Vector({
          id: layer.mapLayerId,
          title: layer.mapLayerTitle,
          type: layer.mapLayerType,
          visible: layer.defaultVisible, 
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


window.app = {};
var app = window.app;


//
// Define rotate to north control.
//



/**
 * @constructor
 * @extends {ol.control.Control}
 * @param {Object=} opt_options Control options.
 */
app.geoLocationControl = function(opt_options) {

  var options = opt_options || {};

  var button = document.createElement('button');
  //button.innerHTML = 'O';
  button.title = getI18nMsg("showMeWhereIAm");

  var this_ = this;

  var handleGeoLocation = function(e) {
    /*if (geolocation.getTracking()){
      geolocation.setTracking(false);
      geoOverlay.getSource().clear(true);
    } else {*/
      GISInfoMap.removeInteraction(draw);
      removeMouseOver();
      $('#deleteOne').removeClass('clearOneActive');
      deleteObj = false;
      geolocation.setTracking(true);
    //}
  };

  button.addEventListener('click', handleGeoLocation, false);
  button.addEventListener('touchstart', handleGeoLocation, false);

  var element = document.createElement('div');
  element.className = 'ol-unselectable ol-control geo-location';
  element.appendChild(button);

  ol.control.Control.call(this, {
    element: element,
    target: options.target
  });

};
ol.inherits(app.geoLocationControl, ol.control.Control);

 

  // Creating the map
  GISInfoMap = new ol.Map({
    target: olMapDivId, // Defined at the top of this file
    controls: ol.control.defaults({
      attributionOptions: /** @type {olx.control.AttributionOptions} */ ({
        collapsible: true
      })
    }).extend([
      new app.geoLocationControl()
    ]),
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

  GISInfoMap.setView(view);




  // Marker overlay
  var marker = new ol.Overlay({
    position: displayMarker ? centerPosition : undefined,
    positioning: 'center-center',
    element: document.getElementById('marker'),
    stopEvent: false
  });

  GISInfoMap.addOverlay(marker);

  // Adding the mouse position control
  var mousePositionControl = new ol.control.MousePosition({
    coordinateFormat: ol.coordinate.createStringXY(8),
    projection: 'EPSG:4326',
    undefinedHTML: '&nbsp;'
  });
  GISInfoMap.addControl(mousePositionControl);

  var layerSwitcher = new ol.control.LayerSwitcher({
    tipLabel: 'Layerswitcher' // Optional label for button
  });
  GISInfoMap.addControl(layerSwitcher);


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
      image: new ol.style.Circle({
        radius: 10,
        fill: new ol.style.Fill({
          color: 'rgba(0, 0, 255, 0.5)'
        })
      }),
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

  

  if (drawnObjs) { // Get drawn objs param from initMap
    var format = new ol.format.GeoJSON();
    var drawnfeatures = format.readFeatures(drawnObjs, {
      dataProjection: 'EPSG:4326',
      featureProjection: GISInfoMap.getView().getProjection().getCode()
    });
    //featureOverlay.clear(true);
    featureOverlay.getSource().addFeatures(drawnfeatures);
  }

  featureOverlay.setMap(GISInfoMap);
  mouseOverlay.setMap(GISInfoMap);

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


  
  function addDrawInteraction() {
    draw = new ol.interaction.Draw({
      features: features,
      type: /** @type {ol.geom.GeometryType} */ (typeSelect.value)
    });
    GISInfoMap.addInteraction(draw);
  }

  var pointerMoveId;
  function removeMouseOver(){
    GISInfoMap.unByKey(pointerMoveId);
    mouseOverlay.getSource().clear(true);
  }


  function chooseFromLayer(chooseLayer) {
    GISInfoMap.removeInteraction(draw);
    GISInfoMap.removeInteraction(modify);
    chooseLayer.setVisible(true);

    var clickInfo = function(pixel) {
      var features = [];
      GISInfoMap.forEachFeatureAtPixel(pixel, function(feature) {

        if (deleteObj){
          if (feature){
            //debugger;
            featureOverlay.getSource().removeFeature(feature);
            setTimeout(function() { //wait until feature is removed
              mouseOverlay.getSource().clear(true);
            }, 100);
          }
          
        }else {
          if (feature.getId()){
            featureOverlay.getSource().addFeature(feature);
          }
        }
      });
    };

    GISInfoMap.on('click', function(evt) {
      var pixel = evt.pixel;
      clickInfo(pixel);
    });

    var mouseInfo = function(pixel) {
      var features = [];
      mouseOverlay.getSource().clear(true);
      GISInfoMap.forEachFeatureAtPixel(pixel, function(feature, layer) {
        mouseOverlay.getSource().addFeature(feature);
      });
    };

    pointerMoveId =  GISInfoMap.on('pointermove', function(evt) {
      var pixel = evt.pixel;
      mouseInfo(pixel);
    });
  }



  /**
   * Let user change the geometry type.
   * @param {Event} e Change event.
   */
  typeSelect.onchange = function(e) {
    GISInfoMap.removeInteraction(draw);
    deleteObj = false;

    for (i = 0; i < chooseLayers.length; i++) {
      if (parseInt(e.target.value) === chooseLayers[i].getProperties().id) {
        choosenLayer = chooseLayers[i];
        hoverAttribute = choosenLayer.getProperties().hoverAttribute;
        choosenLayer.setVisible(true);
        chooseFromLayer(choosenLayer);
      } else {
        chooseLayers[i].setVisible(false);
      }
    }
    if (e.target.value === 'Point' || e.target.value === 'Polygon'){
      GISInfoMap.addInteraction(modify);
      addDrawInteraction();
      removeMouseOver();
    }
  };

  addDrawInteraction();



 /* $('#save-button').click(function() {
    // get the features drawn on the map
    var features = null;
    features = featureOverlay.getSource().getFeatures();
    var format = new ol.format.GeoJSON();
    // write features to GeoJSON format using projection EPSG:4326

    var result = format.writeFeatures(features, {
      dataProjection: 'EPSG:4326',
      featureProjection: map.getView().getProjection().getCode()
    });

    alert(result);
  });*/







  // clear map when user clicks on 'Delete all features'
  $("#delete").click(function() {
    clearMap();
  });

  // clears the map and the output of the data
  function clearMap() {
    featureOverlay.getSource().clear(true);
    geoOverlay.getSource().clear(true);
    //geolocation.setTracking(false);
  }

  $('#deleteOne').click(function() {
    deleteObj = !deleteObj; // toggle
    if (deleteObj){
      $('#deleteOne').addClass('clearOneActive');
      chooseFromLayer(featureOverlay);
      GISInfoMap.removeInteraction(modify);
      GISInfoMap.removeInteraction(draw);
    } else {
      $('#deleteOne').removeClass('clearOneActive');
      removeMouseOver();
      mouseOverlay.getSource().clear(true);
      GISInfoMap.addInteraction(modify);
      addDrawInteraction();
    }
      
  });

  function removeFeature(featureID) {

   var features = featureOverlay.getSource().getFeatures();
     if (features != null && features.length > 0) {
         for (x in features) {
            var id = features[x].getProperties().id;
            if (id == featureID) {
              featureOverlay.getSource().removeFeature(features[x]);
              geoOverlay.getSource().clear(true);
               break;
            }
          }
        }
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


  var geoPosStyle = new ol.style.Style({
    fill: new ol.style.Fill({
        color: 'rgba(0, 0, 255, 0.1)'
      }),
      stroke: new ol.style.Stroke({
        color: 'rgba(0, 0, 255, 0.5)',
        width: 2
      }),
      image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
        anchor: [13, 13],
        anchorXUnits: 'pixels',
        anchorYUnits: 'pixels',
        opacity: 1,
        src: 'images/geoloc2.png'
      }))
    });


 var geoFeatures = new ol.Collection();
  geoOverlay = new ol.layer.Vector({
    source: new ol.source.Vector({
      features: geoFeatures
    }),
    style: geoPosStyle
  });

  geoOverlay.setMap(GISInfoMap);




/* var element = document.createElement('div');
    element.className = 'ol-unselectable ol-control geo-location';

    var button = document.createElement('button');
    button.setAttribute('title', tipLabel);
    element.appendChild(button);

*/
  var geolocation = new ol.Geolocation({
    projection: view.getProjection()
  });

  function el(id) {
    return document.getElementById(id);
  }


  // handle geolocation error.
  geolocation.on('error', function(error) {
    alert(error.message);
  });

  function showalert(message,alerttype) {

    $('#alert_placeholder').append('<div id="alertdiv" class="alert alert-danger fade in"><a class="close" data-dismiss="alert">×</a><span>'+message+'</span></div>')
    setTimeout(function() { // this will automatically close the alert and remove this if the users doesnt close it in 5 secs
      $("#alertdiv").remove();
    }, 5000);
  }


/**
 * Listen for changes in the user's location
 */
  geolocation.on('change:position', function() {

    removeFeature('geoLoc');

    var coordinates = geolocation.getPosition();

    var accuracy = geolocation.getAccuracy();
    if(accuracy > 500){
      showalert('Geolocation accuracy (blue circle) is larger than 500, accuracy now: '+accuracy,'alert-error')
    }
    
    var geoExtent = [
      coordinates[0] - accuracy - 100,
      coordinates[1] - accuracy - 100,
      coordinates[0] + accuracy + 100,
      coordinates[1] + accuracy + 100
      ];
    var posFeature = new ol.Feature({
      geometry: new ol.geom.Point(coordinates),
    });
    posFeature.set('id', 'geoLoc');
   
    featureOverlay.getSource().addFeature(posFeature);
    geoOverlay.getSource().addFeature(posFeature);

    
    var accuracyFeature = new ol.Feature();
      geolocation.on('change:accuracyGeometry', function() {
        accuracyFeature.setGeometry(geolocation.getAccuracyGeometry());
      });

    geoOverlay.getSource().addFeature(accuracyFeature);

    var numFeatures = featureOverlay.getSource().getFeatures().length;
   
    if (numFeatures > 1) { //Zoom to all features
      var extent = featureOverlay.getSource().getExtent();
      GISInfoMap.getView().fit(extent, GISInfoMap.getSize());
    } else if (geoExtent) { //Zoom to accuracy circle
      GISInfoMap.getView().fit(geoExtent, GISInfoMap.getSize());
    } else { //Zoom to geolocation
      GISInfoMap.getView().setCenter(coordinates);
      GISInfoMap.getView().setZoom(15);
    }

    GISInfoMap.addInteraction(modify);
    addDrawInteraction();

    geolocation.setTracking(false);
 
 });



  /*function updateLocationPosition(coordinate) {
    //var locationPosition = ol.coordinate.toStringXY(ol.proj.transform(coordinate, 'EPSG:3857', 'EPSG:4326'), 8);
    // Set/move location pin
    marker.setPosition(coordinate);
    // Update form field "location"
    //var locationEl = document.getElementById("location");
    //locationEl.setAttribute("value", locationPosition);
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

/**
 * @return {GEOJson} returns all features currently on the map in GEOJson format
 */
function getFeatures() {
  var features = featureOverlay.getSource().getFeatures();
  var format = new ol.format.GeoJSON();
  // write features to GeoJSON format using projection EPSG:4326

  var result = format.writeFeatures(features, {
    dataProjection: 'EPSG:4326',
    featureProjection: GISInfoMap.getView().getProjection().getCode()
  });

  return result;
}