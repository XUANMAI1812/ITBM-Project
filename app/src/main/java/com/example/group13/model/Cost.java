package com.example.group13.model;

public class Cost {

    public static final int PENDING = 0;
    public static final int APPROVED = 1;
    private String id; // documentId
    private String name;
    private long amount;
    private String projectId;
    private int status;

    public Cost() {
    }

    public Cost(String name, long amount, String projectId, int status) {
        this.name = name;
        this.amount = amount;
        this.projectId = projectId;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public long getAmount() {
        return amount;
    }

    public String getProjectId() {
        return projectId;
    }

    public int getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}