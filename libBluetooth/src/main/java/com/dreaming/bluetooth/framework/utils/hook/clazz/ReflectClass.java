package com.dreaming.bluetooth.framework.utils.hook.clazz;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * this is a reflect class
 *
 * you can use it to create a class that you did't call directly
 *
 * you can create a script to generate all classes you need!
 *
 * use guide:
 *
 * this class implements part method for android.app.AndroidThread.java
 * shows how to get the current Application
 * you just call AndroidThread().getApplication()
 *
 * public class AndroidThread extends ReflectClass{
 *     private static final String ANDROID_THREAD_CLASS_NAME = "android.app.ActivityThread";
 *     public static class CompanionObject extends ReflectClass{
 *         public CompanionObject() {
 *             super(
 *                 new Builder()
 *                     //public static final String TAG = "ActivityThread";
 *                     .putFieldSafe(ANDROID_THREAD_CLASS_NAME, "TAG")
 *                     // public static ActivityThread currentActivityThread()
 *                     .putMethodSafe(ANDROID_THREAD_CLASS_NAME, "currentActivityThread")
 *                     .build()
 *             );
 *         }
 *
 *         public String getTag(){
 *             return getSafe("TAG");
 *         }
 *
 *         public Object currentActivityThread(){
 *             return invokeSafe("currentActivityThread");
 *         }
 *     }
 *     private static CompanionObject companionObject = new CompanionObject();
 *     public static Object currentActivityThread(){
 *         return companionObject.currentActivityThread();
 *     }
 *
 *     public AndroidThread(){
 *         super(
 *                 new Builder()
 *                         //public Application getApplication()
 *                         .putMethodSafe(ANDROID_THREAD_CLASS_NAME, currentActivityThread(), "getApplication")
 *                     .build()
 *         );
 *     }
 *
 *     public Application getApplication(){
 *         return invokeSafe("getApplication");
 *     }
 * }
 *
 */
