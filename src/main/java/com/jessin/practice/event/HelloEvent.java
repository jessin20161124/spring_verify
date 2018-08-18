package com.jessin.practice.event;

import org.springframework.context.ApplicationEvent;

/**
 * Created by zexin.guo on 17-8-6.
 */
public class HelloEvent extends ApplicationEvent {
    public HelloEvent(Object source) {
        super(source);
    }

}
