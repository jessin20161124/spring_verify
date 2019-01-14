package com.jessin.practice.listener;

import com.jessin.practice.event.HelloEvent;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Created by zexin.guo on 17-8-6.
 */
@Component
public class HelloListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloListener.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");

    /**
     * TODO @EventListener和@Order都在EventListenerMethodProcessor进行处理
     * @param event
     */
    @EventListener
    @Order(1)
    @Async("executor")
    public void onApplicationEvent(HelloEvent event) {
        LOGGER.info("先收到事件 : {} ", event.getSource());
    }

    @EventListener
    @Order(2)
    @Async("executor")
    public void onApplicationEvent2(String event) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("后收到事件 : {} ", event);
    }
}
