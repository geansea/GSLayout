package com.geansea.gslayoutdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.geansea.layout.GSLayout;
import com.geansea.layout.GSLayoutGlyph;
import com.geansea.layout.GSLayoutLine;
import com.geansea.layout.GSSimpleLayout;

public class GSTextView extends View {
    private GSLayout.Parameters parameters;
    private String text;
    private GSSimpleLayout layout;
    private boolean drawHelpingLine;

    public GSTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TextPaint paint = new TextPaint();
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/SourceHanSerifCN-Light.otf"));
        parameters = GSLayout.Parameters.obtain(paint)
                .setFontSize(36)
                .setIndent(2)
                .setAlignment(GSLayout.Alignment.ALIGN_JUSTIFY)
                .setLineSpacing(0.2f)
                .setParagraphSpacing(0.4f);
    }

    public void setText(String text) {
        this.text = text;
        requestLayout();
    }

    public void setPunctuationCompressRate(float punctuationCompressRate) {
        parameters.setPunctuationCompressRate(punctuationCompressRate);
        requestLayout();
    }

    public void setVertical(boolean vertical) {
        parameters.setVertical(vertical);
        requestLayout();
    }

    public void setDrawHelpingLine(boolean drawHelpingLine) {
        this.drawHelpingLine = drawHelpingLine;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layout = null;
        if (text != null) {
            parameters.setWidth(getWidth()).setHeight(getHeight());
            layout = GSSimpleLayout.build(text, parameters);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (layout != null) {
            layout.draw(canvas);
            if (drawHelpingLine) {
                drawHelpingLines(canvas);
            }
        }
    }

    private void drawHelpingLines(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        // Frame
        paint.setARGB(0x80, 0xFF, 0, 0);
        canvas.drawRect(layout.getUsedRect(), paint);
        // Lines
        paint.setARGB(0x80, 0, 0xFF, 0);
        for (GSLayoutLine line : layout.getLines()) {
            canvas.drawRect(line.getUsedRect(), paint);
        }
        // Glyphs
        paint.setARGB(0x80, 0, 0, 0xFF);
        for (GSLayoutLine line : layout.getLines()) {
            PointF lineOrigin = line.getOrigin();
            for (GSLayoutGlyph glyph : line.getGlyphs()) {
                RectF glyphRect = glyph.getUsedRect();
                glyphRect.offset(lineOrigin.x, lineOrigin.y);
                canvas.drawRect(glyphRect, paint);
            }
        }
    }
}
