package com.example.apitestapp.models;

import java.util.Date;

public class ClientMachine {

    private String id;
    private String userId;
    private String machineName;
    private String os;
    private String ipAddress;
    private String hostname;

    private Boolean isActive = true;

    private Date createdAt;

    public ClientMachine() {
    }

    public ClientMachine(String id,
                         String userId,
                         String machineName,
                         String os,
                         String ipAddress,
                         String hostname,
                         Boolean isActive,
                         Date createdAt) {
        this.id = id;
        this.userId = userId;
        this.machineName = machineName;
        this.os = os;
        this.ipAddress = ipAddress;
        this.hostname = hostname;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public static class Builder {
        private String id;
        private String userId;
        private String machineName;
        private String os;
        private String ipAddress;
        private String hostname;
        private Boolean isActive = true;
        private Date createdAt;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder machineName(String machineName) {
            this.machineName = machineName;
            return this;
        }

        public Builder os(String os) {
            this.os = os;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder createdAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ClientMachine build() {
            return new ClientMachine(id, userId, machineName, os, ipAddress, hostname, isActive, createdAt);
        }
    }
}
