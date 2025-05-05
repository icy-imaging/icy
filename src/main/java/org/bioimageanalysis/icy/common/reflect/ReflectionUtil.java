/*
 * Copyright (c) 2010-2024. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */
package org.bioimageanalysis.icy.common.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Reflection tools class.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ReflectionUtil {
    /**
     * @return the Method object corresponding to the specified method name and parameters.
     */
    public static Method getMethod(final Class<?> objectClass, final String methodName, final Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException {
        Class<?> clazz = objectClass;
        Method result = null;

        while ((clazz != null) && (result == null)) {
            try {
                result = clazz.getDeclaredMethod(methodName, parameterTypes);
            }
            catch (final NoSuchMethodException e) {
                // ignore
            }

            clazz = clazz.getSuperclass();
        }

        if (result == null)
            throw new NoSuchMethodException("Method " + methodName + "(..) not found in class " + Objects.requireNonNull(objectClass).getName());

        return result;
    }

    /**
     * Invoke the method of <code>object</code> corresponding to the specified name and with
     * specified parameters values.
     */
    public static Object invokeMethod(final Object object, final String methodName, final Object... args) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        final Class<?>[] parameterTypes = new Class<?>[args.length];

        // build parameter types
        for (int i = 0; i < args.length; i++)
            parameterTypes[i] = args[i].getClass();

        final Method method = getMethod(object.getClass(), methodName, parameterTypes);

        // invoke method
        return method.invoke(object, args);
    }

    /**
     * @return the Field object corresponding to the specified field name.
     */
    public static Field getField(final Class<?> objectClass, final String fieldName) throws SecurityException, NoSuchFieldException {
        Class<?> clazz = objectClass;
        Field result = null;

        while ((clazz != null) && (result == null)) {
            try {
                result = clazz.getDeclaredField(fieldName);
            }
            catch (final NoSuchFieldException e) {
                // ignore
            }

            clazz = clazz.getSuperclass();
        }

        if (result == null)
            throw new NoSuchFieldException(" Field " + fieldName + " not found in class " + Objects.requireNonNull(objectClass).getName());

        return result;
    }

    /**
     * @return the object instance corresponding to the specified field name.
     */
    public static Object getFieldObject(final Object object, final String fieldName) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        return getField(object.getClass(), fieldName).get(object);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(final Object object, final String fieldName, final Object value) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        getField(object.getClass(), fieldName).set(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(final Object object, final String fieldName, final boolean value) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        getField(object.getClass(), fieldName).setBoolean(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(final Object object, final String fieldName, final byte value) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        getField(object.getClass(), fieldName).setByte(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(final Object object, final String fieldName, final char value) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        getField(object.getClass(), fieldName).setChar(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(final Object object, final String fieldName, final short value) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        getField(object.getClass(), fieldName).setShort(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(final Object object, final String fieldName, final int value) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        getField(object.getClass(), fieldName).setInt(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(final Object object, final String fieldName, final long value) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        getField(object.getClass(), fieldName).setLong(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(final Object object, final String fieldName, final float value) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        getField(object.getClass(), fieldName).setFloat(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(final Object object, final String fieldName, final double value) throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException {
        getField(object.getClass(), fieldName).setDouble(object, value);
    }
}
