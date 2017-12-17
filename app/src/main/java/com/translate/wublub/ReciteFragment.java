package com.translate.wublub;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.Random;

public class ReciteFragment extends Fragment{

    TextView word;
    TextView explanation;
    Random rand;
    View view;
    int randId;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.recite, null);

        word = view.findViewById(R.id.word);
        explanation = view.findViewById(R.id.explanation);
        final ImageView nextButton = view.findViewById(R.id.nextBtn);
        ImageView deleteWord = view.findViewById(R.id.delete_word);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                word.setText("");
                explanation.setText("");
                showRecitation();
            }
        });

        deleteWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tmp = word.getText().toString();

                if (!TextUtils.isEmpty(tmp)){
                    DataSupport.deleteAll(Words.class, "ids = ?", String.valueOf(randId));
                    List<Words> allWords = DataSupport.where("ids > ?", String.valueOf(randId)).find(Words.class);
                    for (Words words: allWords){
                        words.setId(words.getId() - 1);
                        words.save();
                    }

                    nextButton.callOnClick();
                    Toast.makeText(getActivity(), "已删除记录 [" + tmp + "]", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rand = new Random();
        //获取数据库实例
        LitePal.getDatabase();
        // 进入界面即开始显示单词
        showRecitation();

        return view;
    } // onCreateView


    private void showRecitation(){
        Words lastWord = DataSupport.findLast(Words.class);
        int count = (lastWord == null ? 0 : lastWord.getId());

        if (count != 0){
            randId = rand.nextInt(count) + 1;
            List<Words> allWords = DataSupport.where("ids = ?", String.valueOf(randId)).find(Words.class);
            for (Words words: allWords) {
                word.setText(words.getSrc());
                explanation.setText(words.getTranslation());
            }
        }
    }

}
