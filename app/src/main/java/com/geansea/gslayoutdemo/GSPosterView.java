package com.geansea.gslayoutdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.geansea.layout.GSLayout;

import org.json.JSONException;
import org.json.JSONObject;

class GSPosterView extends android.support.v7.widget.AppCompatImageView {
    public GSPosterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Disable hardware acceleration for this view
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setLayout(String configJson, Bitmap bitmap, String content, String author) {
        PosterConfig config;
        try {
            JSONObject json = new JSONObject(configJson);
            config = new PosterConfig(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        drawPoster(config, bitmap, content, author);
        setImageBitmap(bitmap);
    }

    private static class PosterTextConfig {
        private final Rect frame;
        boolean vertical;
        GSLayout.Alignment textAlign;
        float fontSize;
        int textColor;

        PosterTextConfig(JSONObject json) throws JSONException {
            int l = json.getInt("l");
            int r = json.getInt("r");
            int t = json.getInt("t");
            int b = json.getInt("b");
            frame = new Rect(l, t, r, b);

            vertical = json.getInt("vertical") != 0;

            String align = json.getString("align");
            switch (align) {
                case "start":
                    textAlign = GSLayout.Alignment.ALIGN_NORMAL;
                    break;
                case "center":
                    textAlign = GSLayout.Alignment.ALIGN_CENTER;
                    break;
                case "end":
                    textAlign = GSLayout.Alignment.ALIGN_OPPOSITE;
                    break;
                default:
                    textAlign = GSLayout.Alignment.ALIGN_NORMAL;
            }

            fontSize = json.getInt("size");

            String color = json.getString("color");
            textColor = Color.parseColor(color);
        }

        void draw(Canvas canvas, String text) {
            TextPaint paint = new TextPaint();
            paint.setAntiAlias(true);
            paint.setTypeface(Typeface.DEFAULT);
            paint.setColor(textColor);
            GSLayout.Builder builder = GSLayout.Builder.obtain(paint)
                    .setRect(frame)
                    .setVertical(vertical)
                    .setFontSize(fontSize)
                    .setTextAlignment(textAlign, textAlign)
                    .setLineSpacing(0.3f)
                    .setParagraphSpacing(0.3f);
            GSLayout layout = builder.build(text);
            if (layout != null) {
                layout.draw(canvas);
            }
        }
    }

    private static class PosterConfig {
        private final PosterTextConfig contentConfig;
        private final PosterTextConfig authorConfig;

        PosterConfig(JSONObject json) throws JSONException {
            JSONObject content = json.getJSONObject("content");
            contentConfig = new PosterTextConfig(content);

            JSONObject author = json.getJSONObject("author");
            authorConfig = new PosterTextConfig(author);
        }

        void draw(Canvas canvas, String content, String author) {
            contentConfig.draw(canvas, content);
            authorConfig.draw(canvas, author);
        }
    }

    private static void drawPoster(PosterConfig config, Bitmap bitmap, String content, String author) {
        Canvas canvas = new Canvas(bitmap);
        config.draw(canvas, content, author);
    }
}
