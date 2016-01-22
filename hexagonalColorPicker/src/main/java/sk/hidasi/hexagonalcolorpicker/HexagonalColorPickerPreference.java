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
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HexagonalColorPickerPreference extends Preference implements OnColorSelectedListener {

	private static final int DEFAULT_PALETTE_RADIUS = 3;

	private int mPaletteRadius;
	private int mShadowColor;
	private int mValue;

	public HexagonalColorPickerPreference(Context context) {
		this(context, null, 0);
	}

	public HexagonalColorPickerPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HexagonalColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		final TypedArray a = getContext().getTheme().obtainStyledAttributes(
				attrs, R.styleable.HexagonalColorPicker, defStyle, defStyle);

		mPaletteRadius = a.getInteger(R.styleable.HexagonalColorPicker_paletteRadius, DEFAULT_PALETTE_RADIUS);
		mShadowColor = a.getColor(R.styleable.HexagonalColorPicker_shadowColor, Color.GRAY);
		a.recycle();
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		setPreviewImage(view, mValue);
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
		dialog.setShadowColor(mShadowColor);
		dialog.show();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, 0);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		onColorSelected(restoreValue ? getPersistedInt(0) : (Integer) defaultValue);
	}

	public int getValue() {
		return mValue;
	}

	private void setPreviewImage(final View view, final int color) {

		if (view == null) return;
		final LinearLayout widgetFrameView = ((LinearLayout)view.findViewById(android.R.id.widget_frame));
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
		colorChoiceDrawable.setStroke(dipToPixels(1), HexagonalColorPicker.getStrokeColor(color));
		iView.setImageDrawable(colorChoiceDrawable);
	}

	private int dipToPixels(final float dip) {
		return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, getContext().getResources().getDisplayMetrics());
	}
}
