<#--
    Copyright (c) 2016 NIBIO <http://www.nibio.no/>.

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
<#assign isGridForecastSupported = (user.organizationId.defaultGridWeatherStationDataSource?has_content)>
<#macro page_head>
    <title>${i18nBundle.viewForecastConfiguration}</title>
</#macro>
<#macro custom_js>
    <script src="/js/resourcebundle.js"></script>
    <script src="/js/forecastConfigurationForm.js"></script>
    <script src="/js/validateForm.js"></script>
    <script src="//code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
    <link href="//code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
    <link rel="stylesheet" href="/css/3rdparty/leaflet.css" />
    <link rel="stylesheet" href="/css/mapModal.css" />
    <script type="text/javascript" src="/js/3rdparty/modernizr_custom.js"></script>
    <script type="text/javascript" src="/js/3rdparty/moment.min.js"></script>
    <script type="text/javascript" src="/js/environment.js"></script>
    <script type="text/javascript" src="/js/util.js"></script>
    <script type="text/javascript" src="/js/3rdparty/chosen.jquery.min.js"></script>
    <script type="text/javascript">
        $(".chosen-select").chosen();
    </script>
    <script type="module">
        import MapModal from '/js/mapModal.js';
        const selectLocationElement = document.querySelector('select[name="locationPointOfInterestId"]');
        const selectWeatherstationElement = document.querySelector('select[name="weatherStationPointOfInterestId"]') != null ? document.querySelector('select[name="weatherStationPointOfInterestId"]') : document.querySelector('select[name="weatherStationPointOfInterestIdDisabled"]');
        //console.info(selectWeatherstationElement);
        
        window.selectedPoiId = <#if forecastConfiguration.locationPointOfInterestId??>${forecastConfiguration.locationPointOfInterestId.pointOfInterestId!"undefined"}<#else>undefined</#if>;
        window.selectedWeatherstationId = <#if forecastConfiguration.weatherStationPointOfInterestId??>${forecastConfiguration.weatherStationPointOfInterestId.pointOfInterestId!"undefined"}<#else>undefined</#if>;

        // Read the list of locations into javascript array, to be able to dynamically manipulate it
        window.locationList = [
            <#list locationPointOfInterests as poi>
            {
                "pointOfInterestId": "${poi.pointOfInterestId}",
                "name": "${poi.name?json_string}",
                "pointOfInterestTypeId": ${poi.pointOfInterestTypeId},
                "location": <#if poi.longitude?? && poi.latitude??>[${poi.longitude?c}, ${poi.latitude?c}]<#else>null</#if>
            },
            </#list>
        ];
        

        const createOption = (value, text, isSelected = false) => {
                const option = document.createElement('option');
                option.value = value;
                option.textContent = text;
                if (isSelected) option.selected = true;
                return option;
            };

        renderPoiSelect(selectLocationElement, locationList, selectedPoiId);
        
        // Setting weather station select list state correct on page load
        <#if isGridForecastSupported && forecastConfiguration.forecastConfigurationId??>
        setUseGridWeatherData(<#if forecastConfiguration.useGridWeatherData>"true"<#else>"false"</#if>)
        handleWeatherDatasourceSelected(<#if forecastConfiguration.weatherStationPointOfInterestId?has_content && forecastConfiguration.weatherStationPointOfInterestId.pointOfInterestId?has_content>${forecastConfiguration.weatherStationPointOfInterestId.pointOfInterestId}</#if>);
        </#if>

        /**
        * @param selectElement the select list to render to
        * @param elementList the list of weather stations to list
        * @param selectedId the id (optional) to be preselected
        * @param coordinateToMatch [lon, lat] coordinate to match. Optional. If set:  The closest weather stations are show at top of list, with distance to coordinate.
        */
        window.renderWeatherstationSelect = function(selectElement, elementList, selectedId, coordinateToMatch)
        {
            selectElement.options.length = 0;

            // Add default option
            selectElement.appendChild(createOption(
                "-1",
                "${i18nBundle.pleaseSelect} ${i18nBundle.weatherStationPointOfInterestId?lower_case}",
                !selectedId
            ));

            // Render all weather stations, order by distance to coordinate
            if(coordinateToMatch != null)
            {
                let stations = [];
                elementList.forEach(poi => {
                    if(poi.pointOfInterestTypeId == POI_TYPE_WEATHERSTATION)
                    {
                        poi.distance = calculateDistanceBetweenCoordinates(poi.location[1], poi.location[0], coordinateToMatch[1], coordinateToMatch[0]).toFixed(1);
                        stations.push(poi);
                    }
                });
                stations.sort(function(a,b){
                    return a.distance - b.distance;
                });
                stations.forEach(poi => {
                        selectElement.appendChild(createOption(
                        poi.pointOfInterestId,
                        poi.name + (poi.distance > 0.0 ? " (" + poi.distance +" km)" : ""),
                        selectedId && poi.pointOfInterestId == selectedId
                    ));
                });
                
            }
            else 
            {
                // Render all weather stations alphabetically
                elementList.forEach(poi => {
                    if(poi.pointOfInterestTypeId == POI_TYPE_WEATHERSTATION)
                    {
                        selectElement.appendChild(createOption(
                        poi.pointOfInterestId,
                        poi.name,
                        selectedId && poi.pointOfInterestId == selectedId
                    ));
                    }
                });
            }
        }

        // Populate select list for point of interest -> NOT GENERAL! Default value is specific to location.
        function renderPoiSelect(selectElement, elementList, selectedId) {
            selectElement.innerHTML = '';

            // Add default option
            selectElement.appendChild(createOption(
                "-1",
                "${i18nBundle.pleaseSelect} ${i18nBundle.locationPointOfInterestId?lower_case}",
                !selectedId
            ));

            // Add options from elementList
            elementList.forEach(poi => {
                selectElement.appendChild(createOption(
                    poi.pointOfInterestId,
                    poi.name,
                    selectedId && poi.pointOfInterestId == selectedId
                ));
            });
        }

        renderWeatherstationSelect(selectWeatherstationElement, locationList, selectedWeatherstationId,
        <#if forecastConfiguration.weatherStationPointOfInterestId??>[${forecastConfiguration.weatherStationPointOfInterestId.longitude?c},${forecastConfiguration.weatherStationPointOfInterestId.latitude?c}]<#else>null</#if>
        );

        window.selectPoi = function(selectElement, selectedId) {
            if (selectedId) {
                const optionIndex = Array.from(selectElement.options).findIndex(option => option.value == selectedId);
                if (optionIndex !== -1) {
                    selectElement.selectedIndex = optionIndex;
                } else {
                    console.error("No matching option found for poi.id=" + selectedId);
                }
            }
        }

        // Get id of currently selected poi from select element
        function getSelectedPoiId(selectElement) {
            const value = selectElement.value;
            const parsedValue = parseInt(value, 10);
            return (!isNaN(parsedValue) && parsedValue > 0) ? parsedValue : undefined;
        }

        function callbackOnCloseLocationMap(poiData) {
            if(!poiData.pointOfInterestId) {
                persistNewLocation(poiData, handleLocationChanged);
            } else {
                selectPoi(selectLocationElement, poiData.pointOfInterestId);
                handleLocationChanged();
            }
            
        }

        function persistNewLocation(poiData, callback) {
            fetch("/rest/poi", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'Authorization': '${user.userUuid!'NOT LOGGED IN?'}'
                },
                body: JSON.stringify(poiData)
            })
                .then(response => response.json())
                .then(poi => {
                    locationList.push(poi);
                    renderPoiSelect(selectLocationElement, locationList, poi.pointOfInterestId);
                    // Assuming that this is not a weather station, since weather stations cannot be created using the
                    // map, the user must have created a .... MAYBE HANDLED AUTOMATICALLY?? by the change event??
                    //console.info("Point of interest successfully persisted", poi);
                    callback();
                })
                .catch(error => {
                    console.error("Unable to persist new point of interest", error);
                });
        }

        // Make function globally available
        window.openLocationMap = () => {
            let poiIds = locationList.map(poi => poi.pointOfInterestId);
            fetch("/rest/poi/geojson", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(poiIds)
            })
            .then(response => response.json())
            .then(geoJson => {
                const isPoiMap = true; // Map should operate on pois (not only coordinates)
                const allowNewPoints = true; // User should be able to create new pois
                const locationMapInstance = new MapModal('location-map', geoJson, '${currentLanguage}', isPoiMap, allowNewPoints, callbackOnCloseLocationMap);
                const selectedPoiId = getSelectedPoiId(selectLocationElement);
                locationMapInstance.openModal(selectedPoiId, ${user.organizationId.defaultMapCenter.y?c}, ${user.organizationId.defaultMapCenter.x?c}, ${user.organizationId.defaultMapZoom} + 1);
            })
            .catch(error => {
                console.error('Unable to open map', error);
            });
        }

    </script>
	<script type="text/javascript">
        const POI_TYPE_WEATHERSTATION = 1;
            $(document).ready(function() {
		// Make sure that there is a date picker present for HTML5 
		// date input fields
		if (!Modernizr.inputtypes.date) {
		    $('input[type=date]').datepicker({ dateFormat: 'yy-mm-dd' });
		}
		
		// Load main form definition (for validation)
		loadFormDefinition("${formId}");
		
		// Show specific fields for model
		if(document.getElementById('modelId').options.selectedIndex > 0)
		{
			renderModelSpecificFields("${formId}");
                        
		}
                
                if(document.getElementById("cropCategoryIdList") != null)
                {
                    initCropCategories();
                }
                    
                sortListAlphabetically(document.getElementById("modelId"),1);
            });
            
            var cropCategories = [];
            function initCropCategories(){
                $.getJSON("/rest/organism/cropcategory/${user.organizationId.organizationId}" , function( json ) {
                            cropCategories = json;
                            renderCropCategories();
                        }
                );
            }

            function renderCropCategories() {
                var cropCategoryIdList = document.getElementById("cropCategoryIdList");                    
                for(var i in cropCategories)
                {
                    var cropCategory = cropCategories[i];
                    // Best effort getting name for crop category
                    var catName = cropCategory.defaultName;
                    for(var j in cropCategory.cropCategoryLocalSet)
                    {
                        var cLocal = cropCategory.cropCategoryLocalSet[j];
                        if(cLocal.cropCategoryLocalPK.locale == "${currentLocale}")
                        {
                            catName = cLocal.localName;
                        }
                    }
                    var cOption = new Option(catName,cropCategory.cropCategoryId);
                    cropCategoryIdList.options[cropCategoryIdList.options.length] = cOption;
                }
            }

            function filterCrops(selectedCropCategoryId)
            {
                // Searching for selected crop category
                for(var i in cropCategories)
                {
                    var cropCategory = cropCategories[i];
                    if(cropCategory.cropCategoryId == parseInt(selectedCropCategoryId))
                    {
                        // Filter based on match
                        var matchingCropOrganismOptions = [];
                        var theRest = [];
                        var cropOrganismIdList = document.getElementById("cropOrganismId");
                        for(var j=0;j<cropOrganismIdList.length;j++)
                        {
                            var cropOption = cropOrganismIdList.options[j];
                            if(cropCategory.cropOrganismIds.indexOf(parseInt(cropOption.value)) >= 0)
                            {
                                matchingCropOrganismOptions.push(cropOption);
                            }
                            else if(parseInt(cropOption.value) > 0)
                            {
                                theRest.push(cropOption);
                            }
                        }
                        renderCropList(matchingCropOrganismOptions, theRest);
                    }
                }
            }
                
            function renderCropList(matchingCropOrganismOptions, theRest)
            {
                var cropOrganismIdList = document.getElementById("cropOrganismId");
                cropOrganismIdList.options.length = 1;
                for(var i in matchingCropOrganismOptions)
                {
                    cropOrganismIdList.options[cropOrganismIdList.options.length] = matchingCropOrganismOptions[i];
                }
                cropOrganismIdList.options[cropOrganismIdList.options.length] = new Option("----",-1);
                for(var i in theRest)
                {
                    cropOrganismIdList.options[cropOrganismIdList.options.length] = theRest[i];
                }
            }
                
            var updateCropPests = function(){
                    var theForm = document.getElementById('${formId}');
                    var selectedCropId = theForm["cropOrganismId"].options[theForm["cropOrganismId"].options.selectedIndex].value;
                    
                    $.getJSON( "/rest/organism/croppest/" + selectedCropId, function( json ) {
                            updateCropPestsCallback(json);
                    })
                    .fail(function() {
                        alert( "Error getting pests for this crop. Please contact system administrator" );
                      });
            };

            var updateCropPestsCallback = function(cropPest) {
                    var pestList = document.getElementById('${formId}')["pestOrganismId"];
                    if(cropPest == null)
                    {
                            // Need to reorganize pests back to default
                            var allPests = [];
                            for(var i=2;i<pestList.options.length;i++)
                            {
                                    allPests.push(pestList.options[i]);
                            }
                            allPests.sort(compareSelectListOptions);
                            pestList.options.length=2; // Keeping the top two
                            for(var i=0;i<allPests.length;i++)
                            {
                                    pestList.options[pestList.options.length] = allPests[i];
                            }
                    }
                    else
                    {
                            var prioritatedPests = [];
                            var unprioritatedPests = []
                            for(var i=2;i<pestList.options.length;i++)
                            {
                                    if(cropPest.pestOrganismIds.indexOf(parseInt(pestList.options[i].value)) >= 0)
                                    {
                                            //console.log(pestList.options[i].value + " is prioritated");
                                            prioritatedPests.push(pestList.options[i]);
                                    }
                                    else if(pestList.options[i].value != "-1") // Avoiding the "---" option
                                    {
                                            //console.log(pestList.options[i].value + " is unprioritated");
                                            unprioritatedPests.push(pestList.options[i]);
                                    }

                            }
                            pestList.options.length=2; // Keeping the top two
                            for(var i=0;i<prioritatedPests.length;i++)
                            {
                                    pestList.options[pestList.options.length] = prioritatedPests[i];
                            }
                            pestList.options[pestList.options.length] = new Option("---","-1");
                            for(var i=0;i<unprioritatedPests.length;i++)
                            {
                                    pestList.options[pestList.options.length] = unprioritatedPests[i];
                            }
                    }
            };

          window.handleWeatherDatasourceSelected = function(weatherStationPointOfInterestId) {
            let weatherStationList = document.getElementById("weatherStationPointOfInterestId");
            let listContainer = document.getElementById("weatherstationListContainer");
            weatherStationPointOfInterestIdHiddenField = document.getElementById("weatherStationPointOfInterestIdHidden");
            // Which data source is selected?
            let useGridWeatherData;
            const radios = document.getElementsByName("useGridWeatherData");
            for(const radio of radios)
            {
                if(radio.checked)
                {
                    useGridWeatherData = radio.value;
                    break;
                }
            }
            //console.info("useGridWeatherData=" + useGridWeatherData)
            if(useGridWeatherData == "true")
            {
              // Select weatherStationId -2
              weatherStationList.selectedIndex = 0;
              // Disable the weatherstation select list
              weatherStationList.disabled=true;
              listContainer.style.display="none";
              weatherStationList.name="weatherStationPointOfInterestIdDisabled";

              // Enable the hidden field
              weatherStationPointOfInterestIdHiddenField.disabled=false
              weatherStationPointOfInterestIdHiddenField.name="weatherStationPointOfInterestId";
            }
            else if(useGridWeatherData == "false")
            {
              // Select weatherStationId -1 OR the optionally provided weatherStationPointOfInterestId
              if(weatherStationPointOfInterestId == undefined || weatherStationPointOfInterestId == null)
              {
                weatherStationList.selectedIndex = 1;
              }
              else
              {
                 for(let i=0;i<weatherStationList.options.length;i++)
                 {
                    weatherStationList.options[i].selected = parseInt(weatherStationList.options[i].value) == weatherStationPointOfInterestId;
                 }
              }
              // Enable the weather station select list
              weatherStationList.disabled=false;
              listContainer.style.display="block";

              weatherStationList.name="weatherStationPointOfInterestId";
              // Disable the hidden field
              weatherStationPointOfInterestIdHiddenField.disabled=true
              weatherStationPointOfInterestIdHiddenField.name="weatherStationPointOfInterestIdDisabled";
            }
          };

          window.setUseGridWeatherData = function(useGridWeatherData)
          {
            //console.info("setuseGridWeatherData, input value=" + useGridWeatherData);
                const radios = document.getElementsByName("useGridWeatherData");
                for(const radio of radios)
                {
                    //console.info("radio value=" + radio.value + ", so the radio should " + (radio.value == useGridWeatherData ? "": "NOT") + " be checked" );
                    radio.checked = radio.value == useGridWeatherData;
                }
          }



          window.handleLocationChanged = function(){
            let weatherstationSelect = document.getElementById("weatherStationPointOfInterestId");
            let weatherDatasourceFieldset = document.getElementById("weatherDatasourceFieldset");
            // Which location has been selected?
            let selectedPoiId = document.getElementById("locationPointOfInterestId").options[document.getElementById("locationPointOfInterestId").selectedIndex].value;
            if(selectedPoiId <= 0)
            {
                if(weatherDatasourceFieldset != null) weatherDatasourceFieldset.disabled = true;
                weatherstationSelect.selectedIndex = 0;
                return;
            }
            // Enable the weather datasource fieldset
            if(weatherDatasourceFieldset != null) weatherDatasourceFieldset.disabled = false;
            
            let selectedLocation = undefined;
            for(let i=0; i<locationList.length;i++)
            {
                if(locationList[i].pointOfInterestId == selectedPoiId)
                {
                    selectedLocation = locationList[i];
                }
            }

            if(selectedLocation.pointOfInterestTypeId == POI_TYPE_WEATHERSTATION)
            {
                renderWeatherstationSelect(weatherstationSelect,locationList, selectedLocation.pointOfInterestId, selectedLocation.location);
            }
            else
            {
                renderWeatherstationSelect(weatherstationSelect,locationList, null, selectedLocation.location);
                weatherstationSelect.selectedIndex = 0;
            }

            <#if isGridForecastSupported>

            setUseGridWeatherData(selectedLocation.pointOfInterestTypeId == POI_TYPE_WEATHERSTATION ? "false" : "true");
            handleWeatherDatasourceSelected(selectedLocation.pointOfInterestTypeId == POI_TYPE_WEATHERSTATION ? selectedLocation.pointOfInterestId: undefined);

            </#if>
          }
	</script>
