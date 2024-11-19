package com.example.flicks_now.model;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;
import java.util.Locale;

public class ThoiGianBL extends PrettyTime {

    public ThoiGianBL() {
        super();
        // Đặt Locale cho tiếng Việt
        this.setLocale(new Locale("vi", "VN"));
    }

    @Override
    public String format(Date date) {
        // Tính toán độ chênh lệch thời gian
        long diffInMillis = new Date().getTime() - date.getTime();
        if (diffInMillis < 5000) {
            return "Vừa xong";
        }
        else if (diffInMillis < 60000) {
            return "Vài giây trước";
        }
        else if (diffInMillis < 3600000) {
            long minutes = diffInMillis / 60000;
            return minutes + " phút trước";
        }
        else if (diffInMillis < 86400000) {
            long hours = diffInMillis / 3600000;
            return hours + " giờ trước";
        }
        else if (diffInMillis < 172800000) {
            return "Ngày hôm qua";
        }
        else if (diffInMillis < 2592000000L) {
            long days = diffInMillis / 86400000;
            return days + " ngày trước";
        }
        else {
            return super.format(date);
        }
    }
}

