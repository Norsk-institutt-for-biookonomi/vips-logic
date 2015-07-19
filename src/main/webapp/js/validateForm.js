/*
 * Copyright (c) 2014 NIBIO <http://www.nibio.no/>. 
 * 
 * This file is part of VIPSLogic.
 * VIPSLogic is free software: you can redistribute it and/or modify
 * it under the terms of the NIBIO Open Source License as published by 
 * NIBIO, either version 1 of the License, or (at your option) any
 * later version.
 * 
 * VIPSLogic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * NIBIO Open Source License for more details.
 * 
 * You should have received a copy of the NIBIO Open Source License
 * along with VIPSLogic.  If not, see <http://www.nibio.no/licenses/>.
 * 
 */
/*
 * This validator has its Java counterpart in no.nibio.web.forms.FormValidator.java 
 * (for server side validation). They use the same JSON file for form validation. 
 * Changes in logic client side should be reflected server side
 */

// Constant defs for field types. Also found in no.nibio.web.forms.FormField
var dataTypes = {
    TYPE_STRING: "STRING",
    TYPE_INTEGER: "INTEGER",
    TYPE_DOUBLE: "DOUBLE",
    TYPE_DATE: "DATE",
    TYPE_TIMESTAMP: "TIMESTAMP",
    TYPE_PASSWORD: "PASSWORD",
    TYPE_EMAIL: "EMAIL",
    TYPE_POINT_WGS84: "POINT_WGS84"
    
    
};

var fieldTypes= {
    TYPE_SELECT_SINGLE: "SELECT_SINGLE",
    TYPE_SELECT_MULTIPLE: "SELECT_MULTIPLE",
    TYPE_INPUT: "INPUT"
};


var defaultValues = {
    DEFAULT_DATE_FORMAT: "YYYY-MM-DD"
};

// Primary field relates to secondary field as TYPE
var relationTypes = {
    RELATION_TYPE_EQUALS: "EQUALS",
    RELATION_TYPE_AFTER: "AFTER"
};

// Storage for all form definitions for current page
var formDefinitions = {};

// The regular expression for email strings
// Checks that string contains at least one ".", an "@" symbol and an 
// appropriate top-level domain suffix (e.g. .com, .net, etc)
// Stolen from this web page: http://www.designchemical.com/blog/index.php/jquery/email-validation-using-jquery/
var emailReg = /^([\w-\.]+@([\w-]+\.)+[\w-]{2,4})?$/;

/**
 * Validation function for all forms
 * @copyright 2013 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 * 
 * It depends on the following:
 * <ul>
 *  <li>theForm - the form to validate, passed in to the function by reference</li>
 *  <li>The form must have an id that corresponds to a JSON file accessible in "/formdefinitions/[form_id].json"</li>
 *  <li>A DOM element with id "errorMsgEl" for where to put validation error messages</li>
 *  <li>jQuery for Ajax calls</li>
 *  <li><a href="http://momentjs.com/">moment.js</a> for date validation</li>
 * </ul>
 * 
 * This function prepares for the validateFormActual function.
 * 
 * @param {Element} theForm the form to validate
 * @param {String} formDefinitionKey
 * @return {boolean} If the form is valid or not
 */
function validateForm(theForm, formDefinitionKey)
{
    var isValid = true;
    if(formDefinitionKey === null || formDefinitionKey === undefined)
    {
        formDefinitionKey = theForm.id;
    }
    var formDefinition = formDefinitions[formDefinitionKey];
    // Iterate through fields in form definition
    for(var i in formDefinition.fields){
        var fieldDefinition = formDefinition.fields[i];
        if(!validateFieldActual(theForm[fieldDefinition.name], theForm, formDefinitionKey)){
            //alert("Validation failed for " + fieldDefinition.name);
            isValid = false;
        }
    }
    
    // Check relations
    if(formDefinition.relations !== undefined && formDefinition.relations !== null)
    {
        for(var i in formDefinition.relations)
        {
            var item = formDefinition.relations[i];
            var primaryFieldWebValue = theForm[item.primaryField].value;
            var secondaryFieldWebValue = theForm[item.secondaryField].value;
            if(primaryFieldWebValue === null || secondaryFieldWebValue === null)
            {
                continue;
            }

            // String equality NOW!
            //console.log("relationTYpe=" + item.relationType);
            if(item.relationType === relationTypes.RELATION_TYPE_EQUALS)
            {
                if(primaryFieldWebValue !== secondaryFieldWebValue)
                {
                    isValid = false;
                    invalidizeField(theForm[item.primaryField], theForm, getI18nMsg("xIsNotEqualToY",[
                        getI18nMsg(item.primaryField),
                        getI18nMsg(item.secondaryField).toLowerCase()
                    ]));
                }
            }
            // Primary field is expected to be a date AFTER secondary key
            else if(item.relationType === relationTypes.RELATION_TYPE_AFTER)
            {
                if(!moment(primaryFieldWebValue).isAfter(moment(secondaryFieldWebValue)))
                {
                    isValid = false;
                    invalidizeField(theForm[item.primaryField], theForm, getI18nMsg("xIsNotAfterY",[
                        getI18nMsg(item.primaryField),
                        getI18nMsg(item.secondaryField).toLowerCase()
                    ]));
                }
            }
            //console.log(item.primaryField);
        }
    }

    //console.log("isValid=" + isValid);
    return isValid;
}

