// mapModal.js
import {
    map,
    divIcon,
    tileLayer,
    geoJSON,
    circleMarker,
    marker,
    GeoJSON,
    DomEvent,
    Control,
    DomUtil
} from '/js/3rdparty/leaflet-src.esm.js';

/**
 * This JavaScript module enables the display of a map modal centered on Norway, with a given set of
 * coordinates representing points of interest. The map is primarily intended for use within VIPSLogic,
 * but can also be imported and used in VIPSWeb or other applications within the VIPS sphere.
 *
 * Follow the steps below in order to set up the map:
 * 1) Include the following stylesheets:
 * <link type="text/css" rel="stylesheet" href="(https://logic.vips.nibio.no)/css/3rdparty/leaflet.css" />
 * <link type="text/css" rel="stylesheet" href="/https://logic.vips.nibio.no)/css/mapModal.css" />
 * 2) Include HTML to display a button, and a div in which the map will be opened:
 *      <i id="open-map-modal-icon" class="fa fa-map-marker" onclick="openLocationMap()"></i>
 *      <div id="location-map" class="map-modal"></div>
 * 3) Include MapModal within a script tag of type 'module', and implement the function openLocationMap(),
 *    which initializes and opens the map modal.
 *
 * Uses css classes from bootstrap 3.4.1
 * Leaflet: https://unpkg.com/leaflet@1.9.4/dist/leaflet-src.esm.js
 */
class MapModal {

    static TRANSLATIONS = {
        nb: {
            selectLocation: 'Velg sted',
            saveLocation: 'Lagre nytt sted',
            createNewLocation: 'Opprett nytt sted',
            name: 'Navn',
            latitude: 'Breddegrad',
            longitude: 'Lengdegrad',
            type: 'Type',
            submitLocation: 'OK',
            zoomToLocation: 'Zoom til meg',
            geolocationNotSupported: 'Geolokalisering støttes ikke av denne nettleseren',
            geolocationFailed: 'Fant ikke din posisjon',
            closeMap: 'Lukk kart',
            poiType0: 'Uspesifisert',
            poiType1: 'Værstasjon',
            poiType2: 'Gård',
            poiType3: 'Felt',
            poiType5: 'Felle',
            poiType6: 'Bigårdsplass',
            poiType7: 'Planteskole',
        },
        en: {
            selectLocation: 'Select location',
            saveLocation: 'Save new location',
            createNewLocation: 'Create New Location',
            name: 'Name',
            latitude: 'Latitude',
            longitude: 'Longitude',
            type: 'Type',
            submitLocation: 'OK',
            zoomToLocation: 'Zoom to My Location',
            geolocationNotSupported: 'Geolocation is not supported by this browser',
            geolocationFailed: 'Unable to retrieve your location',
            closeMap: 'Close Map',
            poiType0: 'Unspecified',
            poiType1: 'Weather station',
            poiType2: 'Farm',
            poiType3: 'Field',
            poiType5: 'Trap',
            poiType6: 'Apiary site',
            poiType7: 'Nursery',
        }
    };

