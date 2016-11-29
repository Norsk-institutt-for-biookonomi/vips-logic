// The globally available map object
var map, featureOverlay, newFeatureOverlay; 

function initMap()
{
    // OpenStreetMap background layer
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
    
    // Detailed map of Norway in shades of grey
  var topo2graatone = new ol.layer.Tile({
    title: "Gråtone",
    type: 'base',
    visible: true,
    source: new ol.source.TileWMS({
        attributions: [
            new ol.Attribution({
              html: "Kartgrunnlag: Statens kartverk (<a href='http://creativecommons.org/licenses/by-sa/3.0/no/' target='new'>cc-by-sa-3.0</a>)"
            })
          ],
      url: "http://opencache.statkart.no/gatekeeper/gk/gk.open?",
      //url: "//openwms.statkart.no/skwms1/wms.topo2.graatone?"//,
      params: {
        LAYERS: 'topo2graatone',
        VERSION: '1.1.1'
      }
    })
  });
  
  // The layer for putting the data
  var features = new ol.Collection();
  
    
var styles = {
    // Bulkemispel = rød
    'cotoneaster bullatus': [new ol.style.Style({
        image: new ol.style.Circle({
            fill: new ol.style.Fill({ color: [255,0,0,1] }),
            stroke: new ol.style.Stroke({ color: [0,0,0,1] }),
            radius: 7
        })
    })],
    // Sprikemispel = dyp oransje
    'cotoneaster divaricata': [new ol.style.Style({
        image: new ol.style.Circle({
            fill: new ol.style.Fill({ color: [239,133,19,1] }),
            stroke: new ol.style.Stroke({ color: [0,0,0,1] }),
            radius: 7
        })
    })],
    // Pilemispel = gul
    'cotoneaster salicifolia': [new ol.style.Style({
        image: new ol.style.Circle({
            fill: new ol.style.Fill({ color: [239,236,19,1] }),
            stroke: new ol.style.Stroke({ color: [0,0,0,1] }),
            radius: 7
        })
    })],
    // Eple = grønn
    'malus domestica': [new ol.style.Style({
        image: new ol.style.Circle({
            fill: new ol.style.Fill({ color: [0,255,0,1] }),
            stroke: new ol.style.Stroke({ color: [0,0,0,1] }),
            radius: 7
        })
    })],
    // Pære = grågrønn
    'pyrus communis': [new ol.style.Style({
        image: new ol.style.Circle({
            fill: new ol.style.Fill({ color: [122,175,131,1] }),
            stroke: new ol.style.Stroke({ color: [0,0,0,1] }),
            radius: 7
        })
    })],
    // Planteriket = blå 
    'plantae': [new ol.style.Style({
        image: new ol.style.Circle({
            fill: new ol.style.Fill({ color: [0,0,255,1] }),
            stroke: new ol.style.Stroke({ color: [0,0,0,1] }),
            radius: 7
        })
    })]
};
  
  featureOverlay = new ol.layer.Vector({
    source: new ol.source.Vector({
      features: features
    }),
    style: function(feature, resolution){
        if(feature.get("cropOrganism") != null && feature.get("cropOrganism")["latinName"] != null)
        {
            return styles[feature.get("cropOrganism")["latinName"].toLowerCase()];
        }
        else 
        {
            return styles["plantae"];
        }
    }
            
  });
  
  newFeatureOverlay = new ol.layer.Vector({
    source: new ol.source.Vector({
      features: new ol.Collection()
    }),
    style: [new ol.style.Style({
        image: new ol.style.Circle({
            fill: new ol.style.Fill({ color: [255,255,255,1] }),
            stroke: new ol.style.Stroke({ color: [0,0,0,1] }),
            radius: 10
        })
    })]
            
  });
  
  
  
  map = new ol.Map({
        target: 'map',
        layers: [
          topo2graatone,
          featureOverlay,
          newFeatureOverlay
        ],
        view: new ol.View({
          center: ol.proj.fromLonLat([8.5, 60.8]),
          zoom: 6
        })
      });
    
    // TODO pestId and from/to can't be hard coded
    // TODO feature properties must be synchronized
    $.getJSON("/rest/observation/filter/1/geoJSON?from=2016-01-01&to=2017-01-01&pestId=" + paerebrann.organismId, function(geoData){
        //console.info(geoData)
        var format = new ol.format.GeoJSON();

        var drawnfeatures = format.readFeatures(geoData, {
          //dataProjection: "EPSG:32633",
          dataProjection: "EPSG:4326",
          featureProjection: map.getView().getProjection().getCode()
        });
        //featureOverlay.clear(true);
        featureOverlay.getSource().addFeatures(drawnfeatures);
    });
    
    map.on('click', function(evt){
        //features = []
        var feature = map.forEachFeatureAtPixel(
            evt.pixel, function(ft, l) { return ft; }
        );

        //console.info(features);
        if (feature) {
            //console.info(feature);
            //you can see all properties with getProperties()
            //console.info(feature.getProperties());
            displayFeature(feature);
        }
        else
        {
            //console.log("Attempt to create new observation at " + evt.pixel);
            var vectorSource = newFeatureOverlay.getSource();
            // Remove any new features already created
            vectorSource.clear();
            //console.info(map.getCoordinateFromPixel(evt.pixel));
            var newFeature = createFeature(map.getCoordinateFromPixel(evt.pixel));
            //console.info(newFeature);
            vectorSource.addFeature(newFeature);
            editFeature(newFeature.getId());
        }
    });
    
}

