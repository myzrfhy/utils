package com.enniu.cloud.services.fc.loan.platform.utils.varmanager;


import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.exception.VarManagerException;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.function.ApplyFunction;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.function.ThrowableFunction;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

/**
 * threadLocal变量管理器，配合ThreadLocalFilter使用
 * 适用的场景：
 * 1、多次DB查询或接口调用间不关心数据的变化，可以仅使用第一次的结果。
 *
 * localMap和shareMap不能同时存在，当拥有shareMap后清除localMap--提高安全性。
 *
 * @author liuyihan
 * @since 2018/6/22
 */
@UtilityClass
public class VarManager {

    private final ThreadLocal<Map<String, Object>> localMapTl = new ThreadLocal<>();
    private final ThreadLocal<Map<String, Object>> shareMapTl = new ThreadLocal<>();
    private final ThreadLocal<Boolean> switchOpen = new ThreadLocal<>();

    /**
     * 开关控制，防止未Clear
     */
    public void open() {
        switchOpen.set(true);
    }

    public void close() {
        switchOpen.remove();
    }

    public <T> void put(String key, T val) {
        if (val == null) {
            return;
        }
        if (switchOpen.get() == null || !switchOpen.get()) {
            throw new VarManagerException("[Varmanager]开关未打开执行put方法");
        }
        //线程内变量更新
        Map<String, Object> targetMap = getTargetMap();
        //若线程变量池、共享变量池都未创建，则创建变量池并赋值给线程变量池
        if (targetMap == null) {
            targetMap = Maps.newHashMap();
            localMapTl.set(targetMap);
        }
        targetMap.put(key, val);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (switchOpen.get() == null || !switchOpen.get()) {
            throw new VarManagerException("[Varmanager]开关未打开执行get方法");
        }
        Map<String, Object> targetMap = getTargetMap();
        if (targetMap != null && targetMap.get(key) != null) {
            return (T) targetMap.get(key);
        }
        return null;
    }

    public void remove(String key) {
        Map<String, Object> targetMap = getTargetMap();
        if (targetMap != null) {
            targetMap.remove(key);
        }
    }

    public void clear() {
        Map<String, Object> targetMap = getTargetMap();
        if (targetMap != null) {
            targetMap.clear();
        }
    }

    public void clearThisCls(String prefix){
        Map<String,Object> thisMap = getTargetMap();
        if(thisMap == null){
            return;
        }
        Set<String> kSet = thisMap.keySet().stream().filter(k->k.startsWith(prefix)).collect(Collectors.toSet());
        for(String k : kSet){
            thisMap.remove(k);
        }
    }

    public void threadLocalClear() {
        localMapTl.remove();
        shareMapTl.remove();
    }

    public Map<String, Object> getOrInitShareMap() {
        Map<String, Object> shareMap;
        //如果已经存在，直接返回
        if ((shareMap = shareMapTl.get()) != null) {
            return shareMap;
        }
        //不存在则用线程变量初始化，copy一个新的map来保证安全性
        shareMap = Maps.newLinkedHashMap();
        Optional.of(localMapTl).map(ThreadLocal::get).orElse(
            Maps.newLinkedHashMap()).forEach(shareMap::put);
        //清空线程内变量、初始化共享变量
        localMapTl.remove();
        shareMapTl.set(shareMap);
        return shareMap;
    }

    public void initShareMap(Map<String, Object> sourceMap) {
        shareMapTl.set(sourceMap);
    }

    /**
     * 获取变量管理map，有共享变量池时优先使用，否则使用本地线程变量池.
     *
     * @return 目标变量池
     */
    private Map<String, Object> getTargetMap() {
        return Optional.ofNullable(shareMapTl.get()).orElse(localMapTl.get());
    }

//**************************************VarManagerFunction***********************************************

    public <T> T apply(String key, ApplyFunction<T> f) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(f);

        if (switchOpen.get() == null || !switchOpen.get()) {
            return f.apply();
        }

        T result = get(key);
        //如果map中存在直接返回
        if (result != null) {
            return result;
        }
        //否则查询并返回
        try {
            result = f.apply();
        } finally {
            if (result != null) {
                put(key, result);
            }
        }
        return result;
    }


    public <T> T applyThrow(String key, ThrowableFunction<T> f) throws Throwable {
        Objects.requireNonNull(key);
        Objects.requireNonNull(f);

        if (switchOpen.get() == null || !switchOpen.get()) {
            return f.apply();
        }

        T result = get(key);
        //如果map中存在直接返回
        if (result != null) {
            return result;
        }
        //否则查询并返回
        try {
            result = f.apply();
        } finally {
            if (result != null) {
                put(key, result);
            }
        }
        return result;
    }

}
