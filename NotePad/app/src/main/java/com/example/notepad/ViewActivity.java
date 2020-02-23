package com.example.notepad;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewActivity : 메모 상세 보기
 */
public class ViewActivity extends AppCompatActivity {
    TextView titleText;
    TextView mainText;
    Button btnEdit;
    Button btnDelete;
    Button btnCancel;
    SQLiteHelper dbHelper;
    RecyclerView imageRecyclerView;
    imageRecyclerAdapter imageRecyclerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        titleText = findViewById(R.id.view_titleText);
        mainText = findViewById(R.id.view_mainText);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);
        btnCancel = findViewById(R.id.btnCancel);
        imageRecyclerView = findViewById(R.id.view_image_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        imageRecyclerView.setLayoutManager(linearLayoutManager);
        dbHelper = new SQLiteHelper(ViewActivity.this);
        Intent intent = getIntent();
        final int seq =  intent.getExtras().getInt("seq");
        Memo memo = dbHelper.selectMemo(seq);
        if(memo != null){
            titleText.setText(memo.getTitleText());
            mainText.setText(memo.getMainText());
            imageRecyclerAdapter=new imageRecyclerAdapter(memo.geturiList(), this);
            imageRecyclerView.setAdapter(imageRecyclerAdapter);
        }

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewActivity.this);
                builder
                        .setTitle("삭제")
                        .setMessage("정말로 메모를 삭제 하시겠습니까?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("Yes" , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dbHelper.deleteMemo(seq);
                                dbHelper.deleteImage(seq);
                                Intent intent= new Intent(ViewActivity.this,MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        })

                        .setNegativeButton("No",null)
                        .show();
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewActivity.this,EditActivity.class);
                intent.putExtra("seq",seq);
                startActivity(intent);
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

}
