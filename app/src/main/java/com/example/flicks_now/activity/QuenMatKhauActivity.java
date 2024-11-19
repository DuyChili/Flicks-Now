package com.example.flicks_now.activity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.databinding.ActivityQuenMatKhauBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

public class QuenMatKhauActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private ActivityQuenMatKhauBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityQuenMatKhauBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("Users"); // Lấy tham chiếu đến Realtime Database
        //binding.resetPasswordButton.setOnClickListener(v->{resetPassword();});
    }

//    private void resetPassword() {
//        String body = "Here is your new password: 123456";
//        String email = binding.edtEmail.getText().toString().trim(); // Dùng binding để lấy giá trị
//
//        if (email.isEmpty()) {
//            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Kiểm tra email có tồn tại trong Realtime Database
//        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                boolean emailFound = false;
//
//                // Duyệt qua tất cả các người dùng trong Realtime Database
//                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
//                    String storedEmail = userSnapshot.child("email").getValue(String.class);
//
//                    if (storedEmail.equals(email)) {
//                        // Nếu tìm thấy email, tiến hành lấy mật khẩu và thực hiện reset
//                        emailFound = true;
//                        String password = userSnapshot.child("password").getValue(String.class);
//
//                        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(signInTask -> {
//                            if (signInTask.isSuccessful()) {
//                                String newPassword = generateRandomPassword(); // Tạo mật khẩu mới ngẫu nhiên
//                                FirebaseUser user = mAuth.getCurrentUser();
//                                if (user != null) {
//                                    // Cập nhật mật khẩu mới trong FirebaseAuth
//                                    user.updatePassword(newPassword).addOnCompleteListener(updateTask -> {
//                                        if (updateTask.isSuccessful()) {
//                                            // Cập nhật mật khẩu mới trong Realtime Database
//                                            mDatabase.child(userSnapshot.getKey()).child("password").setValue(newPassword)
//                                                    .addOnCompleteListener(dbUpdateTask -> {
//                                                        if (dbUpdateTask.isSuccessful()) {
//                                                            sendEmailWithSendGrid(email,newPassword,body);
//                                                            //sendEmailWithNewPassword(email, newPassword); // Gửi mật khẩu mới qua email
//                                                        } else {
//                                                            Toast.makeText(QuenMatKhauActivity.this, "Lỗi cập nhật mật khẩu trong cơ sở dữ liệu.", Toast.LENGTH_SHORT).show();
//                                                        }
//                                                    });
//                                        } else {
//                                            Toast.makeText(QuenMatKhauActivity.this, "Lỗi cập nhật mật khẩu", Toast.LENGTH_SHORT).show();
//                                        }
//                                    });
//                                }
//                            } else {
//                                Toast.makeText(QuenMatKhauActivity.this, "Không thể đăng nhập. Kiểm tra thông tin tài khoản.", Toast.LENGTH_SHORT).show();
//                            }
//                        });
//                        break; // Dừng vòng lặp khi tìm thấy email
//                    }
//                }
//
//                if (!emailFound) {
//                    Toast.makeText(QuenMatKhauActivity.this, "Email không tồn tại trong hệ thống.", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(QuenMatKhauActivity.this, "Lỗi khi kiểm tra email.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }


    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        Random random = new Random();
        while (password.length() < 6) { // 6 ký tự
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }

//    private void sendEmailWithNewPassword(String email, String newPassword) {
//        final String senderEmail = email; // Thay bằng email của bạn
//        final String senderPassword = newPassword; // Thay bằng mật khẩu email
//
//        Properties props = new Properties();
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.port", "587");
//
//        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(senderEmail, senderPassword);
//            }
//        });
//
//        try {
//            // Tạo message email
//            Message message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(senderEmail));
//            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
//            message.setSubject("Mật khẩu mới của bạn");
//            message.setText("Mật khẩu mới của bạn là: " + newPassword);
//
//            // Gửi email
//            Transport.send(message);
//
//            Toast.makeText(this, "Đã gửi mật khẩu mới về email của bạn.", Toast.LENGTH_SHORT).show();
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Lỗi khi gửi email", Toast.LENGTH_SHORT).show();
//        }
//    }
//public void sendEmailWithSendGrid(String toEmail, String subject, String body) {
//    String apiKey = "SENDGRID_API_KEY";  // Thay bằng API Key của bạn
//    String fromEmail = toEmail; // Địa chỉ email người gửi
//
//    OkHttpClient client = new OkHttpClient();
//
//    // Tạo JSON body
//    JSONObject json = new JSONObject();
//    try {
//        json.put("personalizations", new JSONArray()
//                        .put(new JSONObject().put("to", new JSONArray().put(new JSONObject().put("email", toEmail)))))
//                .put("subject", subject);
//        json.put("from", new JSONObject().put("email", fromEmail));
//        json.put("content", new JSONArray()
//                .put(new JSONObject().put("type", "text/plain").put("value", body)));
//
//        RequestBody requestBody = RequestBody.create(
//                json.toString(), MediaType.parse("application/json"));
//
//        Request request = new Request.Builder()
//                .url("https://api.sendgrid.com/v3/mail/send")
//                .post(requestBody)
//                .addHeader("Authorization", "Bearer " + apiKey)
//                .build();
//
//        // Gửi yêu cầu
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                // Xử lý lỗi gửi email
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    // Email đã được gửi thành công
//                    Toast.makeText(QuenMatKhauActivity.this, "gưi email thành công.", Toast.LENGTH_SHORT).show();
//                } else {
//                    // Xử lý khi gửi email thất bại
//                    Toast.makeText(QuenMatKhauActivity.this, "Lỗi khi gưửi email.", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//    } catch (Exception e) {
//        e.printStackTrace();
//    }
//}
}
