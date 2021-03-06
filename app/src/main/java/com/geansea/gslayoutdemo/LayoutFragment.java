package com.geansea.gslayoutdemo;

import android.app.Fragment;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.MaskFilterSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

public class LayoutFragment extends Fragment {
    private boolean mRichTextMode;
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

    public LayoutFragment() {
    }

    public void setRichTextMode(boolean richTextMode) {
        mRichTextMode = richTextMode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTestStringText = "" +
                "The quick brown fox jumps over the lazy dog.\n" +
                "Ligature test: ff ffi ffl fi fl ft\n" +
                "中国网：“中国访谈 世界对话”，欢迎您的收看。" +
                "中国共产党第十九次全国代表大会于2017年10月18日上午9时在北京人民大会堂开幕。" +
                "习近平总书记代表十八届中央委员会向大会作报告。为更全面更深入地理解报告的内容，" +
                "《中国访谈》节目组特别邀请中国人民大学高校哲学社会科学发展战略研究中心研究员韩宇博士对十九大报告进行解读。\n" +
                "絵文字：\u2766\uD83D\uDC8C\uD83D\uDE02";

        SpannableStringBuilder builder = new SpannableStringBuilder();
        appendSpanned(builder, "字号大big", new RelativeSizeSpan(1.5f));
        appendSpanned(builder, "字号正常normal", null);
        appendSpanned(builder, "字号小small", new RelativeSizeSpan(0.6f));
        appendSpanned(builder, "加粗bold", new StyleSpan(Typeface.BOLD));
        appendSpanned(builder, "斜体italic", new StyleSpan(Typeface.ITALIC));
        appendSpanned(builder, "颜色foreground color", new ForegroundColorSpan(Color.BLUE));
        appendSpanned(builder, "背景色background color", new BackgroundColorSpan(Color.YELLOW));
        appendSpanned(builder, "下划线underline", new UnderlineSpan());
        appendSpanned(builder, "删除线strike through", new StrikethroughSpan());
        appendSpanned(builder, "模糊blur", new MaskFilterSpan(new BlurMaskFilter(4, BlurMaskFilter.Blur.NORMAL)));
        appendSpanned(builder, "下标", null);
        appendSpanned(builder, "sub", new SubscriptSpan() {
            @Override
            public void updateDrawState(TextPaint tp) {
                tp.baselineShift += (int) (tp.getTextSize() / 3);
                tp.setTextSize(tp.getTextSize() / 2);
            }

            @Override
            public void updateMeasureState(TextPaint tp) {
                tp.baselineShift += (int) (tp.getTextSize() / 3);
                tp.setTextSize(tp.getTextSize() / 2);
            }
        });
        appendSpanned(builder, "上标", null);
        appendSpanned(builder, "sup", new SuperscriptSpan() {
            @Override
            public void updateDrawState(TextPaint tp) {
                tp.baselineShift -= (int) (tp.getTextSize() / 2);
                tp.setTextSize(tp.getTextSize() / 2);
            }

            @Override
            public void updateMeasureState(TextPaint tp) {
                tp.baselineShift -= (int) (tp.getTextSize() / 2);
                tp.setTextSize(tp.getTextSize() / 2);
            }
        });
        appendSpanned(builder, "阴影shadow", new GSShadowSpan(4, 2, 2, Color.BLUE));
        mTestSpannedText = builder;

        mSystemFont = Typeface.DEFAULT;
        mSourceHanSansFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceHanSansSC-Light.otf");
        mSourceHanSerifFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/SourceHanSerifCN-Light.otf");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_layout, container, false);

        mLayoutView = view.findViewById(R.id.content);
        if (mRichTextMode) {
            mLayoutView.setText(mTestSpannedText);
        } else {
            mLayoutView.setText(mTestStringText);
        }

        mHelpingLineCheckBox = view.findViewById(R.id.helping_line);
        mHelpingLineCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mHelpingLineCheckBox.setChecked(true);

        mVerticalCheckBox = view.findViewById(R.id.vertical_layout);
        mVerticalCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mVerticalCheckBox.setChecked(false);

        mPunctuationCompressCheckBox = view.findViewById(R.id.punctuation_compress);
        mPunctuationCompressCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        mPunctuationCompressCheckBox.setChecked(true);

        mFontGroup = view.findViewById(R.id.font_group);
        mFontGroup.setOnCheckedChangeListener(mOnRadioCheckedChangeListener);
        mFontGroup.check(R.id.source_han_sans_font);

        mFontSizeGroup = view.findViewById(R.id.font_size_group);
        mFontSizeGroup.setOnCheckedChangeListener(mOnRadioCheckedChangeListener);
        mFontSizeGroup.check(R.id.font_size_middle);

        return view;
    }

    private static void appendSpanned(SpannableStringBuilder builder, String text, CharacterStyle span) {
        int start = builder.length();
        builder.append(text);
        if (span != null) {
            builder.setSpan(span, start, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
