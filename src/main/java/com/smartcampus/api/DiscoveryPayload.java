package com.smartcampus.api;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root discovery document (GET /api/v1) with basic HATEOAS-style navigation hints.
 */
public class DiscoveryPayload {

    private String name = "Smart Campus Sensor & Room Management API";
    private String apiVersion = "1.0.0";
    private String administrativeContact = "smart-campus-support@university.example";
    private Map<String, String> collections = new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getAdministrativeContact() {
        return administrativeContact;
    }

    public void setAdministrativeContact(String administrativeContact) {
        this.administrativeContact = administrativeContact;
    }

    public Map<String, String> getCollections() {
        return collections;
    }

    public void setCollections(Map<String, String> collections) {
        this.collections = collections;
    }
}
