package cc.wang1.frp.util;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 布隆过滤器
 * @author wang1
 */
public class BloomFilters {

    /**
     * 预估数据量
     */
    private int INSERTIONS;

    /**
     * 判重错误率
     */
    private double FPP;

    /**
     * 数据填充方法
     */
    private Supplier<List<String>> supplier;

    /**
     * 重建周期
     */
    private Long rebuildInterval;

    /**
     * 是否需要重建
     */
    private final AtomicBoolean dirty = new AtomicBoolean(false);

    /**
     * guava 布隆过滤器
     */
    private AtomicReference<BloomFilter<String>> bloomFilter = new AtomicReference<>();

    /**
     * 异步重建任务处理
     */
    private final ScheduledExecutorService executorService;


    private BloomFilters(Builder builder) {
        this.INSERTIONS = builder.INSERTIONS;
        this.FPP = builder.FPP;
        this.supplier = builder.supplier;
        this.rebuildInterval = builder.rebuildInterval;
        this.executorService = new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread systemClock = new Thread(runnable, "Bloom Filter Rebuild Thread");
            systemClock.setDaemon(true);
            return systemClock;
        });
        executorService.scheduleAtFixedRate(
                this::rebuild,
                0,
                rebuildInterval,
                TimeUnit.MILLISECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(executorService::shutdown, "Bloom Filter Rebuild Thread Released"));
    }

    public boolean contains(String key) {
        return Optional.ofNullable(bloomFilter.get()).map(b -> b.mightContain(key)).orElse(false);
    }

    public void put(String key) {
        if (bloomFilter.get() == null) {
            return;
        }
        bloomFilter.get().put(key);
    }

    /**
     * 重建缓存
     */
    public void rebuild() {
        if (!dirty.get()) {
            Logs.info("There is nothing to rebuild");
            return;
        }

        long start = Clocks.INSTANCE.currentTimeMillis();
        Logs.info("Bloom Filter Rebuild Start At [{}]", start);

        // TODO: 2024/12/20 22:19 这里感觉会丢数据
        BloomFilter<String> oldBloomFilter = bloomFilter.get();
        // 保留旧数据
        BloomFilter<String> newBloomFilter = Optional.ofNullable(oldBloomFilter)
                .map(BloomFilter::copy)
                .orElse(BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), INSERTIONS, FPP));

        List<String> data = supplier.get();
        if (!CollectionUtils.isEmpty(data)) {
            data.stream().filter(StringUtils::hasText).forEach(newBloomFilter::put);
        }else {
            Logs.warn("Bloom Filter Rebuild With Empty Data");
        }

        if (!bloomFilter.compareAndSet(oldBloomFilter, newBloomFilter)) {
            Logs.warn("Bloom Filter Concurrent Rebuild  Failed");
        }

        long end = Clocks.INSTANCE.currentTimeMillis();
        Logs.info("Bloom Filter Rebuild End At [{}] Cost [{}] millis", end, end - start);
    }

    /**
     * Builder
     */
    public static class Builder {

        private int INSERTIONS = 1000000;
        private double FPP = 0.00001;
        private Supplier<List<String>> supplier;
        private Long rebuildInterval = TimeUnit.MINUTES.toMillis(30);

        /**
         * 设置预估数据量
         */
        public Builder withInsertions(int insertions) {
            this.INSERTIONS = insertions;
            return this;
        }

        /**
         * 设置判重错误率
         */
        public Builder withFpp(double fpp) {
            this.FPP = fpp;
            return this;
        }

        /**
         * 设置数据填充方法
         */
        public Builder withSupplier(Supplier<List<String>> supplier) {
            if (supplier == null) {
                throw new IllegalArgumentException("supplier is required");
            }
            this.supplier = supplier;
            return this;
        }

        /**
         * 设置重建周期
         */
        public Builder withRebuildInterval(Long rebuildInterval, TimeUnit unit) {
            if (rebuildInterval == null || unit == null || rebuildInterval <= 0) {
                throw new IllegalArgumentException("rebuildInterval must be greater than 0");
            }
            if (unit.toMillis(rebuildInterval) < TimeUnit.MINUTES.toMillis(10)) {
                Logs.warn("Bloom Filter will Rebuild Interval [{}] less than 10 minutes", rebuildInterval);
            }
            this.rebuildInterval = unit.toMillis(rebuildInterval);
            return this;
        }

        public BloomFilters build() {
            if (supplier == null) {
                throw new IllegalArgumentException("supplier is required");
            }
            return new BloomFilters(this);
        }
    }
}
