package com.example.group13.model;

import java.util.ArrayList;
import java.util.List;

public class Salary {

    private String id;
    private String employeeId;
    private String employeeName;
    private String position;

    private long baseSalary;
    private int workHours;

    private List<SalaryItem> items = new ArrayList<>();
    private long total;

    public Salary() {}

    public Salary(String employeeId, String employeeName, String position,
                  long baseSalary, int workHours, List<SalaryItem> items) {

        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.position = position;
        this.baseSalary = baseSalary;
        this.workHours = workHours;
        this.items = items;
        calculateTotal();
    }

    public void calculateTotal() {
        long sum = baseSalary * workHours;
        if (items != null) {
            for (SalaryItem item : items) {
                sum += item.getAmount();
            }
        }
        this.total = sum;
    }

    public List<SalaryItem> getItems() {
        return items;
    }

    public void setItems(List<SalaryItem> items) {
        this.items = items;
        calculateTotal();
    }

    public long getTotal() {
        return total;
    }

    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getPosition() { return position; }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public long getBaseSalary() { return baseSalary; }
    public int getWorkHours() { return workHours; }
}