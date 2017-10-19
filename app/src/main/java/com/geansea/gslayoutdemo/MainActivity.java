package com.geansea.gslayoutdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private GSTextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText("兩種用法都有。以我所見，以「」爲一級引號者較多，大約占80%吧。香港、台灣目前大部分印刷書籍和雜誌都採用此法。", false);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText("兩種用法都有。以我所見，以「」爲一級引號者較多，大約占80%吧。香港、台灣目前大部分印刷書籍和雜誌都採用此法。", true);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText("", false);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (GSTextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
