package cc.wang1.frp.controller.advice;

import cc.wang1.frp.dto.base.MessagePack;
import cc.wang1.frp.entity.User;
import cc.wang1.frp.mapper.service.UserMapperService;
import cc.wang1.frp.util.Clocks;
import cc.wang1.frp.util.SpringContexts;
import com.google.common.hash.Hashing;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Component
public class AccessTokenControllerAdvisor extends AbstractPointcutAdvisor {

    private static final String PARAMETER_NAME = "access_token";

    public static class AccessTokenValidationException extends RuntimeException {
        public AccessTokenValidationException(String message) {
            super(message);
        }
    }

    @Override
    public Pointcut getPointcut() {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(Method method, Class<?> targetClass) {
                return method.isAnnotationPresent(ValidateToken.class)
                        && method.getReturnType().isAssignableFrom(MessagePack.class);
            }
        };
    }

    @Override
    public Advice getAdvice() {
        return (MethodInterceptor) invocation -> {
            try {
                String accessToken = "";
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (servletRequestAttributes == null
                        || (StringUtils.isBlank(accessToken = servletRequestAttributes.getRequest().getParameter(PARAMETER_NAME))
                            && StringUtils.isBlank(accessToken = servletRequestAttributes.getRequest().getHeader(PARAMETER_NAME)))) {
                    throw new AccessTokenValidationException(String.format("Access Token [%s] 不存在或已失效", accessToken));
                }

                accessToken = Hashing.murmur3_128().hashBytes(accessToken.getBytes()).toString();
                User user = SpringContexts.getSpringContext().getBean(UserMapperService.class).validateAccessToken(accessToken);
                if (user == null || user.getAccessTokenExpiry() < Clocks.INSTANCE.currentTimeMillis()) {
                    throw new AccessTokenValidationException(String.format("Access Token [%s] 不存在或已失效", accessToken));
                }

                return invocation.proceed();
            }catch (AccessTokenValidationException e) {
                MessagePack messagePack = (MessagePack) invocation.getMethod().getReturnType().newInstance();
                messagePack.setStatus(MessagePack.StatusCode.ILLEGAL_ACCESS);
                messagePack.setMessage("blocked.");
                return messagePack;
            }
        };
    }
}
