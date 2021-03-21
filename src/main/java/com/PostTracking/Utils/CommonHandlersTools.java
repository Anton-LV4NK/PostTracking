package com.PostTracking.Utils;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.protocol.BasicAsyncResponseProducer;
import org.apache.http.nio.protocol.HttpAsyncExchange;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.JSONObject;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class CommonHandlersTools {

    public void checkingHTTPMethod(HttpRequest httpRequest) throws  IllegalArgumentException {
        if (!httpRequest.getRequestLine().getMethod().equals("POST")) {
            throw new IllegalArgumentException("must be POST request");
        }
    }
    //response to client request
    public void submitResponse(HttpAsyncExchange httpAsyncExchange, JSONObject responseJSON, int statusHTTP) {
        HttpResponse response = httpAsyncExchange.getResponse();
        response.setStatusCode(statusHTTP);
        response.setEntity(new NStringEntity(responseJSON.toString(), ContentType.APPLICATION_JSON));
        httpAsyncExchange.submitResponse(new BasicAsyncResponseProducer(response));
    }
    public String getEndPointMethod(String uri, Logger m_logger) throws IllegalArgumentException {
        final String[] parts = uri.split("/");
        if (parts.length != 3) {
            throw new IllegalArgumentException("invalid end point or uri value");
        }
        m_logger.info(String.format("URI: %1$s, parts size: %2$d", uri, parts.length));
        return parts[parts.length - 1];
    }
    //delete all null elements in JSON object
    public void normalizeJSON(JSONObject obj)
    {
        Set<String> keys = obj.keySet().stream().collect(Collectors.toSet());
        for (String key : keys)
        {
            if (obj.isNull(key))
            {
                obj.remove(key);
            }
        }
    }
    //data type validator in JSON object
    public void typeFieldValidatorJSON(JSONObject obj, String name, Class[] targetClasses) throws IllegalArgumentException {
        if (!obj.has(name)) {
            throw new IllegalArgumentException("Missed field " + name);
        }
        Object val = obj.get(name);
        if (Arrays.stream(targetClasses).filter(cls -> cls.isInstance(val)).count() < 1) {
            throw new IllegalArgumentException("Field " + name + " is of unexpected type");
        }
    }
    public void rollbackTransaction(Transaction t) {
        if (t != null) {
            t.rollback();
        }
    }
    //select from db to List on field
    public <T, V> List<T> getElementsEqualsToValueFromDB(Session session, Class<T> tClass, String nameColumn, V v) {
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(tClass);
        Root<T> root = criteriaQuery.from(tClass);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get(nameColumn), v));
        return session.createQuery(criteriaQuery).getResultList();
    }
}
