package cc.wang1.frp.dto.base;

import cc.wang1.frp.controller.serializer.StandardEnumJsonSerializer;
import cc.wang1.frp.enums.VerifiableEnum;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessagePack<T> implements Serializable {

    private static final long serialVersionUID = -4124613117037133474L;

    @Getter
    @AllArgsConstructor
    public static enum StatusCode implements VerifiableEnum<Integer> {
        // 成功 code >= 0
        SUCCESS(0, "success"),

        // 异常 code < 0
        ERROR(-1, "system error"),
        ILLEGAL_ACCESS(-2, "illegal access"),;

        @JsonValue
        @EnumValue
        private final Integer code;
        private final String desc;
    }

    @JsonSerialize(using = StandardEnumJsonSerializer.class)
    private StatusCode status;
    private T data;

    private String message;
    public String getMessage() {
        return StringUtils.isNoneBlank(message) ? message : Optional.ofNullable(status).map(StatusCode::getDesc).orElse("");
    }


}