    /**
     * @param mapModalId    The id of the HTML element in which the modal should be opened
     * @param geoJsonData   GeoJson containing all features which should be displayed on the map
     * @param language  The language in which texts should be displayed, either 'nb' or 'en'
     * @param allowNewPoints    Whether or not the user should be allowed to add new points
     * @param callbackOnClose   Callback function to call when closing the modal
     */
    constructor(mapModalId, geoJsonData, language = 'nb', allowNewPoints = false, callbackOnClose = null) {
        this.mapModalElement = document.getElementById(mapModalId);
        this.mapContainerId = mapModalId + "-container";
        this.mapContainerElement = this.addMapContainer(this.mapModalElement, this.mapContainerId);

        // The variable below should instead be: this.selectPoi or this.selectCoordinates
        // this.includeTypeInformation = Object.keys(typeNameMap).length === 7;
        this.includeTypeInformation = true;

        this.geoJsonData = geoJsonData;
        if(language in MapModal.TRANSLATIONS) {
            this.translations = MapModal.TRANSLATIONS[language];
        } else {
            console.error("'"+ language +"' is not a supported language");
            this.translations = MapModal.TRANSLATIONS['nb'];
        }
        this.allowNewPoints = allowNewPoints;
        this.callbackOnClose = callbackOnClose;

        this.map = null;
        this.isMapInitialized = false;
        this.markersByType = {};
        this.selectedNewPointMarker = null;
        this.selectedExistingPointMarker = null;
        this.coordinatePrecision = 6;

        this.displaySelectedFeatureInfoControl = new DisplaySelectedFeatureInfoControl({
            translations: this.translations,
            onSubmit: (feature) => {this.confirmSelection(feature)},
            coordinatePrecision: this.coordinatePrecision
        });
        this.zoomToLocationControl = new ZoomToLocationControl({
            translations: this.translations
        });
        this.closeMapControl = new CloseMapControl({
            translations: this.translations,
            onClose: () => {this.closeModal()}
        });

        // Colours for the available types of pois
        this.typeColorMap = {
            0: "#5DADE2",  // Bright Blue
            1: "#58D68D",  // Vibrant Green
            2: "#AF7AC5",  // Medium Lavender
            3: "#F5B041",  // Warm Orange
            5: "#F7DC6F",  // Bright Yellow
            6: "#DC7633",   // Rich Brown
            7: "#FF33A6"  // Vivid Magenta
        };
    }

    addMapContainer(parentDiv, id) {
        const mapContainer = document.createElement('div');
        mapContainer.id = id;
        mapContainer.style.height = '100%';
        mapContainer.style.width = '100%';
        mapContainer.style.position = 'relative';
        parentDiv.appendChild(mapContainer);
        return mapContainer;
    }

    styleOfSelectedPointMarker(newPoint) {
        const styleMap = {
            radius: 12,
            color: "#FFFFFF",
            weight: 2,
            opacity: 1,
            fillOpacity: 1,
        };
        if (newPoint) {
            styleMap['fillColor'] = "#FF5733";
        }
        return styleMap
    }

    styleOfPointMarker(pointOfInterestTypeId) {
        const color = this.typeColorMap[pointOfInterestTypeId] || "#3498DB";
        return {
            radius: 8,
            fillColor: color,
            color: "#FFFFFF",
            weight: 2,
            opacity: 1,
            fillOpacity: 0.8
        }
    }

