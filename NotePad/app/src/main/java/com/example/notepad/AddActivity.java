package com.example.notepad;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 *AddActivity : 새로운 메모 추가
 *
**/
public class AddActivity extends AppCompatActivity {
    EditText titleText;
    EditText mainText;
    Button btnDone;
    Button btnNo;
    Button btnImageAdd;
    SQLiteHelper dbHelper;
    LinearLayout rootLayout;
    RecyclerView imageRecyclerView;
    imageRecyclerAdapter imageRecyclerAdapter;
    private static final int FROM_CAMERA = 0;
    private static final int FROM_ALBUM = 1;
    private File tempFile;
    //private Uri photoURI;
    Uri tUri;

    private Boolean isPermission = true;
    String currentPhotoPath;
    List<String> uriList=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        imageRecyclerView = findViewById(R.id.image_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        imageRecyclerView.setLayoutManager(linearLayoutManager);

        imageRecyclerAdapter=new imageRecyclerAdapter(uriList, this);
        imageRecyclerView.setAdapter(imageRecyclerAdapter);

        Initialize();
        tedPermission();
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strMain = mainText.getText().toString();
                String strTitle= titleText.getText().toString();
                if(strMain.length()>0 && strTitle.length()>0){
                    Memo memo = new Memo(strTitle,strMain);
                    dbHelper.insertMemo(memo);
                    //dbHelper.allSeq();
                    for(int i=0;i<uriList.size();i++){
                        dbHelper.insertImage(dbHelper.getSeq(),uriList.get(i));
                    }


                    Intent openMainActivity = new Intent(AddActivity.this, MainActivity.class);
                    openMainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(openMainActivity);
                    finish();

                }
                else{
                    Toast.makeText(AddActivity.this,"제목과 내용을 입력해주세요.",Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnImageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
                builder
                        .setTitle("이미지 추가 장치 선택")
                        .setMessage("이미지 추가 장치를 선택하세요")
                        .setIcon(R.drawable.ic_add_a_photo)
                        .setPositiveButton("Camera" , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                takePhoto();
                                dialog.dismiss();
                            }
                        })
                        .setNeutralButton("Gallery", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                selectAlbum();
                                dialog.dismiss();

                            }
                        })
                        .setNegativeButton("외부 Uri", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final EditText urlText = new EditText(AddActivity.this);
                                AlertDialog.Builder extraUrlBuilder = new AlertDialog.Builder(AddActivity.this);
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
    void Initialize(){
        titleText = findViewById(R.id.add_titleText);
        mainText = findViewById(R.id.add_mainText);
        btnDone=findViewById(R.id.btnDone);
        btnNo =findViewById(R.id.btnNo);
        btnImageAdd = findViewById(R.id.btnImageAdd);
        dbHelper = new SQLiteHelper(AddActivity.this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
        if(requestCode==FROM_ALBUM){
            Uri photoUri = data.getData();
            imageRecyclerAdapter.addItem(photoUri.toString());
            imageRecyclerAdapter.notifyDataSetChanged();
        }
        else if(requestCode==FROM_CAMERA){
            galleryAddPic();
            imageRecyclerAdapter.addItem(tUri.toString());
            imageRecyclerAdapter.notifyDataSetChanged();
        }

    }
    private void tedPermission(){
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                //Toast.makeText(AddActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                isPermission=true;
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                //Toast.makeText(AddActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                isPermission=false;
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용해주세요.")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }
    public void selectAlbum(){

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