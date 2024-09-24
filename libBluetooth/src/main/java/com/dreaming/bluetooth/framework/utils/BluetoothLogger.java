package com.dreaming.bluetooth.framework.utils;

import android.util.Log;

import com.dreaming.bluetooth.framework.BluetoothContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * 蓝牙日志记录器
 *
 * 调用打印日志的函数会自动调用记录日志文件的函数
 *
 * 打印日志
 *  @see BluetoothLogger#d(Tag, String, Object...)
 *  @see BluetoothLogger#d(String, Object...)
 *  @see BluetoothLogger#d(Tag, Throwable)
 *  @see BluetoothLogger#d(Throwable)
 *
 *  @see BluetoothLogger#v(Tag, String, Object...)
 *  @see BluetoothLogger#v(String, Object...)
 *  @see BluetoothLogger#v(Tag, Throwable)
 *  @see BluetoothLogger#v(Throwable)
 *
 *  @see BluetoothLogger#i(Tag, String, Object...)
 *  @see BluetoothLogger#i(String, Object...)
 *  @see BluetoothLogger#i(Tag, Throwable)
 *  @see BluetoothLogger#i(Throwable)
 *
 *  @see BluetoothLogger#w(Tag, String, Object...)
 *  @see BluetoothLogger#w(String, Object...)
 *  @see BluetoothLogger#w(Tag, Throwable)
 *  @see BluetoothLogger#w(Throwable)
 *
 *  @see BluetoothLogger#e(Tag, String, Object...)
 *  @see BluetoothLogger#e(String, Object...)
 *  @see BluetoothLogger#e(Tag, Throwable)
 *  @see BluetoothLogger#e(Throwable)
 *
 * 记录日志文件
 * @see BluetoothLogger#logFile(String, String, String)
 * 格式： yyyy-MM-dd HH:mm:ss-sss [Thread -30] [level] [tag - 30] msg
 *
 *
 * 日志控制
 * @see BluetoothLogger#disableTags(Object...)       禁用这个logger某些标签，包括Logcat和LogFile
 * @see BluetoothLogger#disableLogTags(Object...)    禁用这个logger某些标签，包括LogFile
 * @see BluetoothLogger#disableLogcatTags(Object...) 禁用这个logger某些标签，包括Logcat
 *
 * @see BluetoothLogger#disable(Class)        禁用这个类所有类型的记录，包括Logcat和LogFile
 * @see BluetoothLogger#disable(String)       禁用这个标签所有类型的记录，包括Logcat和LogFile
 *
 * @see BluetoothLogger#disableLog(Class)     禁用这个类的所有的LogFile
 * @see BluetoothLogger#disableLog(String)    禁用这个标签的所有的LogFile
 *
 * @see BluetoothLogger#disableLogcat(Class)  禁用这个类的所有的Logcat
 * @see BluetoothLogger#disableLogcat(String) 禁用这个标签的所有的Logcat
 * 对应控制的enable与以上逻辑相反，不作解释
 *
 * 日志等级
 * @see BluetoothLogger#LEVEL_ALL     记录D、V、I、W、E
 * @see BluetoothLogger#LEVEL_DEBUG   记录D、V、I、W、E
 * @see BluetoothLogger#LEVEL_VERBOSE 记录V、I、W、E
 * @see BluetoothLogger#LEVEL_INFO    记录I、W、E
 * @see BluetoothLogger#LEVEL_WARN    记录W、E
 * @see BluetoothLogger#LEVEL_ERROR   记录E
 * @see BluetoothLogger#LEVLE_DISABLE 不做记录
 *
 * 日志等级优先级
 * Logger.level > BluetoothLogger.level 对象等级大于全局等级
 *
 */
public class BluetoothLogger {
    private static final Tag NULL = null;
    private static final int iLenTag    = 40;
    private static final int iLenThread = 20;
    private String tag;
    private boolean enable = true;
    private boolean enableLog = true;
    private boolean enableLogcat = true;
    private int levelLog    = LEVEL_ALL;
    private int levelLogcat = LEVEL_ALL;
    
