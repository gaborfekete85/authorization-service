package com.crimelist.crime.security.oauth2.user;

import java.util.Map;

public class CustomOauth2UserInfo extends OAuth2UserInfo {

    private String id;
    private String name;
    private String email;
    private String imageUrl;

    public CustomOauth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getId() {
        return attributes.get("id").toString();
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return attributes.get("name").toString();
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getImageUrl() {
        return attributes.get("imageUrl").toString();
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
