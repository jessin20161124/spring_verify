package com.jessin.practice.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.OptionHelper;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zexin.guo
 * @link https://shift-alt-ctrl.iteye.com/blog/2394366
 * @create 2019-02-13 下午9:24
 **/
public class CommonLogService {
    public static void main(String[] args) {
        Logger logger = LoggerBuilder.getLogger("hello");
        logger.info("老司机来也");
    }
}

class LoggerBuilder {

    private static final Map<String, Logger> container = new HashMap<>();

    public static Logger getLogger(String name) {
        Logger logger = container.get(name);
        if (logger != null) {
            return logger;
        }
        synchronized (LoggerBuilder.class) {
            logger = container.get(name);
            if (logger != null) {
                return logger;
            }
            logger = build(name);
            container.put(name, logger);
        }
        return logger;
    }

    private static Logger build(String name) {
        // 这个是logback的上下文，包含所有logger的缓存
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        // 从这个LoggerContext拿出来的Logger，会添加到缓存中，并且遍历时会从最后子logger到父logger进行遍历，如com.xxx.flight -> com.xxx -> com
        // 对每个logger，写入日志到该logger的appender中，如果当中某个logger的additive为false，则停止，否则继续向父logger转发
        Logger logger = context.getLogger("FILE-" + name);
        // 是否向上传递，如果向上，则会打印出来
        logger.setAdditive(true);

        /**
         *  <appender name="default"
         class="ch.qos.logback.core.rolling.RollingFileAppender">
         <File>${log.dir}/spring_verify.log</File>
         <prudent>false</prudent>
         <Append>true</Append>
         <encoder>
         <pattern>${sta_pattern}</pattern>
         <charset>${encoding}</charset>
         </encoder>
         <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
         <fileNamePattern>${log.dir}/spring_verify.%d{yyyy-MM-dd-HH}.log
         </fileNamePattern>
         </rollingPolicy>
         <maxHistory>7</maxHistory>
         </appender>
         */
        RollingFileAppender appender = new RollingFileAppender();
        appender.setContext(context);
        // appender的名字
        appender.setName("FILE-" + name);
        // OptionHelper可以替代系统属性，如${catalina.base}
        appender.setFile(OptionHelper.substVars("logs/web-log-" + name + ".log", context));
        appender.setAppend(true);
        appender.setPrudent(false);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd/HH:mm:ss.SSS}|%X{localIp}|[%t] %-5level %logger{50} %line - %m%n");
        encoder.setCharset(Charset.defaultCharset());
        encoder.start();
        // 设置appender编码规则
        appender.setEncoder(encoder);

        // 归档策略
        SizeAndTimeBasedRollingPolicy policy = new SizeAndTimeBasedRollingPolicy();
        String fp = OptionHelper.substVars("logs/web-log-" + name + ".log.%d{yyyy-MM-dd}.%i", context);
        policy.setMaxFileSize(FileSize.valueOf("128MB"));
        policy.setFileNamePattern(fp);
        policy.setMaxHistory(15);
        policy.setTotalSizeCap(FileSize.valueOf("32GB"));
        policy.setParent(appender);
        policy.setContext(context);
        policy.start();
        // appender设置滚动策略
        appender.setRollingPolicy(policy);

        // appender必须开启，且放最后
        appender.start();
        // 核心方法是添加appender，最后日志通过appender写入
        logger.addAppender(appender);
        return logger;
    }
}