package com.geansea.layout;

import android.graphics.Canvas;

public class GSSpannedLayout extends GSLayout {
    protected GSSpannedLayout() {
        super();
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
