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

import sk.hidasi.hexagonalcolorpicker.HexagonalColorPicker.OnColorSelectedListener;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Hexagonal color picker preference.
 */
public class HexagonalColorPickerPreference extends Preference implements OnColorSelectedListener {

    private int mPaletteRadius;
    private int mValue;

    /**
     * Constructor.
     */
    public HexagonalColorPickerPreference(Context context) {
        super(context);
        initAttrs(null, 0);
    }

    /**
     * Constructor.
     */
    public HexagonalColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs, 0);
    }

    /**
     * Constructor.
     */
    public HexagonalColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs, defStyle);
    }

    /**
     * Initialize attributes.
     *
     * @param attrs    Attribute Set
     * @param defStyle Default style
     */
    private void initAttrs(AttributeSet attrs, int defStyle) {

        final TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.HexagonalColorPicker, defStyle, defStyle);

        mPaletteRadius = a.getInteger(R.styleable.HexagonalColorPicker_paletteRadius, HexagonalColorPicker.DEFAULT_PALETTE_RADIUS);
        a.recycle();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        setPreviewImage(holder, mValue);
    }

    @Override
    public void onColorSelected(final int color) {
        if (callChangeListener(color)) {
            mValue = color;
            persistInt(color);
            notifyChanged();
        }
    }

    @Override
    protected void onClick() {
        super.onClick();
        final HexagonalColorPickerDialog dialog = new HexagonalColorPickerDialog(getContext(), R.string.color_picker_default_title, mPaletteRadius, mValue, this);
        dialog.show();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        onColorSelected(defaultValue == null ? getPersistedInt(0) : (Integer) defaultValue);
    }

    /**
     * Get preference color value.
     *
     * @return color value
     */
    public int getValue() {
        return mValue;
    }

    /**
     * Creates a color preview and adds this view to widget frame layout.
     *
     * @param view  Preference view holder
     * @param color Selected color value
     */
    private void setPreviewImage(final PreferenceViewHolder view, final int color) {

        if (view == null) return;
        final LinearLayout widgetFrameView = (LinearLayout)view.findViewById(android.R.id.widget_frame);
        if (widgetFrameView == null) return;
        widgetFrameView.setVisibility(View.VISIBLE);
        widgetFrameView.setPadding(
                widgetFrameView.getPaddingLeft(),
                widgetFrameView.getPaddingTop(),
                dipToPixels(7),
                widgetFrameView.getPaddingBottom()
        );
        // remove already create preview image
        final int count = widgetFrameView.getChildCount();
        if (count > 0) {
            widgetFrameView.removeViews(0, count);
        }
        final ImageView iView = new ImageView(getContext());
        widgetFrameView.addView(iView);
        widgetFrameView.setMinimumWidth(0);
        final int size = dipToPixels(40);
        iView.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        final GradientDrawable colorChoiceDrawable = new GradientDrawable();
        colorChoiceDrawable.setShape(GradientDrawable.OVAL);
        colorChoiceDrawable.setColor(color);
        colorChoiceDrawable.setStroke(dipToPixels(1), HexagonalColorPicker.calculateStrokeColor(color));
        iView.setImageDrawable(colorChoiceDrawable);
    }

    /**
     * Converts 'dip' to pixels.
     *
     * @param dip size in 'dip'
     * @return size in pixels
     */
    private int dipToPixels(final float dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getContext().getResources().getDisplayMetrics());
    }
}
