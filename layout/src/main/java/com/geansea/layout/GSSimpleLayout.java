package com.geansea.layout;

import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import java.util.LinkedList;
import java.util.List;

public class GSSimpleLayout extends GSLayout {
    public static final class Builder {
        private TextPaint paint;
        private int width;
        private int height;
        private float indent;
        private float punctuationCompressRate;
        private Alignment alignment;
        private float lineSpacing;
        private float paragraphSpacing;
        private boolean vertical;

        public static Builder obtain(@NonNull TextPaint paint) {
            return new Builder(paint);
        }

        private Builder(@NonNull TextPaint paint) {
            this.paint = paint;
            alignment = Alignment.ALIGN_NORMAL;
            punctuationCompressRate = 1;
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

        public GSSimpleLayout build(@NonNull String text, int start, int end) {
            GSSimpleLayout layout = new GSSimpleLayout(text, start, end, this);
            layout.doLayout();
            return layout;
        }

        public GSSimpleLayout build(@NonNull String text) {
            return build(text, 0, text.length());
        }
    }

    private static final float SIZE_EXTEND_TIMES = 1.3f;

    private final GSCharacterUtils characterUtils;
    private final GSLayoutUtils layoutUtils;

    private GSSimpleLayout(String text, int start, int end, Builder builder) {
        super(
                text,
                start,
                end,
                builder.paint,
                builder.width,
                builder.height,
                builder.indent,
                builder.punctuationCompressRate,
                builder.alignment,
                builder.lineSpacing,
                builder.paragraphSpacing,
                builder.vertical
        );
        characterUtils = new GSCharacterUtils();
        layoutUtils = new GSLayoutUtils(characterUtils);
    }

    private void doLayout() {
        if (getVertical()) {
            doVerticalLayout();
        } else {
            doHorizontalLayout();
        }
    }

    private void doHorizontalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = getPaint().getTextSize();
        int lineLocation = getStart();
        float maxWidth = 0;
        float lineTop = 0;
        while (lineLocation < getText().length()) {
            GSLayoutLine line = layoutLine(lineLocation);
            line.origin.y = lineTop + line.ascent;
            lineTop = line.origin.y + line.descent;
            if (lineTop > getHeight()) {
                break;
            }
            lines.add(line);
            lineLocation = line.end;
            maxWidth = Math.max(maxWidth, line.size);
            lineTop += fontSize * getLineSpacing();
            if (characterUtils.isNewline(line.glyphs.get(line.glyphs.size() - 1).code())) {
                lineTop += fontSize * getParagraphSpacing();
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            setEnd(last.end);
            setUsedWidth(maxWidth);
            setUsedHeight(last.getUsedRect().bottom);
        }
        setLines(lines);
    }

    private void doVerticalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = getPaint().getTextSize();
        int lineLocation = getStart();
        float maxHeight = 0;
        float lineRight = getWidth();
        while (lineLocation < getText().length()) {
            GSLayoutLine line = layoutLine(lineLocation);
            line.origin.x = lineRight - line.ascent;
            lineRight = line.origin.x - line.descent;
            if (lineRight < 0) {
                break;
            }
            lines.add(line);
            lineLocation = line.end;
            maxHeight = Math.max(maxHeight, line.size);
            lineRight -= fontSize * getLineSpacing();
            if (characterUtils.isNewline(line.glyphs.get(line.glyphs.size() - 1).code())) {
                lineRight -= fontSize * getParagraphSpacing();
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            setEnd(last.end);
            setUsedWidth(getWidth() - last.getUsedRect().left);
            setUsedHeight(maxHeight);
        }
        setLines(lines);
    }

    private GSLayoutLine layoutLine(int start) {
        String text = getText().toString();
        TextPaint paint = getPaint();
        float fontSize = paint.getTextSize();
        float indent = 0;
        if (0 == start || characterUtils.isNewline(text.charAt(start - 1))) {
            indent = fontSize * getIndent();
        }
        float size = getVertical() ? getHeight() : getWidth();
        int count = layoutUtils.breakText(text, paint, start, getEnd(), (size - indent) * SIZE_EXTEND_TIMES);
        LinkedList<GSLayoutGlyph> glyphs;
        if (getVertical()) {
            text = characterUtils.replaceTextForVertical(text);
            glyphs = layoutUtils.getVertGlyphs(text, paint, start, count, indent);
        } else {
            glyphs = layoutUtils.getHoriGlyphs(text, paint, start, count, indent);
        }
        compressGlyphs(glyphs);
        int breakPos = breakPosForGlyphs(glyphs, size);
        glyphs = new LinkedList<>(glyphs.subList(0, breakPos));
        adjustEndGlyphs(glyphs);
        PointF origin = adjustGlyphs(glyphs, size);
        if (getVertical()) {
            return GSLayoutLine.createVerticalLine(text, glyphs, origin);
        } else {
            return GSLayoutLine.createHorizontalLine(text, glyphs, origin);
        }
    }

