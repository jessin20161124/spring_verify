# spring_verify
## Spring 4.2.7相关模块源码学习

#### 一、基本方法
- 类和方法设计相关
1. 关注函数的命名，是否见名知义，如返回值。
2. 关注函数是否只干了一件事，是否符合单一职能。
3. 关注函数的强校验（快速失败）和异常处理，包括处理不了的非法情况直接抛异常，自定义异常的抛出。抛出自定义异常，并捕获。
4. 关注类的设计，接口的职能，是否只干了一个方面的事，是否符合开闭原则，开闭原则主要是指子类可以修改父类的逻辑，不用动老代码，可以扩展。（通过protected实现）。模板方法
5. 关注类的用法，相关public函数的用法，如何避免抛出异常，是配合使用还是单独使用。
6. 关注接口整体的设计，理清接口之间的关系。关注整体的UML图，手动画类图。关注包结构。关注代码分块换行。
7. 关注泛型的使用。
- 阅读调试相关
8. 一步步debug源码。
9. 先看构造函数、属性值，再看public方法，再看protected方法，最后看private方法。重点是先看覆盖或者实现的方法。
10. [看下官方文档](https://docs.spring.io/spring/docs/4.2.7.RELEASE/spring-framework-reference/htmlsingle/#classic-aop-prog)。
11. 通过在正常代码中抛出异常，看到异常栈，从而看到具体的执行过程。
12. 尝试实现一个接口，或者使用其他类，用起来。或者写点测试用例。
13. 线程安全
- 不可变类
- 单线程使用（局部变量）
- 加锁
14. 内部类一般放在最后

#### 二、TODO LIST
1. BeanFactoryPostProcessor只对所在BeanFactory生效，父类的BeanPostProcessor是否会注入到子类中？如@Value("${abc:123456}")
- BeanPostProcessor，包括BeanFactoryPostProcessor都只在当前BeanFactory生效，不会在子容器中生效。
- BeanDefinitionRegistryPostProcessor(BeanFactoryPostProcessor)在org.springframework.context.support.AbstractApplicationContext#invokeBeanFactoryPostProcessors被调用，而BeanPostProcessor在org.springframework.context.support.AbstractApplicationContext#registerBeanPostProcessors被注册到BeanPostProcessor中。随后引用到bean的创建过程中。
2. @Resource按照名字还是属性注入？
- @Resource private User user;默认user这个beanName不存在bean时，会按照类型进行注入。
- 如果指定了注入的名字，@Resource(name="userImpl")，则只寻找对应名字的bean，不存在则抛出异常。

3. BinderFactory的使用，针对每个binder有一个BinderFactory?
- 调用一次HandlerMethod时，只创建一个BinderFactory。
- 针对一个函数入参，该BinderFactory会生成一个DataBinder，绑定入参。
4. @ResponseBody用HttpMessageConverter进行转换，@RequestBody也是？可以多个吗？用的是哪个参数解析器，返回值转换器？
- RequestResponseBodyMethodProcessor。HttpMessageConverter主要在这个类使用。起作用的是MappingJackson2HttpMessageConverter。默认处理application/json等类型。支持的media type在构造函数中传递。
- 先格式化，再转化。参数解析器可以分为按照类解析和按照注解(@RequestParam/@ModelAttribute)解析。@RequestParam进行普通类型绑定，只调用Binder.convertIfNecessary()
- @ModelAttribute进行复杂类型绑定，会自动实例化复杂model（List是接口，无法实例化，会抛异常），可以实例化List属性，且实例类型为ArrayList，会调用Binder.bind()
5. 测试工单代码下META-INF/spring.handlers中的NamespaceUri
```JSON
{
    "http://code.alibabatech.com/schema/dubbo":"com.alibaba.dubbo.config.spring.schema.DubboNamespaceHandler",
    "http://www.springframework.org/schema/aop":"org.springframework.aop.config.AopNamespaceHandler",
    "http://www.springframework.org/schema/oxm":"org.springframework.oxm.config.OxmNamespaceHandler",
    "http://www.springframework.org/schema/task":"org.springframework.scheduling.config.TaskNamespaceHandler",
    "http://mybatis.org/schema/mybatis-spring":"org.mybatis.spring.config.NamespaceHandler",
    "http://www.springframework.org/schema/lang":"org.springframework.scripting.config.LangNamespaceHandler",
    "http://www.springframework.org/schema/c":"org.springframework.beans.factory.xml.SimpleConstructorNamespaceHandler",
    "http://www.qunar.com/schema/qconfig":"qunar.tc.qconfig.client.spring.QConfigNamespaceHandler",
    "http://www.springframework.org/schema/jee":"org.springframework.ejb.config.JeeNamespaceHandler",
    "http://www.springframework.org/schema/jms":"org.springframework.jms.config.JmsNamespaceHandler",
    "http://www.springframework.org/schema/cache":"org.springframework.cache.config.CacheNamespaceHandler",
    "http://www.qunar.com/schema/qmq":"qunar.tc.qmq.consumer.annotation.QmqClientNamespaceHandler",
    "http://www.springframework.org/schema/jdbc":"org.springframework.jdbc.config.JdbcNamespaceHandler",
    "http://www.springframework.org/schema/p":"org.springframework.beans.factory.xml.SimplePropertyNamespaceHandler",
    "http://www.springframework.org/schema/util":"org.springframework.beans.factory.xml.UtilNamespaceHandler",
    "http://www.qunar.com/schema/qschedule":"qunar.tc.qschedule.config.spring.QScheduleNamespaceHandler",
    "http://www.springframework.org/schema/tx":"org.springframework.transaction.config.TxNamespaceHandler",
    "http://www.springframework.org/schema/context":"org.springframework.context.config.ContextNamespaceHandler",
    "http://www.springframework.org/schema/mvc":"org.springframework.web.servlet.config.MvcNamespaceHandler"
}
```
6. ViewResolver将对应的逻辑视图名转化为真正的view
7. 组件之间的关系、职能。
- HandlerMapping
- HandlerAdapter
- HanlderMethodArgumentResolver
- BeanWrapper
- PropertyAccessor
- DataBinder

8. 检测＠ControllerAdvice类上的方法：@ModelAttribute/@InitBinder/或者该类实现RequestBodyAdvice ResponseBodyAdvice
9. ControllerAdviceBean : 在指定的包内或者是可分配的类型或者有某个注解，则可以使用该ControllerAdvice；没有任何条件，适用任何bean
10. spring中的设计模式：工厂模式、适配器模式、模板方法模式、delegate模式、门面模式、组合模式、访问者模式。
11. spring异步请求的尝试。
- 处理器方法返回Callable/WebAsyncTask等即可。但是Servlet需要3.0及以上，filter和servlet均需配置支持异步。需要配置一个单独的业务线程池来处理任务。
- 返回值是通过CallableMethodReturnValueHandler或者AsyncTaskMethodReturnValueHandler处理的。

12. 在spring 处理器方法上标注@ResponseStatus
13. 常见的BeanPostProcessor是在哪注入的？
14. 写一个从请求开始到响应前端的全链路的流程。
15. @applicationContext注入的是父类的还是子类的？
- 注入的是当前所在容器。
16. 获取某个class的所有bean时，如何避免初始化所有的bean？
17. 当前BeanFactory不存在该bean时，尝试从父BeanFactory中获取。
18. beanFactory.getBean(name, Advisor.class)，如果name不是Advisor类型，这里会抛出异常？
- 见org.springframework.aop.framework.autoproxy.BeanFactoryAdvisorRetrievalHelper#findAdvisorBeans。如果name不存在对应的bean，会抛出异常。
19. Spring bean各个阶段使用的BeanPostProcessor。
20. 构造函数实例化bean过程查看。constructor-arg。
21. 尝试使用FactoryBean。
22. BeanWrapperImpl如何注入诸如ref类型的依赖，Resource[]类型的依赖。
- 见applyPropertyValues，使用BeanDefinitionValueResolver。
24. 先使用CustomEditor进行转化，再使用ConversionService进行转化，最后兜底使用默认的PropertyEditor进行转化。
25. 子容器中注入的是DefaultFormattingConversionService？父容器注入的默认是SimpleTypeConverter？
- mvc:annotation-driven默认注入DefaultFormattingConversionService到ConfigurableWebBindingInitializer中，该bean是内部bean，不会暴露到BeanFactory中。
- 由于没有设置TypeConverter类型的bean，所以父子容器默认用的是SimpleTypeConverter。
26. 尝试使用@Value("${excelPath}") private Resource localExcel，并在properties中配置excelPath条目，注意localExcel的流是否最后关闭了？
27. DefaultListableBeanFactory#registerResolvableDependency，查看手动注入了哪些依赖。
- 见org.springframework.context.support.AbstractApplicationContext#refresh中的prepareBeanFactory/postProcessBeanFactory。
28. 尝试使用原生的作用域生成bean
29. 尝试实现一套框架。
30. beanName的转化，如&helloService，&是否在获取bean的时候先去掉？
- 先去掉&，再转化别名为标准名，最后会判断原始name是否包含&，从获取bean。
31. ConfigurationClassPostProcessor的实现过程。
32. getNamesForType时，FactoryBean是如何getType的，需要实例化？
33. 聚集相同的语句。
34. Bean配置中使用的TypeConverter是BeanWrapperImpl，还是BeanFactory中的TypeConverter？配置一个bean xml，尝试注入一个date属性。
35. ApplicationContext相对BeanFactory的扩展功能
- 注入BeanFactoryPostProcessor、BeanPostProcessor
- 添加资源国际化的功能。
- 添加事件发布和监听的功能
- 提前加载单例非懒加载的bean的功能。
- 生命周期处理器。

#### 三、Kafka笔记
1.  使用doGet方法来实现真正的逻辑，比较复杂的判断调用get方法，如加缓存，把两者区别开。
2. 留下protected方法，给子类覆盖，保证可扩展。
3. 不要覆盖函数的入参的值，使用新的变量。
4. synchronized加在局部块中，能使得锁的粒度更小。synchronized的对象可以是任意的，但是该对象在多个线程中用来同步。
5. ConcurrentHashMap是一个分段锁，对于map内部操作是线程安全的，但是对于外部操作不是线程安全的。
6. 工具类声明为abstract，可以防止新建实例。
7. 全局变量要考虑线程安全，如HashMap不是线程安全的。使用ConcurrentHashMap，大部分操作不用synchronized，但是小部分需要，原因看4。
8. 可以根据某个类的需要建立工具类，如BeanFactoryUtils
9. Map可以作为缓存。Guava缓存不用考虑线程安全。
10. 集合对象作为全局变量时，一般直接初始化，并且给出大小。Map容量大小一般为幂次。
11. 接口的方法名比较符合规范，声明可能抛出的异常，包括运行时异常。
13. catch住，记下监控，重新throw ex，避免重复记录日志，关闭资源。一些不合法的情况直接抛出自定义异常。
14. 使用string[0]返回空数组
15. 若不想引入jar包，使用class.forName方式来加载类，构造实例。判断类是否存在。
16. 使用反射newInstance()时，不知道具体有多少个参数，可以使用无参构造函数，然后该实例实现configurable接口，传递Map作为参数即可实现多个参数的初始化。
17. 异常信息越具体越好：throw new IllegalArgumentException("Unknown compression type: " + type);
18. 引入一个中间层，如果不够再引入一个。
19. ArrayDeque底层是一个数组，且长度为2的幂次，双向队列，两头都可以进队和出队。
20. AtomicInteger是线程安全的，原子的。
21. Collections.emptyMap()
22. 将set转化为List，可以使用new ArrayList(Collection)方法，几乎每个集合都有以collection作为入参的构造函数。
23. 返回多个结果，使用数据包装：
```
/*
     * Metadata about a record just appended to the record accumulator
     */
     // 内部类
    public final static class RecordAppendResult {
        public final FutureRecordMetadata future;
        public final boolean batchIsFull;
        public final boolean newBatchCreated;

        public RecordAppendResult(FutureRecordMetadata future, boolean batchIsFull, boolean newBatchCreated) {
            this.future = future;
            this.batchIsFull = batchIsFull;
            this.newBatchCreated = newBatchCreated;
        }
    }
    // 外部调用
     RecordAccumulator.RecordAppendResult result = accumulator.append(tp, timestamp, serializedKey, serializedValue, interceptCallback, remainingWaitMs);
            if (result.batchIsFull || result.newBatchCreated) {
                log.trace("Waking up the sender since topic {} partition {} is either full or getting a new batch", record.topic(), partition);
                this.sender.wakeup();
            }
            return result.future;
```

24. byteBuffer封装了大端和小端，rewind使得position=0，一般在SocketChannel.read(byteBuffer)读满的时候使用，这时byteBuffer可以被发送，或者被拷贝到另一个byteBuffer中。

25. KafkaChannel的成员包括TransporyLayer这个属性，从构造函数中传入.还有NetworkReceive和Send这两个属性，其中NetWorkReceive只会被设置一次，NetWorkSend在发送完后可以重新set。NetworkReceive底层封装了

26. Selector#connect的写法：
```
 @Override
    public void connect(String id, InetSocketAddress address, int sendBufferSize, int receiveBufferSize) throws IOException {
        if (this.channels.containsKey(id))
            throw new IllegalStateException("There is already a connection for id " + id);

        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        Socket socket = socketChannel.socket();
        socket.setKeepAlive(true);
        if (sendBufferSize != Selectable.USE_DEFAULT_BUFFER_SIZE)
            socket.setSendBufferSize(sendBufferSize);
        if (receiveBufferSize != Selectable.USE_DEFAULT_BUFFER_SIZE)
            socket.setReceiveBufferSize(receiveBufferSize);
        socket.setTcpNoDelay(true);
        boolean connected;
        try {
            connected = socketChannel.connect(address);
        } catch (UnresolvedAddressException e) {
            socketChannel.close();
            throw new IOException("Can't resolve address: " + address, e);
        } catch (IOException e) {
            socketChannel.close();
            throw e;
        }
        SelectionKey key = socketChannel.register(nioSelector, SelectionKey.OP_CONNECT);
        KafkaChannel channel = channelBuilder.buildChannel(id, key, maxReceiveSize);
        key.attach(channel);
        this.channels.put(id, channel);

        if (connected) {
            // OP_CONNECT won't trigger for immediately connected channels
            log.debug("Immediately connected to node {}", channel.id());
            immediatelyConnectedKeys.add(key);
            key.interestOps(0);
        }
    }
    
    // 关闭操作：
     /**
     * Closes this channel
     *
     * @throws IOException If I/O error occurs
     */
    @Override
    public void close() throws IOException {
        try {
            socketChannel.socket().close();
            socketChannel.close();
        } finally {
            key.attach(null);
            key.cancel();
        }
    }
```

27. SelectionKey.attach(socketChannel)可以存放数据到这个key中，到时通过attachment()方法返回。
```
/**
     * Get the channel associated with selectionKey
     */
    private KafkaChannel channel(SelectionKey key) {
        return (KafkaChannel) key.attachment();
    }
```
28. LinkedHashMap实现LRU操作：
```
        // initial capacity and load factor are default, we set them explicitly because we want to set accessOrder = true
        this.lruConnections = new LinkedHashMap<>(16, .75F, true);
```
29. 注册到大管家selector后，可以得到SelectionKey，再从SelectionKey得到socketChannel。其中selector处于上层，放在Kafka的Selector中。
30. 比较经典的静态内部类的使用：专门管理和设置节点连接的状态。体现了单一职能和高度的内聚性。这里的静态内部类是私有的。

```
package org.apache.kafka.clients;

import java.util.HashMap;
import java.util.Map;

/**
 * The state of our connection to each node in the cluster.
 * 
 */
final class ClusterConnectionStates {
    private final long reconnectBackoffMs;
    private final Map<String, NodeConnectionState> nodeState;

    public ClusterConnectionStates(long reconnectBackoffMs) {
        this.reconnectBackoffMs = reconnectBackoffMs;
        this.nodeState = new HashMap<String, NodeConnectionState>();
    }

    /**
     * Return true iff we can currently initiate a new connection. This will be the case if we are not
     * connected and haven't been connected for at least the minimum reconnection backoff period.
     * @param id The connection id to check
     * @param now The current time in MS
     * @return true if we can initiate a new connection
     */
    public boolean canConnect(String id, long now) {
        NodeConnectionState state = nodeState.get(id);
        if (state == null)
            return true;
        else
            return state.state == ConnectionState.DISCONNECTED && now - state.lastConnectAttemptMs >= this.reconnectBackoffMs;
    }

    /**
     * Return true if we are disconnected from the given node and can't re-establish a connection yet
     * @param id The connection to check
     * @param now The current time in ms
     */
    public boolean isBlackedOut(String id, long now) {
        NodeConnectionState state = nodeState.get(id);
        if (state == null)
            return false;
        else
            return state.state == ConnectionState.DISCONNECTED && now - state.lastConnectAttemptMs < this.reconnectBackoffMs;
    }

    /**
     * Returns the number of milliseconds to wait, based on the connection state, before attempting to send data. When
     * disconnected, this respects the reconnect backoff time. When connecting or connected, this handles slow/stalled
     * connections.
     * @param id The connection to check
     * @param now The current time in ms
     */
    public long connectionDelay(String id, long now) {
        NodeConnectionState state = nodeState.get(id);
        if (state == null) return 0;
        long timeWaited = now - state.lastConnectAttemptMs;
        if (state.state == ConnectionState.DISCONNECTED) {
            return Math.max(this.reconnectBackoffMs - timeWaited, 0);
        } else {
            // When connecting or connected, we should be able to delay indefinitely since other events (connection or
            // data acked) will cause a wakeup once data can be sent.
            return Long.MAX_VALUE;
        }
    }

    /**
     * Enter the connecting state for the given connection.
     * @param id The id of the connection
     * @param now The current time.
     */
    public void connecting(String id, long now) {
        nodeState.put(id, new NodeConnectionState(ConnectionState.CONNECTING, now));
    }

    /**
     * Return true iff a specific connection is connected
     * @param id The id of the connection to check
     */
    public boolean isConnected(String id) {
        NodeConnectionState state = nodeState.get(id);
        return state != null && state.state == ConnectionState.CONNECTED;
    }

    /**
     * Enter the connected state for the given connection
     * @param id The connection identifier
     */
    public void connected(String id) {
        NodeConnectionState nodeState = nodeState(id);
        nodeState.state = ConnectionState.CONNECTED;
    }

    /**
     * Enter the disconnected state for the given node
     * @param id The connection we have disconnected
     * @param now The current time
     */
    public void disconnected(String id, long now) {
        NodeConnectionState nodeState = nodeState(id);
        nodeState.state = ConnectionState.DISCONNECTED;
        nodeState.lastConnectAttemptMs = now;
    }

    /**
     * Remove the given node from the tracked connection states. The main difference between this and `disconnected`
     * is the impact on `connectionDelay`: it will be 0 after this call whereas `reconnectBackoffMs` will be taken
     * into account after `disconnected` is called.
     *
     * @param id The connection to remove
     */
    public void remove(String id) {
        nodeState.remove(id);
    }
    
    /**
     * Get the state of a given connection
     * @param id The id of the connection
     * @return The state of our connection
     */
    public ConnectionState connectionState(String id) {
        return nodeState(id).state;
    }
    
    /**
     * Get the state of a given node
     * @param id The connection to fetch the state for
     */
    private NodeConnectionState nodeState(String id) {
        NodeConnectionState state = this.nodeState.get(id);
        if (state == null)
            throw new IllegalStateException("No entry found for connection " + id);
        return state;
    }
    
    /**
     * The state of our connection to a node
     */
    private static class NodeConnectionState {

        ConnectionState state;
        long lastConnectAttemptMs;

        public NodeConnectionState(ConnectionState state, long lastConnectAttempt) {
            this.state = state;
            this.lastConnectAttemptMs = lastConnectAttempt;
        }

        public String toString() {
            return "NodeState(" + state + ", " + lastConnectAttemptMs + ")";
        }
    }
}

package org.apache.kafka.clients.consumer.internals;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A class for tracking the topics, partitions, and offsets for the consumer. A partition
 * is "assigned" either directly with {@link #assignFromUser(Collection)} (manual assignment)
 * or with {@link #assignFromSubscribed(Collection)} (automatic assignment from subscription).
 *
 * Once assigned, the partition is not considered "fetchable" until its initial position has
 * been set with {@link #seek(TopicPartition, long)}. Fetchable partitions track a fetch
 * position which is used to set the offset of the next fetch, and a consumed position
 * which is the last offset that has been returned to the user. You can suspend fetching
 * from a partition through {@link #pause(TopicPartition)} without affecting the fetched/consumed
 * offsets. The partition will remain unfetchable until the {@link #resume(TopicPartition)} is
 * used. You can also query the pause state independently with {@link #isPaused(TopicPartition)}.
 *
 * Note that pause state as well as fetch/consumed positions are not preserved when partition
 * assignment is changed whether directly by the user or through a group rebalance.
 *
 * This class also maintains a cache of the latest commit position for each of the assigned
 * partitions. This is updated through {@link #committed(TopicPartition, OffsetAndMetadata)} and can be used
 * to set the initial fetch position (e.g. {@link Fetcher#resetOffset(TopicPartition)}.
 */
public class SubscriptionState {

    /* the pattern user has requested */
    private Pattern subscribedPattern;

    /* the list of topics the user has requested */
    private final Set<String> subscription;

    /* the list of topics the group has subscribed to (set only for the leader on join group completion) */
    private final Set<String> groupSubscription;

    /* the list of partitions the user has requested */
    private final Set<TopicPartition> userAssignment;

    /* the list of partitions currently assigned */
    private final Map<TopicPartition, TopicPartitionState> assignment;

    /* do we need to request a partition assignment from the coordinator? */
    private boolean needsPartitionAssignment;

    /* do we need to request the latest committed offsets from the coordinator? */
    private boolean needsFetchCommittedOffsets;

    /* Default offset reset strategy */
    private final OffsetResetStrategy defaultResetStrategy;

    /* Listener to be invoked when assignment changes */
    private ConsumerRebalanceListener listener;

    private static final String SUBSCRIPTION_EXCEPTION_MESSAGE =
        "Subscription to topics, partitions and pattern are mutually exclusive";

    public SubscriptionState(OffsetResetStrategy defaultResetStrategy) {
        this.defaultResetStrategy = defaultResetStrategy;
        this.subscription = new HashSet<>();
        this.userAssignment = new HashSet<>();
        this.assignment = new HashMap<>();
        this.groupSubscription = new HashSet<>();
        this.needsPartitionAssignment = false;
        this.needsFetchCommittedOffsets = true; // initialize to true for the consumers to fetch offset upon starting up
        this.subscribedPattern = null;
    }

    public void subscribe(Collection<String> topics, ConsumerRebalanceListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("RebalanceListener cannot be null");

        if (!this.userAssignment.isEmpty() || this.subscribedPattern != null)
            throw new IllegalStateException(SUBSCRIPTION_EXCEPTION_MESSAGE);

        this.listener = listener;

        changeSubscription(topics);
    }

    public void changeSubscription(Collection<String> topicsToSubscribe) {
        if (!this.subscription.equals(new HashSet<>(topicsToSubscribe))) {
            this.subscription.clear();
            this.subscription.addAll(topicsToSubscribe);
            this.groupSubscription.addAll(topicsToSubscribe);
            this.needsPartitionAssignment = true;

            // Remove any assigned partitions which are no longer subscribed to
            for (Iterator<TopicPartition> it = assignment.keySet().iterator(); it.hasNext(); ) {
                TopicPartition tp = it.next();
                if (!subscription.contains(tp.topic()))
                    it.remove();
            }
        }
    }

    /**
     * Add topics to the current group subscription. This is used by the group leader to ensure
     * that it receives metadata updates for all topics that the group is interested in.
     * @param topics The topics to add to the group subscription
     */
    public void groupSubscribe(Collection<String> topics) {
        if (!this.userAssignment.isEmpty())
            throw new IllegalStateException(SUBSCRIPTION_EXCEPTION_MESSAGE);
        this.groupSubscription.addAll(topics);
    }

    public void needReassignment() {
        this.groupSubscription.retainAll(subscription);
        this.needsPartitionAssignment = true;
    }

    /**
     * Change the assignment to the specified partitions provided by the user,
     * note this is different from {@link #assignFromSubscribed(Collection)}
     * whose input partitions are provided from the subscribed topics.
     */
    public void assignFromUser(Collection<TopicPartition> partitions) {
        if (!this.subscription.isEmpty() || this.subscribedPattern != null)
            throw new IllegalStateException(SUBSCRIPTION_EXCEPTION_MESSAGE);

        this.userAssignment.clear();
        this.userAssignment.addAll(partitions);

        for (TopicPartition partition : partitions)
            if (!assignment.containsKey(partition))
                addAssignedPartition(partition);

        this.assignment.keySet().retainAll(this.userAssignment);

        this.needsPartitionAssignment = false;
    }

    /**
     * Change the assignment to the specified partitions returned from the coordinator,
     * note this is different from {@link #assignFromUser(Collection)} which directly set the assignment from user inputs
     */
    public void assignFromSubscribed(Collection<TopicPartition> assignments) {
        for (TopicPartition tp : assignments)
            if (!this.subscription.contains(tp.topic()))
                throw new IllegalArgumentException("Assigned partition " + tp + " for non-subscribed topic.");
        this.assignment.clear();
        for (TopicPartition tp: assignments)
            addAssignedPartition(tp);
        this.needsPartitionAssignment = false;
    }

    public void subscribe(Pattern pattern, ConsumerRebalanceListener listener) {
        if (listener == null)
            throw new IllegalArgumentException("RebalanceListener cannot be null");

        if (!this.subscription.isEmpty() || !this.userAssignment.isEmpty())
            throw new IllegalStateException(SUBSCRIPTION_EXCEPTION_MESSAGE);

        this.listener = listener;
        this.subscribedPattern = pattern;
    }

    public boolean hasPatternSubscription() {
        return subscribedPattern != null;
    }

    public void unsubscribe() {
        this.subscription.clear();
        this.userAssignment.clear();
        this.assignment.clear();
        this.needsPartitionAssignment = true;
        this.subscribedPattern = null;
    }


    public Pattern getSubscribedPattern() {
        return this.subscribedPattern;
    }

    public Set<String> subscription() {
        return this.subscription;
    }

    public Set<TopicPartition> pausedPartitions() {
        HashSet<TopicPartition> paused = new HashSet<>();
        for (Map.Entry<TopicPartition, TopicPartitionState> entry : assignment.entrySet()) {
            final TopicPartition tp = entry.getKey();
            final TopicPartitionState state = entry.getValue();
            if (state.paused) {
                paused.add(tp);
            }
        }
        return paused;
    }

    /**
     * Get the subscription for the group. For the leader, this will include the union of the
     * subscriptions of all group members. For followers, it is just that member's subscription.
     * This is used when querying topic metadata to detect the metadata changes which would
     * require rebalancing. The leader fetches metadata for all topics in the group so that it
     * can do the partition assignment (which requires at least partition counts for all topics
     * to be assigned).
     * @return The union of all subscribed topics in the group if this member is the leader
     *   of the current generation; otherwise it returns the same set as {@link #subscription()}
     */
    public Set<String> groupSubscription() {
        return this.groupSubscription;
    }

    private TopicPartitionState assignedState(TopicPartition tp) {
        TopicPartitionState state = this.assignment.get(tp);
        if (state == null)
            throw new IllegalStateException("No current assignment for partition " + tp);
        return state;
    }

    public void committed(TopicPartition tp, OffsetAndMetadata offset) {
        assignedState(tp).committed(offset);
    }

    public OffsetAndMetadata committed(TopicPartition tp) {
        return assignedState(tp).committed;
    }

    public void needRefreshCommits() {
        this.needsFetchCommittedOffsets = true;
    }

    public boolean refreshCommitsNeeded() {
        return this.needsFetchCommittedOffsets;
    }

    public void commitsRefreshed() {
        this.needsFetchCommittedOffsets = false;
    }

    public void seek(TopicPartition tp, long offset) {
        assignedState(tp).seek(offset);
    }

    public Set<TopicPartition> assignedPartitions() {
        return this.assignment.keySet();
    }

    public Set<TopicPartition> fetchablePartitions() {
        Set<TopicPartition> fetchable = new HashSet<>();
        for (Map.Entry<TopicPartition, TopicPartitionState> entry : assignment.entrySet()) {
            if (entry.getValue().isFetchable())
                fetchable.add(entry.getKey());
        }
        return fetchable;
    }

    public boolean partitionsAutoAssigned() {
        return !this.subscription.isEmpty();
    }

    public void position(TopicPartition tp, long offset) {
        assignedState(tp).position(offset);
    }

    public Long position(TopicPartition tp) {
        return assignedState(tp).position;
    }

    public Map<TopicPartition, OffsetAndMetadata> allConsumed() {
        Map<TopicPartition, OffsetAndMetadata> allConsumed = new HashMap<>();
        for (Map.Entry<TopicPartition, TopicPartitionState> entry : assignment.entrySet()) {
            TopicPartitionState state = entry.getValue();
            if (state.hasValidPosition())
                allConsumed.put(entry.getKey(), new OffsetAndMetadata(state.position));
        }
        return allConsumed;
    }

    public void needOffsetReset(TopicPartition partition, OffsetResetStrategy offsetResetStrategy) {
        assignedState(partition).awaitReset(offsetResetStrategy);
    }

    public void needOffsetReset(TopicPartition partition) {
        needOffsetReset(partition, defaultResetStrategy);
    }

    public boolean hasDefaultOffsetResetPolicy() {
        return defaultResetStrategy != OffsetResetStrategy.NONE;
    }

    public boolean isOffsetResetNeeded(TopicPartition partition) {
        return assignedState(partition).awaitingReset();
    }

    public OffsetResetStrategy resetStrategy(TopicPartition partition) {
        return assignedState(partition).resetStrategy;
    }

    public boolean hasAllFetchPositions() {
        for (TopicPartitionState state : assignment.values())
            if (!state.hasValidPosition())
                return false;
        return true;
    }

    public Set<TopicPartition> missingFetchPositions() {
        Set<TopicPartition> missing = new HashSet<>();
        for (Map.Entry<TopicPartition, TopicPartitionState> entry : assignment.entrySet())
            if (!entry.getValue().hasValidPosition())
                missing.add(entry.getKey());
        return missing;
    }

    public boolean partitionAssignmentNeeded() {
        return this.needsPartitionAssignment;
    }

    public boolean isAssigned(TopicPartition tp) {
        return assignment.containsKey(tp);
    }

    public boolean isPaused(TopicPartition tp) {
        return isAssigned(tp) && assignedState(tp).paused;
    }

    public boolean isFetchable(TopicPartition tp) {
        return isAssigned(tp) && assignedState(tp).isFetchable();
    }

    public void pause(TopicPartition tp) {
        assignedState(tp).pause();
    }

    public void resume(TopicPartition tp) {
        assignedState(tp).resume();
    }

    private void addAssignedPartition(TopicPartition tp) {
        this.assignment.put(tp, new TopicPartitionState());
    }

    public ConsumerRebalanceListener listener() {
        return listener;
    }

    private static class TopicPartitionState {
        private Long position; // last consumed position
        private OffsetAndMetadata committed;  // last committed position
        private boolean paused;  // whether this partition has been paused by the user
        private OffsetResetStrategy resetStrategy;  // the strategy to use if the offset needs resetting

        public TopicPartitionState() {
            this.paused = false;
            this.position = null;
            this.committed = null;
            this.resetStrategy = null;
        }

        private void awaitReset(OffsetResetStrategy strategy) {
            this.resetStrategy = strategy;
            this.position = null;
        }

        public boolean awaitingReset() {
            return resetStrategy != null;
        }

        public boolean hasValidPosition() {
            return position != null;
        }

        private void seek(long offset) {
            this.position = offset;
            this.resetStrategy = null;
        }

        private void position(long offset) {
            if (!hasValidPosition())
                throw new IllegalStateException("Cannot set a new position without a valid current position");
            this.position = offset;
        }

        private void committed(OffsetAndMetadata offset) {
            this.committed = offset;
        }

        private void pause() {
            this.paused = true;
        }

        private void resume() {
            this.paused = false;
        }

        private boolean isFetchable() {
            return !paused && hasValidPosition();
        }

    }

}

```
31. 区分来源，如增加个source或者clientId，以便服务端这边可以区分是哪个客户端，方便排查问题。
32. 关闭底层资源:
```
/**
     * Closes this channel
     *
     * @throws IOException If I/O error occurs
     */
    @Override
    public void close() throws IOException {
        try {
            socketChannel.socket().close();
            socketChannel.close();
        } finally {
            key.attach(null);
            // key.cancel可以disconnected？？
            key.cancel();
        }
    }
```
33. Kafka的报文格式：

&nbsp;&nbsp;&nbsp;&nbsp;请求报文：
```
size(header + body) + header + body
```
&nbsp;&nbsp;&nbsp;&nbsp;其中header的格式为:
```
apiKey + apiVersion + clientId + correlationId，apiKey为对应的请求类型，如请求produce、coordinator、joinGroupRequest等。correlationId为客户端自增的id，服务端必须返回。
```
&nbsp;&nbsp;&nbsp;&nbsp;响应的报文格式为：
```
size(payload) + payload
```
&nbsp;&nbsp;&nbsp;&nbsp;size为Int类型，均为4个字，读取响应时，先读取大小，然后根据大小分配ByteBuffer，直到读满分配的ByteBuffer为止。
&nbsp;&nbsp;&nbsp;&nbsp;NetworkReceive的source来自nodeId，也就是KafkaChannel中的id。

34. Kafka中使用了一个后台线程Sender来不断检测是否有业务线程发送record，如果有则发送出去，并回调record中的函数。 

35. 回调类中的方法一般都以on开头。
36. Kafka中给future一个isDone标识，成功或失败都是通过设置这个标识来实现，可以使用这个标识进行阻塞同步。同时可以通过给future添加一些Listener来添加一些行为，在future结束时调用，增强扩展性。
37. 发送请求后通过回调函数处理响应，使用client.poll(future)来阻塞，不断检测是否收到响应，若收到则处理回调函数，然后再从client.poll(future)返回。
38. 接口中定义的内部类默认是public static。
39. metadata对于自己可以是只包含自己订阅的topic的信息，也可以是全部信息。
40. 默认情况下tcp使用Nagle算法来发送数据，即小mss数据必须收到确认才能再发送数据，这样发送小数据时就会造成较大延迟。且Nagle算法一般用于客户端。socket#setTcpNoDelay(true)，禁用该算法。
41. 对于客户端，一般是先写（请求）后读（响应），而服务端一般是先读（请求），后给（响应）。
42. catch住异常后
- 如果需要释放资源的先释放资源，之后记录错误日志，这时方便打印出上下文信息，可以抛出原来的异常。root cause。
- 或者抛出自定义异常，带上上下文信息和原来的exception
- 都加监控

#### 四、Spring IOC笔记
1. 通过继承来获取父类的通用方法。
2. 通过接口继承还是抽象类继承另一个接口。
3. 接口隔离原则，如BeanFactory/BeanDefinitionRegistry。将不同功能的类进行分开，接口隔离，需要使用的时候，可以作为属性引进来，也可以通过继承获得该类的功能。如AbstractBeanFactory一个类实现多个接口，从而用作多种功能使用。
```
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
		implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable {
```

4. 接口可以实现多继承：
```
public interface ConfigurableBeanFactory extends HierarchicalBeanFactory, SingletonBeanRegistry {
```
5. context开头的标签不是默认命名空间的，而是http://www.springframework.org/schema/context，默认的标签有四个：import/alias/bean/beans(嵌入的)

6. BeanDefinition解析的地方：DefaultBeanDefinitionDocumentReader#parseBeanDefinitions，先解析默认的命名空间的标签，再解析自定义标签。return node.getNamespaceURI();
默认的命名空间
http://www.springframework.org/schema/beans

7. 解析bean标签，实际是由BeanDefinitionParserDelegate进行解析，这个类很重要！！！三个方法：delegate.parseBeanDefinitionElement(ele);delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);delegate.parseCustomElement(ele);