var createFeature = function(coordinate)
{
    if(coordinate.length == 2)
    {
        coordinate = [coordinate[0],coordinate[1],0];
    }
    var point = new ol.geom.Point(coordinate);
    var newFeature = new ol.Feature({
        name: "Ny observasjon",
        geometry: point
    });
    newFeature.setId(-1);
    newFeature.setProperties({
            "observationId": -1,
            "observationData": "{\"symptom\":\"\",\"tiltak\":\"\",\"forekomststorrelse\":\"\"}",
            "cropOrganism": {},
            "observationText" : "",
            "timeOfObservation": moment().valueOf()
        });
    
    return newFeature;
}

var displayFeature = function(feature)
{
    var featureForm = document.getElementById("featureForm");
    
    var observationData = JSON.parse(feature.get("observationData"));
    var timeOfObservation = new moment(feature.get("timeOfObservation"));
    var html = [
        '<button type="button" onclick="unFocusForm()">X</button>',
        '<button type="button" onclick="editFeature(\'', feature.getId() ,'\');">Edit</button>',
        '<button type="button" onclick="deleteFeature(' + feature.getId() + ')">Delete</button>',
        '<h3>Registrering</h3>',
        '<table>',
        '<tr><td>Type</td><td>',getLocalizedOrganismName(feature.get("cropOrganism")),'</td></tr>',
        '<tr><td>Størrelse</td><td>',observationData["forekomststorrelse"],'</td></tr>',
        '<tr><td>Symptom</td><td>',observationData["symptom"],'</td></tr>',
        '<tr><td>Tiltak</td><td>',observationData["tiltak"],'</td></tr>',
        '<tr><td>Beskrivelse</td><td>',feature.get("observationText"),'</td></tr>',
        '<tr><td>Dato</td><td>',timeOfObservation.format("DD.MM.YYYY"),'</td></tr>',
        '</table>'
    ];
    featureForm.innerHTML = html.join("");
    focusForm();
}

var forekomsttypeLatinskeNavn = [
    "Cotoneaster bullatus", //"Bulkemispel", 
    "Malus domestica", //"Eple", 
    "Pyrus communis", //"Pære", 
    "Cotoneaster salicifolia", //"Pilemispel", 
    "Cotoneaster divaricata", //"Sprikemispel", 
    "Plantae" // Planteriket (Annet)
];

var forekomsttyper = [];
var paerebrann = {};

function initForekomsttyper()
{
    $.getJSON("/rest/organism/search/latinnames?keywords=" + forekomsttypeLatinskeNavn.join(","), function(data){
       forekomsttyper = data; 
    });
}

function initPaerebrann(){
    $.getJSON("/rest/organism/search/latinnames?keywords=Erwinia amylovora", function(data){
       if(data.length == 1)
       {
           paerebrann = data[0];
           initMap();
       }
    });
}

