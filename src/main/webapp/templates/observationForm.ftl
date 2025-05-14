<#-- 
    Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
  
  This file is part of VIPSLogic.
  This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
--><#include "master.ftl">
<#macro page_head>
    <title><#if observation.observationId?has_content>${i18nBundle.editObservation}<#else>${i18nBundle.newObservation}</#if></title>
</#macro>
<#macro custom_css>
    <link rel="stylesheet" href="/css/3rdparty/chosen.min.css"/>
    <link rel="stylesheet" type="text/css" href="/css/3rdparty/jquery.datetimepicker.css"/>
    <link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css?t=20170623"/>
    <link rel="stylesheet" type="text/css" href="/css/3rdparty/ol-layerswitcher.css"/>
    <link rel="stylesheet" type="text/css" href="/css/map.css"/>
</#macro>
<#macro custom_js>
    <script type="text/javascript" src="/js/3rdparty/chosen.jquery.min.js"></script>
    <script type="text/javascript" src="/js/3rdparty/jquery.datetimepicker.js"></script>
    <script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
    <script type="text/javascript" src="/js/3rdparty/moment.min.js"></script>
    <script type="text/javascript" src="/js/3rdparty/jsoneditor.js"></script>
    <script type="text/javascript" src="/js/3rdparty/ol.js?t=20170623"></script>
    <script type="text/javascript" src="js/3rdparty/ol-layerswitcher.js"></script>

    <script type="text/javascript" src="/js/constants.js"></script>
    <script type="text/javascript" src="/js/resourcebundle.js"></script>
    <script type="text/javascript" src="/js/validateForm.js"></script>
    <script type="text/javascript" src="/js/util.js"></script>
    <script type="text/javascript" src="/js/objectGISInfoMap.js?t=20170623"></script>
    <script type="text/javascript" src="/js/poiFormMap.js"></script>
    <script type="text/javascript">
        var organizationId = ${user.organizationId.organizationId};
        var selectedCropId = <#if observation.cropOrganism?has_content>${observation.cropOrganism.organismId?c}<#else>null</#if>;


        $(document).ready(function () {

            // Load main form definition (for validation)
            loadFormDefinition("observationForm");

            // Initialize the map
            // If observation already registered center on location
            // Otherwise, center and zoom to organizations's default
            <#if observation.location?has_content>
            initGisInfoMap([${(observation.location.x?c)!""}, ${(observation.location.y?c)!""}], 10, true);
            <#elseif !observation.observationTimeSeries?has_content>
            var geoInfo = <#if observation.geoinfo?has_content>${observation.geoinfo}<#else>
            {
            }
            </#if>;
            var chooseFromMapLayers = {"chooseFromMapLayers": <#if mapLayers?has_content>${mapLayers}<#else>[]</#if>};
            initGisInfoMap("observationFormMap", [${defaultMapCenter.x?c}, ${defaultMapCenter.y?c}], ${defaultMapZoom}, false, geoInfo, chooseFromMapLayers);
            </#if>
            <#if editAccess!="W">
                GISInfoMap.removeInteraction(draw);
                document.getElementById("drawOptions").innerHTML = "";
            </#if>
            <#if observation.observationData?has_content>
                observationData = ${observation.observationData};
                getDataSchema(${observation.organism.organismId}, organizationId);
            <#elseif observation.organism?has_content>
                // Setting
                initObservationData(${observation.organism.organismId}, organizationId);
            </#if>
            <#if observation.observationTimeSeries?has_content>
                displayObservationTimeSeriesInfo("${observation.observationTimeSeries.cropOrganismId}", "${observation.observationTimeSeries.organismId}", "${observation.observationTimeSeries.locationPointOfInterestId}")
            </#if>

            // Activating file selection
            $('.btn-file :file').on('fileselect', function (event, numFiles, label) {

                var input = $(this).parents('.input-group').find(':text'),
                    log = numFiles > 1 ? numFiles + ' files selected' : label;

                if (input.length) {
                    input.val(log);
                } else {
                    if (log) alert(log);
                }

            });

            refreshLocationPointOfInterests(<#if observation.locationPointOfInterestId?has_content>${observation.locationPointOfInterestId}<#else>null</#if>);
            <#if ! observation.organism?has_content>
            initCropCategories();
            </#if>
            // Activating chosen plugin
            $(".chosen-select").chosen();

            // Show all available crops
            filterCrops(-1);
        });

        // Adding selected file name to a box
        $(document)
            .on('change', '.btn-file :file', function () {
                var input = $(this),
                    numFiles = input.get(0).files ? input.get(0).files.length : 1,
                    label = input.val().replace(/\\/g, '/').replace(/.*\//, '');
                input.trigger('fileselect', [numFiles, label]);
            });


        // Global for observationData & observationSchema
        let observationData = null;
        let observationSchema = null;
        // Make the quantification fields available
        const quantificationFields = document.getElementById("quantificationFields");
        let editor = null;

        function initObservationData(organismId, organizationId) {
            $.getJSON("/rest/observationdata/model/" + organizationId + "/" + organismId, function (json) {
                observationData = json;
                getDataSchema(organismId, organizationId);
            });
        }

        /**
         * Crop, pest and location should not be editable for observations belonging to time series. We avoid displaying
         * form elements in these cases, and rather display the values statically.
         */
        function displayObservationTimeSeriesInfo(cropOrganismId, organismId, locationPointOfInterestId) {
            document.getElementById("cropDisplayName").innerHTML = nameForCropOrganismId(cropOrganismId);
            document.getElementById("pestDisplayName").innerHTML = nameForOrganismId(organismId);
            fetchPOIs(function (allPois) {
                const locationName = nameForLocationPointOfInterestId(locationPointOfInterestId, allPois);
                document.getElementById("locationDisplayName").innerHTML = locationName || null;
            });
            initLocationMap(locationPointOfInterestId);
            initObservationData(organismId, organizationId);
        }

        function getDataSchema(organismId, organizationId) {
            $.getJSON("/rest/observationdata/schema/" + organizationId + "/" + organismId, buildForm);
        }

        // Populate the quantification fields using JSONEditor (https://github.com/json-editor/json-editor)
        const buildForm = function (retrievedSchema) {
            //console.info(retrievedSchema);
            //console.info(observationData);
            observationSchema = retrievedSchema;
            // Erasing whatever was there before
            if (editor != null) {
                editor.destroy();
            }
            editor = new JSONEditor(quantificationFields, {
                schema: observationSchema,
                theme: 'bootstrap4',
                disable_edit_json: true, // If true, remove all Edit JSON buttons from objects.
                disable_properties: true, // If true, remove all Edit Properties buttons from objects.
                remove_button_labels: true, // Display only icons in buttons. This works only if iconlib is set.
                disable_collapse: true // Make sure no elements can be collapsed
            });
            editor.on("ready", () => {
                editor.setValue(observationData);
            })
        };

        var updateCropPests = function () {
            var theForm = document.getElementById('observationForm');
            var selectedCropId = theForm["cropOrganismId"].options[theForm["cropOrganismId"].options.selectedIndex].value;
            // If this is not a new observation, or the selected crop is not in the database, we keep calm
            if (theForm["observationId"].value !== "-1" || selectedCropId == "-10") {
                return;
            }
            $.getJSON("/rest/organism/croppest/" + selectedCropId, function (json) {
                updateCropPestsCallback(json);
            })
                .fail(function () {
                    alert("Error getting pests for this crop. Please contact system administrator");
                });
        };

        var updateCropPestsCallback = function (cropPest) {
            var pestList = document.getElementById('observationForm')["organismId"];
            if (cropPest == null) {
                // Need to reorganize pests back to default
                var allPests = [];
                for (var i = 2; i < pestList.options.length; i++) {
                    allPests.push(pestList.options[i]);
                }
                allPests.sort(compareSelectListOptions);
                pestList.options.length = 2; // Keeping the top two
                for (var i = 0; i < allPests.length; i++) {
                    pestList.options[pestList.options.length] = allPests[i];
                }
            } else {
                var prioritatedPests = [];
                var unprioritatedPests = []
                for (var i = 2; i < pestList.options.length; i++) {
                    if (cropPest.pestOrganismIds.indexOf(parseInt(pestList.options[i].value)) >= 0) {
                        //console.log(pestList.options[i].value + " is prioritated");
                        prioritatedPests.push(pestList.options[i]);
                    } else if (pestList.options[i].value != "-1") // Avoiding the "---" option
                    {
                        //console.log(pestList.options[i].value + " is unprioritated");
                        unprioritatedPests.push(pestList.options[i]);
                    }

                }
                pestList.options.length = 2; // Keeping the top two
                for (var i = 0; i < prioritatedPests.length; i++) {
                    pestList.options[pestList.options.length] = prioritatedPests[i];
                }
                pestList.options[pestList.options.length] = new Option("---", "-1");
                for (var i = 0; i < unprioritatedPests.length; i++) {
                    pestList.options[pestList.options.length] = unprioritatedPests[i];
                }
            }
        };

        var thePopup;

        function addNewLocationPopup() {
            thePopup = window.open("/poi?action=newPoiForm&organizationId=1&returnIdCallback=refreshLocationPointOfInterests");
        }

        var allPois = [];


        function fetchPOIs(callback) {
            $.getJSON("/rest/poi/user", function (json) {
                callback(json);
            });
        }

        function buildPOIList(poiListElement, allPois, poiTypes, selectedPointOfInterestId) {
            poiListElement.options.length = 1;
            for (const [typeId, typeName] of Object.entries(poiTypes)) {
                poiListElement.options[poiListElement.options.length] = new Option("-- " + typeName + " --", "-1");

                for (let i = 0; i < allPois.length; i++) {
                    const poi = allPois[i];

                    if (poi.pointOfInterestTypeId == typeId) {
                        const poiOption = new Option(poi.name, poi.pointOfInterestId);
                        if (poi.pointOfInterestId === selectedPointOfInterestId) {
                            poiOption.selected = true;
                        }
                        poiListElement.options[poiListElement.options.length] = poiOption;
                    }
                }
            }
        }

        /**
         * Fetches locations, farms, fields, traps and weather stations, renders the list
         */
        function refreshLocationPointOfInterests(selectedPointOfInterestId) {
            const poiTypes = {
                0: "${i18nBundle.genericPlaces}",
                1: "${i18nBundle.weatherStations}",
                2: "${i18nBundle.farms}",
                3: "${i18nBundle.fields}",
                5: "${i18nBundle.traps}"
            };
            const poiListElement = document.getElementById("locationPointOfInterestId");
            if (!poiListElement) {
                return;
            }

            fetchPOIs(function (allPois) {
                buildPOIList(poiListElement, allPois, poiTypes, selectedPointOfInterestId);
                showCorrectMap();
            });
        }

        // If locationPointOfInterestId is selected OR observation drawing map is deliberately disabled,
        // show map for locations
        // If not, show the observation drawing map
        var hideObservationFormMap = <#if hideObservationFormMap??>${hideObservationFormMap?c}<#else>false</#if>;

        function showCorrectMap() {
            var locationList = document.getElementById("locationPointOfInterestId");
            if (hideObservationFormMap && locationList.options[locationList.options.selectedIndex].value == "-1") {

                document.getElementById("observationFormMap").style.display = "none";
                document.getElementById("poiFormMap").style.display = "block";
                initLocationMap(null);
            } else if (locationList.options[locationList.options.selectedIndex].value != "-1") {

                document.getElementById("observationFormMap").style.display = "none";
                document.getElementById("poiFormMap").style.display = "block";
                initLocationMap(locationList.options[locationList.options.selectedIndex].value);
            } else {

                document.getElementById("observationFormMap").style.display = "block";
                document.getElementById("poiFormMap").style.display = "none";
            }
        }

        function initLocationMap(locationPointOfInterestId) {
            if (locationPointOfInterestId != null) {
                $.getJSON("/rest/poi/" + locationPointOfInterestId, function (json) {
                        var poi = json;
                        if (map === undefined || map === null) {
                            initMap([poi.longitude, poi.latitude], 10, ${user.organizationId.organizationId}, locationPointOfInterestId, true);
                        } else {
                            map.getView().setCenter(ol.proj.transform([poi.longitude, poi.latitude], 'EPSG:4326', map.getView().getProjection().getCode()));
                            map.getView().setZoom(10);
                            stationMarker.setPosition(ol.proj.transform([poi.longitude, poi.latitude], 'EPSG:4326', map.getView().getProjection().getCode()));
                        }
                    }
                );
            } else {
                initMap([${defaultMapCenter.x?c}, ${defaultMapCenter.y?c}], ${defaultMapZoom}, ${user.organizationId.organizationId}, locationPointOfInterestId, true);
            }
        }

        function validateGIS(theForm) {
            var locationList = document.getElementById("locationPointOfInterestId");
            var geoInfo = document.getElementById("observationForm")["geoInfo"];
            if (locationList.options[locationList.options.selectedIndex].value == "-1" && (geoInfo.value == "" || geoInfo.value == "{\"type\":\"FeatureCollection\",\"features\":[]}")) {
                alert("${i18nBundle.noMapDataEntered}");
                return false;
            } else {
                return true;
            }
        }

        var cropCategories = [];

        function initCropCategories() {
            $.getJSON("/rest/organism/cropcategory/${user.organizationId.organizationId}", function (json) {
                    cropCategories = json;
                    renderCropCategories();
                }
            );
        }

        function renderCropCategories() {
            var cropCategoryIdList = document.getElementById("cropCategoryIdList");
            for (var i in cropCategories) {
                var cropCategory = cropCategories[i];
                // Best effort getting name for crop category
                var catName = cropCategory.defaultName;
                for (var j in cropCategory.cropCategoryLocalSet) {
                    var cLocal = cropCategory.cropCategoryLocalSet[j];
                    if (cLocal.cropCategoryLocalPK.locale == "${currentLocale}") {
                        catName = cLocal.localName;
                    }
                }
                var cOption = new Option(catName, cropCategory.cropCategoryId);
                cropCategoryIdList.options[cropCategoryIdList.options.length] = cOption;
            }
        }

        // Return name for given cropOrganismId
        function nameForCropOrganismId(cropOrganismId) {
            const crop = cropList.find(crop => crop.organismId == cropOrganismId);
            return crop ? crop.displayName : null;
        }

        // Return name for given organismId
        function nameForOrganismId(organismId) {
            const pest = organismList.find(pest => pest.organismId == organismId);
            return pest ? pest.displayName : null;
        }

        // Return name for given point of interest id
        function nameForLocationPointOfInterestId(pointOfInterestId, allPois) {
            const location = allPois.find(loc => loc.pointOfInterestId == pointOfInterestId);
            return location ? location.name : null;
        }

        /**
         * Based on the selected crop category: Put the related crops
         * on top of the crops list
         */
        function filterCrops(selectedCropCategoryId) {
            // Nothing has been selected, render all in one go
            if (selectedCropCategoryId < 0) {
                var cropOptions = [];
                for (var j = 0; j < cropList.length; j++) {
                    //var cropOption = cropOrganismIdList.options[j];
                    var crop = cropList[j];
                    var cropOption = new Option(crop.displayName, crop.organismId);
                    if (selectedCropId != null && selectedCropId == crop.organismId) {
                        cropOption.selected = true;
                    }
                    cropOptions.push(cropOption);

                }
                renderCropList(cropOptions, []);
            } else {
                // Searching for selected crop category
                for (var i in cropCategories) {
                    var cropCategory = cropCategories[i];
                    if (cropCategory.cropCategoryId == parseInt(selectedCropCategoryId)) {
                        // Filter based on match
                        var matchingCropOrganismOptions = [];
                        var theRest = [];
                        //var cropOrganismIdList = document.getElementById("cropOrganismIdList");
                        for (var j = 0; j < cropList.length; j++) {
                            //var cropOption = cropOrganismIdList.options[j];
                            var crop = cropList[j];
                            var cropOption = new Option(crop.displayName, crop.organismId);
                            if (selectedCropId != null && selectedCropId == crop.organismId) {
                                cropOption.selected = true;
                            }
                            if (cropCategory.cropOrganismIds.indexOf(crop.organismId) >= 0 && cropCategory.maxHierarchyCategoryId >= crop.hierarchyCategoryId) {
                                matchingCropOrganismOptions.push(cropOption);
                            } else if (crop.organismId > 0) {
                                theRest.push(cropOption);
                            }
                        }
                        renderCropList(matchingCropOrganismOptions, theRest);
                    }
                }
            }
        }

        function renderCropList(matchingCropOrganismOptions, theRest) {
            var cropOrganismIdList = document.getElementById("cropOrganismIdList");
            if (!cropOrganismIdList) {
                // HTML element does not exist for observations in time series
                return;
            }
            cropOrganismIdList.options.length = 1;
            for (var i in matchingCropOrganismOptions) {
                cropOrganismIdList.options[cropOrganismIdList.options.length] = matchingCropOrganismOptions[i];
            }
            cropOrganismIdList.options[cropOrganismIdList.options.length] = new Option("----", -1);
            for (var i in theRest) {
                cropOrganismIdList.options[cropOrganismIdList.options.length] = theRest[i];
            }
        }

        <#if noBroadcast>

        /**
         * In the unlikely event that the user in a pre-filled form decides to
         * change the organism, we'll update the observationText and heading
         */
        function updateHeadingAndText(organismName) {
            var registrationOfText = "${i18nBundle.registrationOf} " + organismName;
            document.getElementById("observationHeading").value = registrationOfText;
            document.getElementById("observationText").value = registrationOfText;
        }

        </#if>
        var cropList = [
            <#if ! observation.observationId?has_content || user.isSuperUser() || user.isOrganizationAdmin()>
            {organismId: -10, displayName: "${i18nBundle.missingInDatabase}", hierarchyCategoryId: -1},
            <#list allCrops as cropOrganism>
            {
                organismId: ${cropOrganism.organismId?c},
                displayName: "${cropOrganism.getLocalName(currentLocale.language)!""} (${cropOrganism.latinName!""}) ${hierarchyCategories.getName(cropOrganism.hierarchyCategoryId)?upper_case}",
                hierarchyCategoryId: ${cropOrganism.hierarchyCategoryId!"-1"}
            }<#sep>,
            </#list>
            <#else>
            <#list allCrops as cropOrganism>
            <#if (observation.cropOrganism?has_content && observation.cropOrganism.organismId == cropOrganism.organismId)>
            {
                organismId: ${cropOrganism.organismId},
                displayName: "${cropOrganism.getLocalName(currentLocale.language)!""} (${cropOrganism.latinName!""}) ${hierarchyCategories.getName(cropOrganism.hierarchyCategoryId)?upper_case}",
                hierarchyCategoryId: ${cropOrganism.hierarchyCategoryId}
            }
            </#if>
            </#list>
            </#if>
        ];

        var organismList = [
            <#list allPests as organism>
            {
                organismId: ${organism.organismId},
                displayName: "${organism.getLocalName(currentLocale.language)!""} (${organism.latinName!""})"
            },
            </#list>
        ];

        /**
         * Does all the ifs and buts before form can potentially be submitted
         */
        function prepareFormSubmit(theForm) {
            // Extract GIS info from OpenLayers
            theForm['geoInfo'].value = getFeatures();
            try {
                // If the form is quantified: Inspect the fields and write
                // JSON string to the generic form field "observationData";
                if (theForm.isQuantified.checked) {
                    const errors = editor.validate();
                    if (errors.length) {
                        alert(errors);
                        return false;
                    }
                    theForm['observationData'].value = JSON.stringify(editor.getValue());
                }
                validateGIS(theForm);
                //console.info('validateGIS = ' + (validateGIS(this)));
                //return false; // DEBUG setting
                return validateForm(theForm) && validateGIS(theForm); // PROD setting
            } catch (e) {
                console.log(e.message);
                console.log(e);
                return false;
            }
        }
    </script>
</#macro>
<#macro page_contents>
    <div class="singleBlockContainer">
        <p><a href="${returnPath}" class="btn btn-default back" role="button">${i18nBundle.back}</a>
            <#if observation.observationId?has_content><a
                    href="/observation?action=newObservationForm" class="btn btn-default"
                    role="button">${i18nBundle.addNew}</a></#if></p>
        <h1><#if observation.observationId?has_content>${i18nBundle.editObservation}<#else>${i18nBundle.newObservation}</#if><#if observation.observationTimeSeriesId?has_content> ${i18nBundle.forTimeSeries}</#if><#if shortcut?has_content> - ${shortcut.getLocalLabel(currentLocale.language)?lower_case}</#if></h1>
        <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
            <#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
        </div>
        <#if messageKey?has_content>
            <div class="alert alert-success">${i18nBundle(messageKey)}</div>
        </#if>
        <div class="row">
            <div class="col-md-6">
                <#assign formId = "observationForm">
                <form id="${formId}" role="form" action="/observation?action=observationFormSubmit<#if returnTo?has_content>&returnTo=${returnTo}</#if>"
                      enctype="multipart/form-data" method="POST" onsubmit="return prepareFormSubmit(this);">
                    <!--form id="${formId}" role="form" action="/observation?action=observationFormSubmit" method="POST" onsubmit="this['geoInfo'].value=getFeatures();mw.save();console.log(this['observationData']);this['observationData'].value=JSON.stringify(mw.toInspect);return validateForm(this);"-->
                    <!--form id="${formId}" role="form" action="/observation?action=observationFormSubmit" method="POST" onsubmit="this['geoInfo'].value=getFeatures();mw.save();console.log(this['observationData']);this['observationData'].value=JSON.stringify(mw.toInspect);validateForm(this);return false;"-->
                    <input type="hidden" name="geoInfo" value=""/>
                    <input type="hidden" name="observationData" value=""/>
                    <input type="hidden" name="observationId" value="${observation.observationId!"-1"}"/>
                    <!--button type="button" onclick="var theForm=document.getElementById('observationForm');theForm['geoInfo'].value=getFeatures();try{mw.save();theForm['observationData'].value=JSON.stringify(mw.toInspect);return validateForm(theForm) && validateGIS(theForm);}catch(e){console.log(e.message);console.log(e);return false;}">Test</button-->
                    <#if observation.user?has_content>
                        <div class="form-group">
                            <label>${i18nBundle.observer}
                                : ${observation.user.firstName} ${observation.user.lastName}</label>
                        </div>
                    </#if>
                    <#if observation.lastEditedByUser?has_content>
                        <div class="form-group">
                            <label>${i18nBundle.lastEditedBy}
                                : ${observation.lastEditedByUser.firstName} ${observation.lastEditedByUser.lastName}</label>
                        </div>
                    </#if>
                    <#if observation.observationTimeSeries?has_content>
                        <div class="alert alert-info">
                            <i>${i18nBundle.timeSeriesNonEditable}</i>
                        </div>
                        <div class="form-group">
                            <label for="timeSeriesName">${i18nBundle.timeSeries}:</label>
                            <span id="timeSeriesName"><a href="/observationTimeSeries?action=editObservationTimeSeriesForm&observationTimeSeriesId=${observation.observationTimeSeries.observationTimeSeriesId}">${observation.observationTimeSeries.name} (${observation.observationTimeSeries.year})</a></span>
                        </div>
                        <div class="form-group">
                            <label for="cropOrganismId">${i18nBundle.cropOrganismId}:</label>
                            <span id="cropDisplayName"></span>
                        </div>
                        <div class="form-group">
                            <label for="organismId">${i18nBundle.organism}:</label>
                            <span id="pestDisplayName"></span>
                        </div>
                        <div class="form-group">
                            <label for="locationPointOfInterestId">${i18nBundle.location}:</label>
                            <span id="locationDisplayName"></span>
                        </div>
                        <div class="form-group">
                            <label for="locationVisibility">Synlighet:</label>
                            <span id="locationVisibility">
                                <#if locationVisibilityFormValue == 'public'>
                                    ${i18nBundle.locationIsPublic}
                                <#elseif locationVisibilityFormValue == 'private'>
                                    ${i18nBundle.locationIsPrivate}
                                <#else>
                                    <#list polygonServices as polygonService>
                                        <#if locationVisibilityFormValue == "mask_" + polygonService.polygonServiceId>
                                            ${i18nBundle.maskObservationWith} ${polygonService.polygonServiceName}
                                        </#if>
                                    </#list>
                                </#if>
                            </span>
                        </div>
                        <input type="hidden" name="observationTimeSeriesId" value="${observation.observationTimeSeries.observationTimeSeriesId}">
                        <input type="hidden" name="cropOrganismId" value="${observation.observationTimeSeries.cropOrganism.organismId}">
                        <input type="hidden" name="organismId" value="${observation.observationTimeSeries.organism.organismId}">
                        <input type="hidden" name="locationPointOfInterestId" value="${observation.observationTimeSeries.locationPointOfInterestId}">
                        <input type="hidden" name="locationVisibility" value="${locationVisibilityFormValue}">
                    <#else>
                        <#if ! observation.organism?has_content>
                            <div class="form-group">
                                <label for="cropCategoryId">${i18nBundle.listSelectedCropCategoryOnTop}</label>
                                <select class="form-control" id="cropCategoryIdList" name="cropCategoryId"
                                        onchange="filterCrops(this.options[this.options.selectedIndex].value);">
                                    <option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.cropCategory?lower_case}</option>
                                    <!-- Options added by JavaScript function renderCropCategories() -->
                                </select>
                            </div>
                        </#if>
                        <div class="form-group">
                            <label for="cropOrganismId">${i18nBundle.cropOrganismId}</label>
                            <select class="form-control" id="cropOrganismIdList" name="cropOrganismId"
                                    <#if observation.observationId?has_content && ! user.isSuperUser() && ! user.isOrganizationAdmin()>readonly="readonly" <#else> onblur="validateField(this);" onchange="updateCropPests();"</#if>>
                                <#if ! observation.observationId?has_content || user.isSuperUser() || user.isOrganizationAdmin()>
                                    <option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.cropOrganismId?lower_case}</option>
                                    <option value="-10"
                                            <#if (observation.cropOrganism?has_content && observation.cropOrganism.organismId == -10)>selected="selected"</#if>>${i18nBundle.missingInDatabase}</option>
                                </#if>
                            </select>
                            <span class="help-block" id="${formId}_cropOrganismId_validation"></span>
                        </div>
                        <div class="form-group">
                            <label for="organismId">${i18nBundle.organism}</label>
                            <select class="form-control" name="organismId"
                                    <#if observation.organism?has_content && ! user.isSuperUser() && ! user.isOrganizationAdmin()>readonly="readonly"
                                    <#else>onchange="<#if noBroadcast>updateHeadingAndText(this.options[this.options.selectedIndex].text);</#if>initObservationData(this.options[this.options.selectedIndex].value,organizationId);"
                                    onblur="validateField(this);"</#if>>
                                <#if !observation.organism?has_content || user.isSuperUser() || user.isOrganizationAdmin()>
                                    <option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.organism?lower_case}</option>
                                    <option value="-10"
                                            <#if (observation.organism?has_content && observation.organism.organismId == -10)>selected="selected"</#if>>${i18nBundle.missingInDatabase}</option>
                                    <#list allPests as organism>
                                        <option value="${organism.organismId}"
                                                <#if (observation.organism?has_content && observation.organism.organismId == organism.organismId)>selected="selected"</#if>
                                        >${organism.getLocalName(currentLocale.language)!""} (${organism.latinName!""}
                                            ) ${hierarchyCategories.getName(organism.hierarchyCategoryId)?upper_case}</option>
                                    </#list>
                                <#else>
                                    <#list allPests as organism>
                                        <#if (observation.organism?has_content && observation.organism.organismId == organism.organismId)>
                                            <option value="${organism.organismId}"
                                                    selected="selected">${organism.getLocalName(currentLocale.language)!""}
                                                (${organism.latinName!""}
                                                ) ${hierarchyCategories.getName(organism.hierarchyCategoryId)?upper_case}</option>
                                        </#if>
                                    </#list>
                                </#if>
                            </select>
                            <span class="help-block" id="${formId}_organismId_validation"></span>

                        </div>
                        <div class="form-group">
                            <label for="locationPointOfInterestId">${i18nBundle.location}&nbsp;&nbsp;<button
                                        role="button" type="button"
                                        onclick="addNewLocationPopup();">${i18nBundle.addNew}</button>
                            </label>
                            <select class="form-control" name="locationPointOfInterestId" id="locationPointOfInterestId"
                                    onchange="showCorrectMap();" <#if editAccess!="W">readonly="readonly"</#if>>
                                <option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.location?lower_case}</option>
                            </select>
                            <span class="help-block" id="${formId}_locationPointOfInterestId_validation"></span>
                        </div>
                        <#if editAccess!="W" && observation.locationIsPrivate?has_content && observation.locationIsPrivate == true>
                            <input type="hidden" name="locationVisibility" value="private"/>
                        <#else>
                            <div class="form-group">
                                <div class="radio">
                                    <label>
                                        <input type="radio" name="locationVisibility" value="public"
                                               <#if locationVisibilityFormValue == "public">checked="checked"</#if>
                                        />
                                    </label>
                                    ${i18nBundle.locationIsPublic}
                                </div>
                                <div class="radio">

                                    <label>
                                        <input type="radio" name="locationVisibility" value="private"
                                               <#if locationVisibilityFormValue == "private">checked="checked"</#if>
                                        />
                                    </label>
                                    ${i18nBundle.locationIsPrivate}
                                </div>
                                <#list polygonServices as polygonService>
                                    <div class="radio">
                                        <label>
                                            <input type="radio" name="locationVisibility"
                                                   value="mask_${polygonService.polygonServiceId}"
                                                   <#if locationVisibilityFormValue == "mask_" + polygonService.polygonServiceId>checked="checked"</#if>
                                            />
                                            ${i18nBundle.maskObservationWith} ${polygonService.polygonServiceName}
                                        </label>
                                    </div>
                                </#list>
                            </div>
                        </#if>
                    </#if>
                    <div class="form-group">
                        <label for="organizationGroupId">${i18nBundle.availableFor} ${i18nBundle.organizationGroupList?lower_case}</label>
                        <select class="form-control chosen-select" name="organizationGroupId" multiple="multiple"
                                data-placeholder="${i18nBundle.all}" <#if editAccess!="W">readonly="readonly"</#if>>
                            <#list organizationGroups as group>
                                <option value="${group.organizationGroupId}" <#if selectedOrganizationGroupIds?seq_contains(group.organizationGroupId)> selected="selected"</#if>>${group.groupName}</option>
                            </#list>
                        </select>
                    </div>
                    <#setting time_zone=user.organizationId.defaultTimeZone!"UTC">
                    <div class="form-group">
                        <label for="timeOfObservation">${i18nBundle.timeOfObservation}</label>
                        <input type="date" <#if observation.observationTimeSeries?has_content>min="${minTimeOfObservation?string("yyyy-MM-dd")}" max="${maxTimeOfObservation?string("yyyy-MM-dd")}"</#if> class="form-control" id="timeOfObservation" name="timeOfObservation" placeholder="${i18nBundle.timeOfObservation}" value="${observation.timeOfObservation?string("yyyy-MM-dd")}" onblur="validateField(this);" <#if editAccess!="W">readonly="readonly"</#if>/>
                        <span class="help-block" id="${formId}_timeOfObservation_validation"></span>
                    </div>
                    <div class="form-group">
                        <div class="checkbox">
                            <#if editAccess!="W" && observation.isPositive?has_content && observation.isPositive ==true>
                                <input type="hidden" name="isPositive" value="true"/></#if>
                            <label>
                                <input type="checkbox"
                                       name="isPositive"<#if observation.isPositive?has_content && observation.isPositive == false><#else> checked="checked"</#if> <#if editAccess!="W">disabled="disabled"</#if>/>
                            </label>
                            ${i18nBundle.isPositiveRegistration}
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="checkbox">
                            <#if editAccess!="W" && observation.isQuantified?has_content && observation.isQuantified ==true>
                                <input type="hidden" name="isQuantified" value="true"/></#if>
                            <label>
                                <input type="checkbox"
                                       name="isQuantified"<#if observation.isQuantified?has_content && observation.isQuantified == false><#else> checked="checked"</#if> <#if editAccess!="W">disabled="disabled"</#if>/>
                            </label>
                            ${i18nBundle.isQuantified}
                        </div>
                    </div>
                    <div class="form-group">
                        <fieldset>
                            <legend>${i18nBundle.observationData}</legend>
                            <div id="quantificationFields"></div>
                        </fieldset>
                    </div>
                    <#if !noBroadcast>
                        <div class="form-group">
                            <div class="checkbox">
                                <#if editAccess!="W" && observation.broadcastMessage?has_content && observation.broadcastMessage ==true>
                                    <input type="hidden" name="isQuantified" value="true"/></#if>
                                <label>
                                    <input type="checkbox" name="broadcastMessage"
                                           <#if (observation.broadcastMessage?has_content && observation.broadcastMessage == false) || noBroadcast><#else>checked="checked"</#if> <#if editAccess!="W">disabled="disabled"</#if>/>
                                </label>
                                ${i18nBundle.broadcastMessage}
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="observationHeading">${i18nBundle.observationHeading}</label>
                            <input type="text" class="form-control" name="observationHeading" placeholder=""
                                   value="${observation.observationHeading!""}" onblur="validateField(this);"
                                   <#if editAccess!="W">readonly="readonly"</#if>/>
                            <span class="help-block" id="${formId}_observationHeading_validation"></span>
                        </div>
                        <div class="form-group">
                            <label for="observationText">${i18nBundle.observationText}</label>
                            <textarea class="form-control" name="observationText" placeholder=""
                                      <#if editAccess!="W">readonly="readonly"</#if>>${observation.observationText!""}</textarea>
                            <span class="help-block" id="${formId}_observationText_validation"></span>
                        </div>
                        <#if observation.observationIllustrationSet?has_content && observation.observationIllustrationSet?size gt 0>
                            <#list observation.observationIllustrationSet as illustration>
                                <img src="/static/images/observations/${observation.organismId}/${illustration.observationIllustrationPK.fileName}"
                                     alt="TODO: Add describing text" class="img-responsive"/>
                                <div class="checkbox">
                                    <label>
                                        <input type="checkbox" name="deleteIllustration"
                                               value="${illustration.observationIllustrationPK.fileName}"/>
                                        ${i18nBundle.deleteIllustration}
                                    </label>
                                </div>
                            </#list>
                        </#if>
                        <div class="form-group">
                            <div class="input-group">
                                <label for="illustration"><#if observation.observationIllustrationSet?has_content && observation.observationIllustrationSet?size gt 0>${i18nBundle.addIllustration}<#else>${i18nBundle.newIllustration}</#if></label><br/>
                                <span class="btn btn-default btn-file">${i18nBundle.browse}<input type="file"
                                                                                                  name="illustration"></span>
                                <input type="text" class="form-control" readonly>
                            </div>
                        </div>
                    <#else>
                        <!-- broadcastMessage is checkbox, omitting it sets it default to false -->
                        <input type="hidden" id="observationHeading" name="observationHeading"
                               value="<#if observation.organism?has_content>${i18nBundle.registrationOf} ${observation.organism.getLocalName(currentLocale.language)!""} (${observation.organism.latinName!""})</#if>"/>
                        <input type="hidden" id="observationText" name="observationText"
                               value="<#if observation.organism?has_content>${i18nBundle.registrationOf} ${observation.organism.getLocalName(currentLocale.language)!""} (${observation.organism.latinName!""})</#if>"/>
                    </#if> <!-- visibility -->
                    <#if user.isObservationAuthority() || user.isOrganizationAdmin() || user.isSuperUser()>
                        <div class="form-group">
                            <label for="statusTypeId">${i18nBundle.statusTypeId}</label>
                            <select class="form-control" name="statusTypeId" onblur="validateField(this);">
                                <#list statusTypeIds as statusTypeId>
                                    <option value="${statusTypeId.statusTypeId}"
                                            <#if observation.statusTypeId?has_content && observation.statusTypeId == statusTypeId.statusTypeId>selected="selected"</#if>
                                            <#if !observation.statusTypeId?has_content && 3 == statusTypeId.statusTypeId>selected="selected"</#if>
                                    >${i18nBundle("statusTypeIdTitle_" + statusTypeId.statusTypeId)}</option>
                                </#list>
                            </select>
                            <span class="help-block" id="${formId}_statusTypeId_validation"></span>
                        </div>
                        <div class="form-group">
                            <label for="statusRemarks">${i18nBundle.statusRemarks}</label>
                            <textarea class="form-control" name="statusRemarks"
                                      placeholder="">${observation.statusRemarks!""}</textarea>
                            <span class="help-block" id="${formId}_statusRemarks_validation"></span>
                        </div>
                    </#if>
                    <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
                    <#if observation.observationId?has_content && editAccess == "W">
                        <button type="button" class="btn btn-danger"
                                onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/observation?action=deleteObservation&observationId=${observation.observationId}<#if returnTo?has_content>&returnTo=${returnTo}</#if>';}">${i18nBundle.delete}</button>
                    </#if>
                </form>
            </div>
            <div class="col-md-6">
                <div id="observationFormMap"></div> <!-- Contents is added dynamically by objectGISInfoMap -->
                <div id="poiFormMap" class="map" style="height: 400px;">
                    <div id="popover"></div>
                </div>
            </div>
        </div>
        <div style="display: none;">
            <div id="poiMarker" title="Marker"><img src="/public/images/Map_pin_icon_green_small.png"/></div>
        </div>
    </div>
</#macro>
<@page_html/>