```
// DefaultBeanDefinitionDocumentReader
 @Override
    public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
        this.readerContext = readerContext;
        Element root = doc.getDocumentElement();
        logger.info("加载bean definition，根节点为：" + root);
        doRegisterBeanDefinitions(root);
    }

/**
	 * Register each bean definition within the given root {@code <beans/>} element.
	 */
	protected void doRegisterBeanDefinitions(Element root) {
		// Any nested <beans> elements will cause recursion in this method. In
		// order to propagate and preserve <beans> default-* attributes correctly,
		// keep track of the current (parent) delegate, which may be null. Create
		// the new (child) delegate with a reference to the parent for fallback purposes,
		// then ultimately reset this.delegate back to its original (parent) reference.
		// this behavior emulates a stack of delegates without actually necessitating one.
		BeanDefinitionParserDelegate parent = this.delegate;
		this.delegate = createDelegate(getReaderContext(), root, parent);

		if (this.delegate.isDefaultNamespace(root)) {
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			if (StringUtils.hasText(profileSpec)) {
				String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
						profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
				if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
					return;
				}
			}
		}

		preProcessXml(root);
		parseBeanDefinitions(root, this.delegate);
		postProcessXml(root);

		this.delegate = parent;
	}
	// DefaultBeanDefinitionDocumentReader
	/**
	 * Parse the elements at the root level in the document:
	 * "import", "alias", "bean".
	 * @param root the DOM root element of the document
	 */
	protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
		if (delegate.isDefaultNamespace(root)) {
			NodeList nl = root.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node node = nl.item(i);
				// 跳过文本标签
				if (node instanceof Element) {
					Element ele = (Element) node;
					if (delegate.isDefaultNamespace(ele)) {
						parseDefaultElement(ele, delegate);
					}
					else {
						delegate.parseCustomElement(ele);
					}
				}
			}
		}
		else {
			delegate.parseCustomElement(root);
		}
	}
private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
		if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
			importBeanDefinitionResource(ele);
		}
		else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
			processAliasRegistration(ele);
		}
		else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
			processBeanDefinition(ele, delegate);
		}
		else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
			// recurse
			doRegisterBeanDefinitions(ele);
		}
	}

// 使用BeanDefinitionParserDelegate解析bean标签，
// 并注册到BeanDefinitionRegistry中，
// 实现类有DefaultListableBeanFactory或者XmlBeanFactory

/**
	 * Process the given bean element, parsing the bean definition
	 * and registering it with the registry.
	 */
	protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
		BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
		if (bdHolder != null) {
			bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
			try {
				// Register the final decorated instance.
				BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
			}
			catch (BeanDefinitionStoreException ex) {
				getReaderContext().error("Failed to register bean definition with name '" +
						bdHolder.getBeanName() + "'", ele, ex);
			}
			// Send registration event.
			getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
		}
	}
	
	// BeanDefinitionParserDelegate，解析bean标签
	public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, BeanDefinition containingBean) {
		String id = ele.getAttribute(ID_ATTRIBUTE);
		String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);

		// bean中的name属性是别名
		List<String> aliases = new ArrayList<String>();
		if (StringUtils.hasLength(nameAttr)) {
			String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS);
			aliases.addAll(Arrays.asList(nameArr));
		}

		String beanName = id;
		if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
			beanName = aliases.remove(0);
			if (logger.isDebugEnabled()) {
				logger.debug("No XML 'id' specified - using '" + beanName +
						"' as bean name and " + aliases + " as aliases");
			}
		}

		if (containingBean == null) {
			checkNameUniqueness(beanName, aliases, ele);
		}

		AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);
		if (beanDefinition != null) {
			if (!StringUtils.hasText(beanName)) {
				try {
					if (containingBean != null) {
						beanName = BeanDefinitionReaderUtils.generateBeanName(
								beanDefinition, this.readerContext.getRegistry(), true);
					}
					else {
						// 没有id时产生beanName，自动生成与容器中所有beanName不同的一个。id=类全名#数字
						beanName = this.readerContext.generateBeanName(beanDefinition);
						// Register an alias for the plain bean class name, if still possible,
						// if the generator returned the class name plus a suffix.
						// This is expected for Spring 1.2/2.0 backwards compatibility.
						String beanClassName = beanDefinition.getBeanClassName();
						if (beanClassName != null &&
								beanName.startsWith(beanClassName) && beanName.length() > beanClassName.length() &&
								!this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
						// 定义一个id为类全名的bean，然后定义一个没有名字的bean，后面这个名字为类全名#0，反过来的那个会被覆盖，变成别名？？	aliases.add(beanClassName);
						}
					}
					if (logger.isDebugEnabled()) {
						logger.debug("Neither XML 'id' nor 'name' specified - " +
								"using generated bean name [" + beanName + "]");
					}
				}
				catch (Exception ex) {
					error(ex.getMessage(), ele);
					return null;
				}
			}
			// 返回beanName + 别名 + BeanDefinition
			String[] aliasesArray = StringUtils.toStringArray(aliases);
			return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
		}

		return null;
	}
	
	/**
	 * Parse the bean definition itself, without regard to name or aliases. May return
	 * {@code null} if problems occurred during the parsing of the bean definition.
	 */
	public AbstractBeanDefinition parseBeanDefinitionElement(
			Element ele, String beanName, BeanDefinition containingBean) {

		this.parseState.push(new BeanEntry(beanName));

		String className = null;
		if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
			className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
		}

		try {
			String parent = null;
			if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
				parent = ele.getAttribute(PARENT_ATTRIBUTE);
			}
			AbstractBeanDefinition bd = createBeanDefinition(className, parent);

			parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);
			bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));

			parseMetaElements(ele, bd);
			parseLookupOverrideSubElements(ele, bd.getMethodOverrides());
			parseReplacedMethodSubElements(ele, bd.getMethodOverrides());

			parseConstructorArgElements(ele, bd);
			parsePropertyElements(ele, bd);
			parseQualifierElements(ele, bd);

			bd.setResource(this.readerContext.getResource());
			bd.setSource(extractSource(ele));

			return bd;
		}
		catch (ClassNotFoundException ex) {
			error("Bean class [" + className + "] not found", ele, ex);
		}
		catch (NoClassDefFoundError err) {
			error("Class that bean class [" + className + "] depends on not found", ele, err);
		}
		catch (Throwable ex) {
			error("Unexpected failure during bean definition parsing", ele, ex);
		}
		finally {
			this.parseState.pop();
		}

		return null;
	}
	
	// BeanDefinitionParserDelegate中的自定义类型解析
	public BeanDefinition parseCustomElement(Element ele, BeanDefinition containingBd) {
		String namespaceUri = getNamespaceURI(ele);
		NamespaceHandler handler = this.readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
		if (handler == null) {
			error("Unable to locate Spring NamespaceHandler for XML schema namespace [" + namespaceUri + "]", ele);
			return null;
		}
		return handler.parse(ele, new ParserContext(this.readerContext, this, containingBd));
	}
	
	// NamespaceHandler的获取在DefaultNamespaceHandlerResolver中获取
	/*
	根据namespaceUri从DefaultNamespaceHandlerResolver获取到对应的NamespaceResolver，这部分resolver在META-INF/spring.handlers中定义了。然后实例化该handler，并调用handler的resolve方法解析对应的element
 * 例如：http\://www.springframework.org/schema/mvc=org.springframework.web.servlet.config.MvcNamespaceHandler
 */

package org.springframework.beans.factory.xml;

import com.alibaba.fastjson.JSON;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the {@link NamespaceHandlerResolver} interface.
 * Resolves namespace URIs to implementation classes based on the mappings
 * contained in mapping file.
 *
 * <p>By default, this implementation looks for the mapping file at
 * {@code META-INF/spring.handlers}, but this can be changed using the
 * {@link #DefaultNamespaceHandlerResolver(ClassLoader, String)} constructor.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see NamespaceHandler
 * @see DefaultBeanDefinitionDocumentReader
 */
public class DefaultNamespaceHandlerResolver implements NamespaceHandlerResolver {

	/**
	 * The location to look for the mapping files. Can be present in multiple JAR files.
	 */
	public static final String DEFAULT_HANDLER_MAPPINGS_LOCATION = "META-INF/spring.handlers";


	/** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass());

	/** ClassLoader to use for NamespaceHandler classes */
	private final ClassLoader classLoader;

	/** Resource location to search for */
	private final String handlerMappingsLocation;

	/** Stores the mappings from namespace URI to NamespaceHandler class name / instance */
	private volatile Map<String, Object> handlerMappings;


	/**
	 * Create a new {@code DefaultNamespaceHandlerResolver} using the
	 * default mapping file location.
	 * <p>This constructor will result in the thread context ClassLoader being used
	 * to load resources.
	 * @see #DEFAULT_HANDLER_MAPPINGS_LOCATION
	 */
	public DefaultNamespaceHandlerResolver() {
		this(null, DEFAULT_HANDLER_MAPPINGS_LOCATION);
	}

	/**
	 * Create a new {@code DefaultNamespaceHandlerResolver} using the
	 * default mapping file location.
	 * @param classLoader the {@link ClassLoader} instance used to load mapping resources
	 * (may be {@code null}, in which case the thread context ClassLoader will be used)
	 * @see #DEFAULT_HANDLER_MAPPINGS_LOCATION
	 */
	public DefaultNamespaceHandlerResolver(ClassLoader classLoader) {
		this(classLoader, DEFAULT_HANDLER_MAPPINGS_LOCATION);
	}

	/**
	 * Create a new {@code DefaultNamespaceHandlerResolver} using the
	 * supplied mapping file location.
	 * @param classLoader the {@link ClassLoader} instance used to load mapping resources
	 * may be {@code null}, in which case the thread context ClassLoader will be used)
	 * @param handlerMappingsLocation the mapping file location
	 */
	public DefaultNamespaceHandlerResolver(ClassLoader classLoader, String handlerMappingsLocation) {
		Assert.notNull(handlerMappingsLocation, "Handler mappings location must not be null");
		this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
		this.handlerMappingsLocation = handlerMappingsLocation;
	}


	/**
	 * Locate the {@link NamespaceHandler} for the supplied namespace URI
	 * from the configured mappings.
	 * @param namespaceUri the relevant namespace URI
	 * @return the located {@link NamespaceHandler}, or {@code null} if none found
	 */
	@Override
	public NamespaceHandler resolve(String namespaceUri) {
		// 懒加载
		Map<String, Object> handlerMappings = getHandlerMappings();
		Object handlerOrClassName = handlerMappings.get(namespaceUri);
		if (handlerOrClassName == null) {
			return null;
		}
		else if (handlerOrClassName instanceof NamespaceHandler) {
			// 已经解析了，无需再加载
			return (NamespaceHandler) handlerOrClassName;
		}
		else {
			String className = (String) handlerOrClassName;
			try {
				Class<?> handlerClass = ClassUtils.forName(className, this.classLoader);
				if (!NamespaceHandler.class.isAssignableFrom(handlerClass)) {
					throw new FatalBeanException("Class [" + className + "] for namespace [" + namespaceUri +
							"] does not implement the [" + NamespaceHandler.class.getName() + "] interface");
				}
				// TODO 懒加载
				NamespaceHandler namespaceHandler = (NamespaceHandler) BeanUtils.instantiateClass(handlerClass);
				// 调用init方法
				namespaceHandler.init();
				handlerMappings.put(namespaceUri, namespaceHandler);
				return namespaceHandler;
			}
			catch (ClassNotFoundException ex) {
				throw new FatalBeanException("NamespaceHandler class [" + className + "] for namespace [" +
						namespaceUri + "] not found", ex);
			}
			catch (LinkageError err) {
				throw new FatalBeanException("Invalid NamespaceHandler class [" + className + "] for namespace [" +
						namespaceUri + "]: problem with handler class file or dependent class", err);
			}
		}
	}

	/**
	 * Load the specified NamespaceHandler mappings lazily.
	 */
	private Map<String, Object> getHandlerMappings() {
		// 双检锁 + volatile
		if (this.handlerMappings == null) {
			synchronized (this) {
				if (this.handlerMappings == null) {
					try {
						// 默认映射空间处理器从META-INF/spring.handlers中加载
						// 可以加载多个该文件
						Properties mappings =
								PropertiesLoaderUtils.loadAllProperties(this.handlerMappingsLocation, this.classLoader);
						if (logger.isDebugEnabled()) {
							logger.debug("Loaded NamespaceHandler mappings: " + mappings);
						}
						// 默认ConcurrentHashMap
						Map<String, Object> handlerMappings = new ConcurrentHashMap<String, Object>(mappings.size());
						CollectionUtils.mergePropertiesIntoMap(mappings, handlerMappings);
						this.handlerMappings = handlerMappings;
					}
					catch (IOException ex) {
						throw new IllegalStateException(
								"Unable to load NamespaceHandler mappings from location [" + this.handlerMappingsLocation + "]", ex);
					}
				}
			}
		}
		return this.handlerMappings;
	}

	// TODO 命名空间
	public static void main(String[] args) {
		System.out.println(JSON.toJSONString(new DefaultNamespaceHandlerResolver().getHandlerMappings()));
	}


	@Override
	public String toString() {
		return "NamespaceHandlerResolver using mappings " + getHandlerMappings();
	}

}

```

&nbsp;&nbsp;&nbsp;&nbsp;NamespaceHandler的体系也比较完整，也就是在子类中实现init方法，注入所有mvc:annotation-driven中annotation-driven对应的解析器，即可解析对应的标签。
- 先通过namespaceUri，如http\://www.springframework.org/schema/mvc，找到对应的NamespaceHandler处理器实现类
- 该实现类再找到对应的子标签名对应得到解析器，如AnnotationDrivenBeanDefinitionParser，即可解析对应的标签，里面注入了默认的HandlerMapping/HandlerAdapter。
- 自定义标签解析，主要是为了注入BeanDefinition。
```
package org.springframework.web.servlet.config;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * {@link NamespaceHandler} for Spring MVC configuration namespace.
 *
 * @author Keith Donald
 * @author Jeremy Grelle
 * @author Sebastien Deleuze
 * @since 3.0
 */
public class MvcNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("annotation-driven", new AnnotationDrivenBeanDefinitionParser());
		registerBeanDefinitionParser("default-servlet-handler", new DefaultServletHandlerBeanDefinitionParser());
		registerBeanDefinitionParser("interceptors", new InterceptorsBeanDefinitionParser());
		registerBeanDefinitionParser("resources", new ResourcesBeanDefinitionParser());
		registerBeanDefinitionParser("view-controller", new ViewControllerBeanDefinitionParser());
		registerBeanDefinitionParser("redirect-view-controller", new ViewControllerBeanDefinitionParser());
		registerBeanDefinitionParser("status-controller", new ViewControllerBeanDefinitionParser());
		registerBeanDefinitionParser("view-resolvers", new ViewResolversBeanDefinitionParser());
		registerBeanDefinitionParser("tiles-configurer", new TilesConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("freemarker-configurer", new FreeMarkerConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("velocity-configurer", new VelocityConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("groovy-configurer", new GroovyMarkupConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("script-template-configurer", new ScriptTemplateConfigurerBeanDefinitionParser());
		registerBeanDefinitionParser("cors", new CorsBeanDefinitionParser());
	}

}

package org.springframework.beans.factory.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Support class for implementing custom {@link NamespaceHandler NamespaceHandlers}.
 * Parsing and decorating of individual {@link Node Nodes} is done via {@link BeanDefinitionParser}
 * and {@link BeanDefinitionDecorator} strategy interfaces, respectively.
 *
 * <p>Provides the {@link #registerBeanDefinitionParser} and {@link #registerBeanDefinitionDecorator}
 * methods for registering a {@link BeanDefinitionParser} or {@link BeanDefinitionDecorator}
 * to handle a specific element.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 * @see #registerBeanDefinitionParser(String, BeanDefinitionParser)
 * @see #registerBeanDefinitionDecorator(String, BeanDefinitionDecorator)
 */
public abstract class NamespaceHandlerSupport implements NamespaceHandler {

	/**
	 * Stores the {@link BeanDefinitionParser} implementations keyed by the
	 * local name of the {@link Element Elements} they handle.
	 */
	private final Map<String, BeanDefinitionParser> parsers =
			new HashMap<String, BeanDefinitionParser>();

	/**
	 * Stores the {@link BeanDefinitionDecorator} implementations keyed by the
	 * local name of the {@link Element Elements} they handle.
	 */
	private final Map<String, BeanDefinitionDecorator> decorators =
			new HashMap<String, BeanDefinitionDecorator>();

	/**
	 * Stores the {@link BeanDefinitionDecorator} implementations keyed by the local
	 * name of the {@link Attr Attrs} they handle.
	 */
	private final Map<String, BeanDefinitionDecorator> attributeDecorators =
			new HashMap<String, BeanDefinitionDecorator>();


	/**
	 * Parses the supplied {@link Element} by delegating to the {@link BeanDefinitionParser} that is
	 * registered for that {@link Element}.
	 */
	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		return findParserForElement(element, parserContext).parse(element, parserContext);
	}

	/**
	 * Locates the {@link BeanDefinitionParser} from the register implementations using
	 * the local name of the supplied {@link Element}.
	 */
	private BeanDefinitionParser findParserForElement(Element element, ParserContext parserContext) {
		// localName难道是mvc:annotation-driven中的annotation-driven？？
		String localName = parserContext.getDelegate().getLocalName(element);
		BeanDefinitionParser parser = this.parsers.get(localName);
		if (parser == null) {
			parserContext.getReaderContext().fatal(
					"Cannot locate BeanDefinitionParser for element [" + localName + "]", element);
		}
		return parser;
	}

	/**
	 * Decorates the supplied {@link Node} by delegating to the {@link BeanDefinitionDecorator} that
	 * is registered to handle that {@link Node}.
	 */
	@Override
	public BeanDefinitionHolder decorate(
			Node node, BeanDefinitionHolder definition, ParserContext parserContext) {

		return findDecoratorForNode(node, parserContext).decorate(node, definition, parserContext);
	}

	/**
	 * Locates the {@link BeanDefinitionParser} from the register implementations using
	 * the local name of the supplied {@link Node}. Supports both {@link Element Elements}
	 * and {@link Attr Attrs}.
	 */
	private BeanDefinitionDecorator findDecoratorForNode(Node node, ParserContext parserContext) {
		BeanDefinitionDecorator decorator = null;
		String localName = parserContext.getDelegate().getLocalName(node);
		if (node instanceof Element) {
			decorator = this.decorators.get(localName);
		}
		else if (node instanceof Attr) {
			decorator = this.attributeDecorators.get(localName);
		}
		else {
			parserContext.getReaderContext().fatal(
					"Cannot decorate based on Nodes of type [" + node.getClass().getName() + "]", node);
		}
		if (decorator == null) {
			parserContext.getReaderContext().fatal("Cannot locate BeanDefinitionDecorator for " +
					(node instanceof Element ? "element" : "attribute") + " [" + localName + "]", node);
		}
		return decorator;
	}


	/**
	 * Subclasses can call this to register the supplied {@link BeanDefinitionParser} to
	 * handle the specified element. The element name is the local (non-namespace qualified)
	 * name.
	 */
	protected final void registerBeanDefinitionParser(String elementName, BeanDefinitionParser parser) {
		this.parsers.put(elementName, parser);
	}

	/**
	 * Subclasses can call this to register the supplied {@link BeanDefinitionDecorator} to
	 * handle the specified element. The element name is the local (non-namespace qualified)
	 * name.
	 */
	protected final void registerBeanDefinitionDecorator(String elementName, BeanDefinitionDecorator dec) {
		this.decorators.put(elementName, dec);
	}

	/**
	 * Subclasses can call this to register the supplied {@link BeanDefinitionDecorator} to
	 * handle the specified attribute. The attribute name is the local (non-namespace qualified)
	 * name.
	 */
	protected final void registerBeanDefinitionDecoratorForAttribute(String attrName, BeanDefinitionDecorator dec) {
		this.attributeDecorators.put(attrName, dec);
	}

}

```

8. bean标签的几个子元素：meta/constructor-arg/property，也就是遍历beanName标签的子节点，看名字中是否有这些标签。


9. 对于property的节点，可以有value/ref属性，也可以有子节点。解析propertyName标签。
```
public Object parsePropertyValue(Element ele, BeanDefinition bd, String propertyName) {
		String elementName = (propertyName != null) ?
						"<property> element for property '" + propertyName + "'" :
						"<constructor-arg> element";

		// Should only have one child element: ref, value, list, etc.
		NodeList nl = ele.getChildNodes();
		Element subElement = null;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node instanceof Element && !nodeNameEquals(node, DESCRIPTION_ELEMENT) &&
					!nodeNameEquals(node, META_ELEMENT)) {
				// Child element is what we're looking for.
				if (subElement != null) {
					error(elementName + " must not contain more than one sub-element", ele);
				}
				else {
					subElement = (Element) node;
				}
			}
		}

		boolean hasRefAttribute = ele.hasAttribute(REF_ATTRIBUTE);
		boolean hasValueAttribute = ele.hasAttribute(VALUE_ATTRIBUTE);
		if ((hasRefAttribute && hasValueAttribute) ||
				((hasRefAttribute || hasValueAttribute) && subElement != null)) {
			error(elementName +
					" is only allowed to contain either 'ref' attribute OR 'value' attribute OR sub-element", ele);
		}

		if (hasRefAttribute) {
			String refName = ele.getAttribute(REF_ATTRIBUTE);
			if (!StringUtils.hasText(refName)) {
				error(elementName + " contains empty 'ref' attribute", ele);
			}
			RuntimeBeanReference ref = new RuntimeBeanReference(refName);
			ref.setSource(extractSource(ele));
			return ref;
		}
		else if (hasValueAttribute) {
			TypedStringValue valueHolder = new TypedStringValue(ele.getAttribute(VALUE_ATTRIBUTE));
			valueHolder.setSource(extractSource(ele));
			return valueHolder;
		}
		else if (subElement != null) {
			return parsePropertySubElement(subElement, bd);
		}
		else {
			// Neither child element nor "ref" or "value" attribute found.
			error(elementName + " must specify a ref or value", ele);
			return null;
		}
	}
```

10. 可见，value类型的属性被封装为TypedStringValue，ref类型的属性被封装为RuntimeBeanReference。并存在PropertyValue中。并得到BeanDefinitionHolder，也就是封装了beanName、BeanDefinition、alias。Node节点必须是Element的实例。bean标签中可能有自定义标签，需要进行再次解析。
11. element -> namespace uri(META-INF/spring.handlers) -> NamespaceHandler -> BeanDefinitionParser -> BeanDefinition


12. getBean的过程（单个bean的生命周期）
- 将别名转化为真实名
- 从单例缓存中获取bean，或者获取提前曝光的bean
- 如果当前BeanFactory没有包含这个bean的BeanDefinition，如果有父BeanFactory的话，尝试从父BeanFactory中查找。
- 从beanDefinitionMap中获得当前beanName的BeanDefinition
- 设置该bean为正在构建中
- 根据构造函数创建bean实例，并存放到BeanWrapperImpl中。
- 提前曝光到ObjectFactory缓存中，以允许循环依赖。
- 进行属性注入，先调用BeanPostProcessor#postProcessPropertyValues，注入@Resource属性
- 使用applyPropertyValues调用setter方法，将BeanDefinitionValue设置到对应的属性中，设置之前将BeanDefinitionValue转化为实际的值，使用的是BeanDefinitionValueResolver。如refName -> getBean(refName)。PropertyValue中的value可以为TypedValueString/RuntimeBeanReference...，设置属性是在AbstractNestablePropertyAccessor#setPropretyValues。通过java bean内省Introspetor实现。setter方法。RuntimeBeanReference会被替换为实际的值。通过getBean方法。
- 注入常见Aware实例
- 调用init方法进行初始化。
- 加入单例缓存
- 该bean构建完成，移除出提前曝光缓存。
- 使用bean
- destroy bean

13. Spring中XML bean中可以定义autowired属性：
- <bean autowired=byType/byName/>，这样就不需要配置比较复杂的属性。
- autowired默认为no，也就是不自动注入。自己配置pv，有配就有，没配就无。
14. Spring中一个bean的创建过程有几个步骤：
- 实例化（class为接口不能实例化，类不存在）
- 属性注入（注解、setter、Aware）
- 调用init方法初始化

&nbsp;&nbsp;&nbsp;&nbsp;这几步骤的前后可以有自定义的beanPostProcessor进行扩展。
15. spring使用BeanWrapperImpl来完成对一个bean的实例化，真正的实例是beanWrapper的getWrapperInstance，当调用setPropertyValues方法时将注入相关的属性，但必须是setter方法，与属性定义无关。BeanWrapper还实现了PropertyEditorRegistry，TypeConverter接口。
![BeanWrapperImpl](http://fuwu.qunar.com/gongdan/attach/download?fileName=BeanWrapperImpl.png&uniqueName=file20181006105535634033)
15. 默认使用DefaultListableBeanFactory和XmlApplicaztionContext来构造相应的容器。

16. Tomcat容器启动点
- ContextLoaderListener (start/stop)
- Servlet (init)
- Filter (doFilter)

18. BeanFactory实现了BeanDeifinitionRegistry接口，XmlBeanDefinitionReader将XML属性注入到这个接口中。

19. Spring中的注解自动注入：
- @Resource注解使用CommonAnnotationBeanPostProcessor#postProcessValues，也就是InjectionMetadata$InjectedElement#inject 
- @Autowired和@Value都使用AutowiredAnnotationBeanPostProcessor注解进行处理。
org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor#postProcessPropertyValues
- 这两个后处理器都是InstantiationAwareBeanPostProcessor，在populateBean方法中先调用postProcessPropertyValues方法。
- 这也是spring中的扩展点。
- 这些注解使用反射直接设置属性或方法，不需要相应的setter方法。
- 使用applyPropertyValues设置ref和value，也就是在XML中定义的。需要在BeanDefinitionValueResolver中获取对应的getBean()，最后调用setter方注入。

20. XML定义的bean也可以进行属性转化，如string->resource。PropertyEditor/Converter。PropertyEditroRegistry/ConversionService
21. 内嵌bean使用BeanFactory#createBean解析。

22. Spring中bean的配置有三种方式，分别是xml、注解(@Component/@Service/@Repository/@Controller)、@Configuration/@Bean（java类)的方式。

23. 依赖注入主要有属性注入、构造函数注入、lookup方法注入

24. spring触发的方法，如afterPropertiesSet/setApplicationContext，可以在里面扩展自己的逻辑，保证spring初始化时执行。


23. 一个实现类可以覆盖多个父类或接口的重复方法，如DefaultListableBeanFactory的一个方法，分别是AbstractBeanFactory/ListableBeanFactory/BeanDefinitionRegistry的抽象方法：
```
	@Override
	public boolean containsBeanDefinition(String beanName) {
		Assert.notNull(beanName, "Bean name must not be null");
		return this.beanDefinitionMap.containsKey(beanName);
	}
```

