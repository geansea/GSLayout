package com.geansea.gslayoutdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MainActivity extends AppCompatActivity {

    private GSTextView mTextMessage;
    private CheckBox mHelpingLineCheckBox;
    private CheckBox mVerticalCheckBox;
    private CheckBox mPunctuationCompressCheckBox;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_pure_text:
                    mTextMessage.setText("兩種用法都有。以我所見，以「」爲一級引號者較多，大約占80%吧。香港、台灣目前大部分印刷書籍和雜誌都採用此法。");
                    return true;
                case R.id.navigation_rich_text:
                    mTextMessage.setText("兩種用法都有。以我所見，以「」爲一級引號者較多，大約占80%吧。香港、台灣目前大部分印刷書籍和雜誌都採用此法。");
                    return true;
                case R.id.navigation_complex_area:
                    mTextMessage.setText("");
                    return true;
            }
            return false;
        }
    };

    private CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (buttonView == mHelpingLineCheckBox) {
                mTextMessage.setDrawHelpingLine(isChecked);
            } else if (buttonView == mVerticalCheckBox) {
                mTextMessage.setVertical(isChecked);
            } else if (buttonView == mPunctuationCompressCheckBox) {
                mTextMessage.setPunctuationCompressRate(isChecked ? 0.38f : 0);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (GSTextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_pure_text);

        mHelpingLineCheckBox = (CheckBox) findViewById(R.id.helping_line);
        mHelpingLineCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mHelpingLineCheckBox.setChecked(true);

        mVerticalCheckBox = (CheckBox) findViewById(R.id.vertical);
        mVerticalCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mVerticalCheckBox.setChecked(false);

        mPunctuationCompressCheckBox = (CheckBox) findViewById(R.id.punctuation_compress);
        mPunctuationCompressCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mPunctuationCompressCheckBox.setChecked(true);
    }

}
