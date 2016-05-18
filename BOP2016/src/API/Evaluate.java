package API;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
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
import org.apache.http.util.EntityUtils;

import util.Timer;

public class Evaluate  {
	public static final String KEY = "f7cc29509a8443c5b3a5e56b0e38b5a6";
	public static final int COUNT_INT = 8000;
	public static final String COUNT_STR = COUNT_INT + "";
	public static final String EVALUATE_URI = "https://oxfordhk.azure-api.net/academic/v1.0/evaluate";

	private URI getURI(String expr, int count, int offset, String attrs) {
		URI uri = null;
        try {
        	URIBuilder builder = new URIBuilder(EVALUATE_URI);
            builder.setParameter("expr", expr);
            builder.setParameter("model", "latest");
            builder.setParameter("count", "" + count);
            builder.setParameter("offset", "" + offset);
            builder.setParameter("attributes", attrs);
            builder.addParameter("subscription-key", KEY);
			uri = builder.build();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        return uri;
	}
	
	public String evaluate(String expr, int count, int offset, String attrs) {
		String result = null;
		URI uri = getURI(expr, count, offset, attrs);
        HttpGet request = new HttpGet(uri);
        request.setHeader("Connection", "keep-alive");
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = HttpUtil.getClient();
		try {
			response = httpClient.execute(request);
			HttpEntity entity = response.getEntity();
	        if (entity != null) {
	            result = EntityUtils.toString(entity);
	        }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Timer.log(" evaluate time =");
		return result;
	}
	
	public String evaluate(String expr, int offset, String attrs) {
		return evaluate(expr, COUNT_INT, offset, attrs);
	}
	
    public static void main(String[] args) {
    	Timer.init();
    	System.out.println(new Evaluate().evaluate("Id=1983578042", 0, "Id,AA.AuId,F.FId,J.JId,C.CId,RId"));
    }
}


