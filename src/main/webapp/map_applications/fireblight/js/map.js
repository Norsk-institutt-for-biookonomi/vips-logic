/*
 * Copyright (c) 2017-2023 NIBIO <http://www.nibio.no/>. 
 *
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
 * 
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */

// The globally available map objects
var map, featureOverlay, newFeatureOverlay, nurseryPoisOverlay, apiaryLayer, zoneLayer;//, apiaryPoisOverlay;

// Override localization settings for this particular web page
var hardcodedLanguage = "nb";

// If false, touching map will not create a new item
var registration = false;
var geolocation;

/**
 * Initializes the map with all its layers
 * @returns {undefined}
 */
async function initMap()
{

    var features = new ol.Collection();

    // Icon styling for the observations layer
    var iconRadius = 10;
    var styles = {
        // Bulkemispel = rød
        'cotoneaster bullatus': [new ol.style.Style({
                image: new ol.style.Circle({
                    fill: new ol.style.Fill({color: [255, 0, 0, 1]}),
                    stroke: new ol.style.Stroke({width: 1, color: [0, 0, 0, 1]}),
                    radius: iconRadius
                })
            })],
        // Sprikemispel = dyp oransje
        'cotoneaster divaricata': [new ol.style.Style({
                image: new ol.style.Circle({
                    fill: new ol.style.Fill({color: [239, 133, 19, 1]}),
                    stroke: new ol.style.Stroke({width: 1, color: [0, 0, 0, 1]}),
                    radius: iconRadius
                })
            })],
        // Pilemispel = gul
        'cotoneaster salicifolia': [new ol.style.Style({
                image: new ol.style.Circle({
                    fill: new ol.style.Fill({color: [239, 236, 19, 1]}),
                    stroke: new ol.style.Stroke({width: 1, color: [0, 0, 0, 1]}),
                    radius: iconRadius
                })
            })],
        // Eple = grønn
        'malus domestica': [new ol.style.Style({
                image: new ol.style.Circle({
                    fill: new ol.style.Fill({color: [0, 255, 0, 1]}),
                    stroke: new ol.style.Stroke({width: 1, color: [0, 0, 0, 1]}),
                    radius: iconRadius
                })
            })],
        // Pære = grågrønn
        'pyrus communis': [new ol.style.Style({
                image: new ol.style.Circle({
                    fill: new ol.style.Fill({color: [122, 175, 131, 1]}),
                    stroke: new ol.style.Stroke({width: 1, color: [0, 0, 0, 1]}),
                    radius: iconRadius
                })
            })],
        // Planteriket = blå 
        'plantae': [new ol.style.Style({
                image: new ol.style.Circle({
                    fill: new ol.style.Fill({color: [0, 0, 255, 1]}),
                    stroke: new ol.style.Stroke({width: 1, color: [0, 0, 0, 1]}),
                    radius: iconRadius
                })
            })
        ]
    };

    // Initialize the layer for observations. Empty features array at first
    featureOverlay = new ol.layer.Vector({
        source: new ol.source.Vector({
            features: features
        }),
        style: function (feature, resolution) {
            // Site that has been cleared is all black
            var observationData = JSON.parse(feature.get("observationData"));
            if (observationData["tiltak"] == "Ryddet" && observationData["symptom"] == "Ikke symptom")
            {
                return [new ol.style.Style({
                        image: new ol.style.Circle({
                            fill: new ol.style.Fill({color: [0, 0, 0, 1]}),
                            stroke: new ol.style.Stroke({color: [0, 0, 0, 1]}),
                            radius: iconRadius
                        })
                    })];
            } else if (observationData["tiltak"] == "Ryddet" && observationData["symptom"] == "Symptom")
            {
                return [new ol.style.Style({
                        image: new ol.style.Circle({
                            fill: new ol.style.Fill({color: [255, 255, 255, 1]}),
                            stroke: new ol.style.Stroke({width: 8, color: [0, 0, 0, 1]}),
                            radius: iconRadius
                        })
                    })];
            }

            var retVal = null;
            if (feature.get("cropOrganism") != null && feature.get("cropOrganism")["latinName"] != null)
            {
                retVal = styles[feature.get("cropOrganism")["latinName"].toLowerCase()];
            } else
            {
                retVal = styles["plantae"];
            }
            //console.info(retVal[0].getImage().getStroke().getWidth());
            // If symptom has been registered, mark with inner black dot
            if (observationData["symptom"] == "Symptom")
            {
                retVal = [
                    new ol.style.Style({
                        image: new ol.style.Circle({
                            fill: new ol.style.Fill({color: [0, 0, 0, 1]}),
                            stroke: new ol.style.Stroke({width: 8, color: retVal[0].getImage().getFill().getColor()}),
                            radius: iconRadius
                        })
                    })
                ];
            }
            return retVal;
        }

    });

    // When user registers a new observation, this layer is used to place the marker
    newFeatureOverlay = new ol.layer.Vector({
        source: new ol.source.Vector({
            features: new ol.Collection()
        }),
        style: [new ol.style.Style({
                image: new ol.style.Circle({
                    fill: new ol.style.Fill({color: [255, 255, 255, 1]}),
                    stroke: new ol.style.Stroke({color: [0, 0, 0, 1], width: 3, lineDash: [2, 2]}),
                    radius: 10
                })
            })]

    });



    // Get the background layer for Norway from Statens kartverk
    // TODO: Use await
    var parser = new ol.format.WMTSCapabilities();
    fetch('https://cache.kartverket.no/v1/wmts/1.0.0/WMTSCapabilities.xml').then(function (response) {
        return response.text();
    }).then(function (text) {
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
            

        nurseryPoisOverlay = new ol.layer.Vector({
            source: new ol.source.Vector({
                features: new ol.Collection()
            }),
            style: diseaseSpreadingPoiStyle
        });

        // Get Mattilsynet's layer for apiary sites
        let apiarySource = new ol.source.ImageWMS({
            url: 'https://kart.mattilsynet.no/wmscache/service',
            params: {"LAYERS": "Mattilsynet_Bigaardsplasser"},
            ratio: 1,
            projection: ol.proj.get("EPSG:25833")
        });

        apiaryLayer = new ol.layer.Image({
            source: apiarySource,
            visible: true,
            opacity: 1.0
        });

        // Get the zone map, produced by geomatikkseksjonen from information from Mattilsynet
        let zoneSource = new ol.source.ImageWMS({
            url: 'https://wms.nibio.no/cgi-bin/parebrann',
            //params: {"LAYERS": "parebrann"},
            params: {"LAYERS": "Parebrann_2025"},
            ratio: 1,
            projection: ol.proj.get("EPSG:25833")
        });

        zoneLayer = new ol.layer.Image({
            source: zoneSource,
            visible: false,
            opacity: 0.65
        });

        map = new ol.Map({
            target: 'map',
            controls: ol.control.defaults({attribution: false}), // Hide the attribution
            layers: [
                topo4,
                zoneLayer,
                nurseryPoisOverlay,
                apiaryLayer,
                featureOverlay,
                newFeatureOverlay
            ],
            view: new ol.View({
                center: ol.proj.fromLonLat([8.5, 60.8]),
                zoom: 6
            })
        });

        // Configure geolocation tracker
        geolocation = new ol.Geolocation({
            trackingOptions: {
                enableHighAccuracy: true
            },
            projection: map.getView().getProjection()
        });
        var positionFeature = new ol.Feature();
        positionFeature.setStyle(
                new ol.style.Style({
                    image: new ol.style.Circle({
                        radius: 6,
                        fill: new ol.style.Fill({
                            color: '#3399CC',
                        }),
                        stroke: new ol.style.Stroke({
                            color: '#fff',
                            width: 2,
                        }),
                    }),
                })
                );

        geolocation.on('change:position', function () {
            var coordinates = geolocation.getPosition();
            positionFeature.setGeometry(coordinates ? new ol.geom.Point(coordinates) : null);
        });

        new ol.layer.Vector({
            map: map,
            source: new ol.source.Vector({
                features: [positionFeature]
            })
        });

        // TODO feature properties must be synchronized
        var lastYear = new Date().getFullYear() - 1;
        // Population the season select list, setting last year as default selected
        initSeasonSelectList(lastYear);
        getAndRenderDiseaseSpreadingPois();
        getAndRenderObservations(lastYear);

        // Handle user click on map
        map.on('click', (evt) => {
            //features = []
            var feature = map.forEachFeatureAtPixel(
                    evt.pixel, function (ft, l) {
                        return ft;
                    }
            );

            var vectorSource = newFeatureOverlay.getSource();
            // Remove any new features already created
            vectorSource.clear();

            if (feature && feature.getProperties()["observationId"] != undefined) {
                // Create a fake icon for highlighting
                var fakeFeature = createFeature(feature.getGeometry().getCoordinates());
                vectorSource.addFeature(fakeFeature);
                displayFeature(feature);
            } else if (registration)
            {
                var newFeature = createFeature(map.getCoordinateFromPixel(evt.pixel));
                vectorSource.addFeature(newFeature);
                editFeature(newFeature.getId());
            }
        });

    }); // END FETCH

}

