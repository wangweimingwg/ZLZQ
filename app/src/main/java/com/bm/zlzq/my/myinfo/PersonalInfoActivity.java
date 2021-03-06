package com.bm.zlzq.my.myinfo;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bm.zlzq.BaseActivity;
import com.bm.zlzq.Http.APICallback;
import com.bm.zlzq.Http.APIResponse;
import com.bm.zlzq.Http.Urls;
import com.bm.zlzq.Http.WebServiceAPI;
import com.bm.zlzq.R;
import com.bm.zlzq.my.address.MyAddressActivity;
import com.bm.zlzq.utils.ImageUtils;
import com.bm.zlzq.utils.NewToast;
import com.bm.zlzq.view.RoundImageView;
import com.bm.zlzq.view.WheelDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by wangwm on 2015/12/18.
 */
public class PersonalInfoActivity extends BaseActivity implements APICallback.OnResposeListener {
    private RelativeLayout rl_touxiang, rl_nickname, rl_sex, rl_address;
    private TextView tv_nickname, tv_sex;
    private Uri fromFile;
    private Bitmap bitmap;
    private RoundImageView iv_image;
    private File head;
    private List<String> list = new ArrayList<String>();
    private static final int RESULT_ALBUM = 0; // 相册
    private static final int RESULT_CAMERA = 1;// 相机
    private static final int CROP_PICTURE = 2;// 截取后返回
    private static final int RESULT_NICKNAME = 3;// 修改昵称

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_my_personal_info);
        initView();
        initData();
        initTitle("个人资料");
    }

    private void initView() {
        rl_touxiang = (RelativeLayout) findViewById(R.id.rl_touxiang);
        iv_image = (RoundImageView) findViewById(R.id.iv_image);
        rl_nickname = (RelativeLayout) findViewById(R.id.rl_nickname);
        tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        rl_sex = (RelativeLayout) findViewById(R.id.rl_sex);
        tv_sex = (TextView) findViewById(R.id.tv_sex);
        rl_address = (RelativeLayout) findViewById(R.id.rl_address);

        Intent intent = getIntent();
        String sex = intent.getStringExtra("sex");
        if (sex == "0") {
            tv_sex.setText("男");
        }
        if (sex == "1") {
            tv_sex.setText("女");
        }
        String heads = intent.getStringExtra("head");
        Log.e("head", intent.getStringExtra("head") + "321");

        tv_nickname.setText(intent.getStringExtra("nickname"));
        ImageLoader.getInstance().displayImage(Urls.PHOTO + heads, iv_image, getImageOptions());

        rl_touxiang.setOnClickListener(this);
        rl_nickname.setOnClickListener(this);
        rl_sex.setOnClickListener(this);
        rl_address.setOnClickListener(this);
    }

    private void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_back:
                if (bitmap != null) {
                    Intent intent = new Intent();
                    intent.putExtra("bitmap", bitmap);

                    setResult(RESULT_OK, intent);
                }
                onBackPressed();
                break;
            case R.id.rl_touxiang:
                showdialog(Gravity.BOTTOM);
                final Dialog alertDialog = this.alertDialog;
                alertDialog.getWindow().setContentView(R.layout.dialog_photo);
                TextView tv_camera = (TextView) alertDialog.getWindow().findViewById(
                        R.id.tv_camera);
                TextView tv_album = (TextView) alertDialog.getWindow().findViewById(
                        R.id.tv_album);
                TextView tv_cancel = (TextView) alertDialog.getWindow().findViewById(
                        R.id.tv_cancel);
                tv_camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doCamera();
                        alertDialog.cancel();
                    }
                });
                tv_album.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doAlbum();
                        alertDialog.cancel();
                    }
                });
                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.cancel();
                    }
                });
                break;
            case R.id.rl_nickname:
//                gotoOtherActivity(ModifyNicknameActivity.class);
                Intent intent = new Intent(this, ModifyNicknameActivity.class);
