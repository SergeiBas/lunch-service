package com.lunch.order.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CheckoutForm {

    // обов'язкові лише для гостя (перевіряється в контролері)
    private String guestName;
    private String guestPhone;
    private String guestEmail;

    @NotBlank(message = "Вкажіть адресу доставки")
    @Size(max = 255, message = "Адреса занадто довга")
    private String deliveryAddress;

    @Size(max = 500, message = "Коментар занадто довгий")
    private String comment;

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    public String getGuestPhone() { return guestPhone; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}