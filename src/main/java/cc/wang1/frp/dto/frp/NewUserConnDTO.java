package cc.wang1.frp.dto.frp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class NewUserConnDTO {
    @JsonProperty("user")
    private User user;
    @JsonProperty("proxy_name")
    private String proxyName;
    @JsonProperty("proxy_type")
    private String proxyType;
    @JsonProperty("remote_addr")
    private String remoteAddr;
}
