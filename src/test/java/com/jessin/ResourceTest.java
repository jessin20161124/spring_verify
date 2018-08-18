package com.jessin;

import com.jessin.practice.service.AbstractService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author zexin.guo
 * @create 2018-07-15 下午5:30
 **/
public class ResourceTest {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void test1() {
        // 相对于某个类的url，在同一个路径下
        // 如果是/开头，则是相对于根路径
        Resource resource = new ClassPathResource("hello.txt", AbstractService.class);
        try {
            InputStream inputStream = resource.getInputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while((len = inputStream.read(buffer)) != -1) {
                for (int i = 0; i < len; i++) {
                    logger.info("{}", buffer[i]);
                }
            }
            logger.info("url : {}", resource.getURL());
            logger.info("uri : {}", resource.getFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        // 相对于某个类的url，在同一个路径下
        // 如果是/开头，则是相对于根路径
        Resource resource = new FileSystemResource("/home/jessin/hello.txt");
        try {
            InputStream inputStream = resource.getInputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while((len = inputStream.read(buffer)) != -1) {
                for (int i = 0; i < len; i++) {
                    logger.info("{}", buffer[i]);
                }
            }
            logger.info("url : {}", resource.getURL());
            logger.info("uri : {}", resource.getFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test3()  {
        byte[] b = new byte[] {1, 2, 3};
        Resource resource = new ByteArrayResource(b);
        try {
            logger.info("{}", resource.contentLength());
            logger.info("{}", resource.exists());
            logger.info("{}", resource.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
