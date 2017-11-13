package com.translate.wublub;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

// BottomNavigationView + ViewPager + Fragment 实现底部导航栏及滑动切换页面参考：
// http://www.jianshu.com/p/0ba25cc65889

public class MainActivity extends AppCompatActivity{

    private ViewPager viewPager;
    private MenuItem menuItem;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager)findViewById(R.id.viewpager);
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);

        // 设置底部导航栏的监听事件
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_recite:
                                viewPager.setCurrentItem(0);
                                break;
                            case R.id.item_translate:
                                viewPager.setCurrentItem(1);
                                break;
                            case R.id.item_ocr:
                                viewPager.setCurrentItem(2);
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        setupViewPager(viewPager);
        viewPager.setCurrentItem(1);        // 设置翻译为主页面
    }


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new ReciteFragment());
        adapter.addFragment(new TranslateFragment());
        adapter.addFragment(new OCRFragment());
        viewPager.setAdapter(adapter);
    }

}
