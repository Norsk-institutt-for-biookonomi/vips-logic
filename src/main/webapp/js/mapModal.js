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
 * 3) Include MapModal within a script tag of type 'module', and implement the function openLocationMap()
 * <script type="module">
 *    import MapModal from 'https://logic.vips.nibio.no/js/mapModal.js';
 *    window.openLocationMap = () => {  // Makes the function globally available
 *      let poiIds = locationList.map(poi => poi.pointOfInterestId);
 *      fetch("/rest/poi/geojson", {
 *          method: 'POST',
 *              headers: {
 *                  'Content-Type': 'application/json',
 *                  'Accept': 'application/json'
 *              },
 *              body: JSON.stringify(poiIds)
 *      })
 *          .then(response => response.json())
 *          .then(geoJson => {
 *              const locationMapInstance = new MapModal('location-map', typeNameMap, geoJson, '${currentLanguage}', true, callbackOnCloseLocationMap);
 *              const selectedPoiId = getSelectedPoiId("locationPointOfInterestId");
 *              locationMapInstance.openModal(selectedPoiId);
 *          })
 *          .catch(error => {
 *              console.error('Unable to convert poi list to GeoJson, cannot open map', error);
 *          });
 *      }
 * </script>
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
            closeMap: 'Lukk kart'
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
            closeMap: 'Close Map'
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
        this.mapModalElement = document.getElementById(mapModalId);
        this.mapContainerId = mapModalId + "-container";
        this.mapContainerElement = this.addMapContainer(this.mapModalElement, this.mapContainerId);
        this.typeNameMap = typeNameMap;
        this.geoJsonData = geoJsonData;
        if(language in MapModal.TRANSLATIONS) {
            this.translations = MapModal.TRANSLATIONS[language];
        } else {
            throw new Error("'"+ language +"' is not a supported language");
        }
        this.allowNewPoints = allowNewPoints;
        this.callbackOnClose = callbackOnClose;

        this.map = null;
        this.isMapInitialized = false;
        this.markersByType = {};
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

    addMapContainer(parentDiv, id) {
        const mapContainer = document.createElement('div');
        mapContainer.id = id;
        mapContainer.style.height = '100vh';
        mapContainer.style.width = '100%';
        mapContainer.style.position = 'relative';
        parentDiv.appendChild(mapContainer);
        return mapContainer;
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
        if (this.isMapInitialized) {
            console.error("Map is already initialized");
            return;
        }
        // Initialize the map centered on Norway
        this.map = map(this.mapContainerId).setView([63.4226, 10.3951], 5);
        tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19
        }).addTo(this.map);
        console.info("Create map " + this.mapContainerId + " with points", this.geoJsonData);

        this.setUpSelectedPointInfoPanel();

        // Add points to the map
        geoJSON(this.geoJsonData, {
            filter: (feature) => feature.geometry.type === "Point",
            pointToLayer: (feature, latlng) => {
                return circleMarker(latlng, this.styleOfPointMarker(feature.properties.pointOfInterestTypeId));
            },
            onEachFeature: (feature, layer) => {
                const typeId = feature.properties.pointOfInterestTypeId;

                layer.bindPopup(this.popupContent(feature));
                layer.on('click', () => this.displaySelectedPoint(feature, layer));

                // Add the marker to the markersByType object
                if (!this.markersByType[typeId]) {
                    this.markersByType[typeId] = [];
                }
                this.markersByType[typeId].push(layer);
            }
        }).addTo(this.map);

        const zoomToLocationControl = new ZoomToLocationControl({translations: this.translations});
        this.map.addControl(zoomToLocationControl);

        const closeMapControl = new CloseMapControl({
            translations: this.translations,
            onClose: () => {
                this.closeModal();
            }
        });
        this.map.addControl(closeMapControl);

        const legendControl = new LegendControl({
            typeColorMap: this.typeColorMap,
            typeNameMap: this.typeNameMap,
            markersByType: this.markersByType
        });
        this.map.addControl(legendControl);

        // Enable adding new points if allowed
        if (this.allowNewPoints) {
            this.enablePointCreation();
        }
        this.isMapInitialized = true;
    }

    /**
     * Create information panel for selected point, initially hidden.
     */
    setUpSelectedPointInfoPanel() {
        console.info("Set up selected point info panel");
        const selectedPointInfoHtml = `
        <div id="selected-point-info-panel" style="display: none;">
            <div id="infoMessage"></div>
            <button id="confirmButton" class="btn btn-primary">${this.translations.selectLocation}</button>
        </div>`;
        this.mapContainerElement.insertAdjacentHTML('beforeend', selectedPointInfoHtml);
    }

    /**
     * Display the panel which contains information about the currently selected point,
     * and a button to bring you back to the original page.
     */
    displaySelectedPointInfo(feature) {
        console.info("Display selected point", feature);
        const name = feature.properties.pointOfInterestName;
        const type = this.typeNameMap[feature.properties.pointOfInterestTypeId];
        const latitude = feature.geometry.coordinates[1].toFixed(5);
        const longitude = feature.geometry.coordinates[0].toFixed(5);

        const selectedPointInfo = document.getElementById('selected-point-info-panel');
        const infoMessage = document.getElementById('infoMessage');
        console.info("Add info message to panel", selectedPointInfo);

        infoMessage.innerHTML = `
            <h4>${name}</h4>
            <b>${this.translations.latitude}</b> ${latitude}<br>
            <b>${this.translations.longitude}</b> ${longitude}<br>
            <b>${this.translations.type}</b> ${type}`;
        selectedPointInfo.style.display = 'block';

        const confirmButton = document.getElementById('confirmButton');
        confirmButton.innerHTML = feature.properties.pointOfInterestId ? this.translations.selectLocation : this.translations.saveLocation;
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
                this.displaySelectedPointInfo(feature);
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
            this.currentMarker = null;
        }
    }

    removeSelectedPointMarkerIfExists() {
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
        const latitude = feature.geometry.coordinates[1].toFixed(5);
        const longitude = feature.geometry.coordinates[0].toFixed(5);
        const localizedTypeName = this.typeNameMap[feature.properties.pointOfInterestTypeId];
        return `<div id="poiPopup">
                <h4>${feature.properties.pointOfInterestName}</h4>
                <b>${this.translations.latitude}</b> ${latitude}<br>
                <b>${this.translations.longitude}</b> ${longitude}<br>
                <b>${this.translations.type}</b> ${localizedTypeName}
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
                    <h4 class="panel-title">${this.translations.createNewLocation}</h4>
                    <span id="close-button" style="position: absolute; top: 5px; right: 10px; cursor: pointer; font-size: 18px;">&times;</span>
                </div>
                <div class="panel-body">
                    <div class="form-group">
                        <label for="name">${this.translations.name}:</label>
                        <input type="text" class="form-control" id="name" name="name">
                    </div>
                    <div class="form-group">
                        <label for="latitude">${this.translations.latitude}:</label>
                        <input type="text" class="form-control" id="latitude" name="latitude" value="${latitude.toFixed(5)}">
                    </div>
                    <div class="form-group">
                        <label for="longitude">${this.translations.longitude}:</label>
                        <input type="text" class="form-control" id="longitude" name="longitude" value="${longitude.toFixed(5)}">
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
        this.mapContainerElement.appendChild(htmlElement);
        return htmlElement;
    }

    /**
     * Make modal visible. Initialise map with selected point marked, if given.
     *
     * @param selectedPointOfInterestId
     */
    openModal(selectedPointOfInterestId) {
        this.mapModalElement.style.display = 'block';
        this.initMap();
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
        const button = DomUtil.create('a', 'zoom-to-my-location-button', container);
        button.innerHTML = '<i class="fa fa-map-marker"></i>';
        button.href = '#';
        button.title = this.options.translations.zoomToLocation;

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
        const button = DomUtil.create('a', 'close-map-button', container);
        button.innerHTML = '×'; // Unicode for close symbol
        button.href = '#';
        button.title = this.options.translations.closeMap;

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
        typeNameMap: {},
        markersByType: {}
    },

    onAdd: function (map) {
        const div = DomUtil.create('div', 'info legend');
        const typeColorMap = this.options.typeColorMap;
        const typeNameMap = this.options.typeNameMap;
        const markersByType = this.options.markersByType;

        DomEvent.disableClickPropagation(div);

        for (const type in typeColorMap) {
            if (!markersByType[type]) {
                console.info("No markers found for " + typeNameMap[type]);
                continue;
            }
            const count = markersByType[type].length;
            const color = typeColorMap[type];

            const itemDiv = DomUtil.create('div', 'legend-item', div);
            itemDiv.innerHTML = `<i style="background:${color};"></i>
                    <span>${typeNameMap[type]} (${count})</span>`;

            // Add click listener to toggle markers visibility
            DomEvent.on(itemDiv, 'click', (e) => {
                e.stopPropagation();
                this.toggleMarkers(type, markersByType[type], itemDiv);
            });
        }
        return div;
    },

    toggleMarkers: function (type, markers, itemDiv) {
        if (!markers) return;
        const wasVisible = markers[0].options.opacity !== 0;
        const isVisible = !wasVisible;

        markers.forEach(marker => {
            if (wasVisible) {
                marker.setStyle({opacity: 0, fillOpacity: 0});
            } else {
                marker.setStyle({opacity: 1, fillOpacity: 0.8});
            }
        });
        // Make legend text grey if hidden
        itemDiv.style.color = isVisible ? "#000000" : "#B0B0B0";
    }

});

// Export the module
export default MapModal;