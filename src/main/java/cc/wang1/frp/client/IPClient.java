package cc.wang1.frp.client;

import cc.wang1.frp.dto.base.IPInfoResultDTO;
import cc.wang1.frp.util.Jsons;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import feign.*;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@FeignClient(name = "ipClient", url = "${host.ip-info}", configuration = IPClient.IPClientConfig.class)
public interface IPClient {

    // mapping ip to ipInfo
    // ip查询结果缓存一天
    Cache<String, Response> cache = Caffeine.newBuilder()
            .maximumSize(1000000)
            .expireAfterWrite(Duration.ofDays(1))
            .build();

    @GetMapping
    IPInfoResultDTO queryIPInfo(@RequestParam("ip") String ip, @RequestParam("token") String token);


    class IPClientConfig {

        @Value("${host.ip-info-token}")
        private String token;

        private static final String IP_PARAM = "ip";

        private final Client defaultClient = new Client.Default(null, null);

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public Logger.Level feignLevel() {
            return Logger.Level.FULL;
        }

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public RequestInterceptor requestInterceptor() {
            return new RequestInterceptor() {
                @Override
                public void apply(RequestTemplate requestTemplate) {
                    String ip = requestTemplate.queries().get(IP_PARAM).toArray(new String[0])[0];
                    requestTemplate.header("Accept", "application/json");
                    requestTemplate.uri(ip, true);
                }
            };
        }

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public Decoder handleJsonMimeType() {
            return (response, type) -> {
                if (!(type instanceof Class) && !(type instanceof ParameterizedType) && !(type instanceof WildcardType)) {
                    throw new DecodeException(response.status(), "type is not an instance of Class or ParameterizedType: " + type, response.request());
                } else {
                    String bodyStr = Util.toString(response.body().asReader(Util.UTF_8));
                    return StringUtils.isEmpty(bodyStr) ? null : Jsons.toBean(bodyStr, type);
                }
            };
        }

        @Bean
        @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
        public Client cacheFeignClient() {
            return new Client() {
                @Override
                public Response execute(Request request, Request.Options options) throws IOException {
                    String ip = request.requestTemplate().queries().get(IP_PARAM).toArray(new String[0])[0];
                    Response cachedResult = cache.getIfPresent(ip);
                    if (cachedResult != null) {
                        return cachedResult;
                    }

                    Response result = defaultClient.execute(request, options);
                    // 包装响应体流为可重复读取
                    InputStream cachedInputStream = new ByteArrayInputStream(result.body().asInputStream().readAllBytes());
                    Response.Body wrappedBody = new Response.Body() {
                        @Override
                        public void close() throws IOException {
                            cachedInputStream.reset();
                        }
                        @Override
                        public Integer length() {
                            return result.body().length();
                        }
                        @Override
                        public boolean isRepeatable() {
                            return true;
                        }
                        @Override
                        public InputStream asInputStream() {
                            return cachedInputStream;
                        }
                        @Override
                        public Reader asReader(Charset charset) {
                            return new InputStreamReader(cachedInputStream, charset != null ? charset : StandardCharsets.UTF_8);
                        }
                    };
                    Response response = Response.builder()
                            .status(result.status())
                            .reason(result.reason())
                            .headers(result.headers())
                            .request(request)
                            .body(wrappedBody)
                            .build();

                    cache.put(ip, response);
                    return response;
                }
            };
        }
    }
}
