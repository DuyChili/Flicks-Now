package com.example.flicks_now.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.flicks_now.databinding.ActivityVeChungToiBinding;

public class VeChungToiActivity extends AppCompatActivity {
    private ActivityVeChungToiBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo binding
        binding = ActivityVeChungToiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Thiết lập toolbar
        setupToolbar();

        // Cập nhật nội dung
        updateContent();
        setSupportActionBar(binding.toolbar);
        // Kiểm tra xem ActionBar đã được khởi tạo chưa
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Về chúng tôi"); // Đặt tên mới cho Toolbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiện biểu tượng trở về
        }
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Về chúng tôi");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void updateContent() {

        // Cập nhật nội dung mô tả
        binding.tvDescription.setText("Trong bối cảnh công nghệ phát triển nhanh chóng và nhu cầu giải trí ngày càng đa dạng, việc tiếp cận nội dung giải trí không còn bị giới hạn bởi vị trí địa lý hay thời gian. Ứng dụng FlicksNow  ra đời như một giải pháp hoàn hảo, đáp ứng yêu cầu của người dùng hiện đại về một nền tảng xem phim trực tuyến tiện lợi, chất lượng và phong phú về nội dung.\n" +
                "FlicksNow là nền tảng xem phim online được thiết kế nhằm mang đến cho người dùng những trải nghiệm xem phim tốt nhất, từ các bộ phim điện ảnh đình đám đến loạt phim truyền hình ăn khách và nội dung gốc độc quyền. Với sự hỗ trợ của công nghệ hiện đại, ứng dụng giúp người dùng có thể truy cập kho phim đồ sộ ở bất kỳ đâu, trên bất kỳ thiết bị nào có kết nối Internet, chỉ với vài thao tác đơn giản.\n" +
                "Trong vài năm trở lại đây, xu hướng xem phim trực tuyến đã trở thành thói quen phổ biến trên toàn cầu. Sự gia tăng về nhu cầu giải trí tại nhà, kết hợp với sự phát triển mạnh mẽ của các thiết bị di động thông minh, đã mở ra cơ hội lớn cho các dịch vụ phát trực tuyến. Người dùng ngày càng ưu tiên các ứng dụng cho phép truy cập nội dung theo yêu cầu, thay vì phải chờ đợi các lịch phát sóng truyền thống.\n" +
                "Chính vì vậy, FlicksNow ra đời với sứ mệnh cung cấp một nền tảng xem phim không chỉ đáp ứng được nhu cầu giải trí mà còn tạo ra sự khác biệt thông qua việc cá nhân hóa nội dung, tối ưu hóa trải nghiệm người dùng, và mang đến những giá trị giải trí tốt nhất.\n" +
                "FlicksNow hướng tới việc trở thành nền tảng giải trí số hàng đầu, nơi người dùng có thể dễ dàng tiếp cận một thế giới điện ảnh vô tận chỉ bằng vài cú nhấp chuột. Chúng tôi tin rằng giải trí không chỉ dừng lại ở việc xem phim mà còn là việc kết nối cảm xúc, lan tỏa giá trị văn hóa và thỏa mãn nhu cầu khám phá nghệ thuật của người dùng.\n" +
                "Ứng dụng cam kết tạo ra không gian giải trí với tầm nhìn lâu dài: không chỉ dừng lại ở việc cung cấp nội dung chất lượng cao mà còn xây dựng một hệ sinh thái giải trí toàn diện, nơi người dùng có thể tương tác, chia sẻ và trải nghiệm những giá trị văn hóa khác nhau qua từng bộ phim.\n" +
                "Các tính năng nổi bật của ứng dụng\n" +
                "Kho nội dung phong phú và đa dạng:\n" +
                "\tFlicksNow sở hữu một thư viện khổng lồ với hàng ngàn bộ phim thuộc nhiều thể loại: từ phim điện ảnh bom tấn, phim truyền hình dài tập, đến các phim tài liệu và nội dung gốc độc quyền. Ứng dụng luôn cập nhật nội dung mới nhất mỗi ngày để đảm bảo người dùng không bỏ lỡ bất kỳ tác phẩm nổi bật nào.\n" +
                "Chất lượng hình ảnh vượt trội:\n" +
                "\tFlicksNow hỗ trợ độ phân giải cao, mang lại trải nghiệm xem phim chân thực và sắc nét. Người dùng có thể tùy chọn chất lượng phát sóng phù hợp với kết nối Internet của mình, giúp đảm bảo quá trình xem phim không bị gián đoạn.\n" +
                "Khả năng cá nhân hóa nội dung:\n" +
                "\tMột trong những điểm mạnh của FlicksNow là hệ thống đề xuất thông minh, dựa trên lịch sử xem phim và sở thích của người dùng. Ứng dụng sử dụng các thuật toán phân tích hành vi, từ đó đề xuất những bộ phim hoặc chương trình phù hợp với từng cá nhân, giúp nâng cao trải nghiệm và tiết kiệm thời gian tìm kiếm.\n" +
                "Xem trên nhiều nền tảng:\n" +
                "\tFlicksNow hỗ trợ đa nền tảng, cho phép người dùng xem phim trên nhiều thiết bị khác nhau như smartphone, tablet. Điều này giúp người dùng có thể thưởng thức những bộ phim yêu thích mọi lúc, mọi nơi mà không bị gián đoạn.\n" +
                "Bảo mật thông tin người dùng:\n" +
                "\tTrong thời đại kỹ thuật số, vấn đề bảo mật luôn được đặt lên hàng đầu. FlicksNow sử dụng các công nghệ mã hóa tiên tiến để đảm bảo an toàn thông tin cá nhân của người dùng. Ứng dụng cũng tuân thủ nghiêm ngặt các quy định về quyền riêng tư, giúp người dùng hoàn toàn yên tâm khi sử dụng dịch vụ.\n" +
                "FlicksNow hướng tới mọi đối tượng người dùng yêu thích điện ảnh và các chương trình giải trí. Từ những người trẻ đam mê công nghệ, những gia đình muốn có giây phút giải trí bên nhau, cho đến những người lớn tuổi tìm kiếm sự thư giãn nhẹ nhàng với những bộ phim kinh điển. Ứng dụng đặc biệt phù hợp với người dùng hiện đại, những người mong muốn có trải nghiệm giải trí không giới hạn về thời gian và không gian.\n" +
                "Trong tương lai, FlicksNow  sẽ tiếp tục mở rộng thư viện nội dung, đồng thời đầu tư vào sản xuất các bộ phim, chương trình gốc độc quyền nhằm mang đến những trải nghiệm độc đáo, khác biệt cho người dùng. Ngoài ra, ứng dụng sẽ nghiên cứu và tích hợp thêm các tính năng mới như tương tác trực tiếp với phim (Interactive Movie), thực tế ảo (VR), và nội dung dành cho nhiều đối tượng người dùng hơn, nhằm mang đến một hệ sinh thái giải trí toàn diện.\n" +
                "Kết luận\n" +
                "FlicksNow  không chỉ đơn thuần là một nền tảng xem phim trực tuyến mà còn là cầu nối giữa người dùng với thế giới điện ảnh đa dạng và phong phú. Với sự tiện lợi, kho nội dung khổng lồ và chất lượng phục vụ hàng đầu, ứng dụng hứa hẹn sẽ trở thành người bạn đồng hành không thể thiếu trong những giờ phút giải trí của mọi gia đình và cá nhân. Thông qua FlicksNow, chúng tôi hy vọng người dùng sẽ có cơ hội trải nghiệm những bộ phim tuyệt vời và tận hưởng những khoảnh khắc giải trí đáng nhớ.\n" +
                "\n" +
                "1.2. Phạm vi của hệ thống:\n" +
                "Phiên bản demo\n" +
                "\tPhiên bản demo của hệ thống chỉ mang mục đích trưng bày, không kết nối với cơ sở dữ liệu và không liên kết với các trang web đối tác. Điều này có nghĩa là hệ thống chỉ cung cấp giao diện và chức năng cơ bản để người dùng trải nghiệm mà không thực hiện các thao tác thực tế với dữ liệu.\n" +
                "Hệ điều hành Android\n" +
                "\tHệ thống hỗ trợ hoạt động trên các thiết bị sử dụng hệ điều hành Android, cụ thể là các phiên bản từ Android 10 đến những phiên bản android về sau. Điều này đảm bảo rằng người dùng có thể cài đặt và sử dụng ứng dụng trên các thiết bị chạy những phiên bản Android này.\n" +
                "Thiết bị thử nghiệm\n" +
                "\tỨng dụng đã được thử nghiệm trên một số thiết bị Android cụ thể, bao gồm Google Pixel 4XL, Sony, Vivo, Xiaomi. Đây là những thiết bị đại diện cho các dòng điện thoại phổ biến trong phạm vi hỗ trợ của hệ thống, đảm bảo tính tương thích và hiệu suất khi sử dụng.\n" +
                "Độ phân giải màn hình Android\n" +
                "\tHệ thống hỗ trợ hai độ phân giải màn hình chính là 1440x3040 và 1080x2520. Điều này đảm bảo rằng ứng dụng có thể hoạt động ổn định và hiển thị giao diện một cách tối ưu trên các thiết bị có độ phân giải này.\n" +
                "Hỗ trợ chế độ màn hình ngang (Landscape)\n" +
                "\tỨng dụng có hỗ trợ hiển thị và hoạt động ở chế độ màn hình ngang. Điều này phù hợp với các yêu cầu hiển thị đặc thù của hệ thống và giúp tối ưu trải nghiệm người dùng khi xem phim hay sử dụng các chức năng khác");

        // Thiết lập click listener cho các thông tin liên hệ
        setupContactClickListeners();
    }

    private void setupContactClickListeners() {
        binding.btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:flicknow78@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Liên hệ từ ứng dụng Flick Now");
            startActivity(Intent.createChooser(intent, "Gửi email"));
        });

        binding.btnCall.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+84848478888"));
            startActivity(intent);
        });

        binding.btnVisit.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://www.flicknow.com"));
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
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