/**
 * Attempts to load and index the requested form definition
 * @param {String} formDefinitionKey
 * @param {String} path if set, the form is placed outside the standard path
 * @returns {void}
 */
function loadFormDefinition(formDefinitionKey, path)
{
    if(path === null || path === undefined)
    {
        path = "/formdefinitions/";
    }
    
    $.getJSON( path + formDefinitionKey + ".json")
        .done(function(json){
            formDefinitions[formDefinitionKey] = json;
        })
        .fail(function() {
            alert("Error getting form definition with key=" + formDefinitionKey);
        });
}

/**
 * Prepares for the validation of a field
 * @param {DOMElement} fieldEl
 * @param {String} formDefinitionKey
 * @returns {void}
 */
function validateField(fieldEl, formDefinitionKey)
{
    // Which form does this element belong to?
    var theForm = getFormForField(fieldEl);
    if(formDefinitionKey === null || formDefinitionKey === undefined)
    {    
        formDefinitionKey = theForm.id;
    }
    // If form definition is null, get it before we proceed
    if(formDefinitions[formDefinitionKey] === undefined)
    {
        $.getJSON( "/formdefinitions/" + theForm.id + ".json")
        .done(function(json){
            formDefinitions[theForm.id] = json;
            return validateFieldActual(fieldEl, theForm, formDefinitionKey);
        });
    }
    else
    {
        return validateFieldActual(fieldEl, theForm, formDefinitionKey);
    }
}

/**
 * Returns the requested field definition in a given form definition
 * @param {type} fieldName
 * @param {type} formDefinition
 * @returns {unresolved}
 */
function getFieldDefinition(fieldName, formDefinition)
{
    for(i in formDefinition.fields)
    {
        if(formDefinition.fields[i].name === fieldName)
        {
            return formDefinition.fields[i];
        }
    }
    return null;
}

/**
 * Uniform way of getting the element where to put the evaluation message
 * @param {Element} fieldEl
 * @param {Element} theForm
 * @returns {Element}
 */
function getValidationOutputEl(fieldEl, theForm)
{
    return document.getElementById(theForm.id + "_" + fieldEl.name + "_validation");
}

/**
 * Performs the actual field validation
 * @param {Element} fieldEl the form element to be validated
 * @param {Element} theForm the form
 * @param {String} formDefinitionKey optional key to a formdefinition for a part of the form that has been dynamically added to the main form
 * @returns {boolean}
 */
