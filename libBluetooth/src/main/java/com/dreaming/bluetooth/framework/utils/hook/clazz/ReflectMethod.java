package com.dreaming.bluetooth.framework.utils.hook.clazz;

import com.dreaming.bluetooth.framework.utils.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectMethod<P,T> {
    final P mOwner;
    final Class mClass;
    final Method mMethod;

    public ReflectMethod(String clzName, String methodName, String...methodParams) throws ClassNotFoundException, NoSuchMethodException {
        this(clzName, null, methodName, methodParams);
    }

    public ReflectMethod(Class clz, String methodName, Class...methodParams) throws NoSuchMethodException {
        this(clz, null, methodName, methodParams);
    }

    public ReflectMethod(String clzName, P owner, String methodName, String...methodParams) throws ClassNotFoundException, NoSuchMethodException {
        this(Class.forName(clzName), owner, methodName, ClassUtils.parses(methodParams));
    }

    public ReflectMethod(Class clz, P owner, String methodName, Class...methodParams) throws NoSuchMethodException {
        mClass = clz;
        mOwner = owner;
        mMethod = clz.getDeclaredMethod(methodName, methodParams);
        mMethod.setAccessible(true);
    }

    public static <P,T> ReflectMethod<P,T> createSafe(String clzName, String methodName, String...methodParams){
        try {
            return new ReflectMethod<P,T>(clzName, methodName, methodParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <P,T> ReflectMethod<P,T> createSafe(Class clz, String methodName, Class...methodParams){
        try {
            return new ReflectMethod<P,T>(clz, methodName, methodParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <P,T> ReflectMethod<P,T> createSafe(String clzName, P owner, String methodName, String...methodParams){
        try {
            return new ReflectMethod<P,T>(clzName, owner, methodName, methodParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <P,T> ReflectMethod<P,T> createSafe(Class clz, P owner, String methodName, Class...methodParams){
        try {
            return new ReflectMethod<P,T>(clz, owner, methodName, methodParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public T invoke(Object...args) throws IllegalAccessException, InvocationTargetException {
        return (T) mMethod.invoke(mOwner, args);
    }

    public T invokeSafe(Object...args){
        try {
            return invoke(args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public T invokeWith(Object owner, Object...args) throws InvocationTargetException, IllegalAccessException {
        return (T) mMethod.invoke(owner, args);
    }

    public T invokeWithSafe(Object owner, Object...args){
        try {
            return invokeWith(owner, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
