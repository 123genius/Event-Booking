package com.example.eventbooking.model;

import java.io.Serializable;

public class RequestModel implements Serializable {

    private String customer_name, customer_number, hall_name, date, guest, hall_id, menu_bill, menus, time, total_bill;

    public RequestModel() {
    }

    public RequestModel(String customer_name, String hall_name, String customer_number, String date, String guest, String hall_id, String menu_bill, String menus, String time, String total_bill) {
        this.customer_name = customer_name;
        this.customer_number = customer_number;
        this.date = date;
        this.hall_name = hall_name;
        this.guest = guest;
        this.hall_id = hall_id;
        this.menu_bill = menu_bill;
        this.menus = menus;
        this.time = time;
        this.total_bill = total_bill;
    }

    public String getHall_name() {
        return hall_name;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public String getCustomer_number() {
        return customer_number;
    }

    public String getDate() {
        return date;
    }

    public String getGuest() {
        return guest;
    }

    public String getHall_id() {
        return hall_id;
    }

    public String getMenu_bill() {
        return menu_bill;
    }

    public String getMenus() {
        return menus;
    }

    public String getTime() {
        return time;
    }

    public String getTotal_bill() {
        return total_bill;
    }
}