    initMap(latitude= 65, longitude= 14, zoomLevel= 5) {
        if (this.isMapInitialized) {
            console.error("Map is already initialized");
            return;
        }
        // Initialize the map centered on Norway
        this.map = map(this.mapContainerId).setView([latitude, longitude], zoomLevel);
        tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19
        }).addTo(this.map);
        console.info("Create map " + this.mapContainerId + " centered on (" + latitude + "," + longitude + ") with points", this.geoJsonData);

        this.map.addControl(this.displaySelectedFeatureInfoControl);
        this.map.addControl(this.zoomToLocationControl);
        this.map.addControl(this.closeMapControl);

        // Add points to the map
        geoJSON(this.geoJsonData, {
            filter: (feature) => feature.geometry.type === "Point",
            pointToLayer: (feature, latlng) => {
                return circleMarker(latlng, this.styleOfPointMarker(feature.properties.pointOfInterestTypeId));
            },
            onEachFeature: (feature, layer) => {
                const typeId = feature.properties.pointOfInterestTypeId;
                this.bindActionToPoint(layer);
                if (!this.markersByType[typeId]) {
                    this.markersByType[typeId] = [];
                }
                this.markersByType[typeId].push(layer);
            }
        }).addTo(this.map);

        if(this.includeTypeInformation) {
            this.legendControl = new LegendControl({
                typeColorMap: this.typeColorMap,
                translations: this.translations,
                markersByType: this.markersByType,
                mapModalInstance: this
            });
            this.map.addControl(this.legendControl);
        }

        // Enable adding new points if allowed
        if (this.allowNewPoints) {
            this.enablePointCreation();
        }
        this.isMapInitialized = true;
    }

    // Function called when point is hidden (by deselecting its location type in legend box)
    // If point is already selected, the popup and info panel is removed.
    unbindActionToPoint(layer) {
        if(this.selectedExistingPointMarker === layer) {
            layer.closePopup();
            this.removeSelectedPointMarkerIfExists();
            this.displaySelectedFeatureInfoControl.updateInfoPanel();
        }
        layer.unbindPopup();
        layer.off('click');
    }

    bindActionToPoint(layer) {
        layer.bindPopup(this.popupContent(layer.feature));
        layer.on('click', () => {
            this.displaySelectedPoint(layer.feature, layer, false);
            this.displaySelectedFeatureInfoControl.updateInfoPanel(layer.feature)
        });
    }

    selectPointById(pointOfInterestId) {
        const selectedFeature = this.getFeatureById(pointOfInterestId);
        const selectedLayer = this.getLayerById(pointOfInterestId);
        this.displaySelectedPoint(selectedFeature, selectedLayer, true);
        selectedLayer.openPopup();
        this.displaySelectedFeatureInfoControl.updateInfoPanel(selectedFeature);
    }

    getFeatureById(pointOfInterestId) {
        return this.geoJsonData.features.find(feature => feature.properties.pointOfInterestId == pointOfInterestId);
    }

    getLayerById(pointOfInterestId) {
        let result = null;
        this.map.eachLayer(layer => {
            if (layer instanceof GeoJSON) {
                layer.eachLayer(l => {
                    if (l.feature && l.feature.properties.pointOfInterestId == pointOfInterestId) {
                        result = l;
                    }
                });
            }
        });
        return result;
    }

    displaySelectedPoint(feature, layer, zoomInToSelected = false) {
        // Deselect previously selected point, if any
        this.removeSelectedPointMarkerIfExists();
        this.removeNewPointMarkerIfExists();

        this.selectedExistingPointMarker = layer;
        this.selectedExistingPointMarker.setStyle(this.styleOfSelectedPointMarker(false));
        if (zoomInToSelected) {
            const latLng = this.selectedExistingPointMarker.getLatLng();
            this.map.setView(latLng, 10);
        }
    }

    confirmSelection(feature) {
        if (typeof this.callbackOnClose === 'function') {
            const pointData = {
                pointOfInterestId: feature.properties.pointOfInterestId,
                name: feature.properties.pointOfInterestName,
                pointOfInterestTypeId: feature.properties.pointOfInterestTypeId,
                longitude: feature.geometry.coordinates[0],
                latitude: feature.geometry.coordinates[1]
            };
            this.callbackOnClose(pointData);
        }
        this.closeModal();
    }

    enablePointCreation() {
        this.map.on('click', (e) => {
            const latlng = e.latlng;

            // Click on the map should remove any previous selections
            this.removeSelectedPointMarkerIfExists();
            this.removeNewPointMarkerIfExists();
            this.closeNewPointFormIfOpen();
            this.displaySelectedFeatureInfoControl.updateInfoPanel(null);

            // Calculate the pixel position from the map's click event
            const containerPoint = this.map.latLngToContainerPoint(latlng);

            this.selectedNewPointMarker = circleMarker(latlng, this.styleOfSelectedPointMarker(true)).addTo(this.map);
            const newPointFormElement = this.addHtmlElementNewPointForm(containerPoint.x, containerPoint.y, latlng.lat, latlng.lng)

            DomEvent.disableClickPropagation(newPointFormElement);
            document.addEventListener('click', this.handleClickOutsidePointForm.bind(this), true);

            const closeButton = newPointFormElement.querySelector("#map-poi-close-button");
            const nameInput = newPointFormElement.querySelector('#map-poi-name');
            const latitudeInput = newPointFormElement.querySelector('#map-poi-latitude');
            const longitudeInput = newPointFormElement.querySelector('#map-poi-longitude');
            const typeInput = newPointFormElement.querySelector('#map-poi-type');
            const submitButton = newPointFormElement.querySelector('#map-poi-submit-button');
            const validateInputs = () => {
                const isValidLat = !isNaN(parseFloat(latitudeInput.value)) && isFinite(latitudeInput.value);
                const isValidLng = !isNaN(parseFloat(longitudeInput.value)) && isFinite(longitudeInput.value);
                if (isValidLat && isValidLng) {
                    this.updateMarkerPosition(this.selectedNewPointMarker, parseFloat(latitudeInput.value), parseFloat(longitudeInput.value));
                }
                submitButton.disabled = !(isValidLat && isValidLng);
            };
            latitudeInput.addEventListener('blur', validateInputs);
            longitudeInput.addEventListener('blur', validateInputs);

            closeButton.addEventListener('click', () => {
                newPointFormElement.remove();
                this.removeNewPointMarkerIfExists();
            });

            submitButton.addEventListener('click', () => {
                const feature = this.createFeatureForPoint(nameInput.value, parseInt(typeInput.value, 10), parseFloat(longitudeInput.value), parseFloat(latitudeInput.value));
                this.displaySelectedFeatureInfoControl.updateInfoPanel(feature);
                newPointFormElement.remove();
            });
        });
    }

    updateMarkerPosition(marker, lat, lng) {
        if (marker) {
            console.info("Move marker to new position")
            marker.setLatLng([lat, lng]);
            this.map.setView([lat, lng], this.map.getZoom());
        }
    }

    removeNewPointMarkerIfExists() {
        if (this.selectedNewPointMarker) {
            console.info("Remove selected new point marker", this.selectedNewPointMarker);
            this.map.removeLayer(this.selectedNewPointMarker);
        }
    }

    removeSelectedPointMarkerIfExists() {
        if (this.selectedExistingPointMarker) {
            const pointOfInterestTypeId = this.selectedExistingPointMarker.feature.properties.pointOfInterestTypeId;
            this.selectedExistingPointMarker.setStyle(this.styleOfPointMarker(pointOfInterestTypeId));
        }
    }

    handleClickOutsidePointForm(event) {
        const formElement = document.getElementById('new-point-form');
        if (formElement && !formElement.contains(event.target)) {
            this.closeNewPointFormIfOpen();
        }
    }

    closeNewPointFormIfOpen() {
        const formElement = document.getElementById('new-point-form');
        if (formElement) {
            formElement.remove();
        }
        document.removeEventListener('click', this.handleClickOutsidePointForm.bind(this), true);
    }

    createFeatureForPoint(name, type, longitude, latitude) {
        return {
            "type": "Feature",
            "geometry": {
                "type": "Point",
                "coordinates": [longitude, latitude]
            },
            "properties": {
                "pointOfInterestName": name,
                "pointOfInterestTypeId": type
            }
        };
    }

    popupContent(feature) {
        return `<div id='poi-popup'>${feature.properties.pointOfInterestName}</div>`;
    }

    /**
     * Creates the HTML form for adding a new point, and add it to the map container.
     *
     * @param positionX Where to place the form on the x axis
     * @param positionY Where to place the form on the y axis
     * @param latitude  Latitude of the point clicked
     * @param longitude Longitude of the point clicked
     * @returns {Element}
     */
    addHtmlElementNewPointForm(positionX, positionY, latitude, longitude) {
        const form = document.createElement("div");
        form.id="new-point-form"
        form.classList.add("panel");
        form.classList.add("panel-default");

        // Adjust position so that the form is always displayed within the map
        const mapWidth = this.mapModalElement.offsetWidth;
        const mapHeight = this.mapModalElement.offsetHeight;
        const formWidth = 200; // approximately
        const formHeight = 400; // approximately
        if (positionX + formWidth > mapWidth) {
            positionX = mapWidth - formWidth - 10; // 10px padding from the edge
        }
        if (positionY + formHeight > mapHeight) {
            positionY = mapHeight - formHeight - 10; // 10px padding from the edge
        }
        form.style.left = `${positionX}px`;
        form.style.top = `${positionY}px`;

        form.innerHTML = `
            <div class="panel-heading">
                <h4 class="panel-title">${this.translations.createNewLocation}</h4>
                <span id="map-poi-close-button" style="position: absolute; top: 5px; right: 10px; cursor: pointer; font-size: 18px;">&times;</span>
            </div>
            <div class="panel-body">
                <div class="form-group">
                    <label for="map-poi-name">${this.translations.name}:</label>
                    <input type="text" class="form-control" id="map-poi-name" name="name">
                </div>
                <div class="form-group">
                    <label for="map-poi-latitude">${this.translations.latitude}:</label>
                    <input type="text" class="form-control" id="map-poi-latitude" name="latitude" value="${latitude.toFixed(this.coordinatePrecision)}">
                </div>
                <div class="form-group">
                    <label for="map-poi-longitude">${this.translations.longitude}:</label>
                    <input type="text" class="form-control" id="map-poi-longitude" name="longitude" value="${longitude.toFixed(this.coordinatePrecision)}">
                </div>
                <div class="form-group">
                    <label for="map-poi-type">${this.translations.type}:</label>
                    <select class="form-control" id="map-poi-type" name="type">
                        <option value="2">${this.translations['poiType2']}</option>
                        <option value="3">${this.translations['poiType3']}</option>
                        <option value="5">${this.translations['poiType5']}</option>  
                    </select>
                </div>
                <div class="form-group text-right">
                    <button id="map-poi-submit-button" class="btn btn-primary">${this.translations.submitLocation}</button>
                </div>                
            </div>`;
        this.mapContainerElement.appendChild(form);
        return form;
    }

    /**
     * Make modal visible. Initialise map with selected point marked, if given.
     *
     * @param selectedPointOfInterestId
     * @param latitude
     * @param longitude
     * @param zoomLevel
     */
    openModal(selectedPointOfInterestId, latitude, longitude, zoomLevel) {
        this.mapModalElement.style.display = 'flex';
        this.mapModalElement.style.justifyContent = 'center';
        this.mapModalElement.style.alignItems = 'center';
        this.initMap(latitude, longitude, zoomLevel);
        if (selectedPointOfInterestId) {
            this.selectPointById(selectedPointOfInterestId);
        }
    }

    /**
     * Hide modal. Remove container element and map.
     */
    closeModal() {
        this.mapModalElement.style.display = 'none';
        this.mapContainerElement.remove();
        this.map.remove();
        this.map = null;
    }

}

