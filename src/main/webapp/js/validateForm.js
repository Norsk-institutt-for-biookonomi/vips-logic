/*
 * This validator has its Java counterpart in no.bioforsk.web.forms.FormValidator.java 
 * (for server side validation). They use the same JSON file for form validation. 
 * Changes in logic client side should be reflected server side
 */

// Constant defs for field types. Also found in no.bioforsk.web.forms.FormField
var formFieldTypes = {
    TYPE_STRING: "STRING",
    TYPE_INTEGER: "INTEGER",
    TYPE_DOUBLE: "DOUBLE",
    TYPE_PASSWORD: "PASSWORD",
    TYPE_EMAIL: "EMAIL",
    TYPE_SELECT_SINGLE: "SELECT_SINGLE"
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
 * @copyright 2013 <a href="http://www.bioforsk.no/">Bioforsk</a>
 * @author Tor-Einar Skog <tor-einar.skog@bioforsk.no>
 * 
 * It depends on the following:
 * <ul>
 *  <li>theForm - the form to validate, passed in to the function by reference</li>
 *  <li>The form must have an id that corresponds to a JSON file accessible in "/formdefinitions/[form_id].json"</li>
 *  <li>A DOM element with id "errorMsgEl" for where to put validation error messages</li>
 *  <li>jQuery for Ajax calls</li>
 * </ul>
 * 
 * This function prepares for the validateFormActual function.
 * 
 * @param {DOMElement} theForm the form to validate
 * @return {boolean} If the form is valid or not
 */
function validateForm(theForm)
{
    if(formDefinitions[theForm.id] === undefined)
    {
        $.getJSON( "/formdefinitions/" + theForm.id + ".json")
        .done(function(json){
            formDefinitions[theForm.id] = json;
            return validateFormActual(theForm);
        });
        //return false;
    }
    else
    {
        return validateFormActual(theForm);
    }
}

function validateFormActual(theForm)
{
    var isValid = true;
    var formDefinition = formDefinitions[theForm.id];

    // Iterate through fields in form definition
    for(var i in formDefinition.fields){
        var fieldDefinition = formDefinition.fields[i];
        if(!validateFieldActual(theForm[fieldDefinition.name], theForm)){
            //console.log("Validation failed for " + fieldDefinition.name);
            isValid = false;
        }
    }
    
    // Check repeats
    for(var i in formDefinition.repeatFields)
    {
        var item = formDefinition.repeatFields[i];
        var primaryFieldWebValue = theForm[item.primaryField].value;
        var repeatFieldWebValue = theForm[item.repeatField].value;
        if(primaryFieldWebValue === null || repeatFieldWebValue === null)
        {
            continue;
        }
        if(primaryFieldWebValue !== repeatFieldWebValue)
        {
            isValid = false;
            invalidizeField(theForm[item.repeatField], theForm, getI18nMsg("xIsNotEqualToY",[
                getI18nMsg(item.repeatField),
                getI18nMsg(item.primaryField).toLowerCase()
            ]));
        }
        //console.log(item.primaryField);
    }

    //console.log("isValid=" + isValid);
    return isValid;
}


/**
 * Prepares for the validation of a field
 * @param {DOMElement} fieldEl
 * @returns {void}
 */
function validateField(fieldEl)
{
    // Which form does this element belong to?
    var theForm = getFormForField(fieldEl);
    // If form definition is null, get it before we proceed
    if(formDefinitions[theForm.id] === undefined)
    {
        $.getJSON( "/formdefinitions/" + theForm.id + ".json")
        .done(function(json){
            formDefinitions[theForm.id] = json;
            validateFieldActual(fieldEl, theForm);
        });
    }
    else
    {
        validateFieldActual(fieldEl, theForm);
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
 * @returns {void}
 */
function validateFieldActual(fieldEl, theForm)
{
    var webValue = fieldEl.value;
    var fieldDefinition = getFieldDefinition(fieldEl.name, formDefinitions[theForm.id]);
        
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

    // STRINGS
    // Strings are checked for length
    if(fieldDefinition.type === formFieldTypes.TYPE_STRING)
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

    // Check email format
    // We use a simple regExp for this
    // TODO: This may acutally call a REST service for uniform checking with Java code
    if(fieldDefinition.type === formFieldTypes.TYPE_EMAIL)
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
    if(fieldDefinition.type === formFieldTypes.TYPE_INTEGER || fieldDefinition.type === formFieldTypes.TYPE_DOUBLE)
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
    
    // Single select field - check for nullValue
    if(fieldDefinition.type === formFieldTypes.TYPE_SELECT_SINGLE)
    {
        webValue = fieldEl.options[fieldEl.selectedIndex].value;
        if(fieldDefinition.nullValue === webValue)
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
    
    // Passwords are evaluated in evaluatePassword
    if(fieldDefinition.type === formFieldTypes.TYPE_PASSWORD)
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