package com.PostTracking.Web;

import com.PostTracking.Model.PostOffice;
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
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;

public class PostHandler extends CommonHandlersTools implements HttpAsyncRequestHandler<HttpRequest> {
    private final Logger m_logger;
    private final SessionFactory m_sessionFactory;
    public PostHandler(SessionFactory sessionFactory){
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
            JSONObject returnedJSON = new JSONObject();
            if (method.equals("register_post")) {
                JSONObject params = new JSONObject(body);
                normalizeJSON(params);
                typeFieldValidatorJSON(params, "index", new Class[]{Integer.class, Long.class});
                typeFieldValidatorJSON(params, "name", new Class[]{String.class});
                typeFieldValidatorJSON(params, "address", new Class[]{String.class});
                t = session.beginTransaction();
                // object duplicate check
                List<PostOffice> savedPostOffices = getElementsEqualsToValueFromDB(session, PostOffice.class, "index", params.getLong("index"));
                if (savedPostOffices.size() < 1) {
                    //save object
                    PostOffice postOffice = new PostOffice();
                    postOffice.setIndex(params.getLong("index"));
                    postOffice.setName(params.getString("name"));
                    postOffice.setAddress(params.getString("address"));
                    session.save(postOffice);
                    returnedJSON = postOffice.toJSON();
                } else {
                    m_logger.info("duplicated object post office");
                }
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
}
