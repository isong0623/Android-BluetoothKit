package com.dreaming.bluetooth.framework.utils.hook;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public abstract class CommonProxyHandler<P,T> implements InvocationHandler {
    String sMethodName = "";
    P owner;


    @Override
    public Object invoke(Object proxy, Method method, Object[] args){

        return null;
    }
}
