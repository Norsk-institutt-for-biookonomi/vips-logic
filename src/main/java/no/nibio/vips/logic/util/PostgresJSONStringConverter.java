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

package no.nibio.vips.logic.util;

import java.sql.SQLException;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.postgresql.util.PGobject;

/**
 * NOT IN USE. Does not work with current configuration. Use StringJsonUserType instead
 * Based on this example: http://stackoverflow.com/questions/20339580/are-jpa-eclipselink-custom-types-possible/26126168#26126168
 * @copyright 2015 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */
@Converter
public class PostgresJSONStringConverter implements AttributeConverter<String, PGobject> {

    @Override
    public PGobject convertToDatabaseColumn(String x) {
        //System.out.println("I'm being used, actually!!!! JSON = " + x);
        
        try {
            PGobject po = new PGobject();
            // here we tell Postgres to use JSON as type to treat our json
            po.setType("json");
            // this is Jackson already added as dependency to project, it could be any JSON marshaller
            po.setValue(x);
            return po;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("convertToDatabaseColumn failed");
            return null;
        }
    }

    @Override
    public String convertToEntityAttribute(PGobject y) {
        return y.getValue();
    }

}
