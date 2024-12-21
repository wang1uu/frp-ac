package cc.wang1.frp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HostFlagEnum implements VerifiableEnum<Integer> {
    /**
     * 拒绝访问
     */
    DENIED(1, "denied"),
    /**
     * 始终允许
     */
    ALLOWED(2, "allowed"),
    /**
     * 临时允许
     */
    ALLOWED_WITH_EXPIRY(3, "allowedWithExpiry"),
    /**
     * 失效的
     */
    EXPIRED(4, "expired");

    @JsonValue
    @EnumValue
    private final Integer code;
    private final String desc;
}
