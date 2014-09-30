embedded-redis
==============

Redis embedded server for Java integration testing


Maven dependency
==============

Currently embedded-redis is available in clojars repository:
```
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

Dependency configuration:
```
<dependency>
  <groupId>redis.embedded</groupId>
  <artifactId>embedded-redis</artifactId>
  <version>0.3</version>
</dependency>
```
More at https://clojars.org/redis.embedded/embedded-redis

Usage example
==============

Running RedisServer is as simple as:
```
RedisServer redisServer = new RedisServer(6379);
redisServer.start();
// do some work
redisServer.stop();
```
You can also provide RedisServer with your own redis executable to run:
```
RedisServer redisServer = new RedisServer("/path/to/your/redis", 6379);
```
You can also use fluent API to create RedisServer:
```
RedisServer redisServer = RedisServer.builder()
  .executable("/path/to/your/redis")
  .port(6379)
  .slaveOf("locahost", 6378)
  .configFile("/path/to/your/redis.conf")
  .build();
```
Or even create simple redis.conf file from scratch:
```
RedisServer redisServer = RedisServer.builder()
  .executable("/path/to/your/redis")
  .port(6379)
  .slaveOf("locahost", 6378)
  .setting("daemonize no")
  .setting("appendonly no")
  .build();
```
A simple redis integration test would look like this:
```
public class SomeIntegrationTestThatRequiresRedis {
  private RedisServer redisServer;
  
  @Before
  public void setup() throws Exception {
    redisServer = new RedisServer(6379); // or new RedisServer("/path/to/your/redis", 6379);
    redisServer.start();
  }
  
  @Test
  public void test() throws Exception {
    // testing code that requires redis running
  }
  
  @After
  public void tearDown() throws Exception {
    redisServer.stop();
  }
}
```


Redis version
==============

When not provided with the desired redis executable, RedisServer runs os-dependent executable enclosed in jar. Currently is uses:
- Redis 2.6.14 in case of Linux/Unix
- unofficial Win32/64 port from https://github.com/MSOpenTech/redis (branch 2.6) in case of Windows

However, you should provide RedisServer with redis executable if you need specific version.
