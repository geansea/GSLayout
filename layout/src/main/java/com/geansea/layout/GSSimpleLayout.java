package com.geansea.layout;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import java.util.LinkedList;

public class GSSimpleLayout extends GSLayout {
    public static GSSimpleLayout build(@NonNull String text, int start, int end, @NonNull Builder parameters) {
        start = Math.max(start, 0);
        end = Math.min(end, text.length());
        if (start >= end) {
            return null;
        }
        GSSimpleLayout layout = new GSSimpleLayout(text, start, end, parameters);
        layout.doLayout();
        return layout;
    }

    public static GSSimpleLayout build(@NonNull String text, @NonNull Builder parameters) {
        return build(text, 0, text.length(), parameters);
    }

    private static final float SIZE_EXTEND_TIMES = 1.3f;

    private final GSCharacterUtils characterUtils;
    private final GSLayoutUtils layoutUtils;

    private GSSimpleLayout(String text, int start, int end, Builder parameters) {
        super(text, start, end, parameters);
        characterUtils = new GSCharacterUtils();
        layoutUtils = new GSLayoutUtils(characterUtils);
    }

    private void doLayout() {
        if (getBuilder().vertical) {
            doVerticalLayout();
        } else {
            doHorizontalLayout();
        }
    }

    private void doHorizontalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = getBuilder().getFontSize();
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
            lineTop += fontSize * getBuilder().lineSpacing;
            if (characterUtils.isNewline(line.getLastGlyph())) {
                lineTop += fontSize * getBuilder().paragraphSpacing;
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            RectF lastRect = last.getUsedRect();
            setEnd(last.getEnd());
            setUsedWidth(maxWidth);
            setUsedHeight(lastRect.bottom);
        }
        setLines(lines);
    }

    private void doVerticalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = getBuilder().getFontSize();
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
            lineRight -= fontSize * getBuilder().lineSpacing;
            if (characterUtils.isNewline(line.getLastGlyph())) {
                lineRight -= fontSize * getBuilder().paragraphSpacing;
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            RectF lastRect = last.getUsedRect();
            setEnd(last.getEnd());
            setUsedWidth(getWidth() - lastRect.left);
            setUsedHeight(maxHeight);
        }
        setLines(lines);
    }

    private GSLayoutLine layoutLine(int start) {
        String text = getText().toString();
        TextPaint paint = getBuilder().paint;
        float fontSize = getBuilder().getFontSize();
        float indent = 0;
        if (0 == start || characterUtils.isNewline(text.charAt(start - 1))) {
            indent = fontSize * getBuilder().indent;
        }
        float size = getBuilder().vertical ? getHeight() : getWidth();
        int count = layoutUtils.breakText(text, paint, start, getEnd(), (size - indent) * SIZE_EXTEND_TIMES);
        LinkedList<GSLayoutGlyph> glyphs;
        if (getBuilder().vertical) {
            glyphs = layoutUtils.getVerticalGlyphs(text, paint, start, count, indent);
        } else {
            glyphs = layoutUtils.getHorizontalGlyphs(text, paint, start, count, indent);
        }
        compressGlyphs(glyphs);
        int breakPos = breakGlyphs(glyphs, size);
        glyphs = new LinkedList<>(glyphs.subList(0, breakPos));
        adjustEndGlyphs(glyphs);
        PointF origin = adjustGlyphs(glyphs, text.length(), size);
        if (getBuilder().vertical) {
            return GSLayoutLine.createVerticalLine(text, glyphs, origin);
        } else {
            return GSLayoutLine.createHorizontalLine(text, glyphs, origin);
        }
    }
}