</#macro>
<#macro custom_css>
    <link href="/css/3rdparty/chosen.min.css" rel="stylesheet" />
</#macro>
<#macro page_contents>
<div class="singleBlockContainer">
	<p><a href="/forecastConfiguration" class="btn btn-default back" role="button">${i18nBundle.back}</a></p>
    <h1>${i18nBundle.viewForecastConfiguration}</h1>
    <div id="errorMsgEl" class="alert alert-danger" <#if !formValidation?has_content> style="display:none;"</#if>>
    <#if formValidation?has_content>${formValidation.validationMessages?replace("\n", "<br>")}</#if>
	</div>
    <#if multipleNew>
    <div class="alert alert-warning">${i18nBundle.multipleNewWarningMsg}</div>
    </#if>
	<#if messageKey?has_content>
		<div class="alert alert-success">${i18nBundle(messageKey)}</div>
	</#if>
        <form id="${formId}" role="form" action="/forecastConfiguration?action=forecastConfigurationFormSubmit" method="POST" onsubmit2="return false;" onsubmit="try{ return (validateForm(this) && validateForm(this, this.modelId.options[this.modelId.selectedIndex].value));}catch(err){alert(err);return false;}">
          <input type="hidden" id="forecastConfigurationId" name="forecastConfigurationId" value="${forecastConfiguration.forecastConfigurationId!"-1"}"/>
          <div class="form-group">
	    <label for="modelId">${i18nBundle.modelId}</label>
	    <select class="form-control" id="modelId" name="modelId" onblur="validateField(this);" onchange="renderModelSpecificFields('${formId}');">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.modelId?lower_case}</option>
	    	<#list modelInformations as modelInformation>
	    	<option value="${modelInformation.modelId}"<#if forecastConfiguration.modelId?has_content && modelInformation.modelId == forecastConfiguration.modelId> selected="selected"</#if>>
	    		<#if i18nBundle.containsKey(modelInformation.modelId)>
				${i18nBundle[modelInformation.modelId]}
			<#else>
				${modelInformation.defaultName}
			</#if>
		</option>
	    	</#list>
	    </select>
	    <span class="help-block" id="${formId}_modelId_validation"></span>
	  </div>
          <#if ! forecastConfiguration.cropOrganismId?has_content>
            <div class="form-group">
              <label for="cropCategoryId">${i18nBundle.listSelectedCropCategoryOnTop}</label>
              <select class="form-control" id="cropCategoryIdList" name="cropCategoryId" onchange="filterCrops(this.options[this.options.selectedIndex].value);">
                  <option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.cropCategory?lower_case}</option>
                  <!-- Options added by JavaScript function renderCropCategories() -->
              </select>
              </div>
            </#if>
	  <div class="form-group">
	    <label for="cropOrganismId">${i18nBundle.cropOrganismId}</label>
	    <select class="form-control" id="cropOrganismId" name="cropOrganismId" onblur="validateField(this);" onchange="updateCropPests();">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.cropOrganismId?lower_case}</option>
	    	<#list allCrops?sort_by("latinName") as organism>
			<option value="${organism.organismId}"
				<#if (forecastConfiguration.cropOrganismId?has_content && forecastConfiguration.cropOrganismId.organismId == organism.organismId)>
					selected="selected"
				</#if>
			>${organism.latinName!""}/${organism.tradeName!""}/${organism.getLocalName(currentLocale.language)!""} (${hierarchyCategories.getName(organism.hierarchyCategoryId)})</option>
		</#list>
	    </select>
	    <span class="help-block" id="${formId}_cropOrganismId_validation"></span>
	  </div>
	  <div class="form-group">
		  <div class="checkbox">
			<label>
				<input type="checkbox" name="isPrivate"<#if forecastConfiguration.isPrivate?has_content && forecastConfiguration.isPrivate == true> checked="checked"</#if>
				<#if ! user.isSuperUser() && ! user.isOrganizationAdmin()> readonly="readonly" disabled="disabled"</#if>/>
			</label>
			${i18nBundle.isPrivate}
      <span class="help-block" id="${formId}_isPrivate_validation"></span>
		</div>
	  </div>
	  <div class="form-group">
	    <label for="pestOrganismId">${i18nBundle.pestOrganismId}</label>
	    <select class="form-control" id="pestOrganismId" name="pestOrganismId" onblur="validateField(this);" onchange="renderModelSpecificFields('${formId}');">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.pestOrganismId?lower_case}</option>
	    	<#list allPests?sort_by("latinName") as organism>
			<option value="${organism.organismId}"
				<#if (forecastConfiguration.pestOrganismId?has_content && forecastConfiguration.pestOrganismId.organismId == organism.organismId)>
					selected="selected"
				</#if>
			>${organism.latinName!""}/${organism.tradeName!""}/${organism.getLocalName(currentLocale.language)!""} (${hierarchyCategories.getName(organism.hierarchyCategoryId)})</option>
		</#list>
	    </select>
	    <span class="help-block" id="${formId}_pestOrganismId_validation"></span>
	  </div>
	  <#if !multipleNew?has_content || !multipleNew>
	  <div class="form-group">
	    <label for="locationPointOfInterestId">${i18nBundle.locationPointOfInterestId}</label>
          <div class="select-container" style="flex: 1; display: flex; align-items: center;">
            <select class="form-control" id="locationPointOfInterestId" name="locationPointOfInterestId" onchange="handleLocationChanged();" onblur="validateField(this);" style="width: calc(100% - 30px);">
            </select>&nbsp;
            <button type="button" class="btn btn-primary" onclick="openLocationMap()"><i class="fa fa-map-marker fa-lg"></i>&nbsp;&nbsp;Velg i kart</button>
            
              <div id="location-map" class="map-modal"></div>
          </div>
	    <span class="help-block" id="${formId}_locationPointOfInterestId_validation"></span>
	  </div>
      <#if isGridForecastSupported>
      <div class="alert alert-info" role="alert">Velg sted ovenfor først, og velg deretter værdatakilde. Du kan enten velge en av de tilgjengelige værstasjonene (sortert etter avstand til stedet), 
    eller at ditt valgte steds plassering brukes til å hente værdata fra en ekstern tjeneste. Hvis ditt sted ligger nær
    en av værstasjonene, gir det som oftest den beste kvaliteten på værdata.</div>
    <#else>
    <div class="alert alert-info" role="alert">Velg sted ovenfor først, og velg deretter værstasjon. Hvis stedet du har valgt ikke er en værstasjon, vil værstasjonslista sorteres etter avstand til ditt sted.</div>
    </#if>
    <#if isGridForecastSupported>
    <fieldset id="weatherDatasourceFieldset" <#if !forecastConfiguration.weatherStationPointOfInterestId?has_content>disabled</#if>>
    <legend style="margin-bottom: 0px;">${i18nBundle.weatherDatasource}</legend>
     
        <input type="hidden" id="weatherStationPointOfInterestIdHidden" name="weatherStationPointOfInterestIdDisabled" value="-2" disabled="disabled"/>
        <div class="form-group">
          <div class="radio">
          <label>
            <input type="radio" id="useGridWeatherData" value="true" name="useGridWeatherData"<#if forecastConfiguration.useGridWeatherData?has_content && forecastConfiguration.useGridWeatherData == true> checked="checked"</#if> onclick="handleWeatherDatasourceSelected();"/>
          </label>
          ${i18nBundle.useGridWeatherData}
        </div>
      </#if>
    <#if isGridForecastSupported><div class="radio">
    <label>
        <input type="radio" name="useGridWeatherData" id="useWeatherstationRadio" value="false" onclick="handleWeatherDatasourceSelected();">
        ${i18nBundle.useWeatherStation}
    </label>
    </#if>
      <div class="form-group" id="weatherstationListContainer" style="display: <#if isGridForecastSupported>none<#else>block</#if>;">
        <#if !isGridForecastSupported><label for="weatherStationPointOfInterestId">${i18nBundle.weatherStationPointOfInterestId}</label></#if>
              <select class="form-control" id="weatherStationPointOfInterestId" name="weatherStationPointOfInterestId" onblur="if(document.getElementById('useWeatherstationRadio') == null || document.getElementById('useWeatherstationRadio').checked) {validateField(this);}">
                 
              </select>
        <span class="help-block" id="${formId}_weatherStationPointOfInterestId_validation"></span>
      </div>
    <#if isGridForecastSupported>
    </div> 
    </fieldset>
    </#if>
	  <#else>
	  <input type="hidden" name="multipleNew" value="true"/>
	  <div class="form-group">
	    <label for="weatherStationPointOfInterestIds">${i18nBundle.weatherStationPointOfInterestIds}</label>
	    <select class="form-control chosen-select" name="weatherStationPointOfInterestIds" onblur="validateField(this);" multiple="multiple">
	    	<#list weatherStationPointOfInterests?sort_by("name") as poi>
	    	<option value="${poi.pointOfInterestId}">${poi.name}</option>
	    	</#list>
	    </select>
	    <span class="help-block" id="${formId}_weatherStationPointOfInterestIds_validation"></span>
	  </div>
	  </#if>
          <div class="form-group">
	    <label for="dateStart">${i18nBundle.dateStart}</label>
	    <input type="date" class="form-control" name="dateStart" placeholder="${i18nBundle.dateStart}" value="${(forecastConfiguration.dateStart)!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_dateStart_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="dateEnd">${i18nBundle.dateEnd}</label>
	    <input type="date" class="form-control" name="dateEnd" placeholder="${i18nBundle.dateEnd}" value="${(forecastConfiguration.dateEnd)!""}" onblur="validateField(this);" />
	    <span class="help-block" id="${formId}_dateEnd_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="timeZone">${i18nBundle.timeZone}</label>
	    <select class="form-control" name="timeZone" onblur="validateField(this);">
		<#list availableTimeZones as timeZoneId>
			<option value="${timeZoneId}"<#if 
						(forecastConfiguration.timeZone?has_content && timeZoneId == forecastConfiguration.timeZone)
						|| (!forecastConfiguration.timeZone?has_content && timeZoneId == defaultTimeZoneId)
						> selected="selected"</#if>>${timeZoneId}</option>
		</#list>
	     </select>
	     <span class="help-block" id="${formId}_timeZone_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="vipsLogicUserId">${i18nBundle.vipsLogicUserId}</label>
	    <select class="form-control" name="vipsLogicUserId" onblur="validateField(this);">
	    	<#if user.isSuperUser() || user.isOrganizationAdmin()>
			<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.vipsLogicUserId?lower_case}</option>
			<#list vipsLogicUsers?sort_by("lastName") as vlUser>
			<option value="${vlUser.userId}"<#if (forecastConfiguration.vipsLogicUserId?has_content && vlUser.userId == forecastConfiguration.vipsLogicUserId.userId)
                                                            || (!forecastConfiguration.vipsLogicUserId?has_content && vlUser.userId == user.userId)> selected="selected"</#if>>${vlUser.lastName}, ${vlUser.firstName}</option>
			</#list>
	    	<#else>
			<option value="${user.userId}" selected="selected">${user.lastName}, ${user.firstName}</option>
	    	</#if>
	    </select>
	    <span class="help-block" id="${formId}_vipsLogicUserId_validation"></span>
	  </div>
	  <fieldset id="modelSpecificFields">
	  	
	  </fieldset>
	  <button type="submit" class="btn btn-default">${i18nBundle.submit}</button>
	  <!--button type="button" onclick="var theForm=document.getElementById('${formId}');validateForm(theForm);validateForm(theForm, theForm.modelId.options[theForm.modelId.selectedIndex].value); ">Test</button-->
	  <button type="button" class="btn btn-warning" onclick="if(confirm('${i18nBundle.confirmLeaveForm}')){window.location.href='/forecastConfiguration';}">${i18nBundle.leaveForm}</button>
	  <#if forecastConfiguration.forecastConfigurationId?has_content>
	  <button type="button" class="btn btn-danger" onclick="if(confirm('${i18nBundle.confirmDelete}')){window.location.href='/forecastConfiguration?action=deleteForecastConfiguration&forecastConfigurationId=${forecastConfiguration.forecastConfigurationId}';}">${i18nBundle.delete}</button>
	  </#if>
        </form>
</div>
</#macro>
<@page_html/>
