embedded-redis
==============

Redis embedded server for Java integration testing

Usage
==============

Running RedisServer is as simple as:
```java
RedisServer redisServer = new RedisServer(6379);
redisServer.start();
// do some work
redisServer.stop();
```

You can also provide RedisServer with your own redis executable to run:
```java
RedisServer redisServer = new RedisServer("/path/to/your/redis", 6379);
```

You can also use fluent API to create RedisServer:
```java
RedisServer redisServer = RedisServer.builder()
  .executable("/path/to/your/redis")
  .port(6379)
  .slaveOf("locahost", 6378)
  .configFile("/path/to/your/redis.conf")
  .build();
```

Or even create simple redis.conf file from scratch:
```java
RedisServer redisServer = RedisServer.builder()
  .executable("/path/to/your/redis")
  .port(6379)
  .slaveOf("locahost", 6378)
  .setting("daemonize no")
  .setting("appendonly no")
  .build();
```

Our Embedded Redis has support for HA Redis clusters with Sentinels and master-slave replication

A simple redis integration test with Redis cluster setup similar to that from production would look like this:
```java
public class SomeIntegrationTestThatRequiresRedis {
  private RedisCluster cluster;
  
  @Before
  public void setup() throws Exception {
    //creates a cluster with 3 sentinels, quorum size of 2 and 3 replication groups, each with one master and one slave
    cluster = RedisCluster.builder().sentinelCount(3).quorumSize(2)
                    .replicationGroup("master1", 1)
                    .replicationGroup("master2", 1)
                    .replicationGroup("master3", 1)
                    .build();
    cluster.start();
  }
  
  @Test
  public void test() throws Exception {
    // testing code that requires redis running
  }
  
  @After
  public void tearDown() throws Exception {
    cluster.stop();
  }
}
```


Redis version
==============

When not provided with the desired redis executable, RedisServer runs os-dependent executable enclosed in jar. Currently is uses:
- Redis 2.8.19 in case of Linux/Unix
- Redis 2.8.19 in case of OSX
- unofficial Win32/64 port from https://github.com/MSOpenTech/redis (branch 2.6) in case of Windows

However, you should provide RedisServer with redis executable if you need specific version.
