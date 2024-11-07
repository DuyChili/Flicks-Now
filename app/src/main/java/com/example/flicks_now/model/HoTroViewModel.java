package com.example.flicks_now.model;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class HoTroViewModel extends AndroidViewModel {
    private static final int PICK_IMAGE = 1;
    public MutableLiveData<Uri> selectedImageUri = new MutableLiveData<>();

    public HoTroViewModel(@NonNull Application application) {
        super(application);
    }

    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplication().startActivity(intent);
    }

    public void setImageUri(Uri uri) {
        selectedImageUri.setValue(uri);
    }
}

