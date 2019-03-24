/*
 * Copyright (C) 2015 Robert Hidasi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sk.hidasi.hexagonalcolorpicker;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * Color swatch with oval shape and solid color.
 */
class HexagonalColorSwatch extends AppCompatImageView {

    /**
     * Color of the swatch.
     */
    final public int mColor;
    /**
     * Position of the swatch (in relative coordinates from -1.0 to 1.0)
     */
    final public PointF mPosition;
    /**
     * Animation delay of the swatch (in ms).
     */
    final public int mAnimDelay;


    /**
     * Instantiates a new color swatch.
     *
     * @param context    context
     * @param position   position of the swatch
     * @param color      color of the swatch
     * @param animDelay  animation delay
     */
    public HexagonalColorSwatch(final Context context, final int color, final PointF position, final int animDelay, final Drawable background) {
        super(context);

        mColor = color;
        mPosition = position;
        mAnimDelay = animDelay;

        final GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(mColor);
        setImageDrawable(drawable);
        setBackground(background);
    }

    /**
     * Update stroke width.
     *
     * @param strokeWidth the new stroke width
     */
    public void updateStrokeWidth(final int strokeWidth) {
        final GradientDrawable drawable = (GradientDrawable) getDrawable();
        drawable.setStroke(strokeWidth, HexagonalColorPicker.calculateStrokeColor(mColor));
    }
}
