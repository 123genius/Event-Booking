package com.example.eventbooking.model;

import java.io.Serializable;

public class HallModel implements Serializable {
    private String contact_number, rating, latitude, longitude, email, userImage, rent, status, name, imageUrl, maximum_capacity, mini_capacity, number_of_washrooms, parking, uid, user_name;

    public HallModel(String contact_number, String rating, String latitude, String longitude, String rent, String userImage, String status, String email, String name, String imageUrl, String maximum_capacity, String mini_capacity, String number_of_washrooms, String parking, String uid, String user_name) {
        this.contact_number = contact_number;
        this.email = email;
        this.name = name;
        this.status = status;
        this.rating = rating;
        this.rent = rent;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.userImage = userImage;
        this.maximum_capacity = maximum_capacity;
        this.mini_capacity = mini_capacity;
        this.number_of_washrooms = number_of_washrooms;
        this.parking = parking;
        this.uid = uid;
        this.user_name = user_name;
    }

    public HallModel() {
    }

    public String getRating() {
        return rating;
    }

    public String getRent() {
        return rent;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getStatus() {
        return status;
    }

    public String getContact_number() {
        return contact_number;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getMaximum_capacity() {
        return maximum_capacity;
    }

    public String getMini_capacity() {
        return mini_capacity;
    }

    public String getNumber_of_washrooms() {
        return number_of_washrooms;
    }

    public String getParking() {
        return parking;
    }

    public String getUid() {
        return uid;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUserImage() {
        return userImage;
    }
}
