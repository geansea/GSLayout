package com.geansea.layout;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
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
        private Rect rect;
        private int maxLineCount;
        private float indent;
        private float punctuationCompressRate;
        private Alignment textAlignment;
        private Alignment textEndAlignment;
        private Alignment lineAlignment;
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

        public Builder setRect(Rect rect) {
            this.rect = rect;
            return this;
        }

        public Builder setRect(int left, int top, int right, int bottom) {
            return setRect(new Rect(left, top, right, bottom));
        }

        public Builder setMaxLineCount(int maxLineCount) {
            this.maxLineCount = maxLineCount;
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

        public Builder setTextAlignment(Alignment alignment, Alignment endAlignment) {
            textAlignment = alignment;
            textEndAlignment = endAlignment;
            return this;
        }

        public Builder setLineAlignment(Alignment alignment) {
            lineAlignment = alignment;
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

        public GSLayout build(CharSequence text) {
            return build(text, 0, text.length(), true, true);
        }

        public GSLayout build(CharSequence text,
                              int start,
                              int end,
                              boolean asParaStart,
                              boolean asParaEnd) {
            start = Math.max(start, 0);
            end = Math.min(end, text.length());
            if (start >= end) {
                return null;
            }
            GSLayout layout = new GSLayout(this, text, start, end, asParaStart, asParaEnd);
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
            rect = new Rect();
            textAlignment = Alignment.ALIGN_NORMAL;
            textEndAlignment = Alignment.ALIGN_NORMAL;
            lineAlignment = Alignment.ALIGN_NORMAL;
        }
    }

    private static final float SIZE_EXTEND_TIMES = 1.3f;

    private final Builder builder;
    private final CharSequence text;
    private final int start;
    private final int end;
    private final boolean asParaStart;
    private final boolean asParaEnd;
    private int layoutEnd;
    private RectF usedRect;
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

    public int getLayoutEnd() {
        return layoutEnd;
    }

    public @NonNull
    Rect getRect() {
        return builder.rect;
    }

    public @NonNull
    RectF getUsedRect() {
        return usedRect;
    }

    public @NonNull
    ArrayList<GSLayoutLine> getLines() {
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

    private GSLayout(Builder builder,
                     CharSequence text,
                     int start,
                     int end,
                     boolean asParaStart,
                     boolean asParaEnd) {
        this.builder = builder;
        this.text = text;
        this.start = start;
        this.end = end;
        this.asParaStart = asParaStart;
        this.asParaEnd = asParaEnd;
        layoutEnd = start;
        usedRect = new RectF();
        lines = new LinkedList<>();
    }

    private void doHorizontalLayout() {
        float fontSize = builder.getFontSize();
        float indent = fontSize * builder.indent;
        float lineGap = fontSize * builder.lineSpacing;
        float paraGap = lineGap + fontSize * builder.paragraphSpacing;
        int lineLocation = start;
        float lineTop = builder.rect.top;
        while (lineLocation < text.length()) {
            GSLayoutLine line = layoutLine(lineLocation, indent);
            PointF origin = line.getOrigin();
            RectF lineRect = line.getUsedRect();
            origin.y = lineTop - lineRect.top;
            line.setOrigin(origin);
            float lineBottom = origin.y + lineRect.bottom;
            if (lineBottom > builder.rect.bottom && !lines.isEmpty()) {
                break;
            }
            lines.add(line);
            if (builder.maxLineCount > 0 && lines.size() >= builder.maxLineCount) {
                break;
            }
            lineLocation = line.getEnd();
            lineTop = lineBottom + (line.isParaEnd() ? paraGap : lineGap);
        }
        layoutEnd = lines.getLast().getEnd();
        usedRect = adjustLines();
    }

    private void doVerticalLayout() {
        float fontSize = builder.getFontSize();
        float indent = fontSize * builder.indent;
        int lineLocation = start;
        float lineRight = builder.rect.right;
        while (lineLocation < text.length()) {
            GSLayoutLine line = layoutLine(lineLocation, indent);
            PointF origin = line.getOrigin();
            RectF lineRect = line.getUsedRect();
            origin.x = lineRight - lineRect.right;
            line.setOrigin(origin);
            float lineLeft = origin.x + lineRect.left;
            if (lineLeft < builder.rect.left && lines.size() > 0) {
                break;
            }
            lines.add(line);
            if (builder.maxLineCount > 0 && lines.size() > builder.maxLineCount) {
                break;
            }
            lineLocation = line.getEnd();
            lineRight = lineLeft - fontSize * builder.lineSpacing;
            if (line.isParaEnd()) {
                lineRight -= fontSize * builder.paragraphSpacing;
            }
        }
        layoutEnd = lines.getLast().getEnd();
        usedRect = adjustLines();
    }

    private GSLayoutLine layoutLine(int lineStart, float indent) {
        boolean isParaStart = lineStart == start ? asParaStart : GSCharUtils.isNewline(text.charAt(lineStart - 1));
        float lineIndent = isParaStart ? indent : 0;
        float pos = builder.vertical ? builder.rect.top : builder.rect.left;
        float endPos = builder.vertical ? builder.rect.bottom : builder.rect.right;
        float size = endPos - pos;
        int count = GSLayoutUtils.breakText(text, builder.paint, lineStart, end, size * SIZE_EXTEND_TIMES);
        LinkedList<GSLayoutGlyph> glyphs = GSLayoutUtils.getGlyphs(text, builder.paint, lineStart, count, builder.vertical, lineIndent);
        compressGlyphs(glyphs);
        int breakIndex = breakGlyphs(glyphs, size);
        glyphs = new LinkedList<>(glyphs.subList(0, breakIndex));
        adjustEndGlyphs(glyphs);
        int lineEnd = glyphs.getLast().end;
        boolean isParaEnd = lineEnd == end ? asParaEnd : GSCharUtils.isNewline(text.charAt(lineEnd - 1));
        PointF origin = adjustGlyphs(glyphs, pos, size, isParaEnd);
        return new GSLayoutLine(text, glyphs, origin, builder.vertical, isParaStart, isParaEnd);
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
            if (GSCharUtils.isNewline(glyph1)) {
                glyph1.compressEnd = glyph1.size;
                move -= glyph1.size;
            }
            glyph0 = glyph1;
        }
    }

    private int breakGlyphs(LinkedList<GSLayoutGlyph> glyphs, float size) {
        int breakIndex = 0;
        int index = 0;
        GSLayoutGlyph glyph0 = null;
        for (GSLayoutGlyph glyph1 : glyphs) {
            if (GSCharUtils.canBreak(glyph0, glyph1)) {
                breakIndex = index;
            }
            float currentSize = glyph1.getUsedEndPos();
            if (currentSize > size) {
                if (GSCharUtils.shouldCompressEnd(glyph1) && GSCharUtils.canCompress(glyph1)) {
                    float compressEnd = glyph1.size * builder.punctuationCompressRate;
                    currentSize = glyph1.getEndPos() - compressEnd;
                }
            }
            if (currentSize > size) {
                break;
            }
            ++index;
            glyph0 = glyph1;
        }
        // If all glyphs can be in line
        if (index == glyphs.size()) {
            breakIndex = index;
        }
        // If no valid break position
        if (0 == breakIndex) {
            breakIndex = index;
        }
        // Add next space if possible, for latin layout
        if (breakIndex < glyphs.size()) {
            if (glyphs.get(breakIndex).code() == ' ') {
                ++breakIndex;
            }
        }
        return breakIndex;
    }

    private void adjustEndGlyphs(LinkedList<GSLayoutGlyph> glyphs) {
        // Compress last none CRLF glyph if possible
        GSLayoutGlyph lastGlyph = glyphs.getLast();
        GSLayoutGlyph crlfGlyph = null;
        if (GSCharUtils.isNewline(lastGlyph) && glyphs.size() > 1) {
            crlfGlyph = glyphs.removeLast();
            lastGlyph = glyphs.getLast();
        }
        if (GSCharUtils.shouldCompressEnd(lastGlyph) && GSCharUtils.canCompress(lastGlyph)) {
            lastGlyph.compressEnd = lastGlyph.size * builder.punctuationCompressRate;
        }
        if (lastGlyph.code() == ' ') {
            lastGlyph.compressEnd = lastGlyph.size;
        }
        if (crlfGlyph != null) {
            if (builder.vertical) {
                crlfGlyph.y = lastGlyph.getUsedEndPos();
            } else {
                crlfGlyph.x = lastGlyph.getUsedEndPos();
            }
            glyphs.addLast(crlfGlyph);
        }
    }

    private PointF adjustGlyphs(LinkedList<GSLayoutGlyph> glyphs, float pos, float size, boolean isParaEnd) {
        float originPos = pos;
        GSLayoutGlyph lastGlyph = glyphs.getLast();
        float adjustSize = size - lastGlyph.getUsedEndPos();
        if (adjustSize > 0) {
            Alignment alignment = isParaEnd ? builder.textEndAlignment : builder.textAlignment;
            switch (alignment) {
                case ALIGN_NORMAL:
                    break;
                case ALIGN_OPPOSITE:
                    originPos += adjustSize;
                    break;
                case ALIGN_CENTER:
                    originPos += adjustSize / 2;
                    break;
                case ALIGN_JUSTIFY:
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
                default:
                    break;
            }
        }
        return builder.vertical ? new PointF(0, originPos) : new PointF(originPos, 0);
    }

    private RectF adjustLines() {
        RectF lastLineRect = lines.getLast().getUsedRect();
        float adjustSize = builder.vertical ? (lastLineRect.left - builder.rect.left) : (builder.rect.bottom - lastLineRect.bottom);
        if (adjustSize > 0) {
            float move = 0;
            switch (builder.lineAlignment) {
                case ALIGN_NORMAL:
                    break;
                case ALIGN_OPPOSITE:
                    move += adjustSize;
                    break;
                case ALIGN_CENTER:
                    move += adjustSize / 2;
                    break;
                case ALIGN_JUSTIFY:
                    break;
                default:
                    break;
            }
            for (GSLayoutLine line : lines) {
                PointF origin = line.getOrigin();
                if (builder.vertical) {
                    origin.x -= move;
                } else {
                    origin.y += move;
                }
                line.setOrigin(origin);
            }
        }
        RectF rect = null;
        for (GSLayoutLine line : lines) {
            RectF lineRect = line.getUsedRect();
            if (rect == null) {
                rect = lineRect;
            } else {
                rect.union(lineRect);
            }
        }
        return rect;
    }
}
