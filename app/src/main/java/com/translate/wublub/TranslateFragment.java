package com.translate.wublub;

import android.support.v4.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class TranslateFragment extends Fragment{

    EditText srcText;
    TextView resultText;
    ImageView translateBtn;
    String src;
    MyDatabaseHelper dbHelper;
    String translation;
    GetURL url = new GetURL();
    JsonParse jsonParse = new JsonParse();
    ContentValues values = new ContentValues();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.translate, null);

        srcText = view.findViewById(R.id.src);
        resultText = view.findViewById(R.id.result);
        translateBtn = view.findViewById(R.id.translateBtn);

        translateBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                src = srcText.getText().toString().trim();      // 获取待翻译文本且去除首尾“空”字符

                if (!src.isEmpty()) {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor cursor = db.query("Words", null, "src = ?", new String[]{src}, null, null, null);

                    if (cursor.getCount() != 0) {
                        //如果本地数据库中有所查询的单词，则不用网络查询
                        cursor.moveToFirst();
                        translation = cursor.getString(cursor.getColumnIndex("translation"));
                        resultText.setText(translation);
                        cursor.close();
                    } else {
                        cursor.close();
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

                    } // if else
                } // if else
            }
        });


        /* 以下设置待翻译文本框的提示文本的字体大小 */
        SpannableString s = new SpannableString("在此处输入要翻译的文本……");
        AbsoluteSizeSpan textSize = new AbsoluteSizeSpan(16, true);
        s.setSpan(textSize, 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        srcText.setHint(s);

        /* 获取数据库 RecitesWords 实例*/
        dbHelper = new MyDatabaseHelper(view.getContext() , "ReciteWords.db", null, 1);

        return view;
    } // onCreateView

    private void showTranslateResult(final String result){
        // 子线程无法对UI进行修改，必须切换回主线程
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // json 分析返回的翻译结果，然后显示,并将数据存入数据库
                resultText.setText(jsonParse.parse(result));
                src = srcText.getText().toString();
                translation = jsonParse.parse(result);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                values.put("src", src);
                values.put("translation", translation);
                db.insert("Words", null, values);
                values.clear();
            }
        });
    }

}
