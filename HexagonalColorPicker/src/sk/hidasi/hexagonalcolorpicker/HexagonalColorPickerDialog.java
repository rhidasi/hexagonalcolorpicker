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
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

public class HexagonalColorPickerDialog extends Dialog implements OnColorSelectedListener {

	private static final String KEY_SELECTED_COLOR = "selected_color";
	private static final String KEY_PALETTE_RADIUS = "palette_radius";
	private static final String KEY_TITLE_ID = "title_id";

	private HexagonalColorPicker mPalette;
	private final OnColorSelectedListener mListener;

	private int mTitleResId;
	private int mPaletteRadius;
	private int mSelectedColor;

	public HexagonalColorPickerDialog(Context context, final int titleResId, final int radius,
								final int selectedColor, final OnColorSelectedListener listener) {
		super(context);
		mTitleResId = titleResId;
		mPaletteRadius = radius;
		mSelectedColor = selectedColor;
		mListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mTitleResId = savedInstanceState.getInt(KEY_TITLE_ID);
			mPaletteRadius = savedInstanceState.getInt(KEY_PALETTE_RADIUS);
			mSelectedColor = savedInstanceState.getInt(KEY_SELECTED_COLOR);
		}

		setContentView(R.layout.color_picker_dialog);
		setTitle(mTitleResId);
		mPalette = (HexagonalColorPicker) findViewById(R.id.color_picker);
		mPalette.setAttrs(mPaletteRadius, mSelectedColor, this);
	}

	@Override
	public void onColorSelected(int color) {
		mSelectedColor = color;

		if (mListener != null) {
			mListener.onColorSelected(color);
		}

		dismiss();
	}

	public int getSelectedColor() {
		return mSelectedColor;
	}

	@Override
	public Bundle onSaveInstanceState() {
		final Bundle outState = new Bundle();
		super.onSaveInstanceState();
		outState.putInt(KEY_TITLE_ID, mTitleResId);
		outState.putInt(KEY_PALETTE_RADIUS, mPaletteRadius);
		outState.putInt(KEY_SELECTED_COLOR, mSelectedColor);
		return outState;
	}

}
