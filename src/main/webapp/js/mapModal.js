// mapModal.js
import {
    map,
    tileLayer,
    geoJSON,
    circleMarker,
    marker,
    GeoJSON,
    DomEvent,
    Control,
    DomUtil
} from '/js/3rdparty/leaflet-src.esm.js';
// https://unpkg.com/leaflet@1.9.4/dist/leaflet-src.esm.js

/**
 * Uses css classes from bootstrap 3.4.1
 *
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
            submitLocation: 'OK'
        },
        en: {
            selectLocation: 'Select location',
            createNewLocation: 'Create new location',
            name: 'Name',
            latitude: 'Latitude',
            longitude: 'Longitude',
            type: 'Type',
            submitLocation: 'OK'
        }
    };

    /**
     * @param mapModalId    The id of the HTML element in which the modal should be opened
     * @param typeNameMap   A mapping from pointOfInterestTypeIds to their localized names
     * @param geoJsonData   GeoJson containing all features which should be displayed on the map
     * @param language  The language in which texts should be displayed, either 'nb' or 'en'
     * @param allowNewPoints    Whether or not the user should be allowed to add new points
     * @param callbackOnClose   Callback function to call when closing the modal
     */
    constructor(mapModalId, typeNameMap, geoJsonData, language = 'nb', allowNewPoints = false, callbackOnClose = null) {
        this.mapModalId = mapModalId;
        this.mapContainerId = mapModalId + "-container";
        this.typeNameMap = typeNameMap;
        this.geoJsonData = geoJsonData;
        this.translations = MapModal.TRANSLATIONS[language];
        this.allowNewPoints = allowNewPoints;
        this.callbackOnClose = callbackOnClose;

        this.map = null;
        this.isMapInitialized = false;
        this.selectedNewPointMarker = null;
        this.selectedExistingPointMarker = null;

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

    styleOfSelectedPointMarker() {
        return {
            radius: 12,
            fillColor: "#FF5733",
            color: "#FFFFFF",
            weight: 2,
            opacity: 1,
            fillOpacity: 1,
        }
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

    initMap() {
        if (!this.isMapInitialized) {
            // Initialize the map centered on Norway
            this.map = map(this.mapContainerId).setView([63.4226, 10.3951], 5);
            tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19
            }).addTo(this.map);
            console.info("Create map " + this.mapContainerId + " with points", this.geoJsonData);

            this.setUpSelectedPointInfoPanel();

            const zoomToLocationControl = new ZoomToLocationControl();
            this.map.addControl(zoomToLocationControl);

            const closeMapControl = new CloseMapControl({onClose: () => {this.closeModal();}});
            this.map.addControl(closeMapControl);

            const legendControl = new LegendControl({typeColorMap: this.typeColorMap, typeNameMap: this.typeNameMap});
            this.map.addControl(legendControl);

            // Add points to the map
            geoJSON(this.geoJsonData, {
                filter: (feature) => feature.geometry.type === "Point",
                pointToLayer: (feature, latlng) => {
                    return circleMarker(latlng, this.styleOfPointMarker(feature.properties.pointOfInterestTypeId));
                },
                onEachFeature: (feature, layer) => {
                    layer.bindPopup(this.popupContent(feature));
                    layer.on('click', () => this.displaySelectedPoint(feature, layer));
                }
            }).addTo(this.map);

            // Enable adding new points if allowed
            if (this.allowNewPoints) {
                this.enablePointCreation();
            }
            this.isMapInitialized = true;
        }
    }

    /**
     * Create information panel for selected point, initially hidden.
     */
    setUpSelectedPointInfoPanel() {
        const selectedPointInfoHtml = `
        <div id="selected-point-info-panel" style="display: none;">
            <div id="infoMessage"></div>
            <button id="confirmButton" class="btn btn-primary">${this.translations.selectLocation}</button>
        </div>`;
        document.getElementById(this.mapContainerId).insertAdjacentHTML('beforeend', selectedPointInfoHtml);
    }

    /**
     * Display the panel which contains information about the currently selected point,
     * and a button to bring you back to the original page.
     */
    displaySelectedPointInfo(feature) {
        console.info("Display selected point", feature);
        const name = feature.properties.pointOfInterestName;
        const type = this.typeNameMap[feature.properties.pointOfInterestTypeId];
        const coordinates = feature.geometry.coordinates[1].toFixed(5) + ", " + feature.geometry.coordinates[0].toFixed(5);

        const selectedPointInfo = document.getElementById('selected-point-info-panel');
        const infoMessage = document.getElementById('infoMessage');

        infoMessage.innerHTML = `<strong>${name}</strong><br>${coordinates}<br>${type}`;
        selectedPointInfo.style.display = 'block';

        const confirmButton = document.getElementById('confirmButton');
        confirmButton.style.display = 'block';
        confirmButton.onclick = () => {
            this.confirmSelection(feature);
        };
    }

    hideSelectedPointInfo() {
        const selectedPointInfo = document.getElementById('selected-point-info-panel');
        selectedPointInfo.style.display = 'none';
    }

    selectPointById(pointOfInterestId) {
        const selectedFeature = this.getFeatureById(pointOfInterestId);
        const selectedLayer = this.getLayerById(pointOfInterestId);
        this.displaySelectedPoint(selectedFeature, selectedLayer, true);
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
        this.selectedExistingPointMarker.setStyle(this.styleOfSelectedPointMarker());
        if (zoomInToSelected) {
            const latLng = this.selectedExistingPointMarker.getLatLng();
            this.map.setView(latLng, 10);
        }

        this.displaySelectedPointInfo(feature)
    }

    confirmSelection(feature) {
        if (typeof this.callbackOnClose === 'function') {
            const pointData = {
                id: feature.properties.pointOfInterestId,
                name: feature.properties.pointOfInterestName,
                typeId: feature.properties.pointOfInterestTypeId,
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

            // Click on the map should remove any previously selections
            this.removeSelectedPointMarkerIfExists();
            this.removeNewPointMarkerIfExists();
            this.closeNewPointFormIfOpen();
            this.hideSelectedPointInfo();

            // Calculate the pixel position from the map's click event
            const containerPoint = this.map.latLngToContainerPoint(latlng);

            this.selectedNewPointMarker = circleMarker(latlng, this.styleOfSelectedPointMarker()).addTo(this.map);
            const newPointFormElement = this.addHtmlElementNewPointForm(containerPoint.x + 25, containerPoint.y - 25, latlng.lat, latlng.lng)

            // Click inside the form should not propagate to underlying map
            DomEvent.disableClickPropagation(newPointFormElement);
            // Add event listener to close the form if clicked outside
            document.addEventListener('click', this.handleClickOutsidePointForm.bind(this), true);

            const closeButton = newPointFormElement.querySelector("#close-button");
            const nameInput = newPointFormElement.querySelector('#name');
            const latitudeInput = newPointFormElement.querySelector('#latitude');
            const longitudeInput = newPointFormElement.querySelector('#longitude');
            const typeInput = newPointFormElement.querySelector('#type');
            const submitButton = newPointFormElement.querySelector('#submit-button');

            const validateInputs = () => {
                const isValidLat = !isNaN(parseFloat(latitudeInput.value)) && isFinite(latitudeInput.value);
                const isValidLng = !isNaN(parseFloat(longitudeInput.value)) && isFinite(longitudeInput.value);
                submitButton.disabled = !(isValidLat && isValidLng);
            };
            latitudeInput.addEventListener('input', validateInputs);
            longitudeInput.addEventListener('input', validateInputs);
            validateInputs();

            closeButton.addEventListener('click', () => {
                newPointFormElement.remove();
                this.removeNewPointMarkerIfExists();
            });

            submitButton.addEventListener('click', () => {
                const feature = this.createFeatureForPoint(nameInput.value, parseInt(typeInput.value, 10), parseFloat(latitudeInput.value), parseFloat(longitudeInput.value));
                this.displaySelectedPointInfo(feature);
                newPointFormElement.remove();
            });
        });
    }

    removeNewPointMarkerIfExists() {
        if (this.selectedNewPointMarker) {
            console.info("Remove selected new point marker", this.selectedNewPointMarker);
            this.map.removeLayer(this.selectedNewPointMarker);
            this.currentMarker = null;
        }
    }

    removeSelectedPointMarkerIfExists(){
        if (this.selectedExistingPointMarker) {
            console.info("Remove selected existing point marker", this.selectedExistingPointMarker);
            const pointOfInterestTypeId = this.selectedExistingPointMarker.feature.properties.pointOfInterestTypeId;
            this.selectedExistingPointMarker.setStyle(this.styleOfPointMarker(pointOfInterestTypeId));
        }
    }

    handleClickOutsidePointForm(event) {
        const formElement = document.getElementById('newPointForm');
        if (formElement && !formElement.contains(event.target)) {
            this.closeNewPointFormIfOpen();
        }
    }

    closeNewPointFormIfOpen() {
        const formElement = document.getElementById('newPointForm');
        if (formElement) {
            formElement.remove();
        }
        document.removeEventListener('click', this.handleClickOutsidePointForm.bind(this), true);
    }

    createFeatureForPoint(name, type, longitude, latitude) {
        console.info("Create feature for [" + longitude + "," + latitude + "," + name + "," + type + "]");
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
        const localizedTypeName = this.typeNameMap[feature.properties.pointOfInterestTypeId];
        const coordinates = feature.geometry.coordinates[1].toFixed(5) + ", " + feature.geometry.coordinates[0].toFixed(5);
        return `<div id="poiPopup">
                <strong>${feature.properties.pointOfInterestName}</strong><br>${coordinates}<br>${localizedTypeName}
                </div>`;
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
        const html = `
            <div id="newPointForm" class="panel panel-default"  style="top: ${positionY}px; left: ${positionX}px;">
                <div class="panel-heading">
                    <h3 class="panel-title">${this.translations.createNewLocation}</h3>
                    <span id="close-button" style="position: absolute; top: 5px; right: 10px; cursor: pointer; font-size: 18px;">&times;</span>
                </div>
                <div class="panel-body">
                    <div class="form-group">
                        <label for="name">${this.translations.name}:</label>
                        <input type="text" class="form-control" id="name" name="name">
                    </div>
                    <div class="form-group">
                        <label for="latitude">${this.translations.latitude}:</label>
                        <input type="text" class="form-control" id="latitude" name="latitude" value="${latitude}">
                    </div>
                    <div class="form-group">
                        <label for="longitude">${this.translations.longitude}:</label>
                        <input type="text" class="form-control" id="longitude" name="longitude" value="${longitude}">
                    </div>
                    <div class="form-group">
                        <label for="poiTypeSelect">${this.translations.type}:</label>
                        <select class="form-control" id="type" name="type">
                            <option value="2">${this.typeNameMap[2]}</option>
                            <option value="3">${this.typeNameMap[3]}</option>
                            <option value="5">${this.typeNameMap[5]}</option>
                        </select>
                    </div>
                    <div class="form-group text-right">
                        <button id="submit-button" class="btn btn-primary">${this.translations.submitLocation}</button>
                    </div>                
                </div>
            </div>`;
        const tmpContainer = document.createElement("div");
        tmpContainer.innerHTML = html;
        const htmlElement = tmpContainer.querySelector('#newPointForm');
        document.getElementById(this.mapContainerId).appendChild(htmlElement);
        return htmlElement;
    }

    openModal(selectedPointOfInterestId) {
        document.getElementById(this.mapModalId).style.display = 'block';
        this.initMap();
        if(selectedPointOfInterestId) {
            this.selectPointById(selectedPointOfInterestId);
        }
    }

    closeModal() {
        document.getElementById(this.mapModalId).style.display = 'none';
        if (this.map) {
            this.map.remove();
            this.map = null;
        }
    }
}

