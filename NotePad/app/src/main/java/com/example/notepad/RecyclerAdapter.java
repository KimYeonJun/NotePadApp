package com.example.notepad;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * RecyclerAdapter : n개의 메모 출력을 위한 RecyclerAdapter
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder> {

    Context mContext;
    SQLiteHelper dbHelper;
    private List<Memo> listdata;

    public RecyclerAdapter(List<Memo> listdata, Context context) {
        this.listdata = listdata;
        mContext = context;
        dbHelper = new SQLiteHelper(mContext);

    }
    @Override
    public RecyclerAdapter.ItemViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item,viewGroup,false);
        //return new MainActivity.RecyclerAdapter.ItemViewHolder(view);
        return new ItemViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return listdata.size();
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.ItemViewHolder itemViewHolder, int i) {//데이터를 레이아웃에 넣어주는지 결정
        Memo memo = listdata.get(i);

        itemViewHolder.titleText.setText(memo.getTitleText());
        itemViewHolder.mainText.setText(memo.getMainText());



        if(memo.uriList.size()==0){

            itemViewHolder.img.setBackgroundColor(Color.LTGRAY);
        }
        else {
            String tUri = memo.uriList.get(0);
            Uri uri = Uri.parse(tUri);
            Glide.with(mContext).asBitmap().load(uri).into(itemViewHolder.img);
        }

    }
    void addItem(Memo memo){
        listdata.add(memo);
    }
    void removeItem(int position){
        listdata.remove(position);
    }
    void updateItem(int position,Memo memo){
        listdata.set(position, memo);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder{
        private TextView titleText;
        private TextView mainText;
        private ImageView img;

        public ItemViewHolder(View itemView){
            super(itemView);
            titleText = itemView.findViewById(R.id.item_titleText);
            mainText  = itemView.findViewById(R.id.item_mainText);
            img = itemView.findViewById(R.id.item_image);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { //해당 메모에 대한 정보를 넘겨줘야함.
                    int position = getAdapterPosition();
                    int seq = listdata.get(position).getSeq();

                    if(position != RecyclerView.NO_POSITION){
                        Intent intent = new Intent(mContext,ViewActivity.class);
                        intent.putExtra("seq",seq); //디비용
                        mContext.startActivity(intent);

                    }
                }
            });
        }


    }
}

