/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
 *
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

package no.nibio.vips.logic.entity.helpers;

import no.nibio.vips.logic.entity.*;

/**
 * @copyright 2020 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
public class PointOfInterestFactory {
    public static PointOfInterest getPointOfInterest(Integer pointOfInterestTypeId)
    {
        switch(pointOfInterestTypeId){
            case PointOfInterestType.POINT_OF_INTEREST_TYPE_WEATHER_STATION:
                return new PointOfInterestWeatherStation();
            case PointOfInterestType.POINT_OF_INTEREST_TYPE_FARM:
                return new PointOfInterestTypeFarm();
            case PointOfInterestType.POINT_OF_INTEREST_TYPE_FIELD:
                return new PointOfInterestTypeField();
            case PointOfInterestType.POINT_OF_INTEREST_TYPE_REGION:
                return new PointOfInterestTypeRegion();
            case PointOfInterestType.POINT_OF_INTEREST_TYPE_TRAP:
                return new PointOfInterestTypeTrap();
            case PointOfInterestType.POINT_OF_INTEREST_TYPE_NURSERY:
                return new PointOfInterestTypeNursery();
            default:
                return new PointOfInterest();
        }
    }
}