const DisplaySelectedFeatureInfoControl = Control.extend({
    options: {
        position: 'bottomleft',
        onSubmit: undefined,
        translations: {},
        coordinatePrecision: 5
    },

    onAdd: function (map) {
        const container = DomUtil.create('div', 'leaflet-bar leaflet-control hidden');
        container.id = 'selected-point-info-panel';
        const infoMessageDiv = DomUtil.create('div', 'info-message', container);
        infoMessageDiv.id = 'info-message';
        const button = DomUtil.create('button', 'btn btn-primary', container);
        button.id = 'confirm-button';
        return container;
    },

    updateInfoPanel: function (feature) {
        const container = this._container;
        const infoMessageDiv = container.querySelector('#info-message');
        const confirmButton = container.querySelector('#confirm-button');

        if (feature) {
            const name = feature.properties.pointOfInterestName;
            const type = this.options.translations['poiType' + feature.properties.pointOfInterestTypeId];
            const latitude = feature.geometry.coordinates[1].toFixed(this.options.coordinatePrecision);
            const longitude = feature.geometry.coordinates[0].toFixed(this.options.coordinatePrecision);

            infoMessageDiv.innerHTML = `
            <h4>${name}</h4>
            <b>${this.options.translations.latitude}</b> ${latitude}<br>
            <b>${this.options.translations.longitude}</b> ${longitude}<br>
            <b>${this.options.translations.type}</b> ${type}`

            confirmButton.innerHTML = feature.properties.pointOfInterestId
                ? this.options.translations.selectLocation
                : this.options.translations.saveLocation;

            confirmButton.onclick = () => {
                this.options.onSubmit(feature);
            };
            container.classList.remove('hidden');
        } else {
            container.classList.add('hidden');
        }
    }
});