let openLayersDefaultStyle = undefined;

function getOpenLayersDefaultStyle()
{
    if (openLayersDefaultStyle == undefined)
    {
        var fill = new ol.style.Fill({
            color: 'rgba(255,255,255,0.4)'
        });
        var stroke = new ol.style.Stroke({
            color: '#3399CC',
            width: 1.25
        });
        openLayersDefaultStyle = [
            new ol.style.Style({
                image: new ol.style.Circle({
                    fill: fill,
                    stroke: stroke,
                    radius: 5
                }),
                fill: fill,
                stroke: stroke
            })
        ];
    }

    return openLayersDefaultStyle;
}

/**
 * Styling the different disease spreading pois, e.g. Nurseries
 * @param {ol.Feature} feature
 * @param {Number} resolution
 * @returns {Array}
 */
function diseaseSpreadingPoiStyle(feature, resolution)
{
    if (feature.getProperties()["pointOfInterestTypeId"] == "7") // Nursery
    {
        if (feature.getGeometry().getType() == "Point")
        {
            const innerRadiusInMeters = 1000;
            const outerRadiusInMeters = 2000;
            const viewProjection = map.getView().getProjection();
            const coordsInViewProjection = feature.getGeometry().getCoordinates();
            const longLat = ol.proj.toLonLat(coordsInViewProjection, viewProjection);
            const latitude_rad = longLat[1] * Math.PI / 180;
            const innerCircle = new ol.style.Style({
                image: new ol.style.Circle({
                    fill: new ol.style.Fill({color: [255, 127, 127, 1.0]}),
                    stroke: new ol.style.Stroke({color: [0, 0, 0, 1], width: 3, lineDash: [5, 10], lineCap: "square"}),
                    radius: innerRadiusInMeters / (resolution / viewProjection.getMetersPerUnit() * Math.cos(latitude_rad))
                })
            });
            const outerCircle = new ol.style.Style({
                image: new ol.style.Circle({
                    fill: new ol.style.Fill({color: [255, 127, 127, 0.5]}),
                    stroke: new ol.style.Stroke({color: [0, 0, 0, 1], width: 3, lineDash: [5, 10], lineCap: "square"}),
                    radius: outerRadiusInMeters / (resolution / viewProjection.getMetersPerUnit() * Math.cos(latitude_rad))
                })
            });
            const icon = new ol.style.Style({
                image: new ol.style.Icon({
                    src: 'icons/plant-clipart.png',
                    scale: 8 / resolution
                })
            });

            return [outerCircle, innerCircle, icon];
        }
    } else if (feature.getProperties()["pointOfInterestTypeId"] == "6") // Apiary site - not in use
    {
        return [new ol.style.Style({
                image: new ol.style.Icon({
                    src: 'icons/bees-icon-25.png',
                    scale: 0.2,
                    anchor: [0.3, 1]
                })
            })];

    }

    return getOpenLayersDefaultStyle();
}

