package cc.wang1.frp.dto.frp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class User {
    @JsonProperty("user")
    private String user;
    @JsonProperty("metas")
    private Map<String, String> metas;
    @JsonProperty("run_id")
    private String runId;
}
