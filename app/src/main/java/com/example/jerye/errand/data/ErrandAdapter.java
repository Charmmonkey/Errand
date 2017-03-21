package com.example.jerye.errand.data;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jerye.errand.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jerye on 3/20/2017.
 */

public class ErrandAdapter extends RecyclerView.Adapter<ErrandAdapter.ErrandViewHolder>{
    List<String> destinationList;
    private Context mContext;

    public ErrandAdapter(Context context){
        mContext = context;
    }


    @Override
    public ErrandViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("ErrandAdapter", "create");

        View itemView = LayoutInflater.from(mContext).inflate(R.layout.drawer_item, parent, false );
        return new ErrandViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ErrandViewHolder holder, int position) {
        holder.textView.setText(destinationList.get(position));
        Log.d("ErrandAdapter", "bind");
    }


    public void refreshList(List<String> stringList){
        destinationList = stringList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        int count = 0;
        if(destinationList != null){
            count = destinationList.size();
        }
        return count;
    }

    public class ErrandViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.drawer_item)
        TextView textView;

        ErrandViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
     }
}
