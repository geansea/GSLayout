package com.geansea.layout;

import android.graphics.Canvas;
import android.text.Spanned;

public class GSSpannedLayout extends GSLayout {
    protected GSSpannedLayout(Spanned text) {
        super(text);
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