// Collect disease spreading pois (currently only apiary sites) from observation api and render them
function getAndRenderDiseaseSpreadingPois()
{
    // Nurseries
    fetch("/rest/poi/organization/1/type/geojson?poiTypes=7")
            .then(response => response.json())
            .then(data => {
                let olFeatures = (new ol.format.GeoJSON()).readFeatures(
                        data,
                        {
                            dataProjection: "EPSG:4326",
                            featureProjection: map.getView().getProjection().getCode()
                        }
                );

                //sconsole.info(olFeatures);
                //console.info(diseaseSpreadingPoisOverlay.getSource());
                nurseryPoisOverlay.getSource().clear();
                nurseryPoisOverlay.getSource().addFeatures(olFeatures);

            });
}

/**
 * 
 * @param {type} fromSeason
 * @returns {undefined}
 */
function getAndRenderObservations(fromSeason)
{
    //console.info("getAndRenderObservations(" + season + ")");
    $.getJSON("/rest/observation/filter/1/geoJSON?from=" + fromSeason + "-01-01&pestId=" + paerebrann.organismId, function (geoData) {
        //console.info(geoData)
        var format = new ol.format.GeoJSON();

        var drawnfeatures = format.readFeatures(geoData, {
            //dataProjection: "EPSG:32633",
            dataProjection: "EPSG:4326",
            featureProjection: map.getView().getProjection().getCode()
        });
        featureOverlay.getSource().clear();
        featureOverlay.getSource().addFeatures(drawnfeatures);
    });
}


