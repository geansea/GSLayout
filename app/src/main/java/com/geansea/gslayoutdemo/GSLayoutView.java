package com.geansea.gslayoutdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.geansea.layout.GSLayout;
import com.geansea.layout.GSLayoutGlyph;
import com.geansea.layout.GSLayoutLine;

import java.util.ArrayList;

abstract class GSLayoutView extends View {
    private GSLayout.Parameters parameters;
    private CharSequence text;
    private ArrayList<GSLayoutLine> lines;
    private boolean drawHelpingLine;

    public GSLayoutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
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
