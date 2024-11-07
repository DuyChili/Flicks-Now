package com.example.flicks_now.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flicks_now.adapter.UserAdapter;
import com.example.flicks_now.model.User;
import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityQluserBinding;
import com.example.flicks_now.databinding.DialogUserInfoBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class QLUserActivity extends AppCompatActivity {
    private ActivityQluserBinding binding;
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference yeuCauRef;
    private DatabaseReference usersRef;
    private DatabaseReference loaiNguoiDungRef;
    private HashMap<Long, String> loaiMap;
    private ArrayList<String> danhSachGoi;
    private boolean isLoaiNguoiDungLoaded = false;
    // danh sách người dùng góc
    private List<User> originalUserList;
    private ValueEventListener userValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQluserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo các list ngay từ đầu
        userList = new ArrayList<>();
        originalUserList = new ArrayList<>();

        // Khởi tạo Firebase reference
        yeuCauRef = FirebaseDatabase.getInstance().getReference("YeuCau");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        loaiNguoiDungRef = FirebaseDatabase.getInstance().getReference("LoaiND");

        loaiMap = new HashMap<>();
        // Cài đặt RecyclerView

        hienThiRecyclerView();
        SearchEditText();
        xulyTimKiemSpiner();


        // Xử lý sự kiện lọc theo spinner
        binding.spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedStatus = parent.getItemAtPosition(position).toString();
                String searchQuery = binding.searchEditText.getText().toString();
                //filterUsersByStatus(selectedStatus);
                filterUsers(searchQuery, selectedStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không có hành động gì khi không có lựa chọn
            }
        });
        loadLoaiNguoiDung();
        laySoLuongYeuCauHomNay();
        demSoLuongUserVip();
        demSoLuongUserThuong();

        // Thiết lập sự kiện click cho từng item
        userAdapter.setRecyclerViewItemClickListener(new UserAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d("kiểm tra load người dùng", String.valueOf(isLoaiNguoiDungLoaded));
                if (isLoaiNguoiDungLoaded) {
                    User UserChitiet = userList.get(position);
                    digLogChiTietUser(UserChitiet);
                } else {
                    Toast.makeText(QLUserActivity.this, "Đang tải dữ liệu, vui lòng chờ...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Lấy giá trị mặc định từ Spinner và lọc dữ liệu ngay từ đầu
        String defaultStatus = binding.spinnerStatus.getSelectedItem().toString();
        filterUsersByStatus(defaultStatus);
        binding.btnYeuCau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QLUserActivity.this, QLYeuCauActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loadDuLieu();
    }
    private void digLogChiTietUser(User clickedUser){
        DialogUserInfoBinding dialogBinding = DialogUserInfoBinding.inflate(LayoutInflater.from(this));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(QLUserActivity.this, android.R.layout.simple_spinner_item, danhSachGoi);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogBinding.spinnerUserType.setAdapter(adapter);


        // Hiển thị thông tin người dùng
        dialogBinding.tvUserNameDialog.setText(clickedUser.getName());
        dialogBinding.tvIdUserDialog.setText(clickedUser.getId_user());
        dialogBinding.tvEmailDialog.setText(clickedUser.getEmail());
        dialogBinding.tvTrangThaiDialog.setText(clickedUser.getStatus());
        dialogBinding.tvCreateDialog.setText(convertTimestampToDate(clickedUser.getCreated_at()));

        // Thiết lập dữ liệu cho Spinner từ loaiMap

        // Đặt giá trị mặc định cho Spinner dựa trên id_loaiND của user
        Long idLoaiND = clickedUser.getId_loaiND();
        int position = getSpinnerPosition(idLoaiND);
        Log.d("digLogChiTietUser", "Selected position: " + position + " for idLoaiND: " + idLoaiND);
        dialogBinding.spinnerUserType.setSelection(getSpinnerPosition(idLoaiND));


        // Xử lý sự kiện khi nhấn nút "Xác nhận"
        dialogBinding.btnXacNhan.setOnClickListener(v -> {
            int selectedPosition1 = dialogBinding.spinnerUserType.getSelectedItemPosition();
            Long newUserTypeId = (Long) loaiMap.keySet().toArray()[selectedPosition1];

            // Sử dụng firebaseKey để trỏ tới bản ghi người dùng chính xác
            DatabaseReference userRef = usersRef.child(clickedUser.getFirebaseKey()); // clickedUser là User đã chọn

            userRef.child("id_loaiND").setValue(newUserTypeId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
                    loadDuLieu();
                } else {
                    Toast.makeText(this, "Lỗi khi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        });
        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
    private void hienThiRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        binding.recyclerView.setAdapter(userAdapter);
    }

    private void laySoLuongYeuCauHomNay() {
        DatabaseReference yeuCauRef = FirebaseDatabase.getInstance().getReference("YeuCau");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String ngayHienTai = sdf.format(new Date());

        Query query = yeuCauRef.orderByChild("paymentDate").startAt(ngayHienTai).endAt(ngayHienTai + "\uf8ff");


        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int soLuong = (int) dataSnapshot.getChildrenCount();

                Log.d("laySoLuongYeuCauHomNay", "Số lượng yêu cầu hôm nay: " + soLuong);
                binding.tvSLUserDangKy.setText(String.valueOf(soLuong));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QLUserActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });

        yeuCauRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int countChuaXuLy = 0;
                int countDaXuLy = 0;

                for (DataSnapshot yeuCauSnapshot : dataSnapshot.getChildren()) {
                    String paymentDate = yeuCauSnapshot.child("paymentDate").getValue(String.class);
                    Integer idTrangThai = yeuCauSnapshot.child("idTrangThai").getValue(Integer.class);

                    // Kiểm tra nếu paymentDate trùng với ngày hiện tại
                    if (paymentDate != null && paymentDate.startsWith(ngayHienTai)) {
                        if (idTrangThai != null) {
                            if (idTrangThai == 0) {
                                countChuaXuLy++;
                            } else if (idTrangThai == 1) {
                                countDaXuLy++;
                            }
                        }
                    }
                }

                // Hiển thị số lượng lên TextView
                binding.tvSLUserChuaXL.setText(String.valueOf(countChuaXuLy));
                binding.tvSLUserDaXL.setText(String.valueOf(countDaXuLy));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QLUserActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void demSoLuongUserVip() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int countVipUsers = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Long idLoaiND = userSnapshot.child("id_loaiND").getValue(Long.class);
                    if (idLoaiND != null && idLoaiND == 1) {
                        countVipUsers++;
                    }
                }
                binding.tvSLGoiVipDangKy.setText(String.valueOf(countVipUsers));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QLUserActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void demSoLuongUserThuong() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int countRegularUsers = 0;
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    Long idLoaiND = userSnapshot.child("id_loaiND").getValue(Long.class);
                    if (idLoaiND != null && idLoaiND == 0) {
                        countRegularUsers++;
                    }
                }
                binding.tvSLGoiThuongDangKy.setText(String.valueOf(countRegularUsers));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QLUserActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm để chuyển đổi timestamp sang định dạng dd/MM/yyyy
    private String convertTimestampToDate(Long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void loadDuLieu() {
        // Hiển thị ProgressBar khi bắt đầu tải dữ liệu
        binding.progressBar.setVisibility(View.VISIBLE);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                originalUserList = new ArrayList<>();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String id_user = userSnapshot.child("id_user").getValue(String.class);
                    String name = userSnapshot.child("name").getValue(String.class);
                    String status = userSnapshot.child("status").getValue(String.class);
                    Long idLoaiND = userSnapshot.child("id_loaiND").getValue(Long.class);
                    Long createdAt = userSnapshot.child("created_at").getValue(Long.class);
                    String email = userSnapshot.child("email").getValue(String.class);
                    String firebaseKey = userSnapshot.getKey();
                    // Lấy `type` từ `loaiMap` dựa trên `idLoaiND`, nếu không có thì gán là "Thường"
                    String goi = loaiMap.getOrDefault(idLoaiND, "Loading...");

                    Log.d("kiểm tra gói", "gói ở ql user: " + goi);
                    if (status == null) status = "offline";
                    User user = new User(firebaseKey, id_user, name, status, goi, idLoaiND, createdAt, email);
                    userList.add(user);
                    originalUserList.add(user);

                }

                // Áp dụng bộ lọc ngay khi dữ liệu đã load
                String searchQuery = binding.searchEditText.getText().toString();
                String selectedStatus = binding.spinnerStatus.getSelectedItem().toString();
                //filterUsersByStatus(selectedStatus);
                filterUsers(searchQuery, selectedStatus);
                // Thông báo cho adapter rằng dữ liệu đã thay đổi
                userAdapter.notifyDataSetChanged();
                // Ẩn ProgressBar sau khi dữ liệu đã tải xong
                binding.progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(QLUserActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void loadLoaiNguoiDung() {
        danhSachGoi = new ArrayList<>();
        loaiNguoiDungRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loaiMap.clear();
                for (DataSnapshot loaiSnapshot : snapshot.getChildren()) {
                    Long id = loaiSnapshot.child("id").getValue(Long.class);
                    String type = loaiSnapshot.child("type").getValue(String.class);
                    if (id != null && type != null) {
                        danhSachGoi.add(type);
                        loaiMap.put(id, type);
                    }
                }
                isLoaiNguoiDungLoaded = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QLUserActivity.this, "Lỗi khi tải dữ liệu LoaiND", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getSpinnerPosition(Long idLoaiND) {
        String type = loaiMap.get(idLoaiND); // Lấy `type` từ `loaiMap` dựa trên `id_loaiND`
        if (type != null) {
            int position = danhSachGoi.indexOf(type);
            return position; // Trả về vị trí của `type` trong `danhSachGoi`
        }
        return 0; // Trả về 0 (mặc định) nếu không tìm thấy
    }
    private void filterUsersByStatus(String status) {
        List<User> filteredList = new ArrayList<>();

        if (status.equals("All")) {
            filteredList.addAll(userList);
        } else {
            for (User user : userList) {
                if (user.getStatus().equalsIgnoreCase(status)) {
                    filteredList.add(user);
                }
            }
        }

        userAdapter.updateData(filteredList);
    }
    private void xulyTimKiemSpiner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(adapter);
    }

    private void SearchEditText() {
        binding.searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (originalUserList != null) { // Thêm kiểm tra null
                    filterUsers(s.toString(), binding.spinnerStatus.getSelectedItem().toString());
                }
            }
        });
    }
    private void filterUsers(String searchQuery, String statusFilter) {
        if (originalUserList == null) return; // Thêm kiểm tra null

        List<User> filteredList = new ArrayList<>();
        searchQuery = searchQuery.toLowerCase().trim();

        for (User user : originalUserList) {
            boolean matchesSearch = searchQuery.isEmpty() ||
                    user.getName().toLowerCase().contains(searchQuery) ||
                    user.getEmail().toLowerCase().contains(searchQuery) ||
                    user.getId_user().toLowerCase().contains(searchQuery);

            boolean matchesStatus = statusFilter.equals("All") ||
                    user.getStatus().equalsIgnoreCase(statusFilter);

            if (matchesSearch && matchesStatus) {
                filteredList.add(user);
            }
        }

        userAdapter.updateData(filteredList);
    }

}