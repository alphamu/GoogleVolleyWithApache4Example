package com.alimuzaffar.newvolleyproject;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.HttpStack;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.ProtocolException;
import cz.msebera.android.httpclient.auth.AuthScope;
import cz.msebera.android.httpclient.auth.UsernamePasswordCredentials;
import cz.msebera.android.httpclient.client.CookieStore;
import cz.msebera.android.httpclient.client.CredentialsProvider;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.config.RequestConfig;
import cz.msebera.android.httpclient.client.methods.HttpDelete;
import cz.msebera.android.httpclient.client.methods.HttpEntityEnclosingRequestBase;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpPut;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.client.protocol.HttpClientContext;
import cz.msebera.android.httpclient.config.Registry;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.conn.HttpClientConnectionManager;
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.socket.PlainConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.ssl.SSLConnectionSocketFactory;
import cz.msebera.android.httpclient.conn.ssl.SSLContextBuilder;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.impl.client.BasicCookieStore;
import cz.msebera.android.httpclient.impl.client.BasicCredentialsProvider;
import cz.msebera.android.httpclient.impl.client.DefaultRedirectStrategy;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.message.BasicHeader;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.BasicHttpContext;
import cz.msebera.android.httpclient.protocol.HttpContext;

public class SslHttpStack implements HttpStack {

    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final int TIMEOUT = 30000;

    private final String TAG = getClass().getSimpleName();

    private HttpClient mHttpClient;
    private HttpContext mHttpContext;
    private CookieStore mCookieStore;
    private boolean mIsAllowSelfSigned = false;

    public HttpClient getHttpClient() {
        return mHttpClient;
    }

    /***
     *
     * @param allowSelfSigned This will accept all certificates, not recommended
     *                        for production.
     */
    public SslHttpStack(boolean allowSelfSigned) {
        mCookieStore = new BasicCookieStore();
        mIsAllowSelfSigned = allowSelfSigned;

        RequestConfig requestConfig =
                RequestConfig.custom().setConnectionRequestTimeout(TIMEOUT).setConnectTimeout(TIMEOUT)
                        .setSocketTimeout(TIMEOUT).setCircularRedirectsAllowed(true).build();

        HttpClientBuilder httpClientBuilder =
                HttpClientBuilder.create().setDefaultCookieStore(mCookieStore)
                        .setConnectionManager(new PoolingHttpClientConnectionManager())
                        .setDefaultRequestConfig(requestConfig)
                        .setRedirectStrategy(new DefaultRedirectStrategy() {
                            public boolean isRedirected(HttpRequest request, cz.msebera.android.httpclient.HttpResponse response, HttpContext context) {
                                boolean isRedirect = false;
                                try {
                                    isRedirect = super.isRedirected(request, response, context);
                                } catch (ProtocolException e) {
                                    e.printStackTrace();
                                }
                                if (!isRedirect) {
                                    int responseCode = response.getStatusLine().getStatusCode();
                                    if (responseCode == 301 || responseCode == 302) {
                                        return true;
                                    }
                                }
                                return isRedirect;
                            }
                        });

        try {
            SSLContext sslContext;

            if (mIsAllowSelfSigned) {
                sslContext = SSLContext.getInstance("SSL");
                // set up a TrustManager that trusts everything
                sslContext.init(null, new TrustManager[]{new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        Log.d(TAG, "getAcceptedIssuers =============");
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        Log.d(TAG, "checkClientTrusted =============");
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        Log.d(TAG, "checkServerTrusted =============");
                    }
                }}, new SecureRandom());
            } else {
                SSLContextBuilder builder = new SSLContextBuilder();
                sslContext = builder.useSSL().build();
            }

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

            Registry<ConnectionSocketFactory> socketFactoryRegistry =
                    RegistryBuilder.<ConnectionSocketFactory>create()
                            .register("https", sslsf)
                            .register("http", new PlainConnectionSocketFactory()).build();

            HttpClientConnectionManager httpConnMan =
                    new PoolingHttpClientConnectionManager(socketFactoryRegistry);

