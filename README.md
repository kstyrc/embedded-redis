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
  <version>0.1</version>
</dependency>
```
More at https://clojars.org/redis.embedded/embedded-redis

Usage example
==============

TODO


Redis version
==============

When not provided with the desired redis executable, RedisServer runs os-dependent executable enclosed in jar. Currently is uses:
- Redis 2.6.14 in case of Linux/Unix
- unofficial Win32/64 port from https://github.com/MSOpenTech/redis (branch 2.6) in case of Windows

However, you should provide RedisServer with redis executable if you need specific version.
