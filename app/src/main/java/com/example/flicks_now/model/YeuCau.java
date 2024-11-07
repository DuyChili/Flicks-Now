package com.example.flicks_now.model;

public class YeuCau {
    public String idYeuCau;
    public String idUser;
    public String content;
    public int amount;
    public String  paymentDate;
    public int idTrangThai;

    public YeuCau(String idYeuCau, String idUser, String content, int amount, String  paymentDate, int idTrangThai) {
        this.idYeuCau = idYeuCau;
        this.idUser = idUser;
        this.content = content;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.idTrangThai = idTrangThai;
    }
    public YeuCau() {}

    public String getIdYeuCau() {
        return idYeuCau;
    }

    public void setIdYeuCau(String idYeuCau) {
        this.idYeuCau = idYeuCau;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public int getIdTrangThai() {
        return idTrangThai;
    }

    public void setIdTrangThai(int idTrangThai) {
        this.idTrangThai = idTrangThai;
    }
}
