package com.example.flicks_now.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.flicks_now.adapter.BannerAdapter;
import com.example.flicks_now.adapter.BannerOphimAdapter;
import com.example.flicks_now.adapter.DSPhimAdapter;
import com.example.flicks_now.adapter.DSPhimAdapterOphim;
import com.example.flicks_now.adapter.MyExpandableListAdapter;
import com.example.flicks_now.adapter.PhimAdapter;
import com.example.flicks_now.api.ApiClient;
import com.example.flicks_now.api.ApiService;
import com.example.flicks_now.R;
import com.example.flicks_now.databinding.ActivityMainBinding;
import com.example.flicks_now.model.DSPhim;
import com.example.flicks_now.model.DSPhimAPiOphim;
import com.example.flicks_now.model.Phim;
import com.example.flicks_now.model.PhimAPiOphim;
import com.example.flicks_now.model.QLPhim;
import com.example.flicks_now.model.ThongBaoKhiUngDungTat;
import com.example.flicks_now.model.ThongBaoTrenManHinh;
import com.example.flicks_now.model.TruyCap;
import com.example.flicks_now.response.DSPhimResponse;
import com.example.flicks_now.response.DSResponseOphim;
import com.example.flicks_now.response.PhimResponse;
import com.example.flicks_now.response.PhimResponseOphim;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private PhimAdapter movieAdapter;
    private ApiService apiService;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String idUser;
    private  String nameUser;
    private String emailUser;
    private int idLoaiND;
    // bien de kiểm tra người dùng có đang ỏ trong ứng dụng hay không
    public static Boolean truycap = false;
    private List<DSPhim> DSKkphimPhimLe, DSKkphimBo, DSKkphimHoatHinh, DSKkphimTvShow;
    private List<DSPhimAPiOphim> DSOphimLe, DSOphimBo, DSOphimHoatHinh, DSOphimTvShow;
    private Handler bannerHandler = new Handler();
    private Runnable bannerRunnable;
    private MyExpandableListAdapter adapter;
    private List<String> listHeaders;
    private HashMap<String, List<String>> listChildren;
    private Map<String, String> theLoaiSlugMap = new HashMap<>();
    private Map<String, String> quocGiaSlugMap = new HashMap<>();
    private String theLoaiSlug = null;
    private String quocGiaSlug = null;
    private boolean doubleBackToExitPressedOnce = false;
    private String selectedTheLoaiName;
    private String selectedQuocGiaName;
    private PhimAdapter phimAdapter;
    private DatabaseReference movieRef;
    private List<QLPhim> movieList;
    private boolean isUserLoggedIn = false; // Biến để theo dõi trạng thái đăng nhập
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Goi chuc nang nhan 2 lan de thoat
        getOnBackPressedDispatcher().addCallback(this, callback);
        laythongtinUser();

        theoDoiThayDoiTrenFirebase();

        loc();
        Toast.makeText(MainActivity.this, "Xin chào " + nameUser, Toast.LENGTH_SHORT).show();
        updateUser();
        // Kiểm tra và thêm thông tin truy cập
        kiemTraTruyCap(idUser);

        // Thiết lập ActionBar và DrawerLayout
        setSupportActionBar(binding.toolbar);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, binding.drawerlayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );

        binding.drawerlayout.addDrawerListener(toggle);
        toggle.syncState();

        // Đặt biểu tượng trở về
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về
        }
        // Ẩn tiêu đề
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        swipeRefreshLayout = binding.swipeRefreshLayout; // Khởi tạo SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadSeries(); // Tải lại danh sach phim bo
            loadTVShow();// Tải lại danh sách tvshow
            loadPhimLe();
            loadPhimHoatHinh();
            fetchMoviesFromFirebase();
            binding.dsPhim.setVisibility(View.GONE);
            binding.recyclerViewMovies.setVisibility(View.GONE);
            binding.recyclerTimKiem.setVisibility(View.GONE);
            binding.dsPhimTimKiem.setVisibility(View.GONE);
        });
        binding.expandableListView.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            String headerTitle = listHeaders.get(groupPosition);

            if ("Đăng Nhập".equals(headerTitle)) {
                Intent intent = new Intent(MainActivity.this, DangNhapActivity.class);
                startActivity(intent);
                return true; // Ngăn chặn mở rộng nhóm
            } else if ("Thông tin cá nhân".equals(headerTitle)) {
                Intent intent = new Intent(MainActivity.this, CaNhanActivity.class);
                startActivity(intent);
                return true; // Ngăn chặn mở rộng nhóm
            }else if ("Admin".equals(headerTitle)) {
                Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                startActivity(intent);
                return true; // Ngăn chặn mở rộng nhóm
            }else if ("Đăng Xuất".equals(headerTitle)) {
                dangXuat();
                return true; // Ngăn chặn mở rộng nhóm
            }

            return false; // Cho phép mở rộng nhóm nếu không phải là "Đăng Nhập" hoặc "Thông tin cá nhân"
        });

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        movieRef = database.getReference("movies"); // Đây là nơi lưu trữ thông tin phim trên Firebase

        // Khởi tạo movieList và RecyclerView
        movieList = new ArrayList<>();
        phimAdapter = new PhimAdapter(this, movieList);
        binding.recyclerViewphim.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewphim.setAdapter(phimAdapter);
        // Load phim từ Firebase
        fetchMoviesFromFirebase();

        apiService = ApiClient.getClient().create(ApiService.class);
        // Khởi tạo danh sách phim
        DSKkphimPhimLe = new ArrayList<>();
        DSKkphimBo = new ArrayList<>();
        DSKkphimHoatHinh = new ArrayList<>();
        DSKkphimTvShow = new ArrayList<>();
        // Khởi tạo danh sách phim
        DSOphimLe = new ArrayList<>();
        DSOphimBo = new ArrayList<>();
        DSOphimHoatHinh = new ArrayList<>();
        DSOphimTvShow = new ArrayList<>();

        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
            @Override
            public void onBaseUrlFetched(String name, String url) {
                if ("Kkphim".equals(name)) {
                    binding.xemThemPhimBo.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                        intent.putExtra("type", "series"); // Thêm loại phim bộ
                        startActivity(intent);
                    });
                    binding.xemThemPhimLe.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                        intent.putExtra("type", "movie"); // Thêm loại phim bộ
                        startActivity(intent);
                    });
                    binding.xemThemTVshow.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                        intent.putExtra("type", "tvShow"); // Thêm loại phim bộ
                        startActivity(intent);
                    });
                    binding.xemThemHoatHinh.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                        intent.putExtra("type", "hoathinh"); // Thêm loại phim bộ
                        startActivity(intent);
                    });

                } else if ("Ophim".equals(name)) {
                    binding.xemThemPhimBo.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                        intent.putExtra("type", "series"); // Thêm loại phim bộ
                        startActivity(intent);
                    });
                    binding.xemThemPhimLe.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                        intent.putExtra("type", "movie"); // Thêm loại phim bộ
                        startActivity(intent);
                    });
                    binding.xemThemHoatHinh.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                        intent.putExtra("type", "hoathinh"); // Thêm loại phim bộ
                        startActivity(intent);
                    });
                    binding.xemThemTVshow.setOnClickListener(v -> {
                        Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                        intent.putExtra("type", "tvShow"); // Thêm loại phim bộ
                        startActivity(intent);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, MainActivity.this); // Thêm MainActivity.this làm Context);

        setupRecyclerViews();
        loadSeries();
        loadTVShow();
        loadPhimLe();
        loadPhimHoatHinh();
        navigationBottom();
        ghiLaiTrangThai();

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            // Khởi tạo ThongBaoTrenManHinh
            ThongBaoTrenManHinh thongBao = new ThongBaoTrenManHinh(getApplicationContext());
            // Bắt đầu dịch vụ để lắng nghe thông báo
            thongBao.batDichVuThongBao();
            // Lấy ID người dùng và bắt đầu lắng nghe
            thongBao.layIdNguoiDungHienTai();
            // Khởi động dịch vụ khi nhận được thông báo
            Intent serviceIntent = new Intent(MainActivity.this, ThongBaoKhiUngDungTat.class);
            MainActivity.this.startForegroundService(serviceIntent);
        } else {
            Toast.makeText(this, "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
        }
        // Khởi tạo và chạy banner
        loaDuLieuApiKhiThayDoi();
    }
    private void laythongtinUser(){
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        idUser = sharedPreferences.getString("id_user", null);
        nameUser = sharedPreferences.getString("name", null);
        emailUser  = sharedPreferences.getString("email", null);
        idLoaiND = sharedPreferences.getInt("id_loaiND", -1);
        Log.d("id_loaiND Ban đầu", String.valueOf(idLoaiND));
    }
    private void theoDoiThayDoiTrenFirebase() {
        if (idUser != null) {
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

            // Tìm bản ghi có id_user khớp với idUser
            usersRef.orderByChild("id_user").equalTo(idUser).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            // Lấy dữ liệu mới từ Firebase
                            String newName = userSnapshot.child("name").getValue(String.class);
                            String newEmail = userSnapshot.child("email").getValue(String.class);
                            Integer newIdLoaiND = userSnapshot.child("id_loaiND").getValue(Integer.class);

                            if (newIdLoaiND != null) {
                                // Cập nhật lại SharedPreferences với dữ liệu mới
                                SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("name", newName);
                                editor.putString("email", newEmail);
                                editor.putInt("id_loaiND", newIdLoaiND);
                                editor.apply();

                                // Cập nhật biến trong Activity nếu cần thiết
                                nameUser = newName;
                                emailUser = newEmail;
                                idLoaiND = newIdLoaiND;

                                Log.d("id_loaiND sau khi cập nhập", String.valueOf(idLoaiND));
                            } else {
                                Log.e("Lỗi", "Giá trị id_loaiND null");
                            }
                        }
                    } else {
                        Log.e("Lỗi", "Không tìm thấy người dùng với id_user: " + idUser);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi nếu cần
                    Log.e("FirebaseError", "Không thể lắng nghe thay đổi dữ liệu.", error.toException());
                }
            });
        }
    }

    public static void kiemTraTruyCap(String idUser) {
        // Kiểm tra xem id_user có null hay không và xem ngày truy cập đã tồn tại hay chưa
        DatabaseReference truyCapRef = FirebaseDatabase.getInstance().getReference("TruyCap");
        long currentTime = System.currentTimeMillis();

        // Lấy ngày hiện tại (không bao gồm giờ, phút, giây)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(new Date(currentTime));

        // Tìm kiếm bản ghi theo id_user và ngày truy cập
        truyCapRef.orderByChild("id_user").equalTo(idUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // neu truycap == false thì sẽ thêm vào TruyCap trên firebase
                if (truycap == false){
                    themTruyCap(idUser);
                    truycap = true;
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TruyCap", "Lỗi khi kiểm tra truy cập: " + databaseError.getMessage());
            }
        });
    }

    private void loc(){
        listHeaders = new ArrayList<>();
        listChildren = new HashMap<>();
        hienThiThongTinBoLoc();
        adapter = new MyExpandableListAdapter(this, listHeaders, listChildren);
        binding.expandableListView.setAdapter(adapter);
        binding.expandableListView.setGroupIndicator(null);
        binding.expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String header = (String) adapter.getGroup(groupPosition);
            String selectedItem = (String) adapter.getChild(groupPosition, childPosition);
            // Cập nhật tên thể loại hoặc quốc gia đã chọn
            if ("Thể Loại".equals(header)) {
                selectedTheLoaiName = selectedItem; // Lưu tên thể loại
                theLoaiSlug = theLoaiSlugMap.get(selectedItem); // Lấy slug cho thể loại
                // Cập nhật TextView hiển thị tên thể loại
                binding.tvSlug.setText("Thể loại: " + selectedTheLoaiName);
            } else if ("Quốc Gia".equals(header)) {
                selectedQuocGiaName = selectedItem; // Lưu tên quốc gia
                quocGiaSlug = quocGiaSlugMap.get(selectedItem); // Lấy slug cho quốc gia
                // Cập nhật TextView hiển thị tên quốc gia
                binding.tvSlug.setText("Quốc gia: " + selectedQuocGiaName);
            }

            if (theLoaiSlug != null) {
                // Nếu chỉ có slug thể loại
                ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
                    @Override
                    public void onBaseUrlFetched(String name, String url) {
                        if ("Kkphim".equals(name)) {
                            binding.recyclerViewMovies.setAdapter(new DSPhimAdapter(MainActivity.this, new ArrayList<>())); // Tạo adapter mới
                            hienThiPhimKKPhim(theLoaiSlug, null); // Hoặc xử lý theo cách bạn muốn
                            binding.xemThemBoLoc.setOnClickListener(v -> {
                                Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                                intent.putExtra("theloai", theLoaiSlug); // Thêm loại phim bộ
                                startActivity(intent);
                            });
                        } else if ("Ophim".equals(name)) {
                            binding.recyclerViewMovies.setAdapter(new DSPhimAdapterOphim(MainActivity.this, new ArrayList<>())); // Tạo adapter mới
                            hienThiPhimOPhim(theLoaiSlug, null); // Hoặc xử lý theo cách bạn muốn
                            binding.xemThemBoLoc.setOnClickListener(v -> {
                                Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                                intent.putExtra("theloai", theLoaiSlug); // Thêm loại phim bộ
                                startActivity(intent);
                            });
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(MainActivity.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }, MainActivity.this); // Thêm MainActivity.this làm Context);
            } else if (quocGiaSlug != null) {
                // Nếu chỉ có slug quốc gia
                ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
                    @Override
                    public void onBaseUrlFetched(String name, String url) {
                        if ("Kkphim".equals(name)) {
                            binding.recyclerViewMovies.setAdapter(new DSPhimAdapter(MainActivity.this, new ArrayList<>())); // Tạo adapter mới
                            hienThiPhimKKPhim(null, quocGiaSlug); // Hoặc xử lý theo cách bạn muốn
                            binding.xemThemBoLoc.setOnClickListener(v -> {
                                Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                                intent.putExtra("quocgia", quocGiaSlug); // Thêm quốc gia
                                startActivity(intent);
                            });
                        } else if ("Ophim".equals(name)) {
                            binding.recyclerViewMovies.setAdapter(new DSPhimAdapterOphim(MainActivity.this, new ArrayList<>())); // Tạo adapter mới
                            hienThiPhimOPhim(null, quocGiaSlug); // Hoặc xử lý theo cách bạn muốn
                            binding.xemThemBoLoc.setOnClickListener(v -> {
                                Intent intent = new Intent(MainActivity.this, XemThemPhim.class);
                                intent.putExtra("quocgia", quocGiaSlug); // Thêm quốc gia
                                startActivity(intent);
                            });
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(MainActivity.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }, MainActivity.this); // Thêm MainActivity.this làm Context);
            }
            return false;
        });

    }

    private void hienThiThongTinBoLoc() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        // Khởi tạo danh sách con
        List<String> theLoaiItems = new ArrayList<>();
        List<String> quocGiaItems = new ArrayList<>();

        // Thêm các tiêu đề một lần duy nhất
        listHeaders.add("Thể Loại");
        listHeaders.add("Quốc Gia");
        listHeaders.add("Cài Đặt");

        // Thêm các mục tĩnh khác
        List<String> caiDat = new ArrayList<>();
        caiDat.add("Giao diện sáng");
        caiDat.add("Giao diện tối");
        listChildren.put("Cài Đặt", caiDat);
        listHeaders.add("Thông tin cá nhân");
        listHeaders.add("Admin");
        listHeaders.add("Đăng Nhập");
        if (idUser != null) {
            if (idLoaiND == 3 || idLoaiND == 2) { // Kiểm tra nếu là admin
                isUserLoggedIn = true; // Người dùng là admin

                // Kiểm tra và thêm mục "Admin" nếu chưa có
                if (!listHeaders.contains("Admin")) {
                    listHeaders.add("Admin");
                }

                // Kiểm tra và thêm mục "Đăng Xuất" nếu chưa có
                if (!listHeaders.contains("Đăng Xuất")) {
                    listHeaders.add("Đăng Xuất");
                }

                // Xóa mục "Đăng Nhập" nếu tồn tại
                listHeaders.remove("Đăng Nhập");

            } else { // Người dùng thông thường
                isUserLoggedIn = true;

                // Xóa mục "Admin" nếu tồn tại
                listHeaders.remove("Admin");

                // Kiểm tra và thêm mục "Đăng Xuất" nếu chưa có
                if (!listHeaders.contains("Đăng Xuất")) {
                    listHeaders.add("Đăng Xuất");
                }

                // Xóa mục "Đăng Nhập" nếu tồn tại
                listHeaders.remove("Đăng Nhập");
            }
        } else { // Người dùng chưa đăng nhập
            isUserLoggedIn = false;

            // Xóa các mục "Admin" và "Đăng Xuất" nếu tồn tại
            listHeaders.remove("Admin");
            listHeaders.remove("Đăng Xuất");

            // Kiểm tra và thêm mục "Đăng Nhập" nếu chưa có
            if (!listHeaders.contains("Đăng Nhập")) {
                listHeaders.add("Đăng Nhập");
            }
        }



        // Lấy dữ liệu "Thể Loại" từ Firebase
        database.child("theLoai").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                theLoaiItems.clear();
                theLoaiSlugMap.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String categoryName = itemSnapshot.child("name").getValue(String.class);
                    if (categoryName != null) {
                        String slug = convertToSlug(categoryName); // Chuyển đổi tên sang slug
                        theLoaiItems.add(categoryName);
                        theLoaiSlugMap.put(categoryName, slug); // Lưu slug theo tên thể loại
                    }
                }
                listChildren.put("Thể Loại", theLoaiItems);

                checkAndNotifyAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });

        // Lấy dữ liệu "Quốc Gia" từ Firebase
        database.child("quocGia").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                quocGiaItems.clear(); // Xóa dữ liệu cũ nếu cần
                quocGiaSlugMap.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    String countryName = itemSnapshot.child("name").getValue(String.class);
                    if (countryName != null) {
                        String slug = convertToSlug(countryName); // Chuyển đổi tên sang slug
                        quocGiaItems.add(countryName);
                        quocGiaSlugMap.put(countryName, slug); // Lưu slug theo tên thể loại
                    }
                }
                listChildren.put("Quốc Gia", quocGiaItems);

                checkAndNotifyAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void dangXuat() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("status");

            // Đặt trạng thái là "offline" trong Firebase Database
            userStatusRef.setValue("offline").addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Thực hiện đăng xuất khỏi Firebase Auth
                    FirebaseAuth.getInstance().signOut();

                    // Xóa thông tin trong SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear(); // Xóa tất cả dữ liệu trong SharedPreferences
                    editor.apply();

                    // Chuyển người dùng về MainActivity
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);

                    Toast.makeText(this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Đăng xuất thất bại, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void hienThiPhimKKPhim(String theLoaiSlug, String quocGiaSlug) {
        int page = 1; // Hoặc thay đổi theo nhu cầu của bạn
        // Kiểm tra xem cả hai slug đều không null
        if (theLoaiSlug != null) {
            apiService.getTheLoaiKKPhim(theLoaiSlug, page).enqueue(new Callback<DSPhimResponse>() {
                @Override
                public void onResponse(Call<DSPhimResponse> call, Response<DSPhimResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<DSPhim> theLoaiKkphim = response.body().getData().getItems();

                        if (theLoaiKkphim.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Không tìm thấy dữ liệu cho thể loại này.", Toast.LENGTH_SHORT).show();
                            binding.dsPhim.setVisibility(View.GONE);
                            binding.recyclerViewMovies.setVisibility(View.GONE);
                            binding.recyclerTimKiem.setVisibility(View.GONE);
                            binding.dsPhimTimKiem.setVisibility(View.GONE);
                            return;
                        }
                        // Giới hạn danh sách chỉ 9 phim
                        if (theLoaiKkphim.size() > 9) {
                            theLoaiKkphim = theLoaiKkphim.subList(0, 9);
                        }

                        binding.recyclerViewMovies.setAdapter(new DSPhimAdapter(MainActivity.this, theLoaiKkphim));
                        binding.dsPhim.setVisibility(View.VISIBLE);
                        binding.recyclerViewMovies.setVisibility(View.VISIBLE);
                        binding.recyclerTimKiem.setVisibility(View.GONE);
                        binding.dsPhimTimKiem.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<DSPhimResponse> call, Throwable t) {
                    binding.dsPhim.setVisibility(View.GONE);
                    binding.recyclerViewMovies.setVisibility(View.GONE);
                    binding.recyclerTimKiem.setVisibility(View.GONE);
                    binding.dsPhimTimKiem.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Không thể tải dữ liệu thể loại", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (quocGiaSlug != null) {
            apiService.getQuocGiaKKPhim(quocGiaSlug, page).enqueue(new Callback<DSPhimResponse>() {
                @Override
                public void onResponse(Call<DSPhimResponse> call, Response<DSPhimResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<DSPhim> quocGiaKkphim = response.body().getData().getItems();

                        if (quocGiaKkphim.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Không tìm thấy dữ liệu cho quốc gia này.", Toast.LENGTH_SHORT).show();
                            binding.dsPhim.setVisibility(View.GONE);
                            binding.recyclerViewMovies.setVisibility(View.GONE);
                            binding.recyclerTimKiem.setVisibility(View.GONE);
                            binding.dsPhimTimKiem.setVisibility(View.GONE);
                            return;
                        }
                        // Giới hạn danh sách chỉ 9 phim
                        if (quocGiaKkphim.size() > 9) {
                            quocGiaKkphim = quocGiaKkphim.subList(0, 9);
                        }

                        binding.recyclerViewMovies.setAdapter(new DSPhimAdapter(MainActivity.this, quocGiaKkphim));
                        binding.dsPhim.setVisibility(View.VISIBLE);
                        binding.recyclerViewMovies.setVisibility(View.VISIBLE);
                        binding.recyclerTimKiem.setVisibility(View.GONE);
                        binding.dsPhimTimKiem.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<DSPhimResponse> call, Throwable t) {
                    binding.dsPhim.setVisibility(View.GONE);
                    binding.dsPhimTimKiem.setVisibility(View.GONE);
                    binding.recyclerViewMovies.setVisibility(View.GONE);
                    binding.recyclerTimKiem.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Không thể tải dữ liệu quốc gia", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void hienThiPhimOPhim(String theLoaiSlug, String quocGiaSlug) {
        int page = 1; // Hoặc thay đổi theo nhu cầu của bạn
        // Kiểm tra xem cả hai slug đều không null
        if (theLoaiSlug != null) {
            apiService.getTheLoaiOPhim(theLoaiSlug, page).enqueue(new Callback<DSResponseOphim>() {
                @Override
                public void onResponse(Call<DSResponseOphim> call, Response<DSResponseOphim> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<DSPhimAPiOphim> theLoaiOphim = response.body().getData().getItems();

                        if (theLoaiOphim.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Không tìm thấy dữ liệu cho thể loại này.", Toast.LENGTH_SHORT).show();
                            binding.dsPhim.setVisibility(View.GONE);
                            binding.recyclerViewMovies.setVisibility(View.GONE);
                            binding.recyclerTimKiem.setVisibility(View.GONE);
                            binding.dsPhimTimKiem.setVisibility(View.GONE);
                            return;
                        }
                        // Giới hạn danh sách chỉ 9 phim
                        if (theLoaiOphim.size() > 9) {
                            theLoaiOphim = theLoaiOphim.subList(0, 9);
                        }

                        binding.recyclerViewMovies.setAdapter(new DSPhimAdapterOphim(MainActivity.this, theLoaiOphim));
                        binding.dsPhim.setVisibility(View.VISIBLE);
                        binding.recyclerViewMovies.setVisibility(View.VISIBLE);
                        binding.recyclerTimKiem.setVisibility(View.GONE);
                        binding.dsPhimTimKiem.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<DSResponseOphim> call, Throwable t) {
                    binding.dsPhim.setVisibility(View.GONE);
                    binding.dsPhimTimKiem.setVisibility(View.GONE);
                    binding.recyclerViewMovies.setVisibility(View.GONE);
                    binding.recyclerTimKiem.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Không thể tải dữ liệu thể loại", Toast.LENGTH_SHORT).show();
                }
            });
        } else if (quocGiaSlug != null) {
            apiService.getQuocGiaOPhim(quocGiaSlug, page).enqueue(new Callback<DSResponseOphim>() {
                @Override
                public void onResponse(Call<DSResponseOphim> call, Response<DSResponseOphim> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<DSPhimAPiOphim> quocGiaOphim = response.body().getData().getItems();

                        if (quocGiaOphim.isEmpty()) {
                            Toast.makeText(MainActivity.this, "Không tìm thấy dữ liệu cho quốc gia này.", Toast.LENGTH_SHORT).show();
                            binding.dsPhim.setVisibility(View.GONE);
                            binding.recyclerViewMovies.setVisibility(View.GONE);
                            binding.dsPhimTimKiem.setVisibility(View.GONE);
                            binding.recyclerTimKiem.setVisibility(View.GONE);
                            return;
                        }
                        // Giới hạn danh sách chỉ 9 phim
                        if (quocGiaOphim.size() > 9) {
                            quocGiaOphim = quocGiaOphim.subList(0, 9);
                        }

                        binding.recyclerViewMovies.setAdapter(new DSPhimAdapterOphim(MainActivity.this, quocGiaOphim));
                        binding.dsPhim.setVisibility(View.VISIBLE);
                        binding.dsPhimTimKiem.setVisibility(View.GONE);
                        binding.recyclerViewMovies.setVisibility(View.VISIBLE);
                        binding.recyclerTimKiem.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<DSResponseOphim> call, Throwable t) {
                    binding.dsPhim.setVisibility(View.GONE);
                    binding.dsPhimTimKiem.setVisibility(View.GONE);
                    binding.recyclerViewMovies.setVisibility(View.GONE);
                    binding.recyclerTimKiem.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Không thể tải dữ liệu quốc gia", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }





    private String convertToSlug(String input) {
        // Chuẩn hóa văn bản để tách dấu khỏi các ký tự cơ bản
        String slug = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[Đđ]", "d")   // Thay thế Đ và đ bằng d
                .replaceAll("[^\\p{ASCII}]", "")         // Xóa dấu và các ký tự đặc biệt
                .replaceAll("[^a-zA-Z0-9\\s]", "")       // Xóa các ký tự không phải chữ-số ngoại trừ khoảng trắng
                .trim()                                  // Xóa khoảng trắng đầu và cuối chuỗi
                .replaceAll("\\s+", "-")                 // Thay thế khoảng trắng bằng dấu gạch ngang
                .toLowerCase();                          // Chuyển sang chữ thường để tạo dạng slug

        return slug;
    }

    // Phương thức kiểm tra nếu dữ liệu đã sẵn sàng và thông báo adapter
    private void checkAndNotifyAdapter() {
        // Đảm bảo danh sách tiêu đề và danh sách con đã có đủ dữ liệu
        if (listHeaders.size() >= 3 && listChildren.size() >= 3) {
            adapter.notifyDataSetChanged(); // Cập nhật adapter
        }
    }

    public static void themTruyCap(String idUser) {
        DatabaseReference truyCapRef = FirebaseDatabase.getInstance().getReference("TruyCap");
        // Định dạng ngày và giờ thanh toán theo dd-MM-yyyy HH:mm:ss
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(new Date()); // Lấy ngày và giờ hiện tại và định dạng

        // Tạo một ID mới cho bản ghi truy cập
        String truyCapId = truyCapRef.push().getKey();
        TruyCap truyCap = new TruyCap(idUser, formattedDate);

        // Thêm thông tin truy cập vào Firebase
        truyCapRef.child(truyCapId).setValue(truyCap)
                .addOnSuccessListener(aVoid -> {
                    // Xử lý thành công
                    Log.d("TruyCap", "Thêm truy cập thành công cho người dùng: " + idUser);
                })
                .addOnFailureListener(e -> {
                    // Xử lý lỗi
                    Log.e("TruyCap", "Lỗi khi thêm truy cập: " + e.getMessage());
                });
    }

    private void updateUser(){
        // Tham chiếu đến NavigationView
        NavigationView navigationView = findViewById(R.id.navigationView);  // Giả sử NavigationView có id là nav_view

        // Lấy header view từ NavigationView
        View headerView = navigationView.getHeaderView(0);

        // Tham chiếu đến TextView trong header view
        TextView textView = headerView.findViewById(R.id.tvTenNguoiDung); // Thay bằng id của TextView trong layout_header

        if(nameUser != null){
            // Thay đổi nội dung TextView
            textView.setText(nameUser);
        }else{
            textView.setText("Khách");
        }
    }
    private void ghiLaiTrangThai() {
        // Lấy user hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) { // Kiểm tra user có phải là null hay không
            String userId = user.getUid();
            DatabaseReference userStatusRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("status");

            // Theo dõi trạng thái kết nối Firebase
            DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
            connectedRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean connected = snapshot.getValue(Boolean.class);
                    if (connected) {
                        // Khi người dùng kết nối với Firebase, đặt trạng thái là "online"
                        userStatusRef.setValue("online");

                        // Khi người dùng ngắt kết nối, đặt trạng thái là "offline"
                        userStatusRef.onDisconnect().setValue("offline");
                    } else {
                        // Khi không kết nối, bạn cũng có thể cập nhật lại "offline" nếu cần
                        userStatusRef.setValue("offline");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("Firebase", "Không thể lấy trạng thái kết nối.", error.toException());
                }
            });
        } else {
            // Nếu không có user (trạng thái khách)
            updateUser(); // Cập nhật thông tin người dùng để hiển thị trạng thái "Khách"
            Log.w("Firebase", "Trạng thái Khách");
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Nạp menu
        getMenuInflater().inflate(R.menu.menu_timkiem, menu);
        // Tìm kiếm item trong menu
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // Thiết lập listener cho sự kiện tìm kiếm
        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
            @Override
            public void onBaseUrlFetched(String name, String url) {
                if ("Kkphim".equals(name)) {
                    // Thiết lập listener cho sự kiện tìm kiếm
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            // Khi người dùng nhấn vào nút tìm kiếm trên bàn phím
                            // Hiển thị nội dung tìm kiếm qua Toast
                            Toast.makeText(MainActivity.this, "Tìm kiếm: " + query, Toast.LENGTH_SHORT).show();

                            // Gọi API tìm kiếm với từ khóa và giới hạn 10 kết quả
                            apiService.searchMovies(query, 10).enqueue(new Callback<DSPhimResponse>() {
                                @Override
                                public void onResponse(Call<DSPhimResponse> call, Response<DSPhimResponse> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        List<DSPhim> seriesKkphims = response.body().getData().getItems();
                                        // Nếu có kết quả, hiển thị kết quả tìm kiếm
                                        if (!seriesKkphims.isEmpty()) {
                                            binding.dsPhimTimKiem.setVisibility(View.VISIBLE);
                                            binding.recyclerTimKiem.setVisibility(View.VISIBLE);
                                            binding.recyclerViewMovies.setVisibility(View.GONE);
                                            binding.dsPhim.setVisibility(View.GONE);
                                            binding.recyclerTimKiem.setAdapter(new DSPhimAdapter(MainActivity.this, seriesKkphims));
                                        } else {
                                            binding.dsPhimTimKiem.setVisibility(View.GONE);
                                            binding.recyclerTimKiem.setVisibility(View.GONE);
                                            binding.recyclerViewMovies.setVisibility(View.GONE);
                                            binding.dsPhim.setVisibility(View.GONE);
                                            Toast.makeText(MainActivity.this, "Không tìm thấy phim", Toast.LENGTH_SHORT).show();
                                        }

                                        // Đóng SearchView sau khi tìm kiếm
                                        searchView.clearFocus();
                                    } else {
                                        binding.dsPhimTimKiem.setVisibility(View.GONE);
                                        binding.recyclerTimKiem.setVisibility(View.GONE);
                                        binding.recyclerViewMovies.setVisibility(View.GONE);
                                        binding.dsPhim.setVisibility(View.GONE);
                                        Toast.makeText(MainActivity.this, "Không tìm thấy phim", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<DSPhimResponse> call, Throwable t) {
                                    binding.dsPhimTimKiem.setVisibility(View.GONE);
                                    binding.recyclerTimKiem.setVisibility(View.GONE);
                                    binding.recyclerViewMovies.setVisibility(View.GONE);
                                    binding.dsPhim.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this, "Lỗi khi tìm kiếm", Toast.LENGTH_SHORT).show();
                                }
                            });

                            //searchView.clearFocus();
                            searchItem.collapseActionView();

                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            // Xử lý khi nội dung tìm kiếm thay đổi (nếu cần)
                            return false;
                        }
                    });
                } else if ("Ophim".equals(name)) {
                    // Thiết lập listener cho sự kiện tìm kiếm
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            // Khi người dùng nhấn vào nút tìm kiếm trên bàn phím
                            // Hiển thị nội dung tìm kiếm qua Toast
                            Toast.makeText(MainActivity.this, "Tìm kiếm: " + query, Toast.LENGTH_SHORT).show();

                            // Gọi API tìm kiếm với từ khóa và giới hạn 10 kết quả
                            apiService.searchMoviesOphim(query, 10).enqueue(new Callback<DSResponseOphim>() {
                                @Override
                                public void onResponse(Call<DSResponseOphim> call, Response<DSResponseOphim> response) {
                                    if (response.isSuccessful() && response.body() != null) {
                                        List<DSPhimAPiOphim> seriesOphim = response.body().getData().getItems();
                                        // Nếu có kết quả, hiển thị kết quả tìm kiếm
                                        if (!seriesOphim.isEmpty()) {
                                            binding.dsPhimTimKiem.setVisibility(View.VISIBLE);
                                            binding.recyclerTimKiem.setVisibility(View.VISIBLE);
                                            binding.recyclerViewMovies.setVisibility(View.GONE);
                                            binding.dsPhim.setVisibility(View.GONE);
                                            binding.recyclerTimKiem.setAdapter(new DSPhimAdapterOphim(MainActivity.this, seriesOphim));
                                        } else {
                                            binding.dsPhimTimKiem.setVisibility(View.GONE);
                                            binding.recyclerTimKiem.setVisibility(View.GONE);
                                            binding.recyclerViewMovies.setVisibility(View.GONE);
                                            binding.dsPhim.setVisibility(View.GONE);
                                            Toast.makeText(MainActivity.this, "Không tìm thấy phim", Toast.LENGTH_SHORT).show();
                                        }

                                        // Đóng SearchView sau khi tìm kiếm
                                        searchView.clearFocus();
                                    } else {
                                        binding.dsPhimTimKiem.setVisibility(View.GONE);
                                        binding.recyclerTimKiem.setVisibility(View.GONE);
                                        binding.recyclerViewMovies.setVisibility(View.GONE);
                                        binding.dsPhim.setVisibility(View.GONE);
                                        Toast.makeText(MainActivity.this, "Không tìm thấy phim", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onFailure(Call<DSResponseOphim> call, Throwable t) {
                                    binding.dsPhimTimKiem.setVisibility(View.GONE);
                                    binding.recyclerTimKiem.setVisibility(View.GONE);
                                    binding.recyclerViewMovies.setVisibility(View.GONE);
                                    binding.dsPhim.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this, "Lỗi khi tìm kiếm", Toast.LENGTH_SHORT).show();
                                }
                            });

                            //searchView.clearFocus();
                            searchItem.collapseActionView();

                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            // Xử lý khi nội dung tìm kiếm thay đổi (nếu cần)
                            return false;
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, MainActivity.this); // Thêm MainActivity.this làm Context);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            // Xử lý sự kiện khi nhấn vào tìm kiếm
            Toast.makeText(this, "Bạn muốn tìm kiếm gì", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.nav_thongbao) {
            Intent intent = new Intent(MainActivity.this,ThongBaoActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerViews() {
        binding.recyclerViewMovies.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recyclerTimKiem.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewSeries.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewtvShow.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewphimle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewphimhoathinh.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewphim.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

    }
    private void fetchMoviesFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Movies");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                movieList.clear(); // Xóa dữ liệu cũ (nếu có)
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Lấy dữ liệu từ snapshot
                    QLPhim movie = snapshot.getValue(QLPhim.class);
                    if (movie != null) {
                        // Kiểm tra và thêm vào danh sách
                        movieList.add(movie);
                    }
                }
                // Cập nhật RecyclerView sau khi có dữ liệu
                phimAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("MainActivity", "Failed to fetch data", databaseError.toException());
            }
        });
    }


    private void loadSeries() {
        int page = 1;

        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
            @Override
            public void onBaseUrlFetched(String name, String url) {
                if ("Kkphim".equals(name)) {
                    // Gọi API của Kkphim
                    apiService.getSeries(page).enqueue(new Callback<DSPhimResponse>() {
                        @Override
                        public void onResponse(Call<DSPhimResponse> call, Response<DSPhimResponse> response) {
                            handleKkphimResponse(response, "Series");
                        }

                        @Override
                        public void onFailure(Call<DSPhimResponse> call, Throwable t) {
                            handleError();
                        }
                    });
                } else if ("Ophim".equals(name)) {
                    // Gọi API của Ophim
                    apiService.getSeriesOphim(page).enqueue(new Callback<DSResponseOphim>() {
                        @Override
                        public void onResponse(Call<DSResponseOphim> call, Response<DSResponseOphim> response) {
                            handleOphimResponse(response, "Series");
                        }

                        @Override
                        public void onFailure(Call<DSResponseOphim> call, Throwable t) {
                            handleError();
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, MainActivity.this); // Thêm MainActivity.this làm Context);
    }

    private void loadTVShow() {
        int page = 1;

        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
            @Override
            public void onBaseUrlFetched(String name, String url) {
                if ("Kkphim".equals(name)) {
                    // Gọi API của Kkphim
                    apiService.getTVShow(page).enqueue(new Callback<DSPhimResponse>() {
                        @Override
                        public void onResponse(Call<DSPhimResponse> call, Response<DSPhimResponse> response) {
                            handleKkphimResponse(response, "TVShow");
                        }

                        @Override
                        public void onFailure(Call<DSPhimResponse> call, Throwable t) {
                            handleError();
                        }
                    });
                } else if ("Ophim".equals(name)) {
                    // Gọi API của Ophim
                    apiService.getTVShowOphim(page).enqueue(new Callback<DSResponseOphim>() {
                        @Override
                        public void onResponse(Call<DSResponseOphim> call, Response<DSResponseOphim> response) {
                            handleOphimResponse(response, "TVShow");
                        }

                        @Override
                        public void onFailure(Call<DSResponseOphim> call, Throwable t) {
                            handleError();
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, MainActivity.this); // Thêm MainActivity.this làm Context);
    }

    private void loadPhimHoatHinh() {
        int page = 1;

        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
            @Override
            public void onBaseUrlFetched(String name, String url) {
                if ("Kkphim".equals(name)) {
                    // Gọi API của Kkphim
                    apiService.getHoatHinh(page).enqueue(new Callback<DSPhimResponse>() {
                        @Override
                        public void onResponse(Call<DSPhimResponse> call, Response<DSPhimResponse> response) {
                            handleKkphimResponse(response, "PhimHoatHinh");
                        }

                        @Override
                        public void onFailure(Call<DSPhimResponse> call, Throwable t) {
                            handleError();
                        }
                    });
                } else if ("Ophim".equals(name)) {
                    // Gọi API của Ophim
                    apiService.getHoatHinhOphim(page).enqueue(new Callback<DSResponseOphim>() {
                        @Override
                        public void onResponse(Call<DSResponseOphim> call, Response<DSResponseOphim> response) {
                            handleOphimResponse(response, "PhimHoatHinh");
                        }

                        @Override
                        public void onFailure(Call<DSResponseOphim> call, Throwable t) {
                            handleError();
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, MainActivity.this); // Thêm MainActivity.this làm Context);
    }

    private void loadPhimLe() {
        int page = 1;

        // Kiểm tra API đã chọn từ Firebase hoặc SharedPreferences (tuỳ cách bạn lưu API đã chọn)
        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
            @Override
            public void onBaseUrlFetched(String name, String url) {
                if ("Kkphim".equals(name)) {
                    // Gọi API của Kkphim
                    apiService.getPhimLe(page).enqueue(new Callback<DSPhimResponse>() {
                        @Override
                        public void onResponse(Call<DSPhimResponse> call, Response<DSPhimResponse> response) {
                            handleKkphimResponse(response, "PhimLe");
                        }

                        @Override
                        public void onFailure(Call<DSPhimResponse> call, Throwable t) {
                            handleError();
                        }
                    });
                } else if ("Ophim".equals(name)) {
                    // Gọi API của Ophim
                    apiService.getPhimLeOphim(page).enqueue(new Callback<DSResponseOphim>() {
                        @Override
                        public void onResponse(Call<DSResponseOphim> call, Response<DSResponseOphim> response) {
                            handleOphimResponse(response, "PhimLe");
                        }

                        @Override
                        public void onFailure(Call<DSResponseOphim> call, Throwable t) {
                            handleError();
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, MainActivity.this); // Thêm MainActivity.this làm Context);
    }

    private void navigationBottom() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Đặt item mặc định được chọn là màn hình Home
        bottomNavigationView.setSelectedItemId(R.id.nav_home);

        // Xử lý sự kiện chọn item của Bottom Navigation
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;
                if (item.getItemId() == R.id.nav_home) {
                    return true;
                } else if (item.getItemId() == R.id.nav_vip) {
                    intent = new Intent(MainActivity.this, VipActivity.class);
                }else if(item.getItemId() == R.id.nav_profile) {
                    intent = new Intent(MainActivity.this, CaNhanActivity.class);
                }else if (item.getItemId() == R.id.nav_download) {
                    intent = new Intent(MainActivity.this, TaiPhimActivity.class);
                }

                // Pass the selected item to the new Activity
                if (intent != null) {
                    intent.putExtra("selected_item_id", item.getItemId());
                    startActivity(intent);
                    overridePendingTransition(0, 0);  // No animation for smooth transition
                    return true;
                }
                return false;

            }
        });
    }
    // Hàm cập nhật URL và tải lại dữ liệu
    public void loaDuLieuApiKhiThayDoi() {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("api_sources/selected_source/url");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String newUrl = dataSnapshot.getValue(String.class);
                    // Lưu URL mới vào SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("url", newUrl);
                    editor.apply();

                    loadBaseUrl(newUrl); // Tải lại dữ liệu với URL mới
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FirebaseError", "Error fetching URL: ", databaseError.toException());
            }
        });
    }
    // Hàm lấy URL từ SharedPreferences
    private void loadBaseUrl(String url) {
        ApiClient.setBaseUrl(url); // Update base URL for the API
        apiService = ApiClient.createService(ApiService.class); // Reinitialize apiService

        // Reload all sections with the new API
        loadSeries();
        loadTVShow();
        loadPhimLe();
        loadPhimHoatHinh();

        ApiClient.fetchBaseUrlFromFirebase(new ApiClient.OnBaseUrlFetchListener() {
            @Override
            public void onBaseUrlFetched(String name, String url) {
                if ("Kkphim".equals(name)) {
                    hienThiBanner(); // Gọi hàm Banner
                } else if ("Ophim".equals(name)) {
                    hienThiBannerOphim(); // Gọi hàm BannerOphim
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(MainActivity.this, "Lỗi khi lấy URL: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, MainActivity.this); // Thêm MainActivity.this làm Context);

    }
    // Xử lý response của Kkphim
    private void handleKkphimResponse(Response<DSPhimResponse> response, String phimType) {
        binding.loadingLayout.setVisibility(View.GONE);
        binding.mainContent.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false); // Ngừng loading

        if (response.isSuccessful() && response.body() != null) {
            List<DSPhim> items = response.body().getData().getItems();

            switch (phimType) {
                case "PhimLe":
                    DSKkphimPhimLe = items;
                    binding.recyclerViewphimle.setAdapter(new DSPhimAdapter(MainActivity.this, DSKkphimPhimLe));
                    break;
                case "Series":
                    DSKkphimBo = items;
                    binding.recyclerViewSeries.setAdapter(new DSPhimAdapter(MainActivity.this,DSKkphimBo));
                    break;
                case "TVShow":
                    DSKkphimTvShow = items;
                    binding.recyclerViewtvShow.setAdapter(new DSPhimAdapter(MainActivity.this,DSKkphimTvShow));
                    break;
                case "PhimHoatHinh":
                    DSKkphimHoatHinh = items;
                    binding.recyclerViewphimhoathinh.setAdapter(new DSPhimAdapter(MainActivity.this, DSKkphimHoatHinh));
                    break;
            }
        }
    }

    private void handleOphimResponse(Response<DSResponseOphim> response, String phimType) {
        binding.loadingLayout.setVisibility(View.GONE);
        binding.mainContent.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(false); // Ngừng loading

        if (response.isSuccessful() && response.body() != null) {
            List<DSPhimAPiOphim> items = response.body().getData().getItems();

            switch (phimType) {
                case "PhimLe":
                    DSOphimLe = items;
                    binding.recyclerViewphimle.setAdapter(new DSPhimAdapterOphim(MainActivity.this, DSOphimLe));
                    break;
                case "Series":
                    DSOphimBo = items;
                    binding.recyclerViewSeries.setAdapter(new DSPhimAdapterOphim(MainActivity.this, DSOphimBo));
                    break;
                case "TVShow":
                    DSOphimTvShow = items;
                    binding.recyclerViewtvShow.setAdapter(new DSPhimAdapterOphim(MainActivity.this, DSOphimTvShow));
                    break;
                case "PhimHoatHinh":
                    DSOphimHoatHinh = items;
                    binding.recyclerViewphimhoathinh.setAdapter(new DSPhimAdapterOphim(MainActivity.this, DSOphimHoatHinh));
                    break;
            }
        }
    }

    private void handleError() {
        binding.loadingLayout.setVisibility(View.GONE);
        Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
        binding.mainContent.setVisibility(View.VISIBLE); // Hiển thị lại nội dung chính
        swipeRefreshLayout.setRefreshing(false); // Ngừng loading
    }

    private void hienThiBanner() {
        dungBanner();
        apiService.getMovies(1).enqueue(new Callback<PhimResponse>() {
            @Override
            public void onResponse(Call<PhimResponse> call, Response<PhimResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Phim> movies = response.body().getItems();
                    Log.d("API Response", "Đã lấy được danh sách phim: " + movies.size());

                    // Thiết lập banner với danh sách phim
                    thietLapBannerViewPager(movies);
                } else {
                    Log.e("API Error", "Phản hồi không thành công hoặc nội dung là null");
                }
            }

            @Override
            public void onFailure(Call<PhimResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu banner", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hienThiBannerOphim() {
        dungBanner();
        apiService.getMoviesOphim(1).enqueue(new Callback<PhimResponseOphim>() {
            @Override
            public void onResponse(Call<PhimResponseOphim> call, Response<PhimResponseOphim> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PhimAPiOphim> movie = response.body().getItems();
                    Log.d("API Response", "Đã lấy được danh sách phim: " + movie.size());

                    // Thiết lập banner với danh sách phim
                    thietLapBannerViewPagerOphim(movie);
                } else {
                    Log.e("API Error", "Phản hồi không thành công hoặc nội dung là null");
                }
            }

            @Override
            public void onFailure(Call<PhimResponseOphim> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Lỗi khi tải dữ liệu banner", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void thietLapBannerViewPager(List<Phim> movies) {
        binding.viewPagerBanner.setAdapter(new BannerAdapter(this, movies));
        bannerRunnable = new Runnable() {
            private int trangHienTai = 0;

            @Override
            public void run() {
                if (movies != null && !movies.isEmpty()) {
                    binding.viewPagerBanner.setCurrentItem(trangHienTai, true);
                    trangHienTai = (trangHienTai + 1) % movies.size();
                }
                bannerHandler.postDelayed(this, 4000); // Lặp lại mỗi 4 giây
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 4000);
    }

    private void thietLapBannerViewPagerOphim(List<PhimAPiOphim> movies) {
        binding.viewPagerBanner.setAdapter(new BannerOphimAdapter(this, movies));
        bannerRunnable = new Runnable() {
            private int trangHienTai = 0;

            @Override
            public void run() {
                if (movies != null && !movies.isEmpty()) {
                    binding.viewPagerBanner.setCurrentItem(trangHienTai, true);
                    trangHienTai = (trangHienTai + 1) % movies.size();
                }
                bannerHandler.postDelayed(this, 4000); // Lặp lại mỗi 4 giây
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 4000);
    }

    private void dungBanner() {
        // Xóa runnable hiện tại khỏi handler nếu có
        if (bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        // Giữ màn hình sáng khi ứng dụng hoạt động
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Xóa cờ giữ màn hình sáng khi ứng dụng không còn hoạt động
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}