package com.translate.wublub;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
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
    private Animator mCurrentAnimator;
    private int mShortAnimationDuration;
    View view;
    Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ocr, container, false);

        ImageView start = view.findViewById(R.id.start);
        start.setOnClickListener(new View.OnClickListener() {
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
            }// onClick
        });

        imageView = view.findViewById(R.id.imageView);
        resultText = view.findViewById(R.id.resultText);
        haveRecognized = false;

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUri != null){
                    Bitmap bitmap = ImageUtils.compressBitmap(getActivity(), currentUri);
                    zoomImageFromThumb(imageView, bitmap);
                }
            }
        });

        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        return view;
    } // onCreateView


    @Override
    public void onResume() {
        super.onResume();

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
    } // onResume


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

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        final String base64 = EncryptHelper.getBase64(out.toByteArray());

        OCRParameters tps = new OCRParameters.Builder()
                .source("youdaoocr").timeout(100000)
                .type("10011").lanType("zh-en").build();

        ImageOCRecognizer.getInstance(tps).recognize(base64,
                new OCRListener() {

                    @Override
                    public void onResult(final OCRResult result, String input) {
                        // 在主线程中更新UI
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                resultText.setText(getResult(result));
                            }
                        });
                    }// onResult

                    @Override
                    public void onError(OcrErrorCode error) {
                        resultText.setText("识别失败");
                    }
                });
    } // startRecognize


    private String getResult(OCRResult result) {

        List<Region> regions = result.getRegions();
        StringBuilder sb = new StringBuilder();

        sb.append("识别结果:" + (result.getErrorCode() == 0 ? "成功" : "识别异常"));
        sb.append("\n");

        if (result.getErrorCode() == 0){
            haveRecognized = true;        // 识别成功，设置相应标识，避免对同一照片重复识别
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


    //以下为实现图片放大
    private void zoomImageFromThumb(final View thumbView, Bitmap bitmap) {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        final ImageView expandedImageView = view.findViewById(
                R.id.expanded_image);
        expandedImageView.setImageBitmap(bitmap);

        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        thumbView.getGlobalVisibleRect(startBounds);
        view.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        float startScale=0;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // 横向缩放
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        //以下实现动画效果
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;

        final float startScaleFinal = startScale;
        expandedImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentAnimator != null) {
                    mCurrentAnimator.cancel();
                }

                AnimatorSet set = new AnimatorSet();
                set.play(ObjectAnimator
                        .ofFloat(expandedImageView, View.X, startBounds.left))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.Y,startBounds.top))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_X, startScaleFinal))
                        .with(ObjectAnimator
                                .ofFloat(expandedImageView,
                                        View.SCALE_Y, startScaleFinal));
                set.setDuration(mShortAnimationDuration);
                set.setInterpolator(new DecelerateInterpolator());
                set.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        thumbView.setAlpha(1f);
                        expandedImageView.setVisibility(View.GONE);
                        mCurrentAnimator = null;
                    }
                });
                set.start();
                mCurrentAnimator = set;
            }
        });
    } // zoomImageFromThumb

}
