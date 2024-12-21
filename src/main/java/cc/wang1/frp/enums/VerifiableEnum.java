package cc.wang1.frp.enums;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * 可校验枚举值接口，使用枚举对象中的 code 属性值进行校验
 * @author jie.wang
 */
public interface VerifiableEnum<T> extends StandardEnum <T> {
    /**
     * 默认校验方法（校验是否存在）
     * @author jie.wang
     * @param value 待校验的值（Object）
     * @param required 是否必填
     * @return 校验结果标识（true|false）
     */
    @SuppressWarnings("all")
    default boolean verify(Object value, boolean required) {
        // 多值检查
        if (Objects.nonNull(value) && Collection.class.isAssignableFrom(value.getClass())) {
            return (((Collection) value).isEmpty() && !required) || ((Collection) value).stream().allMatch(this::verify);
        }
        // 单值检查
        return (Objects.isNull(value) && !required) || verify(value);
    }
    default boolean verify(Object value) {
        return Arrays.stream(this.getClass().getEnumConstants()).anyMatch(target -> String.valueOf(target.getCode()).equals(String.valueOf(value)));
    }
    /**
     * 获取指定枚举 code 的实例
     * @author jie.wang
     * @param enumType 枚举类 Class
     * @param value code值
     * @return 指定枚举实例
     */
    @SuppressWarnings("all")
    static <X> X codeOf(Class<X> enumType, Object value) {
        if (!enumType.isEnum() || !VerifiableEnum.class.isAssignableFrom(enumType) || Objects.isNull(value)) {
            return null;
        }
        return Arrays.stream(enumType.getEnumConstants())
                .filter(enumValue -> String.valueOf(((VerifiableEnum)enumValue).getCode()).equals(String.valueOf(value)))
                .findFirst().orElse(null);
    }

    /**
     * 获取指定枚举 code 的实例
     * @author jie.wang
     * @param enumType 枚举类 Class
     * @param value code值
     * @return 指定枚举实例
     */
    @SuppressWarnings("all")
    static <X> X codeOfOrDefalut(Class<X> enumType, Object value,X defalutValue) {
        if (!enumType.isEnum() || !VerifiableEnum.class.isAssignableFrom(enumType) || Objects.isNull(value)) {
            return defalutValue;
        }
        return Arrays.stream(enumType.getEnumConstants()).filter(enumValue -> String.valueOf(((VerifiableEnum)enumValue).getCode()).equals(String.valueOf(value))).findFirst().orElse(defalutValue);
    }
    /**
     * 指定 code 是否等于当前枚举的 code
     * @author jie.wang
     * @param target 需要检查的 code 值
     * @return true or false
     */
    default boolean verifyByCode(T target) {
        return Objects.nonNull(target) && String.valueOf(this.getCode()).equals(String.valueOf(target));
    }
}