package com.geansea.gslayoutdemo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private LayoutFragment mPureLayoutFragment;
    private LayoutFragment mRichLayoutFragment;
    private PosterFragment mPosterFragment;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_pure_text:
                    if (mPureLayoutFragment == null) {
                        mPureLayoutFragment = new LayoutFragment();
                        mPureLayoutFragment.setRichTextMode(false);
                    }
                    fragment = mPureLayoutFragment;
                    break;
                case R.id.navigation_rich_text:
                    if (mRichLayoutFragment == null) {
                        mRichLayoutFragment = new LayoutFragment();
                        mRichLayoutFragment.setRichTextMode(true);
                    }
                    fragment = mRichLayoutFragment;
                    break;
                case R.id.navigation_poster:
                    if (mPosterFragment == null) {
                        mPosterFragment = new PosterFragment();
                    }
                    fragment = mPosterFragment;
                    break;
            }
            if (fragment != null) {
                FragmentManager manager = getFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment, fragment);
                transaction.commit();
                return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_pure_text);
    }
}
