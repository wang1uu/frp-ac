package cc.wang1.frp.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("user")
public class User {

    @TableId
    private String userId;

    private String password;

    private String accessToken;

    private Long accessTokenExpiry;
}
