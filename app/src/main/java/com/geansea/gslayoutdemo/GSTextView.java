package com.geansea.gslayoutdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.geansea.layout.GSLayout;
import com.geansea.layout.GSLayoutGlyph;
import com.geansea.layout.GSLayoutLine;
import com.geansea.layout.GSSimpleLayout;

public class GSTextView extends View {
    private TextPaint paint;
    private GSSimpleLayout layout;
    private String text;
    private boolean vertical;

    public GSTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new TextPaint();
        paint.setTextSize(36);
        paint.setAntiAlias(true);
    }

    public void setText(String text, boolean vertical) {
        this.text = text;
        this.vertical = vertical;
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layout = null;
        if (text != null) {
            GSSimpleLayout.Builder builder = GSSimpleLayout.Builder.obtain(text, paint)
                    .setIndent(2)
                    .setPuncCompressRate(0.38f)
                    .setAlignment(GSLayout.Alignment.ALIGN_JUSTIFY)
                    .setLineSpacing(0.2f)
                    .setParagraphSpacing(0.4f);
            layout = builder.build(0, getWidth(), getHeight(), vertical);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (layout != null) {
            layout.draw(canvas);
            drawHelpingLines(canvas);
        }
    }

    private void drawHelpingLines(Canvas canvas) {
        if (layout != null) {
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setAntiAlias(true);
            // Frame
            paint.setARGB(0x80, 0xFF, 0, 0);
            canvas.drawRect(layout.getUsedRect(), paint);
            // Lines
            paint.setARGB(0x80, 0, 0xFF, 0);
            for (int i = 0; i < layout.getLineCount(); ++i) {
                GSLayoutLine line = layout.getLine(i);
                canvas.drawRect(line.getUsedRect(), paint);
            }
            // Glyphs
            paint.setARGB(0x80, 0, 0, 0xFF);
            for (int i = 0; i < layout.getLineCount(); ++i) {
                GSLayoutLine line = layout.getLine(i);
                PointF lineOrigin = line.getOrigin();
                for (GSLayoutGlyph glyph : line.getGlyphs()) {
                    RectF glyphRect = glyph.getUsedRect();
                    glyphRect.offset(lineOrigin.x, lineOrigin.y);
                    canvas.drawRect(glyphRect, paint);
                }
            }
        }
    }
}
