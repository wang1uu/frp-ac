package cc.wang1.frp.controller;

import cc.wang1.frp.controller.advice.ValidateToken;
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

    @ValidateToken
    @GetMapping("/refresh")
    public MessagePack<Boolean> refresh() {
        accessControlService.refresh();

        return MessagePack.<Boolean>builder()
                .status(MessagePack.StatusCode.SUCCESS)
                .build();
    }

    @ValidateToken
    @GetMapping("/block")
    public MessagePack<Boolean> block(@Param("ip") String ip) {
        accessControlService.block(ip);

        return MessagePack.<Boolean>builder()
                .status(MessagePack.StatusCode.SUCCESS)
                .build();
    }

    @ValidateToken
    @GetMapping("/unblock")
    public MessagePack<Boolean> unblock(@Param("ip") String ip) {
        accessControlService.unblock(ip);

        return MessagePack.<Boolean>builder()
                .status(MessagePack.StatusCode.SUCCESS)
                .build();
    }
}
