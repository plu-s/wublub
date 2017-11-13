package com.translate.wublub;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    EditText srcText;
    TextView resultText;
    GetURL url = new GetURL();
    JsonParse jsonParse = new JsonParse();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button translateBtn = (Button)findViewById(R.id.translateBtn);
        translateBtn.setOnClickListener(this);

        srcText = (EditText)findViewById(R.id.src);
        resultText = (TextView)findViewById(R.id.result);

        /* 以下设置待翻译文本框的提示文本的字体大小 */
        SpannableString s = new SpannableString("在此处输入要翻译的文本……");
        AbsoluteSizeSpan textSize = new AbsoluteSizeSpan(16, true);
        s.setSpan(textSize, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        srcText.setHint(s);

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.translateBtn:
                // 网络请求等耗时操作必须放到子线程里面
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder().url(url.getUrl(srcText.getText().toString())).build();
                            Response response = client.newCall(request).execute();      // 返回 json 格式的数据
                            showTranslateResult(response.body().string());
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    private void showTranslateResult(final String result){
        // 子线程无法对UI进行修改，必须切换回主线程
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultText.setText(jsonParse.parse(result));    // json 分析返回的翻译结果，然后显示
            }
        });
    }
}
