package cc.wang1.frp.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Optional;

@Data
@Builder
public class MessagePack<T> implements Serializable {

    private static final long serialVersionUID = -4124613117037133474L;

    @Getter
    @AllArgsConstructor
    public static enum StatusCode {
        // 成功 code >= 0
        SUCCESS(0, "success"),

        // 异常 code < 0
        ERROR(-1, "system error");

        @JsonValue
        private final Integer code;
        private final String desc;
    }

    private StatusCode statusCode = StatusCode.SUCCESS;
    private T data;

    private String message;
    public String getMessage() {
        return StringUtils.isNoneBlank(message) ? message : Optional.ofNullable(statusCode).map(StatusCode::getDesc).orElse("");
    }


}