//                intent.putExtra("nickname", tv_sex.getText());
                startActivityForResult(intent, RESULT_NICKNAME);
                break;
            case R.id.rl_sex:
                list.clear();
                list.add("男");
                list.add("女");
                WheelDialog.getInstance().ChossDateOrNumDlg(this, "性别", tv_sex, list, new WheelDialog.GetCityIdListener() {
                    @Override
                    public void GetCityId(String provinceId, String cityId, String areaId) {

                        if (provinceId == "男") {
                            WebServiceAPI.getInstance().updateInfo("", "0", PersonalInfoActivity.this, PersonalInfoActivity.this);
                        }
                        if (provinceId == "女") {
                            WebServiceAPI.getInstance().updateInfo("", "1", PersonalInfoActivity.this, PersonalInfoActivity.this);
                        }


                    }
                });
                break;
            case R.id.rl_address:
                goto1OtherActivity(MyAddressActivity.class, 0);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RESULT_NICKNAME:
                    String nickname = data.getStringExtra("nickname");
                    tv_nickname.setText(nickname);
                    break;
                case RESULT_ALBUM:
                    if (data != null) {
                        startPhotoZoom(data.getData());
                    }
                    break;
                case RESULT_CAMERA:
                    if (null != fromFile) {
                        startPhotoZoom(fromFile);
                    }
                    if (data != null) {
                        startPhotoZoom(data.getData());
                    }
                    break;
                case CROP_PICTURE:
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            bitmap = data.getParcelableExtra("data");
                            iv_image.setImageBitmap(ImageUtils.toRoundBitmap(bitmap));

                            //图像保存到文件中
                            FileOutputStream foutput = null;
                            File files = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp");
                            if (!files.exists()) {
                                files.mkdirs();
                            }
                            File mRecVedioPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp/", "cropPicture.jpg");
                            if (mRecVedioPath.exists()) {
                                mRecVedioPath.delete();
                            }
//                            Log.e("ssss", mRecVedioPath.toString());
                            head = mRecVedioPath;
                            try {
                                foutput = new FileOutputStream(mRecVedioPath);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, foutput);
                                foutput.flush();
                                foutput.close();
//                                Log.e("bmap", bitmap.toString());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if (null != foutput) {
                                    try {
                                        foutput.flush();
                                        foutput.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    // 此处请求上传头像接口
                    WebServiceAPI.getInstance().updateHead(head, PersonalInfoActivity.this, PersonalInfoActivity.this);

                    break;
                default:
                    break;
            }
        }
    }

    private void doAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_ALBUM);
    }


    private void doCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File dir = Environment.getExternalStorageDirectory();

            File file = new File(dir, new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date())
                    + ".jpg");
            fromFile = Uri.fromFile(file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fromFile);
            startActivityForResult(intent, RESULT_CAMERA);
        } else {
            NewToast.show(this, "请确认已经插入SD卡", Toast.LENGTH_LONG);
        }
    }

    public void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 150);
        intent.putExtra("outputY", 150);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_PICTURE);
    }

    @Override
    public void OnFailureData(String error, Integer tag) {
        NewToast.show(this, "网络不好,请稍后再试!", NewToast.LENGTH_LONG);
    }

    @Override
    public void OnSuccessData(APIResponse apiResponse, Integer tag) {
        if (tag == 0) {
//            WebServiceAPI.getInstance().updateInfo(tv_nickname.getText().toString().trim(), "", PersonalInfoActivity.this, PersonalInfoActivity.this);
            NewToast.show(this, "修改成功!", NewToast.LENGTH_LONG);
        }
        if (tag == 1) {
//            WebServiceAPI.getInstance().updateHead(head, PersonalInfoActivity.this, PersonalInfoActivity.this);
            NewToast.show(this, "头像保存成功!", NewToast.LENGTH_LONG);
        }
    }

    @Override
    public void OnErrorData(String code, Integer tag) {

    }
}
