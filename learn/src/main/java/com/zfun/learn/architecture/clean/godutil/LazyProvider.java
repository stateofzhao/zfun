package com.zfun.learn.architecture.clean.godutil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

abstract public class LazyProvider<K, V> {
    //不要在这个方法中再搞同步了
    protected abstract V create(K key);

    private volatile Map<K, V> map = null;

    synchronized protected V get(K key) {
        if (null == map) {
            map = Collections.synchronizedMap(new HashMap<>());
        }
        V value = map.get(key);
        if (null == value) {
            value = create(key);
            map.put(key, value);
        }
        return value;
    }
}
