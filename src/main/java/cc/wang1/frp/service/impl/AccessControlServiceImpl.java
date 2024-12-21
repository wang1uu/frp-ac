package cc.wang1.frp.service.impl;

import cc.wang1.frp.entity.Host;
import cc.wang1.frp.enums.HostFlagEnum;
import cc.wang1.frp.mapper.service.HostMapperService;
import cc.wang1.frp.service.AccessControlService;
import cc.wang1.frp.util.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 访问控制
 * @author wang1
 */
@Service
public class AccessControlServiceImpl implements AccessControlService, InitializingBean {

    @Resource
    private HostMapperService hostMapperService;

    private BloomFilters ipBloomFilter;

    private HostCaches hostCaches;


    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化黑名单布隆过滤器
        ipBloomFilter = new BloomFilters.Builder()
                .withRebuildInterval(1L, TimeUnit.HOURS)
                .withSupplier(() -> {
                    LambdaQueryWrapper<Host> condition = Wrappers.lambdaQuery(Host.class)
                            .select(Host::getIp)
                            .eq(Host::getFlag, HostFlagEnum.DENIED);
                    return hostMapperService.list(condition).stream()
                            .map(Host::getIp)
                            .collect(Collectors.toList());
                })
                .build();

        // 初始化缓存
        hostCaches = new HostCaches(10, TimeUnit.MINUTES, 1, TimeUnit.HOURS, 10000000);
        LambdaQueryWrapper<Host> condition = Wrappers.lambdaQuery(Host.class)
                .eq(Host::getFlag, HostFlagEnum.DENIED)
                .or()
                .eq(Host::getFlag, HostFlagEnum.ALLOWED)
                .or()
                .eq(Host::getFlag, HostFlagEnum.ALLOWED_WITH_EXPIRY);
        hostCaches.refresh(hostMapperService.list(condition), false);
    }

    @Override
    public boolean accessControl(String ip) {
        if (ipBloomFilter == null || hostCaches == null) {
            Logs.warn("Ac not finish initialization.");
            return false;
        }
        // 黑名单拒绝访问
        if (ipBloomFilter.contains(ip)) {
            Logs.warn("Host [{}] has been denied.", ip);
            return false;
        }

        Host host = hostCaches.get(ip);

        //  未授权的禁止访问
        if (host == null) {
            Logs.warn("Host [{}] not exist.", ip);
            Host targetHost = Host.builder()
                    .ip(ip)
                    .flag(HostFlagEnum.EXPIRED)
                    .createdBy("Ac System")
                    .updatedBy("Ac System")
                    .lastAccessTime(Clocks.INSTANCE.currentTimeMillis())
                    .build();
            hostCaches.add(targetHost);
            Logs.info("cache host [{}]", Jsons.toJson(targetHost));
            return false;
        }

        HostFlagEnum flag = host.getFlag();
        if (flag == HostFlagEnum.ALLOWED
                || (flag == HostFlagEnum.ALLOWED_WITH_EXPIRY && host.getExpiry() - Clocks.INSTANCE.currentTimeMillis() > 0)) {
            Logs.info("host [{}] access has been allowed.", Jsons.toJson(host));
            return true;
        }
        if (flag == HostFlagEnum.ALLOWED_WITH_EXPIRY && host.getExpiry() - Clocks.INSTANCE.currentTimeMillis() <= 0) {
            Logs.info("host [{}] access has been expired.", Jsons.toJson(host));

            host.setFlag(HostFlagEnum.EXPIRED);
            hostCaches.add(host);

            return false;
        }
        if (flag == HostFlagEnum.EXPIRED || flag == HostFlagEnum.DENIED) {
            Logs.info("host [{}] has been denied.", Jsons.toJson(host));
        }
        return false;
    }

    public void allow(String ip, long expiry, String note) {
        if (StringUtils.isBlank(ip)) {
            Logs.warn("allow operation failed because ip is empty.");
            return;
        }
    }
}
