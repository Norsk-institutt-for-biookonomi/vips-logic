/* 
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
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

function setFieldValue(theForm, fieldName, value)
{
    theForm[fieldName] = value;
}

/* An easy way to sort list options alphabetically
 * 
 * @param {type} a
 * @param {type} b
 * @returns {Number}
 */
var compareSelectListOptions = function(a,b)
{
	if(a.text < b.text)
	{
		return -1;
	}
	if(a.text > b.text)
	{
		return 1;
	}
	return 0;
};

var sortListAlphabetically = function(theList, keepFirst)
{
    keepFirst = keepFirst | 0;
    var allOptions = [];
    for(var i=keepFirst;i<theList.options.length;i++)
    {
            allOptions.push(theList.options[i]);
    }
    allOptions.sort(function(a,b){
        if(a.label < b.label)
	{
		return -1;
	}
	if(a.label > b.label)
	{
		return 1;
	}
	return 0;
    });
    theList.options.length=keepFirst; // Keeping the top items?
    for(var i=0;i<allOptions.length;i++)
    {
            theList.options[theList.options.length] = allOptions[i];
    }
};

/**
 * Depends on the value of currentLanguage and defaultLanguage in /currentLanguage.js
 * @param organism
 * @param language if you want to override system settings, specify language here
 * @returns {String}
 */
function getLocalizedOrganismName(organism, language)
{
    var preferredLanguage = language || environment.currentLanguage;
    // Fallback in case nothing works
    if(organism === null)
    {
            return gettext("Unnamed");
    }
    // Attempting the following languages (in order): current language, default language, English
    var languages = [preferredLanguage, environment.defaultLanguage, "en"];
    for(var j in languages)
    {
            for(var i in organism.organismLocaleSet)
            {
                    var localeSet = organism.organismLocaleSet[i];
                    //console.log(localeSet);
                    if(localeSet.organismLocalePK.locale == languages[j])
                    {
                            return localeSet.localName;
                    }
            }
    }
    // Then we try the latin name
    if(organism.latinName !== null 
                    && organism.latinName !== "")
    {
            return organism.latinName;
    }
    // Then the trade name
    if(organism.tradeName !== null
                    && organism.tradeName !== "")
    {
            return organism.tradeName;
    }
    // Then we give up
    return gettext("Unnamed");
}

/**
 * Depends on the value of currentLanguage and defaultLanguage in /js/environment.js
 * @param cropCategory
 * @returns {String}
 */
function getLocalizedCropCategoryName(cropCategory)
{
	// Fallback in case nothing works
	if(cropCategory === null)
	{
		return "Unnamed";
	}
	// Attempting the following languages (in order): current language, default language, English
	var languages = [environment.currentLanguage, environment.defaultLanguage, "en"];
	for(var j in languages)
	{
		for(var i in cropCategory.cropCategoryLocalSet)
		{
			var localeSet = cropCategory.cropCategoryLocalSet[i];
			if(localeSet.cropCategoryLocalPK.locale.trim() == languages[j].trim())
			{
				return localeSet.localName;
			}
		}
	}
	// Then we try the latin name
	if(cropCategory.defaultName !== null 
			&& cropCategory.defaultName !== "")
	{
		return cropCategory.defaultName;
	}
	// Then we give up
	return "Unnamed";
}

function getLocalizedOptionsHTML(optionsList) {
    var translatedOptionsHTML = "";
    var languages = [environment.currentLanguage, environment.defaultLanguage, "en"];
    
    for(var i in optionsList){
        var option = optionsList[i];
        var label = null;
        var labelList = option.label;
	for(var j in languages)
	{
		for(var k in labelList)
		{
                    if(k === languages[j])
                    {
                        label = labelList[k];
                        break;
                    }
		}
            if(label !== null)
            {
                break;
            }
	}
        translatedOptionsHTML += '<option value="' + option.value + '"' + (option.selected === "true" ? ' selected="selected"' : '') + ">" + label + "</option>";
    }
    
    return translatedOptionsHTML;
}

/** 
 * Ensure that we're able to handle both a unix timestamp and an ISO timestamp
 * Unfortunately, this method depends on momentjs, since date parsing directly in JavaScript is...hard
 * 
 * @param {type} ambiguousValue
 * @returns {Number}
 */
function getUnixTimestampFromJSON(ambiguousValue)
{
	var possibleDateObject;
	if(!isMomentJSAvailable())
	{
		possibleDateObject = new Date(ambiguousValue);
		console.info("Warning: Parsing date without MomentJS. Can't guarantee correct result.");
	}
	else
	{
		possibleDateObject = moment(ambiguousValue).toDate();
	}
	if(possibleDateObject.getTime() === NaN && typeof parseInt(ambiguousValue) === "number")
	{
		return parseInt(ambiguousValue);
	}
	else
	{
		return possibleDateObject.getTime();
	}
}

/**
 * Does what it says
 * 
 * @return {Boolean}
 */
function isMomentJSAvailable()
{
	try {
		moment();
		return true;
	}
	catch (err)
	{
		return false;
	}

}