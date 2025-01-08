package cc.wang1.frp.dto.base;

import lombok.Builder;
import lombok.Data;

/**
 * 枚举 DTO
 * @author jie.wang
 */
@Data
@Builder
public class EnumDTO<T> {
    private T code;
    private String desc;
}