    /**
     * 格式化tag使其满足长度为{@link BluetoothLogger#iLenTag}
     *
     * 对于不足的tag右侧填充空格
     * 对于超过的tag从左到右依次对每个子包删减至最多一个字符
     *
     * @param tag 源处理tag，一般是包含包名的完整类
     * @return 格式化后的tag
     */
    private static String formatTag(String tag){
        tag = tag.replaceFirst("com.dreaming.bluetooth.framework","");
        while(tag.length()<iLenTag){
            tag = tag + " ";
        }
        int len = tag.length();
        if(len == iLenTag) return tag;

        StringBuilder sb = new StringBuilder();
        String[] splits = tag.split("\\.");
        for(int i=0,ni=splits.length;i<ni && len>iLenTag;++i){
            String s = splits[i];
            if(s.length()>1){
                int sLen = s.length()-1;
                if(len - sLen>iLenTag){
                    len -= sLen;
                    splits[i] = s.substring(0,1);
                }
                else{
                    int lLen = len - iLenTag;
                    len = iLenTag;
                    splits[i] = s.substring(0,s.length()-lLen);
                }
            }
        }
        for(int i=0,ni=splits.length;i<ni;++i){
            if(i!=0) sb.append(".");
            sb.append(splits[i]);
        }
        return sb.toString();
    }

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
    private static String currentTime(){
        return sdf.format(new Date());
    }

    private static String currentThread(){
        StringBuilder sb = new StringBuilder();
        sb.append(Thread.currentThread().getName());
        while(sb.length()<iLenThread){
            sb.append(" ");
        }
        return sb.toString();
    }

    private static void put(Object tag, BluetoothLogger logger){
        synchronized (mAllLoggers){
            if(mDisableList.contains(tag)){
                logger.enable = false;
            }
            if(mDisableLogcatList.contains(tag)){
                logger.enableLogcat = false;
            }
            if(mDisableLogList.contains(tag)){
                logger.enableLog = false;
            }
            ArrayList<WeakReference<BluetoothLogger>> lst = mAllLoggers.get(tag);
            if(lst == null){
                lst = new ArrayList<>();
                mAllLoggers.put(tag,lst);
            }
            lst.add(new WeakReference<BluetoothLogger>(logger));
        }
    }
    public BluetoothLogger(Class clz){
        this.tag = formatTag(clz.getName());
        put(clz,this);
    }

    public BluetoothLogger(String tag){
        this.tag = formatTag(tag);
        put(tag,this);
    }

    public BluetoothLogger setLevelLog(int levelLog){
        this.levelLog = levelLog;
        return this;
    }
    
    public BluetoothLogger setLevelLogcat(int levelLogcat){
        this.levelLogcat = levelLogcat;
        return this;
    }

    private Set<Object> sDisableTags = new HashSet<>();
    public BluetoothLogger disableTags(Object...tags){
        sDisableTags.addAll(Arrays.asList(tags));
        return this;
    }
    public BluetoothLogger enableTags(Object...tags){
        sDisableTags.removeAll(Arrays.asList(tags));
        return this;
    }
    private Set<Object> sDisableLogTags = new HashSet<>();
    public BluetoothLogger disableLogTags(Object...tags){
        sDisableLogTags.addAll(Arrays.asList(tags));
        return this;
    }
    public BluetoothLogger enableLogTags(Object...tags){
        sDisableLogTags.removeAll(Arrays.asList(tags));
        return this;
    }
    private Set<Object> sDisableLogcatTags = new HashSet<>();
    public BluetoothLogger disableLogcatTags(Object...tags){
        sDisableLogcatTags.addAll(Arrays.asList(tags));
        return this;
    }
    public BluetoothLogger enableLogcatTags(Object...tags){
        sDisableLogcatTags.removeAll(Arrays.asList(tags));
        return this;
    }

    private int logcatLevel(){
        return Math.min(levelLogcat, BluetoothLogger.logcatLevel);
    }

    private int logLevel(){
        return Math.min(levelLog, BluetoothLogger.logLevel);
    }
    
