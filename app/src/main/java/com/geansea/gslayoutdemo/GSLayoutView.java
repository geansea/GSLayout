package com.geansea.gslayoutdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.geansea.layout.GSLayout;
import com.geansea.layout.GSLayoutGlyph;
import com.geansea.layout.GSLayoutLine;

import java.util.List;

class GSLayoutView extends View {
    private GSLayout.Builder mBuilder;
    private CharSequence mText;
    private GSLayout mLayout;
    private boolean mDrawHelpingLine;

    public GSLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TextPaint paint = new TextPaint();
        paint.setAntiAlias(true);
        mBuilder = GSLayout.Builder.obtain(paint)
                .setIndent(2)
                .setTextAlignment(GSLayout.Alignment.ALIGN_JUSTIFY, GSLayout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(0.2f)
                .setParagraphSpacing(0.2f);
        // Disable hardware acceleration for this view
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    public void setText(CharSequence text) {
        mText = text;
        requestLayout();
    }

    public void setTypeface(Typeface typeface) {
        mBuilder.setTypeface(typeface);
        requestLayout();
    }

    public void setFontSize(float fontSize) {
        mBuilder.setFontSize(fontSize);
        requestLayout();
    }

    public void setPunctuationCompressRate(float punctuationCompressRate) {
        mBuilder.setPunctuationCompressRate(punctuationCompressRate);
        requestLayout();
    }

    public void setVertical(boolean vertical) {
        mBuilder.setVertical(vertical);
        requestLayout();
    }

    public void setDrawHelpingLine(boolean drawHelpingLine) {
        mDrawHelpingLine = drawHelpingLine;
        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mBuilder.setRect(0, 0, getWidth(), getHeight());
        mLayout = mBuilder.build(mText);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLayout == null) {
            return;
        }
        mLayout.draw(canvas);
        if (mDrawHelpingLine) {
            drawHelpingLines(canvas);
        }
    }

    private void drawHelpingLines(Canvas canvas) {
        if (mLayout == null) {
            return;
        }
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        // Frame
        paint.setARGB(0x80, 0xFF, 0, 0);
        canvas.drawRect(mLayout.getUsedRect(), paint);
        // Lines
        List<GSLayoutLine> lines = mLayout.getLines();
        paint.setARGB(0x80, 0, 0xFF, 0);
        for (GSLayoutLine line : lines) {
            canvas.drawRect(line.getUsedRect(), paint);
        }
        // Glyphs
        paint.setARGB(0x80, 0, 0, 0xFF);
        for (GSLayoutLine line : lines) {
            PointF lineOrigin = line.getOrigin();
            for (GSLayoutGlyph glyph : line.getGlyphs()) {
                RectF glyphRect = glyph.getUsedRect();
                glyphRect.offset(lineOrigin.x, lineOrigin.y);
                canvas.drawRect(glyphRect, paint);
            }
        }
    }
}
