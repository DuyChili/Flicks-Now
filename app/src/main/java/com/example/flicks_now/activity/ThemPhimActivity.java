package com.example.flicks_now.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityThemPhimBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ThemPhimActivity extends AppCompatActivity {
    private ActivityThemPhimBinding binding;
    private String[] genres;
    private boolean[] selectedGenres;
    private ArrayList<String> selectedGenresList = new ArrayList<>();
    private HashMap<String, String> goiMap = new HashMap<>();
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityThemPhimBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Lấy dữ liệu từ Firebase
        layDuLieuGoi();
        // xu ly hien thi ảnh Poster
        xulyHienThiAnh();
        layDuLieuTheLoai();

        // Nhận ID phim từ Intent
        String idMovie = getIntent().getStringExtra("id_movie");
        if (idMovie != null) {
            // Nếu ID phim không null, thực hiện chức năng cập nhật
            layDuLieuPhim(idMovie);
            binding.submitButton.setText("Cập nhật");
            binding.submitButton.setOnClickListener(v -> themPhimVaoFirebase());
        } else {
            // Nếu không, đây là chức năng thêm phim
            binding.submitButton.setOnClickListener(v -> themPhimVaoFirebase());
        }
// Khởi tạo danh sách thể loại và các biến liên quan
        genres = getResources().getStringArray(R.array.genre_array); // Lấy danh sách thể loại từ resources
        selectedGenres = new boolean[genres.length]; // Khởi tạo mảng trạng thái cho các thể loại


        binding.resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetForm();
            }
        });


    }

    private void xulyHienThiAnh() {
        binding.posterUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                updatePosterImage(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void updatePosterImage(String posterUrl) {
        // Kiểm tra xem URL có kết thúc bằng ".jpg" không
        if (!TextUtils.isEmpty(posterUrl) && posterUrl.endsWith(".jpg")) {
            Glide.with(this)
                    .load(posterUrl)
                    .into(binding.posterImage); // Thay đổi tên này thành ID của ImageView mà bạn đã đặt
        } else {
            // Nếu URL không hợp lệ, đặt hình ảnh thành hình ảnh placeholder
            Glide.with(this)
                    .load(R.drawable.poster_image_placeholder) // Replace with your placeholder image resource
                    .into(binding.posterImage);
        }
    }

    private void layDuLieuGoi() {
        DatabaseReference goiRef = FirebaseDatabase.getInstance().getReference("Goi");
        goiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> goiList = new ArrayList<>();
                goiList.add("Chọn Gói"); // Thêm tùy chọn mặc định đầu tiên

                goiMap.clear(); // Đảm bảo HashMap được xóa trước khi thêm mới

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy giá trị của cột "type" và "id" trong bảng Goi
                    String goiType = snapshot.child("Type").getValue(String.class);
                    Long goiIdLong = snapshot.child("id").getValue(Long.class);

                    if (goiType != null && goiIdLong != null) {
                        String goiId = String.valueOf(goiIdLong); // Chuyển Long thành String
                        goiList.add(goiType); // Thêm type vào danh sách
                        goiMap.put(goiType, goiId); // Lưu ID tương ứng vào HashMap
                    }
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(ThemPhimActivity.this, android.R.layout.simple_spinner_item, goiList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.goiSpinner.setAdapter(adapter);

                // Đặt giá trị mặc định
                binding.goiSpinner.setSelection(0);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ThemPhimActivity.this, "Lỗi khi tải dữ liệu gói", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void layDuLieuTheLoai() {
        DatabaseReference theLoaiRef = FirebaseDatabase.getInstance().getReference("theLoai");
        theLoaiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String[] genresArray = new String[(int) dataSnapshot.getChildrenCount()];
                boolean[] selectedGenres = new boolean[genresArray.length];
                ArrayList<String> selectedGenresList = new ArrayList<>();

                int index = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy giá trị của cột "name" trong bảng theLoai
                    String theLoaiName = snapshot.child("name").getValue(String.class);
                    if (theLoaiName != null) {
                        genresArray[index] = theLoaiName;
                        index++;
                    }
                }

                Button selectGenresButton = findViewById(R.id.select_genres_button);
                TextView selectedGenresText = findViewById(R.id.selected_genres_text);

                selectGenresButton.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ThemPhimActivity.this);
                    builder.setTitle("Chọn thể loại")
                            .setMultiChoiceItems(genresArray, selectedGenres, (dialog, which, isChecked) -> {
                                if (isChecked) {
                                    selectedGenresList.add(genresArray[which]);
                                } else {
                                    selectedGenresList.remove(genresArray[which]);
                                }
                            })
                            .setPositiveButton("OK", (dialog, which) -> {
                                if (selectedGenresList.isEmpty()) {
                                    selectedGenresText.setText("Bạn chưa chọn thể loại nào");
                                } else {
                                    selectedGenresText.setText("Thể loại đã chọn: " + String.join(", ", selectedGenresList));
                                }
                            })
                            .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                            .show();
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ThemPhimActivity.this, "Lỗi khi tải dữ liệu thể loại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showGenreDialog() {
        String selectedText = binding.selectedGenresText.getText().toString();
        String[] selectedGenresArray = selectedText.isEmpty() ? new String[0] : selectedText.split(", ");
        selectedGenresList.clear();
        Arrays.fill(selectedGenres, false);

        for (int i = 0; i < genres.length; i++) {
            for (String selectedGenre : selectedGenresArray) {
                if (genres[i].equals(selectedGenre.trim())) {
                    selectedGenres[i] = true;
                    selectedGenresList.add(String.valueOf(i));
                    break;
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn thể loại");
        builder.setMultiChoiceItems(genres, selectedGenres, (dialog, which, isChecked) -> {
            if (isChecked) {
                if (!selectedGenresList.contains(String.valueOf(which))) {
                    selectedGenresList.add(String.valueOf(which));
                }
            } else {
                selectedGenresList.remove(String.valueOf(which));
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            StringBuilder selectedGenresText = new StringBuilder();
            for (int i = 0; i < selectedGenresList.size(); i++) {
                selectedGenresText.append(genres[Integer.parseInt(selectedGenresList.get(i))]);
                if (i != selectedGenresList.size() - 1) {
                    selectedGenresText.append(", ");
                }
            }
            binding.selectedGenresText.setText(selectedGenresText.toString());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void themPhimVaoFirebase() {
        // Lấy giá trị từ các trường nhập liệu
        String tenPhim = binding.title.getText().toString().trim();
        String moTa = binding.description.getText().toString().trim();
        String dienVien = binding.cast.getText().toString().trim();
        String tacGia = binding.actor.getText().toString().trim();
        String thoiLuong = binding.duration.getText().toString().trim();
        String namPhatHanh = binding.releaseYear.getText().toString().trim();
        String posterUrl = binding.posterUrl.getText().toString().trim();
        String thumbUrl = binding.thumbUrl.getText().toString().trim();
        String movieUrl = binding.movieUrl.getText().toString().trim();
        // Lấy giá trị từ Spinner
        String goi = binding.goiSpinner.getSelectedItem().toString();
        String goiId = goiMap.get(goi); // Lấy ID từ HashMap
        // Lấy giá trị từ Spinner
        String quocGia = binding.countrySpinner.getSelectedItem().toString();

        // Lấy thể loại từ TextView (đã được chọn từ dialog)
        String theLoai = binding.selectedGenresText.getText().toString().replace("Thể loại đã chọn: ", "").trim();

        // Kiểm tra xem các trường bắt buộc có bị bỏ trống hay không
        if (TextUtils.isEmpty(tenPhim) || TextUtils.isEmpty(moTa) || TextUtils.isEmpty(dienVien) ||
                TextUtils.isEmpty(tacGia) || TextUtils.isEmpty(thoiLuong) || TextUtils.isEmpty(namPhatHanh) ||
                TextUtils.isEmpty(posterUrl) || TextUtils.isEmpty(thumbUrl) || TextUtils.isEmpty(movieUrl) ||
                goi.equals("Chọn Gói") || quocGia.equals("Chọn quốc gia") || theLoai.isEmpty()) {

            Toast.makeText(ThemPhimActivity.this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        // Kiểm tra xem ID có hợp lệ không
        if (goiId == null) {
            Toast.makeText(ThemPhimActivity.this, "Vui lòng chọn một gói hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        // Lấy ngày hiện tại dưới dạng dd/MM/yyyy
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String ngayHienTai = dateFormat.format(Calendar.getInstance().getTime());

        // Tạo đối tượng phim chứa thông tin
        Map<String, Object> movieData = new HashMap<>();
        movieData.put("name", tenPhim);
        movieData.put("content", moTa);
        movieData.put("poster_url", posterUrl);
        movieData.put("thumb_url", thumbUrl);
        movieData.put("movie_url", movieUrl);
        movieData.put("goi", goiId); // Lưu ID gói vào dữ liệu phim
        movieData.put("time", thoiLuong);
        movieData.put("year", namPhatHanh);
        movieData.put("tacGia", tacGia);
        movieData.put("theLoai", theLoai);
        movieData.put("dienVien", dienVien);
        movieData.put("quocGia", quocGia);
        movieData.put("rating", 0);  // Đặt giá trị mặc định
        movieData.put("id_kieuPhim", 0);  // Đặt giá trị mặc định

        // Thêm cột ngày thêm phim và ngày cập nhật với giá trị ngày hiện tại
        movieData.put("ngayThemPhim", ngayHienTai);
        movieData.put("ngayCapNhat", ngayHienTai);

        // Kiểm tra xem ID phim có phải null hay không để quyết định thêm hay cập nhật
        String idMovie = getIntent().getStringExtra("id_movie");
        DatabaseReference moviesRef = FirebaseDatabase.getInstance().getReference().child("Movies");

        if (idMovie != null) {
            // Cập nhật phim
            moviesRef.child(idMovie).child("id_movie").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String existingIdMovie = snapshot.getValue(String.class);
                    movieData.put("id_movie", existingIdMovie); // Ghi đè id_movie cũ vào movieData
                    moviesRef.child(idMovie).setValue(movieData).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ThemPhimActivity.this, "Cập nhật phim thành công!", Toast.LENGTH_SHORT).show();
                            resetForm();
                        } else {
                            Toast.makeText(ThemPhimActivity.this, "Cập nhật phim thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(ThemPhimActivity.this, "Lỗi khi lấy ID phim!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Thêm phim
            String id_Phim = moviesRef.push().getKey();
            // Thêm idPhim vào movieData
            movieData.put("id_movie", id_Phim);
            moviesRef.child(id_Phim).setValue(movieData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(ThemPhimActivity.this, "Thêm phim thành công!", Toast.LENGTH_SHORT).show();
                    resetForm();
                } else {
                    Toast.makeText(ThemPhimActivity.this, "Thêm phim thất bại!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void resetForm() {
        // Đặt lại các trường nhập liệu
        binding.title.setText("");
        binding.description.setText("");
        binding.cast.setText("");
        binding.actor.setText("");
        binding.duration.setText("");
        binding.releaseYear.setText("");
        binding.posterUrl.setText("");
        binding.thumbUrl.setText("");
        binding.movieUrl.setText("");

        // Đặt lại spinner "Gói" về mặc định
        binding.goiSpinner.setSelection(0);  // Vị trí 0 là "Chọn Gói"

        // Đặt lại spinner "Quốc gia" về mặc định
        binding.countrySpinner.setSelection(0);  // Vị trí 0 là "Chọn quốc gia"

        // Đặt lại TextView chọn thể loại
        binding.selectedGenresText.setText("Bạn chưa chọn thể loại nào");

        // Đặt lại danh sách thể loại đã chọn
        selectedGenresList.clear();
        selectedGenres = new boolean[genres.length];  // Reset các lựa chọn thể loại
    }

    private void layDuLieuPhim(String idMovie) {
        DatabaseReference movieRef = FirebaseDatabase.getInstance().getReference("Movies").child(idMovie);
        DatabaseReference goi = FirebaseDatabase.getInstance().getReference("Goi").child(idMovie);
        movieRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Lấy thông tin phim và hiển thị lên các trường nhập liệu
                    String tenPhim = dataSnapshot.child("name").getValue(String.class);
                    String moTa = dataSnapshot.child("content").getValue(String.class);
                    String dienVien = dataSnapshot.child("dienVien").getValue(String.class);
                    String tacGia = dataSnapshot.child("tacGia").getValue(String.class);
                    String thoiLuong = dataSnapshot.child("time").getValue(String.class);
                    String namPhatHanh = dataSnapshot.child("year").getValue(String.class);
                    String posterUrl = dataSnapshot.child("poster_url").getValue(String.class);
                    String thumbUrl = dataSnapshot.child("thumb_url").getValue(String.class);
                    String movieUrl = dataSnapshot.child("movie_url").getValue(String.class);
                    String goiId = dataSnapshot.child("goi").getValue(String.class); // Lấy goiId
                    String quocGia = dataSnapshot.child("quocGia").getValue(String.class);
                    String theLoai = dataSnapshot.child("theLoai").getValue(String.class);

                    // Hiển thị thông tin lên màn hình
                    binding.title.setText(tenPhim);
                    binding.description.setText(moTa);
                    binding.cast.setText(dienVien);
                    binding.actor.setText(tacGia);
                    binding.duration.setText(thoiLuong);
                    binding.releaseYear.setText(namPhatHanh);
                    binding.posterUrl.setText(posterUrl);
                    binding.thumbUrl.setText(thumbUrl);
                    binding.movieUrl.setText(movieUrl);

                    // Đặt vị trí cho Spinner dựa trên ID
//
                    binding.goiSpinner.setSelection(getSpinnerPosition(binding.goiSpinner, goiId));
                    // cho them vao firebase
                    binding.countrySpinner.setSelection(getSpinnerPosition(binding.countrySpinner, quocGia));
                    binding.selectedGenresText.setText("Thể loại đã chọn: " + theLoai);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ThemPhimActivity.this, "Lỗi khi tải dữ liệu phim", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private int getSpinnerPosition(Spinner spinner, String id) {
        for (int i = 0; i < spinner.getCount(); i++) {
            String itemId = goiMap.get(spinner.getItemAtPosition(i).toString());
            if (itemId != null && itemId.equals(id)) {
                return i; // Trả về vị trí tìm thấy
            }
        }
        return 0; // Về mặc định nếu không tìm thấy
    }
}
