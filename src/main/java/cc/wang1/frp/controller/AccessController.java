package cc.wang1.frp.controller;

import cc.wang1.frp.dto.base.MessagePack;
import cc.wang1.frp.service.AccessControlService;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class AccessController {

    @Resource
    private AccessControlService accessControlService;

    @GetMapping("/blocked")
    public MessagePack<Boolean> publicCheck(@Param("ip") String ip) {
        return MessagePack.<Boolean>builder()
                .status(MessagePack.StatusCode.SUCCESS)
                .data(accessControlService.accessControl(ip))
                .build();
    }
}
