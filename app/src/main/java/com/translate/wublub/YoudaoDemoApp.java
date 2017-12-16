package com.translate.wublub;

import android.app.Application;

import com.youdao.sdk.app.YouDaoApplication;

import org.litepal.LitePal;


public class YoudaoDemoApp extends Application {

    private static YoudaoDemoApp swYouAppction;

    @Override
    public void onCreate() {
        super.onCreate();

        //创建应用，每个应用都会有一个Appid，绑定对应的翻译服务实例，即可使用
        YouDaoApplication.init(this,"5998a370375262ca");
        swYouAppction = this;

        LitePal.initialize(getApplicationContext());
    }

    public static YoudaoDemoApp getInstance() {
        return swYouAppction;
    }

}
