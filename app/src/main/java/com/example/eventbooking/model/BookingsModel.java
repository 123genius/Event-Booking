package com.example.eventbooking.model;

public class BookingsModel {

    private String customer_name, review, admin_id, rating, user_id, customer_number, event_date, event_status, event_time, hall_id, hall_name, menus, payment_status, total_bill, total_guest;

    public BookingsModel() {
    }

    public BookingsModel(String customer_name, String rating, String reivew, String admin_id, String user_id, String customer_number, String event_date, String event_status, String event_time, String hall_id, String hall_name, String menus, String payment_status, String total_bill, String total_guest) {
        this.customer_name = customer_name;
        this.customer_number = customer_number;
        this.event_date = event_date;
        this.rating = rating;
        this.review = reivew;
        this.event_status = event_status;
        this.event_time = event_time;
        this.hall_id = hall_id;
        this.user_id = user_id;
        this.admin_id = admin_id;
        this.hall_name = hall_name;
        this.menus = menus;
        this.payment_status = payment_status;
        this.total_bill = total_bill;
        this.total_guest = total_guest;
    }

    public String getReview() {
        return review;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getAdmin_id() {
        return admin_id;
    }

    public String getRating() {
        return rating;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public String getCustomer_number() {
        return customer_number;
    }

    public String getEvent_date() {
        return event_date;
    }

    public String getEvent_status() {
        return event_status;
    }

    public String getEvent_time() {
        return event_time;
    }

    public String getHall_id() {
        return hall_id;
    }

    public String getHall_name() {
        return hall_name;
    }

    public String getMenus() {
        return menus;
    }

    public String getPayment_status() {
        return payment_status;
    }

    public String getTotal_bill() {
        return total_bill;
    }

    public String getTotal_guest() {
        return total_guest;
    }
}