function validateFieldActual(fieldEl, theForm, formDefinitionKey)
{
    
    var webValue = fieldEl.value;
    var fieldDefinition = getFieldDefinition(fieldEl.name, formDefinitions[formDefinitionKey !== null ? formDefinitionKey : theForm.id]);
    // Empty?
    if(webValue === null || webValue === "")
    {
        if(fieldDefinition.required === true)
        {   
            invalidizeField(fieldEl, theForm, getI18nMsg("fieldIsRequired",null));
            return false;
        }
        else
        {
            validizeField(fieldEl, theForm);
            return true;
        }
    }
    
    // Single select field - check for nullValue
    if(fieldDefinition.fieldType === fieldTypes.TYPE_SELECT_SINGLE)
    {
        webValue = fieldEl.options[fieldEl.selectedIndex].value;
        if(fieldDefinition.nullValue === webValue && fieldDefinition.required === true)
        {
            invalidizeField(fieldEl, theForm, getI18nMsg("fieldIsRequired",null));
            return false;
        }
        else
        {
            validizeField(fieldEl, theForm);
            // We don't return yet, because we want to validate the data type
        }
    }
    
    // Multiple select field
    // If required - check that at least one item has been selected
    if(fieldDefinition.fieldType === fieldTypes.TYPE_SELECT_MULTIPLE)
    {
        if(fieldDefinition.required)
        {
            var selectedOptions = 0;
            for(var i=0;i<fieldEl.options.length;i++)
            {
                selectedOptions += fieldEl.options[i].selected ? 1 : 0;
            }
            if(selectedOptions === 0)
            {
                invalidizeField(fieldEl, theForm, getI18nMsg("fieldIsRequired",null));
            return false;
            }
            else
            {
                validizeField(fieldEl, theForm);
                return true;
            }
        }
        else
        {
            return true;
        }
    }

    // STRINGS
    // Strings are checked for length
    if(fieldDefinition.dataType === dataTypes.TYPE_STRING)
    {
        if(fieldDefinition.maxLength !== null && fieldDefinition.maxLength < webValue.length)
        {
            invalidizeField(fieldEl, theForm, getI18nMsg("exceedsMaxLengthOf",[fieldDefinition.maxLength]));
            return false;
        }
        else
        {
            validizeField(fieldEl, theForm);
            return true;
        }
    }
    
    // Dates: check date format
    if(fieldDefinition.dataType === dataTypes.TYPE_DATE)
    {
        var validFormat = (fieldDefinition.dateFormat !== null && fieldDefinition.dateFormat !== undefined) ? fieldDefinition.dateFormat : defaultValues.DEFAULT_DATE_FORMAT;
        if(!moment(webValue, validFormat.toUpperCase(),true).isValid())
        {
            invalidizeField(fieldEl, theForm, getI18nMsg("doesNotMatchFormatX",[validFormat]));
            return false;
        }
        else
        {
            validizeField(fieldEl, theForm);
            return true;
        }
    }
    
    if(fieldDefinition.dataType === dataTypes.TYPE_TIMESTAMP)
    {
        var validDateFormat = (fieldDefinition.dateFormat !== null && fieldDefinition.dateFormat !== undefined) ? 
                                fieldDefinition.dateFormat : defaultValues.DEFAULT_DATE_FORMAT;
        var validTimestampFormat = (fieldDefinition.timestampFormatMomentJS !== null && fieldDefinition.timestampFormatMomentJS !== undefined) ?
                                fieldDefinition.timestampFormatMomentJS: validDateFormat.toUpperCase() + "THH:mm:ssZZ";
        if(!moment(webValue, validTimestampFormat, true).isValid())
        {
            invalidizeField(fieldEl, theForm, getI18nMsg("doesNotMatchFormatX",[validTimestampFormat]));
            return false;
        }
        else
        {
            validizeField(fieldEl, theForm);
            return true;
        }
    }

    // Check email format
    // We use a simple regExp for this
    // TODO: This may acutally call a REST service for uniform checking with Java code
    if(fieldDefinition.dataType === dataTypes.TYPE_EMAIL)
    {
        if(!emailReg.test(webValue))
        {
            invalidizeField(fieldEl, theForm, getI18nMsg("invalidFormat",null));
            return false;
        }
        else
        {
            validizeField(fieldEl, theForm);
            return true;
        }
    }

    // NUMBERS
    // TODO: INTEGERS should not have decimal values
    if(fieldDefinition.dataType === dataTypes.TYPE_INTEGER || fieldDefinition.dataType === dataTypes.TYPE_DOUBLE)
    {
        if(!$.isNumeric(webValue.replace(",",".")))
        {
            invalidizeField(fieldEl, theForm, getI18nMsg("numberRequired",null));
            return false;
        }
        else if(fieldDefinition.minValue !== null && webValue < fieldDefinition.minValue)
        {
            invalidizeField(fieldEl, theForm, getI18nMsg("lowerThanMinimum",[fieldDefinition.minValue]));
            return false;
        }
        else if(fieldDefinition.maxValue !== null && webValue > fieldDefinition.maxValue)
        {
            invalidizeField(fieldEl, theForm, getI18nMsg("higherThanMaximum",[fieldDefinition.maxValue]));
            return false;
        }
        else
        {
            validizeField(fieldEl, theForm);
            return true;
        }
    }
    
    // GIS
    // Point in WGS84 format
    // Format [Double], [Double]
    // TODO Check for minimum and maximum values
    if(fieldDefinition.dataType === dataTypes.TYPE_POINT_WGS84)
    {
        var possibleDoubles = webValue.split(",");
        if(possibleDoubles.length < 2)
        {
            invalidizeField(fieldEl, theForm, getI18nMsg("missingSeparatorComma"));
            return false;
        }
        else if(possibleDoubles.length > 2)
        {
            invalidizeField(fieldEl, theForm, getI18nMsg("tooManySeparatorCommas"));
            return false;
        }
        else
        {
            for(index in possibleDoubles)
            {
                if(!$.isNumeric(possibleDoubles[index]))
                {
                    invalidizeField(fieldEl, theForm, getI18nMsg("invalidFormat"));
                    return false;
                }
            }
        }
        validizeField(fieldEl, theForm);
        return true;
    }
    
    // Passwords are evaluated in evaluatePassword
    if(fieldDefinition.dataType === dataTypes.TYPE_PASSWORD)
    {
        return true;
    }
    
    alert("ERROR: Field " + fieldEl.name + " is of type " + fieldDefinition.type + ", and evaluation of that type is not implemented.");
    return false;
}

