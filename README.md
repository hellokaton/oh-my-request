# Java8 Request

This project is Java8 implementation of HTTP requests

# Usage

## 1. send get request 

```java
String body = get("https://github.com/opensearch.xml").body();
System.out.println(body);
```

## 2. get url save to file

```java
get("https://avatars3.githubusercontent.com/u/3849072?v=3&s=460").saveAsDisk(new File("D:/avatar.png"));
```

