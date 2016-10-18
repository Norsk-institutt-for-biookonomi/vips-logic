/* 
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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
}

/**
 * Depends on the value of currentLanguage and defaultLanguage in /currentLanguage.js
 * @param organism
 * @returns {String}
 */
function getLocalizedOrganismName(organism)
{
	// Fallback in case nothing works
	if(organism === null)
	{
		return gettext("Unnamed");
	}
	// Attempting the following languages (in order): current language, default language, English
	var languages = [environment.currentLanguage, environment.defaultLanguage, "en"];
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

