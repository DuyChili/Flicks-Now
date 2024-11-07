package com.example.flicks_now.adapter;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

import com.example.flicks_now.activity.ChiTietPhimActivity;
import com.example.flicks_now.model.DSPhim;
import com.example.flicks_now.databinding.ItemPhimBinding;

public class DSPhimAdapter extends RecyclerView.Adapter<DSPhimAdapter.DSPhimViewHolder>{
    private Activity context;  // Thêm biến Activity context
    private List<DSPhim> dsPhims;
    private static OnRecyclerViewItemClickListener recyclerViewItemClickListener;

    // Constructor
    public DSPhimAdapter(Activity context, List<DSPhim> dsPhims) {
        this.context = context;  // Khởi tạo biến context
        this.dsPhims = dsPhims;
    }

    // Setter cho listener
    public void setRecyclerViewItemClickListener(OnRecyclerViewItemClickListener recyclerViewItemClickListener) {
        DSPhimAdapter.recyclerViewItemClickListener = recyclerViewItemClickListener;
    }

    @NonNull
    @Override
    public DSPhimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPhimBinding binding = ItemPhimBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DSPhimViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DSPhimViewHolder holder, int position) {
        DSPhim dsPhim = dsPhims.get(position);
        holder.binding.movieTitle.setText(dsPhim.getName());
        holder.binding.movieYear.setText(String.valueOf(dsPhim.getYear()));

        // Sử dụng Glide để load hình ảnh
        Glide.with(context)  // Sử dụng context đã được cung cấp
                .load(dsPhim.getPosterUrl())
                .into(holder.binding.moviePoster);

        /// Luu Position mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return dsPhims.size();
    }

    // ViewHolder class
    public class DSPhimViewHolder extends RecyclerView.ViewHolder {
        ItemPhimBinding binding;
        int position;

        public DSPhimViewHolder(@NonNull ItemPhimBinding binding) {
            super(binding.getRoot());
            this.binding = binding;


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Lay thong tin chi tiet phim tu slug truyen den man hinh chi tiet phim
                    Intent intent = new Intent(view.getContext(), ChiTietPhimActivity.class);
                    DSPhim dsPhim = dsPhims.get(position);
                    intent.putExtra("slug", dsPhim.getSlug());
                    view.getContext().startActivity(intent);
                }
            });


        }

    }

    // Interface để xử lý sự kiện click
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }
}