var getCropOrganism = function(organismId)
{
    for(var i=0;i<forekomsttyper.length;i++)
    {
        if(forekomsttyper[i].organismId == organismId)
        {
            return forekomsttyper[i];
        }
    }
}

var forekomststorrelses = ["Ikke bestemt", "1 plante", "10 planter", "100 planter", "Mer enn 100 planter"];
var symptoms = ["Ikke symptom", "Symptom"];
var tiltaks = ["Ikke ryddet", "Ryddet"];


var editFeature = function(featureId)
{
    var feature = featureId > 0 ? featureOverlay.getSource().getFeatureById(featureId)
                                    : newFeatureOverlay.getSource().getFeatureById(featureId);
    var observationData = JSON.parse(feature.get("observationData"));
    var timeOfObservation = new moment(feature.get("timeOfObservation"));
    var featureForm = document.getElementById("featureForm");
    var html = 
        '<button type="button" onclick="unFocusForm()" title="Avbryt">X</button>' +
        (featureId > 0 ? '<button type="button" onclick="deleteFeature(' + featureId + ')">Delete</button>' : '') +
        '<h3>' + (featureId > 0 ? "R" : "Ny r") + 'egistrering</h3>' +
        '<table>' +
        '<tr><td>Type</td><td>' +
        generateCropSelect("forekomsttype", forekomsttyper, feature.get("cropOrganism")["organismId"]) +           
        '</td></tr>' +
        '<tr><td>Størrelse</td><td>' +
        generateSelect("forekomststorrelse", forekomststorrelses, observationData["forekomststorrelse"]) +           
        '</td></tr>' +
        '<tr><td>Symptom</td><td>' +
        generateSelect("symptom", symptoms, observationData["symptom"]) + 
        '</td></tr>' +
        '<tr><td>Tiltak</td><td>' + 
        generateSelect ("tiltak", tiltaks, observationData["tiltak"]) + 
        '</td></tr>' +
        '<tr><td>Beskrivelse</td><td>' + 
        '<textarea id="beskrivelse" name="beskrivelse">' + (feature.get("observationText") != null ? feature.get("observationText") : "")  + '</textarea>' +
        '</td></tr>' +
        '<tr><td>Dato</td><td>' +
        '<input type="text" id="dato" name="dato" value="'+ timeOfObservation.format("DD.MM.YYYY") + '"/></td></tr>' +
        '<tr><td></td><td>' +
        '<input type="submit" value="Lagre" onclick="storeFeature(' + feature.getId() + ');"/></td></tr>' +
        '</table>';
        
    
    featureForm.innerHTML = html;
    focusForm();
    //console.info(feature);
};

