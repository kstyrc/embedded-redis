embedded-redis
==============

Forked from (https://github.com/kstyrc/embedded-redis)</br>
Based on the embedded-redis(of kstyrc), this embedded-redis updated the following functions.
- Support redis server 4.0.9
- Provide functions for [Redis Cluster](http://redis.io/topics/cluster-tutorial) (not only sentinel custer)
- So, previous RedisCluster is changed to RedisSentinelCluster
- And RedisCluster is added to support Redis Cluster(which comes from Redis 3.0)

Compatibility
- Linux (Works All)
- Windows (Sentinel doesn't work)
- Mac (Sentinel doesn't work)

Redis Version
- Linux (Redis 4.0.9)
- Windows (Redis 3.0.504)
- mac (Redis 4.0.9)

Usage
==============

Running RedisServer cluster like :

```java
        List<ClusterMaster> masters1 = new LinkedList<ClusterMaster>();
        masters1.add(new ClusterMaster("127.0.0.1", 9400,
                3, 0));

        List<ClusterSlave> slaves1 = new LinkedList<ClusterSlave>();
        slaves1.add(new ClusterSlave("127.0.0.1", 9410,
                "127.0.0.1", 9400));

        RedisCluster redisCluster1 = RedisCluster.builder()
                .clusterNodeTimeoutMS(1000) // can be omitted, default is 3 seconds
                .basicAuthPassword("password") // can be omitted(if you don't want use AUTH)
                .masters(masters1)
                .slaves(slaves1)
                .meetWith("127.0.0.1", 9400)
                .build();

        redisCluster1.start();


        List<ClusterMaster> masters2 = new LinkedList<ClusterMaster>();
        masters2.add(new ClusterMaster("127.0.0.1", 9500,
                3, 1));

        List<ClusterSlave> slaves2 = new LinkedList<ClusterSlave>();
        slaves2.add(new ClusterSlave("127.0.0.1", 9510,
                "127.0.0.1", 9500));

        RedisCluster redisCluster2 = RedisCluster.builder()
                .clusterNodeTimeoutMS(1000) // can be omitted, default is 3 seconds
                .basicAuthPassword("password") // can be omitted(if you don't want use AUTH)
                .masters(masters2)
                .slaves(slaves2)
                .meetWith("127.0.0.1", 9400)
                .build();

        redisCluster2.start();



        List<ClusterMaster> masters3 = new LinkedList<ClusterMaster>();
        masters3.add(new ClusterMaster("127.0.0.1", 9600,
                3, 2));

        List<ClusterSlave> slaves3 = new LinkedList<ClusterSlave>();
        slaves3.add(new ClusterSlave("127.0.0.1", 9610,
                "127.0.0.1", 9600));

        RedisCluster redisCluster3 = RedisCluster.builder()
                .clusterNodeTimeoutMS(1000) // can be omitted, default is 3 seconds
                .basicAuthPassword("password") // can be omitted(if you don't want use AUTH)
                .masters(masters3)
                .slaves(slaves3)
                .meetWith("127.0.0.1", 9400)
                .build();

        redisCluster3.setWaitForClusterTimeoutMS(10000).start();

        Thread.sleep(5000);

        redisCluster1.stop();
        redisCluster2.stop();
        redisCluster3.stop();
```
