package com.example.jerye.errand.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jerye.errand.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jerye on 3/18/2017.
 */

public class ErrandAdapter extends RecyclerView.Adapter<ErrandAdapter.ErrandViewHolder> {
    Context mContext;

    public ErrandAdapter(Context context) {
        mContext = context;

    }


    @Override
    public void onBindViewHolder(ErrandViewHolder holder, int position) {

        holder.textView.setText("hi");


    }

    @Override
    public ErrandViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(mContext).inflate(R.layout.drawer_item, parent, false);

        return new ErrandViewHolder(item);
    }

    @Override
    public int getItemCount() {
        return 0;
    }


    public class ErrandViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.drawer_item)
        TextView textView;


        public ErrandViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(itemView);

        }



    }
}
