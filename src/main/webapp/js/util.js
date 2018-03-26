/* 
 * Copyright (c) 2017 NIBIO <http://www.nibio.no/>. 
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
 * Depends on the value of currentLanguage and defaultLanguage in /currentLanguage.js
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

/** Ensure that we're able to handle both a unix timestamp and an ISO timestamp
 * 
 * @param {type} ambiguousValue
 * @returns {Number}
 */
function getUnixTimestampFromJSON(ambiguousValue)
{
	var possibleDateObject = new Date(ambiguousValue);
	if(possibleDateObject.getTime() === NaN && typeof parseInt(ambiguousValue) === "number")
	{
		return parseInt(ambiguousValue);
	}
	else
	{
		return possibleDateObject.getTime();
	}
}