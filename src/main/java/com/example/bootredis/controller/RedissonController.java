package com.example.bootredis.controller;

import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * @author wxl
 */
@RestController
public class RedissonController {
    public static final String REDIS_LOCK = "wxl_lock";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private Redisson redisson;

    @GetMapping("/bug_goods1")
    public String buy_Goods() throws Exception {
        String value = UUID.randomUUID().toString() + Thread.currentThread().getName();
        System.out.println(value);
        RLock lock = redisson.getLock(REDIS_LOCK);
        lock.lock();

        try {

            String result = stringRedisTemplate.opsForValue().get("goods:001");

            int goodNumber = result == null ? 0 : Integer.parseInt(result);
            if (goodNumber > 0) {
                int realNumber = goodNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001", String.valueOf(realNumber));
                System.out.println("成功买到商品  库存还剩下" + realNumber + "\t服务端口为" + serverPort);
                return "成功买到商品  库存还剩下" + realNumber + "\t服务端口为" + serverPort;
            } else {
                System.out.println("商品已经售空/活动结束，欢迎下次光临" + serverPort);
                return "商品已经售空/活动结束，欢迎下次光临" + serverPort;
            }
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }


}
