package  com.example.flicks_now.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flicks_now.databinding.ItemTapphimBinding;
import com.example.flicks_now.model.ChiTietPhim;

import java.util.List;

public class TapPhimAdapter extends RecyclerView.Adapter<TapPhimAdapter.ViewHolder> {
    private Activity context;
    private List<ChiTietPhim.TapPhim.DuLieuServer> listTapPhim;
    private static OnRecyclerViewItemClickListener recyclerViewItemClickListener;

    public TapPhimAdapter(Activity context, List<ChiTietPhim.TapPhim.DuLieuServer> listTapPhim) {
        this.listTapPhim = listTapPhim;
        this.context = context;
    }
    public void setRecyclerViewItemClickListener(OnRecyclerViewItemClickListener listener) {
        recyclerViewItemClickListener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTapphimBinding binding = ItemTapphimBinding.inflate(context.getLayoutInflater(), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietPhim.TapPhim.DuLieuServer tapphim = listTapPhim.get(position);
        holder.binding.tvTapPhim.setText(tapphim.getName()); // Thiết lập tên tập phim cho đúng mục

        /// Luu Position mới cho Holder
        final int pos = position;
        holder.position = pos;
    }

    @Override
    public int getItemCount() {
        return listTapPhim.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemTapphimBinding binding;
        int position;
        public ViewHolder(@NonNull ItemTapphimBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    recyclerViewItemClickListener.onItemClick(view, position);
                }
            });
        }
    }
    // Interface để xử lý sự kiện click
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, int position);
    }

}
