package com.example.jerye.errand.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.jerye.errand.R;
import com.example.jerye.errand.utility.GestureUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by jerye on 3/20/2017.
 */

public class ErrandAdapter
        extends RecyclerView.Adapter<ErrandAdapter.ErrandViewHolder>
        implements GestureUtility.ItemTouchHelperAdapter {
    List<String> locationList = new ArrayList<>();
    Cursor mCursor;
    private ErrandAdapterClickHandler mErrandAdapterClickHandler;
    private Context mContext;
    private static final String TAG = "ErrandAdapter.java";

    public ErrandAdapter(Context context, ErrandAdapterClickHandler clickHandler) {
        mContext = context;
        mErrandAdapterClickHandler = clickHandler;
    }


    @Override
    public ErrandViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d("ErrandAdapter", "create");

        View itemView = LayoutInflater.from(mContext).inflate(R.layout.drawer_item, parent, false);
        return new ErrandViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ErrandViewHolder holder, int position) {
//        mCursor.moveToPosition(position);
//        holder.textView.setText(mCursor.getString(ErrandDBHelper.COLUMN_ID_LOCATION_NAME));
        holder.textView.setText(locationList.get(position));
        Log.d("ErrandAdapter", "bind");
    }


    public void refreshList(Cursor cursor) {
        Log.d(TAG, "ErrandAdapter refreshed");
        mCursor = cursor;
        locationList.clear();
        while (mCursor.moveToNext()) {
            locationList.add(mCursor.getString(ErrandDBHelper.COLUMN_ID_LOCATION_NAME));
        }
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        int count = 0;
        if (mCursor != null) {
            count = mCursor.getCount();
        }
        return count;
    }

    public class ErrandViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.drawer_item)
        TextView textView;

        ErrandViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mErrandAdapterClickHandler.onClick(getAdapterPosition());
        }
    }

    public interface ErrandAdapterClickHandler {
        void onClick(int position);
    }

    @Override
    public void onItemDrag(int from, int to) {
        int sign;

        if(from < to){
            sign = 1;
        }else{
            sign = -1;
        }

        for(int i = from; i* sign < to * sign; i = i+sign){
            Collections.swap(locationList, i , i+sign);
        }

        notifyItemMoved(from, to);
    }

    @Override
    public void onItemSwipe(int position) {
        locationList.remove(position);
        notifyItemRemoved(position);
    }
}
