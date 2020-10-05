package com.example.eventbooking.controller.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventbooking.R;
import com.example.eventbooking.model.MoreModel;
import com.example.eventbooking.view.activities.FeedbacksActivity;
import com.example.eventbooking.view.activities.MyBookingsActivity;
import com.example.eventbooking.view.activities.RecordDetailsActivity;
import com.example.eventbooking.view.activities.RootActivity;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class MoreAdapter extends RecyclerView.Adapter<MoreAdapter.ViewModel> {

    private List<MoreModel> mList;
    private Context mContext;

    public MoreAdapter(List<MoreModel> list, Context context) {
        this.mList = list;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewModel onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.more_single_item, parent, false);
        return new ViewModel(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewModel holder, final int position) {
        final MoreModel model = mList.get(position);
        holder.textView.setText(model.getTitle());
        holder.imageView.setImageResource(model.getImage());
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (position) {
                    case 0:
                        mContext.startActivity(new Intent(mContext, MyBookingsActivity.class));
                        break;
                    case 1:
                        mContext.startActivity(new Intent(mContext, RecordDetailsActivity.class));
                        break;
                    case 3:
                        mContext.startActivity(new Intent(mContext, FeedbacksActivity.class));
                        break;
                    case 5:
                        logout();
                        break;
                        default:
                            break;
                }
            }
        });
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(mContext, RootActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public class ViewModel extends RecyclerView.ViewHolder {
        View mMainView;
        ImageView imageView;
        TextView textView;
        CardView mainView;
        public ViewModel(@NonNull View itemView) {
            super(itemView);
            mMainView = itemView;
            imageView = mMainView.findViewById(R.id.menuImage);
            textView = mMainView.findViewById(R.id.txtMenu);
            mainView = mMainView.findViewById(R.id.btnMenu);
        }
    }
}
