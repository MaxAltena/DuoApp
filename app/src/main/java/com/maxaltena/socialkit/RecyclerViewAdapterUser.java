package com.maxaltena.socialkit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class RecyclerViewAdapterUser extends RecyclerView.Adapter<RecyclerViewAdapterUser.ViewHolder> {
    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<String> mUsernames = new ArrayList<>();
    private ArrayList<String> mImages = new ArrayList<>();
    private ArrayList<String> mPlatformNames = new ArrayList<>();
    private ArrayList<String> mPlatformLinks = new ArrayList<>();
    private ArrayList<String> mIds = new ArrayList<>();
    private Context mContext;


    public RecyclerViewAdapterUser(Context mContext, ArrayList<String> mUsernames, ArrayList<String> mImages, ArrayList<String> mPlatformNames, ArrayList<String> mPlatformLinks){
        this.mUsernames = mUsernames;
        this.mImages = mImages;
        this.mContext = mContext;
        this.mPlatformNames = mPlatformNames;
        this.mPlatformLinks = mPlatformLinks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position){
        Log.d(TAG, "onBindViewHolder: called");

        Glide.with(mContext)
                .asBitmap()
                .load(mImages.get(position))
                .into(holder.image);

        holder.username.setText(mUsernames.get(position));

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(mPlatformLinks.get(position) + mUsernames.get(position)));
                    mContext.startActivity(i);
            }

        });
    }

    @Override
    public int getItemCount() {
        return mUsernames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView username;
        RelativeLayout parentLayout;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            image = itemView.findViewById(R.id.image);
            username = itemView.findViewById(R.id.username);
            parentLayout = itemView.findViewById(R.id.social_card);
        }
    }
}
