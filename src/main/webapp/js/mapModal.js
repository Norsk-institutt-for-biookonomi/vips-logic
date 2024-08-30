// mapModal.js
import {
    map,
    tileLayer,
    geoJSON,
    circleMarker,
    marker,
    GeoJSON,
    DomEvent
} from 'https://unpkg.com/leaflet/dist/leaflet-src.esm.js';

/**
 * Uses css classes from bootstrap 3.4.1
 *
 */
class MapModal {

    /**
     * @param mapContainerId    The id of the HTML element to which the map should be added
     * @param typeNameMap   A mapping from pointOfInterestTypeIds to their localized names
     * @param geoJsonData   GeoJson containing all features which should be displayed on the map
     * @param allowNewPoints    Whether or not the user should be allowed to add new points
     * @param callbackOnPersistNew  Callback function for persisting newly created point
     * @param callbackOnClose   Callback function to call when closing the modal
     */
    constructor(mapContainerId, typeNameMap, geoJsonData, allowNewPoints = false, callbackOnPersistNew = null, callbackOnClose = null) {
        this.mapContainerId = mapContainerId;
        this.typeNameMap = typeNameMap;
        this.geoJsonData = geoJsonData;
        this.allowNewPoints = allowNewPoints;
        this.callbackOnPersistNew = callbackOnPersistNew;
        this.callbackOnClose = callbackOnClose;

        this.map = null;
        this.isMapInitialized = false;
        this.selectedPointLayer = null;
        this.createdPointLayer = null;
        this.createdPoints = [];

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

    initMap() {
        if (!this.isMapInitialized) {
            // Initialize the map centered on Norway
            this.map = map(this.mapContainerId).setView([63.4226, 10.3951], 5);
            tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19
            }).addTo(this.map);

            this.setUpSelectedPointInfoPanel();
            this.setUpZoomToCurrentLocation();

            // Add points to the map
            geoJSON(this.geoJsonData, {
                filter: (feature) => feature.geometry.type === "Point",
                pointToLayer: (feature, latlng) => {
                    const color = this.typeColorMap[feature.properties.pointOfInterestTypeId] || "#3498DB";
                    return circleMarker(latlng, {
                        radius: 8,  // Size of the marker
                        fillColor: color,
                        color: "#FFFFFF",  // Border color
                        weight: 2,  // Border thickness
                        opacity: 1,
                        fillOpacity: 0.8
                    });
                },
                onEachFeature: (feature, layer) => {
                    layer.bindPopup(this.popupContent(feature));
                    layer.on('click', () => this.selectPoint(feature, layer));
                }
            }).addTo(this.map);

            // Enable adding new points if allowed
            if (this.allowNewPoints) {
                console.info("Enable point creation")
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
        <div id="selectedPointInfo" style="display: none;">
            <div id="infoMessage"></div>
            <button id="confirmButton" class="btn btn-primary">Velg sted</button>
        </div>`;
        document.getElementById(this.mapContainerId).insertAdjacentHTML('beforeend', selectedPointInfoHtml);
    }

    setUpZoomToCurrentLocation() {
        const zoomButtonHtml = `<button id="zoomToMyLocationButton" class="btn btn-primary">Zoom til meg</button>`;
        document.getElementById(this.mapContainerId).insertAdjacentHTML('beforeend', zoomButtonHtml);

        let zoomButton = document.getElementById('zoomToMyLocationButton')
        DomEvent.disableClickPropagation(zoomButton);
        zoomButton.addEventListener('click', () => {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition((position) => {
                    const latitude = position.coords.latitude;
                    const longitude = position.coords.longitude;

                    this.map.setView([latitude, longitude], 13);

                    // Add a marker at the user's location
                    const userLocationMarker = marker([latitude, longitude]).addTo(this.map);
                }, (error) => {
                    console.error('Geolocation failed: ' + error.message);
                    alert('Unable to retrieve your location.');
                });
            } else {
                alert('Geolocation is not supported by this browser.');
            }
        });
    }

    /**
     * Display the panel which contains information about the currently selected point,
     * and a button to bring you back to the original page.
     */
    displaySelectedPointInfo(feature) {
        const name = feature.properties.pointOfInterestName;
        console.info("Display selected point " + name);
        const type = this.typeNameMap[feature.properties.pointOfInterestTypeId];
        const coordinates = feature.geometry.coordinates[1].toFixed(5) + ", " + feature.geometry.coordinates[0].toFixed(5);

        const selectedPointInfo = document.getElementById('selectedPointInfo');
        const infoMessage = document.getElementById('infoMessage');

        infoMessage.innerHTML = `<strong>${name}</strong><br>${coordinates}<br>${type}`;
        selectedPointInfo.style.display = 'block';

        const confirmButton = document.getElementById('confirmButton');
        confirmButton.style.display = 'block';
        confirmButton.onclick = () => {
            this.confirmSelection(feature);
        };
    }

    selectPointById(pointOfInterestId) {
        const selectedFeature = this.getFeatureById(pointOfInterestId);
        const selectedLayer = this.getLayerById(pointOfInterestId);
        this.selectPoint(selectedFeature, selectedLayer, true);
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

    selectPoint(feature, layer, zoomInToSelected = false) {
        console.info("Select point", feature);
        // Deselect previously selected point, if any
        if (this.selectedPointLayer) {
            const color = this.typeColorMap[feature.properties.pointOfInterestTypeId] || "#3498DB";
            this.selectedPointLayer.setStyle({
                radius: 8,  // Size of the marker
                fillColor: color,
                color: "#FFFFFF",
                weight: 2,
                opacity: 1,
                fillOpacity: 0.8
            });
        }

        // Highlight the new selected point
        layer.setStyle({
            radius: 12,
            fillColor: "#FF5733",
            fillOpacity: 1,
        });

        if (zoomInToSelected) {
            const latLng = layer.getLatLng();
            this.map.setView(latLng, 10);
        }

        this.displaySelectedPointInfo(feature)
        this.selectedPointLayer = layer;
    }

    confirmSelection(feature) {
        console.info("Confirm selection", feature);
        let poiId = feature.properties.pointOfInterestId;
        if (!poiId && typeof this.callbackOnPersistNew === 'function') {
            const pointData = {
                name: feature.properties.pointOfInterestName,
                typeId: feature.properties.pointOfInterestTypeId,
                longitude: feature.geometry.coordinates[0],
                latitude: feature.geometry.coordinates[1]
            };
            console.info("Persist new", pointData);
            this.callbackOnPersistNew(pointData);
        }
        if (typeof this.callbackOnClose === 'function') {
            this.callbackOnClose(poiId);
            console.info("Goodbye from map modal!")
        }
        this.closeModal();
    }

    enablePointCreation() {
        this.map.on('click', (e) => {
            const latlng = e.latlng;

            // If a form already exists, remove it
            this.closeNewPointFormIfOpen();

            // Calculate the pixel position from the map's click event
            const containerPoint = this.map.latLngToContainerPoint(latlng);
            const newPointFormElement = this.addHtmlElementNewPointForm(containerPoint.x, containerPoint.y, latlng.lat, latlng.lng)

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

            closeButton.addEventListener('click', function() {
                newPointFormElement.remove();
            });

            submitButton.addEventListener('click', () => {
                this.removeExistingNewPoint();
                this.setNewPointAsSelected(nameInput.value, parseFloat(latitudeInput.value), parseFloat(longitudeInput.value), parseInt(typeInput.value, 10));
                newPointFormElement.remove();
            });
        });
    }

    handleClickOutsidePointForm(event) {
        const formElement = document.getElementById('newPointForm');

        // If the clicked element is not inside the form, close the form
        if (formElement && !formElement.contains(event.target)) {
            this.closeNewPointFormIfOpen();
        }
    }

    closeNewPointFormIfOpen() {
        const formElement = document.getElementById('newPointForm');
        if (formElement) {
            formElement.remove();
        }

        // Remove the event listener after closing the form
        document.removeEventListener('click', this.handleClickOutsidePointForm.bind(this), true);
    }

    createFeatureForPoint(longitude, latitude, name, type) {
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

    addNewPointToMap(point) {
        console.info("Add new point to map", point);
        return geoJSON(point, {
            pointToLayer: (feature, latlng) => {
                return circleMarker(latlng, {
                    radius: 8,
                    fillColor: 'green',
                    color: '#000',
                    weight: 1,
                    opacity: 1,
                    fillOpacity: 0.8
                });
            },
            onEachFeature: (feature, layer) => {
                layer.bindPopup(this.popupContent(feature));
                layer.on('click', () => this.selectPoint(feature, layer));
            }
        }).addTo(this.map);
    }

    setNewPointAsSelected(name, latitude, longitude, type) {
        const feature = this.createFeatureForPoint(longitude, latitude, name, type);
        this.createdPoints.push(feature);
        this.createdPointLayer = this.addNewPointToMap(feature);
        this.createdPointLayer.eachLayer((layer) => {
            this.selectPoint(feature, layer);
        });
    }

    removeExistingNewPoint(){
        if (this.createdPointLayer) {
            this.map.removeLayer(this.createdPointLayer);
        }
        if (this.createdPoints.length > 0) {
            this.createdPoints.pop();
        }
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
                    <h3 class="panel-title">Opprett nytt sted</h3>
                    <span id="close-button" style="position: absolute; top: 5px; right: 10px; cursor: pointer; font-size: 18px;">&times;</span>
                </div>
                <div class="panel-body">
                    <div class="form-group">
                        <label for="name">Navn:</label>
                        <input type="text" class="form-control" id="name" name="name">
                    </div>
                    <div class="form-group">
                        <label for="latitude">Breddegrad:</label>
                        <input type="text" class="form-control" id="latitude" name="latitude" value="${latitude}">
                    </div>
                    <div class="form-group">
                        <label for="longitude">Lengdegrad:</label>
                        <input type="text" class="form-control" id="longitude" name="longitude" value="${longitude}">
                    </div>
                    <div class="form-group">
                        <label for="poiTypeSelect">Type:</label>
                        <select class="form-control" id="type" name="type">
                            <option value="2">${this.typeNameMap[2]}</option>
                            <option value="3">${this.typeNameMap[3]}</option>
                            <option value="5">${this.typeNameMap[5]}</option>
                        </select>
                    </div>
                    <div class="form-group text-right">
                        <button id="submit-button" class="btn btn-primary">OK</button>
                    </div>                
                </div>
            </div>`;
        const tmpContainer = document.createElement("div");
        tmpContainer.innerHTML = html;
        const htmlElement = tmpContainer.querySelector('#newPointForm');
        document.getElementById(this.mapContainerId).appendChild(htmlElement);
        return htmlElement;
    }

    /**
     * Function is called when newly created point of interest is successfully persisted to database.
     * @param pointOfInterestId
     */
    saveSuccess(pointOfInterestId) {
        if (this.createdPoints.length < 1 || !this.createdPointLayer) {
            console.error('No newly created points, unable to update with pointOfInterestId=' + pointOfInterestId);
            return;
        }
        let latestCreatedFeature = this.createdPoints[0];
        latestCreatedFeature.properties.pointOfInterestId = pointOfInterestId;
        this.geoJsonData.features.push(latestCreatedFeature);
        this.createdPoints = [];

        this.map.removeLayer(this.createdPointLayer);
        this.addNewPointToMap(latestCreatedFeature);
        this.createdPointLayer = null;

        console.info("this.createdPoints", this.createdPoints);
        console.info("this.createdPointLayer", this.createdPointLayer);
        console.info("this.geoJsonData.features", this.geoJsonData.features);
    }

    openModal(selectedPointOfInterestId) {
        if(selectedPointOfInterestId) {
            this.selectPointById(selectedPointOfInterestId);
        }
        document.getElementById('mapModal').style.display = 'block';
        this.initMap();
    }

    closeModal() {
        document.getElementById('mapModal').style.display = 'none';
    }
}

// Export the module
export default MapModal;