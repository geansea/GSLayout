package com.geansea.layout;

import android.text.TextPaint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

class GSLayoutUtils {
    private HashSet<Character> compressLeftSet = null;
    private HashSet<Character> compressRightSet = null;
    private HashSet<Character> notLineBeginSet = null;
    private HashSet<Character> notLineEndSet = null;
    private HashSet<Character> rotateForVerticalSet = null;
    private HashMap<Character, Character> replaceForVerticalMap = null;

    GSLayoutUtils() {
    }

    ArrayList<GSLayoutGlyph> glyphsForSimpleLayout(
            String text,
            TextPaint paint,
            int start,
            int end,
            float size,
            boolean vertical) {
        int length = paint.breakText(text, start, end, true, size, null);
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
        if ('%' == prevCode && isCjk(code)) {
            return true;
        }
        return false;
    }

    boolean canCompressLeft(char code) {
        if (compressLeftSet == null) {
            Character[] array = new Character[]{
                    '\u2018', // ‘
                    '\u201C', // “
                    '\u3008', // 〈
                    '\u300A', // 《
                    '\u300C', // 「
                    '\u300E', // 『
                    '\u3010', // 【
                    '\u3014', // 〔
                    '\u3016', // 〖
                    '\uFF08', // （
                    '\uFF3B', // ［
                    '\uFF5B', // ｛
            };
            compressLeftSet = new HashSet<>(Arrays.asList(array));
        }
        return compressLeftSet.contains(code);
    }

