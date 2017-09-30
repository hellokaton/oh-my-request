package io.github.biezhi.request;

public interface Const {

    /**
     * 'UTF-8' charset name
     */
    String CHARSET_UTF8 = "UTF-8";

    /**
     * 'application/x-www-form-urlencoded' content type header value
     */
    String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    /**
     * 'application/json' content type header value
     */
    String CONTENT_TYPE_JSON = "application/json";

    /**
     * 'gzip' encoding header value
     */
    String ENCODING_GZIP = "gzip";

    /**
     * 'Accept' header name
     */
    String HEADER_ACCEPT = "Accept";

    /**
     * 'Accept-Charset' header name
     */
    String HEADER_ACCEPT_CHARSET = "Accept-Charset";

    /**
     * 'Accept-Encoding' header name
     */
    String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

    /**
     * 'Authorization' header name
     */
    String HEADER_AUTHORIZATION = "Authorization";

    /**
     * 'Cache-Control' header name
     */
    String HEADER_CACHE_CONTROL = "Cache-Control";

    /**
     * 'Content-Encoding' header name
     */
    String HEADER_CONTENT_ENCODING = "Content-Encoding";

    /**
     * 'Content-Length' header name
     */
    String HEADER_CONTENT_LENGTH = "Content-Length";

    /**
     * 'Content-Type' header name
     */
    String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * 'Date' header name
     */
    String HEADER_DATE = "Date";

    /**
     * 'ETag' header name
     */
    String HEADER_ETAG = "ETag";

    /**
     * 'Expires' header name
     */
    String HEADER_EXPIRES = "Expires";

    /**
     * 'If-None-Match' header name
     */
    String HEADER_IF_NONE_MATCH = "If-None-Match";

    /**
     * 'Last-Modified' header name
     */
    String HEADER_LAST_MODIFIED = "Last-Modified";

    /**
     * 'Location' header name
     */
    String HEADER_LOCATION = "Location";

    /**
     * 'Proxy-Authorization' header name
     */
    String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";

    /**
     * 'Referer' header name
     */
    String HEADER_REFERER = "Referer";

    /**
     * 'Server' header name
     */
    String HEADER_SERVER = "Server";

    /**
     * 'User-Agent' header name
     */
    String HEADER_USER_AGENT = "User-Agent";

    /**
     * 'DELETE' request method
     */
    String METHOD_DELETE = "DELETE";

    /**
     * 'GET' request method
     */
    String METHOD_GET = "GET";

    /**
     * 'HEAD' request method
     */
    String METHOD_HEAD = "HEAD";

    /**
     * 'OPTIONS' options method
     */
    String METHOD_OPTIONS = "OPTIONS";

    /**
     * 'POST' request method
     */
    String METHOD_POST = "POST";

    /**
     * 'PUT' request method
     */
    String METHOD_PUT = "PUT";

    /**
     * 'TRACE' request method
     */
    String METHOD_TRACE = "TRACE";

    /**
     * 'charset' header value parameter
     */
    String PARAM_CHARSET = "charset";

    String DEFAULT_BOUNDARY = "00content0boundary00";

    String DEFAULT_CONTENT_TYPE_MULTIPART = "multipart/form-data; boundary=" + DEFAULT_BOUNDARY;

    String CRLF = "\r\n";

    String[] EMPTY_STRINGS = new String[0];

}
