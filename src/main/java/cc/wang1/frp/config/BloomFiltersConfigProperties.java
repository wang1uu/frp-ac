package cc.wang1.frp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("bloomfilter")
public class BloomFiltersConfigProperties {

    private Long rebuildInterval;

    private Double fpp;

    private Integer Insertions;
}
