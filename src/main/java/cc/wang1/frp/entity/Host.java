package cc.wang1.frp.entity;

import cc.wang1.frp.enums.HostFlagEnum;
import cc.wang1.frp.util.Clocks;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * 主机实体类
 */
@Data
@Builder
@TableName("host")
public class Host {

    /**
     * IP 地址
     */
    @TableId("ip")
    private String ip;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 状态
     */
    private HostFlagEnum flag;

    /**
     * 访问到期时间（毫秒时间戳）
     */
    private Long expiry;

    /**
     * 最后访问时间
     */
    private Long lastAccessTime;

    /**
     * 备注
     */
    private String note;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @Builder.Default
    private Long createdTime = Clocks.INSTANCE.currentTimeMillis();

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 最后更新时间
     */
    @Builder.Default
    private Long updatedTime = Clocks.INSTANCE.currentTimeMillis();
}
