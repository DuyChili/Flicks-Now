package com.example.flicks_now.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flicks_now.R;
import com.example.flicks_now.adapter.QLPhimAdapter;
import com.example.flicks_now.databinding.ActivityQlphimBinding;
import com.example.flicks_now.model.Goi;
import com.example.flicks_now.model.KieuPhim;
import com.example.flicks_now.model.QLPhim;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class QLPhimActivity extends AppCompatActivity {

    private QLPhimAdapter adapter;
    private List<QLPhim> phimList = new ArrayList<>(); // Danh sách phim
    private List<KieuPhim> kieuPhimList = new ArrayList<>();
    private List<Goi> goiList = new ArrayList<>();
    private ActivityQlphimBinding binding;
    private Button selectedButton = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQlphimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Hiển thị tất cả phim khi mở ứng dụng
        fetchMoviesFromFirebase("All");
        updateSelectedButton(binding.btnChonTatCa);


        // Cài đặt RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new QLPhimAdapter(this, phimList, goiList, selectedCount -> {
            if (selectedCount > 0) {
                binding.deleteIcon.setVisibility(View.VISIBLE); // Hiển thị icon xóa nếu có phim được chọn
            } else {
                binding.deleteIcon.setVisibility(View.GONE); // Ẩn icon xóa nếu không có phim nào được chọn
            }
        });

        // Thêm listener cho item click
        adapter.setRecyclerViewItemClickListener((view, position) -> {
            QLPhim selectedPhim = phimList.get(position);
            Intent intent = new Intent(QLPhimActivity.this, ThemPhimActivity.class);
            intent.putExtra("id_movie", selectedPhim.getId_movie()); // Truyền id_movie
            startActivity(intent);
        });
        binding.recyclerView.setAdapter(adapter);


        xulyButton();
    }

    private void xulyButton() {

        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(QLPhimActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            }
        });

        binding.btnAddPhim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent a = new Intent(QLPhimActivity.this  ,ThemPhimActivity.class);
                startActivity(a);

            }
        });


        // Xử lý sự kiện cho các nút
        binding.btnngaytao.setOnClickListener(v ->{
            showDatePickerDialog();
            updateSelectedButton(binding.btnngaytao);
        } );
        xulySpiner();



        // Xử lý sự kiện xóa phim khi nhấn vào icon xóa
        binding.deleteIcon.setOnClickListener(v -> {
            List<QLPhim> selectedMovies = adapter.getSelectedMovies();

            // Kiểm tra nếu có phim được chọn
            if (!selectedMovies.isEmpty()) {
                // Tạo AlertDialog để xác nhận xóa
                new AlertDialog.Builder(this)
                        .setTitle("Xóa phim")
                        .setMessage("Bạn có chắc chắn muốn xóa những phim đã chọn không?")
                        .setPositiveButton("Có", (dialog, which) -> {
                            // Thực hiện xóa phim khi người dùng chọn "Có"
                            deleteSelectedMovies(selectedMovies);
                        })
                        .setNegativeButton("Không", (dialog, which) -> {
                            // Đóng hộp thoại khi người dùng chọn "Không"
                            dialog.dismiss();
                        })
                        .show();
            } else {
                // Hiển thị thông báo nếu không có phim nào được chọn
                Toast.makeText(this, "Không có phim nào được chọn", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý sự kiện khi nhấn nút chọn tất cả
        binding.btnChonTatCa.setOnClickListener(v -> {
            // Lọc lại tất cả các phim không có bộ lọc theo ngày
            updateSelectedButton(binding.btnChonTatCa);
            fetchMoviesFromFirebase("All");
            binding.btnngaytao.setText("Chọn ngày"); // Xóa tiêu đề ngày nếu có

        });

    }

    private void xulySpiner() {
        List<String> goiOptions = Arrays.asList("All", "Thường", "Vip");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, goiOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTimGoi.setAdapter(adapter);

        binding.spinnerTimGoi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGoi = parent.getItemAtPosition(position).toString();
                // Gọi lại fetchMoviesFromFirebase() để tải lại dữ liệu phim từ Firebase
                fetchMoviesFromFirebase(selectedGoi);// Truyền loại gói phim vào đây
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Không cần xử lý gì ở đây
            }
        });

    }

    private void filterMoviesByGoi(String goiType) {
        filterMoviesByGoi(goiType, phimList);  // Lọc theo gói và sử dụng danh sách phim đã lọc
    }

    private void filterMoviesByGoi(String goiType, List<QLPhim> phimListToFilter) {
        List<QLPhim> filteredList = new ArrayList<>();

        for (QLPhim phim : phimListToFilter) {
            if (goiType.equals("All") ||
                    (goiType.equals("Thường") && "0".equals(phim.getGoi())) ||
                    (goiType.equals("Vip") && "1".equals(phim.getGoi()))) {
                filteredList.add(phim);
            }
        }

        Log.d("QLPhimActivity", "Filtered list size by goi: " + filteredList.size());
        adapter.updateMovieList(filteredList);  // Cập nhật lại adapter với danh sách phim đã lọc
    }

    private void deleteSelectedMovies(List<QLPhim> selectedMovies) {
        for (QLPhim phim : selectedMovies) {
            // Xóa phim từ Firebase
            FirebaseDatabase.getInstance().getReference("Movies")
                    .child(phim.getId_movie())
                    .removeValue();
        }
        // Cập nhật lại giao diện
        adapter.getSelectedMovies().clear(); // Xóa danh sách phim đã chọn
        adapter.notifyDataSetChanged(); // Cập nhật RecyclerView
        binding.deleteIcon.setVisibility(View.GONE);
        Toast.makeText(this, "Đã xóa phim", Toast.LENGTH_SHORT).show();
    }

    private void fetchMoviesFromFirebase(String goiType) {

        binding.progressBar.setVisibility(View.VISIBLE);
        fetchGoiFromFirebase();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Movies");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                phimList.clear(); // Xóa dữ liệu cũ (nếu có)
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    QLPhim movie = snapshot.getValue(QLPhim.class);
                    if (movie != null) {
                        phimList.add(movie);
                    }
                }

                // Sau khi tải xong dữ liệu từ Firebase, lọc theo gói
                filterMoviesByGoi(goiType);

                // Nếu người dùng đã chọn ngày, lọc theo ngày
                if (binding.btnngaytao.getText().toString().trim().length() > 0) {
                    String selectedDate = binding.btnngaytao.getText().toString();
                    filterMoviesByDate(selectedDate);
                }
                String a = binding.spinnerTimGoi.getSelectedItem().toString().trim();
                filterMoviesByGoi(a, phimList);  // Lọc theo gói sau khi lọc theo ngày
                binding.progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("QLPhimActivity", "Failed to fetch data", databaseError.toException());
            }
        });
    }


    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                QLPhimActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    binding.btnngaytao.setText(selectedDate);

                    // Lọc lại danh sách phim theo ngày
                    fetchMoviesFromFirebase("All"); // Lọc lại tất cả phim trước khi áp dụng lọc theo ngày
                    filterMoviesByDate(selectedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }
    // Phương thức để cập nhật màu của các nút
    private void updateSelectedButton(Button newButton) {
        // Nếu nút đã được chọn khác nút hiện tại, đổi màu
        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.button_primary)); // Màu mặc định
            selectedButton.setTextColor(getResources().getColor(R.color.text_button)); // Màu chữ mặc định
        }

        // Cập nhật nút hiện tại và đổi màu
        selectedButton = newButton;
        selectedButton.setBackgroundTintList(getResources().getColorStateList(R.color.colorSelected)); // Màu đã chọn
        selectedButton.setTextColor(getResources().getColor(R.color.text_button)); // Màu chữ đã chọn
    }

    private void filterMoviesByDate(String selectedDate) {
        List<QLPhim> filteredList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        for (QLPhim phim : phimList) {
            String ngayTao = phim.getNgayThemPhim();
            Log.d("QLPhimActivity", "Phim date: " + ngayTao + " | Selected date: " + selectedDate);
            if (selectedDate.equals("Chọn ngày")){
                filteredList.add(phim);
            }
            else {
                try {
                    Date phimDate = dateFormat.parse(ngayTao);
                    Date selectedDateObj = dateFormat.parse(selectedDate);

                    if (phimDate.equals(selectedDateObj)) {
                        filteredList.add(phim);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

        }

        Log.d("QLPhimActivity", "Filtered list size by date: " + filteredList.size());
        String a = binding.spinnerTimGoi.getSelectedItem().toString().trim();
        filterMoviesByGoi(a, filteredList);  // Lọc theo gói sau khi lọc theo ngày
    }


    private void fetchGoiFromFirebase() {
        DatabaseReference goiRef = FirebaseDatabase.getInstance().getReference("Goi");
        goiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                goiList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Goi goi = snapshot.getValue(Goi.class);
                    if (goi != null) {
                        goiList.add(goi);
                    }
                }
                fetchGoiFromFirebase();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("QLPhimActivity", "Failed to fetch goi", databaseError.toException());
            }
        });
    }

}