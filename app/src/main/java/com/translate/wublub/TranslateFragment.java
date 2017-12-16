package com.translate.wublub;

import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.translate.wublub.R.id.viewpager;


public class TranslateFragment extends Fragment{

    EditText srcText;
    TextView resultText;
    ImageView translateBtn;
    String src;
    String translation;
    GetURL url = new GetURL();
    JsonParse jsonParse = new JsonParse();
    View view;
    int num;
    NUM Num = new NUM();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.translate, null);

        srcText = view.findViewById(R.id.src);
        resultText = view.findViewById(R.id.result);
        translateBtn = view.findViewById(R.id.translateBtn);

        translateBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                src = srcText.getText().toString().trim();      // 获取待翻译文本且去除首尾“空”字符
                if (!src.isEmpty()) {
                    List<Words> allWords = DataSupport.where("src = ?", src).find(Words.class);
                    if (!allWords.isEmpty()) {
                        //如果本地数据库中有所查询的单词，则不用网络查询
                        for (Words words: allWords) {
                            translation = words.getTranslation();
                            resultText.setText(translation);
                        }
                    } else {
                        // 本地数据库中没有，则网络请求且该耗时操作必须放到子线程里面
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    OkHttpClient client = new OkHttpClient();
                                    Request request = new Request.Builder().url(url.getUrl(srcText.getText().toString())).build();
                                    Response response = client.newCall(request).execute();      // 返回 json 格式的数据
                                    showTranslateResult(response.body().string());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                }
            }
        });


        /* 以下设置待翻译文本框的提示文本的字体大小 */
        SpannableString s = new SpannableString("在此处输入要翻译的文本……");
        AbsoluteSizeSpan textSize = new AbsoluteSizeSpan(16, true);
        s.setSpan(textSize, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        srcText.setHint(s);

        /* 获取数据库 RecitesWords 实例以及建表操作*/
        LitePal.getDatabase();

        return view;
    }


    private void showTranslateResult(final String result){
        // 子线程无法对UI进行修改，必须切换回主线程
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // json 分析返回的翻译结果，然后显示,并将数据存入数据库
                String src = srcText.getText().toString();
                String translation = jsonParse.parse(result);
                resultText.setText(translation);

                LitePal.getDatabase();
                num = Num.getNum();
                Words words = new Words();
                words.setId(num++);
                words.setSrc(src);
                words.setTranslation(translation);
                words.save();
                Num.setNum(num);
            }
        });
    }

}
