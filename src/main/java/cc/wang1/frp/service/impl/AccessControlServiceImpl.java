package cc.wang1.frp.service.impl;

import cc.wang1.frp.dto.frp.NewUserConnDTO;
import cc.wang1.frp.dto.frp.ValidatedResultDTO;
import cc.wang1.frp.enums.FrpOptEnum;
import cc.wang1.frp.enums.VerifiableEnum;
import cc.wang1.frp.event.FrpAccessEvent;
import cc.wang1.frp.mapper.service.HostMapperService;
import cc.wang1.frp.service.AccessControlService;
import cc.wang1.frp.util.BloomFilters;
import cc.wang1.frp.util.Jsons;
import cc.wang1.frp.util.Logs;
import cc.wang1.frp.util.SpringContexts;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class AccessControlServiceImpl implements AccessControlService {

    @Resource
    private HostMapperService hostMapperService;

    @Resource(name = "frpAccessIpBloomFilter")
    private BloomFilters bloomFilters;

    @Override
    public boolean accessControl(String ip) {
        boolean result = bloomFilters.exist(ip);
        Logs.info("datetime [{}] access ip => [{}] forbidden => [{}]", LocalDateTime.now(), ip, result);
        return result;
    }

    @Override
    public void refresh() {
        Logs.info("datetime [{}] refresh manually", LocalDateTime.now());
        bloomFilters.rebuild();
    }

    @Override
    public void block(String ip) {
        Logs.info("block [{}] at [{}]", ip, LocalDateTime.now());
        bloomFilters.block(ip);
    }

    @Override
    public void unblock(String ip) {
        Logs.info("unblock [{}] at [{}]", ip, LocalDateTime.now());
        bloomFilters.unblock(ip);
    }

    @Override
    public ValidatedResultDTO<?> frpAc(HttpServletRequest request) throws IOException {
        final String data = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        Logs.info("[FRP AC] [{}] remote address [{}] request [{}]", LocalDateTime.now(), request.getRemoteAddr(), data);
        if (StringUtils.isBlank(data)) {
            return ValidatedResultDTO.builder().reject(true).build();
        }

        try {
            final JsonNode jsonData = Jsons.readTree(data);
            final FrpOptEnum frpOptEnum;
            if (jsonData.get("op") == null || (frpOptEnum = VerifiableEnum.codeOf(FrpOptEnum.class, jsonData.get("op").asText())) == null) {
                return ValidatedResultDTO.builder().build();
            }

            if (frpOptEnum == FrpOptEnum.NewUserConn) {
                NewUserConnDTO content = Jsons.toBean(jsonData.get("content").toPrettyString(), NewUserConnDTO.class);
                if (content == null || StringUtils.isBlank(content.getRemoteAddr())) {
                    return ValidatedResultDTO.builder()
                            .reject(true)
                            .reject_reason("illegal request.")
                            .build();
                }

                String[] ipAndPort = content.getRemoteAddr().split(":");
                if (ipAndPort.length != 2) {
                    return ValidatedResultDTO.builder()
                            .reject(true)
                            .reject_reason("illegal request.")
                            .build();
                }

                if (bloomFilters.exist(ipAndPort[0])) {
                    return ValidatedResultDTO.builder()
                            .reject(true)
                            .reject_reason("your request has been blocked.")
                            .build();
                }

                // 检查IP
                SpringContexts.getSpringContext().publishEvent(new FrpAccessEvent(this, ipAndPort[0]));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return ValidatedResultDTO.builder().reject(false).build();
    }
}
