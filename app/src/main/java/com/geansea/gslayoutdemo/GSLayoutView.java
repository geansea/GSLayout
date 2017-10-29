package com.geansea.gslayoutdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.Spanned;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.geansea.layout.GSLayout;
import com.geansea.layout.GSLayoutGlyph;
import com.geansea.layout.GSLayoutLine;
import com.geansea.layout.GSSimpleLayout;
import com.geansea.layout.GSSpannedLayout;

import java.util.ArrayList;

class GSLayoutView extends View {
    private GSLayout.Parameters parameters;
    private CharSequence text;
    private boolean complexMode;
    private ArrayList<GSLayoutLine> lines;
    private boolean drawHelpingLine;

    public GSLayoutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TextPaint paint = new TextPaint();
        paint.setAntiAlias(true);
        parameters = GSLayout.Parameters.obtain(paint)
                .setIndent(2)
                .setAlignment(GSLayout.Alignment.ALIGN_JUSTIFY)
                .setLineSpacing(0.2f)
                .setParagraphSpacing(0.2f);
    }

    public void setText(CharSequence text) {
        this.text = text;
        requestLayout();
    }

    public void setComplexMode(boolean complexMode) {
        this.complexMode = complexMode;
        requestLayout();
    }

    public void setTypeface(Typeface typeface) {
        parameters.setTypeface(typeface);
        requestLayout();
    }

    public void setFontSize(float fontSize) {
        parameters.setFontSize(fontSize);
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
        if (complexMode) {
            lines = layoutComplexLines(getWidth(), getHeight());
        } else {
            lines = layoutLines(getWidth(), getHeight(), 0);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lines == null || lines.size() == 0) {
            return;
        }
        for (GSLayoutLine line : lines) {
            line.draw(canvas);
        }
        if (drawHelpingLine) {
            drawHelpingLines(canvas);
        }
    }

    private ArrayList<GSLayoutLine> layoutLines(int width, int height, int start) {
        parameters.setWidth(width).setHeight(height);
        if (text instanceof String) {
            GSSimpleLayout layout = GSSimpleLayout.build((String) text, start, text.length(), parameters);
            if (layout != null) {
                return layout.getLines();
            }
        } else if (text instanceof Spanned) {
            GSSpannedLayout layout = GSSpannedLayout.build((Spanned) text, start, text.length(), parameters);
            if (layout != null) {
                return layout.getLines();
            }
        }
        return null;
    }

    private ArrayList<GSLayoutLine> layoutComplexLines(int width, int height) {
        return null;
    }

    private void drawHelpingLines(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        // Lines
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
