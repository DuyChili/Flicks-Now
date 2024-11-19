package com.example.flicks_now.model;

public class LichSuCapQuyen {
    private String id_LSThayDoiQuyen;
    private String id_userCanUpdate;
    private String id_userYeuCau;
    private String ngayUpdater;
    private Long id_loaiNDUpdate;
    private Long id_loaiNDCu;
    public int idTrangThai;

    public LichSuCapQuyen() {

    }

    public String getId_LSThayDoiQuyen() {
        return id_LSThayDoiQuyen;
    }

    public void setId_LSThayDoiQuyen(String id_LSThayDoiQuyen) {
        this.id_LSThayDoiQuyen = id_LSThayDoiQuyen;
    }

    public String getId_userCanUpdate() {
        return id_userCanUpdate;
    }

    public void setId_userCanUpdate(String id_userCanUpdate) {
        this.id_userCanUpdate = id_userCanUpdate;
    }

    public String getId_userYeuCau() {
        return id_userYeuCau;
    }

    public void setId_userYeuCau(String id_userYeuCau) {
        this.id_userYeuCau = id_userYeuCau;
    }

    public String getNgayUpdater() {
        return ngayUpdater;
    }

    public void setNgayUpdater(String ngayUpdater) {
        this.ngayUpdater = ngayUpdater;
    }

    public Long getId_loaiNDUpdate() {
        return id_loaiNDUpdate;
    }

    public void setId_loaiNDUpdate(Long id_loaiNDUpdate) {
        this.id_loaiNDUpdate = id_loaiNDUpdate;
    }

    public Long getId_loaiNDCu() {
        return id_loaiNDCu;
    }

    public void setId_loaiNDCu(Long id_loaiNDCu) {
        this.id_loaiNDCu = id_loaiNDCu;
    }

    public int getIdTrangThai() {
        return idTrangThai;
    }

    public void setIdTrangThai(int idTrangThai) {
        this.idTrangThai = idTrangThai;
    }

    public LichSuCapQuyen(String id_LSThayDoiQuyen, String id_userCanUpdate, String id_userYeuCau, String ngayUpdater, Long id_loaiNDUpdate, Long id_loaiNDCu, int idTrangThai) {
        this.id_LSThayDoiQuyen = id_LSThayDoiQuyen;
        this.id_userCanUpdate = id_userCanUpdate;
        this.id_userYeuCau = id_userYeuCau;
        this.ngayUpdater = ngayUpdater;
        this.id_loaiNDUpdate = id_loaiNDUpdate;
        this.id_loaiNDCu = id_loaiNDCu;
        this.idTrangThai = idTrangThai;
    }
}
