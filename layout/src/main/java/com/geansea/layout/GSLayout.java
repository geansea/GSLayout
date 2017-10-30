package com.geansea.layout;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;

import java.util.ArrayList;
import java.util.LinkedList;

public class GSLayout {
    public enum Alignment {
        ALIGN_NORMAL,
        ALIGN_OPPOSITE,
        ALIGN_CENTER,
        ALIGN_JUSTIFY,
    }

    public static final class Builder {
        private final TextPaint paint;
        private int width;
        private int height;
        private float indent;
        private float punctuationCompressRate;
        private Alignment alignment;
        private float lineSpacing;
        private float paragraphSpacing;
        private boolean vertical;

        public static Builder obtain(TextPaint paint) {
            return new Builder(paint);
        }

        public Builder setTypeface(Typeface typeface) {
            paint.setTypeface(typeface);
            return this;
        }

        public Builder setFontSize(float fontSize) {
            paint.setTextSize(fontSize);
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setIndent(float indent) {
            this.indent = indent;
            return this;
        }

        public Builder setPunctuationCompressRate(float punctuationCompressRate) {
            this.punctuationCompressRate = punctuationCompressRate;
            return this;
        }

        public Builder setAlignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        public Builder setLineSpacing(float lineSpacing) {
            this.lineSpacing = lineSpacing;
            return this;
        }

        public Builder setParagraphSpacing(float paragraphSpacing) {
            this.paragraphSpacing = paragraphSpacing;
            return this;
        }

        public Builder setVertical(boolean vertical) {
            this.vertical = vertical;
            return this;
        }

        public GSLayout build(CharSequence text, int start, int end) {
            start = Math.max(start, 0);
            end = Math.min(end, text.length());
            if (start >= end) {
                return null;
            }
            GSLayout layout = new GSLayout(this, text, start, end);
            if (vertical) {
                layout.doVerticalLayout();
            } else {
                layout.doHorizontalLayout();
            }
            return layout;
        }

        private float getFontSize() {
            return paint.getTextSize();
        }

        private Builder(TextPaint paint) {
            this.paint = paint;
            alignment = Alignment.ALIGN_NORMAL;
        }
    }

    private static final float SIZE_EXTEND_TIMES = 1.3f;

    private final Builder builder;
    private final CharSequence text;
    private final int start;
    private int end;
    private float usedWidth;
    private float usedHeight;
    private LinkedList<GSLayoutLine> lines;

