// mapModal.js
import {
    map,
    tileLayer,
    marker,
    geoJSON,
    circleMarker,
    DomEvent
} from 'https://unpkg.com/leaflet/dist/leaflet-src.esm.js';

/**
 * Uses css classes from bootstrap 3.4.1
 *
 */
class MapModal {

    constructor(mapContainerId, typeNameMap, geoJsonData, allowNewPoints = false, callback = null) {
        this.mapContainerId = mapContainerId;
        this.typeNameMap = typeNameMap;
        this.geojsonData = geoJsonData;
        this.allowNewPoints = allowNewPoints;
        this.map = null;
        this.isMapInitialized = false;
        this.selectedPointLayer = null;
        this.createdPointLayer = null;
        this.createdPoints = [];
        this.callback = callback;

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
            // Add a tile layer
            tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                maxZoom: 19
            }).addTo(this.map);

            this.createSelectedPointInfo();

            // Add points to the map
            geoJSON(this.geojsonData, {
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

    createSelectedPointInfo() {
        console.debug("Create information panel for selected point, initially hidden");
        const selectedPointInfoHtml = `
        <div id="selectedPointInfo" style="display: none;">
            <div id="infoMessage"></div>
            <button id="confirmButton" class="btn btn-primary">OK</button>
        </div>
    `   ;
        document.getElementById(this.mapContainerId).insertAdjacentHTML('beforeend', selectedPointInfoHtml);
    }

    updateSelectedPointInfo(feature) {
        const name = feature.properties.pointOfInterestName;
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

    selectPoint(feature, layer) {
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

        this.updateSelectedPointInfo(feature)
        this.selectedPointLayer = layer;
    }

    confirmSelection(feature) {
        console.info("Save pointData", feature);
        const pointData = {
            id: feature.properties.pointOfInterestId,
            name: feature.properties.pointOfInterestName,
            typeId: feature.properties.pointOfInterestTypeId,
            altitude: feature.properties.pointOfInterestAltitude,
            longitude: feature.geometry.coordinates[0],
            latitude: feature.geometry.coordinates[1]
        };
        if (typeof this.callback === 'function') {
            console.info("Callback!", pointData);
            this.callback(pointData);
        }
        this.closeModal();
    }

    enablePointCreation() {
        this.map.on('click', (e) => {
            console.log("Map clicked, enabling point creation...");
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
                        <input type="text" class="form-control" id="name" name="name">
                    </div>
                    <div class="form-group">
                        <label for="altitude">Altitude:</label>
                        <input type="text" class="form-control" id="altitude" name="altitude">
                    </div>
                    <div class="form-group">
                        <label for="poiTypeSelect">Type:</label>
                        <select class="form-control" id="poiTypeSelect" name="poiTypeSelect"></select>
                    </div>
                    <div class="form-group text-right">
                        <button id="savePointButton" class="btn btn-primary">Save</button>
                    </div>                
                </div>
            </div>`;
            document.getElementById(this.mapContainerId).insertAdjacentHTML('beforeend', formHtml);

            const typeSelectElement = document.getElementById("poiTypeSelect");
            typeSelectElement.innerHTML = '';
            for (const [id, name] of Object.entries(this.typeNameMap)) {
                const option = document.createElement('option');
                option.value = id;
                option.textContent = name;
                typeSelectElement.appendChild(option);
            }

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

    savePoint(lat, lng) {
        const nameElement = document.getElementById('name');
        const altitudeElement = document.getElementById('altitude');
        const poiTypeSelectElement = document.getElementById("poiTypeSelect")

        if (nameElement && poiTypeSelectElement) {
            const poiName = nameElement.value;
            const poiAltitude = altitudeElement.value;
            const poiType = poiTypeSelectElement.value;

            if (this.createdPointLayer) {
                this.map.removeLayer(this.createdPointLayer);
            }

            const newPoint = {
                "type": "Feature",
                "geometry": {
                    "type": "Point",
                    "coordinates": [lng, lat]
                },
                "properties": {
                    "pointOfInterestName": poiName,
                    "pointOfInterestAltitude": poiAltitude,
                    "pointOfInterestTypeId": poiType
                }
            };
            if (this.createdPoints.length > 0) {
                this.createdPoints.pop(); // Remove the last created point from the list
            }
            this.createdPoints.push(newPoint);
            this.createdPointLayer = geoJSON(newPoint, {
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