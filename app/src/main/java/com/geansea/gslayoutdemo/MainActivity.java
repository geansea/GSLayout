package com.geansea.gslayoutdemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class MainActivity extends AppCompatActivity {

    private String mTestText;

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
                    mTextMessage.setText(mTestText);
                    return true;
                case R.id.navigation_rich_text:
                    return true;
                case R.id.navigation_complex_area:
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

        mTestText = "The quick brown fox jumps over the lazy dog.\n" +
                "Ligature test: ff ffi ffl fi fl ft\n" +
                "《决胜全面建成小康社会 夺取新时代中国特色社会主义伟大胜利》习近平代表第十八届中央委员会于2017年10月18日在中国共产党第十九次全国代表大会上向大会作的报告。\n" +
                "大会的主题是：不忘初心，牢记使命，高举中国特色社会主义伟大旗帜，决胜全面建成小康社会，夺取新时代中国特色社会主义伟大胜利，为实现中华民族伟大复兴的中国梦不懈奋斗。";

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
