package org.xjy.android.nova.common.net;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@SuppressWarnings("deprecation")
public class HttpClientFactory {
	private static final String TAG = "HttpClientFactory";
	private static final String PARAMS_ORIGINAL_HOST = "originalHost";
	private static DefaultHttpClient singletonHttpClient;

	public static DefaultHttpClient createSingletonHttpClient() {
		if (singletonHttpClient == null) {
			singletonHttpClient = createProxyHttpClient();
		}
		return singletonHttpClient;
	}

	public static DefaultHttpClient createProxyHttpClient() {
		try {
			final DefaultHttpClient client = createPlaintHttpClient();
			client.setRoutePlanner(new HttpRoutePlanner() {
				@Override
				public HttpRoute determineRoute(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
					boolean isSecure = "https".equalsIgnoreCase(target.getSchemeName());
					return new HttpRoute(target, null, isSecure);
				}
			});
			return client;
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultHttpClient();
		}
	}

	public static DefaultHttpClient createPlaintHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			PlainSSLSocketFactory socketFactory = new PlainSSLSocketFactory(trustStore);
			socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			BasicHttpParams params = new BasicHttpParams();
//            HttpConnectionParams.setStaleCheckingEnabled(params, false);
			ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(20));
			ConnManagerParams.setMaxTotalConnections(params, 40);
			ConnManagerParams.setTimeout(params, 30000);
			HttpConnectionParams.setConnectionTimeout(params, 30000);
			HttpConnectionParams.setSoTimeout(params, 30000);
			HttpConnectionParams.setSocketBufferSize(params, 8192);
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", socketFactory, 443));
			ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(params, registry);
			params.setParameter(CoreProtocolPNames.USER_AGENT, "nova");
			HttpClientParams.setCookiePolicy(params, CookiePolicy.BROWSER_COMPATIBILITY);
			final DefaultHttpClient client = new DefaultHttpClient(ccm, params);
			client.setHttpRequestRetryHandler(new HttpRequestRetryHandler() {//尽管内部已经使用的就是默认的，但有些机器还是反馈，所以这样加下代码
				@Override
				public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
					if (!new DefaultHttpRequestRetryHandler().retryRequest(exception, executionCount, context)) {
						if (exception instanceof NoHttpResponseException && executionCount < 3) {
							return true;
						} else {
							return false;
						}
					} else {
						return true;
					}
				}
			});
			return client;
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultHttpClient();
		}
	}

	private static class PlainSSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public PlainSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super(truststore);
			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			injectHostname(socket, host);
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}

		private void injectHostname(Socket socket, String host) {
			try {
				Field field = InetAddress.class.getDeclaredField("hostName");
				field.setAccessible(true);
				field.set(socket.getInetAddress(), host);
			} catch (Exception ignored) {
			}
		}
	}

	public static HttpParams makeTimeoutHttpParams(int connectionTimeout, int readTimeout) {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
		HttpConnectionParams.setSoTimeout(params, readTimeout);
		return params;
	}
}