var storeFeature = function(featureId)
{
    var feature = featureId > 0 ? featureOverlay.getSource().getFeatureById(featureId)
                                    : newFeatureOverlay.getSource().getFeatureById(featureId);
    
    // Store, clear newFeature layer
    // Need to add feature as payload
    var format = new ol.format.GeoJSON();
    
    // Add the form data
    var cropOrganism = getCropOrganism(document.getElementById("forekomsttype").options[document.getElementById("forekomsttype").options.selectedIndex].value);
    //console.info(cropOrganism);
    var forekomststorrelse = document.getElementById("forekomststorrelse").options[document.getElementById("forekomststorrelse").options.selectedIndex].value;
    var symptom = document.getElementById("symptom").options[document.getElementById("symptom").options.selectedIndex].value;
    var tiltak = document.getElementById("tiltak").options[document.getElementById("tiltak").options.selectedIndex].value;
    var observationText = document.getElementById("beskrivelse").value;
    var observationHeading = "Registrering av pærebrann";
    var timeOfObservation = moment(document.getElementById("dato").value + "+0200","DD.MM.YYYYZ").valueOf();
    
    feature.setProperties({
       timeOfObservation: timeOfObservation,
       cropOrganism: cropOrganism,
       organism: paerebrann,
       observationHeading: observationHeading,
       observationText: observationText,
       observationData: "{\"symptom\":\"" + symptom + "\",\"tiltak\":\"" + tiltak + "\",\"forekomststorrelse\":\"" + forekomststorrelse + "\"}",
       statusTypeId: 3,
       statusRemarks: "Registrert via pærebrannovervåkningskartet",
       isQuantified: true,
       broadcastMessage: false
    });
    var result = format.writeFeatures([feature], {
        dataProjection: 'EPSG:4326',
        featureProjection: map.getView().getProjection().getCode()
      });
    
    //console.log(feature);
    
    $.ajax({
        type: "POST",
        url: "/rest/observation/gisobservation",
        // The key needs to match your method's input parameter (case-sensitive).
        data: result,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function(geoData){
            //console.info(geoData)
            var format = new ol.format.GeoJSON();

            var drawnfeatures = format.readFeatures(geoData, {
              //dataProjection: "EPSG:32633",
              dataProjection: "EPSG:4326",
              featureProjection: map.getView().getProjection().getCode()
            });
            newFeatureOverlay.getSource().clear(true);
            // If storing an existing feature, remove the one
            // that was there before storing, since the returned
            // one has a new gisId (featureId)
            if(featureId > 0)
            {
                featureOverlay.getSource().removeFeature(feature);
            }
            featureOverlay.getSource().addFeatures(drawnfeatures);
            unFocusForm();
        },
        failure: function(errMsg) {
            alert(errMsg);
        }
    });
    
    
}

/**
 * Delete an existing feature
 * @param {type} featureId
 * @returns {undefined}
 */
var deleteFeature = function(featureId)
{
    if(!confirm("Er du sikker på at du vil slette?"))
    {
        return;
    }
    
    var feature = featureOverlay.getSource().getFeatureById(featureId);
    
    $.ajax({
        type: "DELETE",
        url: "/rest/observation/gisobservation/" + feature.getId(),
        success: function(response){
            console.info(response);
            // If storing an existing feature, remove the one
            // that was there before storing, since the returned
            // one has a new gisId (featureId)
            if(featureId > 0)
            {
                featureOverlay.getSource().removeFeature(feature);
            }
            unFocusForm();
        },
        failure: function(errMsg) {
            alert(errMsg);
        }
    });
}

var generateSelect = function(selectName, options, preselect)
{
    var retVal = '<select id="' + selectName + '" name="' + selectName + '">';
    for(var i=0; i< options.length; i++)
    {
        retVal += '<option value="' + options[i] + '"' + (options[i] == preselect ? " selected=\"selected\"" : "") + '">' + options[i]  + '</option>';
    }
    retVal += '</select>';
    return retVal;
}

var generateCropSelect = function(selectName, cropOrganisms, preselect)
{
    var retVal = '<select id="' + selectName + '" name="' + selectName + '">';
    for(var i=0; i< cropOrganisms.length; i++)
    {
        retVal += '<option value="' + cropOrganisms[i].organismId + '"' + (cropOrganisms[i].organismId == preselect ? " selected=\"selected\"" : "") + '">' + getLocalizedOrganismName(cropOrganisms[i])  + '</option>';
    }
    retVal += '</select>';
    return retVal;
}

var focusForm = function()
{
    var featureForm = document.getElementById("featureForm");
    featureForm.style.display = "block";
}

var unFocusForm = function()
{
    var featureForm = document.getElementById("featureForm");
    featureForm.style.display = "none";
    // Also remove feature (if one) on the New feature overlay
    newFeatureOverlay.getSource().clear();
}

var navigateTo = function(center)
{
    var centerPosition = ol.proj.transform(center, 'EPSG:4326', 'EPSG:3857');
        view = new ol.View({
        center: centerPosition,
        zoom: 12
      });

      map.setView(view);
   
    var searchResultsEl = document.getElementById("searchResults");
    searchResultsEl.innerHTML = "";
    searchResultsEl.style.display="none";
};

function navToLocation() {
    if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function(geopositionObj){
                navigateTo([geopositionObj.coords.longitude, geopositionObj.coords.latitude]);
                }
            );
        } else {
            alert( "Geolocation is not supported by this browser.");
        }
}
        
        

