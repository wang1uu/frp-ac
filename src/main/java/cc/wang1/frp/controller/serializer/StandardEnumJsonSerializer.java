package cc.wang1.frp.controller.serializer;

import cc.wang1.frp.dto.base.EnumDTO;
import cc.wang1.frp.enums.StandardEnum;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

/**
 * 标准枚举序列化器
 * @author jie.wang
 */
public class StandardEnumJsonSerializer extends JsonSerializer<StandardEnum> {

    @Override
    public void serialize(StandardEnum standardEnum, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes == null
                || requestAttributes.getRequest() == null
                || requestAttributes.getRequest().getRequestURI() == null
                || standardEnum == null) {
            return;
        }

        // 序列化 json 数据
        jsonGenerator.writeObject(EnumDTO.builder()
                .code(standardEnum.getCode())
                .desc(standardEnum.getDesc())
                .build());
    }
}