/**
 * 
 * @param {type} fromSeason
 *  @param {type} countyNo
 * @returns {undefined}
 */
function getAndRenderObservationsForReport(fromSeason, countyNo = "-1")
{
    //console.info("getAndRenderObservations(" + season + ")");
    $.getJSON("/rest/observation/filter/1/geoJSON?from=" + fromSeason + "-01-01&pestId=" + paerebrann.organismId, function (geoData) {
        //console.info(geoData)
        // Filter by county
        $.getJSON("/corsproxy/https://ws.geonorge.no/kommuneinfo/v1/fylker/" + countyNo + "/omrade")
                .always(function (serviceResponse) {
                    //console.info(geoData);
                    //console.info(serviceResponse);

                    if (parseInt(countyNo) > 0)
                    {
                        var filteredFeatures = [];
                        for (var i = 0; i < geoData.features.length; i++)
                        {
                            var featureToBeFiltered = geoData.features[i];
                            coordinate = proj4("EPSG:4326", "EPSG:4258", [featureToBeFiltered.geometry.coordinates[0], featureToBeFiltered.geometry.coordinates[1]]);
                            // For some weird reason, d3 returns NOT contains in our case
                            if (!d3.geoContains(serviceResponse.omrade, coordinate))
                            {
                                //console.info(featureToBeFiltered);
                                filteredFeatures.push(featureToBeFiltered);
                            }
                            //console.info(featureToBeFiltered);
                        }
                        //console.info(filteredFeatures);
                        geoData.features = filteredFeatures;
                    }

                    var format = new ol.format.GeoJSON();

                    var drawnfeatures = format.readFeatures(geoData, {
                        //dataProjection: "EPSG:32633",
                        dataProjection: "EPSG:4326",
                        featureProjection: map.getView().getProjection().getCode()
                    });
                    featureOverlay.getSource().clear();
                    featureOverlay.getSource().addFeatures(drawnfeatures);
                }
                );
    });
}

function toggleRegistration(theButton)
{
    if (registration)
    {
        theButton.title = "Registrering er AV";
        theButton.style.color = "white";
    } else
    {
        theButton.title = "Registrering er PÅ";
        theButton.style.color = "red";
    }
    registration = !registration;
}

function toggleTracking(theButton)
{
    geolocation.setTracking(!geolocation.getTracking());
    theButton.style.backgroundColor = geolocation.getTracking() ? "green" : "rgba(0,60,136,.5)";
    theButton.title = geolocation.getTracking() ? "Vis min posisjon er PÅ" : "Vis min posisjon er AV";
}

/**
 * Creates a new feature
 * @param {type} coordinate
 * @returns {createFeature.newFeature|ol.Feature}
 */
var createFeature = function (coordinate)
{
    if (coordinate.length == 2)
    {
        coordinate = [coordinate[0], coordinate[1], 0];
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
        "observationText": "",
        "timeOfObservation": moment().valueOf()
    });

    return newFeature;
}

