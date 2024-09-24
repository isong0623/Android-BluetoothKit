package com.dreaming.bluetooth.framework.utils;

public class ClassUtils {
    public static Class parse(String name) throws ClassNotFoundException {
        return Class.forName(name);
    }

    public static Class parseNoThrow(String name) {
        if(name == null) return null;
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Class[] parses(String...names) throws ClassNotFoundException {
        Class[] classes = new Class[names.length];
        for(int i=0,ni=names.length;i<ni;++i){
            Object o = names[i];
            Class c = Class.forName((String)o);
            classes[i] = c;
        }
        return classes;
    }

    public static Class[] parsesNoThrow(String...names){
        Class[] classes = new Class[names.length];
        for(int i=0,ni=names.length;i<ni;++i){
            Object o = names[i];
            Class c = null;
            try {
                c = Class.forName((String)o);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
            classes[i] = c;
        }
        return classes;
    }
}
