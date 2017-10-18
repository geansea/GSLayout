package com.geansea.layout;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public class GSLayoutLine {
    public CharSequence getText() {
        return text;
    }

    public PointF getOrigin() {
        return origin;
    }

    public RectF getUsedRect() {
        if (vertical) {
            return new RectF(origin.x - descent, origin.y, origin.x + ascent, origin.y + size);
        } else {
            return new RectF(origin.x, origin.y - ascent, origin.x + size, origin.y + descent);
        }
    }

    public ArrayList<GSLayoutGlyph> getGlyphs() {
        return new ArrayList<>(glyphs);
    }

    void draw(Canvas canvas) {
        for (GSLayoutGlyph glyph : glyphs) {
            float glyphX = Math.round(origin.x + glyph.x);
            float glyphY = Math.round(origin.y + glyph.y);
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
    }

    private CharSequence text;
    private int start;
    int end;
    List<GSLayoutGlyph> glyphs;
    PointF origin;
    float ascent;
    float descent;
    float size;
    private boolean vertical;

    static GSLayoutLine createHorizontalLine(CharSequence text, List<GSLayoutGlyph> glyphs, PointF origin) {
        GSLayoutLine line = new GSLayoutLine();
        if (glyphs.size() > 0) {
            GSLayoutGlyph first = glyphs.get(0);
            GSLayoutGlyph last = glyphs.get(glyphs.size() - 1);
            line.start = first.start;
            line.end = last.end;
            line.size = last.getUsedRect().right;
            line.text = text.subSequence(line.start, line.end);
            line.glyphs = new ArrayList<>(glyphs);
            for (GSLayoutGlyph glyph : glyphs) {
                line.ascent = Math.max(line.ascent, glyph.ascent);
                line.descent = Math.max(line.descent, glyph.descent);
            }
            line.origin = origin;
            line.vertical = false;
        }
        return line;
    }

    static GSLayoutLine createVerticalLine(CharSequence text, List<GSLayoutGlyph> glyphs, PointF origin) {
        GSLayoutLine line = new GSLayoutLine();
        if (glyphs.size() > 0) {
            GSLayoutGlyph first = glyphs.get(0);
            GSLayoutGlyph last = glyphs.get(glyphs.size() - 1);
            line.start = first.start;
            line.end = last.end;
            line.size = last.getUsedRect().bottom;
            line.text = text.subSequence(line.start, line.end);
            line.glyphs = new ArrayList<>(glyphs);
            for (GSLayoutGlyph glyph : glyphs) {
                line.ascent = Math.max(line.ascent, glyph.getUsedRect().right);
                line.descent = Math.max(line.descent, -glyph.getUsedRect().left);
            }
            line.origin = origin;
            line.vertical = true;
        }
        return line;
    }

    private GSLayoutLine() {
    }
}
