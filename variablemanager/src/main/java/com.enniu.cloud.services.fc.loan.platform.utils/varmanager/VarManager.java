package com.enniu.cloud.services.fc.loan.platform.utils.varmanager;


import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.function.ApplyFunction;
import com.enniu.cloud.services.fc.loan.platform.utils.varmanager.function.ThrowableFunction;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.experimental.UtilityClass;

/**
 * threadLocal变量管理器，配合ThreadLocalFilter使用
 * 适用的场景：
 * 1、多次DB查询或接口调用间不关心数据的变化，可以仅使用第一次的结果。
 *
 * localMap和shareMap不能同时存在，当拥有shareMap后清除localMap--提高安全性。
 * @author liuyihan
 * @since 2018/6/22
 */
@UtilityClass
public class VarManager {

    private final ThreadLocal<Map<String, Object>> localMapTl = new ThreadLocal<>();
    private final ThreadLocal<Map<String, Object>> shareMapTl = new ThreadLocal<>();
    private final ThreadLocal<Boolean> switchOpen = new ThreadLocal<>();
    private final ThreadLocal<ReentrantReadWriteLock> lockTl = new ThreadLocal<>();

    /**
     * 开关控制，防止未Clear
     */
    public void open(){
        switchOpen.set(true);
    }

    public void close(){
        switchOpen.remove();
    }

    public <T> void put(String key,T val){
        if(val == null) {
            return;
        }
        try {
            //线程内变量更新
            Map<String, Object> targetMap = getTargetMap();

            writeLock();
            //若线程变量池、共享变量池都未创建，则创建变量池并赋值给线程变量池
            if (targetMap == null) {
                targetMap = Maps.newHashMap();
                localMapTl.set(targetMap);
            }
            targetMap.put(key, val);
        }finally {
            writeUnLock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key){
        readLock();
        try {
            Map<String, Object> targetMap = getTargetMap();
            if (targetMap != null && targetMap.get(key) != null) {
                return (T) targetMap.get(key);
            }
            return null;
        }finally {
            readUnLock();
        }
    }

    public void remove(String key){
        Map<String, Object> targetMap = getTargetMap();
        if(targetMap != null){
            targetMap.remove(key);
        }
    }

    public void clear(){
        Map<String,Object> targetMap = getTargetMap();
        if(targetMap != null) {
            targetMap.clear();
        }
    }

    public void threadLocalClear(){
        localMapTl.remove();
        shareMapTl.remove();
        lockTl.remove();
    }

    public Pair<Map<String,Object>,ReentrantReadWriteLock> getOrInitShareMap(){
        Map<String,Object> shareMap;
        //如果已经存在，直接返回
        if((shareMap = shareMapTl.get())!=null){
            return new Pair<>(shareMap, lockTl.get());
        }
        //不存在则用线程变量初始化，copy一个新的map来保证安全性
        shareMap = Maps.newLinkedHashMap();
        Optional.of(localMapTl).map(ThreadLocal::get).orElse(
            Maps.newLinkedHashMap()).forEach(shareMap::put);
        //清空线程内变量、初始化共享变量
        localMapTl.remove();
        shareMapTl.set(shareMap);
        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        lockTl.set(lock);
        return new Pair<>(shareMap, lock);
    }

    public void initShareMap(Map<String,Object> sourceMap){
        shareMapTl.set(sourceMap);
    }

    public void initLock(ReentrantReadWriteLock lock){
        lockTl.set(lock);
    }

    /**
     * 获取变量管理map，有共享变量池时优先使用，否则使用本地线程变量池.
     * @return 目标变量池
     */
    private Map<String,Object> getTargetMap(){
        return Optional.ofNullable(shareMapTl.get()).orElse(localMapTl.get());
    }

    private void readLock(){
        ReentrantReadWriteLock lock = lockTl.get();
        if(lock == null){
            return;
        }
        lock.readLock().lock();
    }

    private void readUnLock(){
        ReentrantReadWriteLock lock = lockTl.get();
        if(lock == null){
            return;
        }
        lock.readLock().unlock();
    }

    private void writeLock(){
        ReentrantReadWriteLock lock = lockTl.get();
        if(lock == null){
            return;
        }
        lock.writeLock().lock();
    }

    private void writeUnLock(){
        ReentrantReadWriteLock lock = lockTl.get();
        if(lock == null){
            return;
        }
        if(lock.isWriteLockedByCurrentThread()){
            lock.writeLock().unlock();
        }
    }


//**************************************VarManagerFunction***********************************************

    public <T> T apply(String key, ApplyFunction<T> f) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(f);

        if(switchOpen.get() == null || !switchOpen.get()){
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

    /**
     * 清除ThreadLocal内变量，并直接调用方法，
     * 用于以下场景：
     *
     * 1、在更新数据前查询过，更新后又需要查询
     * @return method result
     */
    public <T> T clearAndApply(ApplyFunction<T> f) {
        clear();
        return f.apply();
    }

    public <T> T applyThrow(String key, ThrowableFunction<T> f) throws Throwable {
        Objects.requireNonNull(key);
        Objects.requireNonNull(f);

        if(switchOpen.get() == null || !switchOpen.get()){
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
