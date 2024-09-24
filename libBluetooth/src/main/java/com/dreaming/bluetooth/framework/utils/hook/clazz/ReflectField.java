package com.dreaming.bluetooth.framework.utils.hook.clazz;

import java.lang.reflect.Field;

public class ReflectField<P,T> {
    final P mOwner;
    final Class mClass;
    final Field mField;

    public ReflectField(String clzName, String fieldName) throws ClassNotFoundException, NoSuchFieldException {
        this(clzName, null, fieldName);
    }

    public ReflectField(Class clz, String fieldName) throws NoSuchFieldException {
        this(clz, null, fieldName);
    }

    public ReflectField(String clzName, P owner, String fieldName) throws ClassNotFoundException, NoSuchFieldException {
        this(Class.forName(clzName), owner, fieldName);
    }

    public ReflectField(Class clz, P owner, String fieldName) throws NoSuchFieldException {
        mClass = clz;
        mOwner = owner;
        mField = clz.getDeclaredField(fieldName);
        mField.setAccessible(true);
    }


    public static <P,T> ReflectField<P,T> createSafe(String clzName, String fieldName){
        try {
            return new ReflectField<P,T>(clzName, fieldName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <P,T> ReflectField<P,T> createSafe(Class clz, String fieldName){
        try {
            return new ReflectField<P,T>(clz, fieldName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <P,T> ReflectField<P,T> createSafe(String clzName, P owner, String fieldName){
        try {
            return new ReflectField<P,T>(clzName, owner, fieldName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static <P,T> ReflectField<P,T> createSafe(Class clz, P owner, String fieldName){
        try {
            return new ReflectField<P,T>(clz, owner, fieldName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public T get() throws IllegalAccessException {
        return (T) mField.get(mOwner);
    }

    public T getSafe() {
        try {
            return get();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }


    public T get(Object owner) throws IllegalAccessException {
        return (T) mField.get(owner);
    }

    public T getSafe(Object owner) {
        try {
            return (T) mField.get(owner);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
