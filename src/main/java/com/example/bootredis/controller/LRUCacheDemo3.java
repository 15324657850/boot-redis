package com.example.bootredis.controller;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author wxl
 */
public class LRUCacheDemo3<K, V> extends LinkedHashMap<K, V> {
    private int capacity;//缓存坑位

    public LRUCacheDemo3(int capacity) {
        super(capacity, 0.75F, false);
        this.capacity = capacity;

    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return super.size() > capacity;
    }

    public static void main(String[] args) {
        LRUCacheDemo3 lruCacheDemo3 = new LRUCacheDemo3(3);
        lruCacheDemo3.put(1, "a");
        lruCacheDemo3.put(2, "b");
        lruCacheDemo3.put(3, "c");
        System.out.println(lruCacheDemo3.keySet());

        lruCacheDemo3.put(4, "d");
        System.out.println(lruCacheDemo3.keySet());

        lruCacheDemo3.put(3, "c");
        System.out.println(lruCacheDemo3.keySet());

        lruCacheDemo3.put(3, "c");
        System.out.println(lruCacheDemo3.keySet());
        lruCacheDemo3.put(3, "c");
        System.out.println(lruCacheDemo3.keySet());

        lruCacheDemo3.put(5, "x");
        System.out.println(lruCacheDemo3.keySet());


    }
}
