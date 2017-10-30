package com.geansea.gslayoutdemo;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {
    private String mTestStringText;
    private Spanned mTestSpannedText;

    private Typeface mSystemFont;
    private Typeface mSourceHanSansFont;
    private Typeface mSourceHanSerifFont;

    private GSLayoutView mLayoutView;
    private CheckBox mHelpingLineCheckBox;
    private CheckBox mVerticalCheckBox;
    private CheckBox mPunctuationCompressCheckBox;
    private RadioGroup mFontGroup;
    private RadioGroup mFontSizeGroup;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_pure_text:
                    mLayoutView.setComplexMode(false);
                    mLayoutView.setText(mTestStringText);
                    return true;
                case R.id.navigation_rich_text:
                    mLayoutView.setComplexMode(false);
                    mLayoutView.setText(mTestSpannedText);
                    return true;
                case R.id.navigation_complex_area:
                    mLayoutView.setComplexMode(true);
                    mLayoutView.setText(mTestSpannedText);
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
                mLayoutView.setDrawHelpingLine(isChecked);
            } else if (buttonView == mVerticalCheckBox) {
                mLayoutView.setVertical(isChecked);
            } else if (buttonView == mPunctuationCompressCheckBox) {
                mLayoutView.setPunctuationCompressRate(isChecked ? 0.38f : 0);
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener mOnRadioCheckedChangeListener
            = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if (radioGroup == mFontGroup) {
                switch (i) {
                    case R.id.system_font:
                        mLayoutView.setTypeface(mSystemFont);
                        break;
                    case R.id.source_han_sans_font:
                        mLayoutView.setTypeface(mSourceHanSansFont);
                        break;
                    case R.id.source_han_serif_font:
                        mLayoutView.setTypeface(mSourceHanSerifFont);
                        break;
                    default:
                }
            } else if (radioGroup == mFontSizeGroup) {
                switch (i) {
                    case R.id.font_size_smallest:
                        mLayoutView.setFontSize(24);
                        break;
                    case R.id.font_size_small:
                        mLayoutView.setFontSize(36);
                        break;
                    case R.id.font_size_middle:
                        mLayoutView.setFontSize(48);
                        break;
                    case R.id.font_size_large:
                        mLayoutView.setFontSize(60);
                        break;
                    case R.id.font_size_largest:
                        mLayoutView.setFontSize(72);
                        break;
                    default:
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTestStringText = "" +
                "The quick brown fox jumps over the lazy dog.\n" +
                "Ligature test: ff ffi ffl fi fl ft\n" +
                "中国网：“中国访谈 世界对话”，欢迎您的收看。" +
                "中国共产党第十九次全国代表大会于2017年10月18日上午9时在北京人民大会堂开幕。" +
                "习近平总书记代表十八届中央委员会向大会作报告。为更全面更深入地理解报告的内容，" +
                "《中国访谈》节目组特别邀请中国人民大学高校哲学社会科学发展战略研究中心研究员韩宇博士对十九大报告进行解读。\n" +
                "絵文字：\u2766\uD83D\uDC8C\uD83D\uDE02";
        String html = "" +
                "<p>" +
                "<big>大</big>中<small>小</small>" +
                "<b>加粗</b>" +
                "<i>斜体</i>" +
                "<font color='#FF0000'>红</font>" +
                "<font color='#00FF00'>绿</font>" +
                "<font color='#0000FF'>蓝</font>" +
                "<span style='background-color:yellow'>背景色</span>" +
                "上标<sup><small>123</small></sup>" +
                "下标<sub><small>123</small></sub>" +
                "<u>下划线</u>" +
                "<s>删除线</s>" +
                "</p><p>" +
                "</p>";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mTestSpannedText = Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT);
        } else {
            // noinspection deprecation
            mTestSpannedText = Html.fromHtml(html);
        }

        mSystemFont = Typeface.DEFAULT;
        mSourceHanSansFont = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSansSC-Light.otf");
        mSourceHanSerifFont = Typeface.createFromAsset(getAssets(), "fonts/SourceHanSerifCN-Light.otf");

        mLayoutView = (GSLayoutView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_pure_text);

        mHelpingLineCheckBox = (CheckBox) findViewById(R.id.helping_line);
        mHelpingLineCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mHelpingLineCheckBox.setChecked(true);

        mVerticalCheckBox = (CheckBox) findViewById(R.id.vertical_layout);
        mVerticalCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mVerticalCheckBox.setChecked(false);

        mPunctuationCompressCheckBox = (CheckBox) findViewById(R.id.punctuation_compress);
        mPunctuationCompressCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mPunctuationCompressCheckBox.setChecked(true);

        mFontGroup = (RadioGroup) findViewById(R.id.font_group);
        mFontGroup.setOnCheckedChangeListener(mOnRadioCheckedChangeListener);
        mFontGroup.check(R.id.source_han_sans_font);

        mFontSizeGroup = (RadioGroup) findViewById(R.id.font_size_group);
        mFontSizeGroup.setOnCheckedChangeListener(mOnRadioCheckedChangeListener);
        mFontSizeGroup.check(R.id.font_size_middle);
    }
}
