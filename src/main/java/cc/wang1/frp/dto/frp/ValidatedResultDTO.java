package cc.wang1.frp.dto.frp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Frp 校验请求实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidatedResultDTO<T> {
    @Builder.Default
    private boolean reject = false;
    @Builder.Default
    private String reject_reason = "";
    @Builder.Default
    private boolean unchange = true;
    private T content;
}
