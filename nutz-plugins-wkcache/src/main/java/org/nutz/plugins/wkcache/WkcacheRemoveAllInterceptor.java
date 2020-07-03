package org.nutz.plugins.wkcache;

import org.nutz.aop.InterceptorChain;
import org.nutz.ioc.loader.annotation.IocBean;
import org.nutz.lang.Strings;
import org.nutz.plugins.wkcache.annotation.CacheDefaults;
import org.nutz.plugins.wkcache.annotation.CacheRemoveAll;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.lang.reflect.Method;

/**
 * Created by wizzer on 2017/6/14.
 */
@IocBean(singleton = false)
public class WkcacheRemoveAllInterceptor extends AbstractWkcacheInterceptor {

    public void filter(InterceptorChain chain) throws Throwable {
        Method method = chain.getCallingMethod();
        CacheRemoveAll cacheRemoveAll = method.getAnnotation(CacheRemoveAll.class);
        String cacheName = Strings.sNull(cacheRemoveAll.cacheName());
        if (Strings.isBlank(cacheName)) {
            CacheDefaults cacheDefaults = method.getDeclaringClass()
                    .getAnnotation(CacheDefaults.class);
            cacheName = cacheDefaults != null ? cacheDefaults.cacheName() : "wk";
        }
        // 使用 scan 指令来查找所有匹配到的 Key
        ScanParams match = new ScanParams().match(cacheName + ":*");
        ScanResult<String> scan = redisService().scan(ScanParams.SCAN_POINTER_START, match);
        while (true) {
            for (String key : scan.getResult()) {
                redisService().del(key);
            }
            // 已经迭代结束了
            if (scan.isCompleteIteration()) {
                break;
            }
        }
        chain.doChain();
    }
}
