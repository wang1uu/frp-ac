package cc.wang1.frp.enums;

/**
 * 标准枚举
 * @author jie.wang
 */
public interface StandardEnum<T> {
    /**
     * 获取code属性值
     * @author jie.wang
     * @return code 字段值
     */
    T getCode();
    /**
     * 获取desc描述信息
     * @author jie.wang
     * @return desc 字段值
     */
    String getDesc();
}

