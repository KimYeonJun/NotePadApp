package com.example.notepad;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * EditActivity : 메모 편집을 위한 Activity
 */
public class EditActivity extends AppCompatActivity {
    EditText titleText;
    EditText mainText;
    Button btnDone;
    Button btnNo;
    Button btnImageAdd;
    SQLiteHelper dbHelper;
    RecyclerView imageRecyclerView;
    imageRecyclerAdapter imageRecyclerAdapter;
    private static final int FROM_CAMERA = 0;
    private static final int FROM_ALBUM = 1;
    private File tempFile;
    Uri tUri;
    List<String> edit_uriList;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        titleText = findViewById(R.id.edit_titleText);
        mainText = findViewById(R.id.edit_mainText);
        btnDone=findViewById(R.id.edit_btnDone);
        btnNo =findViewById(R.id.edit_btnNo);
        btnImageAdd = findViewById(R.id.edit_btnImageAdd);
        dbHelper = new SQLiteHelper(EditActivity.this);
        imageRecyclerView = findViewById(R.id.edit_image_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        imageRecyclerView.setLayoutManager(linearLayoutManager);
        Intent intent = getIntent();
        final int seq =  intent.getExtras().getInt("seq");
        Memo memo = dbHelper.selectMemo(seq);
        if(memo != null){
            titleText.setText(memo.getTitleText());
            mainText.setText(memo.getMainText());
            edit_uriList = memo.geturiList();
            imageRecyclerAdapter=new imageRecyclerAdapter(edit_uriList, this);
            imageRecyclerView.setAdapter(imageRecyclerAdapter);
        }

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strMain = mainText.getText().toString();
                String strTitle= titleText.getText().toString();
                if(strMain.length()>0 && strTitle.length()>0){
                    dbHelper.deleteImage(seq);
                    Memo memo = new Memo(strTitle,strMain);
                    dbHelper.updateMemo(memo,seq);
                    for(int i=0;i<edit_uriList.size();i++){
                        dbHelper.insertImage(seq,edit_uriList.get(i));
                    }
                    Intent openMainActivity = new Intent(EditActivity.this, MainActivity.class);
                    openMainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(openMainActivity);
                    finish();
                }
                else{
                    Toast.makeText(EditActivity.this,"제목과 내용을 입력해주세요.",Toast.LENGTH_SHORT).show();
                }

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

            }
        });
        btnImageAdd.setOnClickListener(new View.OnClickListener() { //Image 추가할 경우
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this);
                builder
                        .setTitle("이미지 추가 장치 선택")
                        .setMessage("이미지 추가 장치를 선택하세요")
                        .setIcon(R.drawable.ic_add_a_photo)
                        .setPositiveButton("Camera" , new DialogInterface.OnClickListener() { //사진 촬영하는 case
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                takePhoto();
                                dialog.dismiss();
                            }
                        })
                        .setNeutralButton("Gallery", new DialogInterface.OnClickListener() { //갤러리에서 선택하는 case
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectAlbum();
                                dialog.dismiss();

                            }
                        })
                        .setNegativeButton("외부 Uri", new DialogInterface.OnClickListener() { //외부 url 입력 case
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final EditText urlText = new EditText(EditActivity.this);
                                AlertDialog.Builder extraUrlBuilder = new AlertDialog.Builder(EditActivity.this);
                                extraUrlBuilder
                                        .setTitle("외부 Url")
                                        .setMessage("외부 Url을 입력하세요.")
                                        .setView(urlText)
                                        .setIcon(R.drawable.ic_add_a_photo)
                                        .setPositiveButton("Yes" , new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String exUrl = urlText.getText().toString();
                                                Log.d("exUrl",exUrl);
                                                imageRecyclerAdapter.addItem(exUrl);
                                                imageRecyclerAdapter.notifyDataSetChanged();
                                                dialog.dismiss();

                                            }
                                        })

                                        .setNegativeButton("No",null)
                                        .show();
                                dialog.dismiss();

                            }
                        })
                        .show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if(tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e("YeonJun", tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }
            return;
        }
        if(requestCode==FROM_ALBUM){ //갤러리에서 사진 선택하는 case
            Uri photoUri = data.getData();
            Log.d("uri1",photoUri.toString());
            imageRecyclerAdapter.addItem(photoUri.toString());
            imageRecyclerAdapter.notifyDataSetChanged();
        }
        else if(requestCode==FROM_CAMERA){ //사진 촬영하는 case
            //Uri photoUri = data.getData();
            Log.d("uri1",tUri.toString());
            galleryAddPic();
            imageRecyclerAdapter.addItem(tUri.toString());
            imageRecyclerAdapter.notifyDataSetChanged();
        }

    }

    private void selectAlbum(){

        //앨범 열기
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setType("image/*");
        startActivityForResult(intent, FROM_ALBUM);

    }
    private void takePhoto() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (tempFile != null) {
            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N){
                Uri photoUri = FileProvider.getUriForFile(this,"com.example.notepad.provider",tempFile);
                tUri = photoUri;
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                startActivityForResult(intent,FROM_CAMERA);
            }
            else{
                Uri photoUri = Uri.fromFile(tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, FROM_CAMERA);
            }
        }
    }
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
