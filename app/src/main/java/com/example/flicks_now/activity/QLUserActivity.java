package com.example.flicks_now.activity;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;


import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flicks_now.R;
import com.example.flicks_now.adapter.UserAdapter;
import com.example.flicks_now.databinding.ActivityQluserBinding;
import com.example.flicks_now.databinding.DialogTtYeucauBinding;
import com.example.flicks_now.databinding.DialogUserInfoBinding;
import com.example.flicks_now.model.LichSuCapQuyen;
import com.example.flicks_now.model.User;
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

    // danh sách người dùng góc
    private List<User> originalUserList;
    private boolean doubleBackToExitPressedOnce = false;
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;
    private long id_LoaiNDCu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQluserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        laythongtinUser();
        demSLLichSuChuaDoc();
        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);
        // Khởi tạo các list ngay từ đầu
        userList = new ArrayList<>();
        originalUserList = new ArrayList<>();
        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý User"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về

        }
        Log.d("Kiểm tra loại người dùng có phải là Admin không",String.valueOf(idLoaiND));
        if(idLoaiND ==2){
            binding.iconThongBao.setVisibility(View.VISIBLE);
        }else{
            binding.iconThongBao.setVisibility(View.GONE);
        }
        // Xử lý sự kiện khi nhấn vào icon thông báo
        binding.iconThongBao.setOnClickListener(v -> {
            // Thực hiện hành động khi nhấn vào biểu tượng thông báo
            Toast.makeText(this, "Thông báo được nhấn!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, TTYeuCauQuyenActivity.class);
            startActivity(intent);  // Chuyển đến một Activity khác khi nhấn
        });


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
        kiemTraTaiKhoanVaChiTietUser();

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

    }
    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser  = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", -1);
        Log.d("id_loaiND Ban đầu", String.valueOf(idLoaiND));
    }
    private void kiemTraTaiKhoanVaChiTietUser(){
        userAdapter.setRecyclerViewItemClickListener(new UserAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                User UserChitiet = userList.get(position);
                Log.d("kiểm tra id loại người dùng",String.valueOf(idLoaiND));
                if (idLoaiND != 2 && UserChitiet.getId_loaiND() == 2 ){
                    // không thể xem thông tin của admin
                    Toast.makeText(QLUserActivity.this, "Bạn không thể xem thông tin của tài khoản này", Toast.LENGTH_SHORT).show();
                }else{
                    digLogChiTietUser(UserChitiet);
                }


            }
        });

    }
    private void digLogChiTietUser(User clickedUser){
        DialogUserInfoBinding dialogBinding = DialogUserInfoBinding.inflate(LayoutInflater.from(this));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBinding.getRoot());
        AlertDialog dialog = builder.create();
        dialog.show();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(QLUserActivity.this, android.R.layout.simple_spinner_item, new ArrayList<>(loaiMap.values()));
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
        id_LoaiNDCu = clickedUser.getId_loaiND();
        int position = getSpinnerPosition(id_LoaiNDCu);
        Log.d("digLogChiTietUser", "Selected position: " + position + " for idLoaiND: " + id_LoaiNDCu);
        dialogBinding.spinnerUserType.setSelection(getSpinnerPosition(id_LoaiNDCu));


        // Xử lý sự kiện khi nhấn nút "Xác nhận"
        dialogBinding.btnXacNhan.setOnClickListener(v -> {
            int selectedPosition1 = dialogBinding.spinnerUserType.getSelectedItemPosition();
            Long newUserTypeId = (Long) loaiMap.keySet().toArray()[selectedPosition1];

            // Sử dụng firebaseKey để trỏ tới bản ghi người dùng chính xác
            DatabaseReference userRef = usersRef.child(clickedUser.getFirebaseKey()); // clickedUser là User đã chọn
            if(idLoaiND == 2){
                Log.d("kiểm tra id loại người dùng 1: ",String.valueOf(idLoaiND));
                if(clickedUser.getId_loaiND() == 2){
                    Toast.makeText(this, "Bạn không được cập nhập loại người dùng này", Toast.LENGTH_SHORT).show();
                }else{
                    if(newUserTypeId == 2){
                        Toast.makeText(this, "Bạn không được cập nhập loại người dùng này", Toast.LENGTH_SHORT).show();
                    }else{
                        userRef.child("id_loaiND").setValue(newUserTypeId).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                loadDuLieu();
                                hienThiThongBaoXacNhan(userRef,newUserTypeId,clickedUser.getId_user(), idUser,newUserTypeId,dialog);
                            } else {
                                Toast.makeText(this, "Lỗi khi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                            }

                        });
                    }

                }


            }else{
                Log.d("kiểm tra id loại người dùng 2: ",String.valueOf(idLoaiND));
                if(clickedUser.getId_loaiND() == 2 && clickedUser.getId_loaiND() == 3){
                    if(newUserTypeId == 2){
                        Toast.makeText(this, "Bạn không thể cập nhập loại người dùng này", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        // lưu lại vào lịch sử thay đổi
                        hienThiThongBaoXacNhan(userRef,newUserTypeId,clickedUser.getId_user(), idUser,newUserTypeId,dialog);
                    }
                }else{
                    if(newUserTypeId == 2){
                        Toast.makeText(this, "Bạn không thể cập nhập loại người dùng này", Toast.LENGTH_SHORT).show();
                    }else{
                        if(newUserTypeId == 2 || newUserTypeId == 3){
                            hienThiThongBaoXacNhan(userRef,newUserTypeId,clickedUser.getId_user(), idUser,newUserTypeId,dialog);
                        }else{
                            userRef.child("id_loaiND").setValue(newUserTypeId).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    hienThiThongBaoXacNhan(userRef,newUserTypeId,clickedUser.getId_user(), idUser,newUserTypeId,dialog);

                                    loadDuLieu();

                                } else {
                                    Toast.makeText(this, "Lỗi khi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                                }

                            });
                        }
                    }

                }
            }

        });
        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
    }
    private void hienThiThongBaoXacNhan(DatabaseReference userRef,Long newUserTypeId,String id_userCanUpdate, String id_userYeuCau,Long id_loaiNDUpdate,AlertDialog dialogLuuTT) {
        DialogTtYeucauBinding dialogBinding = DialogTtYeucauBinding.inflate(LayoutInflater.from(this));

        // Tạo AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogBinding.getRoot());

        AlertDialog dialog = builder.create();
        // Thiết lập background trong suốt cho dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        // Xử lý sự kiện nút Hủy
        dialogBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        // Xử lý sự kiện nút Xác nhận
        dialogBinding.btnConfirm.setOnClickListener(v -> {
            // Lưu thông tin yêu cầu
            luuThongTinYeuCau(id_userCanUpdate, id_userYeuCau, id_loaiNDUpdate,id_LoaiNDCu);
            userRef.child("id_loaiND").setValue(newUserTypeId).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Thông tin đã được cập nhật", Toast.LENGTH_SHORT).show();
                    loadDuLieu();
                } else {
                    Toast.makeText(this, "Lỗi khi cập nhật thông tin", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
                dialogLuuTT.dismiss();
            });
            // Hiển thị thông báo thành công
            //Toast.makeText(this, "Đã gửi yêu cầu thay đổi loại người dùng này lên Owner", Toast.LENGTH_SHORT).show();

            dialog.dismiss();
        });

        dialog.show();
    }
    private void luuThongTinYeuCau(String id_userCanUpdate, String id_userYeuCau,Long id_loaiNDUpdate,Long id_LoaiNDCu) {
        DatabaseReference LichSuCapQuyenRef = FirebaseDatabase.getInstance().getReference("LichSuCapQuyen");
        // Tạo id thanh toán duy nhất
        String id_LSCapQuyen = LichSuCapQuyenRef.push().getKey();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String ngayUpdater = sdf.format(new Date());

        LichSuCapQuyen listTTYeuCau = new LichSuCapQuyen(
                id_LSCapQuyen,
                id_userCanUpdate,
                id_userYeuCau,
                ngayUpdater,
                id_loaiNDUpdate,
                id_LoaiNDCu,
                0
        );
        // Thêm giao dịch vào Firebase
        LichSuCapQuyenRef.child(id_LSCapQuyen).setValue(listTTYeuCau);
    }
    private void hienThiRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, this);
        binding.recyclerView.setAdapter(userAdapter);
    }

    private void laySoLuongYeuCauHomNay() {

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
                    // Lấy `type` từ `loaiMap` dựa trên `idLoaiND`, nếu không có thì gán là "Loading..."
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
        loaiNguoiDungRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loaiMap.clear();
                for (DataSnapshot loaiSnapshot : snapshot.getChildren()) {
                    Long id = loaiSnapshot.child("id").getValue(Long.class);
                    String type = loaiSnapshot.child("type").getValue(String.class);
                    if (id != null && type != null) {
                        loaiMap.put(id, type);
                    }
                }
                loadDuLieu();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QLUserActivity.this, "Lỗi khi tải dữ liệu LoaiND", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getSpinnerPosition(Long idLoaiND) {
        ArrayList<Long> keys = new ArrayList<>(loaiMap.keySet());
        return keys.indexOf(idLoaiND);
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

    // Thiết lập OnBackPressedDispatcher
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            if (doubleBackToExitPressedOnce) {
                finishAffinity();  // Thoát ứng dụng
                return;
            }
            doubleBackToExitPressedOnce = true;
            Toast.makeText(getApplicationContext(), "Nhấn thoát thêm một lần nữa", Toast.LENGTH_SHORT).show();

            // Reset lại cờ sau 2 giây
            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
        }
    };
    public void demSLLichSuChuaDoc() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("LichSuCapQuyen");
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int soLuongTTYeuCau = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Integer idTrangThai = snapshot.child("idTrangThai").getValue(Integer.class);
                    if (idTrangThai != null && idTrangThai == 0) {
                        soLuongTTYeuCau++;
                    }
                }
                binding.notificationBadgeCount.setText(String.valueOf(soLuongTTYeuCau));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Lỗi khi đọc dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, AdminActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}