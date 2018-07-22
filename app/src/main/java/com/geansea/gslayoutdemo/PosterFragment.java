package com.geansea.gslayoutdemo;

import android.app.Fragment;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

public class PosterFragment extends Fragment {
    private GSPosterView mPostView;
    private RadioGroup mConfigGroup;

    private RadioGroup.OnCheckedChangeListener mOnRadioCheckedChangeListener
            = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
            if (radioGroup == mConfigGroup) {
                switch (i) {
                    case R.id.config_1:
                        loadConfig1();
                        break;
                    case R.id.config_2:
                        loadConfig2();
                        break;
                    case R.id.config_3:
                        loadConfig3();
                        break;
                    default:
                }
            }
        }
    };

    public PosterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_poster, container, false);

        mPostView = view.findViewById(R.id.content);

        mConfigGroup = view.findViewById(R.id.config_group);
        mConfigGroup.setOnCheckedChangeListener(mOnRadioCheckedChangeListener);
        mConfigGroup.check(R.id.config_1);

        return view;
    }

    private void loadConfig1() {
        String config =
                "{" +
                    "\"content\": {" +
                        "\"l\": 60," +
                        "\"r\": 660," +
                        "\"t\": 60," +
                        "\"b\": 640," +
                        "\"vertical\": 0," +
                        "\"align\": \"start\"," +
                        "\"size\": 48," +
                        "\"color\": \"#1F3134\"" +
                    "}," +
                    "\"author\": {" +
                        "\"l\": 60," +
                        "\"r\": 660," +
                        "\"t\": 640," +
                        "\"b\": 700," +
                        "\"vertical\": 0," +
                        "\"align\": \"end\"," +
                        "\"size\": 40," +
                        "\"color\": \"#5A544B\"" +
                    "}" +
                "}";
        Bitmap background = decodeBackground(getResources(), R.drawable.poster_bg_1);
        String content = "If you're in pitch blackness, all you can do is sitting tight until your eyes get used to the dark.\n" +
                "如果你掉进了黑暗里，你能做的，不过是静心等待，直到你的双眼适应黑暗。";
        String author = "村上春树 — 《挪威的森林》";
        mPostView.setLayout(config, background, content, author);
    }

    private void loadConfig2() {
        String config =
                "{" +
                    "\"content\": {" +
                        "\"l\": 300," +
                        "\"r\": 660," +
                        "\"t\": 280," +
                        "\"b\": 1100," +
                        "\"vertical\": 1," +
                        "\"align\": \"start\"," +
                        "\"size\": 48," +
                        "\"color\": \"#0D0015\"" +
                    "}," +
                    "\"author\": {" +
                        "\"l\": 60," +
                        "\"r\": 120," +
                        "\"t\": 560," +
                        "\"b\": 1100," +
                        "\"vertical\": 1," +
                        "\"align\": \"end\"," +
                        "\"size\": 40," +
                        "\"color\": \"#2B2B2B\"" +
                    "}" +
                "}";
        Bitmap background = decodeBackground(getResources(), R.drawable.poster_bg_2);
        String content = "阿飞道：十七朵。\n" +
                "李寻欢的心沉落了下去，笑容也冻结。\n" +
                "因为他数过梅花。他了解一个人在数梅花时，那是多么寂寞。";
        String author = "古龙 — 《多情剑客无情剑》";
        mPostView.setLayout(config, background, content, author);
    }

    private void loadConfig3() {
        String config =
                "{" +
                    "\"content\": {" +
                        "\"l\": 60," +
                        "\"r\": 660," +
                        "\"t\": 60," +
                        "\"b\": 640," +
                        "\"vertical\": 0," +
                        "\"align\": \"center\"," +
                        "\"size\": 48," +
                        "\"color\": \"#EAF4FC\"" +
                    "}," +
                    "\"author\": {" +
                        "\"l\": 330," +
                        "\"r\": 390," +
                        "\"t\": 640," +
                        "\"b\": 1220," +
                        "\"vertical\": 1," +
                        "\"align\": \"end\"," +
                        "\"size\": 40," +
                        "\"color\": \"#F8F4E6\"" +
                    "}" +
                "}";
        Bitmap background = decodeBackground(getResources(), R.drawable.poster_bg_3);
        String content = "钱当然很重要，这我不是不知道；我一天何尝不为钱而受熬苦！\n" +
                "可是，我又觉得，人活这一辈子，还应该有些另外的什么才对……";
        String author = "路遥 — 《平凡的世界》";
        mPostView.setLayout(config, background, content, author);
    }

    private Bitmap decodeBackground(Resources res, int resId) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inMutable = true;
        opts.inScaled = false;
        return BitmapFactory.decodeResource(res, resId, opts);
    }
}
