# oh-my-request

`HTTP` request easier operation.

[![](https://img.shields.io/travis/biezhi/oh-my-request.svg)](https://travis-ci.org/biezhi/oh-my-request)
[![](https://img.shields.io/maven-central/v/io.github.biezhi/oh-my-request.svg)](http://search.maven.org/#search%7Cga%7C1%7Coh-my-request)
[![](https://img.shields.io/badge/license-Apache2-FF0080.svg)](https://github.com/biezhi/oh-my-request/blob/master/LICENSE)
[![@biezhi on zhihu](https://img.shields.io/badge/zhihu-%40biezhi-red.svg)](https://www.zhihu.com/people/biezhi)
[![](https://img.shields.io/github/followers/biezhi.svg?style=social&label=Follow%20Me)](https://github.com/biezhi)

# Usage

**Maven ArtifactId**

```xml
<dependency>
    <groupId>io.github.biezhi</groupId>
    <artifactId>oh-my-request</artifactId>
    <version>0.0.1</version>
</dependency>
```

## 1. Get Request

```java
String body = Request.get("https://github.com/opensearch.xml").body();
System.out.println(body);
```

## 2. Save To File

```java
Request.get("https://avatars3.githubusercontent.com/u/3849072?v=3&s=460")
.receive(new File("D:/avatar.png"));
```

## 3. Post Request

```java
Request.post("http://xxxx.com")
.form("name", "jack")
.body();
```

## 4. Headers

```java
Request.get("http://xxxx.com")
.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) xxxx")
.body();
```

# License

[Apache2](https://github.com/biezhi/oh-my-request/blob/master/LICENSE)