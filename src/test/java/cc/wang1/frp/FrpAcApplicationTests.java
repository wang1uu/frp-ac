package cc.wang1.frp;

import cc.wang1.frp.client.IPClient;
import cc.wang1.frp.dto.frp.NewUserConnDTO;
import cc.wang1.frp.entity.Host;
import cc.wang1.frp.enums.HostFlagEnum;
import cc.wang1.frp.enums.VerifiableEnum;
import cc.wang1.frp.mapper.service.HostMapperService;
import cc.wang1.frp.util.BloomFilters;
import cc.wang1.frp.util.Clocks;
import cc.wang1.frp.util.Jsons;
import cc.wang1.frp.util.Logs;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.benmanes.caffeine.cache.*;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;


@ActiveProfiles("dev")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = FrpAcApplication.class)
class FrpAcApplicationTests {

    @Resource
    private HostMapperService hostMapperService;

    @Value("${host.ip-info-token}")
    private String token;

    @Resource
    private IPClient ipClient;

    @Test
    public void test01() {
        System.out.println(hostMapperService.getById(1));
    }

    @Test
    public void test02() {
        Host host = Host.builder()
                .ip("192.168.1.1")
                .flag(HostFlagEnum.DENIED)
                .build();

        hostMapperService.getBaseMapper().upsert(host);
    }

    @Test
    public void test03() {
        new ScheduledThreadPoolExecutor(1, runnable -> {
            Thread systemClock = new Thread(runnable, "Bloom Filter Rebuild Thread");
            systemClock.setDaemon(false);
            return systemClock;
        }).scheduleAtFixedRate(() -> {
            System.out.println(Clocks.INSTANCE.currentTimeMillis());
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
        }, 0, 1, TimeUnit.SECONDS);

        LockSupport.park();
    }

    @Test
    public void test04() {
        BloomFilters bloomFilter = new BloomFilters.Builder()
                .withSupplier(() -> {
                    return Arrays.asList("a", "b", "c", "d", "e");
                })
                .build();

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));
    }

    @Test
    public void test05() {
        Cache<String, Host> cache = Caffeine.newBuilder()
                .maximumSize(10)
                .evictionListener(new RemovalListener<String, Host>() {
                    @Override
                    public void onRemoval(@Nullable String key, @Nullable Host value, RemovalCause cause) {
                        Logs.warn("Cached Host has been removed [{}]", Jsons.toJson(value));
                        if (value == null || VerifiableEnum.codeOf(HostFlagEnum.class, value.getFlag()) == null) {
                            return;
                        }

                        HostFlagEnum flag = value.getFlag();
                        // 需要持久化保存
                        if (flag == HostFlagEnum.DENIED || flag == HostFlagEnum.ALLOWED) {

                        }
                        if (flag == HostFlagEnum.ALLOWED_WITH_EXPIRY
                                && value.getExpiry() - Clocks.INSTANCE.currentTimeMillis() > 0) {

                        }
                    }
                })
                .build();

        new Expiry<String, Host>() {

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
                        return TimeUnit.MINUTES.toNanos(10);
                    }
                }
                // 针对 已到期/未授权 的缓存 10 分钟
                if (flag == HostFlagEnum.EXPIRED) {
                    return TimeUnit.MINUTES.toNanos(10);
                }
                return 0;
            }

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
        };
    }

    @Test
    public void test06() {
        JsonNode jsonData = Jsons.readTree("{\"version\":\"0.1.0\",\"op\":\"NewUserConn\",\"content\":{\"user\":{\"user\":\"\",\"metas\":null,\"run_id\":\"673ff655461998ee\"},\"proxy_name\":\"demo\",\"proxy_type\":\"tcp\",\"remote_addr\":\"172.21.224.65:55412\"}}\n");

        System.out.println(jsonData.get("op").asText());
        System.out.println(jsonData.get("content").toString());
    }

    @Test
    public void test07() {
        String data = "{\"user\":{\"user\":\"\",\"metas\":null,\"run_id\":\"59aec435356e44f0\"},\"proxy_name\":\"demo\",\"proxy_type\":\"tcp\",\"remote_addr\":\"172.21.224.65:63717\"}";
        NewUserConnDTO bean = Jsons.toBean(data, NewUserConnDTO.class);
        System.out.println(bean);
    }

    @Test
    public void test08() {
        System.out.println(ipClient.queryIPInfo("60.204.153.242", token));
        System.out.println(ipClient.queryIPInfo("60.204.153.242", token));
    }
}
