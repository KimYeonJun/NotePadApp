package com.example.notepad;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;
/**
 * imageRecyclerAdapter: 메모 내의 n개의 이미지 출력을 위한 RecyclerView.Adapter
 */
public class imageRecyclerAdapter extends RecyclerView.Adapter<imageRecyclerAdapter.ItemViewHolder> {

    Context mContext;
    SQLiteHelper dbHelper;
    private List<String> listdata;

    public imageRecyclerAdapter(List<String> listdata, Context context) {
        this.listdata = listdata;
        mContext = context;
        dbHelper = new SQLiteHelper(mContext);

    }
    @Override
    public imageRecyclerAdapter.ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.image_list_item,viewGroup,false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(imageRecyclerAdapter.ItemViewHolder itemViewHolder, int i) {
        final int position = i;
        String tUri = listdata.get(i);
        Uri uri = Uri.parse(tUri);
       // Glide.with(mContext).asBitmap().load(uri).into(itemViewHolder.img);
        Glide.with(mContext)
                .load(uri)
                .placeholder(R.drawable.imagenotfound)
                .error(R.drawable.imagenotfound)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(mContext,"외부 이미지 주소가 정확하지 않습니다.",Toast.LENGTH_SHORT).show();
                        listdata.remove(position);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(itemViewHolder.img);

    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }
    void addItem(String bitmap){
        listdata.add(bitmap);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView img;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.image_recyclerview_item);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder
                            .setTitle("삭제")
                            .setMessage("정말로 삭제 하시겠습니까?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton("Yes" , new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int position = getAdapterPosition();
                                    //int deleteSeq = listdata.get(position).getSeq();
                                    listdata.remove(position);
                                    notifyDataSetChanged();
                                    //dbHelper.deleteMemo(deleteSeq);
                                    //Intent intent= new Intent(mContext,MainActivity.class);
                                    //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    //mContext.startActivity(intent);
                                    dialog.dismiss();
                                }
                            })

                            .setNegativeButton("No",null)
                            .show();
                    return false;
                }
            });

        }

    }
}
