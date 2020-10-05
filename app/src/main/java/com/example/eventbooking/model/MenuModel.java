package com.example.eventbooking.model;

public class MenuModel {
    private String menu_name, menu_price, menu_image;

    public MenuModel(String menu_name, String menu_price, String menu_image) {
        this.menu_name = menu_name;
        this.menu_price = menu_price;
        this.menu_image = menu_image;
    }

    public MenuModel() {
    }

    public void setMenu_name(String menu_name) {
        this.menu_name = menu_name;
    }

    public void setMenu_price(String menu_price) {
        this.menu_price = menu_price;
    }

    public void setMenu_image(String menu_image) {
        this.menu_image = menu_image;
    }

    public String getMenu_name() {
        return menu_name;
    }

    public String getMenu_price() {
        return menu_price;
    }

    public String getMenu_image() {
        return menu_image;
    }
}