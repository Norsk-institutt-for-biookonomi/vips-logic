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

function setFieldValue(theForm, fieldName, value) {
	theForm[fieldName] = value;
}

/* An easy way to sort list options alphabetically
 * 
 * @param {type} a
 * @param {type} b
 * @returns {Number}
 */
var compareSelectListOptions = function (a, b) {
	if (a.text < b.text) {
		return -1;
	}
	if (a.text > b.text) {
		return 1;
	}
	return 0;
};

var sortListAlphabetically = function (theList, keepFirst) {
	keepFirst = keepFirst | 0;
	var allOptions = [];
	for (var i = keepFirst; i < theList.options.length; i++) {
		allOptions.push(theList.options[i]);
	}
	allOptions.sort(function (a, b) {
		if (a.label < b.label) {
			return -1;
		}
		if (a.label > b.label) {
			return 1;
		}
		return 0;
	});
	theList.options.length = keepFirst; // Keeping the top items?
	for (var i = 0; i < allOptions.length; i++) {
		theList.options[theList.options.length] = allOptions[i];
	}
};

/**
 * Depends on the value of currentLanguage and defaultLanguage in /currentLanguage.js
 * @param organism
 * @param language if you want to override system settings, specify language here
 * @returns {String}
 */
function getLocalizedOrganismName(organism, language) {
	var preferredLanguage = language || environment.currentLanguage;
	// Fallback in case nothing works
	if (organism === null) {
		return gettext("Unnamed");
	}
	// Attempting the following languages (in order): current language, default language, English
	var languages = [preferredLanguage, environment.defaultLanguage, "en"];
	for (var j in languages) {
		for (var i in organism.organismLocaleSet) {
			var localeSet = organism.organismLocaleSet[i];
			//console.log(localeSet);
			if (localeSet.organismLocalePK.locale == languages[j]) {
				return localeSet.localName;
			}
		}
	}
	// Then we try the latin name
	if (organism.latinName !== null
		&& organism.latinName !== "") {
		return organism.latinName;
	}
	// Then the trade name
	if (organism.tradeName !== null
		&& organism.tradeName !== "") {
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
function getLocalizedCropCategoryName(cropCategory) {
	// Fallback in case nothing works
	if (cropCategory === null) {
		return "Unnamed";
	}
	// Attempting the following languages (in order): current language, default language, English
	var languages = [environment.currentLanguage, environment.defaultLanguage, "en"];
	for (var j in languages) {
		for (var i in cropCategory.cropCategoryLocalSet) {
			var localeSet = cropCategory.cropCategoryLocalSet[i];
			if (localeSet.cropCategoryLocalPK.locale.trim() == languages[j].trim()) {
				return localeSet.localName;
			}
		}
	}
	// Then we try the latin name
	if (cropCategory.defaultName !== null
		&& cropCategory.defaultName !== "") {
		return cropCategory.defaultName;
	}
	// Then we give up
	return "Unnamed";
}

function getLocalizedOptionsHTML(optionsList) {
	var translatedOptionsHTML = "";
	var languages = [environment.currentLanguage, environment.defaultLanguage, "en"];

	for (var i in optionsList) {
		var option = optionsList[i];
		var label = null;
		var labelList = option.label;
		for (var j in languages) {
			for (var k in labelList) {
				if (k === languages[j]) {
					label = labelList[k];
					break;
				}
			}
			if (label !== null) {
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
function getUnixTimestampFromJSON(ambiguousValue) {
	var possibleDateObject;
	if (!isMomentJSAvailable()) {
		possibleDateObject = new Date(ambiguousValue);
		console.info("Warning: Parsing date without MomentJS. Can't guarantee correct result.");
	}
	else {
		possibleDateObject = moment(ambiguousValue).toDate();
	}
	if (possibleDateObject.getTime() === NaN && typeof parseInt(ambiguousValue) === "number") {
		return parseInt(ambiguousValue);
	}
	else {
		return possibleDateObject.getTime();
	}
}

/**
 * Does what it says
 * 
 * @return {Boolean}
 */
function isMomentJSAvailable() {
	try {
		moment();
		return true;
	}
	catch (err) {
		return false;
	}

}

/** Converts numeric degrees to radians 
 * Essential to make the calculateDistanceBetweenCoordinates to work   
 **/
if (typeof Number.prototype.toRad == 'undefined') {
	Number.prototype.toRad = function () {
		return this * Math.PI / 180;
	}
}



/**
	* Reference: <a href="http://www.movable-type.co.uk/scripts/latlong.html">this webpage</a>
	* <pre>
	* This uses the "haversine" formula to calculate the great-circle distance between two points ? that is, the shortest distance over the earth?s surface ? giving an ?as-the-crow-flies? distance between the points (ignoring any hills, of course!).
	Haversine formula:
			a = sin²(?lat/2) + cos(lat1).cos(lat2).sin²(?long/2)
			c = 2.atan2(?a, ?(1?a))
			d = R.c
	where R is earth's radius (mean radius = 6,371km);
	Note that angles need to be in radians to pass to trig functions!
	* </pre>
	* @param lat1
	* @param lon1
	* @param lat2
	* @param lon2
	* @return distance in km between to coordinates
	*/
function calculateDistanceBetweenCoordinates(lat1, lon1, lat2, lon2) {
	var R = 6371; // km
	var dLat = (lat2 - lat1).toRad();
	var dLon = (lon2 - lon1).toRad();
	lat1 = lat1.toRad();
	lat2 = lat2.toRad();

	var a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
		Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
	var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	var d = R * c;

	return d;
}
