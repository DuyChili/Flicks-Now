package com.example.flicks_now.model;

public class LichSuThanhToan {
    private String idThanhToan;
    private String idUser;
    private String noiDung;
    private String ngayXacNhan;
    private String ngayThanhToan;
    private long soTien;
    private String ngayHetHan;

    public LichSuThanhToan() {

    }

    public LichSuThanhToan(String idThanhToan, String idUser, String noiDung,String ngayThanhToan, String ngayXacNhan, long soTien, String ngayHetHan) {
        this.idThanhToan = idThanhToan;
        this.idUser = idUser;
        this.noiDung = noiDung;
        this.ngayThanhToan = ngayThanhToan;
        this.ngayXacNhan = ngayXacNhan;
        this.soTien = soTien;
        this.ngayHetHan = ngayHetHan;
    }

    public String getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(String ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public String getIdThanhToan() {
        return idThanhToan;
    }

    public void setIdThanhToan(String idThanhToan) {
        this.idThanhToan = idThanhToan;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getNgayXacNhan() {
        return ngayXacNhan;
    }

    public void setNgayXacNhan(String ngayXacNhan) {
        this.ngayXacNhan = ngayXacNhan;
    }

    public long getSoTien() {
        return soTien;
    }

    public void setSoTien(long soTien) {
        this.soTien = soTien;
    }

    public String getNgayHetHan() {
        return ngayHetHan;
    }

    public void setNgayHetHan(String ngayHetHan) {
        this.ngayHetHan = ngayHetHan;
    }
}
