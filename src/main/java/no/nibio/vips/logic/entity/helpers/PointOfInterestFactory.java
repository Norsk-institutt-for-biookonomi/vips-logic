/*
 * Copyright (c) 2020 NIBIO <http://www.nibio.no/>. 
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
            default:
                return new PointOfInterest();
        }
    }
}
