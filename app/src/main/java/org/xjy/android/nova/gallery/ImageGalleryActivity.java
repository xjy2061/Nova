package org.xjy.android.nova.gallery;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ImageGalleryActivity extends AppCompatActivity {
    public static final String EXTRA_URIS = "uris";
    public static final String EXTRA_POSITION = "position";

    private ArrayList<String> mUris;
    private int mPosition;
    private Resources mResources;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mUris = intent.getStringArrayListExtra(EXTRA_URIS);
        mPosition = intent.getIntExtra(EXTRA_POSITION, 0);
        mResources = getResources();

        mViewPager = new ViewPager(this);
        setContentView(mViewPager, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        mViewPager.setCurrentItem(mPosition);
        mViewPager.setAdapter(new ImageAdapter());
    }

    class ImageAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mUris.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public static void launch(Activity activity, ArrayList<String> uris, int position) {
        Intent intent = new Intent(activity, ImageGalleryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.putExtra(EXTRA_URIS, uris);
        intent.putExtra(EXTRA_POSITION, position);
        activity.startActivity(intent);
    }
}
