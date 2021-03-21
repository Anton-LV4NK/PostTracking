import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class Tests {
    private static Environment env;
    @BeforeClass
    public static void setEnv() {
        try {
            env = new Environment();
        } catch (Exception e) {
            System.exit(-1);
        }
    }
    @AfterClass
    public static void closeEnv() {
        try {
            env.close();
        } catch (Exception e) {
            System.exit(-1);
        }
    }

    @Test
    public void check() throws Exception {
        ClientsAsker clientsAsker = new ClientsAsker(env.httpPort);
        JSONObject clientPostBody = new JSONObject();

        //testing /post/register_post/
        long index = 12345;
        String name = "Rostov";
        String address = "Rostov, Rostov street";
        clientPostBody.put("index", index);
        clientPostBody.put("name", name);
        clientPostBody.put("address", address);
        CloseableHttpResponse httpResponse = clientsAsker.clientAsk(clientPostBody, "/post/register_post/");
        Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), 200);
        HttpEntity entity =  httpResponse.getEntity();
        if (entity != null) {
            JSONObject checkObj = new JSONObject(EntityUtils.toString(entity));
            Assert.assertEquals(index, checkObj.getLong("index"));
            Assert.assertEquals(name, checkObj.getString("name"));
            Assert.assertEquals(address, checkObj.getString("address"));
        }
        httpResponse.close();

        //testing /items/arrival/
        clientPostBody = new JSONObject();
        clientPostBody.put("postIndex", index);
        clientPostBody.put("type",1);
        clientPostBody.put("indexSender",1);
        clientPostBody.put("indexReceiver",1);
        clientPostBody.put("addressRecipient", address);
        clientPostBody.put("nameRecipient", name);
        clientPostBody.put("identifier", 1);
        httpResponse = clientsAsker.clientAsk(clientPostBody, "/items/arrival/");
        Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), 200);
        entity =  httpResponse.getEntity();
        if (entity != null) {
            JSONObject checkObj = new JSONObject(EntityUtils.toString(entity));
            Assert.assertFalse(checkObj.isNull("id"));
        }
        httpResponse.close();

        //testing /items/history/
        clientPostBody = new JSONObject();
        clientPostBody.put("identifier", 1);
        httpResponse = clientsAsker.clientAsk(clientPostBody, "/items/history/");
        Assert.assertEquals(httpResponse.getStatusLine().getStatusCode(), 200);
        entity =  httpResponse.getEntity();
        if (entity != null) {
            JSONObject checkObj = new JSONObject(EntityUtils.toString(entity));
            Assert.assertEquals(1, checkObj.getJSONArray("history").length());
        }
        httpResponse.close();
        clientsAsker.close();
    }
}
