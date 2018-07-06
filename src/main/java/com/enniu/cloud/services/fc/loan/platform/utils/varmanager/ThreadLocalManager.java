package com.enniu.cloud.services.fc.loan.platform.utils.varmanager;


import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.function.ApplyFunction;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.function.ThrowableFunction;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import lombok.experimental.UtilityClass;

/**
 * threadLocal变量管理器，配合ThreadLocalFilter使用
 * 适用的场景：
 * 1、多次DB查询或接口调用间不关心数据的变化，可以仅使用第一次的结果。
 * @author liuyihan
 * @since 2018/6/22
 */
@UtilityClass
public class ThreadLocalManager {

    private final ThreadLocal<Map<String, Object>> tl = new ThreadLocal<>();
    private final ThreadLocal<Boolean> switchOpen = new ThreadLocal<>();

    /**
     * 开关控制，防止未Clear
     */
    public void open(){
        switchOpen.set(true);
    }

    public void close(){
        switchOpen.set(false);
    }

    public <T> void put(String key,T val){
        if(val == null) {
            return;
        }
        Map<String, Object> tlMap;
        if((tlMap = tl.get()) == null){
            tlMap = Maps.newHashMap();
            tl.set(tlMap);
        }
        tlMap.put(key, val);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key){
        Map<String, Object> map = tl.get();
        if(map == null){
            return null;
        }
        Object obj = map.get(key);
        return (T)obj;
    }

    public void remove(String key){
        Map<String, Object> tlMap;
        if((tlMap = tl.get()) != null){
            tlMap.remove(key);
        }
    }

    public void clear(){
        tl.remove();
    }


//**************************************VarManagerFunction***********************************************

    public <T> T apply(String key, ApplyFunction<T> f) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(f);

        if(!switchOpen.get()){
            return f.apply();
        }

        T result = get(key);
        //如果map中存在直接返回
        if(result != null){
            return result;
        }
        //否则查询并返回
        try {
            result = f.apply();
        }finally {
            if(result != null){
                put(key, result);
            }
        }
        return result;
    }

    /**
     * 清除ThreadLocal内变量，并直接调用方法，
     * 用于以下场景：
     * 1、数据准确性要求极高，同一RPC内需多次查询
     * 2、在更新数据前查询过，更新后又需要查询
     * @return method result
     */
    public <T> T applyNoCache(ApplyFunction<T> f) {
        clear();
        return f.apply();
    }

    public <T> T applyThrow(String key, ThrowableFunction<T> f) throws Throwable {
        Objects.requireNonNull(key);
        Objects.requireNonNull(f);

        if(!switchOpen.get()){
            return f.apply();
        }

        T result = get(key);
        //如果map中存在直接返回
        if(result != null){
            return result;
        }
        //否则查询并返回
        try {
            result = f.apply();
        }finally {
            if(result != null){
                put(key, result);
            }
        }
        return result;
    }

}
