package com.geansea.layout;

import android.text.TextPaint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class GSLayoutUtils {
    private Set<Character> compressLeftSet = null;
    private Set<Character> compressRightSet = null;
    private Set<Character> notLineBeginSet = null;
    private Set<Character> notLineEndSet = null;
    private Set<Character> rotateForVerticalSet = null;
    private Map<Character, Character> replaceForVerticalMap = null;

    GSLayoutUtils() {
    }

    ArrayList<GSLayoutGlyph> glyphsForSimpleLayout(
            String text,
            TextPaint paint,
            int start,
            int end,
            float width,
            boolean vertical) {
        int length = paint.breakText(text, start, end, true, width, null);
        for (int pos = start; pos < Math.min(start + length + 1, end); ++pos) {
            if (isNewline(text.charAt(pos))) {
                length = pos - start + 1;
                break;
            }
        }
        if (vertical) {
            return glyphsForSimpleVerticalLayout(text, paint, start, length);
        } else {
            return glyphsForSimpleHorizontalLayout(text, paint, start, length);
        }
    }

    private ArrayList<GSLayoutGlyph> glyphsForSimpleHorizontalLayout(
            String text,
            TextPaint paint,
            int start,
            int length) {
        float x = 0;
        float ascent = -paint.ascent();
        float descent = paint.descent();
        float widths[] = new float[length];
        paint.getTextWidths(text, start, start + length, widths);
        ArrayList<GSLayoutGlyph> glyphs = new ArrayList<>(length);
        GSLayoutGlyph glyph = null;
        for (int i = 0; i < length; ++i) {
            float glyphWidth = widths[i];
            if (glyphWidth == 0 && glyph != null) {
                glyph.end++;
                glyph.text = text.substring(glyph.start, glyph.end);
            } else {
                glyph = new GSLayoutGlyph();
                glyph.start = start + i;
                glyph.end = glyph.start + 1;
                glyph.text = text.substring(glyph.start, glyph.end);
                glyph.paint = paint;
                glyph.x = x;
                glyph.y = 0;
                glyph.ascent = ascent;
                glyph.descent = descent;
                glyph.width = glyphWidth;
                glyphs.add(glyph);
            }
            x += glyphWidth;
        }
        return glyphs;
    }

    private ArrayList<GSLayoutGlyph> glyphsForSimpleVerticalLayout(
            String text,
            TextPaint paint,
            int start,
            int length) {
        float y = 0;
        float ascent = -paint.ascent();
        float descent = paint.descent();
        float widths[] = new float[length];
        paint.getTextWidths(text, start, start + length, widths);
        ArrayList<GSLayoutGlyph> glyphs = new ArrayList<>(length);
        GSLayoutGlyph glyph = null;
        for (int i = 0; i < length; ++i) {
            float glyphWidth = widths[i];
            if (glyphWidth == 0 && glyph != null) {
                glyph.end++;
                glyph.text = text.substring(glyph.start, glyph.end);
            } else {
                glyph = new GSLayoutGlyph();
                glyph.start = start + i;
                glyph.end = glyph.start + 1;
                glyph.text = replaceForVertical(text.substring(glyph.start, glyph.end));
                glyph.paint = paint;
                if (shouldRotateForVertical(glyph.utf16Code())) {
                    glyph.x = (descent - ascent) / 2;
                    glyph.y = y;
                    glyph.ascent = ascent;
                    glyph.descent = descent;
                    glyph.width = glyphWidth;
                    glyph.rotateForVertical = true;
                } else {
                    float ascentForVertical = ascent * glyphWidth / (ascent + descent);
                    float descentForVertical = descent * glyphWidth / (ascent + descent);
                    glyph.x = -glyphWidth / 2;
                    glyph.y = y + ascentForVertical;
                    glyph.ascent = ascentForVertical;
                    glyph.descent = descentForVertical;
                    glyph.width = glyphWidth;
                    glyphWidth = ascentForVertical + descentForVertical;
                }
                glyphs.add(glyph);
            }
            y += glyphWidth;
        }
        return glyphs;
    }

    boolean isNewline(char code) {
        return ('\r' == code || '\n' == code);
    }

    boolean shouldAddGap(char prevCode, char code) {
        if (isAlphaDigit(prevCode) && isCjk(code)) {
            return true;
        }
        if (isCjk(prevCode) && isAlphaDigit(code)) {
            return true;
        }
        return false;
    }

    boolean canGlyphCompressLeft(GSLayoutGlyph glyph) {
        if (glyph.width < glyph.paint.getTextSize() * 0.9) {
            return false;
        }
        return canCompressLeft(glyph.utf16Code());
    }

    boolean canGlyphCompressRight(GSLayoutGlyph glyph) {
        if (glyph.width < glyph.paint.getTextSize() * 0.9) {
            return false;
        }
        return canCompressRight(glyph.utf16Code());
    }

    boolean canBreak(char prevCode, char code) {
        if (0 == prevCode) {
            return false;
        }
        // Always can break after space
        if (' ' == prevCode) {
            return true;
        }
        // false Break SPace
        if (0xA0 == prevCode) {
            return false;
        }
        // Space follow prev
        if (' ' == code || 0xA0 == code) {
            return false;
        }
        if (isAlphaDigit(prevCode)) {
            if (isAlphaDigit(code)) {
                return false;
            }
            if ('\'' == code || '\"' == code || '-' == code || '_' == code) {
                return false;
            }
        }
        if (isAlphaDigit(code)) {
            if ('\'' == prevCode || '\"' == prevCode || '\u2019' == prevCode) {
                return false;
            }
        }
        if (cannotLineBegin(code)) {
            return false;
        }
        if (cannotLineEnd(prevCode)) {
            return false;
        }
        return true;
    }

    boolean canStretch(char prevCode, char code) {
        if (!canBreak(prevCode, code)) {
            return false;
        }
        if ('/' == prevCode) {
            if (isAlphaDigit(code)) {
                return false;
            }
        }
        if ('/' == code) {
            if (isAlphaDigit(prevCode)) {
                return false;
            }
        }
        return true;
    }

    private boolean isAlphaDigit(char code) {
        return ('a' <= code && code <= 'z') || ('A' <= code && code <= 'Z') || ('0' <= code && code <= '9');
    }

    private boolean isCjk(char code) {
        return (0x4E00 <= code && code < 0xD800) || (0xE000 <= code && code < 0xFB00);
    }

    private boolean canCompressLeft(char code) {
        if (compressLeftSet == null) {
            compressLeftSet = new HashSet<>();
            compressLeftSet.add('\u2018'); // ‘
            compressLeftSet.add('\u201C'); // “
            compressLeftSet.add('\u3008'); // 〈
            compressLeftSet.add('\u300A'); // 《
            compressLeftSet.add('\u300C'); // 「
            compressLeftSet.add('\u300E'); // 『
            compressLeftSet.add('\u3010'); // 【
            compressLeftSet.add('\u3014'); // 〔
            compressLeftSet.add('\u3016'); // 〖
            compressLeftSet.add('\uFF08'); // （
            compressLeftSet.add('\uFF3B'); // ［
            compressLeftSet.add('\uFF5B'); // ｛
        }
        return compressLeftSet.contains(code);
    }

    private boolean canCompressRight(char code) {
        if (compressRightSet == null) {
            compressRightSet = new HashSet<>();
            compressRightSet.add('\u2019'); // ’
            compressRightSet.add('\u201D'); // ”
            compressRightSet.add('\u3001'); // 、
            compressRightSet.add('\u3002'); // 。
            compressRightSet.add('\u3009'); // 〉
            compressRightSet.add('\u300B'); // 》
            compressRightSet.add('\u300D'); // 」
            compressRightSet.add('\u300F'); // 』
            compressRightSet.add('\u3011'); // 】
            compressRightSet.add('\u3015'); // 〕
            compressRightSet.add('\u3017'); // 〗
            compressRightSet.add('\uFF01'); // ！
            compressRightSet.add('\uFF09'); // ）
            compressRightSet.add('\uFF0C'); // ，
            compressRightSet.add('\uFF1A'); // ：
            compressRightSet.add('\uFF1B'); // ；
            compressRightSet.add('\uFF1F'); // ？
            compressRightSet.add('\uFF3D'); // ］
            compressRightSet.add('\uFF5D'); // ｝
        }
        return compressRightSet.contains(code);
    }

    private boolean cannotLineBegin(char code) {
        if (canCompressRight(code)) {
            return true;
        }
        if (notLineBeginSet == null) {
            notLineBeginSet = new HashSet<>();
            notLineBeginSet.add('!');
            notLineBeginSet.add(')');
            notLineBeginSet.add(',');
            notLineBeginSet.add('.');
            notLineBeginSet.add(':');
            notLineBeginSet.add(';');
            notLineBeginSet.add('>');
            notLineBeginSet.add('?');
            notLineBeginSet.add(']');
            notLineBeginSet.add('}');
        }
        return notLineBeginSet.contains(code);
    }

    private boolean cannotLineEnd(char code) {
        if (canCompressLeft(code)) {
            return true;
        }
        if (notLineEndSet == null) {
            notLineEndSet = new HashSet<>();
            notLineEndSet.add('(');
            notLineEndSet.add('<');
            notLineEndSet.add('[');
            notLineEndSet.add('{');
        }
        return notLineEndSet.contains(code);
    }

    private boolean shouldRotateForVertical(char code) {
        if (code < 0x2600) {
            return true;
        }
        if (rotateForVerticalSet == null) {
            rotateForVerticalSet = new HashSet<>();
            rotateForVerticalSet.add('\u2014'); // —
            rotateForVerticalSet.add('\u2026'); // …
            rotateForVerticalSet.add('\u3008'); // 〈
            rotateForVerticalSet.add('\u3009'); // 〉
            rotateForVerticalSet.add('\u300A'); // 《
            rotateForVerticalSet.add('\u300B'); // 》
            rotateForVerticalSet.add('\u300C'); // 「
            rotateForVerticalSet.add('\u300D'); // 」
            rotateForVerticalSet.add('\u300E'); // 『
            rotateForVerticalSet.add('\u300F'); // 』
            rotateForVerticalSet.add('\u3010'); // 【
            rotateForVerticalSet.add('\u3011'); // 】
            rotateForVerticalSet.add('\u3014'); // 〔
            rotateForVerticalSet.add('\u3015'); // 〕
            rotateForVerticalSet.add('\u3016'); // 〖
            rotateForVerticalSet.add('\u3017'); // 〗
            rotateForVerticalSet.add('\uFF08'); // （
            rotateForVerticalSet.add('\uFF09'); // ）
            rotateForVerticalSet.add('\uFF3B'); // ［
            rotateForVerticalSet.add('\uFF3D'); // ］
            rotateForVerticalSet.add('\uFF5B'); // ｛
            rotateForVerticalSet.add('\uFF5D'); // ｝
        }
        return rotateForVerticalSet.contains(code);
    }

    private String replaceForVertical(String text) {
        if (text.length() > 1) {
            return text;
        }
        if (replaceForVerticalMap == null) {
            replaceForVerticalMap = new HashMap<>();
            replaceForVerticalMap.put('\uFF0C', '\uFE10'); // ，
            replaceForVerticalMap.put('\u3001', '\uFE11'); // 、
            replaceForVerticalMap.put('\u3002', '\uFE12'); // 。
            replaceForVerticalMap.put('\uFF1A', '\uFE13'); // ：
            replaceForVerticalMap.put('\uFF1B', '\uFE14'); // ；
            replaceForVerticalMap.put('\uFF01', '\uFE15'); // ！
            replaceForVerticalMap.put('\uFF1F', '\uFE16'); // ？
            //replaceForVerticalMap.put('\u3016', '\uFE17'); // 〖
            //replaceForVerticalMap.put('\u3017', '\uFE18'); // 〗
            //replaceForVerticalMap.put('\u2026', '\uFE19'); // …
        }
        Character value = replaceForVerticalMap.get(text.charAt(0));
        return (value != null ? String.valueOf(value) : text);
    }
}
