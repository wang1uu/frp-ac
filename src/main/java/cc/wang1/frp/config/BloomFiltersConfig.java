package cc.wang1.frp.config;

import cc.wang1.frp.entity.Host;
import cc.wang1.frp.enums.HostFlagEnum;
import cc.wang1.frp.mapper.service.HostMapperService;
import cc.wang1.frp.util.BloomFilters;
import cc.wang1.frp.util.Jsons;
import cc.wang1.frp.util.Logs;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Configuration
public class BloomFiltersConfig {

    @Resource
    private HostMapperService hostMapperService;

    @Resource
    private BloomFiltersConfigProperties bloomFiltersConfigProperties;

    @Bean("frpAccessIpBloomFilter")
    public BloomFilters bloomFilter() {
        return new BloomFilters.Builder()
                .withSupplier(() -> {
                    List<String> data = hostMapperService.list(Wrappers.lambdaQuery(Host.class).eq(Host::getFlag, HostFlagEnum.DENIED.getCode()))
                            .stream()
                            .map(Host::getIp)
                            .collect(Collectors.toList());
                    Logs.info("初始化数据 [{}]", Jsons.toJson(data));
                    return data;
                })
                .withRebuildInterval(bloomFiltersConfigProperties.getRebuildInterval(), TimeUnit.SECONDS)
                .withFpp(bloomFiltersConfigProperties.getFpp())
                .withInsertions(bloomFiltersConfigProperties.getInsertions())
                .build();
    }
}
