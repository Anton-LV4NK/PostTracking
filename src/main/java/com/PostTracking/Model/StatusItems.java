package com.PostTracking.Model;

import org.json.JSONObject;

public class StatusItems {
    private Long m_id;
    private long m_idPost;
    private long m_idItem;
    private long m_statusItem;
    private long m_transferDate;
    private long m_identifier;

    public StatusItems(){}

    public Long getId() {
        return m_id;
    }

    public void setId(long m_id) {
        this.m_id = m_id;
    }

    public long getIdPost() {
        return m_idPost;
    }

    public void setIdPost(long m_idPost) {
        this.m_idPost = m_idPost;
    }

    public long getIdItem() {
        return m_idItem;
    }

    public void setIdItem(long m_idItem) {
        this.m_idItem = m_idItem;
    }

    public long getStatusItem() {
        return m_statusItem;
    }

    public void setStatusItem(long m_statusItem) {
        this.m_statusItem = m_statusItem;
    }

    public long getTransferDate() {
        return m_transferDate;
    }

    public void setTransferDate(long m_transferDate) {
        this.m_transferDate = m_transferDate;
    }

    public long getIdentifier() {
        return m_identifier;
    }

    public void setIdentifier(long m_identifier) {
        this.m_identifier = m_identifier;
    }

    public JSONObject toJSON() {
        JSONObject res = new JSONObject();
        res.put("idPost", m_idPost);
        res.put("idItem", m_idItem);
        res.put("statusItem", m_statusItem);
        res.put("transferDate", m_transferDate);
        return res;
    }
}
