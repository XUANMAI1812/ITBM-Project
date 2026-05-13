package com.example.group13.model;

public class SalaryItem {

    private String name;
    private long amount;

    public SalaryItem() {}

    public SalaryItem(String name, long amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}