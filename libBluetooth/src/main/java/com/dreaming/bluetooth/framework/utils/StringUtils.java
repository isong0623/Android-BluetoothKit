package com.dreaming.bluetooth.framework.utils;

import java.util.List;

public class StringUtils {
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; ++i) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static <T> String toListString(List<T> s){
        if(s == null) return "";
        StringBuilder sb = new StringBuilder();
        for(T i :s){
            if(sb.length()>0) sb.append(",");
            sb.append(i);
        }
        return sb.toString();
    }

    public static <T> String toArrayString(T[] s){
        if(s == null) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(T i :s){
            if(sb.length()>0) sb.append(",");
            sb.append(i);
        }
        sb.append("]");
        return sb.toString();
    }
}
