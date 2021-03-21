package com.PostTracking.Model;

import org.json.JSONObject;

public class Items {
    private Long m_id;
    private long m_identifier;
    private long m_type;
    private long m_indexSender;
    private long m_indexReceiver;
    private String m_addressRecipient;
    private String m_nameRecipient;

    public Items(){}

    public Long getId() {
        return m_id;
    }

    public void setId(long m_id) {
        this.m_id = m_id;
    }

    public long getIdentifier() {
        return m_identifier;
    }

    public void setIdentifier(long m_identifier) {
        this.m_identifier = m_identifier;
    }

    public long getType() {
        return m_type;
    }

    public void setType(long m_type) {
        this.m_type = m_type;
    }

    public long getIndexSender() {
        return m_indexSender;
    }

    public void setIndexSender(long m_indexSender) {
        this.m_indexSender = m_indexSender;
    }

    public long getIndexReceiver() {
        return m_indexReceiver;
    }

    public void setIndexReceiver(long m_indexReceiver) {
        this.m_indexReceiver = m_indexReceiver;
    }

    public String getAddressRecipient() {
        return m_addressRecipient;
    }

    public void setAddressRecipient(String m_addressRecipient) {
        this.m_addressRecipient = m_addressRecipient;
    }

    public String getNameRecipient() {
        return m_nameRecipient;
    }

    public void setNameRecipient(String m_nameRecipient) {
        this.m_nameRecipient = m_nameRecipient;
    }

    public JSONObject toJSON() {
        JSONObject res = new JSONObject();
        res.put("id", m_id == null ? JSONObject.NULL : m_id);
        res.put("identifier", m_identifier);
        res.put("type", m_type);
        res.put("indexSender", m_indexSender);
        res.put("indexReceiver", m_indexReceiver);
        res.put("addressRecipient", m_addressRecipient);
        res.put("nameRecipient", m_nameRecipient);
        return res;
    }
}
