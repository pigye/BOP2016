import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;;

public class JavaSample {
	static HttpClient httpclient = HttpClients.createDefault();
    public static void main(String[] args) {
    	JavaSample sample = new JavaSample();
    	for (int i = 0;i < 1;i ++) {
    		sample.test();
    	}
    }
    
    public void test() {
    	long st = System.currentTimeMillis();
    	try{
            URIBuilder builder = new URIBuilder("https://oxfordhk.azure-api.net/academic/v1.0/evaluate");
            //2125771191 2037858832 2066636486
            //[2094437628, 2273736245, 165450437, 2273736245]
            String expr = "Id=1871565202";
            System.out.println(expr.length());
            builder.setParameter("expr", expr);
            //builder.setParameter("expr", "RId=2066636486");
            builder.setParameter("model", "latest");
            builder.setParameter("count", "100");
            builder.setParameter("offset", "0");
            builder.setParameter("orderby", "CC:desc");
            builder.setParameter("attributes", "Id,RId,AA.AuId,AA.AfId,C.CId,F.FId,J.JId");
            builder.addParameter("subscription-key", "f7cc29509a8443c5b3a5e56b0e38b5a6");

            URI uri = builder.build();
            HttpGet request = new HttpGet(uri);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null)  {
            	//ObjectMapper mapper = new ObjectMapper();
            	String result = EntityUtils.toString(entity);
            	System.out.println(result);
            	//JsonNode node = mapper.readTree(result);
                //System.out.println(node.size());
                //System.out.println(node.toString());
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println((System.currentTimeMillis() - st) / 1000.0);
    }
}
