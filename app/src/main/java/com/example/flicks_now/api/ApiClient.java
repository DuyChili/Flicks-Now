package com.example.flicks_now.api;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static String baseUrl = "https://phimapi.com/";

    private static Retrofit retrofit;

    public static Retrofit getClient() {
        // Kiểm tra xem baseUrl có giá trị hợp lệ không
        if (baseUrl == null || (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://"))) {
            throw new IllegalArgumentException("Base URL không hợp lệ: " + baseUrl);
        }
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    // Phương thức lấy URL từ Firebase
    public static void fetchBaseUrlFromFirebase(final OnBaseUrlFetchListener listener, Context context) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("api_sources").child("selected_source");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String newUrl = snapshot.child("url").getValue(String.class);
                String source = snapshot.child("name").getValue(String.class); // Lấy name của API

                if (newUrl != null && !newUrl.isEmpty() && source != null) {
                    setBaseUrl(newUrl);
                    listener.onBaseUrlFetched(source, newUrl); // Gọi callback với name và URL
                } else {
                    listener.onError("URL hoặc name không tồn tại");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }
    public static <S> S createService(Class<S> serviceClass) {
        if (retrofit == null || !retrofit.baseUrl().toString().equals(baseUrl)) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(serviceClass);
    }
    public interface OnBaseUrlFetchListener {
        void onBaseUrlFetched(String name, String url); // Thêm name vào callback
        void onError(String errorMessage);
    }
    // Phương thức thay đổi baseUrl
    public static void setBaseUrl(String newBaseUrl) {
        baseUrl = newBaseUrl;
        retrofit = null; // Đặt lại retrofit để sử dụng baseUrl mới
    }
}

