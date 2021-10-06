/*
 * This file is part of ClassGraph.
 *
 * Author: Luke Hutchison
 *
 * Hosted at: https://github.com/classgraph/classgraph
 *
 * --
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Luke Hutchison
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package nonapi.io.github.classgraph.reflection;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassGraph.CircumventEncapsulationMethod;

/** Reflection utility methods that can be used by ClassLoaderHandlers. */
public final class ReflectionUtils {
    /** The reflection driver to use. */
    private static ReflectionDriver reflectionDriver;

    static {
        loadReflectionDriver();
    }

    /** Call this if you change the value of {@link ClassGraph#CIRCUMVENT_ENCAPSULATION}. */
    public static void loadReflectionDriver() {
        if (ClassGraph.CIRCUMVENT_ENCAPSULATION == CircumventEncapsulationMethod.NARCISSUS) {
            try {
                reflectionDriver = new NarcissusReflectionDriver();
            } catch (final Throwable t) {
                System.err.println("Could not load Narcissus reflection driver: " + t);
                // Fall back to standard reflection driver
            }
        } else if (ClassGraph.CIRCUMVENT_ENCAPSULATION == CircumventEncapsulationMethod.JVM_DRIVER) {
            try {
                reflectionDriver = new JVMDriverReflectionDriver();
            } catch (final Throwable t) {
                System.err.println("Could not load JVM-Driver reflection driver: " + t);
                // Fall back to standard reflection driver
            }
        }
        if (reflectionDriver == null) {
            reflectionDriver = new StandardReflectionDriver();
        }
    }

    /**
     * Constructor.
     */
    private ReflectionUtils() {
        // Cannot be constructed
    }

    /**
     * Get the value of the named field in the class of the given object or any of its superclasses. If an exception
     * is thrown while trying to read the field, and throwException is true, then IllegalArgumentException is thrown
     * wrapping the cause, otherwise this will return null. If passed a null object, returns null unless
     * throwException is true, then throws IllegalArgumentException.
     * 
     * @param throwException
     *            If true, throw an exception if the field value could not be read.
     * @param obj
     *            The object.
     * @param fieldName
     *            The field name.
     * 
     * @return The field value.
     * @throws IllegalArgumentException
     *             If the field value could not be read.
     */
    public static Object getFieldVal(final boolean throwException, final Object obj, final String fieldName)
            throws IllegalArgumentException {
        if (obj == null || fieldName == null) {
            if (throwException) {
                throw new NullPointerException();
            } else {
                return null;
            }
        }
        try {
            return reflectionDriver.getField(obj, reflectionDriver.findField(obj.getClass(), fieldName));
        } catch (final Throwable e) {
            if (throwException) {
                throw new IllegalArgumentException(
                        "Can't read field " + obj.getClass().getName() + "." + fieldName + ": " + e);
            }
        }
        return null;
    }

    /**
     * Get the value of the named static field in the given class or any of its superclasses. If an exception is
     * thrown while trying to read the field value, and throwException is true, then IllegalArgumentException is
     * thrown wrapping the cause, otherwise this will return null. If passed a null class reference, returns null
     * unless throwException is true, then throws IllegalArgumentException.
     * 
     * @param throwException
     *            If true, throw an exception if the field value could not be read.
     * @param cls
     *            The class.
     * @param fieldName
     *            The field name.
     * 
     * @return The field value.
     * @throws IllegalArgumentException
     *             If the field value could not be read.
     */
    public static Object getStaticFieldVal(final boolean throwException, final Class<?> cls, final String fieldName)
            throws IllegalArgumentException {
        if (cls == null || fieldName == null) {
            if (throwException) {
                throw new NullPointerException();
            } else {
                return null;
            }
        }
        try {
            return reflectionDriver.getStaticField(reflectionDriver.findField(cls, fieldName));
        } catch (final Throwable e) {
            if (throwException) {
                throw new IllegalArgumentException(
                        "Can't read static field " + cls.getName() + "." + fieldName + ": " + e);
            }
        }
        return null;
    }

    /**
     * Invoke the named method in the given object or its superclasses. If an exception is thrown while trying to
     * call the method, and throwException is true, then IllegalArgumentException is thrown wrapping the cause,
     * otherwise this will return null. If passed a null object, returns null unless throwException is true, then
     * throws IllegalArgumentException.
     * 
     * @param throwException
     *            If true, throw an exception if the field value could not be read.
     * @param obj
     *            The object.
     * @param methodName
     *            The method name.
     * 
     * @return The result of the method invocation.
     * @throws IllegalArgumentException
     *             If the method could not be invoked.
     */
    public static Object invokeMethod(final boolean throwException, final Object obj, final String methodName)
            throws IllegalArgumentException {
        if (obj == null || methodName == null) {
            if (throwException) {
                throw new IllegalArgumentException("Unexpected null argument");
            } else {
                return null;
            }
        }
        try {
            return reflectionDriver.invokeMethod(obj, reflectionDriver.findMethod(obj.getClass(), methodName));
        } catch (final Throwable e) {
            if (throwException) {
                throw new IllegalArgumentException("Method \"" + methodName + "\" could not be invoked: " + e);
            }
            return null;
        }
    }