    boolean canCompressRight(char code) {
        if (compressRightSet == null) {
            Character[] array = new Character[]{
                    '\u2019', // ’
                    '\u201D', // ”
                    '\u3001', // 、
                    '\u3002', // 。
                    '\u3009', // 〉
                    '\u300B', // 》
                    '\u300D', // 」
                    '\u300F', // 』
                    '\u3011', // 】
                    '\u3015', // 〕
                    '\u3017', // 〗
                    '\uFF01', // ！
                    '\uFF09', // ）
                    '\uFF0C', // ，
                    '\uFF1A', // ：
                    '\uFF1B', // ；
                    '\uFF1F', // ？
                    '\uFF3D', // ］
                    '\uFF5D', // ｝
                    // For vertical
                    '\uFE10', // ，
                    '\uFE11', // 、
                    '\uFE12', // 。
            };
            compressRightSet = new HashSet<>(Arrays.asList(array));
        }
        return compressRightSet.contains(code);
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
        if ('\u00A0' == prevCode) {
            return false;
        }
        // Space follow prev
        if (' ' == code || '\u00A0' == code) {
            return false;
        }
        if (isAlphaDigit(prevCode)) {
            if (isAlphaDigit(code)) {
                return false;
            }
            if ('\'' == code || '\"' == code || '-' == code || '_' == code || '%' == code) {
                return false;
            }
        }
        if (isAlphaDigit(code)) {
            if ('\'' == prevCode || '\"' == prevCode || '\u2019' == prevCode) {
                return false;
            }
        }
        if (cannotLineEnd(prevCode)) {
            return false;
        }
        if (cannotLineBegin(code)) {
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
        text = replaceTextForVertical(text);
        float y = 0;
        float fontSize = paint.getTextSize();
        float ascent = -paint.ascent();
        float descent = paint.descent();
        float widths[] = new float[length];
        paint.getTextWidths(text, start, start + length, widths);
        ArrayList<GSLayoutGlyph> glyphs = new ArrayList<>(length);
        GSLayoutGlyph glyph = null;
        for (int i = 0; i < length; ++i) {
            float glyphSize = widths[i];
            if (glyphSize == 0 && glyph != null) {
                glyph.end++;
                glyph.text = text.substring(glyph.start, glyph.end);
            } else {
                glyph = new GSLayoutGlyph();
                glyph.start = start + i;
                glyph.end = glyph.start + 1;
                glyph.text = text.substring(glyph.start, glyph.end);
                glyph.paint = paint;
                if (shouldRotateForVertical(glyph.utf16Code()) || glyphSize < fontSize * 0.9) {
                    glyph.x = (descent - ascent) / 2;
                    glyph.y = y;
                    glyph.ascent = ascent;
                    glyph.descent = descent;
                    glyph.width = glyphSize;
                    glyph.vertical = true;
                    glyph.rotateForVertical = true;
                } else {
                    float extended = ascent + descent - glyphSize;
                    float ascentForVertical = ascent - extended / 2;
                    float descentForVertical = descent - extended / 2;
                    glyph.x = -glyphSize / 2;
                    glyph.y = y + ascentForVertical;
                    glyph.ascent = ascentForVertical;
                    glyph.descent = descentForVertical;
                    glyph.width = glyphSize;
                    glyph.vertical = true;
                    glyphSize = ascentForVertical + descentForVertical;
                }
                glyphs.add(glyph);
            }
            y += glyphSize;
        }
        return glyphs;
    }

    private boolean isAlphaDigit(char code) {
        return ('a' <= code && code <= 'z') || ('A' <= code && code <= 'Z') || ('0' <= code && code <= '9');
    }

    private boolean isCjk(char code) {
        return (0x4E00 <= code && code < 0xD800) || (0xE000 <= code && code < 0xFB00);
    }

    private boolean cannotLineBegin(char code) {
        if (canCompressRight(code)) {
            return true;
        }
        if (notLineBeginSet == null) {
            Character[] array = new Character[]{
                    '!',
                    ')',
                    ',',
                    '.',
                    ':',
                    ';',
                    '>',
                    '?',
                    ']',
                    '}',
            };
            notLineBeginSet = new HashSet<>(Arrays.asList(array));
        }
        return notLineBeginSet.contains(code);
    }

    private boolean cannotLineEnd(char code) {
        if (canCompressLeft(code)) {
            return true;
        }
        if (notLineEndSet == null) {
            Character[] array = new Character[]{
                    '(',
                    '<',
                    '[',
                    '{',
            };
            notLineEndSet = new HashSet<>(Arrays.asList(array));
        }
        return notLineEndSet.contains(code);
    }

    private boolean shouldRotateForVertical(char code) {
        if (code < 0x2600) {
            return true;
        }
        if (rotateForVerticalSet == null) {
            Character[] array = new Character[]{
                    '\u2014', // —
                    '\u2026', // …
                    '\u3008', // 〈
                    '\u3009', // 〉
                    '\u300A', // 《
                    '\u300B', // 》
                    '\u300C', // 「
                    '\u300D', // 」
                    '\u300E', // 『
                    '\u300F', // 』
                    '\u3010', // 【
                    '\u3011', // 】
                    '\u3014', // 〔
                    '\u3015', // 〕
                    '\u3016', // 〖
                    '\u3017', // 〗
                    '\uFF08', // （
                    '\uFF09', // ）
                    '\uFF3B', // ［
                    '\uFF3D', // ］
                    '\uFF5B', // ｛
                    '\uFF5D', // ｝
            };
            rotateForVerticalSet = new HashSet<>(Arrays.asList(array));
        }
        return rotateForVerticalSet.contains(code);
    }

    private char replaceForVertical(char code) {
        if (replaceForVerticalMap == null) {
            replaceForVerticalMap = new HashMap<Character, Character>() {{
                put('\uFF0C', '\uFE10'); // ，
                put('\u3001', '\uFE11'); // 、
                put('\u3002', '\uFE12'); // 。
                put('\uFF1A', '\uFE13'); // ：
                put('\uFF1B', '\uFE14'); // ；
                put('\uFF01', '\uFE15'); // ！
                put('\uFF1F', '\uFE16'); // ？
                //put('\u3016', '\uFE17'); // 〖
                //put('\u3017', '\uFE18'); // 〗
                //put('\u2026', '\uFE19'); // …
                // For quotes
                put('\u2018', '\u300C'); // ‘
                put('\u2019', '\u300D'); // ’
                put('\u201C', '\u300E'); // “
                put('\u201D', '\u300F'); // ”
            }};
        }
        Character value = replaceForVerticalMap.get(code);
        return (value != null ? value : code);
    }

    private String replaceTextForVertical(String text) {
        StringBuilder textBuilder = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); ++i) {
            char code = text.charAt(i);
            code = replaceForVertical(code);
            textBuilder.append(code);
        }
        return textBuilder.toString();
    }
}
