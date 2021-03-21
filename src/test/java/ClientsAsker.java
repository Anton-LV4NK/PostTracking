import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.net.URI;

public class ClientsAsker {
    private final int httpPort;
    private final CloseableHttpClient httpclient;

    public ClientsAsker(int httpHort){
        this.httpPort = httpHort;
        this.httpclient = HttpClients.createDefault();
    }

    public CloseableHttpResponse clientAsk(JSONObject body, String endPoint) throws Exception {
        URIBuilder builder = new URIBuilder()
                .setScheme("http")
                .setHost("localhost")
                .setPort(httpPort)
                .setPath(endPoint);

        URI uri = builder.build();

        HttpPost httpPost = new HttpPost(uri);
        httpPost.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));
        return httpclient.execute(httpPost);
    }

    public void close() throws Exception {
        httpclient.close();
    }
}
