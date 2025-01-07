/* 
 * Copyright (c) 2016 NIBIO <http://www.nibio.no/>. 
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

var initMap = function(container, mapAttribution)
{
	// Creating background layer, Open Street Map is default
	var backgroundLayer = new ol.layer.Tile({
   	 	source: new ol.source.OSM({
   	 					attributions: [
                           new ol.Attribution({
                             html: mapAttribution
                           })
                         ]
                   })
    });
	
	forecastLayer = new ol.layer.Vector({
		source: new ol.source.Vector({
			url: "/rest/applefruitmoth/observationsites/kml/?onlyPubliclyAvailable=false",
			format: new ol.format.KML({"extractAttributes":true}),
			projection: ol.proj.get('EPSG:3857')
		})
	});
	// Layer for popup
	var popOverlay = new ol.Overlay({
	  element: document.getElementById("popover")
	});
	
	// Creating the map
	map = new ol.Map({
			target: container,
			layers: [backgroundLayer, forecastLayer],
			overlays: [popOverlay],
			renderer: 'canvas'
	});
	
	// Setting zoom and center for the map (need to do this after creating map. so that we can transform our
	// center to correct map projection)
	var view = new ol.View({
		center: ol.proj.transform([9,61], 'EPSG:4326', map.getView().getProjection().getCode()),
		zoom: 6
	});
	map.setView(view);
	
	// Using Bootstrap's popover plugin. See http://getbootstrap.com/javascript/#popovers
	var poiDetails = $("#popover");
	
	// State cariable
	var currentClickedFeature = false;
	
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
	    	  currentClickedFeature = feature;
	    	  poiDetails.popover({
	          		animation: true,
	          		trigger: 'manual',
	          		html: true,
	          		placement: "auto top",
	          		title: "<a href='/applefruitmoth?action=viewObservationSite&observationSiteId=" + feature.getId() + "'>" + feature.get("siteName") + "</a>",
	          		content: generatePopupContents(feature.get("warningStatus"), feature.get("description"))
	          	});
	          	
	          	poiDetails.popover('show');
	      } else {
	    	  currentClickedFeature = null;
	    	  poiDetails.popover('destroy');
	      }
	 };
	// On click, display forecasts in popup
		map.on('singleclick', function(evt) {
			var pixel = map.getEventPixel(evt.originalEvent);
			  displayFeatureDetails(pixel);
			});
}

function generatePopupContents(warningStatus, description)
{
	return "<img src='/public/images/station_icon_status_" + warningStatus + ".png'/> " + description;
}


