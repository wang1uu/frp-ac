package cc.wang1.frp.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * 线程池相关配置
 * @author wang1
 */
@Configuration
public class Executors {

    public static final int AVAILABLE_PROCESSORS_COUNT = Runtime.getRuntime().availableProcessors();


    /**
     * 使用一个临时的单线程线程池执行异步任务
     * <br/><b>临时线程池在空闲后会被自动关闭并GC<b/>
     * @author wang1
     * @param task 异步任务
     * @param threadPrefix 线程名称前缀
     * @param beforeHook 执行线程初始化方法
     */
    public static CompletableFuture<Void> runAsyncWithAutoGC(Runnable task, String threadPrefix, BiConsumer<Thread, Runnable> beforeHook) {
        if (task == null) {
            return CompletableFuture.completedFuture(null);
        }

        String threadName = StringUtils.isBlank(threadPrefix)
                ? System.currentTimeMillis() + "-default-temp-single-thread-executor"
                : threadPrefix + "-temp-single-thread-executor";

        ThreadPoolExecutor executor = new ThreadPoolExecutor(0,
                1,
                0,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setDaemon(false);
                    thread.setName(threadName);
                    return thread;
                },
                new ThreadPoolExecutor.AbortPolicy()) {
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                if (beforeHook != null) {
                    try {
                        beforeHook.accept(t, r);
                    } catch (Exception e) {
                        Logs.info("线程 [{}] 执行beforeHook失败，[{}]", t.getName(), Jsons.toJson(e));
                    }
                }
                super.beforeExecute(t, r);
            }
        };
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown, "ShutdownHook-" + threadName));
        return CompletableFuture.runAsync(task, executor);
    }
}

