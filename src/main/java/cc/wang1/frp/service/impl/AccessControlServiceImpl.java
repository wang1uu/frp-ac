package cc.wang1.frp.service.impl;

import cc.wang1.frp.entity.Host;
import cc.wang1.frp.enums.HostFlagEnum;
import cc.wang1.frp.mapper.service.HostMapperService;
import cc.wang1.frp.service.AccessControlService;
import cc.wang1.frp.util.BloomFilters;
import cc.wang1.frp.util.Jsons;
import cc.wang1.frp.util.Logs;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AccessControlServiceImpl implements AccessControlService, InitializingBean {

    @Resource
    private HostMapperService hostMapperService;

    private volatile BloomFilters bloomFilters;

    @Override
    public boolean accessControl(String ip) {
        boolean result = bloomFilters.exist(ip);
        Logs.info("datetime [{}] access ip => [{}] forbidden => [{}]", LocalDateTime.now(), ip, result);
        return result;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 初始化布隆过滤器
        bloomFilters = new BloomFilters.Builder()
                .withSupplier(() -> {
                    List<String> data = hostMapperService.list(Wrappers.lambdaQuery(Host.class).eq(Host::getFlag, HostFlagEnum.DENIED.getCode()))
                            .stream()
                            .map(Host::getIp)
                            .collect(Collectors.toList());
                    Logs.info("初始化数据 [{}]", Jsons.toJson(data));
                    return data;
                })
                .withRebuildInterval(1L, TimeUnit.HOURS)
                .withFpp(0.00001)
                .withInsertions(100000)
                .build();
    }
}
