package com.geansea.layout;

import android.graphics.Canvas;
import android.text.Spanned;
import android.text.TextPaint;

public class GSSpannedLayout extends GSLayout {
    protected GSSpannedLayout(Spanned text,
                              int start,
                              int end,
                              TextPaint paint,
                              int width,
                              int height) {
        super(text, start, end, paint, width, height);
    }

    @Override
    public int getStart() {
        return 0;
    }

    @Override
    public int getLineCount() {
        return 0;
    }

    @Override
    public GSLayoutLine getLine(int lineIndex) {
        return null;
    }

    @Override
    public void draw(Canvas canvas) {

    }
}
