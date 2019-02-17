package com.jessin;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author zexin.guo
 * @create 2019-02-17 下午2:39
 **/
public class ZkTest implements Watcher {
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    ZooKeeper zooKeeper = null;
    Stat stat = new Stat();
    String rootPath = "/zk_root";
    String childrenPath1 = "/zk_root/c1";
    String childrenPath2 = "/zk_root/c2";

    @Test
    public void test1() {
        try {

            zooKeeper = new ZooKeeper("localhost:2181", 900, this);
            countDownLatch.await();

            System.out.println(Thread.currentThread() + "zk连接状态：" + zooKeeper.getState());
//            zooKeeper.create(rootPath,
//                    "".getBytes(),
//                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
//                    CreateMode.PERSISTENT);
            // TODO 监听节点数据变化，获取数据，可以监听watcher，只对该rootPath
            zooKeeper.getData(rootPath, true, stat);
            // -1表示最新版本，基于最新版本操作
            zooKeeper.setData(rootPath, "123".getBytes(), -1);

            // TODO exist watch，判断节点是否存在，可以监听节点创建、删除、数据变化，无法监听子节点变化
            zooKeeper.exists(childrenPath1, true);
            // 创建临时节点，会话断开后节点自动删除，放开所有的权限
            String path = zooKeeper.create(childrenPath1,
                    "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
            System.out.println("成功创建节点：" + path);
            zooKeeper.exists(childrenPath1, true);
            zooKeeper.setData(childrenPath1, "hello".getBytes(), -1);
            zooKeeper.exists(childrenPath1, true);
            zooKeeper.delete(childrenPath1, -1);

            // TODO 注册本实例为watcher，获取children可以监听，子节点watch
            List<String> childrenList = zooKeeper.getChildren(rootPath, true);
            System.out.println(rootPath + "的子节点为：" + childrenList);
            zooKeeper.create(childrenPath2,
                    "".getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL);
            Thread.sleep(10000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println(Thread.currentThread() + "收到watch event：" + watchedEvent);
        if (Event.KeeperState.SyncConnected != watchedEvent.getState()) {
            return;
        }
        try {
            if (Event.EventType.None == watchedEvent.getType() && watchedEvent.getPath() == null) {
                countDownLatch.countDown();
            } else if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {

                // watcher具有一次性的特点，需要反复注册，而且只通知路径下的子节点发生了变化，需要自己获取子节点数据
                System.out.println("重新获取子节点，路径为：" + watchedEvent.getPath() +
                        zooKeeper.getChildren(watchedEvent.getPath(), true));
            } else if (watchedEvent.getType() == Event.EventType.NodeDataChanged) {
                // 重新注册watcher，持续监听，stat中的值会被改变为最新的值
                System.out.println(rootPath +
                        " data : " + new String(zooKeeper.getData(rootPath, true, stat)));
                System.out.println(stat.getCzxid() + "," + stat.getMzxid() + "," + stat.getCversion());
            } else if (watchedEvent.getType() == Event.EventType.NodeCreated) {
                System.out.println("节点被创建：" + watchedEvent.getPath());
            } else if (watchedEvent.getType() == Event.EventType.NodeDeleted) {
                System.out.println("节点被删除：" + watchedEvent.getPath());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

