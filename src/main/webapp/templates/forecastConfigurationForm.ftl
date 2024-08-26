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
<#macro page_head>
    <title>${i18nBundle.viewForecastConfiguration}</title>
</#macro>
<#macro custom_js>
    <script src="/js/resourcebundle.js"></script>
    <script src="/js/forecastConfigurationForm.js"></script>
    <script src="/js/validateForm.js"></script>
    <script src="//code.jquery.com/ui/1.10.3/jquery-ui.min.js"></script>
    <link href="//code.jquery.com/ui/1.10.3/themes/redmond/jquery-ui.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css" />
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
        function callbackPersistNewPoint(pointData) {
            const userId = ${user.userId};
            const params = {
                'name': pointData.name,
                'typeId': pointData.typeId,
                'longitude': pointData.longitude,
                'latitude': pointData.latitude,
                'altitude': '0', // default value - populate using a service for getting altitude for coordinates?
                'userId': userId,
            }
            $.ajax({
                url: "/rest/poi",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(params),
                success: function(response) {
                    addOption(response.pointOfInterestId, response.name);
                    mapModalInstance.saveSuccess(response.pointOfInterestId);
                    console.info("Success:", response);
                },
                error: function(jqXHR, textStatus, errorThrown) {
                    console.error("Error:", textStatus, errorThrown);
                }
            });
        }
        function callbackUpdateLocationPointOfInterest(pointOfInterestId) {
            const selectBox = document.querySelector('select[name="locationPointOfInterestId"]');
            if(pointOfInterestId) {
                let optionFound = false;
                for (let i = 0; i < selectBox.options.length; i++) {
                    if (selectBox.options[i].value == pointOfInterestId) {
                        selectBox.selectedIndex = i; // Select the matching option
                        optionFound = true;
                        break;
                    }
                }
                if (!optionFound) {
                    console.error("No matching option found for poi.id:", pointOfInterestId);
                }
            }
        }
        // TODO Ensure options are sorted alphabetically..?
        function addOption(pointOfInterestId, name) {
            let selectElement = document.querySelector('select[name="locationPointOfInterestId"]');
            let newOption = document.createElement("option");
            newOption.value = pointOfInterestId;
            newOption.text = name;
            selectElement.insertBefore(newOption, selectElement.firstChild);
            selectElement.value = pointOfInterestId;
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

        const poiGeoJson = JSON.parse('${locationPointOfInterestsGeoJson?json_string}');
        const stationGeoJson = JSON.parse('${weatherStationPointOfInterestsGeoJson?json_string}')
        const mapModalInstance = new MapModal('mapContainer', typeNameMap, poiGeoJson, true, callbackPersistNewPoint, callbackUpdateLocationPointOfInterest);
        window.mapModalInstance = mapModalInstance;

        window.openModal = () => {
            const selectElement = document.querySelector('select[name="locationPointOfInterestId"]');
            const selectedOption = selectElement.options[selectElement.selectedIndex];
            if(selectedOption.value && selectedOption.value !== '-1') {
                window.mapModalInstance.setSelectedLocation(selectedOption.value);
            }
            window.mapModalInstance.openModal();
        };

        window.closeModal = () => window.mapModalInstance && window.mapModalInstance.closeModal();
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
	    <select class="form-control" name="locationPointOfInterestId" onblur="validateField(this);">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.locationPointOfInterestId?lower_case}</option>
	    	<#list locationPointOfInterests?sort_by("name") as poi>
	    	<option value="${poi.pointOfInterestId}"<#if forecastConfiguration.locationPointOfInterestId?has_content && poi.pointOfInterestId == forecastConfiguration.locationPointOfInterestId.pointOfInterestId> selected="selected"</#if>>${poi.name}</option>
	    	</#list>
	    </select>
          <button onclick="openModal()">Open Map</button>
          <div id="mapModal" class="modal">
              <div class="modal-content">
                  <span class="close-button" onclick="closeModal()">&times;</span>
                  <div id="mapContainer" style="height: 100vh; width: 100%; position: relative;">
                  </div>
              </div>
          </div>
	    <span class="help-block" id="${formId}_locationPointOfInterestId_validation"></span>
	  </div>
	  <div class="form-group">
	    <label for="weatherStationPointOfInterestId">${i18nBundle.weatherStationPointOfInterestId}</label>
	    <select class="form-control" name="weatherStationPointOfInterestId" onblur="validateField(this);">
	    	<option value="-1">${i18nBundle.pleaseSelect} ${i18nBundle.weatherStationPointOfInterestId?lower_case}</option>
	    	<#list weatherStationPointOfInterests?sort_by("name") as poi>
	    	<option value="${poi.pointOfInterestId}"<#if forecastConfiguration.weatherStationPointOfInterestId?has_content && poi.pointOfInterestId == forecastConfiguration.weatherStationPointOfInterestId.pointOfInterestId> selected="selected"</#if>>${poi.name}</option>
	    	</#list>
	    </select>
	    <span class="help-block" id="${formId}_weatherStationPointOfInterestId_validation"></span>
	  </div>
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