    private void compressGlyphs(LinkedList<GSLayoutGlyph> glyphs) {
        float fontSize = getPaint().getTextSize();
        float move = 0;
        GSLayoutGlyph prevGlyph = null;
        for (GSLayoutGlyph thisGlyph : glyphs) {
            char code = thisGlyph.code();
            char prevCode = (prevGlyph != null) ? prevGlyph.code() : 0;
            // Add gap
            if (characterUtils.shouldAddGap(prevCode, code)) {
                move += fontSize / 6;
            }
            // Punctuation compress
            if (characterUtils.canCompressLeft(code)) {
                if (0 == prevCode) {
                    if (thisGlyph.isFullWidth()) {
                        thisGlyph.compressLeft = thisGlyph.width * getPunctuationCompressRate();
                        move -= thisGlyph.compressLeft;
                    }
                }
                if (characterUtils.canCompressRight(prevCode)) {
                    if (thisGlyph.isFullWidth()) {
                        thisGlyph.compressLeft = thisGlyph.width * getPunctuationCompressRate() / 2;
                        move -= thisGlyph.compressLeft;
                    }
                    if (prevGlyph != null && prevGlyph.isFullWidth()) {
                        prevGlyph.compressRight = prevGlyph.width * getPunctuationCompressRate() / 2;
                        move -= prevGlyph.compressRight;
                    }
                }
            }
            if (characterUtils.canCompressRight(code)) {
                if (characterUtils.canCompressRight(prevCode)) {
                    if (prevGlyph != null && prevGlyph.isFullWidth()) {
                        prevGlyph.compressRight = prevGlyph.width * getPunctuationCompressRate() / 2;
                        move -= prevGlyph.compressRight;
                    }
                }
            }
            // Move
            if (getVertical()) {
                thisGlyph.y += move;
            } else {
                thisGlyph.x += move;
            }
            // Fix CRLF width
            if (characterUtils.isNewline(code)) {
                move -= thisGlyph.width;
                thisGlyph.compressRight = thisGlyph.width;
            }
            prevGlyph = thisGlyph;
        }
    }

    private int breakPosForGlyphs(List<GSLayoutGlyph> glyphs, float size) {
        int breakPos = 0;
        int forceBreakPos = 0;
        for (int i = 1; i < glyphs.size(); ++i) {
            GSLayoutGlyph prevGlyph = glyphs.get(i - 1);
            GSLayoutGlyph thisGlyph = glyphs.get(i);
            if (characterUtils.canBreak(prevGlyph.code(), thisGlyph.code())) {
                breakPos = i;
            }
            float currentSize = getVertical() ? thisGlyph.getUsedRect().bottom : thisGlyph.getUsedRect().right;
            if (currentSize > size) {
                if (characterUtils.canCompressRight(thisGlyph.code()) && thisGlyph.isFullWidth()) {
                        float compressRight = thisGlyph.width * getPunctuationCompressRate();
                        currentSize = (getVertical() ? thisGlyph.getRect().bottom : thisGlyph.getRect().right) - compressRight;
                }
            }
            if (currentSize > size) {
                forceBreakPos = i;
                break;
            }
        }
        // If all glyphs can be in line
        if (0 == forceBreakPos) {
            breakPos = glyphs.size();
        }
        // If no valid break position
        if (0 == breakPos) {
            breakPos = forceBreakPos;
        }
        // Add next space if possible, for latin layout
        if (breakPos < glyphs.size()) {
            if (' ' == glyphs.get(breakPos).code()) {
                ++breakPos;
            }
        }
        return breakPos;
    }

    private void adjustEndGlyphs(List<GSLayoutGlyph> glyphs) {
        // Compress last none CRLF glyph if possible
        int count = glyphs.size();
        GSLayoutGlyph lastGlyph = glyphs.get(count - 1);
        GSLayoutGlyph crlfGlyph = null;
        if (characterUtils.isNewline(lastGlyph.code())) {
            crlfGlyph = lastGlyph;
            if (count > 1) {
                lastGlyph = glyphs.get(count - 2);
            }
        }
        if (characterUtils.canCompressRight(lastGlyph.code()) && lastGlyph.isFullWidth()) {
            lastGlyph.compressRight = lastGlyph.width * getPunctuationCompressRate();
        }
        if (' ' == lastGlyph.code()) {
            lastGlyph.compressRight = lastGlyph.width;
        }
        if (crlfGlyph != null) {
            if (getVertical()) {
                crlfGlyph.y = lastGlyph.getUsedRect().bottom;
            } else {
                crlfGlyph.x = lastGlyph.getUsedRect().right;
            }
        }
    }

    private PointF adjustGlyphs(List<GSLayoutGlyph> glyphs, float width) {
        PointF origin = new PointF();
        GSLayoutGlyph lastGlyph = glyphs.get(glyphs.size() - 1);
        boolean reachEnd = characterUtils.isNewline(lastGlyph.code());
        if (lastGlyph.end == getText().length()) {
            reachEnd = true;
        }
        float lineSize = getVertical() ? lastGlyph.getUsedRect().bottom : lastGlyph.getUsedRect().right;
        float adjustSize = width - lineSize;
        if (adjustSize > 0) {
            switch (getAlignment()) {
                case ALIGN_NORMAL:
                    break;
                case ALIGN_OPPOSITE:
                    if (getVertical()) {
                        origin.y += adjustSize;
                    } else {
                        origin.x += adjustSize;
                    }
                    break;
                case ALIGN_CENTER:
                    if (getVertical()) {
                        origin.y += adjustSize / 2;
                    } else {
                        origin.x += adjustSize / 2;
                    }
                    break;
                case ALIGN_JUSTIFY:
                    if (!reachEnd) {
                        int stretchCount = 0;
                        for (int i = 1; i < glyphs.size(); ++i) {
                            GSLayoutGlyph prevGlyph = glyphs.get(i - 1);
                            GSLayoutGlyph thisGlyph = glyphs.get(i);
                            if (characterUtils.canStretch(prevGlyph.code(), thisGlyph.code())) {
                                ++stretchCount;
                            }
                        }
                        float stretchSize = adjustSize / stretchCount;
                        float move = 0;
                        for (int i = 1; i < glyphs.size(); ++i) {
                            GSLayoutGlyph prevGlyph = glyphs.get(i - 1);
                            GSLayoutGlyph thisGlyph = glyphs.get(i);
                            if (characterUtils.canStretch(prevGlyph.code(), thisGlyph.code())) {
                                move += stretchSize;
                            }
                            if (getVertical()) {
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
        }
        return origin;
    }
}
