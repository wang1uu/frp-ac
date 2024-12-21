package cc.wang1.frp.util;

import cc.wang1.frp.entity.Host;
import cc.wang1.frp.enums.HostFlagEnum;
import cc.wang1.frp.enums.VerifiableEnum;
import cc.wang1.frp.mapper.HostMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.index.qual.NonNegative;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 缓存工具
 * @author wang1
 */
public class HostCaches {

    /**
     * 到期/未授权 元素缓存时间（毫秒）
     */
    private final long expiredCacheDuration;

    /**
     * 异步线程池名称前缀
     */
    private static final String EXECUTOR_NAME_PREFIX = "Caches-ThreadPool-";

    /**
     * caffeine 缓存
     */
    private final Cache<String, Host> cache;

    public HostCaches(long expiredCacheDuration, TimeUnit expireCacheDurationUnit,
                      long schedulePersistDuration, TimeUnit schedulePersistTimeUnit,
                      long maxCount) {
        if (expiredCacheDuration <= 0
                || schedulePersistDuration <= 0
                || schedulePersistTimeUnit == null
                || expireCacheDurationUnit == null
                || maxCount <= 0) {
            throw new IllegalArgumentException("expiredCacheDuration, expireCacheDurationUnit and timeUnit must not be null");
        }

        this.expiredCacheDuration = expireCacheDurationUnit.toMillis(expiredCacheDuration);
        this.cache = Caffeine.newBuilder()
                .maximumSize(maxCount)
                .expireAfter(new Expiry<String, Host>() {
                    @Override
                    public long expireAfterCreate(String key, Host value, long currentTime) {
                        return updateExpiry(value);
                    }
                    @Override
                    public long expireAfterUpdate(String key, Host value, long currentTime, @NonNegative long currentDuration) {
                        return updateExpiry(value);
                    }
                    @Override
                    public long expireAfterRead(String key, Host value, long currentTime, @NonNegative long currentDuration) {
                        return currentDuration;
                    }
                })
                .removalListener((key, value, cause) -> {
                    try {
                        persist(value);
                    }catch (Exception e) {
                        Logs.error("persist eviction data [{}] error [{}]", Jsons.toJson(value), Jsons.toJson(e));
                    }
                })
                .executor(initExecutor())
                .build();

        // 开启持久化线程
        initScheduledExecutor().scheduleAtFixedRate(this::schedulePersist, schedulePersistDuration, schedulePersistDuration, schedulePersistTimeUnit);
    }

    private ThreadPoolTaskExecutor initExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(0);
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
        executor.setDaemon(false);
        // SynchronousQueue
        executor.setQueueCapacity(-1);
        executor.setThreadNamePrefix(EXECUTOR_NAME_PREFIX + Clocks.INSTANCE.currentTimeMillis());
        // TimeUnit.SECONDS
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    public ScheduledExecutorService initScheduledExecutor() {
        return new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread thread = new Thread(runnable, "Cache-Persist-Scheduled-Thread");
            thread.setDaemon(false);
            return thread;
        });
    }


    /**
     * 更新到期时间
     * @param value Host实体
     * @return 存活时间 duration 纳秒
     * @author jie.wang
     */
    private long updateExpiry(Host value) {
        if (value == null || VerifiableEnum.codeOf(HostFlagEnum.class, value.getFlag()) == null) {
            Logs.warn("illegal host value [{}]", Jsons.toJson(value));
            return 0;
        }

        HostFlagEnum flag = value.getFlag();
        if (flag == HostFlagEnum.DENIED || flag == HostFlagEnum.ALLOWED) {
            return Long.MAX_VALUE;
        }
        if (flag == HostFlagEnum.ALLOWED_WITH_EXPIRY) {
            long expiry = value.getExpiry() - Clocks.INSTANCE.currentTimeMillis();
            if (expiry > 0) {
                return TimeUnit.MILLISECONDS.toNanos(expiry);
            }else {
                // 针对 已到期 的缓存 10 分钟
                value.setFlag(HostFlagEnum.EXPIRED);
                return TimeUnit.MILLISECONDS.toNanos(expiredCacheDuration);
            }
        }
        // 针对 已到期/未授权 的缓存 10 分钟
        if (flag == HostFlagEnum.EXPIRED) {
            return TimeUnit.MILLISECONDS.toNanos(expiredCacheDuration);
        }
        return 0;
    }

    /**
     * 持久化被驱除的数据
     * @param value Host 实体
     * @author wang1
     */
    private void persist(Host value) {
        Logs.warn("Cached Host has been removed [{}]", Jsons.toJson(value));
        if (value == null || VerifiableEnum.codeOf(HostFlagEnum.class, value.getFlag()) == null) {
            return;
        }

        HostMapper mapper = SpringContexts.getSpringContext().getBean(HostMapper.class);
        if (mapper == null) {
            Logs.error("Can not find HostMapper");
            return;
        }
        HostFlagEnum flag = value.getFlag();
        // 需要持久化保存
        if (flag == HostFlagEnum.DENIED || flag == HostFlagEnum.ALLOWED) {
            mapper.upsert(value);
        }
        if (flag == HostFlagEnum.ALLOWED_WITH_EXPIRY
                && value.getExpiry() - Clocks.INSTANCE.currentTimeMillis() > 0) {
            mapper.upsert(value);
        }
        if (flag == HostFlagEnum.EXPIRED
                || (flag == HostFlagEnum.ALLOWED_WITH_EXPIRY && value.getExpiry() - Clocks.INSTANCE.currentTimeMillis() <= 0)) {
            mapper.deleteById(value.getIp());
        }
    }

    /**
     * 定时将缓存中的数据持久化存储
     * @author wang1
     */
    private void schedulePersist() {
        Logs.info("start persist cache data at {}", Clocks.INSTANCE.currentTimeMillis());
        HostMapper mapper = SpringContexts.getSpringContext().getBean(HostMapper.class);
        if (mapper == null) {
            Logs.error("Can not find HostMapper");
            return;
        }
        cache.asMap().values().stream()
                .filter(host -> host.getFlag() == HostFlagEnum.ALLOWED
                        || host.getFlag() == HostFlagEnum.DENIED
                        || (host.getFlag() == HostFlagEnum.EXPIRED && host.getExpiry() - Clocks.INSTANCE.currentTimeMillis() > 0))
                .forEach(mapper::upsert);
        Logs.info("finish persist cache data at {}", Clocks.INSTANCE.currentTimeMillis());
    }

    /**
     * 刷新缓存
     * @param hosts host数据
     * @param withClean 清除当前缓存中的数据
     * @author wang1
     */
    public void refresh(List<Host> hosts, boolean withClean) {
        if (CollectionUtils.isEmpty(hosts)) {
            return;
        }
        if (withClean) {
            cache.invalidateAll();
        }
        Map<String, Host> mappingIpToHost = hosts.stream()
                .filter(host -> StringUtils.isNotBlank(host.getIp())
                                && VerifiableEnum.codeOf(HostFlagEnum.class, host.getFlag()) != null)
                .collect(Collectors.toMap(Host::getIp, Function.identity()));
        cache.putAll(mappingIpToHost);
    }


    public void add(Host host) {
        if (host == null
                || StringUtils.isBlank(host.getIp())
                || VerifiableEnum.codeOf(HostFlagEnum.class, host.getFlag()) == null) {
            return;
        }
        cache.put(host.getIp(), host);
    }

    public Host get(String ip) {
        return cache.getIfPresent(ip);
    }

    public void remove(String ip) {
        cache.invalidate(ip);
    }
}