24. 一个类继承一个父类，再实现一个接口，可以使用父类同名的方法来实现接口。
25. 
```
graph TD 
    A[interface]-->B(抽象类)
    B --> C[逻辑抽象类]
    B --> D[bean抽象类_有构造函数]
    C --> 调用父类中的方法_做扩展功能
    C --> 实现父类中的模板方法_扩展功能
    C --> 重载父类中的protected方法_扩展功能
```
26. 委托模式
宿主A --创建--> 委托类B，入参为宿主A，B利用A工作。代理对被代理的对象最熟悉。
27. 
![TypeConverterDelegate类图](http://fuwu.qunar.com/gongdan/attach/download?fileName=typeConverterDelegate.png&uniqueName=file20181004193905708908))

&nbsp;&nbsp;&nbsp;&nbsp;
28. 策略模式的使用
```
// AbstractAutowireCapableBeanFactory#instantiateBean
// 实例化bean时抽取出不同的策略接口，可以由外部设置。
/**
	 * Instantiate the given bean using its default constructor.
	 * @param beanName the name of the bean
	 * @param mbd the bean definition for the bean
	 * @return BeanWrapper for the new instance
	 */
	protected BeanWrapper instantiateBean(final String beanName, final RootBeanDefinition mbd) {
		try {
			Object beanInstance;
			final BeanFactory parent = this;
			if (System.getSecurityManager() != null) {
				beanInstance = AccessController.doPrivileged(new PrivilegedAction<Object>() {
					@Override
					public Object run() {
						return getInstantiationStrategy().instantiate(mbd, beanName, parent);
					}
				}, getAccessControlContext());
			}
			else {
			// 对应的策略可以通过set方法设置，这里使用的是无参构造函数
				beanInstance = getInstantiationStrategy().instantiate(mbd, beanName, parent);
			}
			BeanWrapper bw = new BeanWrapperImpl(beanInstance);
			initBeanWrapper(bw);
			return bw;
		}
		catch (Throwable ex) {
			throw new BeanCreationException(mbd.getResourceDescription(), beanName, "Instantiation of bean failed", ex);
		}
	}
```

29. BeanWrapperImpl继承实现了TypeConverter/PropertyEditorRegistry，以及BeanWrapper接口。
30. 没有配置域名，没配nginx，可以使用ip:port直接访问。不管继承体系多复杂，最终使用的是具体类。

30. 使用完线程池时，最后要注意销毁。

30. 属性注入，requiredType为对应的类对应的属性的实际类型，而赋予的值未必是对应的类型，可能是字符串等。这时需要对应类型的属性编辑器。

31. 获取该属性时再加载，并存起来。下次就不用再加载了，也就是懒加载。懒加载使用缓存，不存在时加载，加载后放回本地缓存。这是永久缓存。
```
/**
	 * This variant retrieves {@link Class#getDeclaredFields()} from a local cache
	 * in order to avoid the JVM's SecurityManager check and defensive array copying.
	 * @param clazz the class to introspect
	 * @return the cached array of fields
	 * @see Class#getDeclaredFields()
	 */
	private static Field[] getDeclaredFields(Class<?> clazz) {
		Field[] result = declaredFieldsCache.get(clazz);
		if (result == null) {
			result = clazz.getDeclaredFields();
			declaredFieldsCache.put(clazz, (result.length == 0 ? NO_FIELDS : result));
		}
		return result;
	}
```
32. MutablePropertyValue可以修改的

34. CommonAnnotationBeanPostProcessor中解析@Resource依赖的代码，使用的是field.set()来直接设置，设置时通过BeanFactory.getBean()获取到对应的依赖的bean。对于List则使用CustomCollectionEditor来转化为List。实例化后属性注入前提前曝光。
```
/**
	 * Obtain a resource object for the given name and type through autowiring
	 * based on the given factory.
	 * @param factory the factory to autowire against
	 * @param element the descriptor for the annotated field/method 待注入的属性或方法
	 * @param requestingBeanName the name of the requesting bean 所在bean
	 * @return the resource object (never {@code null})
	 * @throws BeansException if we failed to obtain the target resource
	 */
	protected Object autowireResource(BeanFactory factory, LookupElement element, String requestingBeanName)
			throws BeansException {

		Object resource;
		Set<String> autowiredBeanNames;
		String name = element.name;
        // @Resource默认按照类型来，指定名字时按照名字来
        // TOOD 这个分支满足什么条件才进去？？未给出beanName，且name对应的bean不存在时，默认类型匹配？？
		if (this.fallbackToDefaultTypeMatch && element.isDefaultName &&
				factory instanceof AutowireCapableBeanFactory && !factory.containsBean(name)) {
			autowiredBeanNames = new LinkedHashSet<String>();
			resource = ((AutowireCapableBeanFactory) factory).resolveDependency(
					element.getDependencyDescriptor(), requestingBeanName, autowiredBeanNames, null);
		}
		else {
		    // 按照名字获取bean
			resource = factory.getBean(name, element.lookupType);
			autowiredBeanNames = Collections.singleton(name);
		}

		if (factory instanceof ConfigurableBeanFactory) {
			ConfigurableBeanFactory beanFactory = (ConfigurableBeanFactory) factory;
			for (String autowiredBeanName : autowiredBeanNames) {
				if (beanFactory.containsBean(autowiredBeanName)) {
					beanFactory.registerDependentBean(autowiredBeanName, requestingBeanName);
				}
			}
		}

		return resource;
	}
	
	/**
	    按照类型进行注入
	*/
	public Object doResolveDependency(DependencyDescriptor descriptor, String beanName,
			Set<String> autowiredBeanNames, TypeConverter typeConverter) throws BeansException {
			// type可能是list/set/map，而不是泛型参数
		Class<?> type = descriptor.getDependencyType();
		logger.info("bean：" + beanName + "依赖类型" + type);
		// 含有@Value注解，获取到@Value里面的值
		Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
		if (value != null) {
			if (value instanceof String) {
				String strVal = resolveEmbeddedValue((String) value);
				BeanDefinition bd = (beanName != null && containsBean(beanName) ? getMergedBeanDefinition(beanName) : null);
				// 解析属性的值，如${abc}，abc=123，则替换为123
				value = evaluateBeanDefinitionString(strVal, bd);
			}
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			logger.info("value[" + value + "]使用转换器为：" + converter);
			return (descriptor.getField() != null ?
					converter.convertIfNecessary(value, type, descriptor.getField()) :
					converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
		}

		if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			DependencyDescriptor targetDesc = new DependencyDescriptor(descriptor);
			targetDesc.increaseNestingLevel();
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, componentType, targetDesc);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					raiseNoSuchBeanDefinitionException(componentType, "array of " + componentType.getName(), descriptor);
				}
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			Object result = converter.convertIfNecessary(matchingBeans.values(), type);
			// TODO 依赖可以排序？？
			if (getDependencyComparator() != null && result instanceof Object[]) {
				Arrays.sort((Object[]) result, adaptDependencyComparator(matchingBeans));
			}
			return result;
		}
		else if (Collection.class.isAssignableFrom(type) && type.isInterface()) {
		// 集合的元素类型
			Class<?> elementType = descriptor.getCollectionType();
			if (elementType == null) {
				if (descriptor.isRequired()) {
					throw new FatalBeanException("No element type declared for collection [" + type.getName() + "]");
				}
				return null;
			}
			DependencyDescriptor targetDesc = new DependencyDescriptor(descriptor);
			targetDesc.increaseNestingLevel();
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, elementType, targetDesc);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					raiseNoSuchBeanDefinitionException(elementType, "collection of " + elementType.getName(), descriptor);
				}
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
			Object result = converter.convertIfNecessary(matchingBeans.values(), type);
			if (getDependencyComparator() != null && result instanceof List) {
				Collections.sort((List<?>) result, adaptDependencyComparator(matchingBeans));
			}
			return result;
		}
		else if (Map.class.isAssignableFrom(type) && type.isInterface()) {
			Class<?> keyType = descriptor.getMapKeyType();
			// key必须是String类型
			if (String.class != keyType) {
				if (descriptor.isRequired()) {
					throw new FatalBeanException("Key type [" + keyType + "] of map [" + type.getName() +
							"] must be [java.lang.String]");
				}
				return null;
			}
			Class<?> valueType = descriptor.getMapValueType();
			if (valueType == null) {
				if (descriptor.isRequired()) {
					throw new FatalBeanException("No value type declared for map [" + type.getName() + "]");
				}
				return null;
			}
			DependencyDescriptor targetDesc = new DependencyDescriptor(descriptor);
			targetDesc.increaseNestingLevel();
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, valueType, targetDesc);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					raiseNoSuchBeanDefinitionException(valueType, "map with value type " + valueType.getName(), descriptor);
				}
				return null;
			}
			if (autowiredBeanNames != null) {
				autowiredBeanNames.addAll(matchingBeans.keySet());
			}
			logger.info("map类型 : " + matchingBeans);
			return matchingBeans;
		}
		else {
		   // ArrayList<interface> / HashMap<String, AbstractClass>走这里
			logger.info(beanName + "另外解析bean" + type);
			Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
			if (matchingBeans.isEmpty()) {
				if (descriptor.isRequired()) {
					raiseNoSuchBeanDefinitionException(type, "", descriptor);
				}
				return null;
			}
			if (matchingBeans.size() > 1) {
				String primaryBeanName = determineAutowireCandidate(matchingBeans, descriptor);
				if (primaryBeanName == null) {
					throw new NoUniqueBeanDefinitionException(type, matchingBeans.keySet());
				}
				if (autowiredBeanNames != null) {
					autowiredBeanNames.add(primaryBeanName);
				}
				return matchingBeans.get(primaryBeanName);
			}
			// We have exactly one match.
			Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
			if (autowiredBeanNames != null) {
				autowiredBeanNames.add(entry.getKey());
			}
			logger.info("beanName依赖，另类解析" + matchingBeans);
			return entry.getValue();
		}
	}
	
	// 对应的寻找依赖的代码，按照类型来
	/**
	 * Find bean instances that match the required type.
	 * Called during autowiring for the specified bean.
	 * @param beanName the name of the bean that is about to be wired
	 * @param requiredType the actual type of bean to look for
	 * (may be an array component type or collection element type)
	 * @param descriptor the descriptor of the dependency to resolve
	 * @return a Map of candidate names and candidate instances that match
	 * the required type (never {@code null})
	 * @throws BeansException in case of errors
	 * @see #autowireByType
	 * @see #autowireConstructor
	 */
	protected Map<String, Object> findAutowireCandidates(
			String beanName, Class<?> requiredType, DependencyDescriptor descriptor) {

        // requiredType是对应依赖的实际类型，如果是数组或者集合，则是元素的类型
        // 这里是寻找对应的注入实例的名字
        // TODO 从当前BeanFactory及其父BeanFactory获取requiredType对应的beanName，注意descriptor.isEager()
		String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
				this, requiredType, true, descriptor.isEager());
		Map<String, Object> result = new LinkedHashMap<String, Object>(candidateNames.length);

        // TODO 从手动注入的依赖中寻找。
		for (Class<?> autowiringType : this.resolvableDependencies.keySet()) {
			if (autowiringType.isAssignableFrom(requiredType)) {
				Object autowiringValue = this.resolvableDependencies.get(autowiringType);
				autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
				if (requiredType.isInstance(autowiringValue)) {
					result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
					break;
				}
			}
		}
		// 对每个符合条件的候选的注入实例的名字，获取对应的实例
		for (String candidateName : candidateNames) {
			if (!isSelfReference(beanName, candidateName) && isAutowireCandidate(candidateName, descriptor)) {
				result.put(candidateName, getBean(candidateName));
			}
		}
		if (result.isEmpty()) {
			DependencyDescriptor fallbackDescriptor = descriptor.forFallbackMatch();
			for (String candidateName : candidateNames) {
				if (!candidateName.equals(beanName) && isAutowireCandidate(candidateName, fallbackDescriptor)) {
					result.put(candidateName, getBean(candidateName));
				}
			}
		}
		return result;
	}
```

32. BeanFactory注入表达式解析器：

```
		beanFactory.setBeanExpressionResolver(new StandardBeanExpressionResolver(beanFactory.getBeanClassLoader()));
```

33. 手动注入已经解析的依赖，需要查看注入了哪些？？
```
// DefaultListableBeanFactory
public void registerResolvableDependency(Class<?> dependencyType, Object autowiredValue) {
		Assert.notNull(dependencyType, "Type must not be null");
		if (autowiredValue != null) {
			Assert.isTrue((autowiredValue instanceof ObjectFactory || dependencyType.isInstance(autowiredValue)),
					"Value [" + autowiredValue + "] does not implement specified type [" + dependencyType.getName() + "]");
			this.resolvableDependencies.put(dependencyType, autowiredValue);
		}
	}
```

35. 
- AbstractBeanFactory实现了getBean的功能，但是底层依赖的是createBean这个抽象方法，DefaultSingletonBeanRegistry这个添加了单例缓存的类，以及别名类SimpleAliasRegistry。

- createBean在AbstractAutowireCapableBeanFactory中实现。AbstractAutowireCapableBeanFactory主要是提供自动装配、实例化、属性注入、初始化等功能。

- AbstractBeanFactory的containBeanDefinition和getBeanDefinition抽象方法在AbstractAutowireCapableBeanFactory仍未实现。

- 直到DefaultListableBeanFactory才实现。而且该类实现了BeanDefinitionRegistry这个接口，有重叠的未实现的方法。

36. Collections.sort默认排序是归并排序或者Timsort排序，是一种稳定的排序，最坏是nlog(n)，需要额外的空间支持排序。所以对于相同优先级的组件，如HandlerAdapter，先出现的优先使用。


#### 五、Spring MVC笔记


1. ThreadLocal使用NamedThreadLocal，而且使用一个LocalContextHolder来封装。
2. HandlerExecutionChain包装了HandlerMethod(实际为Object)和Interceptor。
3. Controller阶段

阶段 | preHandle返回false |  处理器方法抛出异常
- | :-: | -: 
preHandle | 有| 有
handle | 无 | 有
postHandle | 无 | 无
render |无 | 有，异常视图
afterCompletion | 有|有

4. HandlerMapping是有顺序的，按照Ordered排序，越小越优先。
5. 接口命名：ViewResolver

6. 在父类protected的基础上扩充功能：
```java
@Override
	public void initApplicationContext() throws BeansException {
		super.initApplicationContext();
		registerHandlers(this.urlMap);
	}
```
7. AnnotationMappingHandlerAdapter自从3.2开始已经被废弃，希望使用RequestMappingHandlerAdapter。
8. 获取默认的变量名，首字母为小写：ClassUtils.getShortNameAsProperty(valueClass);
9. HandlerMapping主要是提供处理器映射关系的注入或者存储对应对的处理器映射关系，并找到能够处理当前httpServletRequest的Handle，找到对应的HandlerInterceptor拦截器。
- RequestMappingHandlerMapping必须找到标有@RequestMapping或者@Controller注解的实现类，并抽象成HandlerMethod方法。
- RequestMappingHandlerAdapter适配处理器HandlerMethod。
- RequestMappingHandlerMapping通过实现InitializingBean，从而在容器开始时初始化RequestMappingInfo->HandlerMethod的注册表项，其中RequestMappingInfo主要来自RequestMapping注解。

- 通过继承ApplicationObjectSupport来注入ApplicationContext，即实现ApplicationContextAware接口。

- 其中WebApplicationObjectSupport又实现了ServletContextAware来注入ServletContext。子类越来越具体化。

- 注入ApplicationContext后可以访问bean工厂，得到所有的bean。如过滤出标有@Controller的所有类。

- SimpleUrlHandlerMapping要求使用者直接注入urlMap关系，自己不做任何解析，负责找到对应的Handler，实现在AbstractUrlHandlerMapping中，在查找handler时，如果handler是字符串，则会解析为bean。
- 而SimpleControllerHandlerAdapter必须是Controller接口的实现类。据此可以自定义另一个接口实现对特定接口实现类的handler的处理。

10. 子类在覆盖父类的方法时，可以把覆盖方法的返回值改为具体类：
```
// 父类
	protected Object getHandlerInternal(HttpServletRequest request) throws Exception {

// 子类
	protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
```
11. MethodParameter封装了一个参数的具体类、泛型类、参数名、对应的方法、在方法的第几个位置。MethodParameter包装了该参数所在的方法、构造函数、以及参数的对应位置，参数对应的解析器会缓存起来，以便下次直接使用。-1表示返回值。
12. 相同类型的代码放在一块，换行隔开
```
ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
		invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
		invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
		invocableMethod.setDataBinderFactory(binderFactory);
		invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

		ModelAndViewContainer mavContainer = new ModelAndViewContainer();
		mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
		modelFactory.initModel(webRequest, mavContainer, invocableMethod);
		mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);
```
13、协助注解
```

package org.springframework.web.bind.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Controller;

/**
 * A convenience annotation that is itself annotated with {@link Controller @Controller}
 * and {@link ResponseBody @ResponseBody}.
 * <p>
 * Types that carry this annotation are treated as controllers where
 * {@link RequestMapping @RequestMapping} methods assume
 * {@link ResponseBody @ResponseBody} semantics by default.
 *
 * @author Rossen Stoyanchev
 * @author Sam Brannen
 * @since 4.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@ResponseBody
public @interface RestController {

	/**
	 * The value may indicate a suggestion for a logical component name,
	 * to be turned into a Spring bean in case of an autodetected component.
	 * @return the suggested component name, if any
	 * @since 4.0.1
	 */
	String value() default "";

}
```
14. 记录异常栈的方法

```
/**
	 * Process this request, publishing an event regardless of the outcome.
	 * <p>The actual event handling is performed by the abstract
	 * {@link #doService} template method.
	 */
	protected final void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		long startTime = System.currentTimeMillis();
		Throwable failureCause = null;

		LocaleContext previousLocaleContext = LocaleContextHolder.getLocaleContext();
		LocaleContext localeContext = buildLocaleContext(request);

		RequestAttributes previousAttributes = RequestContextHolder.getRequestAttributes();
		ServletRequestAttributes requestAttributes = buildRequestAttributes(request, response, previousAttributes);

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
		asyncManager.registerCallableInterceptor(FrameworkServlet.class.getName(), new RequestBindingInterceptor());

		initContextHolders(request, localeContext, requestAttributes);

		try {
			doService(request, response);
		}
		catch (ServletException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (IOException ex) {
			failureCause = ex;
			throw ex;
		}
		catch (Throwable ex) {
			failureCause = ex;
			throw new NestedServletException("Request processing failed", ex);
		}

		finally {
			resetContextHolders(request, previousLocaleContext, previousAttributes);
			if (requestAttributes != null) {
				requestAttributes.requestCompleted();
			}

			if (logger.isDebugEnabled()) {
				if (failureCause != null) {
					this.logger.debug("Could not complete request", failureCause);
				}
				else {
					if (asyncManager.isConcurrentHandlingStarted()) {
						logger.debug("Leaving response open for concurrent processing");
					}
					else {
						this.logger.debug("Successfully completed request");
					}
				}
			}

			publishRequestHandledEvent(request, response, startTime, failureCause);
		}
	}
```
15. spring mvc的过程：
- HandlerMapping根据request得到HandlerExecutionChain。找到能处理该request的handler。如果没有找到，抛出异常（返回404）。
- 根据对应的Handler(HandlerExecutionChain中的Object handler对象)，找到支持该handler的HandlerAdapter。

- preHandle
- handle -> mv
- postHandle 
- 如果前面出异常，则HandlerExceptionResolver->errorMv
- viewResolver.resolve (逻辑视图->真实视图)
- view.render （渲染model）
- afterCompletion


16. 使用ConfigurationClassPostProcessor这个BeanFactoryPostProcessor来后处理@Configuration类，注入@Bean方法

17. 命名与继承。

&nbsp;&nbsp;&nbsp;&nbsp;具体类中出现xml（配置文件是xml）、web（web容器，有ServletConfig等字段）等关键字，且出现相关的属性。表示具体实现。XmlWebApplicationContext实现实际的加载bean，且使用的reader为XmlBeanDefinitionReader。
父类中无法实现，则定义为抽象方法，子类中可以实现。

18. PathMatchingResourcePatternResolver实现了ResourcePatternResolver，可以解析classpath*:等ant类型的路径解析，解析为Resource。ServletContext是web容器的资源路径。TODO classpath*待确认

- Resource可以获得对应的输入流、文件等，实现类有ClasspathResource、UrlResource等，而ResourceLoader是将具体的location转化为对应的resource，进行了统一，相当于所有Resource的门面。

- ResourcePatternResolver则通过使用DefaultResourceLoader来实现模糊匹配资源的加载，得到多个Resouce

19. 获取某种类型的bean，先查找父子工厂中该类型bean的所有beanName，再逐一加载和过滤。

20. 适配器的使用，将WebRequestInterceptor适配为HandlerInterceptor
```
package org.springframework.web.servlet.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.Assert;
import org.springframework.web.context.request.AsyncWebRequestInterceptor;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
   实现待转接口，底层存储待适配的实例
 * Adapter that implements the Servlet HandlerInterceptor interface
 * and wraps an underlying WebRequestInterceptor.
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.web.context.request.WebRequestInterceptor
 * @see org.springframework.web.servlet.HandlerInterceptor
 */
public class WebRequestHandlerInterceptorAdapter implements AsyncHandlerInterceptor {

	private final WebRequestInterceptor requestInterceptor;


	/**
	 * Create a new WebRequestHandlerInterceptorAdapter for the given WebRequestInterceptor.
	 * @param requestInterceptor the WebRequestInterceptor to wrap
	 */
	public WebRequestHandlerInterceptorAdapter(WebRequestInterceptor requestInterceptor) {
		Assert.notNull(requestInterceptor, "WebRequestInterceptor must not be null");
		this.requestInterceptor = requestInterceptor;
	}


	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		this.requestInterceptor.preHandle(new DispatcherServletWebRequest(request, response));
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
			throws Exception {

		this.requestInterceptor.postHandle(new DispatcherServletWebRequest(request, response),
				(modelAndView != null && !modelAndView.wasCleared() ? modelAndView.getModelMap() : null));
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {

		this.requestInterceptor.afterCompletion(new DispatcherServletWebRequest(request, response), ex);
	}

	@Override
	public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (this.requestInterceptor instanceof AsyncWebRequestInterceptor) {
			AsyncWebRequestInterceptor asyncInterceptor = (AsyncWebRequestInterceptor) this.requestInterceptor;
			DispatcherServletWebRequest webRequest = new DispatcherServletWebRequest(request, response);
			asyncInterceptor.afterConcurrentHandlingStarted(webRequest);
		}
	}

}
```

21. 内部类的使用，如私有类，不单独一个类，因为内部类只在该类中使用。

22. HandlerMapping实现了ApplicationContextAware和ServletContextAware，会初始化applicationContext，其映射关系注入在		AbstractHandlerMethodMapping#initHandlerMethods中，由于实现了InitializingBean，所以在启动时便注入好了。属性注入在初始化方法调用前面，其中需要实现的方法有：
```
/**
	 * Whether the given type is a handler with handler methods.
	 * @param beanType the type of the bean being checked
	 * @return "true" if this a handler type, "false" otherwise.
	 */
	protected abstract boolean isHandler(Class<?> beanType);

	/**
	拿到一个方法的映射信息，如@RequestMapping，可能没有映射信息，则不是处理器方法
	  泛型?是否可以去掉
	
	 * Provide the mapping for a handler method. A method for which no
	 * mapping can be provided is not a handler method.
	 * @param method the method to provide a mapping for
	 * @param handlerType the handler type, possibly a sub-type of the method's
	 * declaring class
	 * @return the mapping, or {@code null} if the method is not mapped
	 */
	protected abstract T getMappingForMethod(Method method, Class<?> handlerType);
	
	// 在RequestMappingHandlerMapping中实现了该方法
	/**
	 * Uses method and type-level @{@link RequestMapping} annotations to create
	 * the RequestMappingInfo.
	 * @return the created RequestMappingInfo, or {@code null} if the method
	 * does not have a {@code @RequestMapping} annotation.
	 * @see #getCustomMethodCondition(Method)
	 * @see #getCustomTypeCondition(Class)
	 */
	@Override
	protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		RequestMappingInfo info = createRequestMappingInfo(method);
		if (info != null) {
		    // 结合类的注解
			RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
			if (typeInfo != null) {
				info = typeInfo.combine(info);
			}
		}
		return info;
	}

	/**
	  TODO AnnotatedElement，标有注解的属性或类
	 * Delegates to {@link #createRequestMappingInfo(RequestMapping, RequestCondition)},
	 * supplying the appropriate custom {@link RequestCondition} depending on whether
	 * the supplied {@code annotatedElement} is a class or method.
	 * @see #getCustomTypeCondition(Class)
	 * @see #getCustomMethodCondition(Method)
	 */
	private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
		RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
		RequestCondition<?> condition = (element instanceof Class<?> ?
				getCustomTypeCondition((Class<?>) element) : getCustomMethodCondition((Method) element));
		return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
	}
	
	/**
	 * 从注解里拿到所有的映射信息
	 * Create a {@link RequestMappingInfo} from the supplied
	 * {@link RequestMapping @RequestMapping} annotation, which is either
	 * a directly declared annotation, a meta-annotation, or the synthesized
	 * result of merging annotation attributes within an annotation hierarchy.
	 */
	protected RequestMappingInfo createRequestMappingInfo(
			RequestMapping requestMapping, RequestCondition<?> customCondition) {

		return RequestMappingInfo
				.paths(resolveEmbeddedValuesInPatterns(requestMapping.path()))
				.methods(requestMapping.method())
				.params(requestMapping.params())
				.headers(requestMapping.headers())
				.consumes(requestMapping.consumes())
				.produces(requestMapping.produces())
				.mappingName(requestMapping.name())
				.customCondition(customCondition)
				.options(this.config)
				.build();
	}
```

23、AbstractHandlerMapping实现了HandlerMapping的getHandler方法，模板方法，也就是查找request对应的handler
```
/**
	 * Look up a handler for the given request, falling back to the default
	 * handler if no specific one is found.
	 * @param request current HTTP request
	 * @return the corresponding handler instance, or the default handler
	 * @see #getHandlerInternal
	 */
	@Override
	public final HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
	    // 抽象方法
		Object handler = getHandlerInternal(request);

		if (handler == null) {
		    // 兜底处理器
			handler = getDefaultHandler();
		}
		if (handler == null) {
			return null;
		}
		// Bean name or resolved handler?
		// 如果是字符串，则认为是beanName，可以从ApplicationContext获取到对应的bean'
		if (handler instanceof String) {
			String handlerName = (String) handler;
			handler = getApplicationContext().getBean(handlerName);
		}

		HandlerExecutionChain executionChain = getHandlerExecutionChain(handler, request);
		if (CorsUtils.isCorsRequest(request)) {
			CorsConfiguration globalConfig = this.corsConfigSource.getCorsConfiguration(request);
			CorsConfiguration handlerConfig = getCorsConfiguration(handler, request);
			CorsConfiguration config = (globalConfig != null ? globalConfig.combine(handlerConfig) : handlerConfig);
			executionChain = getCorsHandlerExecutionChain(request, executionChain, config);
		}
		return executionChain;
	}
```

24. AbstractHandlerMethodMapping实现getHandlerInternal方法：
```
/**
	 * Look up a handler method for the given request.
	 */
	@Override
	protected HandlerMethod getHandlerInternal(HttpServletRequest request) throws Exception {
		String lookupPath = getUrlPathHelper().getLookupPathForRequest(request);
		logger.info("查询访问路径的处理器方法 " + lookupPath);
		// 获取锁放在外边，不放在try/finally里面。读写锁。
		this.mappingRegistry.acquireReadLock();
		try {
		// 重点
			HandlerMethod handlerMethod = lookupHandlerMethod(lookupPath, request);
			if (logger.isDebugEnabled()) {
				if (handlerMethod != null) {
					logger.debug("Returning handler method [" + handlerMethod + "]");
				}
				else {
					logger.debug("Did not find handler method for [" + lookupPath + "]");
				}
			}
			return (handlerMethod != null ? handlerMethod.createWithResolvedBean() : null);
		}
		finally {
			this.mappingRegistry.releaseReadLock();
		}
		
		/**
	 * Look up the best-matching handler method for the current request.
	 * If multiple matches are found, the best match is selected.
	 * @param lookupPath mapping lookup path within the current servlet mapping
	 * @param request the current request
	 * @return the best-matching handler method, or {@code null} if no match
	 * @see #handleMatch(Object, String, HttpServletRequest)
	 * @see #handleNoMatch(Set, String, HttpServletRequest)
	 */
	protected HandlerMethod lookupHandlerMethod(String lookupPath, HttpServletRequest request) throws Exception {
		// 将结果放到matches中
		List<Match> matches = new ArrayList<Match>();
		// 查找url对应的handler
		List<T> directPathMatches = this.mappingRegistry.getMappingsByUrl(lookupPath);
		if (directPathMatches != null) {
			addMatchingMappings(directPathMatches, matches, request);
		}
		// 如果没有找到对应直接的url，则使用RequestMappingInfo->HandlerMethod的注册项
		if (matches.isEmpty()) {
			// No choice but to go through all mappings...
			// 兜底操作，遍历所有注册项，过滤出符合条件的每个过滤项的子集
			// 每个子集里的元素还会进行排序，如pattern最具体的排在第一位
			addMatchingMappings(this.mappingRegistry.getMappings().keySet(), matches, request);
		}

		if (!matches.isEmpty()) {
		    // 基于Request这个标准的比较器
			Comparator<Match> comparator = new MatchComparator(getMappingComparator(request));
			// 对所有新的RequestMappingInfo进行排序，最具体的排在前面
			// 按照路径、参数、请求头等比较项依次比较，一旦比较出结果，则结束比较
			Collections.sort(matches, comparator);
			if (logger.isTraceEnabled()) {
				logger.trace("Found " + matches.size() + " matching mapping(s) for [" +
						lookupPath + "] : " + matches);
			}
			Match bestMatch = matches.get(0);
			if (matches.size() > 1) {
				if (CorsUtils.isPreFlightRequest(request)) {
					return PREFLIGHT_AMBIGUOUS_MATCH;
				}
				Match secondBestMatch = matches.get(1);
				// 如果第一和第二项完成相同，则抛出异常
				if (comparator.compare(bestMatch, secondBestMatch) == 0) {
					Method m1 = bestMatch.handlerMethod.getMethod();
					Method m2 = secondBestMatch.handlerMethod.getMethod();
					throw new IllegalStateException("Ambiguous handler methods mapped for HTTP path '" +
							request.getRequestURL() + "': {" + m1 + ", " + m2 + "}");
				}
			}
			handleMatch(bestMatch.mapping, lookupPath, request);
			return bestMatch.handlerMethod;
		}
		else {
			return handleNoMatch(this.mappingRegistry.getMappings().keySet(), lookupPath, request);
		}
	}
	// 核心方法，List<T>和Collection<T>都可以赋值给Collection<T>
	private void addMatchingMappings(Collection<T> mappings, List<Match> matches, HttpServletRequest request) {
		for (T mapping : mappings) {
			// 抽象方法，match对应的RequestMappingInfo被裁剪过，value值为HanlderMethod
			T match = getMatchingMapping(mapping, request);
			if (match != null) {
				matches.add(new Match(match, this.mappingRegistry.getMappings().get(mapping)));
			}
		}
	}
	
	
	/**
	实现匹配的核心抽象方法，找到匹配的RequestMapping
	 T为已有的方法上的RequestMappingInfo
	 * Check if a mapping matches the current request and return a (potentially
	 * new) mapping with conditions relevant to the current request.
	 * @param mapping the mapping to get a match for
	 * @param request the current HTTP servlet request
	 * @return the match, or {@code null} if the mapping doesn't match
	 */
	protected abstract T getMatchingMapping(T mapping, HttpServletRequest request);
	
	// 在RequestMappingInfoHandlerMapping中实现了该方法：
	
	/**
	 * Check if the given RequestMappingInfo matches the current request and
	 * return a (potentially new) instance with conditions that match the
	 * current request -- for example with a subset of URL patterns.
	 * @return an info in case of a match; or {@code null} otherwise.
	 */
	@Override
	protected RequestMappingInfo getMatchingMapping(RequestMappingInfo info, HttpServletRequest request) {
		return info.getMatchingCondition(request);
	}
	
	// 对应RequestMappingInfo中的getMatchingCondition
	// 从入参中提取出匹配当前RequestMappingInfo的子集，最终映射到对应的handlerMethod
	
	/**
	 * Checks if all conditions in this request mapping info match the provided request and returns
	 * a potentially new request mapping info with conditions tailored to the current request.
	 * <p>For example the returned instance may contain the subset of URL patterns that match to
	 * the current request, sorted with best matching patterns on top.
	 * @return a new instance in case all conditions match; or {@code null} otherwise
	 */
	@Override
	public RequestMappingInfo getMatchingCondition(HttpServletRequest request) {
		RequestMethodsRequestCondition methods = this.methodsCondition.getMatchingCondition(request);
		ParamsRequestCondition params = this.paramsCondition.getMatchingCondition(request);
		HeadersRequestCondition headers = this.headersCondition.getMatchingCondition(request);
		ConsumesRequestCondition consumes = this.consumesCondition.getMatchingCondition(request);
		ProducesRequestCondition produces = this.producesCondition.getMatchingCondition(request);

		if (methods == null || params == null || headers == null || consumes == null || produces == null) {
			if (CorsUtils.isPreFlightRequest(request)) {
				methods = getAccessControlRequestMethodCondition(request);
				if (methods == null || params == null) {
					return null;
				}
			}
			else {
				return null;
			}
		}

		PatternsRequestCondition patterns = this.patternsCondition.getMatchingCondition(request);
		if (patterns == null) {
			return null;
		}

		RequestConditionHolder custom = this.customConditionHolder.getMatchingCondition(request);
		if (custom == null) {
			return null;
		}

		return new RequestMappingInfo(this.name, patterns,
				methods, params, headers, consumes, produces, custom.getCondition());
	}
```

27. HandlerMapping的作用：
- 注册并存储映射关系。如RequestMappingInfo->HandlerMethod，最后返回HandlerExecutionChain
- 根据HttpServletRequest查找处理器。HttpServletRequest->RequestMappingInfo -> HandlerMethod

28. 返回值处理，不加@RequestBody注解，对应返回值处理器
- 返回String，逻辑视图名。
- 返回View，可以将model添加到view中
- 返回Map
- 返回ModelAndView

27. @RequestMapping -> RequestMappingInfo -> PatternRequestCondition/RequestMethodsRequestCondition等 -> PathMatcher/PathHelper/getMatchingCondition -> AntPatternComparator(path)/getPatternComparator(lookupPath)

28. RequestMappingHandlerAdapter调用handler的核心方法，底层针对每个Request，委托给ServletInvocableHandlerMethod进行反射调用，返回ModelAndView。对于每个Request，都创建一个DataBinderFactory，DataBinderFactory为每个入参创建一个DataBinder。DataBinderFactory传递给HandlerMethodArgumentResolver使用。注意每个参数解析只能使用一个参数解析器，先成立先得。
```

/** 
	 * Invoke the {@link RequestMapping} handler method preparing a {@link ModelAndView}
	 * if view resolution is required.
	 * @since 4.2
	 * @see #createInvocableHandlerMethod(HandlerMethod)
	 */
	protected ModelAndView invokeHandlerMethod(HttpServletRequest request,
			HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

		ServletWebRequest webRequest = new ServletWebRequest(request, response);

		// 封装了所有的@InitBinder的注解方法
		WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);
		ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);

		// HandlerMethod的封装
		ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
		invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
		invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
		invocableMethod.setDataBinderFactory(binderFactory);
		invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

		ModelAndViewContainer mavContainer = new ModelAndViewContainer();
		mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
		modelFactory.initModel(webRequest, mavContainer, invocableMethod);
		mavContainer.setIgnoreDefaultModelOnRedirect(this.ignoreDefaultModelOnRedirect);

		AsyncWebRequest asyncWebRequest = WebAsyncUtils.createAsyncWebRequest(request, response);
		asyncWebRequest.setTimeout(this.asyncRequestTimeout);

		WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
		asyncManager.setTaskExecutor(this.taskExecutor);
		asyncManager.setAsyncWebRequest(asyncWebRequest);
		asyncManager.registerCallableInterceptors(this.callableInterceptors);
		asyncManager.registerDeferredResultInterceptors(this.deferredResultInterceptors);

		if (asyncManager.hasConcurrentResult()) {
			Object result = asyncManager.getConcurrentResult();
			mavContainer = (ModelAndViewContainer) asyncManager.getConcurrentResultContext()[0];
			asyncManager.clearConcurrentResult();
			if (logger.isDebugEnabled()) {
				logger.debug("Found concurrent result value [" + result + "]");
			}
			invocableMethod = invocableMethod.wrapConcurrentResult(result);
		}

		// 调用
		invocableMethod.invokeAndHandle(webRequest, mavContainer);
		if (asyncManager.isConcurrentHandlingStarted()) {
			return null;
		}

		return getModelAndView(mavContainer, modelFactory, webRequest);
	}
	/**
     * TODO 先放全局@Controller中@InitBinder方法，再添加当前Handler类中的@InitBinder方法
     * @param handlerMethod
     * @return
     * @throws Exception
     */
	private WebDataBinderFactory getDataBinderFactory(HandlerMethod handlerMethod) throws Exception {
		Class<?> handlerType = handlerMethod.getBeanType();
		Set<Method> methods = this.initBinderCache.get(handlerType);
		// TODO 寻找当前HandlerMethod所在类的@InitBinder注解
		if (methods == null) {
			methods = MethodIntrospector.selectMethods(handlerType, INIT_BINDER_METHODS);
			this.initBinderCache.put(handlerType, methods);
		}
		List<InvocableHandlerMethod> initBinderMethods = new ArrayList<InvocableHandlerMethod>();
		// Global methods first
		// TODO @ControllerAdvice中的@InitBinder??
		for (Entry<ControllerAdviceBean, Set<Method>> entry : this.initBinderAdviceCache.entrySet()) {
			if (entry.getKey().isApplicableToBeanType(handlerType)) {
				Object bean = entry.getKey().resolveBean();
				for (Method method : entry.getValue()) {
					initBinderMethods.add(createInitBinderMethod(bean, method));
				}
			}
		}
		for (Method method : methods) {
			Object bean = handlerMethod.getBean();
			initBinderMethods.add(createInitBinderMethod(bean, method));
		}
		return createDataBinderFactory(initBinderMethods);
	}

    /**
     * TODO 封装每个@InitBinder方法，还是使用InvocableHandlerMethod
     * @param bean
     * @param method
     * @return
     */
	private InvocableHandlerMethod createInitBinderMethod(Object bean, Method method) {
		InvocableHandlerMethod binderMethod = new InvocableHandlerMethod(bean, method);
		// 参数解析为initBinderArgumentResolvers
		binderMethod.setHandlerMethodArgumentResolvers(this.initBinderArgumentResolvers);
		// todo 引进binder全局初始化器
		binderMethod.setDataBinderFactory(new DefaultDataBinderFactory(this.webBindingInitializer));
		binderMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);
		return binderMethod;
	}

	/**
	 * Template method to create a new InitBinderDataBinderFactory instance.
	 * <p>The default implementation creates a ServletRequestDataBinderFactory.
	 * This can be overridden for custom ServletRequestDataBinder subclasses.
	 * @param binderMethods {@code @InitBinder} methods
	 * @return the InitBinderDataBinderFactory instance to use
	 * @throws Exception in case of invalid state or arguments
	 */
	protected InitBinderDataBinderFactory createDataBinderFactory(List<InvocableHandlerMethod> binderMethods)
			throws Exception {

	    // todo 数据绑定工厂，其中getWebBindingInitializer有注入DefaultFormattingConversionService
		return new ServletRequestDataBinderFactory(binderMethods, getWebBindingInitializer());
	}
	
	// 对应的方法在ServletInvocableHandlerMethod
	/**
	 * Invokes the method and handles the return value through one of the
	 * configured {@link HandlerMethodReturnValueHandler}s.
	 * @param webRequest the current request
	 * @param mavContainer the ModelAndViewContainer for this request
	 * @param providedArgs "given" arguments matched by type (not resolved)
	 */
	public void invokeAndHandle(ServletWebRequest webRequest,
			ModelAndViewContainer mavContainer, Object... providedArgs) throws Exception {

		Object returnValue = invokeForRequest(webRequest, mavContainer, providedArgs);
		setResponseStatus(webRequest);

		if (returnValue == null) {
			if (isRequestNotModified(webRequest) || hasResponseStatus() || mavContainer.isRequestHandled()) {
				mavContainer.setRequestHandled(true);
				return;
			}
		}
		else if (StringUtils.hasText(this.responseReason)) {
			mavContainer.setRequestHandled(true);
			return;
		}

		mavContainer.setRequestHandled(false);
		try {
		    // mavContainer可能会设置已经处理了
			this.returnValueHandlers.handleReturnValue(
					returnValue, getReturnValueType(returnValue), mavContainer, webRequest);
		}
		catch (Exception ex) {
			if (logger.isTraceEnabled()) {
				logger.trace(getReturnValueHandlingErrorMessage("Error handling return value", returnValue), ex);
			}
			throw ex;
		}
	}
	//  对应InvocableHandlerMethod中有InvokeForRequest
	/**
	 * Invoke the method after resolving its argument values in the context of the given request.
	 * <p>Argument values are commonly resolved through {@link HandlerMethodArgumentResolver}s.
	 * The {@code providedArgs} parameter however may supply argument values to be used directly,
	 * i.e. without argument resolution. Examples of provided argument values include a
	 * {@link WebDataBinder}, a {@link SessionStatus}, or a thrown exception instance.
	 * Provided argument values are checked before argument resolvers.
	 * @param request the current request
	 * @param mavContainer the ModelAndViewContainer for this request
	 * @param providedArgs "given" arguments matched by type, not resolved
	 * @return the raw value returned by the invoked method
	 * @exception Exception raised if no suitable argument resolver can be found,
	 * or if the method raised an exception
	 */
	public Object invokeForRequest(NativeWebRequest request, ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {

		Object[] args = getMethodArgumentValues(request, mavContainer, providedArgs);
		if (logger.isInfoEnabled()) {
			StringBuilder sb = new StringBuilder("调用 [");
			sb.append(getBeanType().getSimpleName()).append(".");
			sb.append(getMethod().getName()).append("] 方法，参数为 ");
			sb.append(Arrays.asList(args));
			logger.info(sb.toString());
		}
		Object returnValue = doInvoke(args);
		if (logger.isInfoEnabled()) {
			logger.info("方法 [" + getMethod().getName() + "] 返回 [" + returnValue + "]");
		}
		return returnValue;
	}
	// InvocableHandlerMehtod中的getMethodArgumentValues
	/**
	 * Get the method argument values for the current request.
	 */
	private Object[] getMethodArgumentValues(NativeWebRequest request, ModelAndViewContainer mavContainer,
			Object... providedArgs) throws Exception {

        // 获得当前HandlerMethod的所有入参
		MethodParameter[] parameters = getMethodParameters();
		Object[] args = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			MethodParameter parameter = parameters[i];
			parameter.initParameterNameDiscovery(this.parameterNameDiscoverer);
			GenericTypeResolver.resolveParameterType(parameter, getBean().getClass());
			logger.info("得到参数类型：" + parameter.getParameterType().getName());
			// 看提供的providedArgs能否使用，按照类型匹配
			args[i] = resolveProvidedArgument(parameter, providedArgs);
			if (args[i] != null) {
				continue;
			}
			if (this.argumentResolvers.supportsParameter(parameter)) {
				try {
				    // DataBinderFactory作为参数解析器的入参之一。
					args[i] = this.argumentResolvers.resolveArgument(
							parameter, mavContainer, request, this.dataBinderFactory);
					continue;
				}
				catch (Exception ex) {
					if (logger.isDebugEnabled()) {
						logger.debug(getArgumentResolutionErrorMessage("Error resolving argument", i), ex);
					}
					throw ex;
				}
			}
			if (args[i] == null) {
				String msg = getArgumentResolutionErrorMessage("No suitable resolver for argument", i);
				throw new IllegalStateException(msg);
			}
		}
		return args;
	}
	// HandlerMethodArgumentResolverComposite是否支持该parameter，整个类如下：
	// 也就是先找到能处理该parameter的HandlerMethodArgumentResolver，然后调用resovler进行解析
	// 注意该接口也实现了HandlerMethodArgumentResolver

package org.springframework.web.method.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves method parameters by delegating to a list of registered {@link HandlerMethodArgumentResolver}s.
 * Previously resolved method parameters are cached for faster lookups.
 *
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public class HandlerMethodArgumentResolverComposite implements HandlerMethodArgumentResolver {

	protected final Log logger = LogFactory.getLog(getClass());

	private final List<HandlerMethodArgumentResolver> argumentResolvers =
			new LinkedList<HandlerMethodArgumentResolver>();

	private final Map<MethodParameter, HandlerMethodArgumentResolver> argumentResolverCache =
			new ConcurrentHashMap<MethodParameter, HandlerMethodArgumentResolver>(256);


	/**
	 * Add the given {@link HandlerMethodArgumentResolver}.
	 */
	public HandlerMethodArgumentResolverComposite addResolver(HandlerMethodArgumentResolver resolver) {
		this.argumentResolvers.add(resolver);
		return this;
	}

	/**
	 * Add the given {@link HandlerMethodArgumentResolver}s.
	 */
	public HandlerMethodArgumentResolverComposite addResolvers(List<? extends HandlerMethodArgumentResolver> resolvers) {
		if (resolvers != null) {
			for (HandlerMethodArgumentResolver resolver : resolvers) {
				this.argumentResolvers.add(resolver);
			}
		}
		return this;
	}

	/**
	 * Return a read-only list with the contained resolvers, or an empty list.
	 */
	public List<HandlerMethodArgumentResolver> getResolvers() {
		return Collections.unmodifiableList(this.argumentResolvers);
	}


	/**
	 * Whether the given {@linkplain MethodParameter method parameter} is supported by any registered
	 * {@link HandlerMethodArgumentResolver}.
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return (getArgumentResolver(parameter) != null);
	}

	/**
	 * Iterate over registered {@link HandlerMethodArgumentResolver}s and invoke the one that supports it.
	 * @throws IllegalStateException if no suitable {@link HandlerMethodArgumentResolver} is found.
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		HandlerMethodArgumentResolver resolver = getArgumentResolver(parameter);
		if (resolver == null) {
			throw new IllegalArgumentException("Unknown parameter type [" + parameter.getParameterType().getName() + "]");
		}
		return resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
	}

	/**
	 * Find a registered {@link HandlerMethodArgumentResolver} that supports the given method parameter.
	 */
	private HandlerMethodArgumentResolver getArgumentResolver(MethodParameter parameter) {
		HandlerMethodArgumentResolver result = this.argumentResolverCache.get(parameter);
		if (result == null) {
			for (HandlerMethodArgumentResolver methodArgumentResolver : this.argumentResolvers) {
				if (logger.isInfoEnabled()) {
					logger.info("测试参数解析器 [" + methodArgumentResolver + "] 是否支持 [" +
							parameter.getGenericParameterType() + "]");
				}
				if (methodArgumentResolver.supportsParameter(parameter)) {
					logger.info("参数解析器 [" + methodArgumentResolver + "] 支持 [" +
							parameter.getGenericParameterType() + "]");
					result = methodArgumentResolver;
					this.argumentResolverCache.put(parameter, result);
					break;
				}
			}
		}
		return result;
	}

}
```
29. 具体的HandlerMethodArgumentComposite设置是在RequestMappingHandlerAdapter#afterPropertiesSet中：
```
@Override
	public void afterPropertiesSet() {
		// Do this first, it may add ResponseBody advice beans
		initControllerAdviceCache();

		if (this.argumentResolvers == null) {
			List<HandlerMethodArgumentResolver> resolvers = getDefaultArgumentResolvers();
			this.argumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
			logger.info("参数解析器为空");
		}
		if (this.initBinderArgumentResolvers == null) {
            logger.info("InitBinder解析器为空");
            List<HandlerMethodArgumentResolver> resolvers = getDefaultInitBinderArgumentResolvers();
			this.initBinderArgumentResolvers = new HandlerMethodArgumentResolverComposite().addResolvers(resolvers);
		}
		if (this.returnValueHandlers == null) {
		    logger.info("返回值处理器为空");
			List<HandlerMethodReturnValueHandler> handlers = getDefaultReturnValueHandlers();
			this.returnValueHandlers = new HandlerMethodReturnValueHandlerComposite().addHandlers(handlers);
		}
	}
	
	/**
	 * Return the list of argument resolvers to use including built-in resolvers
	 * and custom resolvers provided via {@link #setCustomArgumentResolvers}.
	 */
	private List<HandlerMethodArgumentResolver> getDefaultArgumentResolvers() {
		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<HandlerMethodArgumentResolver>();

		// Annotation-based argument resolution
		// 这里为false，下面会有一个true的
		// 标有注解@RequestParam
		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), false));
		resolvers.add(new RequestParamMapMethodArgumentResolver());
		resolvers.add(new PathVariableMethodArgumentResolver());
		resolvers.add(new PathVariableMapMethodArgumentResolver());
		resolvers.add(new MatrixVariableMethodArgumentResolver());
		resolvers.add(new MatrixVariableMapMethodArgumentResolver());
		// 这里是false，下面会有一个true的
		resolvers.add(new ServletModelAttributeMethodProcessor(false));
		resolvers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));
		resolvers.add(new RequestPartMethodArgumentResolver(getMessageConverters(), this.requestResponseBodyAdvice));
		resolvers.add(new RequestHeaderMethodArgumentResolver(getBeanFactory()));
		resolvers.add(new RequestHeaderMapMethodArgumentResolver());
		resolvers.add(new ServletCookieValueMethodArgumentResolver(getBeanFactory()));
		resolvers.add(new ExpressionValueMethodArgumentResolver(getBeanFactory()));

		// Type-based argument resolution
		resolvers.add(new ServletRequestMethodArgumentResolver());
		resolvers.add(new ServletResponseMethodArgumentResolver());
		resolvers.add(new HttpEntityMethodProcessor(getMessageConverters(), this.requestResponseBodyAdvice));
		resolvers.add(new RedirectAttributesMethodArgumentResolver());
		resolvers.add(new ModelMethodProcessor());
		resolvers.add(new MapMethodProcessor());
		// TODO Errors注入
		resolvers.add(new ErrorsMethodArgumentResolver());
		resolvers.add(new SessionStatusMethodArgumentResolver());
		resolvers.add(new UriComponentsBuilderMethodArgumentResolver());

		// 自定义参数解析器，TODO 待验证
		if (getCustomArgumentResolvers() != null) {
			resolvers.addAll(getCustomArgumentResolvers());
		}

		// Catch-all
		resolvers.add(new RequestParamMethodArgumentResolver(getBeanFactory(), true));
		// TODO 兜底的那一个，对复杂model进行注入
		resolvers.add(new ServletModelAttributeMethodProcessor(true));

		return resolvers;
	}
	
	// 同理，对应的ReturnValueResolver注入：
	/**
	 * Return the list of return value handlers to use including built-in and
	 * custom handlers provided via {@link #setReturnValueHandlers}.
	 */
	private List<HandlerMethodReturnValueHandler> getDefaultReturnValueHandlers() {
		List<HandlerMethodReturnValueHandler> handlers = new ArrayList<HandlerMethodReturnValueHandler>();

		// Single-purpose return value types
		handlers.add(new ModelAndViewMethodReturnValueHandler());
		handlers.add(new ModelMethodProcessor());
		handlers.add(new ViewMethodReturnValueHandler());
		handlers.add(new ResponseBodyEmitterReturnValueHandler(getMessageConverters()));
		handlers.add(new StreamingResponseBodyReturnValueHandler());
		handlers.add(new HttpEntityMethodProcessor(getMessageConverters(),
				this.contentNegotiationManager, this.requestResponseBodyAdvice));
		handlers.add(new HttpHeadersReturnValueHandler());
		handlers.add(new CallableMethodReturnValueHandler());
		handlers.add(new DeferredResultMethodReturnValueHandler());
		handlers.add(new AsyncTaskMethodReturnValueHandler(this.beanFactory));
		handlers.add(new ListenableFutureReturnValueHandler());
		if (completionStagePresent) {
			handlers.add(new CompletionStageReturnValueHandler());
		}

		// Annotation-based return value types
		handlers.add(new ModelAttributeMethodProcessor(false));
		// TODO 处理@ResponseBody返回值
		handlers.add(new RequestResponseBodyMethodProcessor(getMessageConverters(),
				this.contentNegotiationManager, this.requestResponseBodyAdvice));

		// Multi-purpose return value types
		handlers.add(new ViewNameMethodReturnValueHandler());
		handlers.add(new MapMethodProcessor());

		// Custom return value types
		if (getCustomReturnValueHandlers() != null) {
			handlers.addAll(getCustomReturnValueHandlers());
		}

		// Catch-all
		if (!CollectionUtils.isEmpty(getModelAndViewResolvers())) {
			handlers.add(new ModelAndViewResolverMethodReturnValueHandler(getModelAndViewResolvers()));
		}
		else {
			handlers.add(new ModelAttributeMethodProcessor(true));
		}

		return handlers;
	}
```

30. DataBinder中的BindindResult构建：
```
/**
	 * Create the {@link AbstractPropertyBindingResult} instance using standard
	 * JavaBean property access.
	 * @since 4.2.1
	 */
	protected AbstractPropertyBindingResult createBeanPropertyBindingResult() {
	    // getTarget为方法入参？？有ConversionService??
		BeanPropertyBindingResult result = new BeanPropertyBindingResult(getTarget(),
				getObjectName(), isAutoGrowNestedPaths(), getAutoGrowCollectionLimit());
		if (this.conversionService != null) {
			result.initConversion(this.conversionService);
		}
		return result;
	}
```
31. ServletRequest -> MutablePropertyValues进行转换，将Request转化为PV对。
```
MutablePropertyValues mpvs = new ServletRequestParameterPropertyValues(request);
```
34. InitBinder的一些方法也使用InvocableHandlerMethod进行调用。
35. 每个Request都会构建一个ServletInvocableHandlerMethod，并创建一个binderFactory。
36. HandlerMethodArgumentResolver和HandlerMethodReturnValueHandler的子类实现时需要使用DataBinder进行参数绑定，从而解析参数。具体的binder是通过BinderFactory创建的。 binderFactory创建binder转换参数时，注入了WebBindingInitializer相关设置和调用了匹配的所有＠InitBinder方法对binder进行初始化，binder作为最后一个入参。最后binder对参数值进行转换。
37. binder底层存了一个SimpleTypeConverter，可以注入PropertyEditor、ConversionService进行转换。DataBinder实现了TypeConverter和PropertyEditorRegistry两个接口，将CustomePropertyEditor注入后，实际是调用SimpleTypeConverter的registerCustomEditor方法，convertIfNecessary实际是调用PropertyEditorRegistry的findCustomEditor方法，以及一些默认的PropertyEditor方法。
39. ConversionService也可以进行参数转换，是用来替换PropertyEditor的。ConversionService是spring的，PropertyEditor是java beans定义的。
40. XXXSupport都是XXX的实现类，但是可能是抽象类。
42. SimpleTypeConverter底层委托TypeConverterDelegate来实现转化。实际注入时，底层注入CustomPropertyEditor到PropertyRegistrySupport中（SimpleTypeConverter）。由TypeConverterDelegate来调convertIfNecessary，而TypeConverterDelegate又来调propertyEditorRegistrySupport，也就是SimpleTypeConverter这个类。。所以PropertyEditorRegistry是PropertyEditor的注册工厂。而TypeConverter使用这个工厂来获取PropertyEditor，完成属性转换的逻辑。
```
/**
 TypeConverterSupport继承了PropertyEditorRegistrySupport，从而有了PropertyEditorRegistrySupport的功能。findCustomEditor和registorCustomEditor。
 实现了TypeConverter的接口方法，convertIfNecessary，使用的是typeConverterDelegate来进行convert，实际利用了注入的PropertyEditor，TypeConverterDelegate有部分逻辑。
*/
public class SimpleTypeConverter extends TypeConverterSupport {

	public SimpleTypeConverter() {
	   // 初始化父类中的属性
		this.typeConverterDelegate = new TypeConverterDelegate(this);
		// 启用默认的属性编辑器。
		registerDefaultEditors();
	}
}

public abstract class TypeConverterSupport extends PropertyEditorRegistrySupport implements TypeConverter {
```
42. 区别
- SimpleTypeConverter convertIfNecessay，仅仅进行转换。
- BeanWrapperImpl，除了转换，有设置值的功能。

43. 对于@ResponseBody则使用RequestResponseMethodProcessor进行处理。对于没有使用注解，且返回String的，使用ViewNameMethodReturnValueHandler将String作为逻辑视图名，如果String中含有redirect:，则设置重定向字段这个handler在RequestResponseMethodProcessor之后。
44. mavContainer.setRequestHandled(true);表示在HandlerAdapter阶段就已经响应了，不用再渲染view。
45. 继承PropertyEditorSupport，覆盖setAsText即可，setAsText最后要把真正的值设置到setValue中。保证getValue时能够获取到。
46. context:component-scan对应的NamespaceHandler为：ContextNamespaceHandler，注入的parser为：ComponentScanBeanDefinitionParser，会注入常用的BeanPostProcessor，注意查看类是如何变成BeanDefinition的。
 * @see ContextAnnotationAutowireCandidateResolver
 * @see CommonAnnotationBeanPostProcessor
 * @see ConfigurationClassPostProcessor
 * @see org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor
 * @see org.springframework.beans.factory.annotation.RequiredAnnotationBeanPostProcessor
 * @see org.springframework.orm.jpa.support.PersistenceAnnotationBeanPostProcessor
```
protected void registerComponents(
			XmlReaderContext readerContext, Set<BeanDefinitionHolder> beanDefinitions, Element element) {

		Object source = readerContext.extractSource(element);
		CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), source);

		for (BeanDefinitionHolder beanDefHolder : beanDefinitions) {
			compositeDef.addNestedComponent(new BeanComponentDefinition(beanDefHolder));
		}

		// Register annotation config processors, if necessary.
		boolean annotationConfig = true;
		if (element.hasAttribute(ANNOTATION_CONFIG_ATTRIBUTE)) {
			annotationConfig = Boolean.valueOf(element.getAttribute(ANNOTATION_CONFIG_ATTRIBUTE));
		}
		// 默认会注入BeanPostProcessor
		if (annotationConfig) {
			Set<BeanDefinitionHolder> processorDefinitions =
					AnnotationConfigUtils.registerAnnotationConfigProcessors(readerContext.getRegistry(), source);
			for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
				compositeDef.addNestedComponent(new BeanComponentDefinition(processorDefinition));
			}
		}

		readerContext.fireComponentRegistered(compositeDef);
	}
	
	// AnnotationConfigUtils注入常用的BeanPostProcessor，如下：
	/**
	 * Register all relevant annotation post processors in the given registry.
	 * @param registry the registry to operate on
	 * @param source the configuration source element (already extracted)
	 * that this registration was triggered from. May be {@code null}.
	 * @return a Set of BeanDefinitionHolders, containing all bean definitions
	 * that have actually been registered by this call
	 */
	public static Set<BeanDefinitionHolder> registerAnnotationConfigProcessors(
			BeanDefinitionRegistry registry, Object source) {

		DefaultListableBeanFactory beanFactory = unwrapDefaultListableBeanFactory(registry);
		// 除了注册BPP外，还设置了相关组件到BeanFacctory中
		if (beanFactory != null) {
			if (!(beanFactory.getDependencyComparator() instanceof AnnotationAwareOrderComparator)) {
				beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE);
			}
			if (!(beanFactory.getAutowireCandidateResolver() instanceof ContextAnnotationAutowireCandidateResolver)) {
			
			// 注入CandidateResolver，获得@Value上的值	beanFactory.setAutowireCandidateResolver(new ContextAnnotationAutowireCandidateResolver());
			}
		}

		Set<BeanDefinitionHolder> beanDefs = new LinkedHashSet<BeanDefinitionHolder>(4);

        // 类不存在，才引入
		if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(ConfigurationClassPostProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(AutowiredAnnotationBeanPostProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		if (!registry.containsBeanDefinition(REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(RequiredAnnotationBeanPostProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, REQUIRED_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		// Check for JSR-250 support, and if present add the CommonAnnotationBeanPostProcessor.
		if (jsr250Present && !registry.containsBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(CommonAnnotationBeanPostProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, COMMON_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		// Check for JPA support, and if present add the PersistenceAnnotationBeanPostProcessor.
		if (jpaPresent && !registry.containsBeanDefinition(PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition();
			try {
				def.setBeanClass(ClassUtils.forName(PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME,
						AnnotationConfigUtils.class.getClassLoader()));
			}
			catch (ClassNotFoundException ex) {
				throw new IllegalStateException(
						"Cannot load optional framework class: " + PERSISTENCE_ANNOTATION_PROCESSOR_CLASS_NAME, ex);
			}
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, PERSISTENCE_ANNOTATION_PROCESSOR_BEAN_NAME));
		}

		if (!registry.containsBeanDefinition(EVENT_LISTENER_PROCESSOR_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(EventListenerMethodProcessor.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_PROCESSOR_BEAN_NAME));
		}
		// TODO 添加EventListener相关注解的处理？？
		if (!registry.containsBeanDefinition(EVENT_LISTENER_FACTORY_BEAN_NAME)) {
			RootBeanDefinition def = new RootBeanDefinition(DefaultEventListenerFactory.class);
			def.setSource(source);
			beanDefs.add(registerPostProcessor(registry, def, EVENT_LISTENER_FACTORY_BEAN_NAME));
		}

		return beanDefs;
	}
```
47. (ContextAnnotationAutowireCandidateResolver)QualifierAnnotationAutowireCandidateResolver和AutowiredAnnotationBeanPostProcessor用于处理@Value注解，
- AutowiredAnnotationBeanPostProcessor主要是找到有@Value这个注解的属性或者方法，并进行postProcessPropertyValues，进行依赖注入，DefaultListableBeanFactory#doResolveDependency调用CandidateResolver。
- QualifierAnnotationAutowireCandidateResolver获得注解上的值。如${abc}
- 通过BeanFactory的embeddedValueResolvers(List<StringValueResolver>)解析该value。见AbstractBeanFactory#resolveEmbeddedValue
- 最后使用beanExpressionResolver解析得到的值，SPEL？
48. 当定义context:property-holder时，不仅会处理当前beanFactory所在的${}注解，还会将注入StringValueResolver到BeanFactory中，保证对注解@Value也进行处理，见PropertyholderConfigurerSupport。实际就是注入PropertySourcesPlaceholderConfigurer，该class为BeanFactoryPostProcessor，会对当前所在的BeanFactory的BeanDefinition进行${}解析。
```
protected void doProcessProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
			StringValueResolver valueResolver) {

		BeanDefinitionVisitor visitor = new BeanDefinitionVisitor(valueResolver);

		// TODO 只返回beanFactoryToProcess中所有bean的Name，不考虑父类中的
		String[] beanNames = beanFactoryToProcess.getBeanDefinitionNames();
		for (String curName : beanNames) {
			// Check that we're not parsing our own bean definition,
			// to avoid failing on unresolvable placeholders in properties file locations.
			if (!(curName.equals(this.beanName) && beanFactoryToProcess.equals(this.beanFactory))) {
				BeanDefinition bd = beanFactoryToProcess.getBeanDefinition(curName);
				try {
					// TODO 访问者模式，抽取公共类
					visitor.visitBeanDefinition(bd);
				}
				catch (Exception ex) {
					throw new BeanDefinitionStoreException(bd.getResourceDescription(), curName, ex.getMessage(), ex);
				}
			}
		}

		// New in Spring 2.5: resolve placeholders in alias target names and aliases as well.
		beanFactoryToProcess.resolveAliases(valueResolver);

		// New in Spring 3.0: resolve placeholders in embedded values such as annotation attributes.
		// TODO 对所在BeanFactory的bean的@Value进行解析
		beanFactoryToProcess.addEmbeddedValueResolver(valueResolver);
	}
	
	// BeanDeifinitionVisitor
package org.springframework.beans.factory.config;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringValueResolver;

/**
主要针对PropertyValues和Constructor-args
 * Visitor class for traversing {@link BeanDefinition} objects, in particular
 * the property values and constructor argument values contained in them,
 * resolving bean metadata values.
 *
 * <p>Used by {@link PropertyPlaceholderConfigurer} to parse all String values
 * contained in a BeanDefinition, resolving any placeholders found.
 *
 * @author Juergen Hoeller
 * @author Sam Brannen
 * @since 1.2
 * @see BeanDefinition
 * @see BeanDefinition#getPropertyValues
 * @see BeanDefinition#getConstructorArgumentValues
 * @see PropertyPlaceholderConfigurer
 */
public class BeanDefinitionVisitor {

	private StringValueResolver valueResolver;


	/**
	 * Create a new BeanDefinitionVisitor, applying the specified
	 * value resolver to all bean metadata values.
	 * @param valueResolver the StringValueResolver to apply
	 */
	public BeanDefinitionVisitor(StringValueResolver valueResolver) {
		Assert.notNull(valueResolver, "StringValueResolver must not be null");
		this.valueResolver = valueResolver;
	}

	/**
	 * Create a new BeanDefinitionVisitor for subclassing.
	 * Subclasses need to override the {@link #resolveStringValue} method.
	 */
	protected BeanDefinitionVisitor() {
	}


	/**
	 * Traverse the given BeanDefinition object and the MutablePropertyValues
	 * and ConstructorArgumentValues contained in them.
	 * @param beanDefinition the BeanDefinition object to traverse
	 * @see #resolveStringValue(String)
	 */
	public void visitBeanDefinition(BeanDefinition beanDefinition) {
		visitParentName(beanDefinition);
		visitBeanClassName(beanDefinition);
		visitFactoryBeanName(beanDefinition);
		visitFactoryMethodName(beanDefinition);
		visitScope(beanDefinition);
		visitPropertyValues(beanDefinition.getPropertyValues());
		ConstructorArgumentValues cas = beanDefinition.getConstructorArgumentValues();
		visitIndexedArgumentValues(cas.getIndexedArgumentValues());
		visitGenericArgumentValues(cas.getGenericArgumentValues());
	}

	protected void visitParentName(BeanDefinition beanDefinition) {
		String parentName = beanDefinition.getParentName();
		if (parentName != null) {
			String resolvedName = resolveStringValue(parentName);
			if (!parentName.equals(resolvedName)) {
				beanDefinition.setParentName(resolvedName);
			}
		}
	}

	protected void visitBeanClassName(BeanDefinition beanDefinition) {
		String beanClassName = beanDefinition.getBeanClassName();
		if (beanClassName != null) {
			String resolvedName = resolveStringValue(beanClassName);
			if (!beanClassName.equals(resolvedName)) {
				beanDefinition.setBeanClassName(resolvedName);
			}
		}
	}

	protected void visitFactoryBeanName(BeanDefinition beanDefinition) {
		String factoryBeanName = beanDefinition.getFactoryBeanName();
		if (factoryBeanName != null) {
			String resolvedName = resolveStringValue(factoryBeanName);
			if (!factoryBeanName.equals(resolvedName)) {
				beanDefinition.setFactoryBeanName(resolvedName);
			}
		}
	}

	protected void visitFactoryMethodName(BeanDefinition beanDefinition) {
		String factoryMethodName = beanDefinition.getFactoryMethodName();
		if (factoryMethodName != null) {
			String resolvedName = resolveStringValue(factoryMethodName);
			if (!factoryMethodName.equals(resolvedName)) {
				beanDefinition.setFactoryMethodName(resolvedName);
			}
		}
	}
    // ${scope}可配
	protected void visitScope(BeanDefinition beanDefinition) {
		String scope = beanDefinition.getScope();
		if (scope != null) {
			String resolvedScope = resolveStringValue(scope);
			if (!scope.equals(resolvedScope)) {
				beanDefinition.setScope(resolvedScope);
			}
		}
	}

	protected void visitPropertyValues(MutablePropertyValues pvs) {
		PropertyValue[] pvArray = pvs.getPropertyValues();
		for (PropertyValue pv : pvArray) {
			Object newVal = resolveValue(pv.getValue());
			if (!ObjectUtils.nullSafeEquals(newVal, pv.getValue())) {
				pvs.add(pv.getName(), newVal);
			}
		}
	}
    // 按照索引构造？
	protected void visitIndexedArgumentValues(Map<Integer, ConstructorArgumentValues.ValueHolder> ias) {
		for (ConstructorArgumentValues.ValueHolder valueHolder : ias.values()) {
			Object newVal = resolveValue(valueHolder.getValue());
			if (!ObjectUtils.nullSafeEquals(newVal, valueHolder.getValue())) {
				valueHolder.setValue(newVal);
			}
		}
	}
    // 按照类型构造？
	protected void visitGenericArgumentValues(List<ConstructorArgumentValues.ValueHolder> gas) {
		for (ConstructorArgumentValues.ValueHolder valueHolder : gas) {
			Object newVal = resolveValue(valueHolder.getValue());
			if (!ObjectUtils.nullSafeEquals(newVal, valueHolder.getValue())) {
				valueHolder.setValue(newVal);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected Object resolveValue(Object value) {
		if (value instanceof BeanDefinition) {
			visitBeanDefinition((BeanDefinition) value);
		}
		else if (value instanceof BeanDefinitionHolder) {
			visitBeanDefinition(((BeanDefinitionHolder) value).getBeanDefinition());
		}
		else if (value instanceof RuntimeBeanReference) {
			RuntimeBeanReference ref = (RuntimeBeanReference) value;
			String newBeanName = resolveStringValue(ref.getBeanName());
			if (!newBeanName.equals(ref.getBeanName())) {
				return new RuntimeBeanReference(newBeanName);
			}
		}
		else if (value instanceof RuntimeBeanNameReference) {
			// TODO 与RuntimeBeanReference的区别
			RuntimeBeanNameReference ref = (RuntimeBeanNameReference) value;
			String newBeanName = resolveStringValue(ref.getBeanName());
			if (!newBeanName.equals(ref.getBeanName())) {
				return new RuntimeBeanNameReference(newBeanName);
			}
		}
		else if (value instanceof Object[]) {
			visitArray((Object[]) value);
		}
		else if (value instanceof List) {
			visitList((List) value);
		}
		else if (value instanceof Set) {
			visitSet((Set) value);
		}
		else if (value instanceof Map) {
			visitMap((Map) value);
		}
		else if (value instanceof TypedStringValue) {
			TypedStringValue typedStringValue = (TypedStringValue) value;
			String stringValue = typedStringValue.getValue();
			if (stringValue != null) {
				String visitedString = resolveStringValue(stringValue);
				typedStringValue.setValue(visitedString);
			}
		}
		else if (value instanceof String) {
			return resolveStringValue((String) value);
		}
		return value;
	}
    // 逐一解析
	protected void visitArray(Object[] arrayVal) {
		for (int i = 0; i < arrayVal.length; i++) {
			Object elem = arrayVal[i];
			Object newVal = resolveValue(elem);
			if (!ObjectUtils.nullSafeEquals(newVal, elem)) {
				arrayVal[i] = newVal;
			}
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void visitList(List listVal) {
		for (int i = 0; i < listVal.size(); i++) {
			Object elem = listVal.get(i);
			Object newVal = resolveValue(elem);
			if (!ObjectUtils.nullSafeEquals(newVal, elem)) {
				listVal.set(i, newVal);
			}
		}
	}

    // LinkedHashSet保证有序
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void visitSet(Set setVal) {
		Set newContent = new LinkedHashSet();
		boolean entriesModified = false;
		for (Object elem : setVal) {
			int elemHash = (elem != null ? elem.hashCode() : 0);
			Object newVal = resolveValue(elem);
			int newValHash = (newVal != null ? newVal.hashCode() : 0);
			newContent.add(newVal);
			entriesModified = entriesModified || (newVal != elem || newValHash != elemHash);
		}
		if (entriesModified) {
			setVal.clear();
			setVal.addAll(newContent);
		}
	}
    // LinkedHashMap保证有序，key-value均更新
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void visitMap(Map<?, ?> mapVal) {
		Map newContent = new LinkedHashMap();
		boolean entriesModified = false;
		for (Map.Entry entry : mapVal.entrySet()) {
			Object key = entry.getKey();
			int keyHash = (key != null ? key.hashCode() : 0);
			Object newKey = resolveValue(key);
			int newKeyHash = (newKey != null ? newKey.hashCode() : 0);
			Object val = entry.getValue();
			Object newVal = resolveValue(val);
			newContent.put(newKey, newVal);
			entriesModified = entriesModified || (newVal != val || newKey != key || newKeyHash != keyHash);
		}
		if (entriesModified) {
			mapVal.clear();
			mapVal.putAll(newContent);
		}
	}

	/**
	 * Resolve the given String value, for example parsing placeholders.
	 * @param strVal the original String value
	 * @return the resolved String value
	 */
	protected String resolveStringValue(String strVal) {
		if (this.valueResolver == null) {
			throw new IllegalStateException("No StringValueResolver specified - pass a resolver " +
					"object into the constructor or override the 'resolveStringValue' method");
		}
		String resolvedValue = this.valueResolver.resolveStringValue(strVal);
		// Return original String if not modified.
		return (strVal.equals(resolvedValue) ? strVal : resolvedValue);
	}

}

```
49. DefaultListableBeanFactory中的beanDefinitionNames保存bean加入的顺序。且每次更新时是通过赋值来更新的，而不是通过add(element)。该字段加了volatile关键字。
50. 使用ServletInvocableHandlerMethod调用方法的，一般都能进行参数绑定和返回值处理，如@RequestParam/@ResonseBody等。
51. 泛型接口：
```
package org.springframework.format;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * A factory that creates formatters to format values of fields annotated with a particular
 * {@link Annotation}.
 *
 * <p>For example, a {@code DateTimeFormatAnnotationFormatterFactory} might create a formatter
 * that formats {@code Date} values set on fields annotated with {@code @DateTimeFormat}.
 *
 * @author Keith Donald
 * @since 3.0
 * @param <A> the annotation type that should trigger formatting
 */
public interface AnnotationFormatterFactory<A extends Annotation> {

	/**
	 * The types of fields that may be annotated with the &lt;A&gt; annotation.
	 */
	Set<Class<?>> getFieldTypes();

	/**
	 * 先将fieldType转化为T，再调用Printer<T>打印
	 * Get the Printer to print the value of a field of {@code fieldType} annotated with
	 * {@code annotation}.
	 * <p>If the type T the printer accepts is not assignable to {@code fieldType}, a
	 * coercion from {@code fieldType} to T will be attempted before the Printer is invoked.
	 * @param annotation the annotation instance
	 * @param fieldType the type of field that was annotated
	 * @return the printer
	 */
	Printer<?> getPrinter(A annotation, Class<?> fieldType);

	/**
	 * 如Printer<DateTime>，@DateTimeFormat Date date;
	 * 先解析为DateTime，再转化为Date
	 * Get the Parser to parse a submitted value for a field of {@code fieldType}
	 * annotated with {@code annotation}.
	 * <p>If the object the parser returns is not assignable to {@code fieldType},
	 * a coercion to {@code fieldType} will be attempted before the field is set.
	 * @param annotation the annotation instance
	 * @param fieldType the type of field that was annotated
	 * @return the parser
	 */
	Parser<?> getParser(A annotation, Class<?> fieldType);

}
// 一个实现类：
package org.springframework.format.datetime.joda;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.support.EmbeddedValueResolutionSupport;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;

/**
 * Formats fields annotated with the {@link DateTimeFormat} annotation using Joda-Time.
 * <p>
 * <p><b>NOTE:</b> Spring's Joda-Time support requires Joda-Time 2.x, as of Spring 4.0.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @see DateTimeFormat
 * @since 3.0
 */
@Slf4j
public class JodaDateTimeFormatAnnotationFormatterFactory extends EmbeddedValueResolutionSupport
        implements AnnotationFormatterFactory<DateTimeFormat> {

    private static final Set<Class<?>> FIELD_TYPES;

    static {
        // Create the set of field types that may be annotated with @DateTimeFormat.
        // Note: the 3 ReadablePartial concrete types are registered explicitly since
        // addFormatterForFieldType rules exist for each of these types
        // (if we did not do this, the default byType rules for LocalDate, LocalTime,
        // and LocalDateTime would take precedence over the annotation rule, which
        // is not what we want)
        Set<Class<?>> fieldTypes = new HashSet<Class<?>>(8);
        fieldTypes.add(ReadableInstant.class);
        fieldTypes.add(LocalDate.class);
        fieldTypes.add(LocalTime.class);
        fieldTypes.add(LocalDateTime.class);
        fieldTypes.add(Date.class);
        fieldTypes.add(Calendar.class);
        fieldTypes.add(Long.class);
        FIELD_TYPES = Collections.unmodifiableSet(fieldTypes);
    }


    @Override
    public final Set<Class<?>> getFieldTypes() {
        return FIELD_TYPES;
    }

    @Override
    public Printer<?> getPrinter(DateTimeFormat annotation, Class<?> fieldType) {
        DateTimeFormatter formatter = getFormatter(annotation, fieldType);
        if (ReadablePartial.class.isAssignableFrom(fieldType)) {
            return new ReadablePartialPrinter(formatter);
        } else if (ReadableInstant.class.isAssignableFrom(fieldType) || Calendar.class.isAssignableFrom(fieldType)) {
            // assumes Calendar->ReadableInstant converter is registered
            return new ReadableInstantPrinter(formatter);
        } else {
            // assumes Date->Long converter is registered
            return new MillisecondInstantPrinter(formatter);
        }
    }

    @Override
    public Parser<?> getParser(DateTimeFormat annotation, Class<?> fieldType) {
        log.info("获取打印器，属性为：{}，注解为：{}", fieldType, annotation);
        if (LocalDate.class == fieldType) {
            return new LocalDateParser(getFormatter(annotation, fieldType));
        } else if (LocalTime.class == fieldType) {
            return new LocalTimeParser(getFormatter(annotation, fieldType));
        } else if (LocalDateTime.class == fieldType) {
            return new LocalDateTimeParser(getFormatter(annotation, fieldType));
        } else {
            // 先转化为DateTime，再调DateTimeToDateConverter进行类型转换。
            return new DateTimeParser(getFormatter(annotation, fieldType));
        }
    }

    /**
     * Factory method used to create a {@link DateTimeFormatter}.
     *
     * @param annotation the format annotation for the field
     * @param fieldType  the type of field
     * @return a {@link DateTimeFormatter} instance
     * @since 3.2
     */
    protected DateTimeFormatter getFormatter(DateTimeFormat annotation, Class<?> fieldType) {
        DateTimeFormatterFactory factory = new DateTimeFormatterFactory();
        factory.setStyle(resolveEmbeddedValue(annotation.style()));
        factory.setIso(annotation.iso());
        factory.setPattern(resolveEmbeddedValue(annotation.pattern()));
        return factory.createDateTimeFormatter();
    }

}
```
52. 导入接口中定义的class并使用
```
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair;
```
53. mvc:annotation-driven默认会注入三个异常解析器：
```
// ExceptionHandlerExceptionResolver专门使用@ExceptionHandler来处理异常，包括在@ControllerAdvice和Controller中的@ExceptionHandler方法
ExceptionHandlerExceptionResolver order=0
// 在异常类上写上注解@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "参数非法")，将会返回异常信息
ResponseStatusExceptionResolver order=1
DefaultHandlerExceptionResolver order=2
```
&nbsp;&nbsp;&nbsp;&nbsp;默认注入FormattingConversionServiceFactoryBean，可以看到默认注入的ConversionService为DefaultFormattingConversionService
```
package org.springframework.format.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.convert.support.ConversionServiceFactory;
import org.springframework.format.*;
import org.springframework.util.StringValueResolver;

import java.util.Set;

/**
 * A factory providing convenient access to a {@code FormattingConversionService}
 * configured with converters and formatters for common types such as numbers and
 * datetimes.
 *
 * <p>Additional converters and formatters can be registered declaratively through
 * {@link #setConverters(Set)} and {@link #setFormatters(Set)}. Another option
 * is to register converters and formatters in code by implementing the
 * {@link FormatterRegistrar} interface. You can then configure provide the set
 * of registrars to use through {@link #setFormatterRegistrars(Set)}.
 *
 * <p>A good example for registering converters and formatters in code is
 * {@code JodaTimeFormatterRegistrar}, which registers a number of
 * date-related formatters and converters. For a more detailed list of cases
 * see {@link #setFormatterRegistrars(Set)}
 *
 * <p>Like all {@code FactoryBean} implementations, this class is suitable for
 * use when configuring a Spring application context using Spring {@code <beans>}
 * XML. When configuring the container with
 * {@link org.springframework.context.annotation.Configuration @Configuration}
 * classes, simply instantiate, configure and return the appropriate
 * {@code FormattingConversionService} object from a
 * {@link org.springframework.context.annotation.Bean @Bean} method.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @author Chris Beams
 * @since 3.0
 */
public class FormattingConversionServiceFactoryBean
		implements FactoryBean<FormattingConversionService>, EmbeddedValueResolverAware, InitializingBean {

	private Set<?> converters;

	private Set<?> formatters;

	private Set<FormatterRegistrar> formatterRegistrars;

	private boolean registerDefaultFormatters = true;

	private StringValueResolver embeddedValueResolver;

	/**
	 * 默认为FormattingConversionService
	 */
	private FormattingConversionService conversionService;


	/**
	 * Configure the set of custom converter objects that should be added.
	 * @param converters instances of any of the following:
	 * {@link org.springframework.core.convert.converter.Converter},
	 * {@link org.springframework.core.convert.converter.ConverterFactory},
	 * {@link org.springframework.core.convert.converter.GenericConverter}
	 */
	public void setConverters(Set<?> converters) {
		this.converters = converters;
	}

	/**
	 * Configure the set of custom formatter objects that should be added.
	 * @param formatters instances of {@link Formatter} or {@link AnnotationFormatterFactory}
	 */
	public void setFormatters(Set<?> formatters) {
		this.formatters = formatters;
	}

	/**
	 * <p>Configure the set of FormatterRegistrars to invoke to register
	 * Converters and Formatters in addition to those added declaratively
	 * via {@link #setConverters(Set)} and {@link #setFormatters(Set)}.
	 * <p>FormatterRegistrars are useful when registering multiple related
	 * converters and formatters for a formatting category, such as Date
	 * formatting. All types related needed to support the formatting
	 * category can be registered from one place.
	 * <p>FormatterRegistrars can also be used to register Formatters
	 * indexed under a specific field type different from its own &lt;T&gt;,
	 * or when registering a Formatter from a Printer/Parser pair.
	 * @see FormatterRegistry#addFormatterForFieldType(Class, Formatter)
	 * @see FormatterRegistry#addFormatterForFieldType(Class, Printer, Parser)
	 */
	public void setFormatterRegistrars(Set<FormatterRegistrar> formatterRegistrars) {
		this.formatterRegistrars = formatterRegistrars;
	}

	/**
	 * Indicate whether default formatters should be registered or not.
	 * <p>By default, built-in formatters are registered. This flag can be used
	 * to turn that off and rely on explicitly registered formatters only.
	 * @see #setFormatters(Set)
	 * @see #setFormatterRegistrars(Set)
	 */
	public void setRegisterDefaultFormatters(boolean registerDefaultFormatters) {
		this.registerDefaultFormatters = registerDefaultFormatters;
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver embeddedValueResolver) {
		this.embeddedValueResolver = embeddedValueResolver;
	}


	@Override
	public void afterPropertiesSet() {
		// registerDefaultFormatters默认为true
		this.conversionService = new DefaultFormattingConversionService(this.embeddedValueResolver, this.registerDefaultFormatters);
		// 注入配置的converters
		ConversionServiceFactory.registerConverters(this.converters, this.conversionService);
		// 注入配置的formatters和formatterRegistrars
		registerFormatters();
	}

	private void registerFormatters() {
		if (this.formatters != null) {
			for (Object formatter : this.formatters) {
				if (formatter instanceof Formatter<?>) {
					this.conversionService.addFormatter((Formatter<?>) formatter);
				}
				else if (formatter instanceof AnnotationFormatterFactory<?>) {
					this.conversionService.addFormatterForFieldAnnotation((AnnotationFormatterFactory<?>) formatter);
				}
				else {
					throw new IllegalArgumentException(
							"Custom formatters must be implementations of Formatter or AnnotationFormatterFactory");
				}
			}
		}
		if (this.formatterRegistrars != null) {
			for (FormatterRegistrar registrar : this.formatterRegistrars) {
				registrar.registerFormatters(this.conversionService);
			}
		}
	}


	/**
	 * 返回单例对象
	 * @return
	 */
	@Override
	public FormattingConversionService getObject() {
		return this.conversionService;
	}

	@Override
	public Class<? extends FormattingConversionService> getObjectType() {
		return FormattingConversionService.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}

	
	// 对应的DefaultFormattingConversionService
package org.springframework.format.support;

import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.DateFormatterRegistrar;
import org.springframework.format.datetime.joda.JodaTimeFormatterRegistrar;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.number.money.CurrencyUnitFormatter;
import org.springframework.format.number.money.Jsr354NumberFormatAnnotationFormatterFactory;
import org.springframework.format.number.money.MonetaryAmountFormatter;
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringValueResolver;

/**
 * A specialization of {@link FormattingConversionService} configured by default with
 * converters and formatters appropriate for most applications.
 *
 * <p>Designed for direct instantiation but also exposes the static {@link #addDefaultFormatters}
 * utility method for ad hoc use against any {@code FormatterRegistry} instance, just
 * as {@code DefaultConversionService} exposes its own
 * {@link DefaultConversionService#addDefaultConverters addDefaultConverters} method.
 *
 * <p>Automatically registers formatters for JSR-354 Money & Currency, JSR-310 Date-Time
 * and/or Joda-Time, depending on the presence of the corresponding API on the classpath.
 *
 * @author Chris Beams
 * @author Juergen Hoeller
 * @since 3.1
 */
public class DefaultFormattingConversionService extends FormattingConversionService {

	private static final boolean jsr354Present = ClassUtils.isPresent(
			"javax.money.MonetaryAmount", DefaultFormattingConversionService.class.getClassLoader());

	private static final boolean jsr310Present = ClassUtils.isPresent(
			"java.time.LocalDate", DefaultFormattingConversionService.class.getClassLoader());

	private static final boolean jodaTimePresent = ClassUtils.isPresent(
			"org.joda.time.LocalDate", DefaultFormattingConversionService.class.getClassLoader());


	/**
	 * Create a new {@code DefaultFormattingConversionService} with the set of
	 * {@linkplain DefaultConversionService#addDefaultConverters default converters} and
	 * {@linkplain #addDefaultFormatters default formatters}.
	 */
	public DefaultFormattingConversionService() {
		this(null, true);
	}

	/**
	 * Create a new {@code DefaultFormattingConversionService} with the set of
	 * {@linkplain DefaultConversionService#addDefaultConverters default converters} and,
	 * based on the value of {@code registerDefaultFormatters}, the set of
	 * {@linkplain #addDefaultFormatters default formatters}.
	 * @param registerDefaultFormatters whether to register default formatters
	 */
	public DefaultFormattingConversionService(boolean registerDefaultFormatters) {
		this(null, registerDefaultFormatters);
	}

	/**
	 * Create a new {@code DefaultFormattingConversionService} with the set of
	 * {@linkplain DefaultConversionService#addDefaultConverters default converters} and,
	 * based on the value of {@code registerDefaultFormatters}, the set of
	 * {@linkplain #addDefaultFormatters default formatters}
	 * @param embeddedValueResolver delegated to {@link #setEmbeddedValueResolver(StringValueResolver)}
	 * prior to calling {@link #addDefaultFormatters}.
	 * @param registerDefaultFormatters whether to register default formatters
	 */
	public DefaultFormattingConversionService(StringValueResolver embeddedValueResolver, boolean registerDefaultFormatters) {
		// 给父类设置embeddedValueResolver
		setEmbeddedValueResolver(embeddedValueResolver);
		// TODO FormattingConversionService实现了ConverterRegistry接口和FormatterRegistry接口，接口隔离原则
		// 这里添加默认的Converters
		DefaultConversionService.addDefaultConverters(this);
		// true
		if (registerDefaultFormatters) {
			addDefaultFormatters(this);
		}
	}


	/**
	 * Add formatters appropriate for most environments: including number formatters,
	 * JSR-354 Money & Currency formatters, JSR-310 Date-Time and/or Joda-Time formatters,
	 * depending on the presence of the corresponding API on the classpath.
	 * @param formatterRegistry the service to register default formatters with
	 */
	public static void addDefaultFormatters(FormatterRegistry formatterRegistry) {
		// Default handling of number values
		formatterRegistry.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());

		// Default handling of monetary values
		if (jsr354Present) {
			formatterRegistry.addFormatter(new CurrencyUnitFormatter());
			formatterRegistry.addFormatter(new MonetaryAmountFormatter());
			formatterRegistry.addFormatterForFieldAnnotation(new Jsr354NumberFormatAnnotationFormatterFactory());
		}

		// Default handling of date-time values
		if (jsr310Present) {
			// just handling JSR-310 specific date and time types
			new DateTimeFormatterRegistrar().registerFormatters(formatterRegistry);
		}
		// TODO joda Time class存在，这里注入@DateTimeFormat等注解处理
		if (jodaTimePresent) {
			// handles Joda-specific types as well as Date, Calendar, Long
			new JodaTimeFormatterRegistrar().registerFormatters(formatterRegistry);
		}
		else {
			// regular DateFormat-based Date, Calendar, Long converters
			new DateFormatterRegistrar().registerFormatters(formatterRegistry);
		}
	}

}

   // 对应的FormattingConversionService
package org.springframework.format.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.format.*;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.lang.annotation.Annotation;
import java.text.ParseException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
GenericConversionService实现了ConversionService(canConvert/convert)和ConverterRegistry(addConverter GenericConverter)的功能。
 * TODO GenericConversionService实现了Converter的注册功能和利用Convert来转化的功能。
 * A {@link org.springframework.core.convert.ConversionService} implementation
 * designed to be configured as a {@link FormatterRegistry}.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 3.0
 */
public class FormattingConversionService extends GenericConversionService
		implements FormatterRegistry, EmbeddedValueResolverAware {

	protected static final Logger LOGGER = LoggerFactory.getLogger(FormattingConversionService.class);

	private StringValueResolver embeddedValueResolver;

	private final Map<AnnotationConverterKey, GenericConverter> cachedPrinters =
			new ConcurrentHashMap<AnnotationConverterKey, GenericConverter>(64);

	private final Map<AnnotationConverterKey, GenericConverter> cachedParsers =
			new ConcurrentHashMap<AnnotationConverterKey, GenericConverter>(64);


	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.embeddedValueResolver = resolver;
	}


	@Override
	public void addFormatter(Formatter<?> formatter) {
		addFormatterForFieldType(getFieldType(formatter), formatter);
	}

	/**
	 * TODO 这里全部调用功能public void addConverter(GenericConverter converter)来实现FormatterRegistry的方法
	 * @param fieldType the field type to format
	 * @param formatter the formatter to add
	 */
	@Override
	public void addFormatterForFieldType(Class<?> fieldType, Formatter<?> formatter) {
		addConverter(new PrinterConverter(fieldType, formatter, this));
		addConverter(new ParserConverter(fieldType, formatter, this));
	}

	@Override
	public void addFormatterForFieldType(Class<?> fieldType, Printer<?> printer, Parser<?> parser) {
		addConverter(new PrinterConverter(fieldType, printer, this));
		addConverter(new ParserConverter(fieldType, parser, this));
	}

	/**
	 * JodaDateTimeFormatAnnotationFormatterFactory
	 * @param annotationFormatterFactory the annotation formatter factory to add
	 */
	@Override
	public void addFormatterForFieldAnnotation(AnnotationFormatterFactory<? extends Annotation> annotationFormatterFactory) {
		// 获取类<A>中的注解A
		Class<? extends Annotation> annotationType = getAnnotationType(annotationFormatterFactory);
		if (this.embeddedValueResolver != null && annotationFormatterFactory instanceof EmbeddedValueResolverAware) {
			((EmbeddedValueResolverAware) annotationFormatterFactory).setEmbeddedValueResolver(this.embeddedValueResolver);
		}
		// 获取支持的fieldType
		Set<Class<?>> fieldTypes = annotationFormatterFactory.getFieldTypes();
		// TODO Date / LocalDate / Long
		for (Class<?> fieldType : fieldTypes) {
			addConverter(new AnnotationPrinterConverter(annotationType, annotationFormatterFactory, fieldType));
			// TODO @DataTimeFormat fieldType : Date /...
			// TODO 添加converter到converters中
			addConverter(new AnnotationParserConverter(annotationType, annotationFormatterFactory, fieldType));
		}
	}


	static Class<?> getFieldType(Formatter<?> formatter) {
		Class<?> fieldType = GenericTypeResolver.resolveTypeArgument(formatter.getClass(), Formatter.class);
		if (fieldType == null) {
			throw new IllegalArgumentException("Unable to extract parameterized field type argument from Formatter [" +
					formatter.getClass().getName() + "]; does the formatter parameterize the <T> generic type?");
		}
		return fieldType;
	}

	@SuppressWarnings("unchecked")
	static Class<? extends Annotation> getAnnotationType(AnnotationFormatterFactory<? extends Annotation> factory) {
		Class<? extends Annotation> annotationType = (Class<? extends Annotation>)
				GenericTypeResolver.resolveTypeArgument(factory.getClass(), AnnotationFormatterFactory.class);
		if (annotationType == null) {
			throw new IllegalArgumentException("Unable to extract parameterized Annotation type argument from " +
					"AnnotationFormatterFactory [" + factory.getClass().getName() +
					"]; does the factory parameterize the <A extends Annotation> generic type?");
		}
		return annotationType;
	}


	private static class PrinterConverter implements GenericConverter {

		private final Class<?> fieldType;

		private final TypeDescriptor printerObjectType;

		@SuppressWarnings("rawtypes")
		private final Printer printer;

		private final ConversionService conversionService;

		public PrinterConverter(Class<?> fieldType, Printer<?> printer, ConversionService conversionService) {
			this.fieldType = fieldType;
			this.printerObjectType = TypeDescriptor.valueOf(resolvePrinterObjectType(printer));
			this.printer = printer;
			this.conversionService = conversionService;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Collections.singleton(new ConvertiblePair(this.fieldType, String.class));
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			if (source == null) {
				return "";
			}
			if (!sourceType.isAssignableTo(this.printerObjectType)) {
				source = this.conversionService.convert(source, sourceType, this.printerObjectType);
			}
			return this.printer.print(source, LocaleContextHolder.getLocale());
		}

		private Class<?> resolvePrinterObjectType(Printer<?> printer) {
			return GenericTypeResolver.resolveTypeArgument(printer.getClass(), Printer.class);
		}

		@Override
		public String toString() {
			return (this.fieldType.getName() + " -> " + String.class.getName() + " : " + this.printer);
		}
	}


	private static class ParserConverter implements GenericConverter {

		private final Class<?> fieldType;

		private final Parser<?> parser;

		private final ConversionService conversionService;

		public ParserConverter(Class<?> fieldType, Parser<?> parser, ConversionService conversionService) {
			this.fieldType = fieldType;
			this.parser = parser;
			this.conversionService = conversionService;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Collections.singleton(new ConvertiblePair(String.class, this.fieldType));
		}

		/**
		 * 先格式化，后如果类型不能匹配，再convert
		 * @param source
		 * @param sourceType
		 * @param targetType
		 * @return
		 */
		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			String text = (String) source;
			if (!StringUtils.hasText(text)) {
				return null;
			}
			Object result;
			try {
				result = this.parser.parse(text, LocaleContextHolder.getLocale());
				LOGGER.info("格式化值：{}，结果为：{}", text, result);
			}
			catch (ParseException ex) {
				throw new IllegalArgumentException("Parse attempt failed for value [" + text + "]", ex);
			}
			if (result == null) {
				throw new IllegalStateException("Parsers are not allowed to return null");
			}
			TypeDescriptor resultType = TypeDescriptor.valueOf(result.getClass());
			if (!resultType.isAssignableTo(targetType)) {
				Object oldResult = result;
				result = this.conversionService.convert(result, resultType, targetType);
				LOGGER.info("转换值：{}，结果为：{}", oldResult, result);
			}
			return result;
		}

		@Override
		public String toString() {
			return (String.class.getName() + " -> " + this.fieldType.getName() + ": " + this.parser);
		}
	}


	private class AnnotationPrinterConverter implements ConditionalGenericConverter {

		private final Class<? extends Annotation> annotationType;

		@SuppressWarnings("rawtypes")
		private final AnnotationFormatterFactory annotationFormatterFactory;

		private final Class<?> fieldType;

		public AnnotationPrinterConverter(Class<? extends Annotation> annotationType,
				AnnotationFormatterFactory<?> annotationFormatterFactory, Class<?> fieldType) {
			this.annotationType = annotationType;
			this.annotationFormatterFactory = annotationFormatterFactory;
			this.fieldType = fieldType;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Collections.singleton(new ConvertiblePair(this.fieldType, String.class));
		}

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			return sourceType.hasAnnotation(this.annotationType);
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			Annotation ann = sourceType.getAnnotation(this.annotationType);
			if (ann == null) {
				throw new IllegalStateException(
						"Expected [" + this.annotationType.getName() + "] to be present on " + sourceType);
			}
			AnnotationConverterKey converterKey = new AnnotationConverterKey(ann, sourceType.getObjectType());
			GenericConverter converter = cachedPrinters.get(converterKey);
			if (converter == null) {
				Printer<?> printer = this.annotationFormatterFactory.getPrinter(
						converterKey.getAnnotation(), converterKey.getFieldType());
				converter = new PrinterConverter(this.fieldType, printer, FormattingConversionService.this);
				cachedPrinters.put(converterKey, converter);
			}
			return converter.convert(source, sourceType, targetType);
		}

		@Override
		public String toString() {
			return ("@" + this.annotationType.getName() + " " + this.fieldType.getName() + " -> " +
					String.class.getName() + ": " + this.annotationFormatterFactory);
		}
	}


	/**
	 * 先用Formatter，再用ConversionService进行转化，多了Formatter这一层
	 * 在ParserConverter基础上封装，核心是AnnotationFormatterFactory
	 */
	private class AnnotationParserConverter implements ConditionalGenericConverter {

		private final Class<? extends Annotation> annotationType;

		@SuppressWarnings("rawtypes")
		private final AnnotationFormatterFactory annotationFormatterFactory;

		// TODO fieldType有啥用
		private final Class<?> fieldType;

		public AnnotationParserConverter(Class<? extends Annotation> annotationType,
				AnnotationFormatterFactory<?> annotationFormatterFactory, Class<?> fieldType) {
			this.annotationType = annotationType;
			this.annotationFormatterFactory = annotationFormatterFactory;
			this.fieldType = fieldType;
		}

		/**
		 * 将String 转化为 Date?LocalDate？
		 * @return
		 */
		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Collections.singleton(new ConvertiblePair(String.class, fieldType));
		}

		/**
		 * TODO 先调，targetType上有@DateTimeFormat
		 * @param sourceType 如String
		 * @param targetType 如Date
		 * @return
		 */
		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			return targetType.hasAnnotation(this.annotationType);
		}

		/**
		 *
		 * @param source 原始值
		 * @param sourceType 如String
		 * @param targetType 如Date 有@DateTimeFormat
		 * @return
		 */
		@Override
		@SuppressWarnings("unchecked")
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			Annotation ann = targetType.getAnnotation(this.annotationType);
			if (ann == null) {
				throw new IllegalStateException(
						"Expected [" + this.annotationType.getName() + "] to be present on " + targetType);
			}
			AnnotationConverterKey converterKey = new AnnotationConverterKey(ann, targetType.getObjectType());
			GenericConverter converter = cachedParsers.get(converterKey);
			if (converter == null) {
				// TODO 使用注解，使用格式化工厂，添加缓存
				Parser<?> parser = this.annotationFormatterFactory.getParser(
						converterKey.getAnnotation(), converterKey.getFieldType());
				// TODO 封装ParserConverter
				converter = new ParserConverter(this.fieldType, parser, FormattingConversionService.this);
				cachedParsers.put(converterKey, converter);
			}
			// TODO 使用ParserConverter进行解析，先用parser格式化，再用FormattingConversionService转化
			return converter.convert(source, sourceType, targetType);
		}

		@Override
		public String toString() {
			return (String.class.getName() + " -> @" + this.annotationType.getName() + " " +
					this.fieldType.getName() + ": " + this.annotationFormatterFactory);
		}
	}


	private static class AnnotationConverterKey {

		private final Annotation annotation;

		private final Class<?> fieldType;

		public AnnotationConverterKey(Annotation annotation, Class<?> fieldType) {
			this.annotation = annotation;
			this.fieldType = fieldType;
		}

		public Annotation getAnnotation() {
			return this.annotation;
		}

		public Class<?> getFieldType() {
			return this.fieldType;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof AnnotationConverterKey)) {
				return false;
			}
			AnnotationConverterKey otherKey = (AnnotationConverterKey) other;
			return (this.annotation.equals(otherKey.annotation) && this.fieldType.equals(otherKey.fieldType));
		}

		@Override
		public int hashCode() {
			return (this.annotation.hashCode() + 29 * this.fieldType.hashCode());
		}
	}

}

    // GenericConversionService
package org.springframework.core.convert.support;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.core.ResolvableType;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalConverter;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.core.convert.converter.GenericConverter.ConvertiblePair;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
ConfigurableConversionService实现了public interface ConfigurableConversionService extends ConversionService, ConverterRegistry 接口。
 * Base {@link ConversionService} implementation suitable for use in most environments.
 * Indirectly implements {@link ConverterRegistry} as registration API through the
 * {@link ConfigurableConversionService} interface.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Phillip Webb
 * @author David Haraburda
 * @since 3.0
 */
public class GenericConversionService implements ConfigurableConversionService {

	/**
	 * General NO-OP converter used when conversion is not required.
	 */
	private static final GenericConverter NO_OP_CONVERTER = new NoOpConverter("NO_OP");

	/**
	 * Used as a cache entry when no converter is available.  This converter is never
	 * returned.
	 */
	private static final GenericConverter NO_MATCH = new NoOpConverter("NO_MATCH");


	/** Java 8's java.util.Optional.empty() */
	private static Object javaUtilOptionalEmpty = null;

	static {
		try {
			Class<?> clazz = ClassUtils.forName("java.util.Optional", GenericConversionService.class.getClassLoader());
			javaUtilOptionalEmpty = ClassUtils.getMethod(clazz, "empty").invoke(null);
		}
		catch (Exception ex) {
			// Java 8 not available - conversion to Optional not supported then.
		}
	}


	private final Converters converters = new Converters();

	private final Map<ConverterCacheKey, GenericConverter> converterCache =
			new ConcurrentReferenceHashMap<ConverterCacheKey, GenericConverter>(64);


	// ConverterRegistry implementation

	@Override
	public void addConverter(Converter<?, ?> converter) {
		ResolvableType[] typeInfo = getRequiredTypeInfo(converter, Converter.class);
		Assert.notNull(typeInfo, "Unable to the determine sourceType <S> and targetType " +
				"<T> which your Converter<S, T> converts between; declare these generic types.");
		addConverter(new ConverterAdapter(converter, typeInfo[0], typeInfo[1]));
	}

	@Override
	public void addConverter(Class<?> sourceType, Class<?> targetType, Converter<?, ?> converter) {
		addConverter(new ConverterAdapter(
				converter, ResolvableType.forClass(sourceType), ResolvableType.forClass(targetType)));
	}

	/**
		子类主要调这个方法，注册表项
	*/
	@Override
	public void addConverter(GenericConverter converter) {
		this.converters.add(converter);
		invalidateCache();
	}

	@Override
	public void addConverterFactory(ConverterFactory<?, ?> converterFactory) {
		ResolvableType[] typeInfo = getRequiredTypeInfo(converterFactory, ConverterFactory.class);
		Assert.notNull(typeInfo, "Unable to the determine source type <S> and target range type R which your " +
				"ConverterFactory<S, R> converts between; declare these generic types.");
		addConverter(new ConverterFactoryAdapter(converterFactory,
				new ConvertiblePair(typeInfo[0].resolve(), typeInfo[1].resolve())));
	}

	@Override
	public void removeConvertible(Class<?> sourceType, Class<?> targetType) {
		this.converters.remove(sourceType, targetType);
		invalidateCache();
	}


	// ConversionService implementation

	@Override
	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		Assert.notNull(targetType, "targetType to convert to cannot be null");
		return canConvert((sourceType != null ? TypeDescriptor.valueOf(sourceType) : null),
				TypeDescriptor.valueOf(targetType));
	}

	@Override
	public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
		Assert.notNull(targetType, "targetType to convert to cannot be null");
		if (sourceType == null) {
			return true;
		}
		GenericConverter converter = getConverter(sourceType, targetType);
		return (converter != null);
	}

	/**
	 * Return whether conversion between the sourceType and targetType can be bypassed.
	 * <p>More precisely, this method will return true if objects of sourceType can be
	 * converted to the targetType by returning the source object unchanged.
	 * @param sourceType context about the source type to convert from
	 * (may be {@code null} if source is {@code null})
	 * @param targetType context about the target type to convert to (required)
	 * @return {@code true} if conversion can be bypassed; {@code false} otherwise
	 * @throws IllegalArgumentException if targetType is {@code null}
	 * @since 3.2
	 */
	public boolean canBypassConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
		Assert.notNull(targetType, "targetType to convert to cannot be null");
		if (sourceType == null) {
			return true;
		}
		GenericConverter converter = getConverter(sourceType, targetType);
		return (converter == NO_OP_CONVERTER);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T convert(Object source, Class<T> targetType) {
		Assert.notNull(targetType, "targetType to convert to cannot be null");
		return (T) convert(source, TypeDescriptor.forObject(source), TypeDescriptor.valueOf(targetType));
	}

	@Override
	public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		Assert.notNull(targetType, "targetType to convert to cannot be null");
		if (sourceType == null) {
			Assert.isTrue(source == null, "source must be [null] if sourceType == [null]");
			return handleResult(null, targetType, convertNullSource(null, targetType));
		}
		if (source != null && !sourceType.getObjectType().isInstance(source)) {
			throw new IllegalArgumentException("source to convert from must be an instance of " +
					sourceType + "; instead it was a " + source.getClass().getName());
		}
		GenericConverter converter = getConverter(sourceType, targetType);
		if (converter != null) {
			Object result = ConversionUtils.invokeConverter(converter, source, sourceType, targetType);
			return handleResult(sourceType, targetType, result);
		}
		return handleConverterNotFound(source, sourceType, targetType);
	}

	/**
	 * Convenience operation for converting a source object to the specified targetType,
	 * where the targetType is a descriptor that provides additional conversion context.
	 * Simply delegates to {@link #convert(Object, TypeDescriptor, TypeDescriptor)} and
	 * encapsulates the construction of the sourceType descriptor using
	 * {@link TypeDescriptor#forObject(Object)}.
	 * @param source the source object
	 * @param targetType the target type
	 * @return the converted value
	 * @throws ConversionException if a conversion exception occurred
	 * @throws IllegalArgumentException if targetType is {@code null},
	 * or sourceType is {@code null} but source is not {@code null}
	 */
	public Object convert(Object source, TypeDescriptor targetType) {
		return convert(source, TypeDescriptor.forObject(source), targetType);
	}

	@Override
	public String toString() {
		return this.converters.toString();
	}


	// Protected template methods

	/**
	 * Template method to convert a {@code null} source.
	 * <p>The default implementation returns {@code null} or the Java 8
	 * {@link java.util.Optional#empty()} instance if the target type is
	 * {@code java.util.Optional}. Subclasses may override this to return
	 * custom {@code null} objects for specific target types.
	 * @param sourceType the sourceType to convert from
	 * @param targetType the targetType to convert to
	 * @return the converted null object
	 */
	protected Object convertNullSource(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (javaUtilOptionalEmpty != null && targetType.getObjectType() == javaUtilOptionalEmpty.getClass()) {
			return javaUtilOptionalEmpty;
		}
		return null;
	}

	/**
	 * Hook method to lookup the converter for a given sourceType/targetType pair.
	 * First queries this ConversionService's converter cache.
	 * On a cache miss, then performs an exhaustive search for a matching converter.
	 * If no converter matches, returns the default converter.
	 * @param sourceType the source type to convert from
	 * @param targetType the target type to convert to
	 * @return the generic converter that will perform the conversion,
	 * or {@code null} if no suitable converter was found
	 * @see #getDefaultConverter(TypeDescriptor, TypeDescriptor)
	 */
	protected GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
		ConverterCacheKey key = new ConverterCacheKey(sourceType, targetType);
		GenericConverter converter = this.converterCache.get(key);
		if (converter != null) {
			return (converter != NO_MATCH ? converter : null);
		}

		// 先从注册的Converters中找到converter
		converter = this.converters.find(sourceType, targetType);
		if (converter == null) {
			// 没找到，则查找默认的converter，基本上是无操作，直接返回
			converter = getDefaultConverter(sourceType, targetType);
		}

		if (converter != null) {
			this.converterCache.put(key, converter);
			return converter;
		}

		this.converterCache.put(key, NO_MATCH);
		return null;
	}

	/**
	 * Return the default converter if no converter is found for the given sourceType/targetType pair.
	 * <p>Returns a NO_OP Converter if the sourceType is assignable to the targetType.
	 * Returns {@code null} otherwise, indicating no suitable converter could be found.
	 * @param sourceType the source type to convert from
	 * @param targetType the target type to convert to
	 * @return the default generic converter that will perform the conversion
	 */
	protected GenericConverter getDefaultConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return (sourceType.isAssignableTo(targetType) ? NO_OP_CONVERTER : null);
	}


	// Internal helpers

	private ResolvableType[] getRequiredTypeInfo(Object converter, Class<?> genericIfc) {
		ResolvableType resolvableType = ResolvableType.forClass(converter.getClass()).as(genericIfc);
		ResolvableType[] generics = resolvableType.getGenerics();
		if (generics.length < 2) {
			return null;
		}
		Class<?> sourceType = generics[0].resolve();
		Class<?> targetType = generics[1].resolve();
		if (sourceType == null || targetType == null) {
			return null;
		}
		return generics;
	}

	private void invalidateCache() {
		this.converterCache.clear();
	}

	private Object handleConverterNotFound(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			assertNotPrimitiveTargetType(sourceType, targetType);
			return null;
		}
		if (sourceType.isAssignableTo(targetType) && targetType.getObjectType().isInstance(source)) {
			return source;
		}
		throw new ConverterNotFoundException(sourceType, targetType);
	}

	private Object handleResult(TypeDescriptor sourceType, TypeDescriptor targetType, Object result) {
		if (result == null) {
			assertNotPrimitiveTargetType(sourceType, targetType);
		}
		return result;
	}

	private void assertNotPrimitiveTargetType(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (targetType.isPrimitive()) {
			throw new ConversionFailedException(sourceType, targetType, null,
					new IllegalArgumentException("A null value cannot be assigned to a primitive type"));
		}
	}


	/**
	 * Adapts a {@link Converter} to a {@link GenericConverter}.
	 */
	@SuppressWarnings("unchecked")
	private final class ConverterAdapter implements ConditionalGenericConverter {

		private final Converter<Object, Object> converter;

		private final ConvertiblePair typeInfo;

		private final ResolvableType targetType;

		public ConverterAdapter(Converter<?, ?> converter, ResolvableType sourceType, ResolvableType targetType) {
			this.converter = (Converter<Object, Object>) converter;
			this.typeInfo = new ConvertiblePair(sourceType.resolve(Object.class), targetType.resolve(Object.class));
			this.targetType = targetType;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Collections.singleton(this.typeInfo);
		}

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			// Check raw type first...
			if (this.typeInfo.getTargetType() != targetType.getObjectType()) {
				return false;
			}
			// Full check for complex generic type match required?
			ResolvableType rt = targetType.getResolvableType();
			if (!(rt.getType() instanceof Class) && !rt.isAssignableFrom(this.targetType) &&
					!this.targetType.hasUnresolvableGenerics()) {
				return false;
			}
			return !(this.converter instanceof ConditionalConverter) ||
					((ConditionalConverter) this.converter).matches(sourceType, targetType);
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			if (source == null) {
				return convertNullSource(sourceType, targetType);
			}
			return this.converter.convert(source);
		}

		@Override
		public String toString() {
			return (this.typeInfo + " : " + this.converter);
		}
	}


	/**
	 * Adapts a {@link ConverterFactory} to a {@link GenericConverter}.
	 */
	@SuppressWarnings("unchecked")
	private final class ConverterFactoryAdapter implements ConditionalGenericConverter {

		private final ConverterFactory<Object, Object> converterFactory;

		private final ConvertiblePair typeInfo;

		public ConverterFactoryAdapter(ConverterFactory<?, ?> converterFactory, ConvertiblePair typeInfo) {
			this.converterFactory = (ConverterFactory<Object, Object>) converterFactory;
			this.typeInfo = typeInfo;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return Collections.singleton(this.typeInfo);
		}

		@Override
		public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
			boolean matches = true;
			if (this.converterFactory instanceof ConditionalConverter) {
				matches = ((ConditionalConverter) this.converterFactory).matches(sourceType, targetType);
			}
			if (matches) {
				Converter<?, ?> converter = this.converterFactory.getConverter(targetType.getType());
				if (converter instanceof ConditionalConverter) {
					matches = ((ConditionalConverter) converter).matches(sourceType, targetType);
				}
			}
			return matches;
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			if (source == null) {
				return convertNullSource(sourceType, targetType);
			}
			return this.converterFactory.getConverter(targetType.getObjectType()).convert(source);
		}

		@Override
		public String toString() {
			return (this.typeInfo + " : " + this.converterFactory);
		}
	}


	/**
	 * Key for use with the converter cache.
	 */
	private static final class ConverterCacheKey {

		private final TypeDescriptor sourceType;

		private final TypeDescriptor targetType;

		public ConverterCacheKey(TypeDescriptor sourceType, TypeDescriptor targetType) {
			this.sourceType = sourceType;
			this.targetType = targetType;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof ConverterCacheKey)) {
				return false;
			}
			ConverterCacheKey otherKey = (ConverterCacheKey) other;
			return (ObjectUtils.nullSafeEquals(this.sourceType, otherKey.sourceType) &&
					ObjectUtils.nullSafeEquals(this.targetType, otherKey.targetType));
		}

		@Override
		public int hashCode() {
			return (ObjectUtils.nullSafeHashCode(this.sourceType) * 29 +
					ObjectUtils.nullSafeHashCode(this.targetType));
		}

		@Override
		public String toString() {
			return ("ConverterCacheKey [sourceType = " + this.sourceType +
					", targetType = " + this.targetType + "]");
		}
	}


	/**
	 * Manages all converters registered with the service.
	 */
	private static class Converters {

		private final Set<GenericConverter> globalConverters = new LinkedHashSet<GenericConverter>();

		private final Map<ConvertiblePair, ConvertersForPair> converters =
				new LinkedHashMap<ConvertiblePair, ConvertersForPair>(36);

		public void add(GenericConverter converter) {
			// GenericConverter中存在Set<ConvertiblePair> getConvertibleTypes();方法，表示支持的转换类型
			Set<ConvertiblePair> convertibleTypes = converter.getConvertibleTypes();
			if (convertibleTypes == null) {
				Assert.state(converter instanceof ConditionalConverter,
						"Only conditional converters may return null convertible types");
				this.globalConverters.add(converter);
			}
			else {
				for (ConvertiblePair convertiblePair : convertibleTypes) {
					// 获取到该ConvertiblePair对应的Converter队列，并追加。
					ConvertersForPair convertersForPair = getMatchableConverters(convertiblePair);
					// ConvertersForPair底层存储用的是LinkedList
					convertersForPair.add(converter);
				}
			}
		}

		private ConvertersForPair getMatchableConverters(ConvertiblePair convertiblePair) {
			ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
			if (convertersForPair == null) {
				convertersForPair = new ConvertersForPair();
				this.converters.put(convertiblePair, convertersForPair);
			}
			return convertersForPair;
		}

		public void remove(Class<?> sourceType, Class<?> targetType) {
			this.converters.remove(new ConvertiblePair(sourceType, targetType));
		}

		/**
		 * Find a {@link GenericConverter} given a source and target type.
		 * <p>This method will attempt to match all possible converters by working
		 * through the class and interface hierarchy of the types.
		 * @param sourceType the source type
		 * @param targetType the target type
		 * @return a matching {@link GenericConverter}, or {@code null} if none found
		 */
		public GenericConverter find(TypeDescriptor sourceType, TypeDescriptor targetType) {
			// Search the full type hierarchy
			List<Class<?>> sourceCandidates = getClassHierarchy(sourceType.getType());
			List<Class<?>> targetCandidates = getClassHierarchy(targetType.getType());
			for (Class<?> sourceCandidate : sourceCandidates) {
				for (Class<?> targetCandidate : targetCandidates) {
					ConvertiblePair convertiblePair = new ConvertiblePair(sourceCandidate, targetCandidate);
					GenericConverter converter = getRegisteredConverter(sourceType, targetType, convertiblePair);
					if (converter != null) {
						return converter;
					}
				}
			}
			return null;
		}

		private GenericConverter getRegisteredConverter(TypeDescriptor sourceType,
				TypeDescriptor targetType, ConvertiblePair convertiblePair) {

			// Check specifically registered converters
			ConvertersForPair convertersForPair = this.converters.get(convertiblePair);
			// 获取到所在队列
			if (convertersForPair != null) {
				GenericConverter converter = convertersForPair.getConverter(sourceType, targetType);
				if (converter != null) {
					return converter;
				}
			}
			// Check ConditionalConverters for a dynamic match
			for (GenericConverter globalConverter : this.globalConverters) {
				if (((ConditionalConverter) globalConverter).matches(sourceType, targetType)) {
					return globalConverter;
				}
			}
			return null;
		}

		/**
		 * Returns an ordered class hierarchy for the given type.
		 * @param type the type
		 * @return an ordered list of all classes that the given type extends or implements
		 */
		private List<Class<?>> getClassHierarchy(Class<?> type) {
			List<Class<?>> hierarchy = new ArrayList<Class<?>>(20);
			Set<Class<?>> visited = new HashSet<Class<?>>(20);
			addToClassHierarchy(0, ClassUtils.resolvePrimitiveIfNecessary(type), false, hierarchy, visited);
			boolean array = type.isArray();

			int i = 0;
			while (i < hierarchy.size()) {
				Class<?> candidate = hierarchy.get(i);
				candidate = (array ? candidate.getComponentType() : ClassUtils.resolvePrimitiveIfNecessary(candidate));
				Class<?> superclass = candidate.getSuperclass();
				if (superclass != null && superclass != Object.class && superclass != Enum.class) {
					addToClassHierarchy(i + 1, candidate.getSuperclass(), array, hierarchy, visited);
				}
				addInterfacesToClassHierarchy(candidate, array, hierarchy, visited);
				i++;
			}

			if (Enum.class.isAssignableFrom(type)) {
				addToClassHierarchy(hierarchy.size(), Enum.class, array, hierarchy, visited);
				addToClassHierarchy(hierarchy.size(), Enum.class, false, hierarchy, visited);
				addInterfacesToClassHierarchy(Enum.class, array, hierarchy, visited);
			}

			addToClassHierarchy(hierarchy.size(), Object.class, array, hierarchy, visited);
			addToClassHierarchy(hierarchy.size(), Object.class, false, hierarchy, visited);
			return hierarchy;
		}
		
		/**
			获取到对应的类层次关系
		*/
		private void addInterfacesToClassHierarchy(Class<?> type, boolean asArray,
				List<Class<?>> hierarchy, Set<Class<?>> visited) {

			for (Class<?> implementedInterface : type.getInterfaces()) {
				addToClassHierarchy(hierarchy.size(), implementedInterface, asArray, hierarchy, visited);
			}
		}

		private void addToClassHierarchy(int index, Class<?> type, boolean asArray,
				List<Class<?>> hierarchy, Set<Class<?>> visited) {

			if (asArray) {
				type = Array.newInstance(type, 0).getClass();
			}
			if (visited.add(type)) {
				hierarchy.add(index, type);
			}
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("ConversionService converters =\n");
			for (String converterString : getConverterStrings()) {
				builder.append('\t').append(converterString).append('\n');
			}
			return builder.toString();
		}

		private List<String> getConverterStrings() {
			List<String> converterStrings = new ArrayList<String>();
			for (ConvertersForPair convertersForPair : converters.values()) {
				converterStrings.add(convertersForPair.toString());
			}
			Collections.sort(converterStrings);
			return converterStrings;
		}
	}


	/**
	 * Manages converters registered with a specific {@link ConvertiblePair}.
	 */
	private static class ConvertersForPair {

		private final LinkedList<GenericConverter> converters = new LinkedList<GenericConverter>();

		public void add(GenericConverter converter) {
			this.converters.addFirst(converter);
		}

		// 如果有条件，还必须匹配，如属性上有注解@DateTimeFormat
		public GenericConverter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
			for (GenericConverter converter : this.converters) {
				if (!(converter instanceof ConditionalGenericConverter) ||
						((ConditionalGenericConverter) converter).matches(sourceType, targetType)) {
					return converter;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return StringUtils.collectionToCommaDelimitedString(this.converters);
		}
	}


	/**
	 * Internal converter that performs no operation.
	 */
	private static class NoOpConverter implements GenericConverter {

		private final String name;

		public NoOpConverter(String name) {
			this.name = name;
		}

		@Override
		public Set<ConvertiblePair> getConvertibleTypes() {
			return null;
		}

		@Override
		public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
			return source;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

}
 // 所以，DefaultFormattingConversionService中，注入了 new JodaTimeFormatterRegistrar().registerFormatters(formatterRegistry);对应的代码如下：
 @Override
	public void registerFormatters(FormatterRegistry registry) {
		JodaTimeConverters.registerConverters(registry);

		DateTimeFormatter dateFormatter = getFormatter(Type.DATE);
		DateTimeFormatter timeFormatter = getFormatter(Type.TIME);
		DateTimeFormatter dateTimeFormatter = getFormatter(Type.DATE_TIME);

		addFormatterForFields(registry,
				new ReadablePartialPrinter(dateFormatter),
				new LocalDateParser(dateFormatter),
				LocalDate.class);

		addFormatterForFields(registry,
				new ReadablePartialPrinter(timeFormatter),
				new LocalTimeParser(timeFormatter),
				LocalTime.class);

		addFormatterForFields(registry,
				new ReadablePartialPrinter(dateTimeFormatter),
				new LocalDateTimeParser(dateTimeFormatter),
				LocalDateTime.class);

		addFormatterForFields(registry,
				new ReadableInstantPrinter(dateTimeFormatter),
				new DateTimeParser(dateTimeFormatter),
				ReadableInstant.class);

		// In order to retain backwards compatibility we only register Date/Calendar
		// types when a user defined formatter is specified (see SPR-10105)
		if (this.formatters.containsKey(Type.DATE_TIME)) {
			addFormatterForFields(registry,
					new ReadableInstantPrinter(dateTimeFormatter),
					new DateTimeParser(dateTimeFormatter),
					Date.class, Calendar.class);
		}

		registry.addFormatterForFieldType(Period.class, new PeriodFormatter());
		registry.addFormatterForFieldType(Duration.class, new DurationFormatter());
		if (jodaTime2Available) {
			JodaTime2Delegate.registerAdditionalFormatters(registry);
		}

		// TODO 添加joda解析
		registry.addFormatterForFieldAnnotation(new JodaDateTimeFormatAnnotationFormatterFactory());
	}
	


package org.springframework.format.datetime.joda;

import lombok.extern.slf4j.Slf4j;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.context.support.EmbeddedValueResolutionSupport;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.*;

/**
 * Formats fields annotated with the {@link DateTimeFormat} annotation using Joda-Time.
 * <p>
 * <p><b>NOTE:</b> Spring's Joda-Time support requires Joda-Time 2.x, as of Spring 4.0.
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @see DateTimeFormat
 * @since 3.0
 */
@Slf4j
public class JodaDateTimeFormatAnnotationFormatterFactory extends EmbeddedValueResolutionSupport
        implements AnnotationFormatterFactory<DateTimeFormat> {

    private static final Set<Class<?>> FIELD_TYPES;

    static {
        // Create the set of field types that may be annotated with @DateTimeFormat.
        // Note: the 3 ReadablePartial concrete types are registered explicitly since
        // addFormatterForFieldType rules exist for each of these types
        // (if we did not do this, the default byType rules for LocalDate, LocalTime,
        // and LocalDateTime would take precedence over the annotation rule, which
        // is not what we want)
        Set<Class<?>> fieldTypes = new HashSet<Class<?>>(8);
        fieldTypes.add(ReadableInstant.class);
        fieldTypes.add(LocalDate.class);
        fieldTypes.add(LocalTime.class);
        fieldTypes.add(LocalDateTime.class);
        fieldTypes.add(Date.class);
        fieldTypes.add(Calendar.class);
        fieldTypes.add(Long.class);
        FIELD_TYPES = Collections.unmodifiableSet(fieldTypes);
    }


    /**
     * 最终映射成String + Date -> getParser
     * @return
     */
    @Override
    public final Set<Class<?>> getFieldTypes() {
        return FIELD_TYPES;
    }

    @Override
    public Printer<?> getPrinter(DateTimeFormat annotation, Class<?> fieldType) {
        DateTimeFormatter formatter = getFormatter(annotation, fieldType);
        if (ReadablePartial.class.isAssignableFrom(fieldType)) {
            return new ReadablePartialPrinter(formatter);
        } else if (ReadableInstant.class.isAssignableFrom(fieldType) || Calendar.class.isAssignableFrom(fieldType)) {
            // assumes Calendar->ReadableInstant converter is registered
            return new ReadableInstantPrinter(formatter);
        } else {
            // assumes Date->Long converter is registered
            return new MillisecondInstantPrinter(formatter);
        }
    }

    /**
     * 根据fieldType类型，找到对应的解析器
     * @param annotation the annotation instance
     * @param fieldType the type of field that was annotated
     * @return
     */
    @Override
    public Parser<?> getParser(DateTimeFormat annotation, Class<?> fieldType) {
        log.info("获取打印器，属性为：{}，注解为：{}", fieldType, annotation);
        if (LocalDate.class == fieldType) {
            return new LocalDateParser(getFormatter(annotation, fieldType));
        } else if (LocalTime.class == fieldType) {
            return new LocalTimeParser(getFormatter(annotation, fieldType));
        } else if (LocalDateTime.class == fieldType) {
            return new LocalDateTimeParser(getFormatter(annotation, fieldType));
        } else {
            return new DateTimeParser(getFormatter(annotation, fieldType));
        }
    }

    /**
     * Factory method used to create a {@link DateTimeFormatter}.
     *
     * @param annotation the format annotation for the field
     * @param fieldType  the type of field
     * @return a {@link DateTimeFormatter} instance
     * @since 3.2
     */
    protected DateTimeFormatter getFormatter(DateTimeFormat annotation, Class<?> fieldType) {
        DateTimeFormatterFactory factory = new DateTimeFormatterFactory();
        factory.setStyle(resolveEmbeddedValue(annotation.style()));
        factory.setIso(annotation.iso());
        factory.setPattern(resolveEmbeddedValue(annotation.pattern()));
        return factory.createDateTimeFormatter();
    }

}
```
54. SimpleTypeConverter、BeanWrapperImpl，都有继承TypeConverterSupport，实现参数注入和绑定。BeanPropertyBindingResult使用BeanWrapperImpl来实现参数绑定。前段传递参数时进行绑定，xml设置值property values进行绑定，参数绑定都有具体的绑定对象。DataBinder实现了PropertyRegistry和TypeConverter的功能，底层使用BeanPropertyBindingResult来实现。

55. PropertyAccessor的功能：setPropertyValue(String propertyName, String propertyValue)和getPropertyValue(String propertyName)

56. spring父容器，默认不注入ConversionService，而子容器默认使用mvc:annotation-driven时，会默注入DefaultFormattingConversionService。
57. 所有的文件路径应该都支持ant类型的路径，在注入的对象为Resource[]时，且使用的editor是ResourceArrayPropertyEditor。该editor在ApplicationContext的refresh中的某个步骤进行注入。

#### 五、Spring AOP
1. 如下开启AOP，会注入AnnotationAwareAspectJAutoProxyCreator作为bean，该bean是BeanPostProcessor，实现了postProcessAfterInitialization，且是ProxyConfig的子类。该bean中使用advisedBeans(ConcurrentHashMap)来缓存每个bean是否需要进行AOP增强。

```
<aop:aspectj-autoproxy proxy-target-class="true"/>
```

2.
- 在org.springframework.aop.aspectj.annotation.AbstractAspectJAdvisorFactory#hasAspectAnnotation判断了一个bean是否有@Aspect注解，并会过滤出来。

- 封装的Advisor为InstantiationModelAwarePointcutAdvisorImpl。AspectJExpressionPointcut封装了对应的注解值，如@Before("login()")，login上的注解@Pointcut("..execution(..abc..)")。即命名切点，引用。InstantiationModelAwarePointcutAdvisorImpl实例中封装了Advice，最后被调用的是Advice。

- 创建代理见org.springframework.aop.framework.ProxyFactory#getProxy(java.lang.ClassLoader)类。实际也就是DefaultAopProxyFactory.createAopProxy(this).getProxy(classLoader)。注意ProxyFactory与AopProxyFactory没有继承关系。ProxyFactory是一个配置类，继承自ProxyConfig，实现了Advised接口（含有目标对象），而AopProxyFactory是专门创建AopProxy的，有三个子类，分别是JdkDynamicAopProxy、CglibAopProxy、ObjenesisCglibAopProxy，这三个代理类都会创建具体的代理类，来替换实际的类。

- 在CglibAopProxy创建了代理。而org.springframework.aop.Pointcut的子类包括ExpressionPointcut，是spring的接口。spring的pointcut先进行类拦截，再进行方法级别的拦截。AspectJExpressionPointcut中使用PointcutExpression来记录对应的表达式，如"..execution(..abc..)"。PointcutExpression中有Pointcut属性，存储实际的切点。注意这里的Pointcut是org.aspectj.weaver.patterns.Pointcut，是个类。
3. @Async使用AsyncAnnotationBeanPostProcessor来处理。

5. 方法的第一个参数为JoinPoint，只有@Around才能是ProceedingJointPoint。AspectBeanName -> List<Advisor>最后会缓存在advisorsCache中。

6. Advisor -> Advice + Pointcut(classFilter + methodMatcher)，Advisor相当于切面，Advice是横切代码，如记日志，记监控，同时还有切入的方位。JoinPoint是实际匹配的方法。而Pointcut用来寻找匹配的方法。注意代码中一个Advisor只有一个Advice，一个Pointcut。

7. AOP阶段 
- 从BeanFactory及其父BeanFactory中找到所有的Advisor，以及标注有@Aspect注解的bean，将bean的标有@Before/@AfterReturning等注解的方法也转化为AspectJExpressionAdvisor。
- 当每个实例在经过BeanPostProcessor#postProcessAfterInitialization时，到AnnotationAspectJAwareAutoProxyCreator时，过滤出适合当前类（目标类）的Advisor列表，匹配标准为目标类匹配，而且该类及其接口至少存在一个方法能够被该advisor静态匹配。
- 如果过滤的Advisor非空，则根据AOP配置，创建JDK动态代理实例（实现IncoationHandler#invoke）或者CGLib动态代理实例，并将postProcessAfterInitialization返回结果替换为代理实例。对于JDK，该实例至少实现了原来接口、SpringProxy、Advised。将Advice转换为Advisor，并添加到ProxyFactory中。
- 当调用被增强类的某个方法时，如果是JDK代理实例，则调用其invoke方法。类已经过滤了，类可以不再匹配了，判断该Advisor是否可以匹配该方法，包括动态匹配(isRuntime()=true)，获取到所有可以的Advisor，获取其Advice并转化为MethodInterceptor。最后从前往后开始执行MethodInterceptor。在某个合适的地方会调用连接点。
- 对于不可代理的方法，则直接调用其原来的连接点。
- 注意，对于代理的bean，其所有方法都会走invoke方法，包括toString。代理之后，其他BeanPostProcessor继续走BeanPostProcessor#postProcessAfterInitialization时，里面的实现要考虑是否是代理。用了很多缓存。

7. 使用了很多委托，实现了代码的抽离。org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#findCandidateAdvisors查找Advisor时，委托给了		this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
进行查找。这里注入BeanFactory，查找所有的Advisor类型的bean。
8. org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator#postProcessAfterInitialization:
```
/**
	 * Create a proxy with the configured interceptors if the bean is
	 * identified as one to proxy by the subclass.
	 * @see #getAdvicesAndAdvisorsForBean
	 */
	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean != null) {
			Object cacheKey = getCacheKey(bean.getClass(), beanName);
			if (!this.earlyProxyReferences.contains(cacheKey)) {
				return wrapIfNecessary(bean, beanName, cacheKey);
			}
		}
		return bean;
	}
	
	/**
	 * Wrap the given bean if necessary, i.e. if it is eligible for being proxied.
	 * @param bean the raw bean instance
	 * @param beanName the name of the bean
	 * @param cacheKey the cache key for metadata access
	 * @return TODO a proxy wrapping the bean, or the raw bean instance as-is
	 */
	protected Object wrapIfNecessary(Object bean, String beanName, Object cacheKey) {
		if (beanName != null && this.targetSourcedBeans.contains(beanName)) {
			return bean;
		}
		if (Boolean.FALSE.equals(this.advisedBeans.get(cacheKey))) {
			return bean;
		}
		if (isInfrastructureClass(bean.getClass()) || shouldSkip(bean.getClass(), beanName)) {
			this.advisedBeans.put(cacheKey, Boolean.FALSE);
			return bean;
		}
        // TODO 抽象方法
		Object[] specificInterceptors = getAdvicesAndAdvisorsForBean(bean.getClass(), beanName, null);
		// TODO 该bean可以被增强
		if (specificInterceptors != DO_NOT_PROXY) {
			// TODO 增强的bean，TargetSource封装原始的bean
			this.advisedBeans.put(cacheKey, Boolean.TRUE);
			Object proxy = createProxy(
					bean.getClass(), beanName, specificInterceptors, new SingletonTargetSource(bean));
			this.proxyTypes.put(cacheKey, proxy.getClass());
			// TODO 替换掉了
			return proxy;
		}
		// TODO 不增强的bean
		this.advisedBeans.put(cacheKey, Boolean.FALSE);
		return bean;
	}
	/**
	 * TODO 核心代码
	 * Create an AOP proxy for the given bean.
	 * @param beanClass the class of the bean
	 * @param beanName the name of the bean
	 * @param specificInterceptors the set of interceptors that is
	 * specific to this bean (may be empty, but not null)
	 * @param targetSource the TargetSource for the proxy,
	 * already pre-configured to access the bean
	 * @return the AOP proxy for the bean
	 * @see #buildAdvisors
	 */
	protected Object createProxy(
			Class<?> beanClass, String beanName, Object[] specificInterceptors, TargetSource targetSource) {

		if (this.beanFactory instanceof ConfigurableListableBeanFactory) {
			AutoProxyUtils.exposeTargetClass((ConfigurableListableBeanFactory) this.beanFactory, beanName, beanClass);
		}

		// TODO 委托给ProxyFactory来获取proxy
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.copyFrom(this);

		if (!proxyFactory.isProxyTargetClass()) {
			if (shouldProxyTargetClass(beanClass, beanName)) {
				proxyFactory.setProxyTargetClass(true);
			}
			else {
				evaluateProxyInterfaces(beanClass, proxyFactory);
			}
		}

		Advisor[] advisors = buildAdvisors(beanName, specificInterceptors);
		for (Advisor advisor : advisors) {
			proxyFactory.addAdvisor(advisor);
		}

		// 设置原始的bean
		proxyFactory.setTargetSource(targetSource);
		customizeProxyFactory(proxyFactory);

		proxyFactory.setFrozen(this.freezeProxy);
		if (advisorsPreFiltered()) {
			proxyFactory.setPreFiltered(true);
		}

		return proxyFactory.getProxy(getProxyClassLoader());
	}
```
9. 创建JDK动态代理或者CGLib动态代理：org.springframework.aop.framework.DefaultAopProxyFactory#createAopProxy
```
@Override
	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		// config是ProxyFactory
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class<?> targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			// Spring bean中目标类不可能是接口，接口无法在创建时实例化
			if (targetClass.isInterface() || Proxy.isProxyClass(targetClass)) {
				return new JdkDynamicAopProxy(config);
			}
			return new ObjenesisCglibAopProxy(config);
		}
		else {
			return new JdkDynamicAopProxy(config);
		}
	}
```
10. JdkDynamicAopProxy的实现
```
package org.springframework.aop.framework;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.AopInvocationException;
import org.springframework.aop.RawTargetAccess;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * JDK-based {@link AopProxy} implementation for the Spring AOP framework,
 * based on JDK {@link java.lang.reflect.Proxy dynamic proxies}.
 *
 * <p>Creates a dynamic proxy, implementing the interfaces exposed by
 * the AopProxy. Dynamic proxies <i>cannot</i> be used to proxy methods
 * defined in classes, rather than interfaces.
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by an {@link AdvisedSupport} class. This class is internal
 * to Spring's AOP framework and need not be used directly by client code.
 *
 * <p>Proxies created using this class will be thread-safe if the
 * underlying (target) class is thread-safe.
 *
 * <p>Proxies are serializable so long as all Advisors (including Advices
 * and Pointcuts) and the TargetSource are serializable.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Dave Syer
 * @see java.lang.reflect.Proxy
 * @see AdvisedSupport
 * @see ProxyFactory
 */
final class JdkDynamicAopProxy implements AopProxy, InvocationHandler, Serializable {

	/** use serialVersionUID from Spring 1.2 for interoperability */
	private static final long serialVersionUID = 5531744639992436476L;


	/*
	 * NOTE: We could avoid the code duplication between this class and the CGLIB
	 * proxies by refactoring "invoke" into a template method. However, this approach
	 * adds at least 10% performance overhead versus a copy-paste solution, so we sacrifice
	 * elegance for performance. (We have a good test suite to ensure that the different
	 * proxies behave the same :-)
	 * This way, we can also more easily take advantage of minor optimizations in each class.
	 */

	/** We use a static Log to avoid serialization issues */
	private static final Log logger = LogFactory.getLog(JdkDynamicAopProxy.class);

	/**
	 *  TODO 保留的是Support类
	 *  Config used to configure this proxy
	 *
	 */
	private final AdvisedSupport advised;

	/**
	 * Is the {@link #equals} method defined on the proxied interfaces?
	 */
	private boolean equalsDefined;

	/**
	 * Is the {@link #hashCode} method defined on the proxied interfaces?
	 */
	private boolean hashCodeDefined;


	/**
	 * config包含了目标对象，以及该类支持的Advisor列表等
	 * Construct a new JdkDynamicAopProxy for the given AOP configuration.
	 * @param config the AOP configuration as AdvisedSupport object
	 * @throws AopConfigException if the config is invalid. We try to throw an informative
	 * exception in this case, rather than let a mysterious failure happen later.
	 */
	public JdkDynamicAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("No advisors and no TargetSource specified");
		}
		this.advised = config;
	}


	@Override
	public Object getProxy() {
		return getProxy(ClassUtils.getDefaultClassLoader());
	}

	@Override
	public Object getProxy(ClassLoader classLoader) {
		logger.info("创建jdk动态代理: 目标来源是： " + this.advised.getTargetSource());
		// TODO 此处有三个接口：原来类实现的接口、SpringProxy、Advised
		Class<?>[] proxiedInterfaces = AopProxyUtils.completeProxiedInterfaces(this.advised);
		findDefinedEqualsAndHashCodeMethods(proxiedInterfaces);
		// 添加自己实现的接口、SpringProxy标记接口、Advised接口
		return Proxy.newProxyInstance(classLoader, proxiedInterfaces, this);
	}

	/**
	 * Finds any {@link #equals} or {@link #hashCode} method that may be defined
	 * on the supplied set of interfaces.
	 * @param proxiedInterfaces the interfaces to introspect
	 */
	private void findDefinedEqualsAndHashCodeMethods(Class<?>[] proxiedInterfaces) {
		for (Class<?> proxiedInterface : proxiedInterfaces) {
			Method[] methods = proxiedInterface.getDeclaredMethods();
			for (Method method : methods) {
				if (AopUtils.isEqualsMethod(method)) {
					this.equalsDefined = true;
				}
				if (AopUtils.isHashCodeMethod(method)) {
					this.hashCodeDefined = true;
				}
				if (this.equalsDefined && this.hashCodeDefined) {
					return;
				}
			}
		}
	}


	/**
	 * TODO 核心方法，成为JDK代理实例后，每个方法都会调用这个方法。
	 * Implementation of {@code InvocationHandler.invoke}.
	 * <p>Callers will see exactly the exception thrown by the target,
	 * unless a hook method throws an exception.
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		MethodInvocation invocation;
		Object oldProxy = null;
		boolean setProxyContext = false;

		TargetSource targetSource = this.advised.targetSource;
		Class<?> targetClass = null;
		Object target = null;

		try {
			if (!this.equalsDefined && AopUtils.isEqualsMethod(method)) {
				// The target does not implement the equals(Object) method itself.
				return equals(args[0]);
			}
			if (!this.hashCodeDefined && AopUtils.isHashCodeMethod(method)) {
				// The target does not implement the hashCode() method itself.
				return hashCode();
			}
			// TODO 方法声明所在类是接口，且是Advised，则调用本地的Advised...
			if (!this.advised.opaque && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				// Service invocations on ProxyConfig with the proxy config...
				// TODO 由原来的ProxyConfig处理？？
				return AopUtils.invokeJoinpointUsingReflection(this.advised, method, args);
			}

			Object retVal;

			if (this.advised.exposeProxy) {
				// Make invocation available if necessary.
				oldProxy = AopContext.setCurrentProxy(proxy);
				setProxyContext = true;
			}

			// May be null. Get as late as possible to minimize the time we "own" the target,
			// in case it comes from a pool.
			// TODO 调用原来的实例，target
			target = targetSource.getTarget();
			if (target != null) {
				targetClass = target.getClass();
			}

			// Get the interception chain for this method.
			List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);

			// Check whether we have any advice. If we don't, we can fallback on direct
			// reflective invocation of the target, and avoid creating a MethodInvocation.
			// TODO 增强列表为空，调用连接点方法即可。
			if (chain.isEmpty()) {
				// We can skip creating a MethodInvocation: just invoke the target directly
				// Note that the final invoker must be an InvokerInterceptor so we know it does
				// nothing but a reflective operation on the target, and no hot swapping or fancy proxying.
				Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
				retVal = AopUtils.invokeJoinpointUsingReflection(target, method, argsToUse);
			}
			else {
				// We need to create a method invocation...
				// TODO 封装所有Advisor，也就是MethodBeforeServcie等为MethodInterceptor，所有逻辑都已经装好了，无需拼接增强和连接点。Advisor可能减少了。
				invocation = new ReflectiveMethodInvocation(proxy, target, method, args, targetClass, chain);
				// Proceed to the joinpoint through the interceptor chain.
				// 注意，chain里面的方法也可能调用这个proceed，从而构成递归
				retVal = invocation.proceed();
			}

			// Massage return value if necessary.
			Class<?> returnType = method.getReturnType();
			if (retVal != null && retVal == target && returnType.isInstance(proxy) &&
					!RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
				// Special case: it returned "this" and the return type of the method
				// is type-compatible. Note that we can't help if the target sets
				// a reference to itself in another returned object.
				retVal = proxy;
			}
			else if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
				throw new AopInvocationException(
						"Null return value from advice does not match primitive return type for: " + method);
			}
			return retVal;
		}
		finally {
			if (target != null && !targetSource.isStatic()) {
				// Must have come from TargetSource.
				targetSource.releaseTarget(target);
			}
			if (setProxyContext) {
				// Restore old proxy.
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}


	/**
	 * Equality means interfaces, advisors and TargetSource are equal.
	 * <p>The compared object may be a JdkDynamicAopProxy instance itself
	 * or a dynamic proxy wrapping a JdkDynamicAopProxy instance.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other == null) {
			return false;
		}

		JdkDynamicAopProxy otherProxy;
		if (other instanceof JdkDynamicAopProxy) {
			otherProxy = (JdkDynamicAopProxy) other;
		}
		else if (Proxy.isProxyClass(other.getClass())) {
			InvocationHandler ih = Proxy.getInvocationHandler(other);
			if (!(ih instanceof JdkDynamicAopProxy)) {
				return false;
			}
			otherProxy = (JdkDynamicAopProxy) ih;
		}
		else {
			// Not a valid comparison...
			return false;
		}

		// If we get here, otherProxy is the other AopProxy.
		return AopProxyUtils.equalsInProxy(this.advised, otherProxy.advised);
	}

	/**
	 * Proxy uses the hash code of the TargetSource.
	 */
	@Override
	public int hashCode() {
		return JdkDynamicAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}

}
```
11. org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator#getAdvicesAndAdvisorsForBean的实现：
```
package org.springframework.aop.framework.autoproxy;

import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.List;

/**
 * Generic auto proxy creator that builds AOP proxies for specific beans
 * based on detected Advisors for each bean.
 *
 * <p>Subclasses must implement the abstract {@link #findCandidateAdvisors()}
 * method to return a list of Advisors applying to any object. Subclasses can
 * also override the inherited {@link #shouldSkip} method to exclude certain
 * objects from auto-proxying.
 *
 * <p>Advisors or advices requiring ordering should implement the
 * {@link org.springframework.core.Ordered} interface. This class sorts
 * Advisors by Ordered order value. Advisors that don't implement the
 * Ordered interface will be considered as unordered; they will appear
 * at the end of the advisor chain in undefined order.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see #findCandidateAdvisors
 */
@SuppressWarnings("serial")
public abstract class AbstractAdvisorAutoProxyCreator extends AbstractAutoProxyCreator {

	private BeanFactoryAdvisorRetrievalHelper advisorRetrievalHelper;


	/**
	 * 在设置BeanFactory时，就设置了
	 * @param beanFactory
	 */
	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);
		if (!(beanFactory instanceof ConfigurableListableBeanFactory)) {
			throw new IllegalStateException("Cannot use AdvisorAutoProxyCreator without a ConfigurableListableBeanFactory");
		}
		initBeanFactory((ConfigurableListableBeanFactory) beanFactory);
	}

	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// 注入BeanFactory
		this.advisorRetrievalHelper = new BeanFactoryAdvisorRetrievalHelperAdapter(beanFactory);
	}

	/**
	 * 实现父类的抽象方法
	 * @param beanClass the class of the bean to advise
	 * @param beanName the name of the bean
	 * @param targetSource
	 * @return
	 */
	@Override
	protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource targetSource) {
		List<Advisor> advisors = findEligibleAdvisors(beanClass, beanName);
		if (advisors.isEmpty()) {
			return DO_NOT_PROXY;
		}
		return advisors.toArray();
	}

	/**
	 * Find all eligible Advisors for auto-proxying this class.
	 * @param beanClass the clazz to find advisors for
	 * @param beanName the name of the currently proxied bean
	 * @return the empty List, not {@code null},
	 * if there are no pointcuts or interceptors
	 * @see #findCandidateAdvisors
	 * @see #sortAdvisors
	 * @see #extendAdvisors
	 */
	protected List<Advisor> findEligibleAdvisors(Class<?> beanClass, String beanName) {
		// 获取所有的Advisor列表，包括自定义的Advisor，以及Aspect切面类中的方法
		List<Advisor> candidateAdvisors = findCandidateAdvisors();
		// 从候选的所有的Advisor获取适用于当前beanClass的Advisor列表
		List<Advisor> eligibleAdvisors = findAdvisorsThatCanApply(candidateAdvisors, beanClass, beanName);
		// TODO 注入ExposeInvocationInterceptor
		extendAdvisors(eligibleAdvisors);
		if (!eligibleAdvisors.isEmpty()) {
			eligibleAdvisors = sortAdvisors(eligibleAdvisors);
		}
		return eligibleAdvisors;
	}

	/**
	 * Find all candidate Advisors to use in auto-proxying.
	 * @return the List of candidate Advisors
	 */
	protected List<Advisor> findCandidateAdvisors() {
		// 交给委托类来查找到Advisor
		return this.advisorRetrievalHelper.findAdvisorBeans();
	}

	/**
	 * Search the given candidate Advisors to find all Advisors that
	 * can apply to the specified bean.
	 * @param candidateAdvisors the candidate Advisors
	 * @param beanClass the target's bean class
	 * @param beanName the target's bean name
	 * @return the List of applicable Advisors
	 * @see ProxyCreationContext#getCurrentProxiedBeanName()
	 */
	protected List<Advisor> findAdvisorsThatCanApply(
			List<Advisor> candidateAdvisors, Class<?> beanClass, String beanName) {

		ProxyCreationContext.setCurrentProxiedBeanName(beanName);
		try {
			// 从所有候选Advisor中找到适合该类的Advisor，封装到工具类中
			return AopUtils.findAdvisorsThatCanApply(candidateAdvisors, beanClass);
		}
		finally {
			ProxyCreationContext.setCurrentProxiedBeanName(null);
		}
	}

	/**
	 * Return whether the Advisor bean with the given name is eligible
	 * for proxying in the first place.
	 * @param beanName the name of the Advisor bean
	 * @return whether the bean is eligible
	 */
	protected boolean isEligibleAdvisorBean(String beanName) {
		return true;
	}

	/**
	 * Sort advisors based on ordering. Subclasses may choose to override this
	 * method to customize the sorting strategy.
	 * @param advisors the source List of Advisors
	 * @return the sorted List of Advisors
	 * @see org.springframework.core.Ordered
	 * @see org.springframework.core.annotation.Order
	 * @see org.springframework.core.annotation.AnnotationAwareOrderComparator
	 */
	protected List<Advisor> sortAdvisors(List<Advisor> advisors) {
		AnnotationAwareOrderComparator.sort(advisors);
		return advisors;
	}

	/**
	 * Extension hook that subclasses can override to register additional Advisors,
	 * given the sorted Advisors obtained to date.
	 * <p>The default implementation is empty.
	 * <p>Typically used to add Advisors that expose contextual information
	 * required by some of the later advisors.
	 * @param candidateAdvisors Advisors that have already been identified as
	 * applying to a given bean
	 */
	protected void extendAdvisors(List<Advisor> candidateAdvisors) {
	}

	/**
	   默认不再进行类过滤了，已经过滤了
	 * This auto-proxy creator always returns pre-filtered Advisors.
	 */
	@Override
	protected boolean advisorsPreFiltered() {
		return true;
	}


	/**
	 * Subclass of BeanFactoryAdvisorRetrievalHelper that delegates to
	 * surrounding AbstractAdvisorAutoProxyCreator facilities.
	 */
	private class BeanFactoryAdvisorRetrievalHelperAdapter extends BeanFactoryAdvisorRetrievalHelper {

		public BeanFactoryAdvisorRetrievalHelperAdapter(ConfigurableListableBeanFactory beanFactory) {
			super(beanFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AbstractAdvisorAutoProxyCreator.this.isEligibleAdvisorBean(beanName);
		}
	}

}
```
12. @Aspect在父类的基础上增加Advisor：
```
package org.springframework.aop.aspectj.annotation;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@link AspectJAwareAdvisorAutoProxyCreator} subclass that processes all AspectJ
 * annotation aspects in the current application context, as well as Spring Advisors.
 *
 * <p>Any AspectJ annotated classes will automatically be recognized, and their
 * advice applied if Spring AOP's proxy-based model is capable of applying it.
 * This covers method execution joinpoints.
 *
 * <p>If the &lt;aop:include&gt; element is used, only @AspectJ beans with names matched by
 * an include pattern will be considered as defining aspects to use for Spring auto-proxying.
 *
 * <p>Processing of Spring Advisors follows the rules established in
 * {@link org.springframework.aop.framework.autoproxy.AbstractAdvisorAutoProxyCreator}.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see org.springframework.aop.aspectj.annotation.AspectJAdvisorFactory
 */
@SuppressWarnings("serial")
public class AnnotationAwareAspectJAutoProxyCreator extends AspectJAwareAdvisorAutoProxyCreator {

	private List<Pattern> includePatterns;

	/**
	 * 默认设置默认工厂
	 */
	private AspectJAdvisorFactory aspectJAdvisorFactory = new ReflectiveAspectJAdvisorFactory();

	private BeanFactoryAspectJAdvisorsBuilder aspectJAdvisorsBuilder;


	/**
	 * Set a list of regex patterns, matching eligible @AspectJ bean names.
	 * <p>Default is to consider all @AspectJ beans as eligible.
	 */
	public void setIncludePatterns(List<String> patterns) {
		this.includePatterns = new ArrayList<Pattern>(patterns.size());
		for (String patternText : patterns) {
			this.includePatterns.add(Pattern.compile(patternText));
		}
	}

	public void setAspectJAdvisorFactory(AspectJAdvisorFactory aspectJAdvisorFactory) {
		Assert.notNull(aspectJAdvisorFactory, "AspectJAdvisorFactory must not be null");
		this.aspectJAdvisorFactory = aspectJAdvisorFactory;
	}

	@Override
	protected void initBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		// TODO aspectJAdvisorFactory在定义处赋予了默认值
		super.initBeanFactory(beanFactory);
		this.aspectJAdvisorsBuilder =
				new BeanFactoryAspectJAdvisorsBuilderAdapter(beanFactory, this.aspectJAdvisorFactory);
	}


	/**
	 * TODO 在父类的基础上，增加了@Aspect所在类所在方法注册为Advisor的功能
	 * @return
	 */
	@Override
	protected List<Advisor> findCandidateAdvisors() {
		// Add all the Spring advisors found according to superclass rules.
		List<Advisor> advisors = super.findCandidateAdvisors();
		// Build Advisors for all AspectJ aspects in the bean factory.
		advisors.addAll(this.aspectJAdvisorsBuilder.buildAspectJAdvisors());
		return advisors;
	}

	@Override
	protected boolean isInfrastructureClass(Class<?> beanClass) {
		// Previously we setProxyTargetClass(true) in the constructor, but that has too
		// broad an impact. Instead we now override isInfrastructureClass to avoid proxying
		// aspects. I'm not entirely happy with that as there is no good reason not
		// to advise aspects, except that it causes advice invocation to go through a
		// proxy, and if the aspect implements e.g the Ordered interface it will be
		// proxied by that interface and fail at runtime as the advice method is not
		// defined on the interface. We could potentially relax the restriction about
		// not advising aspects in the future.
		return (super.isInfrastructureClass(beanClass) || this.aspectJAdvisorFactory.isAspect(beanClass));
	}

	/**
	 * Check whether the given aspect bean is eligible for auto-proxying.
	 * <p>If no &lt;aop:include&gt; elements were used then "includePatterns" will be
	 * {@code null} and all beans are included. If "includePatterns" is non-null,
	 * then one of the patterns must match.
	 */
	protected boolean isEligibleAspectBean(String beanName) {
		if (this.includePatterns == null) {
			return true;
		}
		else {
			for (Pattern pattern : this.includePatterns) {
				if (pattern.matcher(beanName).matches()) {
					return true;
				}
			}
			return false;
		}
	}


	/**
	 * Subclass of BeanFactoryAspectJAdvisorsBuilderAdapter that delegates to
	 * surrounding AnnotationAwareAspectJAutoProxyCreator facilities.
	 */
	private class BeanFactoryAspectJAdvisorsBuilderAdapter extends BeanFactoryAspectJAdvisorsBuilder {

		public BeanFactoryAspectJAdvisorsBuilderAdapter(
				ListableBeanFactory beanFactory, AspectJAdvisorFactory advisorFactory) {
			super(beanFactory, advisorFactory);
		}

		@Override
		protected boolean isEligibleBean(String beanName) {
			return AnnotationAwareAspectJAutoProxyCreator.this.isEligibleAspectBean(beanName);
		}
	}

}
```
13. Enhancer创建一个被代理对象的子类并且拦截所有的方法调用（包括从Object中继承的toString和hashCode方法）。Enhancer不能够拦截final方法，例如Object.getClass()方法，这是由于Java final方法语义决定的。基于同样的道理，Enhancer也不能对fianl类进行代理操作。这也是Hibernate为什么不能持久化final class的原因。

14. CGLIB代理
```
package org.springframework.aop.framework;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.*;
import org.springframework.aop.support.AopUtils;
import org.springframework.cglib.core.ClassGenerator;
import org.springframework.cglib.core.CodeGenerationException;
import org.springframework.cglib.core.SpringNamingPolicy;
import org.springframework.cglib.proxy.*;
import org.springframework.cglib.transform.impl.UndeclaredThrowableStrategy;
import org.springframework.core.SmartClassLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * CGLIB-based {@link AopProxy} implementation for the Spring AOP framework.
 *
 * <p>Formerly named {@code Cglib2AopProxy}, as of Spring 3.2, this class depends on
 * Spring's own internally repackaged version of CGLIB 3.</i>.
 *
 * <p>Objects of this type should be obtained through proxy factories,
 * configured by an {@link AdvisedSupport} object. This class is internal
 * to Spring's AOP framework and need not be used directly by client code.
 *
 * <p>{@link DefaultAopProxyFactory} will automatically create CGLIB-based
 * proxies if necessary, for example in case of proxying a target class
 * (see the {@link DefaultAopProxyFactory attendant javadoc} for details).
 *
 * <p>Proxies created using this class are thread-safe if the underlying
 * (target) class is thread-safe.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @author Ramnivas Laddad
 * @author Chris Beams
 * @author Dave Syer
 * @see org.springframework.cglib.proxy.Enhancer
 * @see AdvisedSupport#setProxyTargetClass
 * @see DefaultAopProxyFactory
 */
@SuppressWarnings("serial")
class CglibAopProxy implements AopProxy, Serializable {

	// Constants for CGLIB callback array indices
	private static final int AOP_PROXY = 0;
	private static final int INVOKE_TARGET = 1;
	private static final int NO_OVERRIDE = 2;
	private static final int DISPATCH_TARGET = 3;
	private static final int DISPATCH_ADVISED = 4;
	private static final int INVOKE_EQUALS = 5;
	private static final int INVOKE_HASHCODE = 6;


	/** Logger available to subclasses; static to optimize serialization */
	protected static final Log logger = LogFactory.getLog(CglibAopProxy.class);

	/** Keeps track of the Classes that we have validated for final methods */
	private static final Map<Class<?>, Boolean> validatedClasses = new WeakHashMap<Class<?>, Boolean>();


	/** The configuration used to configure this proxy */
	protected final AdvisedSupport advised;

	protected Object[] constructorArgs;

	protected Class<?>[] constructorArgTypes;

	/** Dispatcher used for methods on Advised */
	private final transient AdvisedDispatcher advisedDispatcher;

	private transient Map<String, Integer> fixedInterceptorMap;

	private transient int fixedInterceptorOffset;


	/**
	 * Create a new CglibAopProxy for the given AOP configuration.
	 * @param config the AOP configuration as AdvisedSupport object
	 * @throws AopConfigException if the config is invalid. We try to throw an informative
	 * exception in this case, rather than let a mysterious failure happen later.
	 */
	public CglibAopProxy(AdvisedSupport config) throws AopConfigException {
		Assert.notNull(config, "AdvisedSupport must not be null");
		if (config.getAdvisors().length == 0 && config.getTargetSource() == AdvisedSupport.EMPTY_TARGET_SOURCE) {
			throw new AopConfigException("No advisors and no TargetSource specified");
		}
		this.advised = config;
		this.advisedDispatcher = new AdvisedDispatcher(this.advised);
	}

	/**
	 * Set constructor arguments to use for creating the proxy.
	 * @param constructorArgs the constructor argument values
	 * @param constructorArgTypes the constructor argument types
	 */
	public void setConstructorArguments(Object[] constructorArgs, Class<?>[] constructorArgTypes) {
		if (constructorArgs == null || constructorArgTypes == null) {
			throw new IllegalArgumentException("Both 'constructorArgs' and 'constructorArgTypes' need to be specified");
		}
		if (constructorArgs.length != constructorArgTypes.length) {
			throw new IllegalArgumentException("Number of 'constructorArgs' (" + constructorArgs.length +
					") must match number of 'constructorArgTypes' (" + constructorArgTypes.length + ")");
		}
		this.constructorArgs = constructorArgs;
		this.constructorArgTypes = constructorArgTypes;
	}


	@Override
	public Object getProxy() {
		return getProxy(null);
	}

    /**
        核心方法，生成CGLIB代理类实例
    */
	@Override
	public Object getProxy(ClassLoader classLoader) {
		if (logger.isDebugEnabled()) {
			logger.debug("Creating CGLIB proxy: target source is " + this.advised.getTargetSource());
		}

		try {
			Class<?> rootClass = this.advised.getTargetClass();
			Assert.state(rootClass != null, "Target class must be available for creating a CGLIB proxy");
			// 代理的目标类
			Class<?> proxySuperClass = rootClass;
			if (ClassUtils.isCglibProxyClass(rootClass)) {
				proxySuperClass = rootClass.getSuperclass();
				Class<?>[] additionalInterfaces = rootClass.getInterfaces();
				for (Class<?> additionalInterface : additionalInterfaces) {
					this.advised.addInterface(additionalInterface);
				}
			}

			// Validate the class, writing log messages as necessary.
			validateClassIfNecessary(proxySuperClass, classLoader);

			// TODO cglib Configure CGLIB Enhancer...
			Enhancer enhancer = createEnhancer();
			if (classLoader != null) {
				enhancer.setClassLoader(classLoader);
				if (classLoader instanceof SmartClassLoader &&
						((SmartClassLoader) classLoader).isClassReloadable(proxySuperClass)) {
					enhancer.setUseCache(false);
				}
			}
			// 设置父类，如HelloServiceImpl
			enhancer.setSuperclass(proxySuperClass);
			// 代理的接口，如HelloService/SpringProxy/Advised
			enhancer.setInterfaces(AopProxyUtils.completeProxiedInterfaces(this.advised));
			enhancer.setNamingPolicy(SpringNamingPolicy.INSTANCE);
			enhancer.setStrategy(new ClassLoaderAwareUndeclaredThrowableStrategy(classLoader));

			Callback[] callbacks = getCallbacks(rootClass);
			Class<?>[] types = new Class<?>[callbacks.length];
			for (int x = 0; x < types.length; x++) {
				types[x] = callbacks[x].getClass();
			}
			// fixedInterceptorMap only populated at this point, after getCallbacks call above
			enhancer.setCallbackFilter(new ProxyCallbackFilter(
					this.advised.getConfigurationOnlyCopy(), this.fixedInterceptorMap, this.fixedInterceptorOffset));
			enhancer.setCallbackTypes(types);

			// Generate the proxy class and create a proxy instance.
			return createProxyClassAndInstance(enhancer, callbacks);
		}
		catch (CodeGenerationException ex) {
			throw new AopConfigException("Could not generate CGLIB subclass of class [" +
					this.advised.getTargetClass() + "]: " +
					"Common causes of this problem include using a final class or a non-visible class",
					ex);
		}
		catch (IllegalArgumentException ex) {
			throw new AopConfigException("Could not generate CGLIB subclass of class [" +
					this.advised.getTargetClass() + "]: " +
					"Common causes of this problem include using a final class or a non-visible class",
					ex);
		}
		catch (Exception ex) {
			// TargetSource.getTarget() failed
			throw new AopConfigException("Unexpected AOP exception", ex);
		}
	}

    /**
       在子类中被覆盖
    */
	protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
		enhancer.setInterceptDuringConstruction(false);
		enhancer.setCallbacks(callbacks);
		return (this.constructorArgs != null ?
				enhancer.create(this.constructorArgTypes, this.constructorArgs) :
				enhancer.create());
	}

	/**
	 * Creates the CGLIB {@link Enhancer}. Subclasses may wish to override this to return a custom
	 * {@link Enhancer} implementation.
	 */
	protected Enhancer createEnhancer() {
		return new Enhancer();
	}

	/**
	 * Checks to see whether the supplied {@code Class} has already been validated and
	 * validates it if not.
	 */
	private void validateClassIfNecessary(Class<?> proxySuperClass, ClassLoader proxyClassLoader) {
		if (logger.isInfoEnabled()) {
			synchronized (validatedClasses) {
				if (!validatedClasses.containsKey(proxySuperClass)) {
					doValidateClass(proxySuperClass, proxyClassLoader);
					validatedClasses.put(proxySuperClass, Boolean.TRUE);
				}
			}
		}
	}

	/**
	 * Checks for final methods on the given {@code Class}, as well as package-visible
	 * methods across ClassLoaders, and writes warnings to the log for each one found.
	 */
	private void doValidateClass(Class<?> proxySuperClass, ClassLoader proxyClassLoader) {
		if (Object.class != proxySuperClass) {
			Method[] methods = proxySuperClass.getDeclaredMethods();
			for (Method method : methods) {
				int mod = method.getModifiers();
				if (!Modifier.isStatic(mod)) {
					if (Modifier.isFinal(mod)) {
						logger.info("Unable to proxy method [" + method + "] because it is final: " +
								"All calls to this method via a proxy will NOT be routed to the target instance.");
					}
					else if (!Modifier.isPublic(mod) && !Modifier.isProtected(mod) && !Modifier.isPrivate(mod) &&
							proxyClassLoader != null && proxySuperClass.getClassLoader() != proxyClassLoader) {
						logger.info("Unable to proxy method [" + method + "] because it is package-visible " +
								"across different ClassLoaders: All calls to this method via a proxy will " +
								"NOT be routed to the target instance.");
					}
				}
			}
			doValidateClass(proxySuperClass.getSuperclass(), proxyClassLoader);
		}
	}

	private Callback[] getCallbacks(Class<?> rootClass) throws Exception {
		// Parameters used for optimisation choices...
		boolean exposeProxy = this.advised.isExposeProxy();
		boolean isFrozen = this.advised.isFrozen();
		boolean isStatic = this.advised.getTargetSource().isStatic();

		// Choose an "aop" interceptor (used for AOP calls).
		// TODO AOP 回调逻辑在这里实现
		Callback aopInterceptor = new DynamicAdvisedInterceptor(this.advised);

		// Choose a "straight to target" interceptor. (used for calls that are
		// unadvised but can return this). May be required to expose the proxy.
		Callback targetInterceptor;
		if (exposeProxy) {
			targetInterceptor = isStatic ?
					new StaticUnadvisedExposedInterceptor(this.advised.getTargetSource().getTarget()) :
					new DynamicUnadvisedExposedInterceptor(this.advised.getTargetSource());
		}
		else {
			targetInterceptor = isStatic ?
					new StaticUnadvisedInterceptor(this.advised.getTargetSource().getTarget()) :
					new DynamicUnadvisedInterceptor(this.advised.getTargetSource());
		}

		// Choose a "direct to target" dispatcher (used for
		// unadvised calls to static targets that cannot return this).
		Callback targetDispatcher = isStatic ?
				new StaticDispatcher(this.advised.getTargetSource().getTarget()) : new SerializableNoOp();

		Callback[] mainCallbacks = new Callback[] {
				aopInterceptor,  // for normal advice
				targetInterceptor,  // invoke target without considering advice, if optimized
				new SerializableNoOp(),  // no override for methods mapped to this
				targetDispatcher, this.advisedDispatcher,
				new EqualsInterceptor(this.advised),
				new HashCodeInterceptor(this.advised)
		};

		Callback[] callbacks;

		// If the target is a static one and the advice chain is frozen,
		// then we can make some optimisations by sending the AOP calls
		// direct to the target using the fixed chain for that method.
		if (isStatic && isFrozen) {
			Method[] methods = rootClass.getMethods();
			Callback[] fixedCallbacks = new Callback[methods.length];
			this.fixedInterceptorMap = new HashMap<String, Integer>(methods.length);

			// TODO: small memory optimisation here (can skip creation for methods with no advice)
			for (int x = 0; x < methods.length; x++) {
				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(methods[x], rootClass);
				fixedCallbacks[x] = new FixedChainStaticTargetInterceptor(
						chain, this.advised.getTargetSource().getTarget(), this.advised.getTargetClass());
				this.fixedInterceptorMap.put(methods[x].toString(), x);
			}

			// Now copy both the callbacks from mainCallbacks
			// and fixedCallbacks into the callbacks array.
			callbacks = new Callback[mainCallbacks.length + fixedCallbacks.length];
			System.arraycopy(mainCallbacks, 0, callbacks, 0, mainCallbacks.length);
			System.arraycopy(fixedCallbacks, 0, callbacks, mainCallbacks.length, fixedCallbacks.length);
			this.fixedInterceptorOffset = mainCallbacks.length;
		}
		else {
			callbacks = mainCallbacks;
		}
		return callbacks;
	}

	/**
	 * Process a return value. Wraps a return of {@code this} if necessary to be the
	 * {@code proxy} and also verifies that {@code null} is not returned as a primitive.
	 */
	private static Object processReturnType(Object proxy, Object target, Method method, Object retVal) {
		// Massage return value if necessary
		if (retVal != null && retVal == target && !RawTargetAccess.class.isAssignableFrom(method.getDeclaringClass())) {
			// Special case: it returned "this". Note that we can't help
			// if the target sets a reference to itself in another returned object.
			retVal = proxy;
		}
		Class<?> returnType = method.getReturnType();
		if (retVal == null && returnType != Void.TYPE && returnType.isPrimitive()) {
			throw new AopInvocationException(
					"Null return value from advice does not match primitive return type for: " + method);
		}
		return retVal;
	}


	@Override
	public boolean equals(Object other) {
		return (this == other || (other instanceof CglibAopProxy &&
				AopProxyUtils.equalsInProxy(this.advised, ((CglibAopProxy) other).advised)));
	}

	@Override
	public int hashCode() {
		return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
	}


	/**
	 * Serializable replacement for CGLIB's NoOp interface.
	 * Public to allow use elsewhere in the framework.
	 */
	public static class SerializableNoOp implements NoOp, Serializable {
	}


	/**
	 * Method interceptor used for static targets with no advice chain. The call
	 * is passed directly back to the target. Used when the proxy needs to be
	 * exposed and it can't be determined that the method won't return
	 * {@code this}.
	 */
	private static class StaticUnadvisedInterceptor implements MethodInterceptor, Serializable {

		private final Object target;

		public StaticUnadvisedInterceptor(Object target) {
			this.target = target;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object retVal = methodProxy.invoke(this.target, args);
			return processReturnType(proxy, this.target, method, retVal);
		}
	}


	/**
	 * Method interceptor used for static targets with no advice chain, when the
	 * proxy is to be exposed.
	 */
	private static class StaticUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

		private final Object target;

		public StaticUnadvisedExposedInterceptor(Object target) {
			this.target = target;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			try {
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object retVal = methodProxy.invoke(this.target, args);
				return processReturnType(proxy, this.target, method, retVal);
			}
			finally {
				AopContext.setCurrentProxy(oldProxy);
			}
		}
	}


	/**
	 * Interceptor used to invoke a dynamic target without creating a method
	 * invocation or evaluating an advice chain. (We know there was no advice
	 * for this method.)
	 */
	private static class DynamicUnadvisedInterceptor implements MethodInterceptor, Serializable {

		private final TargetSource targetSource;

		public DynamicUnadvisedInterceptor(TargetSource targetSource) {
			this.targetSource = targetSource;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object target = this.targetSource.getTarget();
			try {
				Object retVal = methodProxy.invoke(target, args);
				return processReturnType(proxy, target, method, retVal);
			}
			finally {
				this.targetSource.releaseTarget(target);
			}
		}
	}


	/**
	 * Interceptor for unadvised dynamic targets when the proxy needs exposing.
	 */
	private static class DynamicUnadvisedExposedInterceptor implements MethodInterceptor, Serializable {

		private final TargetSource targetSource;

		public DynamicUnadvisedExposedInterceptor(TargetSource targetSource) {
			this.targetSource = targetSource;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			Object target = this.targetSource.getTarget();
			try {
				oldProxy = AopContext.setCurrentProxy(proxy);
				Object retVal = methodProxy.invoke(target, args);
				return processReturnType(proxy, target, method, retVal);
			}
			finally {
				AopContext.setCurrentProxy(oldProxy);
				this.targetSource.releaseTarget(target);
			}
		}
	}


	/**
	 * Dispatcher for a static target. Dispatcher is much faster than
	 * interceptor. This will be used whenever it can be determined that a
	 * method definitely does not return "this"
	 */
	private static class StaticDispatcher implements Dispatcher, Serializable {

		private Object target;

		public StaticDispatcher(Object target) {
			this.target = target;
		}

		@Override
		public Object loadObject() {
			return this.target;
		}
	}


	/**
	 * Dispatcher for any methods declared on the Advised class.
	 */
	private static class AdvisedDispatcher implements Dispatcher, Serializable {

		private final AdvisedSupport advised;

		public AdvisedDispatcher(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object loadObject() throws Exception {
			return this.advised;
		}
	}


	/**
	 * Dispatcher for the {@code equals} method.
	 * Ensures that the method call is always handled by this class.
	 */
	private static class EqualsInterceptor implements MethodInterceptor, Serializable {

		private final AdvisedSupport advised;

		public EqualsInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
			Object other = args[0];
			if (proxy == other) {
				return true;
			}
			if (other instanceof Factory) {
				Callback callback = ((Factory) other).getCallback(INVOKE_EQUALS);
				if (!(callback instanceof EqualsInterceptor)) {
					return false;
				}
				AdvisedSupport otherAdvised = ((EqualsInterceptor) callback).advised;
				return AopProxyUtils.equalsInProxy(this.advised, otherAdvised);
			}
			else {
				return false;
			}
		}
	}


	/**
	 * Dispatcher for the {@code hashCode} method.
	 * Ensures that the method call is always handled by this class.
	 */
	private static class HashCodeInterceptor implements MethodInterceptor, Serializable {

		private final AdvisedSupport advised;

		public HashCodeInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) {
			return CglibAopProxy.class.hashCode() * 13 + this.advised.getTargetSource().hashCode();
		}
	}


	/**
	 * Interceptor used specifically for advised methods on a frozen, static proxy.
	 */
	private static class FixedChainStaticTargetInterceptor implements MethodInterceptor, Serializable {

		private final List<Object> adviceChain;

		private final Object target;

		private final Class<?> targetClass;

		public FixedChainStaticTargetInterceptor(List<Object> adviceChain, Object target, Class<?> targetClass) {
			this.adviceChain = adviceChain;
			this.target = target;
			this.targetClass = targetClass;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			MethodInvocation invocation = new CglibMethodInvocation(proxy, this.target, method, args,
					this.targetClass, this.adviceChain, methodProxy);
			// If we get here, we need to create a MethodInvocation.
			Object retVal = invocation.proceed();
			retVal = processReturnType(proxy, this.target, method, retVal);
			return retVal;
		}
	}


	/**
	  TODO 核心类
	 * General purpose AOP callback. Used when the target is dynamic or when the
	 * proxy is not frozen.
	 */
	private static class DynamicAdvisedInterceptor implements MethodInterceptor, Serializable {

		private final AdvisedSupport advised;

		public DynamicAdvisedInterceptor(AdvisedSupport advised) {
			this.advised = advised;
		}

		@Override
		public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
			Object oldProxy = null;
			boolean setProxyContext = false;
			Class<?> targetClass = null;
			Object target = null;
			try {
				if (this.advised.exposeProxy) {
					// Make invocation available if necessary.
					oldProxy = AopContext.setCurrentProxy(proxy);
					setProxyContext = true;
				}
				// May be null. Get as late as possible to minimize the time we
				// "own" the target, in case it comes from a pool...
				target = getTarget();
				if (target != null) {
					targetClass = target.getClass();
				}
				List<Object> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
				Object retVal;
				// Check whether we only have one InvokerInterceptor: that is,
				// no real advice, but just reflective invocation of the target.
				if (chain.isEmpty() && Modifier.isPublic(method.getModifiers())) {
					// TODO 没有找到代理时，直接调用连接点
					// We can skip creating a MethodInvocation: just invoke the target directly.
					// Note that the final invoker must be an InvokerInterceptor, so we know
					// it does nothing but a reflective operation on the target, and no hot
					// swapping or fancy proxying.
					Object[] argsToUse = AopProxyUtils.adaptArgumentsIfNecessary(method, args);
					// TODO 直接调用连接点
					retVal = methodProxy.invoke(target, argsToUse);
				}
				else {
				    // TODO 创建MI，chain可能被过滤了不少
					// We need to create a method invocation...
					retVal = new CglibMethodInvocation(proxy, target, method, args, targetClass, chain, methodProxy).proceed();
				}
				retVal = processReturnType(proxy, target, method, retVal);
				return retVal;
			}
			finally {
				if (target != null) {
					releaseTarget(target);
				}
				if (setProxyContext) {
					// Restore old proxy.
					AopContext.setCurrentProxy(oldProxy);
				}
			}
		}

		@Override
		public boolean equals(Object other) {
			return (this == other ||
					(other instanceof DynamicAdvisedInterceptor &&
							this.advised.equals(((DynamicAdvisedInterceptor) other).advised)));
		}

		/**
		 * CGLIB uses this to drive proxy creation.
		 */
		@Override
		public int hashCode() {
			return this.advised.hashCode();
		}

		protected Object getTarget() throws Exception {
			return this.advised.getTargetSource().getTarget();
		}

		protected void releaseTarget(Object target) throws Exception {
			this.advised.getTargetSource().releaseTarget(target);
		}
	}


	/**
	 * Implementation of AOP Alliance MethodInvocation used by this AOP proxy.
	 */
	private static class CglibMethodInvocation extends ReflectiveMethodInvocation {

		private final MethodProxy methodProxy;

		private final boolean publicMethod;

		public CglibMethodInvocation(Object proxy, Object target, Method method, Object[] arguments,
				Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers, MethodProxy methodProxy) {

			super(proxy, target, method, arguments, targetClass, interceptorsAndDynamicMethodMatchers);
			this.methodProxy = methodProxy;
			this.publicMethod = Modifier.isPublic(method.getModifiers());
		}

		/**
		 * Gives a marginal performance improvement versus using reflection to
		 * invoke the target when invoking public methods.
		 */
		@Override
		protected Object invokeJoinpoint() throws Throwable {
			if (this.publicMethod) {
			// TODO 这里将method改为methodProxy，提高性能？？
				return this.methodProxy.invoke(this.target, this.arguments);
			}
			else {
			    // TODO 底层反射调用
				return super.invokeJoinpoint();
			}
		}
	}


	/**
	 * CallbackFilter to assign Callbacks to methods.
	 */
	private static class ProxyCallbackFilter implements CallbackFilter {

		private final AdvisedSupport advised;

		private final Map<String, Integer> fixedInterceptorMap;

		private final int fixedInterceptorOffset;

		public ProxyCallbackFilter(AdvisedSupport advised, Map<String, Integer> fixedInterceptorMap, int fixedInterceptorOffset) {
			this.advised = advised;
			this.fixedInterceptorMap = fixedInterceptorMap;
			this.fixedInterceptorOffset = fixedInterceptorOffset;
		}

		/**
		   决定选择哪个callback，返回对应callback的下标
		 * Implementation of CallbackFilter.accept() to return the index of the
		 * callback we need.
		 * <p>The callbacks for each proxy are built up of a set of fixed callbacks
		 * for general use and then a set of callbacks that are specific to a method
		 * for use on static targets with a fixed advice chain.
		 * <p>The callback used is determined thus:
		 * <dl>
		 * <dt>For exposed proxies</dt>
		 * <dd>Exposing the proxy requires code to execute before and after the
		 * method/chain invocation. This means we must use
		 * DynamicAdvisedInterceptor, since all other interceptors can avoid the
		 * need for a try/catch block</dd>
		 * <dt>For Object.finalize():</dt>
		 * <dd>No override for this method is used.</dd>
		 * <dt>For equals():</dt>
		 * <dd>The EqualsInterceptor is used to redirect equals() calls to a
		 * special handler to this proxy.</dd>
		 * <dt>For methods on the Advised class:</dt>
		 * <dd>the AdvisedDispatcher is used to dispatch the call directly to
		 * the target</dd>
		 * <dt>For advised methods:</dt>
		 * <dd>If the target is static and the advice chain is frozen then a
		 * FixedChainStaticTargetInterceptor specific to the method is used to
		 * invoke the advice chain. Otherwise a DyanmicAdvisedInterceptor is
		 * used.</dd>
		 * <dt>For non-advised methods:</dt>
		 * <dd>Where it can be determined that the method will not return {@code this}
		 * or when {@code ProxyFactory.getExposeProxy()} returns {@code false},
		 * then a Dispatcher is used. For static targets, the StaticDispatcher is used;
		 * and for dynamic targets, a DynamicUnadvisedInterceptor is used.
		 * If it possible for the method to return {@code this} then a
		 * StaticUnadvisedInterceptor is used for static targets - the
		 * DynamicUnadvisedInterceptor already considers this.</dd>
		 * </dl>
		 */
		@Override
		public int accept(Method method) {
			if (AopUtils.isFinalizeMethod(method)) {
				logger.debug("Found finalize() method - using NO_OVERRIDE");
				return NO_OVERRIDE;
			}
			if (!this.advised.isOpaque() && method.getDeclaringClass().isInterface() &&
					method.getDeclaringClass().isAssignableFrom(Advised.class)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Method is declared on Advised interface: " + method);
				}
				return DISPATCH_ADVISED;
			}
			// We must always proxy equals, to direct calls to this.
			if (AopUtils.isEqualsMethod(method)) {
				logger.debug("Found 'equals' method: " + method);
				return INVOKE_EQUALS;
			}
			// We must always calculate hashCode based on the proxy.
			if (AopUtils.isHashCodeMethod(method)) {
				logger.debug("Found 'hashCode' method: " + method);
				return INVOKE_HASHCODE;
			}
			Class<?> targetClass = this.advised.getTargetClass();
			// Proxy is not yet available, but that shouldn't matter.
			List<?> chain = this.advised.getInterceptorsAndDynamicInterceptionAdvice(method, targetClass);
			boolean haveAdvice = !chain.isEmpty();
			boolean exposeProxy = this.advised.isExposeProxy();
			boolean isStatic = this.advised.getTargetSource().isStatic();
			boolean isFrozen = this.advised.isFrozen();
			if (haveAdvice || !isFrozen) {
				// If exposing the proxy, then AOP_PROXY must be used.
				if (exposeProxy) {
					if (logger.isDebugEnabled()) {
						logger.debug("Must expose proxy on advised method: " + method);
					}
					return AOP_PROXY;
				}
				String key = method.toString();
				// Check to see if we have fixed interceptor to serve this method.
				// Else use the AOP_PROXY.
				if (isStatic && isFrozen && this.fixedInterceptorMap.containsKey(key)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Method has advice and optimisations are enabled: " + method);
					}
					// We know that we are optimising so we can use the FixedStaticChainInterceptors.
					int index = this.fixedInterceptorMap.get(key);
					return (index + this.fixedInterceptorOffset);
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("Unable to apply any optimisations to advised method: " + method);
					}
					return AOP_PROXY;
				}
			}
			else {
				// See if the return type of the method is outside the class hierarchy
				// of the target type. If so we know it never needs to have return type
				// massage and can use a dispatcher.
				// If the proxy is being exposed, then must use the interceptor the
				// correct one is already configured. If the target is not static, then
				// cannot use a dispatcher because the target cannot be released.
				if (exposeProxy || !isStatic) {
					return INVOKE_TARGET;
				}
				Class<?> returnType = method.getReturnType();
				if (targetClass == returnType) {
					if (logger.isDebugEnabled()) {
						logger.debug("Method " + method +
								"has return type same as target type (may return this) - using INVOKE_TARGET");
					}
					return INVOKE_TARGET;
				}
				else if (returnType.isPrimitive() || !returnType.isAssignableFrom(targetClass)) {
					if (logger.isDebugEnabled()) {
						logger.debug("Method " + method +
								" has return type that ensures this cannot be returned- using DISPATCH_TARGET");
					}
					return DISPATCH_TARGET;
				}
				else {
					if (logger.isDebugEnabled()) {
						logger.debug("Method " + method +
								"has return type that is assignable from the target type (may return this) - " +
								"using INVOKE_TARGET");
					}
					return INVOKE_TARGET;
				}
			}
		}

		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof ProxyCallbackFilter)) {
				return false;
			}
			ProxyCallbackFilter otherCallbackFilter = (ProxyCallbackFilter) other;
			AdvisedSupport otherAdvised = otherCallbackFilter.advised;
			if (this.advised == null || otherAdvised == null) {
				return false;
			}
			if (this.advised.isFrozen() != otherAdvised.isFrozen()) {
				return false;
			}
			if (this.advised.isExposeProxy() != otherAdvised.isExposeProxy()) {
				return false;
			}
			if (this.advised.getTargetSource().isStatic() != otherAdvised.getTargetSource().isStatic()) {
				return false;
			}
			if (!AopProxyUtils.equalsProxiedInterfaces(this.advised, otherAdvised)) {
				return false;
			}
			// Advice instance identity is unimportant to the proxy class:
			// All that matters is type and ordering.
			Advisor[] thisAdvisors = this.advised.getAdvisors();
			Advisor[] thatAdvisors = otherAdvised.getAdvisors();
			if (thisAdvisors.length != thatAdvisors.length) {
				return false;
			}
			for (int i = 0; i < thisAdvisors.length; i++) {
				Advisor thisAdvisor = thisAdvisors[i];
				Advisor thatAdvisor = thatAdvisors[i];
				if (!equalsAdviceClasses(thisAdvisor, thatAdvisor)) {
					return false;
				}
				if (!equalsPointcuts(thisAdvisor, thatAdvisor)) {
					return false;
				}
			}
			return true;
		}

		private boolean equalsAdviceClasses(Advisor a, Advisor b) {
			Advice aa = a.getAdvice();
			Advice ba = b.getAdvice();
			if (aa == null || ba == null) {
				return (aa == ba);
			}
			return (aa.getClass() == ba.getClass());
		}

		private boolean equalsPointcuts(Advisor a, Advisor b) {
			// If only one of the advisor (but not both) is PointcutAdvisor, then it is a mismatch.
			// Takes care of the situations where an IntroductionAdvisor is used (see SPR-3959).
			return (!(a instanceof PointcutAdvisor) ||
					(b instanceof PointcutAdvisor &&
							ObjectUtils.nullSafeEquals(((PointcutAdvisor) a).getPointcut(), ((PointcutAdvisor) b).getPointcut())));
		}

		@Override
		public int hashCode() {
			int hashCode = 0;
			Advisor[] advisors = this.advised.getAdvisors();
			for (Advisor advisor : advisors) {
				Advice advice = advisor.getAdvice();
				if (advice != null) {
					hashCode = 13 * hashCode + advice.getClass().hashCode();
				}
			}
			hashCode = 13 * hashCode + (this.advised.isFrozen() ? 1 : 0);
			hashCode = 13 * hashCode + (this.advised.isExposeProxy() ? 1 : 0);
			hashCode = 13 * hashCode + (this.advised.isOptimize() ? 1 : 0);
			hashCode = 13 * hashCode + (this.advised.isOpaque() ? 1 : 0);
			return hashCode;
		}
	}


	/**
	 * CGLIB GeneratorStrategy variant which exposes the application ClassLoader
	 * as thread context ClassLoader for the time of class generation
	 * (in order for ASM to pick it up when doing common superclass resolution).
	 */
	private static class ClassLoaderAwareUndeclaredThrowableStrategy extends UndeclaredThrowableStrategy {

		private final ClassLoader classLoader;

		public ClassLoaderAwareUndeclaredThrowableStrategy(ClassLoader classLoader) {
			super(UndeclaredThrowableException.class);
			this.classLoader = classLoader;
		}

		@Override
		public byte[] generate(ClassGenerator cg) throws Exception {
			if (this.classLoader == null) {
				return super.generate(cg);
			}

			Thread currentThread = Thread.currentThread();
			ClassLoader threadContextClassLoader;
			try {
				threadContextClassLoader = currentThread.getContextClassLoader();
			}
			catch (Throwable ex) {
				// Cannot access thread context ClassLoader - falling back...
				return super.generate(cg);
			}

			boolean overrideClassLoader = !this.classLoader.equals(threadContextClassLoader);
			if (overrideClassLoader) {
				currentThread.setContextClassLoader(this.classLoader);
			}
			try {
				return super.generate(cg);
			}
			finally {
				if (overrideClassLoader) {
					// Reset original thread context ClassLoader.
					currentThread.setContextClassLoader(threadContextClassLoader);
				}
			}
		}
	}

}
```
15. CGLIB类库已经直接集成到Spring-core中，子类中对Enhancer.create进行覆盖：
```
package org.springframework.aop.framework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.Factory;
import org.springframework.objenesis.SpringObjenesis;

/**
 * Objenesis-based extension of {@link CglibAopProxy} to create proxy instances
 * without invoking the constructor of the class.
 *
 * @author Oliver Gierke
 * @author Juergen Hoeller
 * @since 4.0
 */
@SuppressWarnings("serial")
class ObjenesisCglibAopProxy extends CglibAopProxy {

	private static final Log logger = LogFactory.getLog(ObjenesisCglibAopProxy.class);

	private static final SpringObjenesis objenesis = new SpringObjenesis();


	/**
	 * Create a new ObjenesisCglibAopProxy for the given AOP configuration.
	 * @param config the AOP configuration as AdvisedSupport object
	 */
	public ObjenesisCglibAopProxy(AdvisedSupport config) {
		super(config);
	}


	@Override
	@SuppressWarnings("unchecked")
	protected Object createProxyClassAndInstance(Enhancer enhancer, Callback[] callbacks) {
		Class<?> proxyClass = enhancer.createClass();
		Object proxyInstance = null;

		if (objenesis.isWorthTrying()) {
			try {
				proxyInstance = objenesis.newInstance(proxyClass, enhancer.getUseCache());
			}
			catch (Throwable ex) {
				logger.debug("Unable to instantiate proxy using Objenesis, " +
						"falling back to regular proxy construction", ex);
			}
		}

		if (proxyInstance == null) {
			// Regular instantiation via default constructor...
			try {
				proxyInstance = (this.constructorArgs != null ?
						proxyClass.getConstructor(this.constructorArgTypes).newInstance(this.constructorArgs) :
						proxyClass.newInstance());
			}
			catch (Throwable ex) {
				throw new AopConfigException("Unable to instantiate proxy using Objenesis, " +
						"and regular proxy instantiation via default constructor fails as well", ex);
			}
		}

		((Factory) proxyInstance).setCallbacks(callbacks);
		return proxyInstance;
	}
}
```
