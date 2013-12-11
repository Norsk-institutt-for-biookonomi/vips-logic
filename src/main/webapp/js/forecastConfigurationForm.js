/*
 * Copyright (c) 2013 Bioforsk <http://www.bioforsk.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with VIPSLogic.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

/* 
 * Helpers for the template forecastConfigurationForm.ftl.
 * Depends on /js/validateForm.js
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 * 
 */

/**
 * Responsible for rendering the model specific fields
 * @returns {void}
 */
function renderModelSpecificFields()
{
    // Get the model Id
    var modelIdEl = document.getElementById("modelId");
    var modelId = modelIdEl.options[modelIdEl.options.selectedIndex].value;
    
    // Get form definition if not already present
    if(formDefinitions[modelId] === null || formDefinitions[modelId] === undefined)
    {
    // Get the fields (JSON format)
     $.getJSON( "/formdefinitions/models/" + modelId + ".json")
        .done(function(json){
            formDefinitions[modelId] = json;
            renderModelSpecificFieldsCallback(modelId);
        });
    }
    else
    {
        renderModelSpecificFieldsCallback(modelId);
    }
}

/**
 * 
 * @param {type} modelId
 * @returns {undefined}
 */
function renderModelSpecificFieldsCallback(modelId)
{
    var formDefinition = formDefinitions[modelId];
    var fieldsHTML = ["<legend>" + getI18nMsg("specificFieldsForX", [getI18nMsg(modelId)])+ "</legend>"];
    for(var i in formDefinition.fields){
        var fieldDefinition = formDefinition.fields[i];
        fieldsHTML.push(createFieldHTML(modelId, fieldDefinition));
    }
    
    var fieldSet = document.getElementById("modelSpecificFields");
    fieldSet.innerHTML = fieldsHTML.join("\n");
    fieldSet.style.display="block";
    
    // Fetch any values if present
    fetchModelSpecificFieldValues();
}

/**
 * If this forecast configuration has existing model specific values: fetch them and attempt to display them
 * If the values are for a different model than the currently selected one, they will not be displayed
 * 
 * @returns {void}
 */
function fetchModelSpecificFieldValues()
{
    // Get forecast configuration id
    var forecastConfigurationId = document.getElementById("forecastConfigurationId").value;
    // Just checking...
    if(forecastConfigurationId <= 0)
    {
        return;
    }
    $.getJSON( "/rest/forecastmodelconfiguration/" + forecastConfigurationId)
        .done(function(json){
            fetchModelSpecificFieldValuesCallback(json);
        });
    
}

/**
 * 
 * @param {JSON} fieldValues
 * @returns {undefined}
 */
function fetchModelSpecificFieldValuesCallback(fieldValues)
{
    var theForm = getFormForField(document.getElementById("modelSpecificFields"));
    for(var i in fieldValues){
        // Parametername consists of [MODELID]_[FORM_FIELD_NAME_UNCAMELED]
        // We convert it to formfield name
        var fieldName = getFieldNameFromModelConfigParameter(fieldValues[i].forecastModelConfigurationPK.modelConfigParameter);
        //console.log(fieldValues[i].forecastModelConfigurationPK.modelConfigParameter + ": " + fieldName);
        if(theForm[fieldName] !== null && theForm[fieldName] !== undefined)
        {
            theForm[fieldName].value=fieldValues[i].parameterValue;
        }
    }
}

/**
 * Parametername consists of [MODELID]_[FORM_FIELD_NAME_UNCAMELCASED]
 * We convert it to formfield name (formFieldNameCamelCased)
 * @param {type} modelConfigParameter
 * @returns {undefined}
 */
function getFieldNameFromModelConfigParameter(modelConfigParameter)
{
    var fieldName = "";
    var fieldNameParts = modelConfigParameter.split("_");
    for(var i=1;i<fieldNameParts.length;i++)
    {
        // First letter in first part is lower case
        // First letter in all consecutive parts are upper case
        // All other letters are lower case
        fieldName +=    (i === 1 ? fieldNameParts[i].charAt(0).toLowerCase() : fieldNameParts[i].charAt(0).toUpperCase())
                        + fieldNameParts[i].slice(1).toLowerCase();
    }
    return fieldName;
}

var FIELD_PREFIX =  [
                        '<div class="form-group">',
                        '<label for="{0}">{1}</label>'
                    ].join("\n");
                    
var FIELD_SUFFIX =  [
                        '<span class="help-block" id="forecastConfigurationForm_{0}_validation"></span>',
                        '</div>'
                    ].join("\n");

function createFieldHTML(modelId, fieldDefinition)
{
    var fieldHTML = "";
    if(fieldDefinition.type !== formFieldTypes.TYPE_SELECT_SINGLE || fieldDefinition.type !== formFieldTypes.TYPE_SELECT_MULTIPLE)
    {
        var inputType = getHTMLInputType(fieldDefinition.type);
        fieldHTML = '<input type="' + inputType + '" class="form-control" name="' + fieldDefinition.name + '" placeholder="' + getI18nMsg(fieldDefinition.name) + '" onblur="validateField(this,\'' + modelId + '\');" />';
    }

    return [
            replaceParams(FIELD_PREFIX,[fieldDefinition.name, getI18nMsg(fieldDefinition.name)]),
            fieldHTML,
            replaceParams(FIELD_SUFFIX,[fieldDefinition.name])
    ].join("\n");
}

/**
 * Mapping between field type in VIPSLogic and HTML input type
 * @param {String} fieldType
 * @returns {String}
 */
function getHTMLInputType(fieldType)
{
    switch(fieldType)
    {
        case formFieldTypes.TYPE_STRING:
            return "text";
            break;
        case formFieldTypes.TYPE_INTEGER:
            return "number";
            break;
        case formFieldTypes.TYPE_DOUBLE:
            return "number";
            break;
        case formFieldTypes.TYPE_DATE:
            return "date";
            break;
        case formFieldTypes.TYPE_TIMESTAMP:
            return "datetime";
            break;
        case formFieldTypes.TYPE_PASSWORD:
            return "password";
            break;
        case formFieldTypes.TYPE_EMAIL:
            return "email";
            break;
        default:
            return "text";
    }
}

	    
	    



