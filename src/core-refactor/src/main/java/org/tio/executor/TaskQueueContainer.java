package org.tio.executor;

import org.tio.robin.RoundRobin;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Copyright (c) for darkidiot
 * Date:2017/8/27
 * Author: <a href="darkidiot@icloud.com">darkidiot</a>
 * Desc: 任务队列分发容器
 */
public class TaskQueueContainer<T> {
    /** 名称 */
    private String name;
    /** 任务队列容器 */
    private final ConcurrentHashMap<String, T> cacheMap = new ConcurrentHashMap<>();

    private RoundRobin robin;

    private void expand(String name,T t){
        cacheMap.put(name,t);
    }


}