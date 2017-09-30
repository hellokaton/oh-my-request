package io.github.biezhi.request;

import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.AccessController;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPInputStream;

import static io.github.biezhi.request.Const.*;
import static io.github.biezhi.request.Utils.append;
import static io.github.biezhi.request.Utils.encode;
import static java.net.HttpURLConnection.*;
import static java.net.Proxy.Type.HTTP;

/**
 * A fluid interface for making HTTP requests using an underlying
 * {@link HttpURLConnection} (or sub-class).
 * <p>
 * Each instance supports making a single request and cannot be reused for
 * further requests.
 */
public final class Request {

    private static SSLSocketFactory TRUSTED_FACTORY;
    private static HostnameVerifier TRUSTED_VERIFIER;
    private static ConnectionFactory CONNECTION_FACTORY = ConnectionFactory.DEFAULT;

    private HttpURLConnection connection = null;

    private final URL url;

    private final String requestMethod;

    private RequestOutputStream output;

    private boolean multipart;

    private boolean form;

    private boolean ignoreCloseExceptions = true;

    private boolean unCompress = false;

    private int bufferSize = 8192;

    private long totalSize = -1;

    private long totalWritten = 0;

    private Proxy proxy;

    private UploadProgress progress = UploadProgress.DEFAULT;

    private static String getValidCharset(final String charset) {
        if (charset != null && charset.length() > 0)
            return charset;
        else
            return CHARSET_UTF8;
    }

