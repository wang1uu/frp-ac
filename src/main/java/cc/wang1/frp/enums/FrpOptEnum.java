package cc.wang1.frp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FrpOptEnum implements VerifiableEnum<String>{
    Login("Login", "Login"),
    NewProxy("NewProxy", "NewProxy"),
    NewUserConn("NewUserConn", "NewUserConn"),
    NewWorkConn("NewUserConn", "NewUserConn");

    @JsonValue
    @EnumValue
    private final String code;
    private final String desc;
}