    /**
     * Invoke the named method in the given object or its superclasses. If an exception is thrown while trying to
     * call the method, and throwException is true, then IllegalArgumentException is thrown wrapping the cause,
     * otherwise this will return null. If passed a null object, returns null unless throwException is true, then
     * throws IllegalArgumentException.
     * 
     * @param throwException
     *            Whether to throw an exception on failure.
     * @param obj
     *            The object.
     * @param methodName
     *            The method name.
     * @param argType
     *            The type of the method argument.
     * @param param
     *            The parameter value to use when invoking the method.
     * 
     * @return The result of the method invocation.
     * @throws IllegalArgumentException
     *             If the method could not be invoked.
     */
    public static Object invokeMethod(final boolean throwException, final Object obj, final String methodName,
            final Class<?> argType, final Object param) throws IllegalArgumentException {
        if (obj == null || methodName == null || argType == null) {
            if (throwException) {
                throw new IllegalArgumentException("Unexpected null argument");
            } else {
                return null;
            }
        }
        try {
            return reflectionDriver.invokeMethod(obj,
                    reflectionDriver.findMethod(obj.getClass(), methodName, argType), param);
        } catch (final Throwable e) {
            if (throwException) {
                throw new IllegalArgumentException("Method \"" + methodName + "\" could not be invoked: " + e);
            }
            return null;
        }
    }

    /**
     * Invoke the named static method. If an exception is thrown while trying to call the method, and throwException
     * is true, then IllegalArgumentException is thrown wrapping the cause, otherwise this will return null. If
     * passed a null class reference, returns null unless throwException is true, then throws
     * IllegalArgumentException.
     * 
     * @param throwException
     *            Whether to throw an exception on failure.
     * @param cls
     *            The class.
     * @param methodName
     *            The method name.
     * 
     * @return The result of the method invocation.
     * @throws IllegalArgumentException
     *             If the method could not be invoked.
     */
    public static Object invokeStaticMethod(final boolean throwException, final Class<?> cls,
            final String methodName) throws IllegalArgumentException {
        if (cls == null || methodName == null) {
            if (throwException) {
                throw new IllegalArgumentException("Unexpected null argument");
            } else {
                return null;
            }
        }
        try {
            return reflectionDriver.invokeStaticMethod(reflectionDriver.findMethod(cls, methodName));
        } catch (final Throwable e) {
            if (throwException) {
                throw new IllegalArgumentException(
                        "Static method \"" + methodName + "\" could not be invoked: " + e);
            }
            return null;
        }
    }

    /**
     * Invoke the named static method. If an exception is thrown while trying to call the method, and throwException
     * is true, then IllegalArgumentException is thrown wrapping the cause, otherwise this will return null. If
     * passed a null class reference, returns null unless throwException is true, then throws
     * IllegalArgumentException.
     * 
     * @param throwException
     *            Whether to throw an exception on failure.
     * @param cls
     *            The class.
     * @param methodName
     *            The method name.
     * @param argType
     *            The type of the method argument.
     * @param param
     *            The parameter value to use when invoking the method.
     * 
     * @return The result of the method invocation.
     * @throws IllegalArgumentException
     *             If the method could not be invoked.
     */
    public static Object invokeStaticMethod(final boolean throwException, final Class<?> cls,
            final String methodName, final Class<?> argType, final Object param) throws IllegalArgumentException {
        if (cls == null || methodName == null || argType == null) {
            if (throwException) {
                throw new IllegalArgumentException("Unexpected null argument");
            } else {
                return null;
            }
        }
        try {
            return reflectionDriver.invokeStaticMethod(reflectionDriver.findMethod(cls, methodName, argType),
                    param);
        } catch (final Throwable e) {
            if (throwException) {
                throw new IllegalArgumentException(
                        "Static method \"" + methodName + "\" could not be invoked: " + e);
            }
            return null;
        }
    }

    /**
     * Call Class.forName(className), but return null if any exception is thrown.
     * 
     * @param className
     *            The class name to load.
     * @return The class of the requested name, or null if an exception was thrown while trying to load the class.
     */
    public static Class<?> classForNameOrNull(final String className) {
        try {
            return reflectionDriver.findClass(className);
        } catch (final Throwable e) {
            return null;
        }
    }

}