    private static SSLSocketFactory getTrustedFactory()
            throws RequestException {
        if (TRUSTED_FACTORY == null) {
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    // Intentionally left blank
                }
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    // Intentionally left blank
                }
            }};
            try {
                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustAllCerts, new SecureRandom());
                TRUSTED_FACTORY = context.getSocketFactory();
            } catch (GeneralSecurityException e) {
                IOException ioException = new IOException("Security exception configuring SSL context");
                ioException.initCause(e);
                throw new RequestException(ioException);
            }
        }
        return TRUSTED_FACTORY;
    }

    private static HostnameVerifier getTrustedVerifier() {
        if (TRUSTED_VERIFIER == null)
            TRUSTED_VERIFIER = (hostname, session) -> true;
        return TRUSTED_VERIFIER;
    }



    /**
     * Specify the {@link ConnectionFactory} used to create new requests.
     */
    public static void setConnectionFactory(final ConnectionFactory connectionFactory) {
        if (connectionFactory == null)
            CONNECTION_FACTORY = ConnectionFactory.DEFAULT;
        else
            CONNECTION_FACTORY = connectionFactory;
    }

    /**
     * Start a 'GET' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request get(final CharSequence url) throws RequestException {
        return new Request(url, METHOD_GET);
    }

    /**
     * Start a 'GET' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request get(final URL url) throws RequestException {
        return new Request(url, METHOD_GET);
    }

    /**
     * Start a 'GET' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params  The query parameters to include as part of the baseUrl
     * @param encode  true to encode the full URL
     * @return request
     */
    public static Request get(final CharSequence baseUrl,
                              final Map<?, ?> params, final boolean encode) {
        String url = append(baseUrl, params);
        return get(encode ? encode(url) : url);
    }

    /**
     * Start a 'GET' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode  true to encode the full URL
     * @param params  the name/value query parameter pairs to include as part of the
     *                baseUrl
     * @return request
     */
    public static Request get(final CharSequence baseUrl,
                              final boolean encode, final Object... params) {
        String url = append(baseUrl, params);
        return get(encode ? encode(url) : url);
    }

    /**
     * Start a 'POST' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request post(final CharSequence url)
            throws RequestException {
        return new Request(url, METHOD_POST);
    }

    /**
     * Start a 'POST' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request post(final URL url) throws RequestException {
        return new Request(url, METHOD_POST);
    }

    /**
     * Start a 'POST' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params  the query parameters to include as part of the baseUrl
     * @param encode  true to encode the full URL
     * @return request
     */
    public static Request post(final CharSequence baseUrl,
                               final Map<?, ?> params, final boolean encode) {
        String url = append(baseUrl, params);
        return post(encode ? encode(url) : url);
    }

    /**
     * Start a 'POST' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode  true to encode the full URL
     * @param params  the name/value query parameter pairs to include as part of the
     *                baseUrl
     * @return request
     */
    public static Request post(final CharSequence baseUrl,
                               final boolean encode, final Object... params) {
        String url = append(baseUrl, params);
        return post(encode ? encode(url) : url);
    }

    /**
     * Start a 'PUT' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request put(final CharSequence url)
            throws RequestException {
        return new Request(url, METHOD_PUT);
    }

    /**
     * Start a 'PUT' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request put(final URL url) throws RequestException {
        return new Request(url, METHOD_PUT);
    }

    /**
     * Start a 'PUT' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params  the query parameters to include as part of the baseUrl
     * @param encode  true to encode the full URL
     * @return request
     */
    public static Request put(final CharSequence baseUrl,
                              final Map<?, ?> params, final boolean encode) {
        String url = append(baseUrl, params);
        return put(encode ? encode(url) : url);
    }

    /**
     * Start a 'PUT' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode  true to encode the full URL
     * @param params  the name/value query parameter pairs to include as part of the
     *                baseUrl
     * @return request
     */
    public static Request put(final CharSequence baseUrl,
                              final boolean encode, final Object... params) {
        String url = append(baseUrl, params);
        return put(encode ? encode(url) : url);
    }

    /**
     * Start a 'DELETE' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request delete(final CharSequence url)
            throws RequestException {
        return new Request(url, METHOD_DELETE);
    }

    /**
     * Start a 'DELETE' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request delete(final URL url) throws RequestException {
        return new Request(url, METHOD_DELETE);
    }

    /**
     * Start a 'DELETE' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params  The query parameters to include as part of the baseUrl
     * @param encode  true to encode the full URL
     * @return request
     */
    public static Request delete(final CharSequence baseUrl,
                                 final Map<?, ?> params, final boolean encode) {
        String url = append(baseUrl, params);
        return delete(encode ? encode(url) : url);
    }

    /**
     * Start a 'DELETE' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode  true to encode the full URL
     * @param params  the name/value query parameter pairs to include as part of the
     *                baseUrl
     * @return request
     */
    public static Request delete(final CharSequence baseUrl,
                                 final boolean encode, final Object... params) {
        String url = append(baseUrl, params);
        return delete(encode ? encode(url) : url);
    }

    /**
     * Start a 'HEAD' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request head(final CharSequence url)
            throws RequestException {
        return new Request(url, METHOD_HEAD);
    }

    /**
     * Start a 'HEAD' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request head(final URL url) throws RequestException {
        return new Request(url, METHOD_HEAD);
    }

    /**
     * Start a 'HEAD' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params  The query parameters to include as part of the baseUrl
     * @param encode  true to encode the full URL
     * @return request
     */
    public static Request head(final CharSequence baseUrl,
                               final Map<?, ?> params, final boolean encode) {
        String url = append(baseUrl, params);
        return head(encode ? encode(url) : url);
    }

    /**
     * Start a 'GET' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode  true to encode the full URL
     * @param params  the name/value query parameter pairs to include as part of the
     *                baseUrl
     * @return request
     */
    public static Request head(final CharSequence baseUrl,
                               final boolean encode, final Object... params) {
        String url = append(baseUrl, params);
        return head(encode ? encode(url) : url);
    }

    /**
     * Start an 'OPTIONS' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request options(final CharSequence url)
            throws RequestException {
        return new Request(url, METHOD_OPTIONS);
    }

    /**
     * Start an 'OPTIONS' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request options(final URL url) throws RequestException {
        return new Request(url, METHOD_OPTIONS);
    }

    /**
     * Start a 'TRACE' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request trace(final CharSequence url)
            throws RequestException {
        return new Request(url, METHOD_TRACE);
    }

    /**
     * Start a 'TRACE' request to the given URL
     *
     * @param url
     * @return request
     * @throws RequestException
     */
    public static Request trace(final URL url) throws RequestException {
        return new Request(url, METHOD_TRACE);
    }

    /**
     * Set the 'http.keepAlive' property to the given value.
     * <p>
     * This setting will apply to all requests.
     *
     * @param keepAlive
     */
    public static void keepAlive(final boolean keepAlive) {
        setProperty("http.keepAlive", Boolean.toString(keepAlive));
    }

    /**
     * Set the 'http.maxConnections' property to the given value.
     * <p>
     * This setting will apply to all requests.
     *
     * @param maxConnections
     */
    public static void maxConnections(final int maxConnections) {
        setProperty("http.maxConnections", Integer.toString(maxConnections));
    }

    /**
     * Set the 'http.proxyHost' and 'https.proxyHost' properties to the given host
     * value.
     * <p>
     * This setting will apply to all requests.
     *
     * @param host
     */
    public static void proxyHost(final String host) {
        setProperty("http.proxyHost", host);
        setProperty("https.proxyHost", host);
    }

    /**
     * Set the 'http.proxyPort' and 'https.proxyPort' properties to the given port
     * number.
     * <p>
     * This setting will apply to all requests.
     *
     * @param port
     */
    public static void proxyPort(final int port) {
        final String portValue = Integer.toString(port);
        setProperty("http.proxyPort", portValue);
        setProperty("https.proxyPort", portValue);
    }

    /**
     * Set the 'http.nonProxyHosts' property to the given host values.
     * <p>
     * Hosts will be separated by a '|' character.
     * <p>
     * This setting will apply to all requests.
     *
     * @param hosts
     */
    public static void nonProxyHosts(final String... hosts) {
        if (hosts != null && hosts.length > 0) {
            StringBuilder separated = new StringBuilder();
            int           last      = hosts.length - 1;
            for (int i = 0; i < last; i++)
                separated.append(hosts[i]).append('|');
            separated.append(hosts[last]);
            setProperty("http.nonProxyHosts", separated.toString());
        } else
            setProperty("http.nonProxyHosts", null);
    }

    /**
     * Set property to given value.
     * <p>
     * Specifying a null value will cause the property to be cleared
     *
     * @param name
     * @param value
     * @return previous value
     */
    private static String setProperty(final String name, final String value) {
        final PrivilegedAction<String> action;
        if (value != null)
            action = () -> System.setProperty(name, value);
        else
            action = () -> System.clearProperty(name);
        return AccessController.doPrivileged(action);
    }

    /**
     * Create HTTP connection wrapper
     *
     * @param url    Remote resource URL.
     * @param method HTTP request method (e.g., "GET", "POST").
     * @throws RequestException
     */
    public Request(final CharSequence url, final String method) throws RequestException {
        try {
            this.url = new URL(url.toString());
        } catch (MalformedURLException e) {
            throw new RequestException(e);
        }
        this.requestMethod = method;
    }

    /**
     * Create HTTP connection wrapper
     *
     * @param url    Remote resource URL.
     * @param method HTTP request method (e.g., "GET", "POST").
     * @throws RequestException
     */
    public Request(final URL url, final String method) throws RequestException {
        this.url = url;
        this.requestMethod = method;
    }

    private HttpURLConnection createConnection() {
        try {
            final HttpURLConnection connection;
            if (this.proxy != null)
                connection = CONNECTION_FACTORY.create(url, this.proxy);
            else
                connection = CONNECTION_FACTORY.create(url);
            connection.setRequestMethod(requestMethod);
            return connection;
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }

    @Override
    public String toString() {
        return method() + ' ' + url();
    }

    /**
     * Get underlying connection
     *
     * @return connection
     */
    public HttpURLConnection getConnection() {
        if (connection == null)
            connection = createConnection();
        return connection;
    }

    /**
     * Set whether or not to ignore exceptions that occur from calling
     * {@link Closeable#close()}
     * <p>
     * The default value of this setting is <code>true</code>
     *
     * @param ignore
     * @return this request
     */
    public Request ignoreCloseExceptions(final boolean ignore) {
        ignoreCloseExceptions = ignore;
        return this;
    }

    /**
     * Get whether or not exceptions thrown by {@link Closeable#close()} are
     * ignored
     *
     * @return true if ignoring, false if throwing
     */
    public boolean ignoreCloseExceptions() {
        return ignoreCloseExceptions;
    }

    /**
     * Get the status code of the response
     *
     * @return the response code
     * @throws RequestException
     */
    public int code() throws RequestException {
        try {
            closeOutput();
            return getConnection().getResponseCode();
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }

    /**
     * Set the value of the given {@link AtomicInteger} to the status code of the
     * response
     *
     * @param output
     * @return this request
     * @throws RequestException
     */
    public Request code(final AtomicInteger output) throws RequestException {
        output.set(code());
        return this;
    }

    /**
     * Is the response code a 200 OK?
     *
     * @return true if 200, false otherwise
     * @throws RequestException
     */
    public boolean ok() throws RequestException {
        return HTTP_OK == code();
    }

    /**
     * Is the response code a 201 Created?
     *
     * @return true if 201, false otherwise
     * @throws RequestException
     */
    public boolean created() throws RequestException {
        return HTTP_CREATED == code();
    }

    /**
     * Is the response code a 204 No Content?
     *
     * @return true if 204, false otherwise
     * @throws RequestException
     */
    public boolean noContent() throws RequestException {
        return HTTP_NO_CONTENT == code();
    }

    /**
     * Is the response code a 500 Internal Server Error?
     *
     * @return true if 500, false otherwise
     * @throws RequestException
     */
    public boolean serverError() throws RequestException {
        return HTTP_INTERNAL_ERROR == code();
    }

    /**
     * Is the response code a 400 Bad Request?
     *
     * @return true if 400, false otherwise
     * @throws RequestException
     */
    public boolean badRequest() throws RequestException {
        return HTTP_BAD_REQUEST == code();
    }

    /**
     * Is the response code a 404 Not Found?
     *
     * @return true if 404, false otherwise
     * @throws RequestException
     */
    public boolean notFound() throws RequestException {
        return HTTP_NOT_FOUND == code();
    }

    /**
     * Is the response code a 304 Not Modified?
     *
     * @return true if 304, false otherwise
     * @throws RequestException
     */
    public boolean notModified() throws RequestException {
        return HTTP_NOT_MODIFIED == code();
    }

    /**
     * Get status message of the response
     *
     * @return message
     * @throws RequestException
     */
    public String message() throws RequestException {
        try {
            closeOutput();
            return getConnection().getResponseMessage();
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }

    /**
     * Disconnect the connection
     *
     * @return this request
     */
    public Request disconnect() {
        getConnection().disconnect();
        return this;
    }

    /**
     * Set chunked streaming mode to the given size
     *
     * @param size
     * @return this request
     */
    public Request chunk(final int size) {
        getConnection().setChunkedStreamingMode(size);
        return this;
    }

    /**
     * Set the size used when buffering and copying between streams
     * <p>
     * This size is also used for send and receive buffers created for both char
     * and byte arrays
     * <p>
     * The default buffer size is 8,192 bytes
     *
     * @param size
     * @return this request
     */
    public Request bufferSize(final int size) {
        if (size < 1)
            throw new IllegalArgumentException("Size must be greater than zero");
        bufferSize = size;
        return this;
    }

    /**
     * Get the configured buffer size
     * <p>
     * The default buffer size is 8,192 bytes
     *
     * @return buffer size
     */
    public int bufferSize() {
        return bufferSize;
    }

    /**
     * Set whether or not the response body should be automatically uncompressed
     * when read from.
     * <p>
     * This will only affect requests that have the 'Content-Encoding' response
     * header set to 'gzip'.
     * <p>
     * This causes all receive methods to use a {@link GZIPInputStream} when
     * applicable so that higher level streams and readers can read the data
     * uncompressed.
     * <p>
     * Setting this option does not cause any request headers to be set
     * automatically so {@link #acceptGzipEncoding()} should be used in
     * conjunction with this setting to tell the server to gzip the response.
     *
     * @param uncompress
     * @return this request
     */
    public Request uncompress(final boolean uncompress) {
        this.unCompress = uncompress;
        return this;
    }

    /**
     * Create byte array output stream
     *
     * @return stream
     */
    protected ByteArrayOutputStream byteStream() {
        final int size = contentLength();
        if (size > 0)
            return new ByteArrayOutputStream(size);
        else
            return new ByteArrayOutputStream();
    }

    /**
     * Get response as {@link String} in given character set
     * <p>
     * This will fall back to using the UTF-8 character set if the given charset
     * is null
     *
     * @param charset
     * @return string
     * @throws RequestException
     */
    public String body(final String charset) throws RequestException {
        final ByteArrayOutputStream output = byteStream();
        try {
            copy(buffer(), output);
            return output.toString(getValidCharset(charset));
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }

    /**
     * Get response as {@link String} using character set returned from
     * {@link #charset()}
     *
     * @return string
     * @throws RequestException
     */
    public String body() throws RequestException {
        return body(charset());
    }

    /**
     * Is the response body empty?
     *
     * @return true if the Content-Length response header is 0, false otherwise
     * @throws RequestException
     */
    public boolean isBodyEmpty() throws RequestException {
        return contentLength() == 0;
    }

    /**
     * Get response as byte array
     *
     * @return byte array
     * @throws RequestException
     */
    public byte[] bytes() throws RequestException {
        final ByteArrayOutputStream output = byteStream();
        try {
            copy(buffer(), output);
        } catch (IOException e) {
            throw new RequestException(e);
        }
        return output.toByteArray();
    }

    /**
     * Get response in a buffered stream
     *
     * @return stream
     * @throws RequestException
     * @see #bufferSize(int)
     */
    public BufferedInputStream buffer() throws RequestException {
        return new BufferedInputStream(stream(), bufferSize);
    }

    /**
     * Get stream to response body
     *
     * @return stream
     * @throws RequestException
     */
    public InputStream stream() throws RequestException {
        InputStream stream;
        if (code() < HTTP_BAD_REQUEST) {
            try {
                stream = getConnection().getInputStream();
            } catch (IOException e) {
                throw new RequestException(e);
            }
        } else {
            stream = getConnection().getErrorStream();
            if (stream == null) {
                try {
                    stream = getConnection().getInputStream();
                } catch (IOException e) {
                    if (contentLength() > 0)
                        throw new RequestException(e);
                    else
                        stream = new ByteArrayInputStream(new byte[0]);
                }
            }
        }
        if (!unCompress || !ENCODING_GZIP.equals(contentEncoding()))
            return stream;
        try {
            return new GZIPInputStream(stream);
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }

    /**
     * Get reader to response body using given character set.
     * <p>
     * This will fall back to using the UTF-8 character set if the given charset
     * is null
     *
     * @param charset
     * @return reader
     * @throws RequestException
     */
    public InputStreamReader reader(final String charset) throws RequestException {
        try {
            return new InputStreamReader(stream(), getValidCharset(charset));
        } catch (UnsupportedEncodingException e) {
            throw new RequestException(e);
        }
    }

    /**
     * Get reader to response body using the character set returned from
     * {@link #charset()}
     *
     * @return reader
     * @throws RequestException
     */
    public InputStreamReader reader() throws RequestException {
        return reader(charset());
    }

    /**
     * Get buffered reader to response body using the given character set r and
     * the configured buffer size
     *
     * @param charset
     * @return reader
     * @throws RequestException
     * @see #bufferSize(int)
     */
    public BufferedReader bufferedReader(final String charset) throws RequestException {
        return new BufferedReader(reader(charset), bufferSize);
    }

    /**
     * Get buffered reader to response body using the character set returned from
     * {@link #charset()} and the configured buffer size
     *
     * @return reader
     * @throws RequestException
     * @see #bufferSize(int)
     */
    public BufferedReader bufferedReader() throws RequestException {
        return bufferedReader(charset());
    }

    /**
     * Stream response body to file
     *
     * @param file
     * @return this request
     * @throws RequestException
     */
    public Request receive(final File file) throws RequestException {
        final OutputStream output;
        try {
            output = new BufferedOutputStream(new FileOutputStream(file), bufferSize);
        } catch (FileNotFoundException e) {
            throw new RequestException(e);
        }
        return new CloseOperation<Request>(output, ignoreCloseExceptions) {
            @Override
            protected Request run() throws RequestException, IOException {
                return receive(output);
            }
        }.call();
    }

    /**
     * Stream response to given output stream
     *
     * @param output
     * @return this request
     * @throws RequestException
     */
    public Request receive(final OutputStream output) throws RequestException {
        try {
            return copy(buffer(), output);
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }

    /**
     * Stream response to given print stream
     *
     * @param output
     * @return this request
     * @throws RequestException
     */
    public Request receive(final PrintStream output) throws RequestException {
        return receive((OutputStream) output);
    }

    /**
     * Receive response into the given writer
     *
     * @param writer
     * @return this request
     * @throws RequestException
     */
    public Request receive(final Writer writer) throws RequestException {
        final BufferedReader reader = bufferedReader();
        return new CloseOperation<Request>(reader, ignoreCloseExceptions) {
            @Override
            public Request run() throws IOException {
                return copy(reader, writer);
            }
        }.call();
    }

    /**
     * Set read timeout on connection to given value
     *
     * @param timeout
     * @return this request
     */
    public Request readTimeout(final int timeout) {
        getConnection().setReadTimeout(timeout);
        return this;
    }

    /**
     * Set connect timeout on connection to given value
     *
     * @param timeout
     * @return this request
     */
    public Request connectTimeout(final int timeout) {
        getConnection().setConnectTimeout(timeout);
        return this;
    }

    /**
     * Set header name to given value
     *
     * @param name
     * @param value
     * @return this request
     */
    public Request header(final String name, final String value) {
        getConnection().setRequestProperty(name, value);
        return this;
    }

    /**
     * Set header name to given value
     *
     * @param name
     * @param value
     * @return this request
     */
    public Request header(final String name, final Number value) {
        return header(name, value != null ? value.toString() : null);
    }

    /**
     * Set all headers found in given map where the keys are the header names and
     * the values are the header values
     *
     * @param headers
     * @return this request
     */
    public Request headers(final Map<String, String> headers) {
        if (!headers.isEmpty())
            for (Entry<String, String> header : headers.entrySet())
                header(header);
        return this;
    }

    /**
     * Set header to have given entry's key as the name and value as the value
     *
     * @param header
     * @return this request
     */
    public Request header(final Entry<String, String> header) {
        return header(header.getKey(), header.getValue());
    }

    /**
     * Get a response header
     *
     * @param name
     * @return response header
     * @throws RequestException
     */
    public String header(final String name) throws RequestException {
        closeOutputQuietly();
        return getConnection().getHeaderField(name);
    }

    /**
     * Get all the response headers
     *
     * @return map of response header names to their value(s)
     * @throws RequestException
     */
    public Map<String, List<String>> headers() throws RequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFields();
    }

    /**
     * Get a date header from the response falling back to returning -1 if the
     * header is missing or parsing fails
     *
     * @param name
     * @return date, -1 on failures
     * @throws RequestException
     */
    public long dateHeader(final String name) throws RequestException {
        return dateHeader(name, -1L);
    }

    /**
     * Get a date header from the response falling back to returning the given
     * default value if the header is missing or parsing fails
     *
     * @param name
     * @param defaultValue
     * @return date, default value on failures
     * @throws RequestException
     */
    public long dateHeader(final String name, final long defaultValue) throws RequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFieldDate(name, defaultValue);
    }

    /**
     * Get an integer header from the response falling back to returning -1 if the
     * header is missing or parsing fails
     *
     * @param name
     * @return header value as an integer, -1 when missing or parsing fails
     * @throws RequestException
     */
    public int intHeader(final String name) throws RequestException {
        return intHeader(name, -1);
    }

    /**
     * Get an integer header value from the response falling back to the given
     * default value if the header is missing or if parsing fails
     *
     * @param name
     * @param defaultValue
     * @return header value as an integer, default value when missing or parsing
     * fails
     * @throws RequestException
     */
    public int intHeader(final String name, final int defaultValue) throws RequestException {
        closeOutputQuietly();
        return getConnection().getHeaderFieldInt(name, defaultValue);
    }

    /**
     * Get all values of the given header from the response
     *
     * @param name
     * @return non-null but possibly empty array of {@link String} header values
     */
    public String[] headers(final String name) {
        final Map<String, List<String>> headers = headers();
        if (headers == null || headers.isEmpty())
            return EMPTY_STRINGS;

        final List<String> values = headers.get(name);
        if (values != null && !values.isEmpty())
            return values.toArray(new String[values.size()]);
        else
            return EMPTY_STRINGS;
    }

    /**
     * Get parameter with given name from header value in response
     *
     * @param headerName
     * @param paramName
     * @return parameter value or null if missing
     */
    public String parameter(final String headerName, final String paramName) {
        return getParam(header(headerName), paramName);
    }

    /**
     * Get all parameters from header value in response
     * <p>
     * This will be all key=value pairs after the first ';' that are separated by
     * a ';'
     *
     * @param headerName
     * @return non-null but possibly empty map of parameter headers
     */
    public Map<String, String> parameters(final String headerName) {
        return getParams(header(headerName));
    }

    /**
     * Get parameter values from header value
     *
     * @param header
     * @return parameter value or null if none
     */
    protected Map<String, String> getParams(final String header) {
        if (header == null || header.length() == 0)
            return Collections.emptyMap();

        final int headerLength = header.length();
        int       start        = header.indexOf(';') + 1;
        if (start == 0 || start == headerLength)
            return Collections.emptyMap();

        int end = header.indexOf(';', start);
        if (end == -1)
            end = headerLength;

        Map<String, String> params = new LinkedHashMap<>();
        while (start < end) {
            int nameEnd = header.indexOf('=', start);
            if (nameEnd != -1 && nameEnd < end) {
                String name = header.substring(start, nameEnd).trim();
                if (name.length() > 0) {
                    String value  = header.substring(nameEnd + 1, end).trim();
                    int    length = value.length();
                    if (length != 0)
                        if (length > 2 && '"' == value.charAt(0) && '"' == value.charAt(length - 1))
                            params.put(name, value.substring(1, length - 1));
                        else
                            params.put(name, value);
                }
            }

            start = end + 1;
            end = header.indexOf(';', start);
            if (end == -1)
                end = headerLength;
        }

        return params;
    }

    /**
     * Get parameter value from header value
     *
     * @param value
     * @param paramName
     * @return parameter value or null if none
     */
    private String getParam(final String value, final String paramName) {
        if (value == null || value.length() == 0)
            return null;

        final int length = value.length();
        int       start  = value.indexOf(';') + 1;
        if (start == 0 || start == length)
            return null;

        int end = value.indexOf(';', start);
        if (end == -1)
            end = length;

        while (start < end) {
            int nameEnd = value.indexOf('=', start);
            if (nameEnd != -1 && nameEnd < end
                    && paramName.equals(value.substring(start, nameEnd).trim())) {
                String paramValue  = value.substring(nameEnd + 1, end).trim();
                int    valueLength = paramValue.length();
                if (valueLength != 0)
                    if (valueLength > 2 && '"' == paramValue.charAt(0)
                            && '"' == paramValue.charAt(valueLength - 1))
                        return paramValue.substring(1, valueLength - 1);
                    else
                        return paramValue;
            }

            start = end + 1;
            end = value.indexOf(';', start);
            if (end == -1)
                end = length;
        }

        return null;
    }

    /**
     * Get 'charset' parameter from 'Content-Type' response header
     *
     * @return charset or null if none
     */
    public String charset() {
        return parameter(HEADER_CONTENT_TYPE, PARAM_CHARSET);
    }

    /**
     * Set the 'User-Agent' header to given value
     *
     * @param userAgent
     * @return this request
     */
    public Request userAgent(final String userAgent) {
        return header(HEADER_USER_AGENT, userAgent);
    }

    /**
     * Set the 'Referer' header to given value
     *
     * @param referer
     * @return this request
     */
    public Request referer(final String referer) {
        return header(HEADER_REFERER, referer);
    }

    /**
     * Set value of {@link HttpURLConnection#setUseCaches(boolean)}
     *
     * @param useCaches
     * @return this request
     */
    public Request useCaches(final boolean useCaches) {
        getConnection().setUseCaches(useCaches);
        return this;
    }

    /**
     * Set the 'Accept-Encoding' header to given value
     *
     * @param acceptEncoding
     * @return this request
     */
    public Request acceptEncoding(final String acceptEncoding) {
        return header(HEADER_ACCEPT_ENCODING, acceptEncoding);
    }

    /**
     * Set the 'Accept-Encoding' header to 'gzip'
     *
     * @return this request
     * @see #uncompress(boolean)
     */
    public Request acceptGzipEncoding() {
        return acceptEncoding(ENCODING_GZIP);
    }

    /**
     * Set the 'Accept-Charset' header to given value
     *
     * @param acceptCharset
     * @return this request
     */
    public Request acceptCharset(final String acceptCharset) {
        return header(HEADER_ACCEPT_CHARSET, acceptCharset);
    }

    /**
     * Get the 'Content-Encoding' header from the response
     *
     * @return this request
     */
    public String contentEncoding() {
        return header(HEADER_CONTENT_ENCODING);
    }

    /**
     * Get the 'Server' header from the response
     *
     * @return server
     */
    public String server() {
        return header(HEADER_SERVER);
    }

    /**
     * Get the 'Date' header from the response
     *
     * @return date value, -1 on failures
     */
    public long date() {
        return dateHeader(HEADER_DATE);
    }

    /**
     * Get the 'Cache-Control' header from the response
     *
     * @return cache control
     */
    public String cacheControl() {
        return header(HEADER_CACHE_CONTROL);
    }

    /**
     * Get the 'ETag' header from the response
     *
     * @return entity tag
     */
    public String eTag() {
        return header(HEADER_ETAG);
    }

    /**
     * Get the 'Expires' header from the response
     *
     * @return expires value, -1 on failures
     */
    public long expires() {
        return dateHeader(HEADER_EXPIRES);
    }

    /**
     * Get the 'Last-Modified' header from the response
     *
     * @return last modified value, -1 on failures
     */
    public long lastModified() {
        return dateHeader(HEADER_LAST_MODIFIED);
    }

    /**
     * Get the 'Location' header from the response
     *
     * @return location
     */
    public String location() {
        return header(HEADER_LOCATION);
    }

    /**
     * Set the 'Authorization' header to given value
     *
     * @param authorization
     * @return this request
     */
    public Request authorization(final String authorization) {
        return header(HEADER_AUTHORIZATION, authorization);
    }

    /**
     * Set the 'Proxy-Authorization' header to given value
     *
     * @param proxyAuthorization
     * @return this request
     */
    public Request proxyAuthorization(final String proxyAuthorization) {
        return header(HEADER_PROXY_AUTHORIZATION, proxyAuthorization);
    }

    /**
     * Set the 'Authorization' header to given values in Basic authentication
     * format
     *
     * @param name
     * @param password
     * @return this request
     */
    public Request basic(final String name, final String password) {
        return authorization("Basic " + Base64.getEncoder().encodeToString((name + ':' + password).getBytes()));
    }

    /**
     * Set the 'Proxy-Authorization' header to given values in Basic authentication
     * format
     *
     * @param name
     * @param password
     * @return this request
     */
    public Request proxyBasic(final String name, final String password) {
        return proxyAuthorization("Basic " + Base64.getEncoder().encodeToString((name + ':' + password).getBytes()));
    }

    /**
     * Set the 'If-Modified-Since' request header to the given value
     *
     * @param ifModifiedSince
     * @return this request
     */
    public Request ifModifiedSince(final long ifModifiedSince) {
        getConnection().setIfModifiedSince(ifModifiedSince);
        return this;
    }

    /**
     * Set the 'If-None-Match' request header to the given value
     *
     * @param ifNoneMatch
     * @return this request
     */
    public Request ifNoneMatch(final String ifNoneMatch) {
        return header(HEADER_IF_NONE_MATCH, ifNoneMatch);
    }

    /**
     * Set the 'Content-Type' request header to the given value
     *
     * @param contentType
     * @return this request
     */
    public Request contentType(final String contentType) {
        return contentType(contentType, null);
    }

    /**
     * Set the 'Content-Type' request header to the given value and charset
     *
     * @param contentType
     * @param charset
     * @return this request
     */
    public Request contentType(final String contentType, final String charset) {
        if (charset != null && charset.length() > 0) {
            final String separator = "; " + PARAM_CHARSET + '=';
            return header(HEADER_CONTENT_TYPE, contentType + separator + charset);
        } else
            return header(HEADER_CONTENT_TYPE, contentType);
    }

    /**
     * Get the 'Content-Type' header from the response
     *
     * @return response header value
     */
    public String contentType() {
        return header(HEADER_CONTENT_TYPE);
    }

    /**
     * Get the 'Content-Length' header from the response
     *
     * @return response header value
     */
    public int contentLength() {
        return intHeader(HEADER_CONTENT_LENGTH);
    }

    /**
     * Set the 'Content-Length' request header to the given value
     *
     * @param contentLength
     * @return this request
     */
    public Request contentLength(final String contentLength) {
        return contentLength(Integer.parseInt(contentLength));
    }

    /**
     * Set the 'Content-Length' request header to the given value
     *
     * @param contentLength
     * @return this request
     */
    public Request contentLength(final int contentLength) {
        getConnection().setFixedLengthStreamingMode(contentLength);
        return this;
    }

    /**
     * Set the 'Accept' header to given value
     *
     * @param accept
     * @return this request
     */
    public Request accept(final String accept) {
        return header(HEADER_ACCEPT, accept);
    }

    /**
     * Set the 'Accept' header to 'application/json'
     *
     * @return this request
     */
    public Request acceptJson() {
        return accept(CONTENT_TYPE_JSON);
    }

    /**
     * Copy from input stream to output stream
     *
     * @param input
     * @param output
     * @return this request
     * @throws IOException
     */
    protected Request copy(final InputStream input, final OutputStream output)
            throws IOException {
        return new CloseOperation<Request>(input, ignoreCloseExceptions) {

            @Override
            public Request run() throws IOException {
                final byte[] buffer = new byte[bufferSize];
                int          read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    totalWritten += read;
                    progress.onUpload(totalWritten, totalSize);
                }
                return Request.this;
            }
        }.call();
    }

    /**
     * Copy from reader to writer
     *
     * @param input
     * @param output
     * @return this request
     * @throws IOException
     */
    protected Request copy(final Reader input, final Writer output)
            throws IOException {
        return new CloseOperation<Request>(input, ignoreCloseExceptions) {

            @Override
            public Request run() throws IOException {
                final char[] buffer = new char[bufferSize];
                int          read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                    totalWritten += read;
                    progress.onUpload(totalWritten, -1);
                }
                return Request.this;
            }
        }.call();
    }

    /**
     * Set the UploadProgress callback for this request
     *
     * @param callback
     * @return this request
     */
    public Request progress(final UploadProgress callback) {
        if (callback == null)
            progress = UploadProgress.DEFAULT;
        else
            progress = callback;
        return this;
    }

    private Request incrementTotalSize(final long size) {
        if (totalSize == -1)
            totalSize = 0;
        totalSize += size;
        return this;
    }

    /**
     * Close output stream
     *
     * @return this request
     * @throws RequestException
     * @throws IOException
     */
    protected Request closeOutput() throws IOException {
        progress(null);
        if (output == null)
            return this;
        if (multipart)
            output.write(CRLF + "--" + DEFAULT_BOUNDARY + "--" + CRLF);
        if (ignoreCloseExceptions)
            try {
                output.close();
            } catch (IOException ignored) {
                // Ignored
            }
        else
            output.close();
        output = null;
        return this;
    }

    /**
     * Call {@link #closeOutput()} and re-throw a caught {@link IOException}s as
     * an {@link RequestException}
     *
     * @return this request
     * @throws RequestException
     */
    protected Request closeOutputQuietly() throws RequestException {
        try {
            return closeOutput();
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }

    /**
     * Open output stream
     *
     * @return this request
     * @throws IOException
     */
    protected Request openOutput() throws IOException {
        if (output != null)
            return this;
        getConnection().setDoOutput(true);
        final String charset = getParam(
                getConnection().getRequestProperty(HEADER_CONTENT_TYPE), PARAM_CHARSET);
        output = new RequestOutputStream(getConnection().getOutputStream(), charset,
                bufferSize);
        return this;
    }

    /**
     * Start part of a multipart
     *
     * @return this request
     * @throws IOException
     */
    protected Request startPart() throws IOException {
        if (!multipart) {
            multipart = true;
            contentType(DEFAULT_CONTENT_TYPE_MULTIPART).openOutput();
            output.write("--" + DEFAULT_BOUNDARY + CRLF);
        } else
            output.write(CRLF + "--" + DEFAULT_BOUNDARY + CRLF);
        return this;
    }

    /**
     * Write part header
     *
     * @param name
     * @param filename
     * @return this request
     * @throws IOException
     */
    protected Request writePartHeader(final String name, final String filename)
            throws IOException {
        return writePartHeader(name, filename, null);
    }

    /**
     * Write part header
     *
     * @param name
     * @param filename
     * @param contentType
     * @return this request
     * @throws IOException
     */
    protected Request writePartHeader(final String name,
                                      final String filename, final String contentType) throws IOException {
        final StringBuilder partBuffer = new StringBuilder();
        partBuffer.append("form-data; name=\"").append(name);
        if (filename != null)
            partBuffer.append("\"; filename=\"").append(filename);
        partBuffer.append('"');
        partHeader("Content-Disposition", partBuffer.toString());
        if (contentType != null)
            partHeader(HEADER_CONTENT_TYPE, contentType);
        return send(CRLF);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     */
    public Request part(final String name, final String part) {
        return part(name, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param part
     * @return this request
     * @throws RequestException
     */
    public Request part(final String name, final String filename,
                        final String part) throws RequestException {
        return part(name, filename, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param contentType value of the Content-Type part header
     * @param part
     * @return this request
     * @throws RequestException
     */
    public Request part(final String name, final String filename,
                        final String contentType, final String part) throws RequestException {
        try {
            startPart();
            writePartHeader(name, filename, contentType);
            output.write(part);
        } catch (IOException e) {
            throw new RequestException(e);
        }
        return this;
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     * @throws RequestException
     */
    public Request part(final String name, final Number part)
            throws RequestException {
        return part(name, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param part
     * @return this request
     * @throws RequestException
     */
    public Request part(final String name, final String filename,
                        final Number part) throws RequestException {
        return part(name, filename, part != null ? part.toString() : null);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     * @throws RequestException
     */
    public Request part(final String name, final File part)
            throws RequestException {
        return part(name, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param part
     * @return this request
     * @throws RequestException
     */
    public Request part(final String name, final String filename,
                        final File part) throws RequestException {
        return part(name, filename, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param contentType value of the Content-Type part header
     * @param part
     * @return this request
     * @throws RequestException
     */
    public Request part(final String name, final String filename,
                        final String contentType, final File part) throws RequestException {
        final InputStream stream;
        try {
            stream = new BufferedInputStream(new FileInputStream(part));
            incrementTotalSize(part.length());
        } catch (IOException e) {
            throw new RequestException(e);
        }
        return part(name, filename, contentType, stream);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param part
     * @return this request
     * @throws RequestException
     */
    public Request part(final String name, final InputStream part)
            throws RequestException {
        return part(name, null, null, part);
    }

    /**
     * Write part of a multipart request to the request body
     *
     * @param name
     * @param filename
     * @param contentType value of the Content-Type part header
     * @param part
     * @return this request
     * @throws RequestException
     */
    public Request part(final String name, final String filename,
                        final String contentType, final InputStream part)
            throws RequestException {
        try {
            startPart();
            writePartHeader(name, filename, contentType);
            copy(part, output);
        } catch (IOException e) {
            throw new RequestException(e);
        }
        return this;
    }

    /**
     * Write a multipart header to the response body
     *
     * @param name
     * @param value
     * @return this request
     * @throws RequestException
     */
    public Request partHeader(final String name, final String value)
            throws RequestException {
        return send(name).send(": ").send(value).send(CRLF);
    }

    /**
     * Write contents of file to request body
     *
     * @param input
     * @return this request
     * @throws RequestException
     */
    public Request send(final File input) throws RequestException {
        final InputStream stream;
        try {
            stream = new BufferedInputStream(new FileInputStream(input));
            incrementTotalSize(input.length());
        } catch (FileNotFoundException e) {
            throw new RequestException(e);
        }
        return send(stream);
    }

    /**
     * Write byte array to request body
     *
     * @param input
     * @return this request
     * @throws RequestException
     */
    public Request send(final byte[] input) throws RequestException {
        if (input != null)
            incrementTotalSize(input.length);
        return send(new ByteArrayInputStream(input));
    }

    /**
     * Write stream to request body
     * <p>
     * The given stream will be closed once sending completes
     *
     * @param input
     * @return this request
     * @throws RequestException
     */
    public Request send(final InputStream input) throws RequestException {
        try {
            openOutput();
            copy(input, output);
        } catch (IOException e) {
            throw new RequestException(e);
        }
        return this;
    }

    /**
     * Write reader to request body
     * <p>
     * The given reader will be closed once sending completes
     *
     * @param input
     * @return this request
     * @throws RequestException
     */
    public Request send(final Reader input) throws RequestException {
        try {
            openOutput();
        } catch (IOException e) {
            throw new RequestException(e);
        }
        final Writer writer = new OutputStreamWriter(output,
                output.getEncoder().charset());
        return new FlushOperation<Request>(writer) {

            @Override
            protected Request run() throws IOException {
                return copy(input, writer);
            }
        }.call();
    }

    /**
     * Write char sequence to request body
     * <p>
     * The charset configured via {@link #contentType(String)} will be used and
     * UTF-8 will be used if it is unset.
     *
     * @param value
     * @return this request
     * @throws RequestException
     */
    public Request send(final CharSequence value) throws RequestException {
        try {
            openOutput();
            output.write(value.toString());
        } catch (IOException e) {
            throw new RequestException(e);
        }
        return this;
    }

    /**
     * Create writer to request output stream
     *
     * @return writer
     * @throws RequestException
     */
    public OutputStreamWriter writer() throws RequestException {
        try {
            openOutput();
            return new OutputStreamWriter(output, output.getEncoder().charset());
        } catch (IOException e) {
            throw new RequestException(e);
        }
    }

    /**
     * Write the values in the map as form data to the request body
     * <p>
     * The pairs specified will be URL-encoded in UTF-8 and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param values
     * @return this request
     * @throws RequestException
     */
    public Request form(final Map<?, ?> values) throws RequestException {
        return form(values, CHARSET_UTF8);
    }

    /**
     * Write the key and value in the entry as form data to the request body
     * <p>
     * The pair specified will be URL-encoded in UTF-8 and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param entry
     * @return this request
     * @throws RequestException
     */
    public Request form(final Entry<?, ?> entry) throws RequestException {
        return form(entry, CHARSET_UTF8);
    }

    /**
     * Write the key and value in the entry as form data to the request body
     * <p>
     * The pair specified will be URL-encoded and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param entry
     * @param charset
     * @return this request
     * @throws RequestException
     */
    public Request form(final Entry<?, ?> entry, final String charset)
            throws RequestException {
        return form(entry.getKey(), entry.getValue(), charset);
    }

    /**
     * Write the name/value pair as form data to the request body
     * <p>
     * The pair specified will be URL-encoded in UTF-8 and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param name
     * @param value
     * @return this request
     * @throws RequestException
     */
    public Request form(final Object name, final Object value)
            throws RequestException {
        return form(name, value, CHARSET_UTF8);
    }

    /**
     * Write the name/value pair as form data to the request body
     * <p>
     * The values specified will be URL-encoded and sent with the
     * 'application/x-www-form-urlencoded' content-type
     *
     * @param name
     * @param value
     * @param charset
     * @return this request
     * @throws RequestException
     */
    public Request form(final Object name, final Object value, String charset)
            throws RequestException {
        final boolean first = !form;
        if (first) {
            contentType(CONTENT_TYPE_FORM, charset);
            form = true;
        }
        charset = getValidCharset(charset);
        try {
            openOutput();
            if (!first)
                output.write('&');
            output.write(URLEncoder.encode(name.toString(), charset));
            output.write('=');
            if (value != null)
                output.write(URLEncoder.encode(value.toString(), charset));
        } catch (IOException e) {
            throw new RequestException(e);
        }
        return this;
    }

    /**
     * Write the values in the map as encoded form data to the request body
     *
     * @param values
     * @param charset
     * @return this request
     * @throws RequestException
     */
    public Request form(final Map<?, ?> values, final String charset)
            throws RequestException {
        if (!values.isEmpty())
            for (Entry<?, ?> entry : values.entrySet())
                form(entry, charset);
        return this;
    }

    /**
     * Configure HTTPS connection to trust all certificates
     * <p>
     * This method does nothing if the current request is not a HTTPS request
     *
     * @return this request
     * @throws RequestException
     */
    public Request trustAllCerts() throws RequestException {
        final HttpURLConnection connection = getConnection();
        if (connection instanceof HttpsURLConnection)
            ((HttpsURLConnection) connection)
                    .setSSLSocketFactory(getTrustedFactory());
        return this;
    }

    /**
     * Configure HTTPS connection to trust all hosts using a custom
     * {@link HostnameVerifier} that always returns <code>true</code> for each
     * host verified
     * <p>
     * This method does nothing if the current request is not a HTTPS request
     *
     * @return this request
     */
    public Request trustAllHosts() {
        final HttpURLConnection connection = getConnection();
        if (connection instanceof HttpsURLConnection)
            ((HttpsURLConnection) connection)
                    .setHostnameVerifier(getTrustedVerifier());
        return this;
    }

    /**
     * Get the {@link URL} of this request's connection
     *
     * @return request URL
     */
    public URL url() {
        return getConnection().getURL();
    }

    /**
     * Get the HTTP method of this request
     *
     * @return method
     */
    public String method() {
        return getConnection().getRequestMethod();
    }

    /**
     * Configure an HTTP proxy on this connection. Use {{@link #proxyBasic(String, String)} if
     * this proxy requires basic authentication.
     *
     * @param proxyHost
     * @param proxyPort
     * @return this request
     */
    public Request useProxy(final String proxyHost, final int proxyPort) {
        if (connection != null)
            throw new IllegalStateException("The connection has already been created. This method must be called before reading or writing to the request.");

        this.proxy = new Proxy(HTTP, new InetSocketAddress(proxyHost, proxyPort));
        return this;
    }

    public Request useProxy(Proxy proxy) {
        if (connection != null)
            throw new IllegalStateException("The connection has already been created. This method must be called before reading or writing to the request.");

        this.proxy = proxy;
        return this;
    }

    /**
     * Set whether or not the underlying connection should follow redirects in
     * the response.
     *
     * @param followRedirects - true fo follow redirects, false to not.
     * @return this request
     */
    public Request followRedirects(final boolean followRedirects) {
        getConnection().setInstanceFollowRedirects(followRedirects);
        return this;
    }

}
