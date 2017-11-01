package com.geansea.layout;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.UnderlineSpan;

import java.util.ArrayList;
import java.util.LinkedList;

public class GSLayoutLine {
    private CharSequence text;
    private int start;
    private int end;
    private LinkedList<GSLayoutGlyph> glyphs;
    private float originX;
    private float originY;
    private float ascent;
    private float descent;
    private float size;
    private boolean vertical;

    public CharSequence getText() {
        return text;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public PointF getOrigin() {
        return new PointF(originX, originY);
    }

    public void setOrigin(PointF origin) {
        originX = origin.x;
        originY = origin.y;
    }

    public RectF getUsedRect() {
        if (vertical) {
            return new RectF(originX - descent, originY, originX + ascent, originY + size);
        } else {
            return new RectF(originX, originY - ascent, originX + size, originY + descent);
        }
    }

    public ArrayList<GSLayoutGlyph> getGlyphs() {
        return new ArrayList<>(glyphs);
    }

    public void draw(Canvas canvas) {
        // Decoration below text
        drawBackgroundColor(canvas);
        drawUnderline(canvas);
        // Text
        for (GSLayoutGlyph glyph : glyphs) {
            float glyphX = Math.round(originX + glyph.getDrawX());
            float glyphY = Math.round(originY + glyph.getDrawY());
            if (glyph.rotateForVertical) {
                canvas.save();
                canvas.translate(glyphX, glyphY);
                canvas.rotate(90);
                canvas.drawText(glyph.text, 0, 0, glyph.paint);
                canvas.restore();
            } else {
                canvas.drawText(glyph.text, glyphX, glyphY, glyph.paint);
            }
        }
        // Decoration above text
        drawStrikeThrough(canvas);
    }

    GSLayoutLine(CharSequence text, LinkedList<GSLayoutGlyph> glyphs, PointF origin, boolean vertical) {
        if (glyphs.size() > 0) {
            GSLayoutGlyph first = glyphs.getFirst();
            GSLayoutGlyph last = glyphs.getLast();
            this.text = text;
            start = first.start;
            end = last.end;
            this.glyphs = glyphs;
            originX = origin.x;
            originY = origin.y;
            ascent = GSLayoutHelper.getGlyphsMaxAscent(glyphs, vertical);
            descent = GSLayoutHelper.getGlyphsMaxDescent(glyphs, vertical);
            size = vertical ? last.getUsedRect().bottom : last.getUsedRect().right;
            this.vertical = vertical;
        }
    }

    GSLayoutGlyph getLastGlyph() {
        if (glyphs == null || glyphs.size() == 0) {
            return null;
        }
        return glyphs.getLast();
    }

    private void drawBackgroundColor(Canvas canvas) {
        if (!(text instanceof Spanned)) {
            return;
        }
        Spanned spanned = (Spanned) text;
    }

    private void drawUnderline(Canvas canvas) {
        if (!(text instanceof Spanned)) {
            return;
        }
        Spanned spanned = (Spanned) text;
        UnderlineSpan[] spans = spanned.getSpans(start, end, UnderlineSpan.class);
        if (spans == null || spans.length == 0) {
            return;
        }
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2); // TODO
        paint.setARGB(0xFF, 0, 0, 0); // TODO
        for (UnderlineSpan span : spans) {
            RectF spanRect = getRect(span);
            if (spanRect == null) {
                continue;
            }
            spanRect.offset(originX, originY);
            if (vertical) {
                float lineX = spanRect.left;
                canvas.drawLine(lineX, spanRect.top, lineX, spanRect.bottom, paint);
            } else {
                float lineY = (spanRect.bottom + originY) / 2;
                canvas.drawLine(spanRect.left, lineY, spanRect.right, lineY, paint);
            }
        }
    }

    private void drawStrikeThrough(Canvas canvas) {
        if (!(text instanceof Spanned)) {
            return;
        }
        Spanned spanned = (Spanned) text;
    }

    private RectF getRect(CharacterStyle span) {
        if (!(text instanceof Spanned)) {
            return null;
        }
        Spanned spanned = (Spanned) text;
        int spanStart = Math.max(spanned.getSpanStart(span), start);
        int spanEnd = Math.min(spanned.getSpanEnd(span), end);
        if (spanStart >= spanEnd) {
            return null;
        }
        ArrayList<GSLayoutGlyph> glyphs = getGlyphs();
        int glyphStart = GSLayoutHelper.getGlyphIndexWithPosition(glyphs, spanStart);
        int glyphEnd = GSLayoutHelper.getGlyphIndexWithPosition(glyphs, spanEnd - 1) + 1;
        return GSLayoutHelper.getRect(glyphs, glyphStart, glyphEnd, vertical);
    }
}
