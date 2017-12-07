package com.translate.wublub;

//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class ReciteFragment extends Fragment{

    TextView word;
    TextView explanation;
    int count;
    int randNum;
    Random rand;
    MyDatabaseHelper dbHelper;
    SQLiteDatabase db;
    Cursor cursor;
    String Strword;
    String Strexplannation;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.recite, null);

        word = view.findViewById(R.id.word);
        explanation = view.findViewById(R.id.explanation);
        Button transButton = view.findViewById(R.id.transBtn);
        Button nextButton = view.findViewById(R.id.nextBtn);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                explanation.setText("");
                getCount();
                if (count != 0) {
                    setWord();
                }
            }
        });

        transButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if (Strexplannation != null) {
                    explanation.setText(Strexplannation);
                }
            }
        });

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getCount();
        if (count != 0) {
            setWord();
        }
    }

    //获取当前数据库中的数据数量
    private void getCount() {
        dbHelper = new MyDatabaseHelper(view.getContext(), "ReciteWords.db", null, 1);
        db = dbHelper.getWritableDatabase();
        cursor = db.query("Words", null, null, null, null, null, null);
        count = cursor.getCount();
    }

    //获取到不大于数据库中数据数量的一个整数，用于随机取得数据库中的一个数据
    private void setWord() {
        rand = new Random();
        randNum = rand.nextInt(count) + 1;
        String StrrandNum = String.valueOf(randNum);
        cursor = db.query("Words", null, "id = ?", new String[] {StrrandNum}, null, null, null);
        cursor.moveToFirst();
        Strword = cursor.getString(cursor.getColumnIndex("src"));
        Strexplannation = cursor.getString(cursor.getColumnIndex("translation"));
        word.setText(Strword);
        cursor.close();
    }
}
