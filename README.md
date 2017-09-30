# oh-my-request

this project is easy use http request.

<p align="center">
    <a href="https://travis-ci.org/biezhi/oh-my-request"><img src="https://img.shields.io/travis/biezhi/oh-my-request.svg?style=flat-square"></a>
    <a href="http://search.maven.org/#search%7Cga%7C1%7Coh-my-request"><img src="https://img.shields.io/maven-central/v/io.github.biezhi/oh-my-request.svg?style=flat-square"></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square"></a>
</p>

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