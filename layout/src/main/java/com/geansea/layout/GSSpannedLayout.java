package com.geansea.layout;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.Spanned;
import android.text.TextPaint;

import java.util.LinkedList;

public class GSSpannedLayout extends GSLayout {
    public static GSSpannedLayout build(@NonNull Spanned text, int start, int end, @NonNull Parameters parameters) {
        start = Math.max(start, 0);
        end = Math.min(end, text.length());
        if (start >= end) {
            return null;
        }
        GSSpannedLayout layout = new GSSpannedLayout(text, start, end, parameters);
        layout.doLayout();
        return layout;
    }

    public static GSSpannedLayout build(@NonNull Spanned text, @NonNull Parameters parameters) {
        return build(text, 0, text.length(), parameters);
    }

    private static final float SIZE_EXTEND_TIMES = 1.3f;

    private final GSCharacterUtils characterUtils;
    private final GSLayoutUtils layoutUtils;

    private GSSpannedLayout(Spanned text, int start, int end, Parameters parameters) {
        super(text, start, end, parameters);
        characterUtils = new GSCharacterUtils();
        layoutUtils = new GSLayoutUtils(characterUtils);
    }

    private void doLayout() {
        if (getParameters().vertical) {
            doVerticalLayout();
        } else {
            doHorizontalLayout();
        }
    }

    private void doHorizontalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = getParameters().getFontSize();
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
            lineTop += fontSize * getParameters().lineSpacing;
            if (characterUtils.isNewline(line.getLastGlyph())) {
                lineTop += fontSize * getParameters().paragraphSpacing;
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            setEnd(last.getEnd());
            setUsedWidth(maxWidth);
            setUsedHeight(last.getUsedRect().bottom);
        }
        setLines(lines);
    }

    private void doVerticalLayout() {
        LinkedList<GSLayoutLine> lines = new LinkedList<>();
        float fontSize = getParameters().getFontSize();
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
            lineRight -= fontSize * getParameters().lineSpacing;
            if (characterUtils.isNewline(line.getLastGlyph())) {
                lineRight -= fontSize * getParameters().paragraphSpacing;
            }
        }
        if (lines.size() > 0) {
            GSLayoutLine last = lines.getLast();
            setEnd(last.getEnd());
            setUsedWidth(getWidth() - last.getUsedRect().left);
            setUsedHeight(maxHeight);
        }
        setLines(lines);
    }

    private GSLayoutLine layoutLine(int start) {
        Spanned text = (Spanned) getText();
        TextPaint paint = getParameters().paint;
        float fontSize = getParameters().getFontSize();
        float indent = 0;
        if (0 == start || characterUtils.isNewline(text.charAt(start - 1))) {
            indent = fontSize * getParameters().indent;
        }
        float size = getParameters().vertical ? getHeight() : getWidth();
        int count = layoutUtils.breakText(text, paint, start, getEnd(), (size - indent) * SIZE_EXTEND_TIMES);
        LinkedList<GSLayoutGlyph> glyphs;
        if (getParameters().vertical) {
            glyphs = layoutUtils.getVerticalGlyphs(text, paint, start, count, indent);
        } else {
            glyphs = layoutUtils.getHorizontalGlyphs(text, paint, start, count, indent);
        }
        layoutUtils.compressGlyphs(glyphs, getParameters());
        int breakPos = layoutUtils.breakGlyphs(glyphs, getParameters(), size);
        glyphs = new LinkedList<>(glyphs.subList(0, breakPos));
        layoutUtils.adjustEndGlyphs(glyphs, getParameters());
        PointF origin = layoutUtils.adjustGlyphs(glyphs, getParameters(), text.length(), size);
        if (getParameters().vertical) {
            return GSLayoutLine.createVerticalLine(text, glyphs, origin);
        } else {
            return GSLayoutLine.createHorizontalLine(text, glyphs, origin);
        }
    }
}
