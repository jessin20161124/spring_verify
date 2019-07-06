package com.jessin;

import com.google.common.collect.Lists;
import com.jessin.practice.bean.User;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * @author zexin.guo
 * @create 2019-02-05 上午11:50
 **/
public class StreamTest {
    private User user = new User();

    @Before
    public void before() {
        user.setName("tom");
    }

    @Test
    public void predicateTest() {
        // 前半句是入参，后半句必须是返回布尔值
        Predicate<User> predicate = user -> user.getName().equals("tom");
        System.out.println(predicate.test(user));
    }

    @Test
    public void functionTest() {
        // 前半句是入参，后半句必须返回String
        Function<User, String> function = user -> user.toString();
        System.out.println(function.apply(user));
    }

    @Test
    public void supplierTest() {
        // 前半句是无参，后半句返回user
        Supplier<User> supplier = () -> new User();
        // 方法引用也可以
        Supplier<User> supplier2 = User::new;
        System.out.println(supplier.get());
    }

    @Test
    public void consumerTest() {
        // 前半句是入参，后半句是语句，没有返回值，一般是一段逻辑的处理
        Consumer<User> consumer = user -> System.out.println(user);
        Consumer<User> consumer2 = user -> user.getName();
        consumer.accept(user);
    }

    /**
     * 扁平化操作
     */
    @Test
    public void flatMapTest() {
        User user1 = new User();
        user1.setName("xiaoming");
        User user2 = new User();
        user2.setName("xiaohong");
        // flatMap中function的第二个参数是stream，最后会把所有的stream合并在一起
        Stream<User> userStream = Stream.of(user1, user2);
        userStream.map(user -> user.getName())
                .flatMap(name -> Stream.of(name.split(""))).forEach(System.out::println);
    }

    @Test
    public void peekTest() {
        Stream<String> stream = Stream.of("hello", "world");
        // peek是对每个元素进行消费一次，是中间操作，而foreach是终端操作
        // 最后带一个终端操作才会触发中间操作
        // 多个中间操作，最后是终端操作
        stream.peek(str -> System.out.println(str)).collect(toList());
    }

    /**
     * 流只能有一次终端操作，操作后不能继续操作
     */
    @Test
    public void biFunctionTest() {
        User user1 = new User();
        user1.setName("xiaoming");
        User user2 = new User();
        user2.setName("xiaohong");
        List<User> userList = Lists.newArrayList(user1, user2);
        // 使用name作为比较器
        Collections.sort(userList,
                Comparator.comparing(user -> user.getName()));
        userList.stream().forEach(System.out::println);
    }

    @Test
    public void streamCloseTest() {
        Stream<String> stringStream = Stream.of("hello", "world");
        stringStream.collect(toList());
        // 失败
        stringStream.collect(toList());
    }

    @Test
    public void testBiConsumer() {
//        BiConsumer<Integer, Integer> biConsumer = this::add;
//        biConsumer.accept(1, 2);

          BiConsumer<A, Integer> biConsumer1 = A::print;
//        BiConsumer<A, Integer> biConsumer1 = this::add;

        biConsumer1.accept(new A(), 10);
    }

    public void add(int a, int b) {
        System.out.println(a + b);
    }
    public void add(A a, int b) {
        a.print(b);
    }

    class A {
        public void print(int a) {
            System.out.println(a);
        }
    }
}
