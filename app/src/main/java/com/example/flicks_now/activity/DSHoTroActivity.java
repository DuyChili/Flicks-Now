package com.example.flicks_now.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.flicks_now.adapter.HoTroAdapter;
import com.example.flicks_now.model.HoTro;
import com.example.flicks_now.databinding.ActivityDsHoTroBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DSHoTroActivity extends AppCompatActivity {
    private ActivityDsHoTroBinding binding;
    private HoTroAdapter hoTroAdapter;
    private List<HoTro> hoTroList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDsHoTroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());




        hoTroAdapter = new HoTroAdapter(DSHoTroActivity.this,hoTroList);
        binding.recyclerViewApis.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewApis.setAdapter(hoTroAdapter);

        binding.btnBack.setOnClickListener(v -> onBackPressed());
        getHoTroFromDatabase();
    }
    private void getHoTroFromDatabase() {
        DatabaseReference hoTroRef = FirebaseDatabase.getInstance().getReference("HoTro");

        hoTroRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hoTroList.clear(); // Clear the list before adding new items
                for (DataSnapshot hoTroSnapshot : snapshot.getChildren()) {
                    HoTro hoTro = hoTroSnapshot.getValue(HoTro.class);
                    if (hoTro != null) {
                        Log.d("HoTroData", "Loaded HoTro ID: " + hoTro.getUserId()); // Log loaded ID
                        hoTroList.add(0,hoTro); // Add to list if HoTro is not null
                    } else {
                        Log.d("HoTroData", "HoTro is null for key: " + hoTroSnapshot.getKey());
                    }
                }
                hoTroAdapter.notifyDataSetChanged(); // Update adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DSHoTroActivity.this, "Error fetching HoTro: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // Giữ màn hình sáng khi hoạt động
    }

    @Override
    protected void onPause() {
        super.onPause();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);  // Tắt giữ màn hình sáng khi dừng hoạt động
    }
}