    private static String getThrowableString(Throwable e) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);

        while (e != null) {
            e.printStackTrace(printWriter);
            e = e.getCause();
        }

        String text = writer.toString();
        printWriter.close();
        return text;
    }
    
    public static class Tag{
        private Object tag;
        public Tag(Object oTag){
            tag = oTag;
        }
        public Object getTag(){
            return tag;
        }

        @Override
        public String toString() {
            if(tag instanceof String) return (String)tag;
            if(tag instanceof Class) return ((Class)tag).getSimpleName();
            return tag.toString();
        }
    }

    private void log(Tag oTag, int levelOfLogcat, String levelTag, String msg, Object...msgFormatValue){
        if(!enable) return;
        String s = String.format(msg, msgFormatValue);
        boolean enableLogTag = true;
        boolean enableLogcatTag = true;
        String sTag = "";
        if(oTag != null){
            if(sDisableTags.contains(oTag)) return;
            if(oTag.tag instanceof String){
                sTag = (String)oTag.tag;
            }
            else if(oTag.tag instanceof Class){
                sTag = ((Class)oTag.tag).getSimpleName();
            }
            else{
                try { sTag = String.valueOf(oTag); } catch (Exception e) { }
            }
            enableLogcatTag = !sDisableLogcatTags.contains(oTag);
            enableLogTag    = !sDisableLogTags.contains(oTag);
        }
        if(StringUtils.isNotBlank(sTag)){
            while(sTag.length()<15){
                sTag+=" ";
            }
            sTag = sTag.substring(0, Math.min(sTag.length(), 15));
            s = sTag +" -> " + s;
        }
        if(enableLogcatTag && enableLogcat && logcatLevel() <= levelOfLogcat) {
            switch (levelOfLogcat){
                case LEVEL_DEBUG  : Log.d(tag, s); break;
                case LEVEL_VERBOSE: Log.v(tag, s); break;
                case LEVEL_INFO   : Log.i(tag, s); break;
                case LEVEL_WARN   : Log.w(tag, s); break;
                case LEVEL_ERROR  : Log.e(tag, s); break;
            }
        }
        if(enableLogTag    && enableLog    && logLevel   () <= levelOfLogcat) logFile(tag, levelTag, s);
    }


    public void d(Tag oTag, String msg, Object...args){
        log(oTag, LEVEL_DEBUG, "DEBUG  ", msg, args);
    }
    public void d(String msg, Object...args){
        d(NULL,msg,args);
    }
    public void d(Tag oTag, Throwable e){
        d(oTag, getThrowableString(e));
    }
    public void d(Throwable e){
        d(NULL, e);
    }

    public void v(Tag oTag, String msg, Object...args){
        log(oTag, LEVEL_VERBOSE, "VERBOSE", msg, args);
    }
    public void v(String msg, Object...args){
        v(NULL, msg,args);
    }
    public void v(Tag oTag, Throwable e){
        v(oTag, getThrowableString(e));
    }
    public void v(Throwable e){
        v(NULL, e);
    }

    public void i(Tag oTag, String msg, Object...args){
        log(oTag, LEVEL_INFO, "INFO   ", msg, args);
    }
    public void i(String msg, Object...args){
        i(NULL, msg, args);
    }
    public void i(Tag oTag, Throwable e){
        i(oTag, getThrowableString(e));
    }
    public void i(Throwable e){
        i(NULL, e);
    }

    public void w(Tag oTag, String msg, Object...args){
        log(oTag, LEVEL_WARN, "WARN   ", msg, args);
    }
    public void w(String msg, Object...args){
        w(NULL,msg,args);
    }
    public void w(Tag oTag, Throwable e){
        w(oTag, getThrowableString(e));
    }
    public void w(Throwable e){
        w(NULL,e);
    }

    public void e(Tag oTag, String msg, Object...args){
        log(oTag, LEVEL_ERROR, "ERROR  ", msg, args);
    }
    public void e(String msg, Object...args){
        e(NULL,msg,args);
    }
    public void e(Tag oTag, Throwable e){
        e(oTag, getThrowableString(e));
    }
    public void e(Throwable e){
        e(NULL, e);
    }

    private static long logTimestamp = 0L;
    private static File fLog;
    private static FileWriter fwLog;
    private synchronized static void logFile(String tag, String level, String msg) {
        if(fLog==null || System.currentTimeMillis() - logTimestamp>3600000L){
            logTimestamp = System.currentTimeMillis();
            try { if(fwLog!=null) fwLog.close(); } catch (Exception e) { }
            fLog = new File(BluetoothContext.get().getFilesDir(), "logs/"+new SimpleDateFormat("yyyy-MM-dd HH.mm").format(logTimestamp)+".log");
            if(!fLog.getParentFile().exists()) fLog.getParentFile().mkdirs();
            try { if(!fLog.exists()) fLog.createNewFile(); } catch (IOException e) { }
            Log.d("BluetoothLogger","write log at: "+fLog.getAbsolutePath());
            try { fwLog = new FileWriter(fLog.getAbsolutePath()); } catch (Exception e) {e.printStackTrace();}
        }
        StringBuilder sb = new StringBuilder();
        sb.append(currentTime());
        sb.append(" ");
        sb.append(currentThread());
        sb.append(" ");
        sb.append(level);
        sb.append(" ");
        sb.append(tag);
        sb.append(" ");
        sb.append(msg);
        sb.append("\n");
        try {
            fwLog.append(sb);
            fwLog.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static final HashMap<Object, ArrayList<WeakReference<BluetoothLogger>>> mAllLoggers = new HashMap<Object, ArrayList<WeakReference<BluetoothLogger>>>();
    private static final Set<Object> mDisableList = new HashSet<Object>();
    private static void set(Object tag, boolean enable, int type){
        synchronized (mAllLoggers){
            ArrayList<WeakReference<BluetoothLogger>> loggers = mAllLoggers.get(tag);
            if(loggers == null) return;
            for(int i=loggers.size()-1;i>-1;--i){
                WeakReference<BluetoothLogger> refLogger = loggers.get(i);
                BluetoothLogger logger = refLogger.get();
                if(logger == null){
                    loggers.remove(i);
                    continue;
                }
                switch (type){
                    case 1:
                        logger.enable = enable;
                        break;
                    case 2:
                        logger.enableLogcat = enable;
                        break;
                    case 3:
                        logger.enableLog = enable;
                        break;
                }
            }
        }
    }
    public static void disable(String tag){
        mDisableList.add(tag);
        set(tag,false,1);
    }
    public static void disable(Class tag){
        mDisableList.add(tag);
        set(tag,false,1);
    }
    public static void enable(Class tag){
        mDisableList.remove(tag);
        set(tag,true,1);
    }
    public static void enable(String tag){
        mDisableList.remove(tag);
        set(tag,true,1);
    }

    private static final Set<Object> mDisableLogcatList = new HashSet<Object>();
    public static void disableLogcat(String tag){
        mDisableLogcatList.add(tag);
        set(tag,false,2);
    }
    public static void disableLogcat(Class tag){
        mDisableLogcatList.add(tag);
        set(tag,false,2);
    }
    public static void enableLogcat(Class tag){
        mDisableLogcatList.remove(tag);
        set(tag,true,2);
    }
    public static void enableLogcat(String tag){
        mDisableLogcatList.remove(tag);
        set(tag,true,2);
    }

    private static final Set<Object> mDisableLogList = new HashSet<Object>();
    public static void disableLog(String tag){
        mDisableLogList.add(tag);
        set(tag,false,3);
    }
    public static void disableLog(Class tag){
        mDisableLogList.add(tag);
        set(tag,false,3);
    }
    public static void enableLog(Class tag){
        mDisableLogList.remove(tag);
        set(tag,true,3);
    }
    public static void enableLog(String tag){
        mDisableLogList.remove(tag);
        set(tag,true,3);
    }
    
    public static final int LEVEL_ALL     = 0;
    public static final int LEVEL_DEBUG   = 1;
    public static final int LEVEL_VERBOSE = 2;
    public static final int LEVEL_INFO    = 3;
    public static final int LEVEL_WARN    = 4;
    public static final int LEVEL_ERROR   = 5;
    public static final int LEVLE_DISABLE = 6;
    
    private static int logcatLevel = LEVEL_ALL;
    public static void setLogcatLevel(int level){
        BluetoothLogger.logcatLevel = level;
    }
    public static int logLevel = LEVEL_ALL;
    public static void setLogLevel(int level){
        BluetoothLogger.logLevel = level;
    }
}
