embedded-redis
==============

[![Build Status](https://ci.appveyor.com/api/projects/status/github/kstyrc/embedded-redis?branch=master&svg=true)](https://ci.appveyor.com/project/kstyrc/embedded-redis) Windows

[![Build Status](https://travis-ci.org/kstyrc/embedded-redis.png?branch=master)](https://travis-ci.org/kstyrc/embedded-redis) Linux

Redis embedded server for Java integration testing

Maven dependency
==============

Maven Central:
```xml
<dependency>
  <groupId>com.github.kstyrc</groupId>
  <artifactId>embedded-redis</artifactId>
  <version>0.6</version>
</dependency>
```

Previous releases (before 0.6):
```xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>

...

<dependency>
  <groupId>redis.embedded</groupId>
  <artifactId>embedded-redis</artifactId>
  <version>0.5</version>
</dependency>
```

Usage
==============

Running RedisServer is as simple as:
```java
RedisServer redisServer = new RedisServer(6379);
redisServer.start();
// do some work
redisServer.stop();
```

You can also provide RedisServer with your own executable:
```java
// 1) given explicit file (os-independence broken!)
RedisServer redisServer = new RedisServer("/path/to/your/redis", 6379);

// 2) given os-independent matrix
RedisExecProvider customProvider = RedisExecProvider.defaultProvider()
  .override(OS.UNIX, "/path/to/unix/redis")
  .override(OS.WINDOWS, Architecture.x86, "/path/to/windows/redis")
  .override(OS.Windows, Architecture.x86_64, "/path/to/windows/redis")
  .override(OS.MAC_OS_X, Architecture.x86, "/path/to/macosx/redis")
  .override(OS.MAC_OS_X, Architecture.x86_64, "/path/to/macosx/redis")
  
RedisServer redisServer = new RedisServer(customProvider, 6379);
```

You can also use fluent API to create RedisServer:
```java
RedisServer redisServer = RedisServer.builder()
  .redisExecProvider(customRedisProvider)
  .port(6379)
  .slaveOf("locahost", 6378)
  .configFile("/path/to/your/redis.conf")
  .build();
```

Or even create simple redis.conf file from scratch:
```java
RedisServer redisServer = RedisServer.builder()
  .redisExecProvider(customRedisProvider)
  .port(6379)
  .slaveOf("locahost", 6378)
  .setting("daemonize no")
  .setting("appendonly no")
  .setting("maxheap 128M")
  .build();
```

## Setting up a cluster

Our Embedded Redis has support for HA Redis clusters with Sentinels and master-slave replication

#### Using ephemeral ports
A simple redis integration test with Redis cluster on ephemeral ports, with setup similar to that from production would look like this:
```java
public class SomeIntegrationTestThatRequiresRedis {
  private RedisCluster cluster;
  private Set<String> jedisSentinelHosts;

  @Before
  public void setup() throws Exception {
    //creates a cluster with 3 sentinels, quorum size of 2 and 3 replication groups, each with one master and one slave
    cluster = RedisCluster.builder().ephemeral().sentinelCount(3).quorumSize(2)
                    .replicationGroup("master1", 1)
                    .replicationGroup("master2", 1)
                    .replicationGroup("master3", 1)
                    .build();
    cluster.start();

    //retrieve ports on which sentinels have been started, using a simple Jedis utility class
    jedisSentinelHosts = JedisUtil.sentinelHosts(cluster);
  }
  
  @Test
  public void test() throws Exception {
    // testing code that requires redis running
    JedisSentinelPool pool = new JedisSentinelPool("master1", jedisSentinelHosts);
  }
  
  @After
  public void tearDown() throws Exception {
    cluster.stop();
  }
}
```

#### Retrieving ports
The above example starts Redis cluster on ephemeral ports, which you can later get with ```cluster.ports()```,
which will return a list of all ports of the cluster. You can also get ports of sentinels with ```cluster.sentinelPorts()```
or servers with ```cluster.serverPorts()```. ```JedisUtil``` class contains utility methods for use with Jedis client.

#### Using predefined ports
You can also start Redis cluster on predefined ports and even mix both approaches:
```java
public class SomeIntegrationTestThatRequiresRedis {
  private RedisCluster cluster;

  @Before
  public void setup() throws Exception {
    final List<Integer> sentinels = Arrays.asList(26739, 26912);
    final List<Integer> group1 = Arrays.asList(6667, 6668);
    final List<Integer> group2 = Arrays.asList(6387, 6379);
    //creates a cluster with 3 sentinels, quorum size of 2 and 3 replication groups, each with one master and one slave
    cluster = RedisCluster.builder().sentinelPorts(sentinels).quorumSize(2)
                    .serverPorts(group1).replicationGroup("master1", 1)
                    .serverPorts(group2).replicationGroup("master2", 1)
                    .ephemeralServers().replicationGroup("master3", 1)
                    .build();
    cluster.start();
  }
//(...)
```
The above will create and start a cluster with sentinels on ports ```26739, 26912```, first replication group on ```6667, 6668```,
second replication group on ```6387, 6379``` and third replication group on ephemeral ports.

Redis version
==============

When not provided with the desired redis executable, RedisServer runs os-dependent executable enclosed in jar. Currently is uses:
- Redis 2.8.19 in case of Linux/Unix
- Redis 2.8.19 in case of OSX
- Redis 2.8.19 in case of Windows: https://github.com/MSOpenTech/redis/releases/tag/win-2.8.19

However, you should provide RedisServer with redis executable if you need specific version.


License
==============
Licensed under the Apache License, Version 2.0


Contributors
==============
 * Krzysztof Styrc ([@kstyrc](http://github.com/kstyrc))
 * Piotr Turek ([@turu](http://github.com/turu))
 * anthonyu ([@anthonyu](http://github.com/anthonyu))
 * Artem Orobets ([@enisher](http://github.com/enisher))
 * Sean Simonsen ([@SeanSimonsen](http://github.com/SeanSimonsen))
 * Rob Winch ([@rwinch](http://github.com/rwinch))


Changelog
==============

### 0.6
 * Support JDK 6 +

### 0.5
 * OS detection fix
 * redis binary per OS/arch pair
 * Updated to 2.8.19 binary for Windows

### 0.4 
 * Updated for Java 8
 * Added Sentinel support
 * Ability to create arbitrary clusters on arbitrary (ephemeral) ports
 * Updated to latest guava 
 * Throw an exception if redis has not been started
 * Redis errorStream logged to System.out

### 0.3
 * Fluent API for RedisServer creation

### 0.2
 * Initial decent release
