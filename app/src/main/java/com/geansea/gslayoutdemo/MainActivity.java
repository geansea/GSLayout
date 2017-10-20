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
                "中国网：“中国访谈 世界对话”，欢迎您的收看。" +
                "中国共产党第十九次全国代表大会于2017年10月18日上午9时在北京人民大会堂开幕。" +
                "习近平总书记代表十八届中央委员会向大会作报告。为更全面更深入地理解报告的内容，" +
                "《中国访谈》节目组特别邀请中国人民大学高校哲学社会科学发展战略研究中心研究员韩宇博士对十九大报告进行解读。\n" +
                "Emoji support: \u2766\uD83D\uDC8C\uD83D\uDE02";

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
