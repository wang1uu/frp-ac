package cc.wang1.frp.dto.frp;

import lombok.Data;

import java.util.Map;

@Data
public class LoginOperationDTO {
    private String version;
    private String hostname;
    private String os;
    private String arch;
    private String user;
    private long timestamp;
    private String privilegeKey;
    private String runId;
    private int poolCount;
    private Map<String, String> metas;
    private String clientAddress;
}
