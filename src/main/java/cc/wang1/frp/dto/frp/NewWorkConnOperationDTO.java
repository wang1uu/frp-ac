package cc.wang1.frp.dto.frp;

import lombok.Data;

@Data
public class NewWorkConnOperationDTO {
    private User user;
    private String runId;
    private long timestamp;
    private String privilegeKey;
}
