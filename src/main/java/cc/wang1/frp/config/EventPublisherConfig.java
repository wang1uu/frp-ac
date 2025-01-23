package cc.wang1.frp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

import javax.annotation.Resource;
import java.util.concurrent.Executor;

@Configuration
public class EventPublisherConfig {

    @Resource(name = "frpEventPublishExecuteThreadPool")
    private Executor frpEventPublishExecuteThreadPool;

    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(frpEventPublishExecuteThreadPool);
        return eventMulticaster;
    }
}
