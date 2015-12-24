# Java8 Request

This project is Java8 implementation of HTTP requests

# Usage

## 1. Get Request

```java
String body = get("https://github.com/opensearch.xml").body();
System.out.println(body);
```

## 2. Save To File

```java
get("https://avatars3.githubusercontent.com/u/3849072?v=3&s=460")
.saveAsDisk(new File("D:/avatar.png"));
```

## 3. Post Request

```java
post("http://xxxx.com")
.param("name", "jack")
.body();
```

## 4. Headers

```java
get("http://xxxx.com")
.header(Header.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; WOW64) xxxx")
.body();
```