/*
 * Copyright (c) 2015 NIBIO <http://www.nibio.no/>. 
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

package no.nibio.vips.logic.util;

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

/**
 * Adapted from this: http://stackoverflow.com/questions/21940642/hibernate-postgres-array-type
 * @copyright 2015-2019 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */

public class IntegerArrayUserType implements UserType<Integer[]> {

    public IntegerArrayUserType()
    {
        super();
    }
    /**
     * The class returned by <tt>nullSafeGet()</tt>.
     *
     * @return Class
     */
    @Override
    public Class<Integer[]> returnedClass() {
        return Integer[].class;
    }


    /**
     * Are objects of this type mutable?
     *
     * @return boolean
     */
    @Override
    public boolean isMutable() {
        return true;
    }

    
    @Override
    public int getSqlType() {
        return Types.ARRAY;
    }

    @Override
    public boolean equals(Integer[] x, Integer[] y) {
        if( x== null){

            return y== null;
        }

        return x.equals( y);
    }

    @Override
    public int hashCode(Integer[] j) {
        return Arrays.hashCode(j);
    }

    @Override
    public void nullSafeSet(PreparedStatement ps, Integer[] j, int i, SharedSessionContractImplementor ssci) throws SQLException {
         if (j == null) {
            ps.setNull(i, Types.OTHER);
            return;
        }

        Integer[] castObject = (Integer[]) j;
        Array array = ssci.getJdbcConnectionAccess().obtainConnection().createArrayOf("integer", castObject); // The postgres array data type
        ps.setArray(i, array);
    }

    @Override
    public Integer[] deepCopy(Integer[] j) {
        return j;
    }

    @Override
    public Serializable disassemble(Integer[] j) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Integer[] nullSafeGet(ResultSet rs, int i, SharedSessionContractImplementor ssci, Object o) throws SQLException {
        if (rs.wasNull()) {
            return null;
        }
        try
        {
            Integer[] array = (Integer[]) rs.getArray(i).getArray();
            return array;
        }
        catch(NullPointerException ex)
        {
           return new Integer[0];
        }
    }

    @Override
    public Integer[] assemble(Serializable srlzbl, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

   


}
