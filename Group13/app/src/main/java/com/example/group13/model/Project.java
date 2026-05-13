package com.example.group13.model;

import java.util.List;

public class Project {

    private transient String firebaseKey;

    private String projectId; // ID nghiệp vụ (ID002)
    private String name;

    private String managerId;
    private List<String> memberIds;

    private String description;
    private double cost;
    private String status;
    private String startDate;
    private String endDate;
    private String documentUrl;

    public Project() {}

    public Project(String projectId,
                   String name,
                   String managerId,
                   String description,
                   List<String> memberIds,
                   double cost,
                   String status,
                   String startDate,
                   String endDate) {

        this.projectId = projectId;
        this.name = name;
        this.managerId = managerId;
        this.description = description;
        this.memberIds = memberIds;
        this.cost = cost;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // ===== GETTER =====
    public String getFirebaseKey() { return firebaseKey; } // 🔥
    public String getProjectId() { return projectId; }
    public String getName() { return name; }
    public String getManagerId() { return managerId; }
    public List<String> getMemberIds() { return memberIds; }
    public String getDescription() { return description; }
    public double getCost() { return cost; }
    public String getStatus() { return status; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getDocumentUrl() { return documentUrl; }

    // ===== SETTER =====
    public void setFirebaseKey(String firebaseKey) { this.firebaseKey = firebaseKey; } // 🔥
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
    public void setProjectId(String projectId) { this.projectId = projectId; }
}