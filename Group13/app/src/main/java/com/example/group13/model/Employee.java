package com.example.group13.model;

public class Employee {

    private String id;
    private String employeeName;
    private String position;
    private String phone;
    private String email;

    private String employeeId;
    private String dateOfBirth;
    private String gender;
    private String department;
    private String startDate;

    private boolean profileCompleted;

    public Employee() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmployeeName() { return employeeName; }
    public String getPosition() { return position; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    public String getEmployeeId() { return employeeId; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getDepartment() { return department; }
    public String getStartDate() { return startDate; }

    public boolean isProfileCompleted() { return profileCompleted; }
}