package com.geansea.layout;

import android.graphics.Canvas;
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
        drawBackgroundColor();
        drawUnderline();
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
        drawStrikeThrough();
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
            ascent = getGlyphsMaxAscent(glyphs, vertical);
            descent = getGlyphsMaxDescent(glyphs, vertical);
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

    private static float getGlyphsMaxAscent(LinkedList<GSLayoutGlyph> glyphs, boolean vertical) {
        float ascent = 0;
        if (glyphs == null || glyphs.size() == 0) {
            return ascent;
        }
        for (GSLayoutGlyph glyph : glyphs) {
            float glyphAscent = vertical ? glyph.getUsedRect().right : -glyph.getUsedRect().top;
            ascent = Math.max(ascent, glyphAscent);
        }
        return ascent;
    }

    private static float getGlyphsMaxDescent(LinkedList<GSLayoutGlyph> glyphs, boolean vertical) {
        float descent = 0;
        if (glyphs == null || glyphs.size() == 0) {
            return descent;
        }
        for (GSLayoutGlyph glyph : glyphs) {
            float glyphDescent = vertical ? -glyph.getUsedRect().left : glyph.getUsedRect().bottom;
            descent = Math.max(descent, glyphDescent);
        }
        return descent;
    }

    private void drawBackgroundColor() {
        if (!(text instanceof Spanned)) {
            return;
        }
        Spanned spanned = (Spanned) text;
    }

    private void drawUnderline() {
        if (!(text instanceof Spanned)) {
            return;
        }
        Spanned spanned = (Spanned) text;
        UnderlineSpan[] spans = spanned.getSpans(start, end, UnderlineSpan.class);
        if (spans == null || spans.length == 0) {
            return;
        }
        ArrayList<GSLayoutGlyph> glyphs = getGlyphs();
        for (UnderlineSpan span : spans) {
            int spanStart = Math.max(spanned.getSpanStart(span), start);
            int spanEnd = Math.min(spanned.getSpanEnd(span), end);
            if (spanStart >= spanEnd) {
                continue;
            }
            LinkedList<GSLayoutGlyph> spanGlyphs = new LinkedList<>(glyphs.subList(spanStart, spanEnd));
            GSLayoutGlyph first = spanGlyphs.getFirst();
            GSLayoutGlyph last = spanGlyphs.getLast();
            float start = vertical ? first.getUsedRect().top : first.getUsedRect().left;
            float end = vertical ? last.getUsedRect().bottom : last.getUsedRect().right;
            float spanAscent = getGlyphsMaxAscent(spanGlyphs, vertical);
            float spanDescent = getGlyphsMaxDescent(spanGlyphs, vertical);
            RectF spanRect;
            if (vertical) {
                spanRect = new RectF(-descent, originY, ascent, originY + size);
            } else {
                spanRect = new RectF(first.getUsedRect().left, -ascent, last.getUsedRect().right, descent);
            }
            spanRect.offset(originX, originY);
        }
    }

    private void drawStrikeThrough() {
        if (!(text instanceof Spanned)) {
            return;
        }
        Spanned spanned = (Spanned) text;
    }

    private int getGlyphIndexWithStart(int glyphStart) {
        if (glyphStart < start || glyphStart >= end) {
            return -1;
        }
        int glyphIndex = glyphStart - start;
        return 0;
    }

    private int getGlyphIndexWithEnd(int glyphEnd) {
        return getGlyphIndexWithStart(glyphEnd - 1);
    }

    private RectF getRect(int glyphStart, int glyphEnd) {
        return null;
    }

    private RectF getRectGlyphs(CharacterStyle span) {
        if (!(text instanceof Spanned)) {
            return null;
        }
        Spanned spanned = (Spanned) text;
        int spanStart = Math.max(spanned.getSpanStart(span), start);
        int spanEnd = Math.min(spanned.getSpanEnd(span), end);
        if (spanStart >= spanEnd) {
            return null;
        }
        int glyphStart = spanStart;
        int glyphEnd = spanEnd;
        //return new LinkedList<>(glyphs.subList(glyphStart, glyphEnd));
        return null;
    }
}
