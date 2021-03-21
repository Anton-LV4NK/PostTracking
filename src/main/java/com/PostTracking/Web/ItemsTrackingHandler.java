package com.PostTracking.Web;

import com.PostTracking.Model.Items;
import com.PostTracking.Model.PostOffice;
import com.PostTracking.Model.StatusItems;
import com.PostTracking.Utils.CommonHandlersTools;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.nio.protocol.*;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class ItemsTrackingHandler extends CommonHandlersTools implements HttpAsyncRequestHandler<HttpRequest>, StatusItemCode {
    private final Logger m_logger;
    private final SessionFactory m_sessionFactory;
    public ItemsTrackingHandler(SessionFactory sessionFactory) {
        super();
        m_logger =  LogManager.getLogger(ItemsTrackingHandler.class);
        m_sessionFactory = sessionFactory;
    }
    @Override
    public HttpAsyncRequestConsumer<HttpRequest> processRequest(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {
        return new BasicAsyncRequestConsumer();
    }

    @Override
    public void handle(HttpRequest httpRequest, HttpAsyncExchange httpAsyncExchange, HttpContext httpContext) throws HttpException, IOException {
        Session session = m_sessionFactory.openSession();
        Transaction t = null;
        try {
            checkingHTTPMethod(httpRequest);
            final String method = getEndPointMethod(httpRequest.getRequestLine().getUri(), m_logger);
            final String body = EntityUtils.toString(((HttpEntityEnclosingRequest) httpRequest).getEntity());
            JSONObject params = new JSONObject(body);
            normalizeJSON(params);
            JSONObject returnedJSON = new JSONObject();
            if (method.equals("arrival")) {
                typeFieldValidatorJSON(params, "type", new Class[]{Integer.class, Long.class});
                typeFieldValidatorJSON(params, "indexSender", new Class[]{Integer.class, Long.class});
                typeFieldValidatorJSON(params, "indexReceiver", new Class[]{Integer.class, Long.class});
                typeFieldValidatorJSON(params, "identifier", new Class[]{Integer.class, Long.class});
                typeFieldValidatorJSON(params, "addressRecipient", new Class[]{String.class});
                typeFieldValidatorJSON(params, "nameRecipient", new Class[]{String.class});
                typeFieldValidatorJSON(params, "postIndex", new Class[]{Integer.class, Long.class});

                long postIndex = params.getLong("postIndex");
                long identifier = params.getLong("identifier");
                t = session.beginTransaction();
                //check the existence of a post office
                List<PostOffice> savedPostOffices = getElementsEqualsToValueFromDB(session, PostOffice.class, "index", postIndex);
                if (savedPostOffices.size() != 1) {
                    m_logger.error("post office does not exist . post index:  " + postIndex);
                    throw new IllegalArgumentException("post office does not exist . post index:  " + postIndex);
                }
                List<Items> savedItemsList = getElementsEqualsToValueFromDB(session, Items.class, "identifier", identifier);
                boolean isItemExist = savedItemsList.size() == 1;
                if (!isItemExist) {
                    Items newItem = new Items();
                    newItem.setType(params.getLong("type"));
                    newItem.setIndexSender(params.getLong("indexSender"));
                    newItem.setIndexReceiver(params.getLong("indexReceiver"));
                    newItem.setIdentifier(identifier);
                    newItem.setAddressRecipient(params.getString("addressRecipient"));
                    newItem.setNameRecipient(params.getString("nameRecipient"));
                    session.save(newItem);
                    m_logger.info("save item. identifier item: " + identifier);
                    //need new item id from db
                    savedItemsList = getElementsEqualsToValueFromDB(session, Items.class, "identifier", identifier);
                }
                int statusItemCode = isItemExist ? StatusItemCode.ITEM_ARRIVAL : StatusItemCode.ITEM_REGISTER;
                returnedJSON = saveStatusItemAndGetJson(session, statusItemCode,postIndex, savedItemsList, identifier);
                t.commit();
                m_logger.info("save relocation item or register. identifier item: " + identifier);
            } else if (method.equals("departure") || method.equals("received")) {
                typeFieldValidatorJSON(params, "identifier", new Class[]{Integer.class, Long.class});
                typeFieldValidatorJSON(params, "postIndex", new Class[]{Integer.class, Long.class});
                long postIndex = params.getLong("postIndex");
                long identifier = params.getLong("identifier");
                t = session.beginTransaction();
                //check the existence of a post office
                List<PostOffice> savedPostOffices = getElementsEqualsToValueFromDB(session, PostOffice.class, "index", postIndex);
                if (savedPostOffices.size() != 1) {
                    m_logger.error("post office does not exist. post index:  " + postIndex);
                    throw new IllegalArgumentException("post office does not exist. post index: " + postIndex);
                }
                //check the existence of a item
                List<Items> savedItemsList = getElementsEqualsToValueFromDB(session, Items.class, "identifier", identifier);
                if (savedItemsList.size() != 1) {
                    m_logger.error("item does not exist. identifier item: " + identifier);
                    throw new IllegalArgumentException("item does not exist. identifier item: " + identifier);
                }
                int statusItemCode = method.equals("departure") ? StatusItemCode.ITEM_DEPARTURE : StatusItemCode.ITEM_RECEIVED;
                returnedJSON = saveStatusItemAndGetJson(session, statusItemCode,postIndex, savedItemsList, identifier);
                m_logger.info(String.format("save relocation item from method:  %1$s. identifier item:  %2$d", method, identifier));
                t.commit();
            } else if (method.equals("history")) {
                typeFieldValidatorJSON(params, "identifier", new Class[]{Integer.class, Long.class});
                long identifier = params.getLong("identifier");
                t = session.beginTransaction();
                //check the existence of a item
                List<Items> savedItemsList = getElementsEqualsToValueFromDB(session, Items.class, "identifier", identifier);
                if (savedItemsList.size() != 1) {
                    m_logger.error("item does not exist. identifier item: " + identifier);
                    throw new IllegalArgumentException("item does not exist. identifier item: " + identifier);
                }
                List<StatusItems> savedStatusItemsList = getElementsEqualsToValueFromDB(session, StatusItems.class, "identifier", identifier);
                JSONArray arr = new JSONArray();
                savedStatusItemsList.stream().forEach(e -> arr.put(e.toJSON()));
                returnedJSON.put("history", arr);
                m_logger.info("status_item method. history returned. identifier item: " + identifier);
                t.commit();
            } else {
                throw new IllegalArgumentException("invalid end point method");
            }
            submitResponse(httpAsyncExchange, returnedJSON, HttpStatus.SC_OK);
        } catch (IllegalArgumentException e) {
            rollbackTransaction(t);
            submitResponse(httpAsyncExchange, new JSONObject().put("error", e.toString()), HttpStatus.SC_BAD_REQUEST);
        } catch (Exception e) {
            rollbackTransaction(t);
            submitResponse(httpAsyncExchange, new JSONObject().put("error", e.toString()), HttpStatus.SC_INTERNAL_SERVER_ERROR);
        } finally {
            session.close();
        }
    }

    private JSONObject saveStatusItemAndGetJson(Session session, int statusItemCode, long postIndex, List<Items> savedItemsList, long identifier) {
        StatusItems statusItem = new StatusItems();
        statusItem.setIdItem(savedItemsList.get(0).getId());
        statusItem.setIdPost(postIndex);
        statusItem.setStatusItem(statusItemCode);
        statusItem.setIdentifier(identifier);
        statusItem.setTransferDate(Instant.now().getEpochSecond());
        session.save(statusItem);
        JSONObject savedItem = savedItemsList.get(0).toJSON();
        savedItem.put("statusCode", statusItemCode);

        return savedItem;
    }
}
