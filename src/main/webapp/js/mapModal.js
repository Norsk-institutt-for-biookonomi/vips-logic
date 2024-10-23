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
            createNewLocation: 'Opprett nytt sted',
            name: 'Navn',
            latitude: 'Breddegrad',
            longitude: 'Lengdegrad',
            type: 'Type',
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
            createNewLocation: 'Create New Location',
            name: 'Name',
            latitude: 'Latitude',
            longitude: 'Longitude',
            type: 'Type',
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
     * @param isPoiMap    True if the map operates on pois with an id, name and type, false if it operates on coordinates
     * @param allowNewPoints    Whether or not the user should be allowed to add new points
     * @param callbackOnClose   Callback function to call when closing the modal
     */
    constructor(mapModalId, geoJsonData, language = 'nb', isPoiMap = true, allowNewPoints = false, callbackOnClose = null) {
        this.mapModalElement = document.getElementById(mapModalId);
        this.mapContainerId = mapModalId + "-container";
        this.mapContainerElement = this.addMapContainer(this.mapModalElement, this.mapContainerId);

        // The variable below should instead be: this.selectPoi or this.selectCoordinates
        // this.includeTypeInformation = Object.keys(typeNameMap).length === 7;
        this.includeTypeInformation = true;

        this.geoJsonData = geoJsonData;
        if (language in MapModal.TRANSLATIONS) {
            this.translations = MapModal.TRANSLATIONS[language];
        } else {
            console.error("'" + language + "' is not a supported language");
            this.translations = MapModal.TRANSLATIONS['nb'];
        }
        this.isPoiMap = isPoiMap;
        this.allowNewPoints = allowNewPoints;
        this.callbackOnClose = callbackOnClose;

        this.map = null;
        this.isMapInitialized = false;
        this.markersByType = {};
        this.selectedNewPointMarker = null;
        this.selectedExistingPointMarker = null;
        this.coordinatePrecision = 6;

        this.zoomToLocationControl = new ZoomToLocationControl({
            translations: this.translations
        });
        this.closeMapControl = new CloseMapControl({
            translations: this.translations,
            onClose: () => {
                this.closeModal()
            }
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

    initMap(latitude = 65, longitude = 14, zoomLevel = 5) {
        if (this.isMapInitialized) {
            console.error("Map is already initialized");
            return;
        }
        // Initialize the map centered on Norway
        this.map = map(this.mapContainerId).setView([latitude, longitude], zoomLevel);
        tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19
        }).addTo(this.map);
        console.info("Create map " + this.mapContainerId + " centered on (" + latitude + "," + longitude + ") with points", this.geoJsonData ? this.geoJsonData.features : null);

        this.map.addControl(this.zoomToLocationControl);
        this.map.addControl(this.closeMapControl);

        // Add points to the map if given
        if (this.geoJsonData.features) {
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
        }

        // Poi type selection panel should only be included if map operates on pois
        if (this.isPoiMap) {
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
        if (this.selectedExistingPointMarker === layer) {
            layer.closePopup();
            this.removeSelectedPointMarkerIfExists();
        }
        layer.unbindPopup();
        layer.off('click');
    }

    bindActionToPoint(layer) {
        layer.bindPopup(this.popupContent(layer.feature));
        layer.on('click', () => {
            this.displaySelectedPoint(layer.feature, layer, false);
        });
    }

    selectPointById(pointOfInterestId) {
        const selectedFeature = this.getFeatureById(pointOfInterestId);
        const selectedLayer = this.getLayerById(pointOfInterestId);
        this.displaySelectedPoint(selectedFeature, selectedLayer, true);
        selectedLayer.openPopup();
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
                const formName = nameInput ? nameInput.value : '';
                const formType = typeInput ? parseInt(typeInput.value, 10) : '';
                const feature = this.createFeatureForPoint(formName, formType, parseFloat(longitudeInput.value), parseFloat(latitudeInput.value));
                this.confirmSelection(feature);
            });
        });
    }

    updateMarkerPosition(marker, lat, lng) {
        if (marker) {
            marker.setLatLng([lat, lng]);
            this.map.setView([lat, lng], this.map.getZoom());
        }
    }

    removeNewPointMarkerIfExists() {
        if (this.selectedNewPointMarker) {
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
                "pointOfInterestId": 0,
                "pointOfInterestName": name,
                "pointOfInterestTypeId": type
            }
        };
    }

    popupContent(feature) {
        const popupElement = document.createElement("div");
        popupElement.id = 'poi-popup';
        const name = feature.properties.pointOfInterestName;
        const type = this.translations['poiType' + feature.properties.pointOfInterestTypeId];
        const latitude = feature.geometry.coordinates[1].toFixed(this.coordinatePrecision);
        const longitude = feature.geometry.coordinates[0].toFixed(this.coordinatePrecision);
        const buttonLabel = this.translations.selectLocation;
        popupElement.innerHTML = `<h4>${name}</h4>
        <b>${this.translations.latitude}</b> ${latitude}<br>
        <b>${this.translations.longitude}</b> ${longitude}<br>
        <b>${this.translations.type}</b> ${type}<br><br>
        <button id="submit-button" class="btn btn-primary">${buttonLabel}</button>`
        const buttonElement = popupElement.querySelector("#submit-button");
        buttonElement.addEventListener('click', () => {
            this.confirmSelection(feature);
        });
        return popupElement;
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
        form.id = "new-point-form"
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
                <div id="form-group-poi-name" class="form-group">
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
                <div id="form-group-poi-type" class="form-group">
                    <label for="map-poi-type">${this.translations.type}:</label>
                    <select class="form-control" id="map-poi-type" name="type">
                        <option value="2">${this.translations['poiType2']}</option>
                        <option value="3">${this.translations['poiType3']}</option>
                        <option value="5">${this.translations['poiType5']}</option>  
                    </select>
                </div>
                <div class="form-group text-right">
                    <button id="map-poi-submit-button" class="btn btn-primary">${this.translations.selectLocation}</button>
                </div>                
            </div>`;

        // If map does not operate on pois, the form should not include input fields for name and type
        if (!this.isPoiMap) {
            form.querySelector("#form-group-poi-name").remove()
            form.querySelector("#form-group-poi-type").remove()
        }

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
                if (this.options.onClose) {
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