var displayFeature = function (feature)
{
    var featureForm = document.getElementById("featureForm");

    var observationData = JSON.parse(feature.get("observationData"));
    var timeOfObservation = new moment(feature.get("timeOfObservation"));
    var html = [
        '<button type="button" onclick="unFocusForm()">X</button>',
        '<button type="button" onclick="editFeature(\'', feature.getId(), '\');">Rediger</button>',
        '<button type="button" onclick="deleteFeature(' + feature.getId() + ')">Slett</button>',
        '<h3>Registrering</h3>',
        '<table>',
        '<tr><td>Type</td><td>', getLocalizedOrganismName(feature.get("cropOrganism"), hardcodedLanguage), '</td></tr>',
        '<tr><td>Størrelse</td><td>', observationData["forekomststorrelse"], '</td></tr>',
        '<tr><td>Symptom</td><td>', observationData["symptom"], '</td></tr>',
        '<tr><td>Tiltak</td><td>', observationData["tiltak"], '</td></tr>',
        '<tr><td>Beskrivelse</td><td>', feature.get("observationText"), '</td></tr>',
        '<tr><td>Dato</td><td>', timeOfObservation.format("DD.MM.YYYY"), '</td></tr>',
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
    $.getJSON("/rest/organism/search/latinnames?keywords=" + forekomsttypeLatinskeNavn.join(","), function (data) {
        forekomsttyper = data;
    });
}

function initPaerebrann() {
    $.getJSON("/rest/organism/search/latinnames?keywords=Erwinia amylovora", function (data) {
        if (data.length == 1)
        {
            paerebrann = data[0];
            initMap();
        }
    });
}

var getCropOrganism = function (organismId)
{
    for (var i = 0; i < forekomsttyper.length; i++)
    {
        if (forekomsttyper[i].organismId == organismId)
        {
            return forekomsttyper[i];
        }
    }
}

var forekomststorrelses = ["Ikke bestemt", "1 plante", "10 planter", "100 planter", "Mer enn 100 planter"];
var symptoms = ["Ikke symptom", "Symptom"];
var tiltaks = ["Ikke ryddet", "Ryddet"];


var editFeature = function (featureId)
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
            generateSelect("tiltak", tiltaks, observationData["tiltak"]) +
            '</td></tr>' +
            '<tr><td>Beskrivelse</td><td>' +
            '<textarea id="beskrivelse" name="beskrivelse">' + (feature.get("observationText") != null ? feature.get("observationText") : "") + '</textarea>' +
            '</td></tr>' +
            '<tr><td>Dato</td><td>' +
            '<input type="text" id="dato" name="dato" size="10" value="' + timeOfObservation.format("DD.MM.YYYY") + '"/></td></tr>' +
            '<tr><td></td><td>' +
            '<input type="submit" value="Lagre" onclick="storeFeature(' + feature.getId() + ');"/></td></tr>' +
            '</table>';


    featureForm.innerHTML = html;
    focusForm();
    //console.info(feature);
};

var storeFeature = function (featureId)
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
    var timeOfObservation = moment(document.getElementById("dato").value + "+0200", "DD.MM.YYYYZ");
    if (timeOfObservation.year() < 2000)
    {
        alert("Feil dato (før år 2000). Datoformat er DD.MM.ÅÅÅÅ");
        return;
    }

    feature.setProperties({
        timeOfObservation: timeOfObservation.valueOf(),
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
        success: function (geoData) {
            //console.info(geoData)
            var format = new ol.format.GeoJSON();

            var drawnfeatures = format.readFeatures(geoData, {
                dataProjection: "EPSG:4326",
                featureProjection: map.getView().getProjection().getCode()
            });
            newFeatureOverlay.getSource().clear(true);
            // If storing an existing feature, remove the one
            // that was there before storing, since the returned
            // one has a new gisId (featureId)
            if (featureId > 0)
            {
                featureOverlay.getSource().removeFeature(feature);
            }
            featureOverlay.getSource().addFeatures(drawnfeatures);
            unFocusForm();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 401)
            {
                if (confirm("Kan ikke lagre fordi du er logget ut av applikasjonen. Klikk OK for å logge inn."))
                {
                    window.location.reload();
                }
            } else
            {
                alert("Beklager, en feil oppsto. Status = " + jqXHR.status + ", eventuell feilmelding: " + textStatus);
            }
        }
    });


}

/**
 * Delete an existing feature
 * @param {type} featureId
 * @returns {undefined}
 */
var deleteFeature = function (featureId)
{
    if (!confirm("Er du sikker på at du vil slette?"))
    {
        return;
    }

    var feature = featureOverlay.getSource().getFeatureById(featureId);

    $.ajax({
        type: "DELETE",
        url: "/rest/observation/gisobservation/" + feature.getId(),
        success: function (response) {
            //console.info(response);
            // If storing an existing feature, remove the one
            // that was there before storing, since the returned
            // one has a new gisId (featureId)
            if (featureId > 0)
            {
                featureOverlay.getSource().removeFeature(feature);
            }
            unFocusForm();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            if (jqXHR.status == 401)
            {
                if (confirm("Kan ikke slette fordi du er logget ut av applikasjonen. Klikk OK for å logge inn."))
                {
                    window.location.reload();
                }
            } else
            {
                alert("Beklager, en feil oppsto. Status = " + jqXHR.status + ", eventuell feilmelding: " + textStatus);
            }
        }
    });
}

