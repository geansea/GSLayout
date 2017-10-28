package com.geansea.layout;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.text.TextPaint;

import java.util.LinkedList;
import java.util.List;

public class GSSimpleLayout extends GSLayout {
    public static GSSimpleLayout build(@NonNull String text, int start, int end, @NonNull Parameters parameters) {
        GSSimpleLayout layout = new GSSimpleLayout(text, start, end, parameters);
        layout.doLayout();
        return layout;
    }

    public static GSSimpleLayout build(@NonNull String text, @NonNull Parameters parameters) {
        return build(text, 0, text.length(), parameters);
    }

    private static final float SIZE_EXTEND_TIMES = 1.3f;

    private final GSCharacterUtils characterUtils;
    private final GSLayoutUtils layoutUtils;

    private GSSimpleLayout(String text, int start, int end, Parameters parameters) {
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
            if (characterUtils.isNewline(line.getLastGlyph().code())) {
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
            if (characterUtils.isNewline(line.getLastGlyph().code())) {
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
        String text = getText().toString();
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
            glyphs = layoutUtils.getVertGlyphs(text, paint, start, count, indent);
        } else {
            glyphs = layoutUtils.getHoriGlyphs(text, paint, start, count, indent);
        }
        layoutUtils.compressGlyphs(glyphs, getParameters());
        //compressGlyphs(glyphs);
        int breakPos = breakPosForGlyphs(glyphs, size);
        glyphs = new LinkedList<>(glyphs.subList(0, breakPos));
        adjustEndGlyphs(glyphs);
        PointF origin = adjustGlyphs(glyphs, size);
        if (getParameters().vertical) {
            return GSLayoutLine.createVerticalLine(text, glyphs, origin);
        } else {
            return GSLayoutLine.createHorizontalLine(text, glyphs, origin);
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
            float currentSize = getParameters().vertical ? thisGlyph.getUsedRect().bottom : thisGlyph.getUsedRect().right;
            if (currentSize > size) {
                if (characterUtils.canCompressRight(thisGlyph.code()) && thisGlyph.isFullWidth()) {
                    float compressRight = thisGlyph.width * getParameters().punctuationCompressRate;
                    currentSize = (getParameters().vertical ? thisGlyph.getRect().bottom : thisGlyph.getRect().right) - compressRight;
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
            lastGlyph.compressRight = lastGlyph.width * getParameters().punctuationCompressRate;
        }
        if (' ' == lastGlyph.code()) {
            lastGlyph.compressRight = lastGlyph.width;
        }
        if (crlfGlyph != null) {
            if (getParameters().vertical) {
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
        float lineSize = getParameters().vertical ? lastGlyph.getUsedRect().bottom : lastGlyph.getUsedRect().right;
        float adjustSize = width - lineSize;
        if (adjustSize > 0) {
            switch (getParameters().alignment) {
                case ALIGN_NORMAL:
                    break;
                case ALIGN_OPPOSITE:
                    if (getParameters().vertical) {
                        origin.y += adjustSize;
                    } else {
                        origin.x += adjustSize;
                    }
                    break;
                case ALIGN_CENTER:
                    if (getParameters().vertical) {
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
                            if (getParameters().vertical) {
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
