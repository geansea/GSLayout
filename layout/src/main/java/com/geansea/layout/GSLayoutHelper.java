package com.geansea.layout;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public final class GSLayoutHelper {
    public static int getGlyphIndexWithPosition(ArrayList<GSLayoutGlyph> glyphs, int position) {
        if (glyphs.size() == 0) {
            return -1;
        }
        int start = glyphs.get(0).start;
        int end = glyphs.get(glyphs.size() - 1).end;
        if (!(start <= position && position < end)) {
            return -1;
        }
        // Guess
        int glyphIndex = position - start;
        GSLayoutGlyph glyph = glyphs.get(glyphIndex);
        // Fix
        if (glyph.end <= position) {
            for (++glyphIndex; glyphIndex < glyphs.size(); ++glyphIndex) {
                glyph = glyphs.get(glyphIndex);
                if (glyph.start <= position && position < glyph.end) {
                    break;
                }
            }
        } else if (position < glyph.start) {
            for (--glyphIndex; glyphIndex >= 0; --glyphIndex) {
                glyph = glyphs.get(glyphIndex);
                if (glyph.start <= position && position < glyph.end) {
                    break;
                }
            }
        }
        return glyphIndex;
    }

    public static RectF getRect(ArrayList<GSLayoutGlyph> glyphs, int glyphIndexStart, int glyphIndexEnd, boolean vertical) {
        if (!(0 <= glyphIndexStart && glyphIndexStart < glyphIndexEnd && glyphIndexEnd <= glyphs.size())) {
            return null;
        }
        RectF startRect = glyphs.get(glyphIndexStart).getUsedRect();
        RectF endRect = glyphs.get(glyphIndexEnd - 1).getUsedRect();
        float ascent = getGlyphsMaxAscent(glyphs.subList(glyphIndexStart, glyphIndexEnd), vertical);
        float descent = getGlyphsMaxDescent(glyphs.subList(glyphIndexStart, glyphIndexEnd), vertical);
        if (vertical) {
            return new RectF(-descent, startRect.top, ascent, endRect.bottom);
        } else {
            return new RectF(startRect.left, -ascent, endRect.right, descent);
        }
    }

    static float getGlyphsMaxAscent(List<GSLayoutGlyph> glyphs, boolean vertical) {
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

    static float getGlyphsMaxDescent(List<GSLayoutGlyph> glyphs, boolean vertical) {
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
}
