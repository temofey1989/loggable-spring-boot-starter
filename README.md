![example event parameter](https://github.com/temofey1989/logging-spring-boot-starter/actions/workflows/build.yml/badge.svg?branch=main)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=temofey1989_logging-spring-boot-starter&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=temofey1989_logging-spring-boot-starter)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

# Overview

Logging extension for [Spring Boot](https://spring.io/projects/spring-boot) application to simplify logging of the function execution (input parameters, return value and thrown exception).

The goal of this extension is to systematize and simplify the way of logging.

---

## Setup

### Maven

```xml

<dependency>
    <groupId>io.justdevit.spring</groupId>
    <artifactId>logging-spring-boot-starter</artifactId>
    <version>${logging-spring-boot-starter.version}</version>
</dependency>
```

### Gradle

```groovy
implementation group: 'io.justdevit.spring', name: 'logging-spring-boot-starter', version: $loggingSpringBootStarterVersion
```

### Gradle (kt)

```kotlin
implementation("io.justdevit.spring:logging-spring-boot-starter:$loggingSpringBootStarterVersion")
```

### Spring Configuration

To set action log format define `logging.action-log-format` parameter in the `application.properties` or `application.yml` file.

**Supported formats:**

| Format          | Description                                                                                        |
|-----------------|----------------------------------------------------------------------------------------------------|
| `console`       | Standard format for logging to the console (**default**).                                          |
| `logstash-json` | Format for [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder).      |
| `custom`        | Custom format. Use this value only in case you have your own implementation of `ActionLogResolver` |

---

## Features

### Loggable Annotation

To apply logging of the function just add `@Loggable` annotation on method or class.  
In case of annotated class all public (non-final) methods will be wrapped with logging interceptor.

For example:

```kotlin
@Service
class HelloService {

    @Loggable
    fun sayHello(name: String = "world"): String = "Hello $name!"
}
```

Calling `helloService.sayHello("Peter")` will generate this logging records:

```
2021-12-11 19:42:46.005 INFO ... io.justdevit.HelloService : Action 'HelloService::sayHello' has started. Parameters: [name=Peter] 
2021-12-11 19:42:46.005 INFO ... io.justdevit.HelloService : Action 'HelloService::sayHello' has successfully finished. Return value: [Hello Peter!]
```

In case of using [Logstash Logback Encoder](https://github.com/logfellow/logstash-logback-encoder) (_pretty_):

```json
{
  "@timestamp": "2021-12-11 19:42:46.005",
  ...
  "level": "INFO",
  "logger": "io.justdevit.HelloService",
  "action": "HelloService::sayHello",
  "message": "Action 'HelloService::sayHello' has started.",
  "arguments": {
    "name": "Peter"
  }
}
{
  "@timestamp": "2021-12-11 19:42:46.005",
  ...
  "level": "INFO",
  "logger": "io.justdevit.HelloService",
  "action": "HelloService::sayHello",
  "message": "Action 'HelloService::sayHello' has successfully finished.",
  "arguments": {
    "result": "Hello Peter!"
  }
}
```

Logging in JSON format helps Logs Aggregator such ELK, Splunk, etc. optimize indexing of the logs, which have a huge benefit for searching and analyzing logs of your application.

### Custom Action Name Resolution

In some cases you can have a requirement to generate custom action names for your methods.  
To do so just implement `ActionNameResolver` interface and register it as a Spring bean.  
You can have as many custom method as you need. For example for `@RestController` method or messaging listener method.

### Custom Action Log Format

In case you need specific format for logging messages, just implement `ActionLogResolver` and set `logging.action-log-format = custom`.
