package com.lunch.order.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CloseGroupForm {

    @NotBlank(message = "Вкажіть адресу доставки")
    @Size(max = 255, message = "Адреса занадто довга")
    private String deliveryAddress;

    @Size(max = 500, message = "Коментар занадто довгий")
    private String comment;

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}