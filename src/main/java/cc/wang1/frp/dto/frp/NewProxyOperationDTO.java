package cc.wang1.frp.dto.frp;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NewProxyOperationDTO {
    private User user;
    private String proxyName;
    private String proxyType;
    private boolean useEncryption;
    private boolean useCompression;
    private String bandwidthLimit;
    private String bandwidthLimitMode;
    private String group;
    private String groupKey;

    // For tcp and udp only
    private int remotePort;

    // For http and https only
    private List<String> customDomains;
    private String subdomain;
    private String locations;
    private String httpUser;
    private String httpPwd;
    private String hostHeaderRewrite;
    private Map<String, String> headers;

    // For stcp only
    private String sk;

    // For tcpmux only
    private String multiplexer;

    private Map<String, String> metas;
}