var generateSelect = function (selectName, options, preselect)
{
    var retVal = '<select id="' + selectName + '" name="' + selectName + '">';
    for (var i = 0; i < options.length; i++)
    {
        retVal += '<option value="' + options[i] + '"' + (options[i] == preselect ? " selected=\"selected\"" : "") + '">' + options[i] + '</option>';
    }
    retVal += '</select>';
    return retVal;
}

var generateCropSelect = function (selectName, cropOrganisms, preselect)
{
    var retVal = '<select id="' + selectName + '" name="' + selectName + '">';
    for (var c = 0; c < forekomsttypeLatinskeNavn.length; c++)
    {
        currentLatinName = forekomsttypeLatinskeNavn[c];
        for (var i = 0; i < cropOrganisms.length; i++)
        {
            if (cropOrganisms[i].latinName == currentLatinName)
            {
                retVal += '<option value="' + cropOrganisms[i].organismId + '"' + (cropOrganisms[i].organismId == preselect ? " selected=\"selected\"" : "") + '">' +
                        (currentLatinName == "Plantae" ? "Annet" : getLocalizedOrganismName(cropOrganisms[i], hardcodedLanguage))
                        + '</option>';
            }
        }
    }
    retVal += '</select>';
    return retVal;
}

var focusForm = function ()
{
    var featureForm = document.getElementById("featureForm");
    featureForm.style.display = "block";
}

var unFocusForm = function ()
{
    var featureForm = document.getElementById("featureForm");
    featureForm.style.display = "none";
    // Also remove feature (if one) on the New feature overlay
    newFeatureOverlay.getSource().clear();
}

var navigateTo = function (center)
{
    var centerPosition = ol.proj.transform(center, 'EPSG:4326', 'EPSG:3857');
    view = new ol.View({
        center: centerPosition,
        zoom: 16
    });

    map.setView(view);

    var searchResultsEl = document.getElementById("searchResults");
    searchResultsEl.innerHTML = "";
    searchResultsEl.style.display = "none";
};

/**
 * Uses the client's geolocation information - displays it on the map
 */
function showLocation() {
    if (navigator.geolocation) {
        if (window.location.protocol === "http:")
        {
            navigator.geolocation.getCurrentPosition(function (geopositionObj) {
                // TODO: position and display location icon
            }
            );
        } else
        {
            if (confirm("Lokalisering fungerer bare over https (sikker tilkobling mellom nettleser og tjener). Klikk OK for å gå til sikker tilkobling."))
            {
                window.location = "https:" + window.location.href.substring(window.location.protocol.length);
            }
        }
    } else {
        alert("Lokaliseringsfunksjonen er ikke tilgjengelig i denne nettleseren.");
    }
}

function navToLocation() {
    if (navigator.geolocation) {
        if (window.location.protocol === "https:")
        {
            navigator.geolocation.getCurrentPosition(function (geopositionObj) {
                navigateTo([geopositionObj.coords.longitude, geopositionObj.coords.latitude]);
            }
            );
        } else
        {
            if (confirm("Lokalisering fungerer bare over https (sikker tilkobling mellom nettleser og tjener). Klikk OK for å gå til sikker tilkobling."))
            {
                window.location = "https:" + window.location.href.substring(window.location.protocol.length);
            }
        }
    } else {
        alert("Lokaliseringsfunksjonen er ikke tilgjengelig i denne nettleseren.");
    }
}

/**
 * 
 * @param {type} selectedSeason
 * @returns {undefined}
 */
function initSeasonSelectList(selectedSeason)
{
    // What's the current year?
    var thisYear = new Date().getFullYear();
    // How many years do we go back?
    $.ajax({
        url: "/rest/observation/first/" + paerebrann.organismId
    })
            .done(function (data) {
                var time = moment(data);
                var firstYear = time.toDate().getFullYear();
                // Loop gjennom år, lag valgliste :-)
                var startSeasonList = document.getElementById("startSeason");
                for (var i = firstYear; i <= thisYear; i++)
                {
                    var yearOpt = new Option("", i);
                    yearOpt.innerHTML = i + " &rarr;"
                    if (i == selectedSeason)
                    {
                        yearOpt.selected = true;
                    }
                    startSeasonList.options[startSeasonList.options.length] = yearOpt;
                }
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
                alert(textStatus);
            });

}

