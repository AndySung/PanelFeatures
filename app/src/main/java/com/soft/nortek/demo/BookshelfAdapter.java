package com.soft.nortek.demo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class BookshelfAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private List<String> mBooks;
    private Context mContext;
    private LayoutInflater mInflater;

    private OnItemClickListener mOnItemClickListener;

    public BookshelfAdapter(Context context, List<String> books){
        this.mContext = context;
        this.mBooks = books;
        this.mInflater = LayoutInflater.from(context);
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.book_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.mTvBookName.setText(mBooks.get(position));
        
        // item click
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            });
        }

        // item long click
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mOnItemClickListener.onItemLongClick(holder.itemView, position);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

}
class MyViewHolder extends RecyclerView.ViewHolder {
    TextView mTvBookName;
    public MyViewHolder(View view) {
        super(view);
        mTvBookName = view.findViewById(R.id.book_name);
    }
}