public abstract class ReflectClass {
    protected final Map<String, Map<Integer, ReflectMethod>> mMethods;
    protected final Map<String, Map<Integer, ReflectField>> mFields;
    public static class Builder{
        protected final Map<String, Map<Integer, ReflectMethod>> mMethods;
        protected final Map<String, Map<Integer, ReflectField>> mFields;
        public Builder(){
            mMethods = new HashMap<>();
            mFields = new HashMap<>();
        }
        //region safe method
        public <P,T> Builder putMethodSafe(String clzName, String methodName, String...methodParams){
            return putMethodSafe((Integer)null, (T)null, (P)null, clzName, methodName, methodParams);
        }
        public <P,T> Builder putMethodSafe(Integer id, String clzName, String methodName, String...methodParams){
            return putMethodSafe(id,  (T)null, (P)null,  clzName, methodName, methodParams);
        }
        private  <P,T> Builder putMethodSafe(Integer id, T t, P p, String clzName, String methodName, String...methodParams) {
            try {
                return putMethod(id, new ReflectMethod<P,T>(clzName,methodName,methodParams));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return this;
        }
        
        public <P,T> Builder putMethodSafe(Class clz, String methodName, Class...methodParams){
            return putMethodSafe((Integer) null, (T)null, (P)null, clz, methodName, methodParams);
        }
        public <P,T> Builder putMethodSafe(Integer id, Class clz, String methodName, Class...methodParams){
            return putMethodSafe(id, (T)null, (P)null, clz, methodName, methodParams);
        }
        private  <P,T> Builder putMethodSafe(Integer id, T t, P p, Class clz, String methodName, Class...methodParams){
            try {
                return putMethod(id, new ReflectMethod<P,T>(clz, methodName, methodParams));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return this;
        }
        
        public <P,T> Builder putMethodSafe(String clzName, P owner, String methodName, String...methodParams){
            return putMethodSafe((Integer)null, (P)null, clzName,owner,methodName,methodParams);
        }
        public <P,T> Builder putMethodSafe(Integer id, String clzName, P owner, String methodName, String...methodParams){
            return putMethodSafe(id, (P)null, clzName,owner,methodName,methodParams);
        }
        private  <P,T> Builder putMethodSafe(Integer id, P p, String clzName, P owner, String methodName, String...methodParams){
            try {
                return putMethod(id, new ReflectMethod<P,T>(clzName, owner, methodName, methodParams));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return this;
        }

        public <P,T> Builder putMethodSafe(Class clz, P owner, String methodName, Class...methodParams){
            return putMethodSafe((Integer)null, (P)null, clz, owner, methodName, methodParams);
        }
        public <P,T> Builder putMethodSafe(Integer id, Class clz, P owner, String methodName, Class...methodParams){
            return putMethodSafe(id, (P)null, clz, owner, methodName, methodParams);
        }
        private <P,T> Builder putMethodSafe(Integer id, P p, Class clz, P owner, String methodName, Class...methodParams){
            try {
                return putMethod(id, new ReflectMethod<P,T>(clz,owner,methodName, methodParams));
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return this;
        }
        //endregion

        //region unsafe method
        public <P,T> Builder putMethod(String clzName, String methodName, String...methodParams) throws NoSuchMethodException, ClassNotFoundException {
            return putMethod((Integer)null, (T)null, (P)null, clzName, methodName, methodParams);
        }
        public <P,T> Builder putMethod(Integer id, String clzName, String methodName, String...methodParams) throws NoSuchMethodException, ClassNotFoundException {
            return putMethod(id,  (T)null, (P)null,  clzName, methodName, methodParams);
        }
        private  <P,T> Builder putMethod(Integer id, T t, P p, String clzName, String methodName, String...methodParams) throws NoSuchMethodException, ClassNotFoundException {
            return putMethod(id, new ReflectMethod<P,T>(clzName,methodName, methodParams));
        }

        public <P,T> Builder putMethod(Class clz, String methodName, Class...methodParams) throws NoSuchMethodException {
            return putMethod((Integer) null, (T)null, (P)null, clz, methodName, methodParams);
        }
        public <P,T> Builder putMethod(Integer id, Class clz, String methodName, Class...methodParams) throws NoSuchMethodException {
            return putMethod(id, (T)null, (P)null, clz, methodName, methodParams);
        }
        private  <P,T> Builder putMethod(Integer id, T t, P p, Class clz, String methodName, Class...methodParams) throws NoSuchMethodException {
            return putMethod(id, new ReflectMethod<P,T>(clz, methodName, methodParams));
        }

        public <P,T> Builder putMethod(String clzName, P owner, String methodName, String...methodParams) throws NoSuchMethodException, ClassNotFoundException {
            return putMethod((Integer)null, (P)null, clzName,owner,methodName, methodParams);
        }
        public <P,T> Builder putMethod(Integer id, String clzName, P owner, String methodName, String...methodParams) throws NoSuchMethodException, ClassNotFoundException {
            return putMethod(id, (P)null, clzName,owner,methodName, methodParams);
        }
        private  <P,T> Builder putMethod(Integer id, T t, String clzName, P owner, String methodName, String...methodParams) throws NoSuchMethodException, ClassNotFoundException {
            return putMethod(id, new ReflectMethod<P,T>(clzName, owner, methodName, methodParams));
        }

        public <P,T> Builder putMethod(Class clz, P owner, String methodName, Class...methodParams) throws NoSuchMethodException {
            return putMethod((Integer)null, (P)null, clz, owner, methodName, methodParams);
        }
        public <P,T> Builder putMethod(Integer id, Class clz, P owner, String methodName, Class...methodParams) throws NoSuchMethodException {
            return putMethod(id, (P)null, clz, owner, methodName, methodParams);
        }
        private <P,T> Builder putMethod(Integer id, T t, Class clz, P owner, String methodName, Class...methodParams) throws NoSuchMethodException {
            return putMethod(id, new ReflectMethod<P,T>(clz,owner,methodName,methodParams));
        }
        //endregion

        public <P,T> Builder putMethod(ReflectMethod<P,T> method){
            return putMethod(null, method);
        }
        public <P,T> Builder putMethod(Integer id, ReflectMethod<P,T> method){
            Map<Integer, ReflectMethod> m = mMethods.get(method.mMethod.getName());
            if(m == null){
                m = new HashMap<>();
                mMethods.put(method.mMethod.getName(), m);
            }

            if(id == null){
                for(int i=0;i<Integer.MAX_VALUE;++i){
                    if(!m.containsKey(i)){
                        m.put(i, method);
                        break;
                    }
                }
            }
            else{
                m.put(id, method);
            }

            return this;
        }

        //region safe field
        public <P,T> Builder putFieldSafe(String clzName, String fieldName){
            return putFieldSafe((Integer)null, (T)null, (P)null, clzName, fieldName);
        }
        public <P,T> Builder putFieldSafe(Integer id, String clzName, String fieldName){
            return putFieldSafe(id, (T)null, (P)null, clzName, fieldName);
        }
        private <P,T> Builder putFieldSafe(Integer id, T t, P p, String clzName, String fieldName){
            try {
                return putField(id, new ReflectField<P,T>(clzName,fieldName));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return this;
        }

        public <P,T> Builder putFieldSafe(Class clz,String fieldName){
            return putFieldSafe((Integer)null, (T)null, (P)null, clz,fieldName);
        }
        public <P,T> Builder putFieldSafe(Integer id, Class clz, String fieldName){
            return putFieldSafe(id, (T)null, (P)null,clz,fieldName);
        }
        private <P,T> Builder putFieldSafe(Integer id, T t, P p, Class clz,String fieldName){
            try {
                return putField(id, new ReflectField<P,T>(clz,fieldName));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return this;
        }

        public <P,T> Builder putFieldSafe(String clzName, P owner, String fieldName){
            return putFieldSafe(null,(T)null, clzName,owner,fieldName);
        }
        public <P,T> Builder putFieldSafe(Integer id, String clzName, P owner, String fieldName){
            return putFieldSafe(id,null,clzName,owner,fieldName);
        }
        private <P,T> Builder putFieldSafe(Integer id, T t, String clzName, P owner, String fieldName){
            try {
                return putField(id, new ReflectField<P,T>(clzName,owner,fieldName));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return this;
        }

        public <P,T> Builder putFieldSafe(Class clz, P owner, String fieldName){
            return putFieldSafe(null, null, null, clz, owner, fieldName);
        }
        public <P,T> Builder putFieldSafe(Integer id, Class clz, P owner, String fieldName){
            return putFieldSafe(id, null, null, clz, owner, fieldName);
        }
        private  <P,T> Builder putFieldSafe(Integer id, T t, P p, Class clz, P owner, String fieldName){
            try {
                return putField(id, new ReflectField<P,T>(clz,owner,fieldName));
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            return this;
        }
        //endregion

        //region unsafe field
        public <P,T> Builder putField(String clzName, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
            return putField((Integer)null, (T)null, (P)null, clzName, fieldName);
        }
        public <P,T> Builder putField(Integer id, String clzName, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
            return putField(id, (T)null, (P)null, clzName, fieldName);
        }
        private <P,T> Builder putField(Integer id, T t, P p, String clzName, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
            return putField(id, new ReflectField<P,T>(clzName,fieldName));
        }
        
        public <P,T> Builder putField(Class clz,String fieldName) throws NoSuchFieldException {
            return putField((Integer)null, (T)null, (P)null, clz,fieldName);
        }
        public <P,T> Builder putField(Integer id, Class clz,String fieldName) throws NoSuchFieldException {
            return putField(id, (T)null, (P)null,clz,fieldName);
        }
        private <P,T> Builder putField(Integer id, T t, P p, Class clz,String fieldName) throws NoSuchFieldException {
            return putField(id, new ReflectField<P,T>(clz,fieldName));
        }
        
        public <P,T> Builder putField(String clzName, P owner, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
            return putField(null,(T)null, clzName,owner,fieldName);
        }
        public <P,T> Builder putField(Integer id, String clzName, P owner, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
            return putField(id,null,clzName,owner,fieldName);
        }
        private <P,T> Builder putField(Integer id, T t, String clzName, P owner, String fieldName) throws NoSuchFieldException, ClassNotFoundException {
            return putField(id, new ReflectField<P,T>(clzName,owner,fieldName));
        }
        
        public <P,T> Builder putField(Class clz, P owner, String fieldName) throws NoSuchFieldException {
            return putField(null, null, null, clz, owner, fieldName);
        }
        public <P,T> Builder putField(Integer id, Class clz, P owner, String fieldName) throws NoSuchFieldException {
            return putField(id, null, null, clz, owner, fieldName);
        }
        private  <P,T> Builder putField(Integer id, T t, P p, Class clz, P owner, String fieldName) throws NoSuchFieldException {
            return putField(id, new ReflectField<P,T>(clz,owner,fieldName));
        }
        //endregion
        
        public <P,T> Builder putField(ReflectField<P,T> field){
            return putField(null, field);
        }
        public <P,T> Builder putField(Integer id, ReflectField<P,T> field){
            Map<Integer, ReflectField> m = mFields.get(field.mField.getName());
            if(m == null){
                m = new HashMap<>();
                mFields.put(field.mField.getName(), m);
            }

            if(id == null){
                for(int i=0;i<Integer.MAX_VALUE;++i){
                    if(!m.containsKey(i)){
                        m.put(i, field);
                        break;
                    }
                }
            }
            else{
                m.put(id, field);
            }

            return this;
        }

        public Builder build(){
            return this;
        }
    }

    protected ReflectClass(Builder builder){
        this.mMethods = builder.mMethods;
        this.mFields = builder.mFields;
    }


    protected <T> T invokeSafe(String methodName, Object...args) {
        return invokeSafe(null, methodName, args);
    }

    protected <T> T invokeSafe(Integer id, String methodName, Object...args) {
        try {
            return invoke(id, methodName,args);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected <T> T invokeSafe(ReflectMethod rftMethod, Object...args) {
        try {
            return (T) rftMethod.invoke(args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected <T> T invoke(String methodName, Object...args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return invoke(null, methodName, args);
    }

    protected <T> T invoke(Integer id, String methodName, Object...args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Map<Integer, ReflectMethod> map = mMethods.get(methodName);
        if(id != null){
            return invoke(map.get(id), args);
        }
        if(map.size() == 1){
            return invoke(map.get(map.keySet().iterator().next()),args);
        }
        throw new NoSuchMethodException();
    }

    protected <T> T invoke(ReflectMethod rftMethod, Object...args) throws InvocationTargetException, IllegalAccessException {
        return (T) rftMethod.invoke(args);
    }

    protected <T> T getSafe(String fieldName){
        return getSafe(null, fieldName);
    }
    protected <T> T getSafe(Integer id, String fieldName){
        try {
            return get(id,fieldName);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    protected <T> T getSafe(ReflectField rftField){
        try {
            return (T) rftField.get();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected <T> T get( String fieldName) throws NoSuchFieldException, IllegalAccessException {
        return get(null, fieldName);
    }
    protected <T> T get(Integer id, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Map<Integer, ReflectField> map = mFields.get(fieldName);
        if(id != null){
            return get(map.get(id));
        }
        if(map.size() == 1){
            return get(map.get(map.keySet().iterator().next()));
        }
        throw new NoSuchFieldException();
    }
    protected <T> T get(ReflectField rftField) throws IllegalAccessException {
        return (T) rftField.get();
    }
}
