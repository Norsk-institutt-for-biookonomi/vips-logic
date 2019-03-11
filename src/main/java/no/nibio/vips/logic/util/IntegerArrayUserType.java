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

import java.io.Serializable;
import java.sql.Array;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

/**
 * Adapted from this: http://stackoverflow.com/questions/21940642/hibernate-postgres-array-type
 * @copyright 2015-2019 <a href="http://www.nibio.no/">NIBIO</a>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 */

public class IntegerArrayUserType implements UserType {

    /**
     * Return the SQL type codes for the columns mapped by this type. The
     * codes are defined on <tt>java.sql.Types</tt>.
     *
     * @return int[] the typecodes
     * @see java.sql.Types
     */
    @Override
    public int[] sqlTypes() {
        return new int[] { Types.ARRAY };
    }

    /**
     * The class returned by <tt>nullSafeGet()</tt>.
     *
     * @return Class
     */
    @Override
    public Class returnedClass() {
        return Integer[].class;
    }

    /**
     * Compare two instances of the class mapped by this type for persistence "equality".
     * Equality of the persistent state.
     *
     * @param x
     * @param y
     * @return boolean
     */
    @Override
    public boolean equals(Object x, Object y) throws HibernateException {

        if( x== null){

            return y== null;
        }

        return x.equals( y);
    }

    /**
     * Get a hashcode for the instance, consistent with persistence "equality"
     */
    @Override
    public int hashCode(Object x) throws HibernateException {

        return x.hashCode();
    }

    /**
     * Retrieve an instance of the mapped class from a JDBC resultset. Implementors
     * should handle possibility of null values.
     *
     * @param rs      a JDBC result set
     * @param names   the column names
     * @param session
     * @param owner   the containing entity  @return Object
     * @throws org.hibernate.HibernateException
     *
     * @throws java.sql.SQLException
     */
    //@Override
    public Object nullSafeGet(
            ResultSet rs, 
            String[] names, 
            SessionImplementor session, 
            Object owner) throws HibernateException, SQLException {
       if (rs.wasNull()) {
        return null;
    }

       try
       {
        Integer[] array = (Integer[]) rs.getArray(names[0]).getArray();
        return array;
       }
       catch(NullPointerException ex)
       {
           return new Integer[0];
       }
    }

    /**
     * Write an instance of the mapped class to a prepared statement. Implementors
     * should handle possibility of null values. A multi-column type should be written
     * to parameters starting from <tt>index</tt>.
     *
     * @param st      a JDBC prepared statement
     * @param value   the object to write
     * @param index   statement parameter index
     * @param session
     * @throws org.hibernate.HibernateException
     *
     * @throws java.sql.SQLException
     */
    //@Override
    public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }

        Integer[] castObject = (Integer[]) value;
        Array array = session.connection().createArrayOf("integer", castObject); // The postgres array data type
        st.setArray(index, array);
    }

    /**
     * Return a deep copy of the persistent state, stopping at entities and at
     * collections. It is not necessary to copy immutable objects, or null
     * values, in which case it is safe to simply return the argument.
     *
     * @param value the object to be cloned, which may be null
     * @return Object a copy
     */
    @Override
    public Object deepCopy(Object value) throws HibernateException {

        return value;
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

    /**
     * Transform the object into its cacheable representation. At the very least this
     * method should perform a deep copy if the type is mutable. That may not be enough
     * for some implementations, however; for example, associations must be cached as
     * identifier values. (optional operation)
     *
     * @param value the object to be cached
     * @return a cachable representation of the object
     * @throws org.hibernate.HibernateException
     *
     */
    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Integer[])this.deepCopy( value);
    }

    /**
     * Reconstruct an object from the cacheable representation. At the very least this
     * method should perform a deep copy if the type is mutable. (optional operation)
     *
     * @param cached the object to be cached
     * @param owner  the owner of the cached object
     * @return a reconstructed object from the cachable representation
     * @throws org.hibernate.HibernateException
     *
     */
    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return this.deepCopy( cached);
    }

    /**
     * During merge, replace the existing (target) value in the entity we are merging to
     * with a new (original) value from the detached entity we are merging. For immutable
     * objects, or null values, it is safe to simply return the first parameter. For
     * mutable objects, it is safe to return a copy of the first parameter. For objects
     * with component values, it might make sense to recursively replace component values.
     *
     * @param original the value from the detached entity being merged
     * @param target   the value in the managed entity
     * @return the value to be merged
     */
    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }

    /**
     * Retrieve an instance of the mapped class from a JDBC resultset. Implementors
     * should handle possibility of null values.
     *
     * @param rs      a JDBC result set
     * @param strings   the column names
     * @param ssci
     * @throws org.hibernate.HibernateException
     *
     * @throws java.sql.SQLException
     */
    @Override
    public Object nullSafeGet(ResultSet rs, String[] strings, SharedSessionContractImplementor ssci, Object o) throws HibernateException, SQLException {
        if (rs.wasNull()) {
            return null;
        }
        try
        {
            Integer[] array = (Integer[]) rs.getArray(strings[0]).getArray();
            return array;
        }
        catch(NullPointerException ex)
        {
           return new Integer[0];
        }
    }

    /**
     * Write an instance of the mapped class to a prepared statement. Implementors
     * should handle possibility of null values. A multi-column type should be written
     * to parameters starting from <tt>index</tt>.
     *
     * @param ps      a JDBC prepared statement
     * @param o   the object to write
     * @param i   statement parameter index
     * @param ssci
     * @throws org.hibernate.HibernateException
     *
     * @throws java.sql.SQLException
     */
    @Override
    public void nullSafeSet(PreparedStatement ps, Object o, int i, SharedSessionContractImplementor ssci) throws HibernateException, SQLException {
        if (o == null) {
            ps.setNull(i, Types.OTHER);
            return;
        }

        Integer[] castObject = (Integer[]) o;
        Array array = ssci.connection().createArrayOf("integer", castObject); // The postgres array data type
        ps.setArray(i, array);
    }


}
