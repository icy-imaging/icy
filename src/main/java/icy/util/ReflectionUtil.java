/*
 * Copyright 2010-2015 Institut Pasteur.
 * 
 * This file is part of Icy.
 * 
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
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
package icy.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Reflection tools class.
 * 
 * @author Stephane
 */
public class ReflectionUtil
{
    /**
     * @return the Method object corresponding to the specified method name and parameters.
     */
    public static Method getMethod(Class<?> objectClass, String methodName, Class<?>... parameterTypes)
            throws SecurityException, NoSuchMethodException
    {
        Class<?> clazz = objectClass;
        Method result = null;

        while ((clazz != null) && (result == null))
        {
            try
            {
                result = clazz.getDeclaredMethod(methodName, parameterTypes);
            }
            catch (NoSuchMethodException e)
            {
                // ignore
            }

            clazz = clazz.getSuperclass();
        }

        if (result == null)
            throw new NoSuchMethodException(
                    "Method " + methodName + "(..) not found in class " + objectClass.getName());

        return result;
    }

    /**
     * @deprecated using <i>forceAccess</i> is not anymore possible with Java17 so we have to stop doing that.<br>
     *             Use {@link #getMethod(Class, String, Class...)} instead.
     */
    @Deprecated
    public static Method getMethod(Class<?> objectClass, String methodName, boolean forceAccess,
            Class<?>... parameterTypes) throws SecurityException, NoSuchMethodException
    {
        Class<?> clazz = objectClass;
        Method result = null;

        while ((clazz != null) && (result == null))
        {
            try
            {
                result = clazz.getDeclaredMethod(methodName, parameterTypes);
            }
            catch (NoSuchMethodException e)
            {
                // ignore
            }

            clazz = clazz.getSuperclass();
        }

        if (result == null)
            throw new NoSuchMethodException(
                    "Method " + methodName + "(..) not found in class " + objectClass.getName());

        if (forceAccess)
            result.setAccessible(true);

        return result;
    }

    /**
     * @deprecated using <i>forceAccess</i> is not anymore possible with Java17 so we have to stop doing that.<br>
     *             Use {@link #getMethod(Class, String, Class...)} instead.
     */
    @Deprecated
    public static Method getMethod(Object object, String methodName, boolean forceAccess, Class<?>... parameterTypes)
            throws SecurityException, NoSuchMethodException
    {
        return getMethod(object.getClass(), methodName, forceAccess, parameterTypes);
    }

    /**
     * Invoke the method of <code>object</code> corresponding to the specified name and with
     * specified parameters values.
     */
    public static Object invokeMethod(Object object, String methodName, Object... args) throws SecurityException,
            NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
    {
        final Class<?>[] parameterTypes = new Class<?>[args.length];

        // build parameter types
        for (int i = 0; i < args.length; i++)
            parameterTypes[i] = args[i].getClass();

        // get method
        final Method method = getMethod(object.getClass(), methodName, parameterTypes);
        // invoke method
        return method.invoke(object, args);
    }

    /**
     * @deprecated using <i>forceAccess</i> is not anymore possible with Java17 so we have to stop doing that.<br>
     *             Use {@link #invokeMethod(Object, String, Object...)} instead
     */
    @Deprecated
    public static Object invokeMethod(Object object, String methodName, boolean forceAccess, Object... args)
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException
    {
        final Class<?>[] parameterTypes = new Class<?>[args.length];

        // build parameter types
        for (int i = 0; i < args.length; i++)
            parameterTypes[i] = args[i].getClass();

        // get method
        final Method method = getMethod(object.getClass(), methodName, forceAccess, parameterTypes);
        // invoke method
        return method.invoke(object, args);
    }

    /**
     * @return the Field object corresponding to the specified field name.
     */
    public static Field getField(Class<?> objectClass, String fieldName) throws SecurityException, NoSuchFieldException
    {
        Class<?> clazz = objectClass;
        Field result = null;

        while ((clazz != null) && (result == null))
        {
            try
            {
                result = clazz.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e)
            {
                // ignore
            }

            clazz = clazz.getSuperclass();
        }

        if (result == null)
            throw new NoSuchFieldException(" Field " + fieldName + " not found in class " + objectClass.getName());

        return result;
    }

    /**
     * @deprecated using <i>forceAccess</i> is not anymore possible with Java17 so we have to stop doing that.<br>
     *             Use {@link #getField(Class, String)} instead
     */
    @Deprecated
    public static Field getField(Class<?> objectClass, String fieldName, boolean forceAccess)
            throws SecurityException, NoSuchFieldException
    {
        Class<?> clazz = objectClass;
        Field result = null;

        while ((clazz != null) && (result == null))
        {
            try
            {
                result = clazz.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e)
            {
                // ignore
            }

            clazz = clazz.getSuperclass();
        }

        if (result == null)
            throw new NoSuchFieldException(" Field " + fieldName + " not found in class " + objectClass.getName());

        if (forceAccess)
            result.setAccessible(true);

        return result;
    }

    /**
     * @deprecated using <i>forceAccess</i> is not anymore possible with Java17 so we have to stop doing that.<br>
     *             Use {@link #getField(Class, String)} instead.
     */
    @Deprecated
    public static Field getField(Object object, String fieldName, boolean forceAccess)
            throws SecurityException, NoSuchFieldException
    {
        return getField(object.getClass(), fieldName, forceAccess);
    }

    /**
     * @return the object instance corresponding to the specified field name.
     */
    public static Object getFieldObject(Object object, String fieldName)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        return getField(object.getClass(), fieldName).get(object);
    }

    /**
     * @deprecated using <i>forceAccess</i> is not anymore possible with Java17 so we have to stop doing that.<br>
     *             Use {@link #getFieldObject(Object, String)} instead.
     */
    @Deprecated
    public static Object getFieldObject(Object object, String fieldName, boolean forceAccess)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        return getField(object.getClass(), fieldName, forceAccess).get(object);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(Object object, String fieldName, Object value)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        getField(object.getClass(), fieldName).set(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(Object object, String fieldName, boolean value)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        getField(object.getClass(), fieldName).setBoolean(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(Object object, String fieldName, byte value)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        getField(object.getClass(), fieldName).setByte(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(Object object, String fieldName, char value)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        getField(object.getClass(), fieldName).setChar(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(Object object, String fieldName, short value)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        getField(object.getClass(), fieldName).setShort(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(Object object, String fieldName, int value)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        getField(object.getClass(), fieldName).setInt(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(Object object, String fieldName, long value)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        getField(object.getClass(), fieldName).setLong(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(Object object, String fieldName, float value)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        getField(object.getClass(), fieldName).setFloat(object, value);
    }

    /**
     * Set the value of the object instance corresponding to the specified field name.
     */
    public static void setFieldValue(Object object, String fieldName, double value)
            throws IllegalArgumentException, IllegalAccessException, SecurityException, NoSuchFieldException
    {
        getField(object.getClass(), fieldName).setDouble(object, value);
    }
}