const ZoomToLocationControl = Control.extend({
    options: {
        position: 'topleft',
        translations: {}
    },

    onAdd: function (map) {
        // Create the button element
        const container = DomUtil.create('div', 'leaflet-bar leaflet-control');
        const zoomToLocationButton = DomUtil.create('a', 'map-button', container);
        zoomToLocationButton.innerHTML = '<i class="fa fa-map-marker"></i>';
        zoomToLocationButton.href = '#';
        zoomToLocationButton.title = this.options.translations.zoomToLocation;

        DomEvent
            .on(zoomToLocationButton, 'click', DomEvent.stop)
            .on(zoomToLocationButton, 'click', () => {
                if (navigator.geolocation) {
                    const locationIcon = divIcon({
                        html: '<i class="fa fa-map-marker fa-3x"></i>',
                        iconSize: [40, 60],
                        className: 'location-marker',
                        iconAnchor: [20, 60]
                    });
                    navigator.geolocation.getCurrentPosition((position) => {
                        const latitude = position.coords.latitude;
                        const longitude = position.coords.longitude;

                        map.setView([latitude, longitude], 13);
                        marker([latitude, longitude], {icon: locationIcon}).addTo(map);
                    }, (error) => {
                        console.error('Geolocation failed: ' + error.message);
                        alert(this.options.translations.geolocationFailed);
                    });
                } else {
                    alert(this.options.translations.geolocationNotSupported);
                }
            });
        return container;
    }
});


