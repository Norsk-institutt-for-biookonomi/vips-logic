// mapModal.js
import {
    map,
    tileLayer,
    geoJSON,
    circleMarker, GeoJSON,
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
            0: "#B3CDE0",  // Light blue
            1: "#FBB4AE",  // Soft pink
            2: "#CCEBC5",  // Light green
            3: "#DECBE4",  // Lavender
            5: "#FED9A6",  // Light peach
            6: "#FFFFCC",  // Pale yellow
            7: "#E5D8BD"   // Beige
        };

    }

    initMap() {
        if (!this.isMapInitialized) {
            // Initialize the map centered on Norway
            this.map = map(this.mapContainerId).setView([63.4226, 10.3951], 5);
            tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19
            }).addTo(this.map);

            this.createSelectedPointInfo();

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
    createSelectedPointInfo() {
        const selectedPointInfoHtml = `
        <div id="selectedPointInfo" style="display: none;">
            <div id="infoMessage"></div>
            <button id="confirmButton" class="btn btn-primary">Velg sted</button>
        </div>`;
        document.getElementById(this.mapContainerId).insertAdjacentHTML('beforeend', selectedPointInfoHtml);
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
        let feature = this.geoJsonData.features.find(feature => feature.properties.pointOfInterestId == pointOfInterestId);
        if (!feature) {
            console.info("Feature with id=" + pointOfInterestId + " not found, assume that the user wants the newly created", this.createdPoints[0]);
            return this.createdPoints.length > 0 ? this.createdPoints[0] : null;
        }
        return feature;
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
        if (!result) {
            console.info("Layer with id=" + pointOfInterestId + " not found, assume that the user wants the newly created", this.createdPointLayer);
            this.createdPointLayer.eachLayer((layer) => {
                result = layer;
            });
        }
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
                color: "#FFFFFF",  // Border color
                weight: 2,  // Border thickness
                opacity: 1,
                fillOpacity: 0.8
            });
        }

        // Highlight the new selected point
        layer.setStyle({
            fillColor: 'red',
            color: '#f00'
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

            // Show form for creating a new point
            const formHtml = `
            <div id="pointForm" class="panel panel-default"  style="top: ${containerPoint.y}px; left: ${containerPoint.x}px;">
                <div class="panel-heading">
                    <h3 class="panel-title">Create new point</h3>
                </div>
                <div class="panel-body">
                    <div class="form-group">
                        <label for="name">Name:</label>
                        <input type="text" class="form-control" id="name" name="name" autofocus>
                    </div>
                    <div class="form-group">
                        <label for="poiTypeSelect">Type:</label>
                        <select class="form-control" id="poiTypeSelect" name="poiTypeSelect">
                            <option value="2">${this.typeNameMap[2]}</option>
                            <option value="3">${this.typeNameMap[3]}</option>
                            <option value="5">${this.typeNameMap[5]}</option>
                        </select>
                    </div>
                    <div class="form-group text-right">
                        <button id="savePointButton" class="btn btn-primary">Save</button>
                    </div>                
                </div>
            </div>`;
            document.getElementById(this.mapContainerId).insertAdjacentHTML('beforeend', formHtml);

            const formElement = document.getElementById('pointForm');
            DomEvent.disableClickPropagation(formElement);

            // Add event listener to close the form if clicked outside
            document.addEventListener('click', this.handleClickOutsidePointForm.bind(this), true);

            document.getElementById('savePointButton').addEventListener('click', () => {
                this.savePoint(latlng.lat, latlng.lng);
            });
        });
    }

    handleClickOutsidePointForm(event) {
        const formElement = document.getElementById('pointForm');

        // If the clicked element is not inside the form, close the form
        if (formElement && !formElement.contains(event.target)) {
            this.closeNewPointFormIfOpen();
        }
    }

    closeNewPointFormIfOpen() {
        const formElement = document.getElementById('pointForm');
        if (formElement) {
            formElement.remove();
        }

        // Remove the event listener after closing the form
        document.removeEventListener('click', this.handleClickOutsidePointForm.bind(this), true);
    }

    createFeatureForPoint(lng, lat, poiName, poiType) {
        return {
            "type": "Feature",
            "geometry": {
                "type": "Point",
                "coordinates": [lng, lat]
            },
            "properties": {
                "pointOfInterestName": poiName,
                "pointOfInterestTypeId": poiType
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

    savePoint(lat, lng) {
        const poiNameElement = document.getElementById('name');
        const poiTypeElement = document.getElementById("poiTypeSelect")

        if (poiNameElement && poiTypeElement) {
            const poiName = poiNameElement.value;
            const poiType = parseInt(poiTypeElement.value, 10);

            // There should only be one newly created point available
            if (this.createdPointLayer) {
                this.map.removeLayer(this.createdPointLayer);
            }
            if (this.createdPoints.length > 0) {
                this.createdPoints.pop();
            }

            const newPoint = this.createFeatureForPoint(lng, lat, poiName, poiType);
            this.createdPoints.push(newPoint);
            this.createdPointLayer = this.addNewPointToMap(newPoint);
            this.createdPointLayer.eachLayer((layer) => {
                this.selectPoint(newPoint, layer);
            });

            const formElement = document.getElementById('pointForm');
            if (formElement) {
                formElement.remove();
            }
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

    setSelectedLocation(selectedValue) {
        this.selectPointById(selectedValue);
    }

    openModal(points) {
        document.getElementById('mapModal').style.display = 'block';
        this.initMap();
    }

    closeModal() {
        document.getElementById('mapModal').style.display = 'none';
    }
}

// Export the module
export default MapModal;