package com.translate.wublub;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity{

    public ViewPager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView bottomNavigationView;
    private IntentFilter intentFilter;
    private NetworkChangeReceiver networkChangeReceiver;

    private String filePath;
    Uri currentUri;
    private Boolean networkOK = true;
    AlertDialog dialog;

    final static int RECITE_PAGE = 0;
    final static int TRANSLATE_PAGE = 1;
    final static int OCR_PAGE = 2;
    final static int GET_PIC_FROM_CAMERA = 0;
    final static int GET_PIC_FROM_ALBUM = 1;
    final static int CAMERA_CODE_FOR_RESULT = 101;
    final static int ALBUM_CODE_FOR_RESULT = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionbar = getSupportActionBar();
        if(actionbar != null){
            actionbar.hide();
        }

        // 网络状态广播
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);

        // 提示网络错误
        setNetworkFlag();
        if (!networkOK){
            Toast.makeText(MainActivity.this,"网络已断开", Toast.LENGTH_LONG).show();
        }

        // 写外部存储为危险权限
        //如果 targetSdkVersion 设置为 >=23 的值，则需要在运行时申请危险权限
        if(!isPermissionGranted(this, WRITE_EXTERNAL_STORAGE)){
            String[] perssions = {WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, perssions, 1);
        }

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);

        setupBttomNavigationView();
    }


    private static boolean isPermissionGranted(final Context context, final String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }


    private void setupBttomNavigationView() {

        // 设置底部导航栏的监听事件
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        if (!networkOK) {
                            Toast.makeText(MainActivity.this, "网络已断开", Toast.LENGTH_SHORT).show();
                        }

                        switch (item.getItemId()) {
                            case R.id.item_recite:
                                viewPager.setCurrentItem(RECITE_PAGE);
                                break;
                            case R.id.item_translate:
                                viewPager.setCurrentItem(TRANSLATE_PAGE);
                                break;
                            case R.id.item_ocr:
                                viewPager.setCurrentItem(OCR_PAGE);
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });

        // 设置页面切换监听事件
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);     // 撤销上一个页面的选中状态
                } else {
                    bottomNavigationView.getMenu().getItem(TRANSLATE_PAGE).setChecked(false);
                }
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);      // 设置选中状态

                // 判断当前页面是否为 OCR 页面
                // 若是，则将 OCR 页面的 AlertDialog 弹框显示留给 MainActivity 实现
                if (position == OCR_PAGE) {
                    showPicChoices();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        setupViewPager(viewPager);
        viewPager.setCurrentItem(TRANSLATE_PAGE);        // 设置翻译为主页面

    } // setupBttomNavigationView


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new ReciteFragment());
        adapter.addFragment(new TranslateFragment());
        adapter.addFragment(new OCRFragment());
        viewPager.setAdapter(adapter);
    }


    private void showPicChoices(){
        /* 进入OCR界面后首先打开 AlertDialog，必须选择拍照或者相册二者之一 */
        final String[] getPicChoice = new String[]{"拍摄照片", "从相册选择"};
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(MainActivity.this)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        // 如果不进行选择，则退回到翻译界面
                        menuItem.setChecked(false);
                        menuItem = bottomNavigationView.getMenu().getItem(TRANSLATE_PAGE);
                        menuItem.setChecked(true);
                        viewPager.setCurrentItem(TRANSLATE_PAGE);
                    }
                })
                .setItems(getPicChoice, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == GET_PIC_FROM_CAMERA){
                            getPicFromCamera();
                        }else{ // i == GET_PIC_FROM_ALBUM
                            getPicFromAlbum();
                        }
                    }
                });
        dialog = alertDlg.show();
    }


    private void getPicFromCamera(){
        String state = Environment.getExternalStorageState(); // 判断是否存在sd卡
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            filePath = getFileName();

            // 安卓 7.0 (API_LEVLE 24) 及以上版本不能暴露真实 uri 路径，可以用以下方法解决
            if (Build.VERSION.SDK_INT >= 24){
                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this,
                        "android.wublub.fileprovider", new File(filePath)));
            }else{
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath)));
            }

            startActivityForResult(intent, CAMERA_CODE_FOR_RESULT);
        }else{
            Toast toast = Toast.makeText(this, "请检查手机是否有外部存储空间", Toast.LENGTH_LONG);
            toast.show();
        }
    }


    private void getPicFromAlbum(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, ALBUM_CODE_FOR_RESULT);
    }


    private String getFileName() {
        String saveDir = Environment.getExternalStorageDirectory() + "/myPic";
        File dir = new File(saveDir);

        if (!dir.exists()) {
            dir.mkdir();
        }

        //用日期作为文件名，确保唯一性
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return saveDir + "/" + formatter.format(date) + ".png";
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_CODE_FOR_RESULT || requestCode == ALBUM_CODE_FOR_RESULT){
            if (resultCode == RESULT_OK) {
                Uri uri = null;
                if(data != null){     // 从相册选择是为真
                    uri = data.getData();
                }
                if (uri == null && !TextUtils.isEmpty(filePath)) {  // 拍照时为真
                    uri = Uri.parse(filePath);
                }
                currentUri = uri;
            }else{
                // 选择照片或者拍照时中途退出，也直接返回翻译界面
                currentUri = null;
                menuItem.setChecked(false);
                menuItem = bottomNavigationView.getMenu().getItem(TRANSLATE_PAGE);
                menuItem.setChecked(true);
                viewPager.setCurrentItem(TRANSLATE_PAGE);
            }
        }
    }


    // 用于在 OCRFragment 中获取当前相片文件的 uri
    public Uri getCurrentUri(){
        return currentUri;
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }


    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            setNetworkFlag();
            if (!networkOK){
                Toast.makeText(context,"网络已断开", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setNetworkFlag(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isAvailable()){
            networkOK = true;
        }
        else {
            networkOK = false;
        }
    }

}