    public CharSequence getText() {
        return text;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getWidth() {
        return builder.width;
    }

    public int getHeight() {
        return builder.height;
    }

    public float getUsedWidth() {
        return usedWidth;
    }

    public float getUsedHeight() {
        return usedHeight;
    }

    public RectF getUsedRect() {
        if (builder.vertical) {
            return new RectF(builder.width - usedWidth, 0, builder.width, usedHeight);
        } else {
            return new RectF(0, 0, usedWidth, usedHeight);
        }
    }

    public ArrayList<GSLayoutLine> getLines() {
        if (lines == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(lines);
    }

    public void draw(Canvas canvas) {
        if (lines == null || lines.size() == 0) {
            return;
        }
        for (GSLayoutLine line : lines) {
            line.draw(canvas);
        }
    }

    private GSLayout(Builder builder, CharSequence text, int start, int end) {
        this.builder = builder;
        this.text = text;
        this.start = start;
        this.end = end;
    }

    private void doHorizontalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = builder.getFontSize();
        int lineLocation = getStart();
        float maxWidth = 0;
        float lineTop = 0;
        while (lineLocation < getText().length()) {
            GSLayoutLine line = layoutLine(lineLocation);
            PointF lineOrigin = line.getOrigin();
            RectF lineRect = line.getUsedRect();
            lineOrigin.y = lineTop - lineRect.top;
            lineTop = lineOrigin.y + lineRect.bottom;
            if (lineTop > getHeight()) {
                break;
            }
            lines.add(line);
            lineLocation = line.getEnd();
            maxWidth = Math.max(maxWidth, lineRect.width());
            lineTop += fontSize * builder.lineSpacing;
            if (GSCharUtils.isNewline(line.getLastGlyph().code())) {
                lineTop += fontSize * builder.paragraphSpacing;
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            RectF lastRect = last.getUsedRect();
            end = last.getEnd();
            usedWidth = maxWidth;
            usedHeight = lastRect.bottom;
        }
        this.lines = lines;
    }

    private void doVerticalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = builder.getFontSize();
        int lineLocation = getStart();
        float maxHeight = 0;
        float lineRight = getWidth();
        while (lineLocation < getText().length()) {
            GSLayoutLine line = layoutLine(lineLocation);
            PointF lineOrigin = line.getOrigin();
            RectF lineRect = line.getUsedRect();
            lineOrigin.x = lineRight - lineRect.right;
            lineRight = lineOrigin.x + lineRect.left;
            if (lineRight < 0) {
                break;
            }
            lines.add(line);
            lineLocation = line.getEnd();
            maxHeight = Math.max(maxHeight, lineRect.height());
            lineRight -= fontSize * builder.lineSpacing;
            if (GSCharUtils.isNewline(line.getLastGlyph().code())) {
                lineRight -= fontSize * builder.paragraphSpacing;
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            RectF lastRect = last.getUsedRect();
            end = last.getEnd();
            usedWidth = getWidth() - lastRect.left;
            usedHeight = maxHeight;
        }
        this.lines = lines;
    }

    private GSLayoutLine layoutLine(int start) {
        float indent = 0;
        if (0 == start || GSCharUtils.isNewline(text.charAt(start - 1))) {
            indent = builder.getFontSize() * builder.indent;
        }
        float size = builder.vertical ? getHeight() : getWidth();
        int count = GSLayoutUtils.breakText(text, builder.paint, start, getEnd(), (size - indent) * SIZE_EXTEND_TIMES);
        LinkedList<GSLayoutGlyph> glyphs;
        if (builder.vertical) {
            glyphs = GSLayoutUtils.getVerticalGlyphs(text, builder.paint, start, count, indent);
        } else {
            glyphs = GSLayoutUtils.getHorizontalGlyphs(text, builder.paint, start, count, indent);
        }
        compressGlyphs(glyphs);
        int breakPos = breakGlyphs(glyphs, size);
        glyphs = new LinkedList<>(glyphs.subList(0, breakPos));
        adjustEndGlyphs(glyphs);
        PointF origin = adjustGlyphs(glyphs, text.length(), size);
        if (builder.vertical) {
            return GSLayoutLine.createVerticalLine(text, glyphs, origin);
        } else {
            return GSLayoutLine.createHorizontalLine(text, glyphs, origin);
        }
    }

    private void compressGlyphs(LinkedList<GSLayoutGlyph> glyphs) {
        float move = 0;
        GSLayoutGlyph glyph0 = null;
        for (GSLayoutGlyph glyph1 : glyphs) {
            // Add gap
            if (GSCharUtils.shouldAddGap(glyph0, glyph1)) {
                move += builder.getFontSize() / 6;
            }
            // Punctuation compress
            if (GSCharUtils.shouldCompressStart(glyph1)) {
                if (glyph0 == null && GSCharUtils.canCompress(glyph1)) {
                    glyph1.compressStart = glyph1.size * builder.punctuationCompressRate;
                    move -= glyph1.compressStart;
                }
                if (GSCharUtils.shouldCompressEnd(glyph0)) {
                    if (GSCharUtils.canCompress(glyph1)) {
                        glyph1.compressStart = glyph1.size * builder.punctuationCompressRate / 2;
                        move -= glyph1.compressStart;
                    }
                    if (GSCharUtils.canCompress(glyph0)) {
                        glyph0.compressEnd = glyph0.size * builder.punctuationCompressRate / 2;
                        move -= glyph0.compressEnd;
                    }
                }
            }
            if (GSCharUtils.shouldCompressEnd(glyph1)) {
                if (GSCharUtils.shouldCompressEnd(glyph0) && GSCharUtils.canCompress(glyph0)) {
                        glyph0.compressEnd = glyph0.size * builder.punctuationCompressRate / 2;
                        move -= glyph0.compressEnd;
                }
            }
            // Move
            if (builder.vertical) {
                glyph1.y += move;
            } else {
                glyph1.x += move;
            }
            // Fix CRLF width
            if (GSCharUtils.isNewline(glyph1.code())) {
                glyph1.compressEnd = glyph1.size;
                move -= glyph1.size;
            }
            glyph0 = glyph1;
        }
    }

    private int breakGlyphs(LinkedList<GSLayoutGlyph> glyphs, float size) {
        int breakPos = 0;
        int pos = 0;
        GSLayoutGlyph glyph0 = null;
        for (GSLayoutGlyph glyph1 : glyphs) {
            if (GSCharUtils.canBreak(glyph0, glyph1)) {
                breakPos = pos;
            }
            float currentSize = glyph1.getUsedEndSize();
            if (currentSize > size) {
                if (GSCharUtils.shouldCompressEnd(glyph1) && GSCharUtils.canCompress(glyph1)) {
                    float compressEnd = glyph1.size * builder.punctuationCompressRate;
                    currentSize = glyph1.getEndSize() - compressEnd;
                }
            }
            if (currentSize > size) {
                break;
            }
            pos++;
            glyph0 = glyph1;
        }
        // If all glyphs can be in line
        if (pos == glyphs.size()) {
            breakPos = pos;
        }
        // If no valid break position
        if (0 == breakPos) {
            breakPos = pos;
        }
        // Add next space if possible, for latin layout
        if (breakPos < glyphs.size()) {
            if (glyphs.get(breakPos).code() == ' ') {
                ++breakPos;
            }
        }
        return breakPos;
    }

    private void adjustEndGlyphs(LinkedList<GSLayoutGlyph> glyphs) {
        // Compress last none CRLF glyph if possible
        GSLayoutGlyph lastGlyph = glyphs.getLast();
        GSLayoutGlyph crlfGlyph = null;
        if (GSCharUtils.isNewline(lastGlyph.code()) && glyphs.size() > 1) {
            crlfGlyph = lastGlyph;
            glyphs.removeLast();
            lastGlyph = glyphs.peekLast();
        }
        if (GSCharUtils.shouldCompressEnd(lastGlyph) && GSCharUtils.canCompress(lastGlyph)) {
            lastGlyph.compressEnd = lastGlyph.size * builder.punctuationCompressRate;
        }
        if (lastGlyph.code() == ' ') {
            lastGlyph.compressEnd = lastGlyph.size;
        }
        if (crlfGlyph != null) {
            if (builder.vertical) {
                crlfGlyph.y = lastGlyph.getUsedEndSize();
            } else {
                crlfGlyph.x = lastGlyph.getUsedEndSize();
            }
            glyphs.addLast(crlfGlyph);
        }
    }

    private PointF adjustGlyphs(LinkedList<GSLayoutGlyph> glyphs, int textLength, float size) {
        PointF origin = new PointF();
        GSLayoutGlyph lastGlyph = glyphs.getLast();
        boolean lastLine = GSCharUtils.isNewline(lastGlyph.code()) || lastGlyph.end == textLength;
        float adjustSize = size - lastGlyph.getUsedEndSize();
        if (adjustSize == 0) {
            return origin;
        }
        switch (builder.alignment) {
            case ALIGN_NORMAL:
                break;
            case ALIGN_OPPOSITE:
                if (builder.vertical) {
                    origin.y += adjustSize;
                } else {
                    origin.x += adjustSize;
                }
                break;
            case ALIGN_CENTER:
                if (builder.vertical) {
                    origin.y += adjustSize / 2;
                } else {
                    origin.x += adjustSize / 2;
                }
                break;
            case ALIGN_JUSTIFY:
                if (!lastLine) {
                    int stretchCount = 0;
                    for (int i = 1; i < glyphs.size(); ++i) {
                        GSLayoutGlyph prevGlyph = glyphs.get(i - 1);
                        GSLayoutGlyph thisGlyph = glyphs.get(i);
                        if (GSCharUtils.canStretch(prevGlyph, thisGlyph)) {
                            ++stretchCount;
                        }
                    }
                    float stretchSize = adjustSize / stretchCount;
                    float move = 0;
                    for (int i = 1; i < glyphs.size(); ++i) {
                        GSLayoutGlyph prevGlyph = glyphs.get(i - 1);
                        GSLayoutGlyph thisGlyph = glyphs.get(i);
                        if (GSCharUtils.canStretch(prevGlyph, thisGlyph)) {
                            move += stretchSize;
                        }
                        if (builder.vertical) {
                            thisGlyph.y += move;
                        } else {
                            thisGlyph.x += move;
                        }
                    }
                    break;
                }
            default:
                break;
        }
        return origin;
    }
}
