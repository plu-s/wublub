package com.translate.wublub;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.util.List;
import java.util.Random;

public class ReciteFragment extends Fragment{

    TextView word;
    TextView explanation;
    int count;
    int randNum;
    Random rand;
    View view;
    int id;
    int num;
    NUM Num = new NUM();
    Words all[] = new Words[1000];
    int n = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.recite, null);

        word = view.findViewById(R.id.word);
        explanation = view.findViewById(R.id.explanation);
        ImageView nextButton = view.findViewById(R.id.nextBtn);
        ImageView deleteWord = view.findViewById(R.id.delete_word);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                word.setText("");
                explanation.setText("");
                getCount();
                if (count != 0) {
                    setWord();
                }
            }
        });

        deleteWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String delete = word.getText().toString();
                //查询这个单词的ID
                List<Words> words = DataSupport.where("src = ?", delete).find(Words.class);
                for (Words word: words) {
                    id = word.getId();
                }
                DataSupport.deleteAll(Words.class, "src = ?", delete);
                word.setText("");
                explanation.setText("");

                //更新LitePal数据库中的数据（重新按id号大小排序）
                List<Words> reWords = DataSupport.findAll(Words.class);
                for (Words word: reWords) {
                    if (word.getId() > id) {
                        all[n] = word;
                        n++;
                    }
                }
                //冒泡排序
                Words temp;
                if (n > 0) {
                    if (n > 1) {
                        for (int i = 0; i < n; i++) {
                            for (int j = i + 1; j < n; j++) {
                                if (all[i].getId() > all[j].getId()) {
                                    temp = all[i];
                                    all[i] = all[j];
                                    all[j] = temp;  // 两个数交换位置
                                }
                            }
                        }
                        //更新数据
                        for (int i = 0; i < n; i++) {
                            Words Word = all[i];
                            int WordId = Word.getId();
                            String WordSrc = Word.getSrc();
                            Words newWords = new Words();
                            newWords.setId(--WordId);
                            newWords.updateAll("src = ?", WordSrc);
                        }
                    } else if (n == 1){
                        Words Word = all[0];
                        int WordId = Word.getId();
                        String WordSrc = Word.getSrc();
                        Words newWords = new Words();
                        newWords.setId(--WordId);
                        newWords.updateAll("src = ?", WordSrc);
                    }
                }


                //更改下一个将要插入的数据的ids号为当前的ids号减1
                num = Num.getNum();
                Num.setNum(--num);

                getCount();
                if (count != 0) {
                    setWord();
                }

                //恢复默认值
                n = 0;
            }
        });


        //获取数据库实例
        LitePal.getDatabase();

        //初始化界面
        getCount();
        if (count != 0) {
            setWord();
        }

        return view;
    }

    //获取当前数据库中的数据数量
    private void getCount() {
        List<Words> allWords = DataSupport.findAll(Words.class);
        count = allWords.size();
    }

    //获取到不大于数据库中数据量的一个整数，用于随机取得数据库中的一个数据
    private void setWord() {
        rand = new Random();
        randNum = rand.nextInt(count) + 1;
        String StrrandNum = String.valueOf(randNum);
        List<Words> allWords = DataSupport.where("ids = ?", StrrandNum).find(Words.class);
        for (Words words: allWords) {
            word.setText(words.getSrc());
            explanation.setText(words.getTranslation());
        }
    }
}
