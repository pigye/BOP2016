package API;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

public class HttpUtil {
	public static CloseableHttpClient[] clients;
	public static int index;
	
	static {
		index = 0;
		clients = new CloseableHttpClient[20];
		for (int i = 0;i < clients.length;i ++) clients[i] = init();
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					for (CloseableHttpClient httpClient : clients) {
						httpClient.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}));
	}
	
	public static synchronized CloseableHttpClient getClient() {
		CloseableHttpClient client = clients[index ++];
		if (index >= clients.length) {
			index = 0;
		}
		return client;
	}
	
	public static CloseableHttpClient init() {
		ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
		LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSocketFactory();
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
              .register("http", plainsf)
              .register("https", sslsf)
              .build();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
		// 将最大连接数增加到200
		cm.setMaxTotal(200);
		// 将每个路由基础的连接增加到20
		cm.setDefaultMaxPerRoute(20);

		// 将目标主机的最大连接数增加到50
		HttpHost localhost = new HttpHost(Evaluate.EVALUATE_URI);
		cm.setMaxPerRoute(new HttpRoute(localhost), 50);
		
		//请求重试处理
		HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
	        public boolean retryRequest(IOException exception,int executionCount, HttpContext context) {
	            if (executionCount >= 5) {// 如果已经重试了5次，就放弃	                
	                return false;
	            }
				if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试					
					return true;
				}
				if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常					
					return false;
				}	            
	            if (exception instanceof InterruptedIOException) {// 超时	                
	                return false;
	            }
	            if (exception instanceof UnknownHostException) {// 目标服务器不可达	                
	                return false;
	            }
	            if (exception instanceof ConnectTimeoutException) {// 连接被拒绝	                
	                return false;
	            }
	            if (exception instanceof SSLException) {// ssl握手异常	                
	                return false;
	            }
	            
	            HttpClientContext clientContext = HttpClientContext.adapt(context);
	            HttpRequest request = clientContext.getRequest();
	            // 如果请求是幂等的，就再次尝试
	            if (!(request instanceof HttpEntityEnclosingRequest)) {	                
	                return true;
	            }
	            return false;
	        }
	    };  
		return HttpClients.custom()
				.setConnectionManager(cm)
				.setRetryHandler(httpRequestRetryHandler)
				.build();
	}
}
