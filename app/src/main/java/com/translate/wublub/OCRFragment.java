package com.translate.wublub;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.youdao.ocr.online.ImageOCRecognizer;
import com.youdao.ocr.online.Line;
import com.youdao.ocr.online.OCRListener;
import com.youdao.ocr.online.OCRParameters;
import com.youdao.ocr.online.OCRResult;
import com.youdao.ocr.online.OcrErrorCode;
import com.youdao.ocr.online.Region;
import com.youdao.ocr.online.Word;
import com.youdao.sdk.app.EncryptHelper;

import java.io.ByteArrayOutputStream;
import java.util.List;


public class OCRFragment extends Fragment {

    private ImageView imageView;
    private TextView resultText;
    private Uri currentUri;
    private Boolean haveRecognized;   // 是否已经对当前图片进行过OCR识别

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ocr, container, false);

        Log.d("【记录】", "onCreateView in OCRFragment");

        Button recognizeBtn = view.findViewById(R.id.recognizeBtn);
        recognizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUri == null) {
                    Toast toast = Toast.makeText(getActivity(), "请拍摄或选择图片", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                if (!haveRecognized){
                    try {
                        Bitmap bitmap = ImageUtils.compressBitmap(getActivity(), currentUri);
                        startRecognize(bitmap);
                        resultText.setText("正在识别，请稍等....");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        imageView = view.findViewById(R.id.imageView);
        resultText = view.findViewById(R.id.resultText);

        haveRecognized = false;

        return view;
    } // onCreateView


    @Override
    public void onResume() {
        super.onResume();

        Log.d("【记录】", "onResume in OCRFragment");

        MainActivity mainActivity = (MainActivity) getActivity();

        if (currentUri != mainActivity.getCurrentUri()){

            releaseImageViewResouce();
            resultText.setText("");
            haveRecognized = false;
            currentUri = mainActivity.getCurrentUri();      // 从活动中获取当前待识别图片的 uri 资源

            if (currentUri != null) {
                Bitmap bitmap = ImageUtils.compressBitmap(getActivity(), currentUri);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d("【记录】", "onDestroyView in OCRFragment");
        releaseImageViewResouce();
        imageView.setImageDrawable(null);
    }


    // 释放照片占有的内存
    private  void releaseImageViewResouce() {

        if (imageView == null) {
            return;
        }

        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }


    private void startRecognize(final Bitmap bitmap) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] datas = baos.toByteArray();
        final String base64 = EncryptHelper.getBase64(datas);

        OCRParameters tps = new OCRParameters.Builder()
                .source("youdaoocr").timeout(100000)
                .type("10011").lanType("zh-en").build();

        ImageOCRecognizer.getInstance(tps).recognize(base64,
                new OCRListener() {

                    @Override
                    public void onResult(OCRResult result, String input) {
                        resultText.setText(getResult(result));
                    }

                    @Override
                    public void onError(OcrErrorCode error) {
                        resultText.setText("识别失败");
                    }
                });

    }


    private String getResult(OCRResult result) {

        List<Region> regions = result.getRegions();
        StringBuilder sb = new StringBuilder();

        sb.append("识别结果:" + (result.getErrorCode() == 0 ? "成功" : "识别异常"));
        sb.append("\n");

        if (result.getErrorCode() == 0){
            haveRecognized = true;        // 识别成功，设置相应标识，避免重复识别
        }

        for (Region region : regions) {
            List<Line> lines = region.getLines();

            for (Line line : lines) {

                List<Word> words = line.getWords();

                for (Word word : words) {
                    sb.append(word.getText()).append(" ");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
