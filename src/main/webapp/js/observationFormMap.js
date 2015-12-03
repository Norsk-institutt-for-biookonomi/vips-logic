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

/**
 *
 * @param {ol.Coordinate} center - coordinates for the map's center (WGS84)
 * @param {int} zoomLevel - the zoom level (1-15, 1 is world wide view, 15 is greatest zoom)
 * @param {boolean} displayMarker - show observation marker in center location
 * @returns {void}
 */

function initMap(center, zoomLevel, displayMarker) {


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

  var fylke_layer = new ol.layer.Vector({
    title: 'Fylkesgrenser',
    type: 'overlay',
    visible: false,
    source: new ol.source.GeoJSON({
      projection: 'EPSG:3857',
      url: '/geoserver/ows?srsname=EPSG:3857&format_options=decimals:0&service=WFS&version=1.0.0&outputFormat=json&request=GetFeature&typeName=sl:n5_forv_fylke_mv&'
    }),
    style: new ol.style.Style({
      stroke: new ol.style.Stroke({
        color: 'rgba(0, 0, 255, 1.0)',
        width: 3
      })
    })
  });
  var kommune_layer = new ol.layer.Vector({
    title: 'Kommunegrenser',
    type: 'overlay',
    visible: false,
    source: new ol.source.GeoJSON({
      projection: 'EPSG:3857',
      url: '/geoserver/ows?srsname=EPSG:3857&format_options=decimals:0&service=WFS&version=1.0.0&outputFormat=json&request=GetFeature&typeName=sl:n2000_komm_flate&'
    }),
    style: new ol.style.Style({
      stroke: new ol.style.Stroke({
        color: 'rgba(255, 0, 0, 1.0)',
        width: 1
      })
    })
  });


  /* var vector_layer = new ol.layer.Vector({
    title: 'Tegnelayer',
    type: 'overlay',
    visible: true,
    name: 'my_vectorlayer',
    source: new ol.source.Vector(),
    style: new ol.style.Style({
      fill: new ol.style.Fill({
        color: 'rgba(255, 00, 255, 0.2)'
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


  function switchLayer() {
    var checkedLayer = $('#layerswitcher input[name=layer]:checked').val();
    for (i = 0, ii = layers.length; i < ii; ++i) layers[i].setVisible(i == checkedLayer);
  }

  $(function() {
    switchLayer()
  });
  $("#layerswitcher input[name=layer]").change(function() {
    switchLayer()
  });

  /*var layers = [

    new ol.layer.Group({
      'title': 'Bakgrunnskart',
      layers: [
        new ol.layer.Tile({
          title: 'Bing Arial',
          type: 'base',
          visible: false,
          source: new ol.source.BingMaps({
            imagerySet: 'Aerial',
            key: 'Ak-dzM4wZjSqTlzveKz5u0d4IQ4bRzVI309GxmkgSVr1ewS6iPSrOvOKhA-CJlm3'
          })
        }),
        new ol.layer.Tile({
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
        })
      ]
    }),
    new ol.layer.Group({
      title: 'Kartlag',
      layers: [

        new ol.layer.Vector({
          title: 'Fylkesgrenser',
          type: 'overlay',
          visible: false,
          source: new ol.source.GeoJSON({
            projection: 'EPSG:3857',
            url: '/geoserver/ows?srsname=EPSG:3857&format_options=decimals:0&service=WFS&version=1.0.0&outputFormat=json&request=GetFeature&typeName=sl:n5_forv_fylke_mv&'
          }),
          style: new ol.style.Style({
            stroke: new ol.style.Stroke({
              color: 'rgba(0, 0, 255, 1.0)',
              width: 3
            })
          })
        }),
        new ol.layer.Vector({
          title: 'Kommunegrenser',
          type: 'overlay',
          visible: false,
          source: new ol.source.GeoJSON({
            projection: 'EPSG:3857',
            url: '/geoserver/ows?srsname=EPSG:3857&format_options=decimals:0&service=WFS&version=1.0.0&outputFormat=json&request=GetFeature&typeName=sl:n2000_komm_flate&'
          }),
          style: new ol.style.Style({
            stroke: new ol.style.Stroke({
              color: 'rgba(255, 0, 0, 1.0)',
              width: 1
            })
          })
        }),
        vector_layer
      ]
    })
  ];*/

  // Creating the map
  var map = new ol.Map({
    target: 'observationFormMap',
    //layers: layers,
    layers: [osm, bingArial, fylke_layer, kommune_layer],
    renderer: 'canvas'
  });

  var centerPosition = ol.proj.transform(center, 'EPSG:4326', map.getView().getProjection().getCode());

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
    tipLabel: 'Légende' // Optional label for button
  });
  map.addControl(layerSwitcher);

  // Listening for single clicks, position observation pin and updating form element
  /*map.on(['singleclick'], function(evt) {
    updateLocationPosition(evt.coordinate);
  });*/

  //###################### DRAWING ########################################
  // make interactions global so they can later be removed
  // The features are not added to a regular vector layer/source,
  // but to a feature overlay which holds a collection of features.
  // This collection is passed to the modify and also the draw
  // interaction, so that both can add or modify features.
  var featureOverlay = new ol.FeatureOverlay({
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
  featureOverlay.setMap(map);

  var modify = new ol.interaction.Modify({
    features: featureOverlay.getFeatures(),
    // the SHIFT key must be pressed to delete vertices, so
    // that new vertices can be drawn at the same position
    // of existing vertices
    deleteCondition: function(event) {
      return ol.events.condition.shiftKeyOnly(event) &&
        ol.events.condition.singleClick(event);
    }
  });
  map.addInteraction(modify);

  var draw; // global so we can remove it later
  function addInteraction() {
    draw = new ol.interaction.Draw({
      features: featureOverlay.getFeatures(),
      type: /** @type {ol.geom.GeometryType} */ (typeSelect.value)
    });
    map.addInteraction(draw);
  }

  var typeSelect = document.getElementById('type');


  /**
   * Let user change the geometry type.
   * @param {Event} e Change event.
   */
  typeSelect.onchange = function(e) {
    map.removeInteraction(draw);
    addInteraction();
  };

  addInteraction();



  $('#save-button').click(function() {
    // get the features drawn on the map
    var features = featureOverlay.getFeatures().getArray();
    // create an object to write features on a output KML file 
    var format = new ol.format.GeoJSON(
      /*{
      defaultDataProjection: 'EPSG:3857'
    }*/
    );
    // write features to GeoJSON format using projection EPSG:4326
    var result = format.writeFeatures(features, {
      featureProjection: 'EPSG:4326'
    });

    /*var desimalDegrees = format.readFeatures(result, {
      dataProjection: 'EPSG:3857',
      featureProjection: 'EPSG:4326'
    });*/
    //debugger;
    // Save KML node as KML file using FileSaver.js script
    alert(result);
    clearMap();
    //var str = (new XMLSerializer).serializeToString(kml);
    //var blob = new Blob([str], {type: "text/plain;charset=utf-8;"});
    //saveAs(blob, "NovaCamada.kml");
  });





  // clear map when user clicks on 'Delete all features'
  $("#delete").click(function() {
    clearMap();
  });

  // clears the map and the output of the data
  function clearMap() {
    featureOverlay.getFeatures().clear();
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

  var track = new ol.dom.Input(document.getElementById('track'));
  track.bindTo('checked', geolocation, 'tracking');

  // update the HTML page when the position changes.
  geolocation.on('change', function() {
    console.log('Geolocation change');
    $('#accuracy').text(geolocation.getAccuracy() + ' [m]');
    $('#altitude').text(geolocation.getAltitude() + ' [m]');
    $('#altitudeAccuracy').text(geolocation.getAltitudeAccuracy() + ' [m]');
    $('#heading').text(geolocation.getHeading() + ' [rad]');
    $('#speed').text(geolocation.getSpeed() + ' [m/s]');
  });

  // handle geolocation error.
  geolocation.on('error', function(error) {
    var info = document.getElementById('info');
    info.innerHTML = error.message;
    info.style.display = '';
  });

  var accuracyFeature = new ol.Feature();
  accuracyFeature.bindTo('geometry', geolocation, 'accuracyGeometry');

  var positionFeature = new ol.Feature();
  positionFeature.setStyle(new ol.style.Style({
    image: new ol.style.Circle({
      radius: 6,
      fill: new ol.style.Fill({
        color: '#3399CC'
      }),
      stroke: new ol.style.Stroke({
        color: '#fff',
        width: 2
      })
    })
  }));


  positionFeature.bindTo('geometry', geolocation, 'position')
    .transform(function() {}, function(coordinates) {
      updateLocationPosition(coordinates);
      return coordinates ? new ol.geom.Point(coordinates) : null;
    });

  var featuresOverlay = new ol.FeatureOverlay({
    map: map,
    features: [accuracyFeature, positionFeature]
  });




  function updateLocationPosition(coordinate) {
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

    /**
     *
     * @param {ol.Coordinate} center - coordinates for the map's center (WGS84)
     * @param {int} zoomLevel - the zoom level (1-15, 1 is world wide view, 15 is greatest zoom)
     * @param {boolean} displayMarker - show observation marker in center location
     * @returns {void}
     */

    function initMap(center, zoomLevel, displayMarker) {


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

      var fylke_layer = new ol.layer.Vector({
        title: 'Fylkesgrenser',
        type: 'overlay',
        visible: false,
        source: new ol.source.Vector({
          projection: 'EPSG:3857',
          format: new ol.format.GeoJSON(),
          url: '/geoserver/ows?srsname=EPSG:3857&format_options=decimals:0&service=WFS&version=1.0.0&outputFormat=json&request=GetFeature&typeName=sl:n5_forv_fylke_mv&'
        }),
        style: new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'rgba(0, 0, 255, 1.0)',
            width: 3
          })
        })
      });
      var kommune_layer = new ol.layer.Vector({
        title: 'Kommunegrenser',
        type: 'overlay',
        visible: false,
        source: new ol.source.Vector({
          projection: 'EPSG:3857',
          format: new ol.format.GeoJSON(),
          url: '/geoserver/ows?srsname=EPSG:3857&format_options=decimals:0&service=WFS&version=1.0.0&outputFormat=json&request=GetFeature&typeName=sl:n2000_komm_flate&'
        }),
        style: new ol.style.Style({
          stroke: new ol.style.Stroke({
            color: 'rgba(255, 0, 0, 1.0)',
            width: 1
          })
        })
      });


      /* var vector_layer = new ol.layer.Vector({
    title: 'Tegnelayer',
    type: 'overlay',
    visible: true,
    name: 'my_vectorlayer',
    source: new ol.source.Vector(),
    style: new ol.style.Style({
      fill: new ol.style.Fill({
        color: 'rgba(255, 00, 255, 0.2)'
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


  function switchLayer() {
    var checkedLayer = $('#layerswitcher input[name=layer]:checked').val();
    for (i = 0, ii = layers.length; i < ii; ++i) layers[i].setVisible(i == checkedLayer);
  }

  $(function() {
    switchLayer()
  });
  $("#layerswitcher input[name=layer]").change(function() {
    switchLayer()
  });

  /*var layers = [

    new ol.layer.Group({
      'title': 'Bakgrunnskart',
      layers: [
        new ol.layer.Tile({
          title: 'Bing Arial',
          type: 'base',
          visible: false,
          source: new ol.source.BingMaps({
            imagerySet: 'Aerial',
            key: 'Ak-dzM4wZjSqTlzveKz5u0d4IQ4bRzVI309GxmkgSVr1ewS6iPSrOvOKhA-CJlm3'
          })
        }),
        new ol.layer.Tile({
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
        })
      ]
    }),
    new ol.layer.Group({
      title: 'Kartlag',
      layers: [

        new ol.layer.Vector({
          title: 'Fylkesgrenser',
          type: 'overlay',
          visible: false,
          source: new ol.source.GeoJSON({
            projection: 'EPSG:3857',
            url: '/geoserver/ows?srsname=EPSG:3857&format_options=decimals:0&service=WFS&version=1.0.0&outputFormat=json&request=GetFeature&typeName=sl:n5_forv_fylke_mv&'
          }),
          style: new ol.style.Style({
            stroke: new ol.style.Stroke({
              color: 'rgba(0, 0, 255, 1.0)',
              width: 3
            })
          })
        }),
        new ol.layer.Vector({
          title: 'Kommunegrenser',
          type: 'overlay',
          visible: false,
          source: new ol.source.GeoJSON({
            projection: 'EPSG:3857',
            url: '/geoserver/ows?srsname=EPSG:3857&format_options=decimals:0&service=WFS&version=1.0.0&outputFormat=json&request=GetFeature&typeName=sl:n2000_komm_flate&'
          }),
          style: new ol.style.Style({
            stroke: new ol.style.Stroke({
              color: 'rgba(255, 0, 0, 1.0)',
              width: 1
            })
          })
        }),
        vector_layer
      ]
    })
  ];*/

      // Creating the map
      var map = new ol.Map({
        target: 'observationFormMap',
        //layers: layers,
        layers: [osm, bingArial, fylke_layer, kommune_layer],
        renderer: 'canvas'
      });

      var centerPosition = ol.proj.transform(center, 'EPSG:4326', map.getView().getProjection().getCode());

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
        tipLabel: 'Légende' // Optional label for button
      });
      map.addControl(layerSwitcher);

      // Listening for single clicks, position observation pin and updating form element
      /*map.on(['singleclick'], function(evt) {
    updateLocationPosition(evt.coordinate);
  });*/

      //###################### DRAWING ########################################
      // make interactions global so they can later be removed
      // The features are not added to a regular vector layer/source,
      // but to a feature overlay which holds a collection of features.
      // This collection is passed to the modify and also the draw
      // interaction, so that both can add or modify features.
      var features = new ol.Collection();
      var featureOverlay = new ol.layer.Vector({
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
      map.addInteraction(modify);

      var draw; // global so we can remove it later
      function addInteraction() {
        draw = new ol.interaction.Draw({
          features: features,
          type: /** @type {ol.geom.GeometryType} */ (typeSelect.value)
        });
        map.addInteraction(draw);
      }

      var typeSelect = document.getElementById('type');


      /**
       * Let user change the geometry type.
       * @param {Event} e Change event.
       */
      typeSelect.onchange = function(e) {
        map.removeInteraction(draw);
        addInteraction();
      };

      addInteraction();



      $('#save-button').click(function() {
        // get the features drawn on the map
        var features = featureOverlay.getSource().getFeatures();
        // create an object to write features on a output KML file 
        var format = new ol.format.GeoJSON(
          /*{
      defaultDataProjection: 'EPSG:3857'
    }*/
        );
        // write features to GeoJSON format using projection EPSG:4326

        var result = format.writeFeatures(features, {
          featureProjection: 'EPSG:4326'
        });

        /*var desimalDegrees = format.readFeatures(result, {
      dataProjection: 'EPSG:3857',
      featureProjection: 'EPSG:4326'
    });*/
        //debugger;
        // Save KML node as KML file using FileSaver.js script
        alert(result);
        clearMap();
        //var str = (new XMLSerializer).serializeToString(kml);
        //var blob = new Blob([str], {type: "text/plain;charset=utf-8;"});
        //saveAs(blob, "NovaCamada.kml");
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
      });

      // update the HTML page when the position changes.
      geolocation.on('change', function() {
        /*el('accuracy').innerText = geolocation.getAccuracy() + ' [m]';
  el('altitude').innerText = geolocation.getAltitude() + ' [m]';
  el('altitudeAccuracy').innerText = geolocation.getAltitudeAccuracy() + ' [m]';
  el('heading').innerText = geolocation.getHeading() + ' [rad]';
  el('speed').innerText = geolocation.getSpeed() + ' [m/s]';*/
      });

      // handle geolocation error.
      geolocation.on('error', function(error) {
        alert(error.message);
        /*var info = document.getElementById('info');
  info.innerHTML = error.message;
  info.style.display = '';*/
      });

      var accuracyFeature = new ol.Feature();
      geolocation.on('change:accuracyGeometry', function() {
        accuracyFeature.setGeometry(geolocation.getAccuracyGeometry());
      });

      var positionFeature = new ol.Feature();
      positionFeature.setStyle(new ol.style.Style({
        image: new ol.style.Circle({
          radius: 6,
          fill: new ol.style.Fill({
            color: '#3399CC'
          }),
          stroke: new ol.style.Stroke({
            color: '#fff',
            width: 2
          })
        })
      }));

      geolocation.on('change:position', function() {
        var coordinates = geolocation.getPosition();
        updateLocationPosition(coordinates);
        positionFeature.setGeometry(coordinates ?
          new ol.geom.Point(coordinates) : null);
      });

      var featuresOverlay = new ol.layer.Vector({
        map: map,
        source: new ol.source.Vector({
          features: [accuracyFeature, positionFeature]
        })
      });





      function updateLocationPosition(coordinate) {
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
      }
    }
  }
}