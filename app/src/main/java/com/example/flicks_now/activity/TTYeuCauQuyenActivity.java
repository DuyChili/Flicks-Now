package com.example.flicks_now.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flicks_now.adapter.TTYeuCauQuyenAdapter;
import com.example.flicks_now.databinding.ActivityTtyeuCauQuyenBinding;
import com.example.flicks_now.databinding.DialogTtYeucauDetailBinding;
import com.example.flicks_now.model.LichSuCapQuyen;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TTYeuCauQuyenActivity extends AppCompatActivity {
    private ActivityTtyeuCauQuyenBinding binding;
    private TTYeuCauQuyenAdapter adapter;
    private List<LichSuCapQuyen> yeuCauList;
    private DatabaseReference databaseReference;
    private DatabaseReference usersRef, loaiNDRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTtyeuCauQuyenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        loaiNDRef = FirebaseDatabase.getInstance().getReference("LoaiND");

        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Lịch sử thay đổi quyền");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về

        }

        // Khởi tạo danh sách và adapter
        yeuCauList = new ArrayList<>();
        adapter = new TTYeuCauQuyenAdapter(this, yeuCauList);
        // xử lý nhấn vào item
        adapter.setOnItemClickListener(yeuCau -> chiTietYeuCau(yeuCau));
        // Thiết lập RecyclerView
        binding.ttYeuCauRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.ttYeuCauRecyclerView.setAdapter(adapter);

        // Khởi tạo FirebaseDatabase và tham chiếu đến nhánh chứa yêu cầu
        databaseReference = FirebaseDatabase.getInstance().getReference("LichSuCapQuyen");

        // Đọc dữ liệu từ Firebase và cập nhật vào danh sách
        fetchYeuCauData();
    }

    private void fetchYeuCauData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                yeuCauList.clear(); // Xóa danh sách cũ để cập nhật dữ liệu mới
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    LichSuCapQuyen yeuCau = dataSnapshot.getValue(LichSuCapQuyen.class);
                    if (yeuCau != null) {
                        yeuCauList.add(yeuCau);
                    }
                }
                adapter.notifyDataSetChanged(); // Cập nhật lại adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TTYeuCauQuyenActivity.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chiTietYeuCau(LichSuCapQuyen yeuCau) {
        DialogTtYeucauDetailBinding dialogBinding = DialogTtYeucauDetailBinding.inflate(LayoutInflater.from(this));
        // Biến lưu tên của người yêu cầu và người bị thay đổi
        final String[] tenNguoiYeuCau = {null};
        final String[] tenNguoiBiThayDoi = {null};
        final String[] loaiNguoiDungCu = {"Unknown"};
        final String[] loaiNguoiDungMoi = {"Unknown"};
        // Lấy tham chiếu tới bảng Users



        // Truy vấn thông tin người dùng từ bảng Users theo id_user trong yêu cầu
        usersRef.orderByChild("id_user").equalTo(yeuCau.getId_userYeuCau()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        tenNguoiYeuCau[0] = userSnapshot.child("name").getValue(String.class);
                        dialogBinding.tvUserRequest.setText(tenNguoiYeuCau[0] != null ? tenNguoiYeuCau[0] : "Unknown User");
                    }
                } else {
                    dialogBinding.tvUserRequest.setText("Unknown User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogBinding.tvUserRequest.setText("Error Loading User");
            }
        });

        // Lấy tên người bị thay đổi và loại người dùng cũ
        usersRef.orderByChild("id_user").equalTo(yeuCau.getId_userCanUpdate()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        tenNguoiBiThayDoi[0] = userSnapshot.child("name").getValue(String.class);
                        dialogBinding.tvUserNeedUpdate.setText(tenNguoiBiThayDoi[0] != null ? tenNguoiBiThayDoi[0] : "Unknown User");

                        // Lấy id_loaiND cũ từ người dùng
                        Long idLoaiNDCu = yeuCau.getId_loaiNDCu();
                        if (idLoaiNDCu != null) {
                            // Truy vấn bảng LoaiND để lấy tên loại người dùng cũ
                            loaiNDRef.child(String.valueOf(idLoaiNDCu)).child("type").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot loaiSnapshot) {
                                    loaiNguoiDungCu[0] = loaiSnapshot.getValue(String.class);
                                    updateRequestText(dialogBinding, tenNguoiYeuCau, tenNguoiBiThayDoi, loaiNguoiDungCu, loaiNguoiDungMoi);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(TTYeuCauQuyenActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(TTYeuCauQuyenActivity.this, "Không tìm thấy user", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    dialogBinding.tvUserNeedUpdate.setText("Unknown User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dialogBinding.tvUserNeedUpdate.setText("Error Loading User");
            }
        });

        // Truy vấn LoaiND mới để lấy tên loại người dùng mới
        loaiNDRef.child(String.valueOf(yeuCau.getId_loaiNDUpdate())).child("type").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot loaiSnapshot) {
                loaiNguoiDungMoi[0] = loaiSnapshot.getValue(String.class);
                updateRequestText(dialogBinding, tenNguoiYeuCau, tenNguoiBiThayDoi, loaiNguoiDungCu, loaiNguoiDungMoi);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TTYeuCauQuyenActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });

        dialogBinding.tvUpdateDate.setText(yeuCau.getNgayUpdater());

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();

        Log.d("Kiểm tra id loại người dùng Update", String.valueOf(yeuCau.getId_loaiNDUpdate()));

        if(yeuCau.idTrangThai == 0){
            dialogBinding.btnConfirm.setText("Xác nhận"); // Đặt text mặc định
            DatabaseReference TTYeuCauRef = FirebaseDatabase.getInstance().getReference("LichSuCapQuyen")
                    .child(yeuCau.getId_LSThayDoiQuyen());

            TTYeuCauRef.child("idTrangThai").setValue(1).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                }
            });

            dialogBinding.btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
            });
        }else{

            dialogBinding.btnConfirm.setText("Quay lại"); // Đặt text thành "Quay lại"
            dialogBinding.btnConfirm.setOnClickListener(v -> dialog.dismiss()); // Đóng dialog khi nhấn
        }
    }

    // Hàm cập nhật tvRequest
    private void updateRequestText(DialogTtYeucauDetailBinding dialogBinding, String[] tenNguoiYeuCau, String[] tenNguoiBiThayDoi, String[] loaiNguoiDungCu, String[] loaiNguoiDungMoi) {
        if (tenNguoiYeuCau[0] != null && tenNguoiBiThayDoi[0] != null) {
            String requestText = tenNguoiYeuCau[0] + " đã thay đổi loại người dùng của "
                    + tenNguoiBiThayDoi[0] + " từ "
                    + loaiNguoiDungCu[0] + " sang " + loaiNguoiDungMoi[0];
            dialogBinding.tvNoiDungYeuCau.setText(requestText);
        }
    }
    // Hàm xử lý xác nhận thay đổi loại người dùng
    private void xacNhanThayDoiLoaiNguoiDung(String idUserCanUpdate, Long idLoaiNDUpdate, DialogTtYeucauDetailBinding dialogBinding) {
        Log.d("Kiểm tra id loại người dùng Update", String.valueOf(idLoaiNDUpdate));
        usersRef.orderByChild("id_user").equalTo(idUserCanUpdate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        userSnapshot.getRef().child("id_loaiND").setValue(idLoaiNDUpdate).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(TTYeuCauQuyenActivity.this, "Cập nhật loại người dùng thành công", Toast.LENGTH_SHORT).show();

                            } else {
                                Toast.makeText(TTYeuCauQuyenActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> dialogBinding.tvNoiDungYeuCau.setText("Lỗi: " + e.getMessage()));
                    }
                } else {
                    Toast.makeText(TTYeuCauQuyenActivity.this, "Không tìm thấy người dùng với id_user: ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                dialogBinding.tvNoiDungYeuCau.setText("Lỗi: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, QLUserActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}