/**
 * Recursive function to travers upwards in tree until we find the form
 * for the given element
 * @param {DOMElement} fieldEl
 * @returns {DOMelement} the form
 */
function getFormForField(fieldEl)
{
    // Stop condition
    if(fieldEl.tagName.toLowerCase() === "form")
    {
        return fieldEl;
    }
    else
    {
        if(fieldEl.parentNode === null || fieldEl.parentNode === undefined)
        {
            alert("Could not find a form!");
            return null;
        }
        return getFormForField(fieldEl.parentNode);
    }
}

/**
 * Sends a request to server and gets password evaluation. 
 * If not OK, highlights field and displays error (somehow)
 * @param {DOMElement} passwordEl
 */
function evaluatePassword(passwordEl)
{
    // Get ID for evaluation output
    var validationOutputEl = getValidationOutputEl(passwordEl, getFormForField(passwordEl));
    var passwordAttempt = passwordEl.value;
    // If empty, we return immediately
    if(passwordAttempt === null || passwordAttempt === "")
    {
        validationOutputEl.innerHTML = getI18nMsg("fieldIsRequired",null);
        styleInvalid(passwordEl);
        return;
    }
    
    $.ajax({
        type:"GET",
        url: "/rest/evaluatepassword/" + passwordAttempt,
        statusCode:{
            200: function(data,textStatus, jqXHR){
                if(data === "true")
                {
                    validationOutputEl.innerHTML="";
                    styleValid(passwordEl);
                }
                else
                {
                    validationOutputEl.innerHTML=data.replace(/\n/g, "<br/>");
                    styleInvalid(passwordEl);
                }
            },
            404: function(jqXHR,textStatus,errorThrown){
                alert ("Error: " + jqXHR.responseText);
            },
            500: function(jqXHR,textStatus,errorThrown){
                alert ("Error: " + jqXHR.responseText);
            }
        }
    });
}

/**
 * Marks a field as invalid. Adds validation error message
 * @param {Element} inputEl
 * @param {Element} theForm
 * @param {String} message the validation error message
 * @returns {void}
 */
function invalidizeField(inputEl, theForm, message)
{
    // Make sure we don't try to manipulate hidden fields
    if(inputEl.type === "hidden")
    {
        return;
    }
    styleInvalid(theForm[inputEl.name]);
    getValidationOutputEl(inputEl, theForm).innerHTML = message;
}

/**
 * 
 * @param {type} inputEl
 * @param {type} theForm
 * @returns {undefined}
 */
function validizeField(inputEl, theForm)
{
    // Make sure we don't try to manipulate hidden fields
    if(inputEl.type === "hidden")
    {
        return;
    }
    styleValid(theForm[inputEl.name]);
    getValidationOutputEl(inputEl, theForm).innerHTML = "";
}

/**
 * 
 * @param {type} inputEl
 * @returns {undefined}
 */
function styleInvalid(inputEl)
{
    $(inputEl.parentNode).addClass("has-error");
}

function styleValid(inputEl)
{
    $(inputEl.parentNode).removeClass("has-error");
}

/**
 * Gets a message by key. The message should be translated
 * Depends on /js/resourcebundle.js for the dictionary i18Texts
 * @param {String} msgKey
 * @param {Array} positionalArguments - for string substitution in message
 * @returns {String} the message
 */
function getI18nMsg(msgKey,positionalArguments)
{
    return replaceParams(i18Texts[msgKey], positionalArguments);
}

/**
 * Mimic of the MessageFormat.format method in Java.
 * Very nicely explained here: http://stackoverflow.com/questions/1353408/messageformat-in-javascript-parameters-in-localized-ui-strings
 * @param {String} string
 * @param {Array} replacements
 * @returns {String}
 */
function replaceParams(string, replacements) {
    return string.replace(/\{(\d+)\}/g, function() {
        return replacements[arguments[1]];
    });
}