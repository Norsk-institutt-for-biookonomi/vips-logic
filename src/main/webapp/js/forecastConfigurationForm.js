/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

/* 
 * Helpers for the template forecastConfigurationForm.ftl.
 * Depends on /js/validateForm.js
 * @copyright 2013-2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 * 
 */

/**
 * Responsible for rendering the model specific fields
 * @param {String} formId DOM id of the form to render to
 * @returns {void}
 */
function renderModelSpecificFields(formId)
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
            renderModelSpecificFieldsCallback(modelId, formId);
        });
    }
    else
    {
        renderModelSpecificFieldsCallback(modelId, formId);
    }
}

/**
 * 
 * @param {type} modelId
 * @returns {undefined}
 */
function renderModelSpecificFieldsCallback(modelId,formId)
{
    var fieldSet = document.getElementById("modelSpecificFields");
    var formDefinition = formDefinitions[modelId];
    
    // If there are no specific fields, erase what was there
    if(formDefinition.fields.length === 0)
    {
        fieldSet.innerHTML = "";
        fieldSet.style.display="none";
        return;
    }
    
    var fieldsHTML = ["<legend>" + getI18nMsg("specificFieldsForX", [getI18nMsg(modelId)])+ "</legend>"];
    for(var i in formDefinition.fields){
        var fieldDefinition = formDefinition.fields[i];
        fieldsHTML.push(createFieldHTML(modelId, formId, fieldDefinition));
    }
    
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
                        '<span class="help-block" id="{1}_{0}_validation"></span>',
                        '</div>'
                    ].join("\n");

function createFieldHTML(modelId, formId, fieldDefinition)
{
    var fieldHTML = "";
    if(fieldDefinition.fieldType !== fieldTypes.TYPE_SELECT_SINGLE && fieldDefinition.fieldType !== fieldTypes.TYPE_SELECT_MULTIPLE)
    {
        var inputType = getHTMLInputType(fieldDefinition.dataType);
        fieldHTML = '<input type="' + inputType + '" class="form-control" name="' + fieldDefinition.name + '" placeholder="' + getI18nMsg(fieldDefinition.name) + '" onblur="validateField(this,\'' + modelId + '\');" value="' + (fieldDefinition.defaultValue != null ? fieldDefinition.defaultValue : "")  + '" />';
    }
    else if(fieldDefinition.fieldType === fieldTypes.TYPE_SELECT_SINGLE || fieldDefinition.fieldType === fieldTypes.TYPE_SELECT_MULTIPLE)
    {
        fieldHTML = '<select class="form-control" name="' + fieldDefinition.name + '"onblur="validateField(this,\'' + modelId + '\')" ' + (fieldDefinition.fieldType === fieldTypes.TYPE_SELECT_MULTIPLE ? ' multiple="multiple"' : '') + '>';
        fieldHTML += getLocalizedOptionsHTML(fieldDefinition.selectOptions);
        fieldHTML += '</select>';
    }

    return [
            replaceParams(FIELD_PREFIX,[fieldDefinition.name, getI18nMsg(fieldDefinition.name)]),
            fieldHTML,
            replaceParams(FIELD_SUFFIX,[fieldDefinition.name,formId])
    ].join("\n");
}

/**
 * Mapping between field type in VIPSLogic and HTML input type
 * @param {String} fieldType
 * @returns {String}
 */
function getHTMLInputType(dataType)
{
    switch(dataType)
    {
        case dataTypes.TYPE_STRING:
            return "text";
            break;
        case dataTypes.TYPE_INTEGER:
            return "number";
            break;
        case dataTypes.TYPE_DOUBLE:
            return "number' step='any";
            break;
        case dataTypes.TYPE_DATE:
            return "date";
            break;
        case dataTypes.TYPE_TIMESTAMP:
            return "datetime";
            break;
        case dataTypes.TYPE_PASSWORD:
            return "password";
            break;
        case dataTypes.TYPE_EMAIL:
            return "email";
            break;
        default:
            return "text";
    }
}
