package com.example.bootredis.controller;

import com.example.bootredis.utils.RedisUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author wxl
 */
@RestController
public class RedisController {
    public static final String REDIS_LOCK = "wxl_lock";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private Redisson redisson;

    @GetMapping("/bug_goods")
    public String buy_Goods() throws Exception {
        String value = UUID.randomUUID().toString() + Thread.currentThread().getName();
        System.out.println(value);
        RLock lock = redisson.getLock(REDIS_LOCK);
        lock.lock();



        try {
            Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, value, 40L, TimeUnit.SECONDS);
            System.out.println(flag);
            if (!flag) {
                return "抢索失败 请稍后重试";
            }
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
//            if (stringRedisTemplate.opsForValue().get(REDIS_LOCK).equals(value)) {
//                stringRedisTemplate.delete(REDIS_LOCK);
//            }
      /*      while (true) {
                stringRedisTemplate.watch(REDIS_LOCK);
                if (stringRedisTemplate.opsForValue().get(REDIS_LOCK).equalsIgnoreCase(value)) {
                    stringRedisTemplate.setEnableTransactionSupport(true);
                    stringRedisTemplate.multi();
                    stringRedisTemplate.delete(REDIS_LOCK);
                    List<Object> list = stringRedisTemplate.exec();
                    if (list == null) {
                        continue;
                    }
                    stringRedisTemplate.unwatch();
                    break;

                }
            }*/
            //lua 脚本
            Jedis jedis = RedisUtils.getJedis();
            String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1]\n" +
                    "then\n" +
                    "    return redis.call(\"del\",KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";

            try {
                Object o = jedis.eval(script, Collections.singletonList(REDIS_LOCK), Collections.singletonList(value));
                if ("1".equalsIgnoreCase(o.toString())) {
                    System.out.println("---del redis lock ok");
                } else {
                    System.out.println("---del redis lock fail");

                }
            } finally {

                if (jedis != null) {
                    jedis.close();
                }
            }
        }
    }

}
