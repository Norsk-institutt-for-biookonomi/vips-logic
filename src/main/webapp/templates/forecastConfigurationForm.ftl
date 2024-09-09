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

        // Read the list of locations into javascript array, to be able to dynamically manipulate it
        let locationList = [
            <#list locationPointOfInterests as poi>
            {
                "pointOfInterestId": "${poi.pointOfInterestId}",
                "name": "${poi.name?json_string}"
            },
            </#list>
        ];
        renderPoiSelect(selectLocationElement, locationList);

        // Populate select list for point of interest -> NOT GENERAL! Default value is specific to location.
        function renderPoiSelect(selectElement, elementList, selectedId) {
            selectElement.innerHTML = '';

            const createOption = (value, text, isSelected = false) => {
                const option = document.createElement('option');
                option.value = value;
                option.textContent = text;
                if (isSelected) option.selected = true;
                return option;
            };

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

        function selectPoi(selectElement, selectedId) {
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
                persistNewLocation(poiData);
            } else {
                selectPoi(selectLocationElement, poiData.pointOfInterestId);
            }
        }

        function persistNewLocation(poiData) {
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
                    console.info("Point of interest successfully persisted", poi);
                })
                .catch(error => {
                    console.error("Unable to persist new point of interest", error);
                });
        }

        const typeNameMap = {
            0: "${i18nBundle["pointOfInterestType_0"]}",
            1: "${i18nBundle["pointOfInterestType_1"]}",
            2: "${i18nBundle["pointOfInterestType_2"]}",
            3: "${i18nBundle["pointOfInterestType_3"]}",
            5: "${i18nBundle["pointOfInterestType_5"]}",
            6: "${i18nBundle["pointOfInterestType_6"]}",
            7: "${i18nBundle["pointOfInterestType_7"]}"
        };

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
                const locationMapInstance = new MapModal('location-map', typeNameMap, geoJson, '${currentLanguage}', true, callbackOnCloseLocationMap);
                const selectedPoiId = getSelectedPoiId(selectLocationElement);
                locationMapInstance.openModal(selectedPoiId);
            })
            .catch(error => {
                console.error('Unable to open map', error);
            });
        }

    </script>
	<script type="text/javascript">
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
          var handleUseGridWeatherDataClicked = function(theCheckBox, weatherStationPointOfInterestId) {
            weatherStationList = document.getElementById("weatherStationPointOfInterestId");
            weatherStationPointOfInterestIdHiddenField = document.getElementById("weatherStationPointOfInterestIdHidden");
            if(theCheckBox.checked)
            {
              // Select weatherStationId -2
              weatherStationList.selectedIndex = 0;
              // Disable the weatherstation select list
              weatherStationList.disabled=true;
              weatherStationList.name="weatherStationPointOfInterestIdDisabled";

              // Enable the hidden field
              weatherStationPointOfInterestIdHiddenField.disabled=false
              weatherStationPointOfInterestIdHiddenField.name="weatherStationPointOfInterestId";
            }
            else
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
              weatherStationList.name="weatherStationPointOfInterestId";
              // Disable the hidden field
              weatherStationPointOfInterestIdHiddenField.disabled=true
              weatherStationPointOfInterestIdHiddenField.name="weatherStationPointOfInterestIdDisabled";
            }
          };

          // Setting weather station select list state correct on page load
          <#if isGridForecastSupported>
          handleUseGridWeatherDataClicked(document.getElementById("useGridWeatherData")<#if forecastConfiguration.weatherStationPointOfInterestId?has_content && forecastConfiguration.weatherStationPointOfInterestId.pointOfInterestId?has_content>,${forecastConfiguration.weatherStationPointOfInterestId.pointOfInterestId}</#if>);
          </#if>

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
            <select class="form-control" id="locationPointOfInterestId" name="locationPointOfInterestId" onblur="validateField(this);" style="width: calc(100% - 30px);">
            </select>
            <i id="open-map-modal-icon" class="fa fa-map-marker" onclick="openLocationMap()"></i>
              <div id="location-map" class="map-modal"></div>
          </div>
	    <span class="help-block" id="${formId}_locationPointOfInterestId_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="weatherStationPointOfInterestId">${i18nBundle.weatherStationPointOfInterestId}</label>
            <select class="form-control" id="weatherStationPointOfInterestId" name="weatherStationPointOfInterestId" onblur="if(!document.getElementById('useGridWeatherData').checked) {validateField(this);}">
                <#if isGridForecastSupported>
              <option value="-2">${i18nBundle.doNotUse} ${i18nBundle.weatherStationPointOfInterestId?lower_case}</option>
            </#if>
            <option value="-1"<#if !forecastConfiguration.weatherStationPointOfInterestId?has_content && !forecastConfiguration.useGridWeatherData> selected="selected"</#if>>${i18nBundle.pleaseSelect} ${i18nBundle.weatherStationPointOfInterestId?lower_case}</option>
                <#list weatherStationPointOfInterests?sort_by("name") as poi>
                <option value="${poi.pointOfInterestId}"<#if
              forecastConfiguration.weatherStationPointOfInterestId?has_content
              && forecastConfiguration.weatherStationPointOfInterestId.pointOfInterestId?has_content
              && poi.pointOfInterestId == forecastConfiguration.weatherStationPointOfInterestId.pointOfInterestId> selected="selected"</#if>>${poi.name}</option>
                </#list>
            </select>
	    <span class="help-block" id="${formId}_weatherStationPointOfInterestId_validation"></span>
	  </div>
    <#if isGridForecastSupported>
      <input type="hidden" id="weatherStationPointOfInterestIdHidden" name="weatherStationPointOfInterestIdDisabled" value="-2" disabled="disabled"/>
      <div class="form-group">
        <div class="checkbox">
        <label>
          <input type="checkbox" id="useGridWeatherData" name="useGridWeatherData"<#if forecastConfiguration.useGridWeatherData?has_content && forecastConfiguration.useGridWeatherData == true> checked="checked"</#if> onclick="handleUseGridWeatherDataClicked(this);"/>
        </label>
        ${i18nBundle.useGridWeatherData}
        <span class="help-block" id="${formId}_useGridWeatherData_validation"></span>
      </div>
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
