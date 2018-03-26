/* 
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

var allObservations = []; // Populated asynchronously
var drawnFeatures = []; // Populated asynchronously
var currentDate; // Initialized in initMap
var map;
var featureOverlay;

/**
 * Default coloring
 * @type Array
 */
var ageColors = [
    [7, 'rgba(255, 0, 0, 1.0)'], // Red
    [30, 'rgba(244, 206, 66, 1.0)'], // Orange
    [60, 'rgba(0, 0, 255, 1.0)'], // Blue
];

var ageStyles;

/*
 * Observation map
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
var initMap = function(   
            center, 
            zoomLevel, 
            organizationId,
            from,
            to,
            pestId,
            cropId,
            cropCategoryId,
            customAgeColors
        )
{
    if(typeof customAgeColors !== 'undefined')
    {
        ageColors = customAgeColors;
    }
    
    initAgeStyles();
    currentDateInMillis = moment(to).format("X") * 1000; // Reference for coloring the observations
    // Background layer is OpenStreetMap
    var backgroundLayer = new ol.layer.Tile({
                    source: new ol.source.OSM({
                        attributions: [
                            new ol.Attribution({
                              html: mapConstants.MAP_ATTRIBUTION
                            })
                          ]
                    })
    });
    
    // Layer for popup
    var popOverlay = new ol.Overlay({
      element: document.getElementById("popover")
    });
    
    // Creating the map
    map = new ol.Map({
                    target: 'observationMap',
                    layers: [backgroundLayer],
                    overlays: [popOverlay],
                    renderer: 'canvas'
    });
    
    var centerPosition = ol.proj.transform(center, 'EPSG:4326', map.getView().getProjection().getCode());
    
    // Setting zoom and center for the map (need to do this after creating map. so that we kan transform our
    // center to correct map projection)
    var view = new ol.View({
            center: centerPosition,
            zoom:zoomLevel
    });
    map.setView(view);
    
    // Need to build the query string
    var params = [];
    
    if(from !== "")
    {
        params.push("from=" + from);
    }
    if(to !== "")
    {
        params.push("to=" + to);
    }
    if(pestId !== null)
    {
        params.push("pestId=" + pestId);
    }
    if(cropId !== null)
    {
        params.push("cropId=" + cropId);
    }
    if(cropCategoryId !== null)
    {
        params.push("cropCategoryId=" + cropCategoryId);
    }
    
    
    $.getJSON( "/rest/observation/filter/" + organizationId + (params.length > 0 ? "?" + params.join("&") : ""), function( data ) {
        allObservations = data;
        renderObservationTable(data);
        var geoJSON = {"type":"FeatureCollection","features":[]};
        for(var i=0;i<data.length;i++)
        {
            var observation = data[i];
            var obsFeatures = null;
            if(observation.location !== null && observation.location.geoJSON !== null)
            {
                obsFeatures = JSON.parse(observation.location.geoJSON).features;
                // Need to add observation id for these locations
                for(var j=0; j<obsFeatures.length; j++)
                {
                    obsFeatures[j].properties["observationId"] = observation.observationId;
                }
            }
            else if(observation.geoinfo !== null && observation.geoinfo.trim() !== "")
            {
                obsFeatures = JSON.parse(observation.geoinfo).features;
            }
            else
            {
                continue;
            }
            
            for(var j=0; j<obsFeatures.length; j++)
            {
                geoJSON.features.push(obsFeatures[j]);
            }
        }
        var features = new ol.Collection();        
        featureOverlay = new ol.layer.Vector({
		    source: new ol.source.Vector({
		      features: features
		    }),
		    style: getCorrectStyle
		  });
	
	
        var format = new ol.format.GeoJSON();
        drawnfeatures = format.readFeatures(geoJSON, {
          dataProjection: 'EPSG:4326',
          featureProjection: map.getView().getProjection().getCode()
        });
        
        featureOverlay.getSource().addFeatures(drawnfeatures);
        //console.log(featureOverlay);
        featureOverlay.setMap(map);
        if(drawnfeatures.length > 0)
        {
            extent = featureOverlay.getSource().getExtent();
            map.getView().fit(extent, map.getSize());
        }
    });
    
    // Using Bootstrap's popover plugin. See http://getbootstrap.com/javascript/#popovers
    var poiDetails = $("#popover");
    
    var displayFeatureDetails = function(pixel, coordinate) {
        var feature = map.forEachFeatureAtPixel(pixel, function(feature,layer){
           return feature; 
        });

        if (feature) {
            var relAge = getObservationRelativeAge(feature);
            if(relAge == null || relAge < 0)
            {
                return;
            }
            // Position the popup, and hiding it
            // Resetting information from (possible) former popups
            var geometry = feature.getGeometry();
            popOverlay.setPosition(ol.extent.getCenter(geometry.getExtent()));
            // Get the observation that this feature belongs to
            var observation = getObservation(feature.get("observationId"));
            //popOverlay.setPosition(geometry.getCoordinates());
            poiDetails.popover('destroy');
            var illustrationElm = "";
            if(observation.observationIllustrationSet.length == 1)
            {
                    var illustration = observation.observationIllustrationSet[0]; 
                    illustrationElm = "<img src='/static/images/observations/" + observation.organismId + "/" + illustration.observationIllustrationPK.fileName + "' class='img-responsive'/>";
            }
            // Create the popup, showing it
            poiDetails.popover({
                    animation: true,
                    trigger: 'manual',
                    html: true,
                    placement: "auto top",
                    title: "<a href='/observation?action=editObservationForm&observationId=" + observation.observationId + "' target='new'>" + observation.observationHeading + "</a>",
                    content: "[" + moment(observation.timeOfObservation).format("YYYY-MM-DD HH:mm ZZ") + "]: " 
                            + observation.observationText 
                            + illustrationElm
            });
           

          poiDetails.popover('show');


        } else {
            poiDetails.popover('destroy');
        }
    };
    
    map.on('singleclick', function(evt) {
            var pixel = map.getEventPixel(evt.originalEvent);
              displayFeatureDetails(pixel);
    });
    
    
};

var getObservation = function(observationId)
{
    for(var i=0; i<allObservations.length;i++)
    {
        if(allObservations[i].observationId == observationId)
        {
            return allObservations[i];
        }
    }
    return null;
};


var renderObservationTable = function(data)
{
    var tbody = document.getElementById("observationTableBody");
    var tbodyHTML = [];
    for(var i=0; i<data.length;i++)
    {
        var obs = data[i];
        tbodyHTML.push("<tr>");
        tbodyHTML.push("<td>" + moment(obs.timeOfObservation).format("YYYY-MM-DD HH:mm ZZ") + "</td>");
        tbodyHTML.push("<td>" + getLocalizedOrganismName(obs.organism) + "</td>");
        tbodyHTML.push("<td>" + getLocalizedOrganismName(obs.cropOrganism) + "</td>");
        tbodyHTML.push("<td>" + (obs.location != null ? obs.location.name : "") + "</td>");
        tbodyHTML.push("<td>" + obs.user.firstName + " " + obs.user.lastName + "</td>");
        tbodyHTML.push("<td>" + obs.observationHeading + "</td>");
        tbodyHTML.push("<td></td>");
        tbodyHTML.push("</tr>");
    }
    tbody.innerHTML = tbodyHTML.join("\n");
};

// TODO: Set selected if possible
var renderOrganismField = function(organismList, fieldId, selectedId)
{
    // Sort alphabetically by local name
    organismList.sort(function(a,b){
        if (getLocalizedOrganismName(a) < getLocalizedOrganismName(b)) return -1;
        if (getLocalizedOrganismName(a) > getLocalizedOrganismName(b)) return 1;
        return 0;
    });
    var list = document.getElementById(fieldId);
    list.options.length=0;
    list.options[0] = new Option("",""); // For the chosenjs to print data-placeholder 
    for(var i=0;i<organismList.length;i++)
    {
        var organism = organismList[i];
        var newOption = new Option(getLocalizedOrganismName(organism),organism.organismId);
        if(organism.organismId === selectedId)
        {
            newOption.selected = true;
        }
        list.options[list.options.length] = newOption;
    }
};

var renderCropCategoryField = function(cropCategoryList, selectedId)
{
    // TODO: Sort by local name
    cropCategoryList.sort(function(a,b){
        if (getLocalizedCropCategoryName(a) < getLocalizedCropCategoryName(b)) return -1;
        if (getLocalizedCropCategoryName(a) > getLocalizedCropCategoryName(b)) return 1;
        return 0;
    });
    var list = document.getElementById("cropCategoryList");
    list.options.length=0;
    list.options[0] = new Option("",""); // For the chosenjs to print data-placeholder 
    for(var i=0;i<cropCategoryList.length;i++)
    {
        var cropCategory = cropCategoryList[i];
        var newOption = new Option(getLocalizedCropCategoryName(cropCategory),cropCategory.cropCategoryId);
        if(cropCategory.cropCategoryId === selectedId)
        {
            newOption.selected = true;
        }
        list.options[list.options.length] = newOption;
    }
};

var initForm = function(organizationId,
            pestId,
            cropId,
            cropCategoryId,
            postRenderFormActions
        )
{
    $.getJSON( "/rest/observation/pest/" + organizationId , function( pestList ) {
        renderOrganismField(pestList, "pestList", pestId);
        $.getJSON( "/rest/observation/crop/" + organizationId , function( cropList ) {
            renderOrganismField(cropList, "cropList", cropId);
             $.getJSON( "/rest/organism/cropcategory/" + organizationId , function( cropCategoryList ) {
                renderCropCategoryField(cropCategoryList, cropCategoryId);
                postRenderFormActions(); // Activate chosen.js
            });
        });
    });
};

// Global configs
var styleOld = 
          new ol.style.Style({
                fill: new ol.style.Fill({
                  color: 'rgba(0, 0, 0, 0.2)'
                }),
                stroke: new ol.style.Stroke({
                  color: '#000000',
                  width: 2
                }),
                image: new ol.style.Circle({
                  radius: 7,
                  fill: new ol.style.Fill({
                    color: '#000000'
                  })
                })
              });
  var styleInvisible = new ol.style.Style({
                fill: new ol.style.Fill({
                  color: 'rgba(0, 0, 0, 0.0)'
                }),
                stroke: new ol.style.Stroke({
                  color: 'rgba(0, 0, 0, 0.0)',
                  width: 0
                }),
                image: new ol.style.Circle({
                  radius: 0,
                  fill: new ol.style.Fill({
                    color: 'rgba(0, 0, 0, 0.0)'
                  })
                })
              });

var initAgeStyles = function(){
    ageStyles = [];
    for(var i in ageColors)
    {
        ageStyles.push([
            ageColors[i][0],
            new ol.style.Style({
                fill: new ol.style.Fill({
                  color: ageColors[i][1].replace("1.0","0.2")
                }),
                stroke: new ol.style.Stroke({
                  color: ageColors[i][1],
                  width: 2
                }),
                image: new ol.style.Circle({
                  radius: 6,
                  fill: new ol.style.Fill({
                    color: ageColors[i][1]
                  })
                })
              })
          ]); 
    }
};

var getCorrectStyle = function(feature){
            var age = getObservationRelativeAge(feature);
            if(age == null)
            {
                return;
            }
            if(age < 0)
            {
                return styleInvisible;
            }
            for(var i in ageStyles)
            {
                if(age < ageStyles[i][0])
                {
                    return ageStyles[i][1];
                }
            }
            return styleOld;
        };

var getObservationRelativeAge = function(feature)
{
    var observation = getObservation(feature.get("observationId")); // TODO include observations timestamp in feature. Speeds things up
    if(observation == null)
    {
        console.info("No obs with id=" + feature.get("observationId"));
        return null; // Means invisible
    }
    return Math.floor((currentDateInMillis - getUnixTimestampFromJSON(observation.timeOfObservation)) / (1000 * 60 * 60 * 24)) + 1;
};