            //sslContext commented out because it shouldn't be needed since we pass it
            //in the socketFactoryRegistry as part of the SSLConnectionSocketFactory
            httpClientBuilder.setConnectionManager(httpConnMan);

            // This will only supply the username and password is the server
            // prompts for it. You'll have to create a singleton for your Application.
            // You can leave out this section.
            /*
            if (MyApplication.getAppContext().getResources().getInteger(R.integer.auth_required) == 1) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("Username", "Password"));
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }*/

        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        mHttpClient = httpClientBuilder.build();
        mHttpContext = new BasicHttpContext();

        mHttpContext.setAttribute(HttpClientContext.COOKIE_STORE, mCookieStore);
    }

    @SuppressWarnings("unused")
    private static void addHeaders(HttpUriRequest httpRequest, Map<String, String> headers) {
        for (String key : headers.keySet()) {
            httpRequest.setHeader(key, headers.get(key));
        }
    }

    @SuppressWarnings("unused")
    private static List<NameValuePair> getPostParameterPairs(Map<String, String> postParams) {
        List<NameValuePair> result = new ArrayList<NameValuePair>(postParams.size());
        for (String key : postParams.keySet()) {
            result.add(new BasicNameValuePair(key, postParams.get(key)));
        }
        return result;
    }

    @Override
    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders)
            throws IOException, AuthFailureError {

        HttpUriRequest httpRequest = createHttpRequest(request, additionalHeaders);

        HttpResponse resp = mHttpClient.execute(httpRequest);
        return resp;
    }


    /**
     * Creates the appropriate subclass of HttpUriRequest for passed in request.
     */
    @SuppressWarnings("deprecation")
  /* protected */ static HttpUriRequest createHttpRequest(Request<?> request,
                                                          Map<String, String> additionalHeaders) throws AuthFailureError {
        switch (request.getMethod()) {
            case Method.DEPRECATED_GET_OR_POST: {
                // This is the deprecated way that needs to be handled for backwards
                // compatibility.
                // If the request's post body is null, then the assumption is that
                // the request is
                // GET. Otherwise, it is assumed that the request is a POST.
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    HttpPost postRequest = new HttpPost(request.getUrl());
                    postRequest.addHeader(HEADER_CONTENT_TYPE, request.getPostBodyContentType());
                    HttpEntity entity;
                    entity = new ByteArrayEntity(postBody);
                    postRequest.setEntity(entity);
                    return postRequest;
                } else {
                    return new HttpGet(request.getUrl());
                }
            }
            case Method.GET:
                HttpGet getRequest = new HttpGet(request.getUrl());
                setHeaderFromRequest(getRequest, request);
                return getRequest;
            case Method.DELETE:
                HttpDelete delRequest = new HttpDelete(request.getUrl());
                setHeaderFromRequest(delRequest, request);
                return delRequest;
            case Method.POST: {
                HttpPost postRequest = new HttpPost(request.getUrl());
                postRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(postRequest, request);
                setHeaderFromRequest(postRequest, request);
                return postRequest;
            }
            case Method.PUT: {
                HttpPut putRequest = new HttpPut(request.getUrl());
                putRequest.addHeader(HEADER_CONTENT_TYPE, request.getBodyContentType());
                setEntityIfNonEmptyBody(putRequest, request);
                setHeaderFromRequest(putRequest, request);
                return putRequest;
            }
            default:
                throw new IllegalStateException("Unknown request method.");
        }
    }

    private static void setEntityIfNonEmptyBody(HttpEntityEnclosingRequestBase httpRequest,
                                                Request<?> request) throws AuthFailureError {
        byte[] body = request.getBody();
        if (body != null) {
            HttpEntity entity = new ByteArrayEntity(body);
            httpRequest.setEntity(entity);
        }
    }

    private static void setHeaderFromRequest(HttpRequestBase base, Request<?> request) throws AuthFailureError {
        Map<String, String> getHeaders = request.getHeaders();
        if (!getHeaders.isEmpty()) {
            for (Entry<String, String> entry : getHeaders.entrySet()) {
                base.addHeader(entry.getKey(), entry.getValue());
            }
        }
    }

}