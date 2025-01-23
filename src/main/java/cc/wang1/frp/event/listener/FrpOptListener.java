package cc.wang1.frp.event.listener;

import cc.wang1.frp.client.IPClient;
import cc.wang1.frp.dto.base.IPInfoResultDTO;
import cc.wang1.frp.event.FrpAccessEvent;
import cc.wang1.frp.util.BloomFilters;
import cc.wang1.frp.util.Jsons;
import cc.wang1.frp.util.Logs;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FrpOptListener {

    @Resource
    private IPClient ipClient;

    @Resource(name = "frpAccessIpBloomFilter")
    private BloomFilters bloomFilters;

    @Value("${host.ip-info-token}")
    private String ipInfoToken;

    @EventListener
    public void handleAccessEvent(FrpAccessEvent event) {
        Logs.info("handleAccessEvent: [{}]", Jsons.toJson(event));
        if (StringUtils.isBlank(event.getIp())) {
            return;
        }

        IPInfoResultDTO result = ipClient.queryIPInfo(event.getIp(), ipInfoToken);
        Logs.info("queryIPInfo: [{}]", Jsons.toJson(result));

        // 自动封禁非CN的IP
        if (!"CN".equals(result.getCountry())) {
            bloomFilters.block(event.getIp());
            Logs.info("auto block ip: [{}]", event.getIp());
        }
    }
}
