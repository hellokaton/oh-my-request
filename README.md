# oh-my-request

this project is easy use http request.

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