const CloseMapControl = Control.extend({
    options: {
        position: 'topright',
        translations: {}
    },

    onAdd: function (map) {
        // Create a container for the button
        const container = DomUtil.create('div', 'leaflet-bar leaflet-control');

        // Create the button element
        const closeMapButton = DomUtil.create('a', 'map-button', container);
        closeMapButton.innerHTML = '×'; // Unicode for close symbol
        closeMapButton.href = '#';
        closeMapButton.title = this.options.translations.closeMap;

        DomEvent
            .on(closeMapButton, 'click', DomEvent.stop)
            .on(closeMapButton, 'click', () => {
                if(this.options.onClose) {
                    this.options.onClose();
                }
        });
        return container;
    }
});

const LegendControl = Control.extend({
    options: {
        position: 'bottomright',
        typeColorMap: {},
        translations: {},
        markersByType: {},
        mapModalInstance: null,
    },
    onAdd: function (map) {
        const legendDiv = DomUtil.create('div', 'info legend');
        const typeColorMap = this.options.typeColorMap;
        const translations = this.options.translations;
        const markersByType = this.options.markersByType;

        DomEvent.disableClickPropagation(legendDiv);

        for (const type in typeColorMap) {
            if (!markersByType[type]) {
                continue;
            }
            const count = markersByType[type].length;
            const color = typeColorMap[type];

            const itemDiv = DomUtil.create('div', 'legend-item', legendDiv);
            itemDiv.innerHTML = `<i style="background:${color};"></i>
                    <span>${translations['poiType' + type]} (${count})</span>`;

            DomEvent
                .on(itemDiv, 'click', DomEvent.stop)
                .on(itemDiv, 'click', () => {
                    this.toggleMarkers(type, markersByType[type], itemDiv);
                });
    }
        return legendDiv;
    },

    toggleMarkers: function (type, markers, itemDiv) {
        if (!markers) return;
        const wasVisible = markers[0].options.opacity !== 0;
        const isVisible = !wasVisible;

        markers.forEach(marker => {
            const markerElement = marker.getElement();
            if (wasVisible) {
                this.options.mapModalInstance.unbindActionToPoint(marker);
                if (markerElement) {
                    markerElement.style.pointerEvents = 'none';
                }
                marker.setStyle({opacity: 0, fillOpacity: 0});
            } else {
                marker.setStyle({opacity: 1, fillOpacity: 0.8});
                this.options.mapModalInstance.bindActionToPoint(marker);
                if (markerElement) {
                    markerElement.style.pointerEvents = '';
                }
            }
        });
        // Mark hidden with line-through
        itemDiv.style.textDecoration = isVisible ? "none" : "line-through solid black 2px";
    }



});

// Export the module
export default MapModal;
