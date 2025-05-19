<#include "master.ftl">
<#macro page_head>
    <title><#if observationTimeSeries.observationTimeSeriesId?has_content>${i18nBundle.editObservation}<#else>${i18nBundle.newObservation}</#if></title>
</#macro>
<#macro custom_css>
    <link rel="stylesheet" type="text/css" href="/css/3rdparty/jquery.datetimepicker.css"/>
    <link rel="stylesheet" type="text/css" href="/css/3rdparty/ol.css?t=20170623"/>
    <link rel="stylesheet" type="text/css" href="/css/3rdparty/ol-layerswitcher.css"/>
    <link rel="stylesheet" type="text/css" href="/css/map.css"/>
</#macro>
<#macro custom_js>
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
<script>
    let organizationId = ${user.organizationId.organizationId};
    let selectedCropId = <#if observationTimeSeries.cropOrganism?has_content>${observationTimeSeries.cropOrganism.organismId?c}<#else>null</#if>;

    $(document).ready(function () {
        loadFormDefinition("observationTimeSeriesForm");
        refreshLocationPointOfInterests(<#if observationTimeSeries.locationPointOfInterestId?has_content>${observationTimeSeries.locationPointOfInterestId}<#else>null</#if>);
        <#if !observationTimeSeries.organism?has_content>
            initCropCategories();
        </#if>
        <#if isEditable?has_content && !isEditable>
            displayObservationTimeSeriesInfo("${observationTimeSeries.cropOrganismId}", "${observationTimeSeries.organismId}", "${observationTimeSeries.locationPointOfInterestId}")
        </#if>
        filterCrops(-1);
    });

    let thePopup;
    function addNewLocationPopup() {
        thePopup = window.open("/poi?action=newPoiForm&organizationId=1&returnIdCallback=refreshLocationPointOfInterests");
    }

    let allPois = [];
    function fetchPOIs(callback) {
        $.getJSON("/rest/poi/user", function (json) {
            callback(json);
        });
    }

    function refreshLocationPointOfInterests(selectedPointOfInterestId) {
        const poiTypes = {
            0: "${i18nBundle.genericPlaces}",
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

    function buildPOIList(poiListElement, allPois, poiTypes, selectedPointOfInterestId) {
        poiListElement.options.length = 1;
        for (const [typeId, typeName] of Object.entries(poiTypes)) {
            let poiTypeOption = new Option("-- " + typeName + " --", "-1");
            poiTypeOption.disabled = true;
            poiListElement.options[poiListElement.options.length] = poiTypeOption;

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

    function showCorrectMap() {
        let locationList = document.getElementById("locationPointOfInterestId");
        if (locationList.options[locationList.options.selectedIndex].value === "-1") {
            document.getElementById("poiFormMap").style.display = "block";
            initLocationMap(null);
        } else {
            document.getElementById("poiFormMap").style.display = "block";
            initLocationMap(locationList.options[locationList.options.selectedIndex].value);
        }
    }

    /**
     * Crop, pest and location should not be editable for time series with observations. We avoid displaying
     * form elements in these cases, and rather display the values statically.
     */
    function displayObservationTimeSeriesInfo(cropOrganismId, organismId, locationPointOfInterestId) {
        document.getElementById("cropDisplayName").innerHTML = nameForCropOrganismId(cropOrganismId);
        document.getElementById("pestDisplayName").innerHTML = nameForOrganismId(organismId);
        fetchPOIs(function (allPois) {
            const locationName = nameForLocationPointOfInterestId(locationPointOfInterestId, allPois);
            document.getElementById("locationDisplayName").innerHTML = locationName || null;
        });
        initLocationMap(locationPointOfInterestId)
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

    let cropCategories = [];

    function initCropCategories() {
        $.getJSON("/rest/organism/cropcategory/${user.organizationId.organizationId}", function (json) {
                cropCategories = json;
                renderCropCategories();
            }
        );
    }

    function renderCropCategories() {
        let cropCategoryIdList = document.getElementById("cropCategoryIdList");
        for (let i in cropCategories) {
            let cropCategory = cropCategories[i];
            // Best effort getting name for crop category
            let catName = cropCategory.defaultName;
            for (let j in cropCategory.cropCategoryLocalSet) {
                let cLocal = cropCategory.cropCategoryLocalSet[j];
                if (cLocal.cropCategoryLocalPK.locale == "${currentLocale}") {
                    catName = cLocal.localName;
                }
            }
            cropCategoryIdList.options[cropCategoryIdList.options.length] = new Option(catName, cropCategory.cropCategoryId);
        }
    }

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
        let cropOrganismIdList = document.getElementById("cropOrganismIdList");
        if (!cropOrganismIdList) {
            // HTML element does not exist for observations in time series
            return;
        }
        cropOrganismIdList.options.length = 1;
        for (let i in matchingCropOrganismOptions) {
            cropOrganismIdList.options[cropOrganismIdList.options.length] = matchingCropOrganismOptions[i];
        }
    }

    let cropList = [
        <#if ! observationTimeSeries.observationTimeSeriesId?has_content || user.isSuperUser() || user.isOrganizationAdmin()>
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
        <#if (observationTimeSeries.cropOrganism?has_content && observationTimeSeries.cropOrganism.organismId == cropOrganism.organismId)>
        {
            organismId: ${cropOrganism.organismId},
            displayName: "${cropOrganism.getLocalName(currentLocale.language)!""} (${cropOrganism.latinName!""}) ${hierarchyCategories.getName(cropOrganism.hierarchyCategoryId)?upper_case}",
            hierarchyCategoryId: ${cropOrganism.hierarchyCategoryId}
        }
        </#if>
        </#list>
        </#if>
    ];

    let organismList = [
        <#list allPests as organism>
        {
            organismId: ${organism.organismId},
            displayName: "${organism.getLocalName(currentLocale.language)!""} (${organism.latinName!""})"
        },
        </#list>
    ];
    let updateCropPests = function () {
        let theForm = document.getElementById('observationTimeSeriesForm');
        theForm["organismId"].options.selectedIndex = -1;

        let selectedCropId = theForm["cropOrganismId"].options[theForm["cropOrganismId"].options.selectedIndex].value;
        // If this is not a new observation, or the selected crop is not in the database, we keep calm
        if (theForm["observationTimeSeriesId"].value !== "-1" || selectedCropId === "-10") {
            return;
        }
        $.getJSON("/rest/organism/croppest/" + selectedCropId, function (json) {
            updateCropPestsCallback(json);
        })
            .fail(function () {
                alert("Error getting pests for this crop. Please contact system administrator");
            });
    };

    let updateCropPestsCallback = function (cropPest) {
        let pestList = document.getElementById('observationTimeSeriesForm')["organismId"];
        if (cropPest == null) {
            // Need to reorganize pests back to default
            let allPests = [];
            for (let i = 2; i < pestList.options.length; i++) {
                allPests.push(pestList.options[i]);
            }
            allPests.sort(compareSelectListOptions);
            pestList.options.length = 2; // Keeping the top two
            for (let i = 0; i < allPests.length; i++) {
                pestList.options[pestList.options.length] = allPests[i];
            }
        } else {
            let prioritizedPests = [];
            for (let i = 2; i < pestList.options.length; i++) {
                if (cropPest.pestOrganismIds.indexOf(parseInt(pestList.options[i].value)) >= 0) {
                    prioritizedPests.push(pestList.options[i]);
                }
            }
            pestList.options.length = 2; // Keeping the top two
            for (let i = 0; i < prioritizedPests.length; i++) {
                pestList.options[pestList.options.length] = prioritizedPests[i];
            }
        }
    };
</script>

</#macro>

<#macro page_contents>
<div class="singleBlockContainer">
    <p><a href="/observationTimeSeries" class="btn btn-default back" role="button">${i18nBundle.back}</a><#if observationTimeSeries.observationTimeSeriesId?has_content><a href="/observation?action=newObservationForm&observationTimeSeriesId=${observationTimeSeries.observationTimeSeriesId}&returnTo=timeseries" class="btn btn-default" role="button">${i18nBundle.addNewObservationInTimeSeries}</a></#if></p>
    <h1><#if observationTimeSeries.observationTimeSeriesId?has_content>${i18nBundle.editObservationTimeSeries}<#else>${i18nBundle.newObservationTimeSeries}</#if><#if shortcut?has_content> - ${shortcut.getLocalLabel(currentLocale.language)?lower_case}</#if></h1>
    <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
        <#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
    </div>
    <#if messageKey?has_content>
        <div class="alert alert-success">${i18nBundle(messageKey)}</div>
    </#if>
    <div class="row">
        <div class="col-md-6">
            <#assign formId = "observationTimeSeriesForm">
            <form id="${formId}" role="form" action="/observationTimeSeries?action=observationTimeSeriesFormSubmit" enctype="multipart/form-data" method="POST">
                <input type="hidden" name="geoInfo" value=""/>
                <input type="hidden" name="observationTimeSeriesId" value="${observationTimeSeries.observationTimeSeriesId!"-1"}"/>
                <#if observationTimeSeries.user?has_content>
                    <div class="form-group">
                        <label>Registrert av</label>
                        <span>${observationTimeSeries.user.firstName} ${observationTimeSeries.user.lastName}</span>
                    </div>
                </#if>
                <#if observationTimeSeries.lastModifiedByUser?has_content>
                    <div class="form-group">
                        <label>${i18nBundle.lastEditedBy}</label>
                        <span>${observationTimeSeries.lastModifiedByUser.firstName} ${observationTimeSeries.lastModifiedByUser.lastName}</span>
                    </div>
                </#if>
                <#if isEditable?has_content && !isEditable>
                    <div class="alert alert-info">
                        <i>Kultur, organisme, år og sted kan ikke redigeres for tidsserier som inneholder observasjoner.</i>
                    </div>
                    <div class="form-group">
                        <label for="cropDisplayName">${i18nBundle.cropOrganismId}</label>
                        <span id="cropDisplayName"></span>
                    </div>
                    <div class="form-group">
                        <label for="pestDisplayName">${i18nBundle.organism}</label>
                        <span id="pestDisplayName"></span>
                    </div>
                    <div class="form-group">
                        <label>År</label> ${observationTimeSeries.year}
                    </div>
                    <div class="form-group">
                        <label for="locationDisplayName">${i18nBundle.location}</label>
                        <span id="locationDisplayName"></span>
                    </div>
                    <div class="form-group">
                        <label for="locationVisibility">Synlighet</label>
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
                    <input type="hidden" name="observationTimeSeriesId" value="${observationTimeSeries.observationTimeSeriesId}">
                    <input type="hidden" name="year" value="${observationTimeSeries.year}">
                    <input type="hidden" name="cropOrganismId" value="${observationTimeSeries.cropOrganism.organismId}">
                    <input type="hidden" name="organismId" value="${observationTimeSeries.organism.organismId}">
                    <input type="hidden" name="locationPointOfInterestId" value="${observationTimeSeries.locationPointOfInterestId}">
                    <input type="hidden" name="locationVisibility" value="${locationVisibilityFormValue}">
                <#else>
                    <#if ! observationTimeSeries.organism?has_content>
                        <div class="form-group">
                            <label for="cropCategoryIdList">${i18nBundle.listSelectedCropCategoryOnTop}</label>
                            <select class="form-control" id="cropCategoryIdList" name="cropCategoryId"
                                    onchange="filterCrops(this.options[this.options.selectedIndex].value);">
                                <option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.cropCategory?lower_case}</option>
                                <!-- Options added by JavaScript function renderCropCategories() -->
                            </select>
                        </div>
                    </#if>
                    <div class="form-group">
                        <label for="cropOrganismIdList">${i18nBundle.cropOrganismId}</label>
                        <select class="form-control" id="cropOrganismIdList" name="cropOrganismId" onblur="validateField(this);" onchange="updateCropPests();">
                            <#if !observationTimeSeries.observationTimeSeriesId?has_content>
                                <option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.cropOrganismId?lower_case}</option>
                                <option value="-10" <#if (observationTimeSeries.cropOrganism?has_content && observationTimeSeries.cropOrganism.organismId == -10)>selected="selected"</#if>>${i18nBundle.missingInDatabase}</option>
                            </#if>
                        </select>
                        <span class="help-block" id="${formId}_cropOrganismId_validation"></span>
                    </div>
                    <div class="form-group">
                        <label for="organismId">${i18nBundle.organism}</label>
                        <select class="form-control" id="organismId" name="organismId" onblur="validateField(this);">
                            <#if !observationTimeSeries.organism?has_content>
                                <option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.organism?lower_case}</option>
                                <option value="-10" <#if (observationTimeSeries.organism?has_content && observationTimeSeries.organism.organismId == -10)>selected="selected"</#if>>${i18nBundle.missingInDatabase}</option>
                                <#list allPests as organism>
                                    <option value="${organism.organismId}">${organism.getLocalName(currentLocale.language)!""} (${organism.latinName!""}) ${hierarchyCategories.getName(organism.hierarchyCategoryId)?upper_case}</option>
                                </#list>
                            <#else>
                                <#list allPests as organism>
                                    <option value="${organism.organismId}" <#if (observationTimeSeries.organism?has_content && observationTimeSeries.organism.organismId == organism.organismId)>selected="selected"</#if>>${organism.getLocalName(currentLocale.language)!""}
                                        (${organism.latinName!""}
                                        ) ${hierarchyCategories.getName(organism.hierarchyCategoryId)?upper_case}</option>
                                </#list>
                            </#if>
                        </select>
                        <span class="help-block" id="${formId}_organismId_validation"></span>
                    </div>

                    <div class="form-group">
                        <label for="yearList">${i18nBundle.year}</label>
                        <select class="form-control" id="yearList" name="year">
                            <#if !observationTimeSeries.observationTimeSeriesId?has_content>
                                <option value="" disabled selected>${i18nBundle.pleaseSelect} ${i18nBundle.year?lower_case}</option>
                            </#if>
                            <#list years as year>
                                <option value="${year}" <#if observationTimeSeries.observationTimeSeriesId?has_content && observationTimeSeries.year == year>selected</#if>>${year}</option>
                            </#list>
                        </select>
                    </div>
                    <div class="form-group">
                        <label for="locationPointOfInterestId">${i18nBundle.location}&nbsp;&nbsp;<button
                                    role="button" type="button"
                                    onclick="addNewLocationPopup();">${i18nBundle.addNew}</button>
                        </label>
                        <select class="form-control" name="locationPointOfInterestId" id="locationPointOfInterestId" onchange="showCorrectMap();">
                            <option value="" disabled selected>${i18nBundle.pleaseSelect} ${i18nBundle.location?lower_case}</option>
                        </select>
                        <span class="help-block" id="${formId}_locationPointOfInterestId_validation"></span>
                    </div>
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
                <div class="form-group">
                    <label for="name">${i18nBundle.observationTimeSeriesName}</label>
                    <input type="text" class="form-control" id="name" name="name" placeholder=""
                           value="${observationTimeSeries.name!""}" onblur="validateField(this);" />
                    <span class="help-block" id="${formId}_name_validation"></span>
                </div>
                <div class="form-group">
                    <label for="description">${i18nBundle.observationTimeSeriesDescription}</label>
                    <textarea class="form-control" id="description" name="description" placeholder="">${observationTimeSeries.description!""}</textarea>
                    <span class="help-block" id="${formId}_description_validation"></span>
                </div>
                <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
                <#if observationTimeSeries.observationTimeSeriesId?has_content && isEditable>
                    <button type="button" class="btn btn-danger"
                            onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/observationTimeSeries?action=deleteObservationTimeSeries&observationTimeSeriesId=${observationTimeSeries.observationTimeSeriesId}';}">${i18nBundle.delete}</button>
                </#if>

            </form>
        </div>
        <div class="col-md-6">
            <div id="poiFormMap" class="map" style="height: 400px;">
                <div id="popover"></div>
            </div>
        </div>
    </div>
    <#if observations?has_content>
        <h2>Registrerte observasjoner</h2>
        <div class="row mt-3">
            <div class="table-responsive">
                <table class="table table-striped">
                    <thead>
                    <th>${i18nBundle.timeOfObservation}</th>
                    <th>${i18nBundle.cropOrganismId}</th>
                    <th>${i18nBundle.organism}</th>
                    <th>${i18nBundle.location}</th>
                    <th>${i18nBundle.observer}</th>
                    <th>${i18nBundle.heading}</th>
                    <th>${i18nBundle.status}</th>
                    <th></th>
                    </thead>
                    <tbody>
                    <#list observations as observation>
                        <tr>
                            <td>${observation.timeOfObservation?string("yyyy-MM-dd")}</td>
                            <td>${observation.cropOrganism.latinName!""}/${observation.cropOrganism.getLocalName(currentLocale.language)!""}</td>
                            <td>${observation.organism.latinName!""}/${observation.organism.getLocalName(currentLocale.language)!""}</td>
                            <td><#if observation.location?has_content>${observation.location.name!""}</#if></td>
                            <td><#if observation.user?has_content>${observation.user.firstName!""} ${observation.user.lastName!""}</#if></td>
                            <td>${observation.observationHeading}</td>
                            <td><#switch observation.statusTypeId><#case 1>${i18nBundle.pending}<#break><#case 2>${i18nBundle.rejected}<#break><#case 3>${i18nBundle.approved}</#switch></td>
                            <td><#if observation.userId == user.userId><a href="/observation?action=editObservationForm&observationId=${observation.observationId}&returnTo=timeseries" class="btn btn-default" role="button">${i18nBundle.edit}</a></#if></td>
                        </tr>
                    </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </#if>
</div>
</#macro>
<@page_html/>
