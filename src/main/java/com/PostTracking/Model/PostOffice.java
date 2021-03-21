package com.PostTracking.Model;

import org.json.JSONObject;

public class PostOffice {
    private long m_id;
    private long m_index;
    private String m_name;
    private String m_address;

    public PostOffice(){}

    public long getId() {
        return m_id;
    }

    public void setId(long m_id) {
        this.m_id = m_id;
    }

    public long getIndex() {
        return m_index;
    }

    public void setIndex(long m_index) {
        this.m_index = m_index;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String m_name) {
        this.m_name = m_name;
    }

    public String getAddress() {
        return m_address;
    }

    public void setAddress(String m_address) {
        this.m_address = m_address;
    }

    public JSONObject toJSON() {
        JSONObject res = new JSONObject();
        res.put("index", m_index);
        res.put("name", m_name);
        res.put("address", m_address);
        return res;
    }
}