const ZoomToLocationControl = Control.extend({
    options: {
        position: 'topleft'
    },

    onAdd: function(map) {
        // Create the button element
        const container = DomUtil.create('div', 'leaflet-bar leaflet-control');
        const button = DomUtil.create('a', 'zoom-to-my-location-button', container);
        button.innerHTML = 'Zoom';
        button.href = '#';
        button.title = 'Zoom to My Location';

        DomEvent.disableClickPropagation(button);

        DomEvent.on(button, 'click', DomEvent.stop)
            .on(button, 'click', DomEvent.preventDefault)
            .on(button, 'click', () => {
                if (navigator.geolocation) {
                    navigator.geolocation.getCurrentPosition((position) => {
                        const latitude = position.coords.latitude;
                        const longitude = position.coords.longitude;

                        map.setView([latitude, longitude], 13);

                        // Add a marker at the user's location
                        marker([latitude, longitude]).addTo(map);
                    }, (error) => {
                        console.error('Geolocation failed: ' + error.message);
                        alert('Unable to retrieve your location.');
                    });
                } else {
                    alert('Geolocation is not supported by this browser.');
                }
            });
        return container;
    }
});


const CloseMapControl = Control.extend({
    options: {
        position: 'topright'
    },

    onAdd: function(map) {
        // Create a container for the button
        const container = DomUtil.create('div', 'leaflet-bar leaflet-control');

        // Create the button element
        const button = DomUtil.create('a', 'close-map-button', container);
        button.innerHTML = '×'; // Unicode for close symbol
        button.href = '#';
        button.title = 'Close Map';

        // Disable click propagation and default behavior
        DomEvent.disableClickPropagation(button);
        DomEvent.on(button, 'click', DomEvent.preventDefault);
        DomEvent.on(button, 'click', () => {
            this.options.onClose();
        });
        return container;
    }
});

const LegendControl = Control.extend({
        options: {
            position: 'bottomright',
            typeColorMap: {},
            typeNameMap: {}
        },

        onAdd: function(map){
            const div = DomUtil.create('div', 'info legend');
            const typeColorMap = this.options.typeColorMap;
            const typeNameMap = this.options.typeNameMap;

            for (const type in typeColorMap) {
                const color = typeColorMap[type];
                div.innerHTML += `
                <div class="legend-item">
                    <i style="background:${color};"></i>
                    <span>${typeNameMap[type]}</span>
                </div>`;
            }

            return div;
        }
});

// Export the module
export default MapModal;