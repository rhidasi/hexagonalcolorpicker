HexagonalColorPicker for Android
===================================

Simple color picker for Android with a hexagonal design.

The number of color swatches can be configured (parameter `"paletteRadius"`).

Can be used as custom View, Dialog, or Preference.

`android:minSdkVersion=8`

View (activity)
---------------

Color Picker can be put in XML layout just like any other android view.

You can optionally define color change listener (see `HexagonalColorPicker#setListener`).

**Example:**

```xml
	<sk.hidasi.hexagonalcolorpicker.HexagonalColorPicker
		android:id="@+id/hexagonalColorPicker"
		android:layout_width="match_parent"
		android:layout_height="wrap_content"
		android:layout_gravity="center_horizontal"
		hidasi:paletteRadius="3"
		hidasi:shadowDistance="2dp"
		hidasi:shadowColor="@color/shadow"
		android:padding="4dp" />
```

**Screenshot:**

![Screen 1][screen1]

Dialog
------

The usage of Dialog class is really straightforward.

**Example:**

```java
	HexagonalColorPickerDialog dialog = new HexagonalColorPickerDialog(getContext(), R.string.color_picker_default_title, paletteRadius, initialColor, listener);
	dialog.show();
```

**Screenshot:**

![Screen 4][screen4]

Preference
----------

Color Picker can be used also on Preferece Screen. Usage is the same as for any other preferences. You can provide default, initial color value by setting `android:defaultValue` attribute. Value selected in the dialog will be stored under the key provided with `android:key` attribute.

**Example:**

```xml
	<sk.hidasi.hexagonalcolorpicker.HexagonalColorPickerPreference
		android:key="key_color"
		android:title="@string/color"
		android:summary="@string/tap_to_change_color"
		android:defaultValue="@color/red"
		hidasi:shadowDistance="2dp"
		hidasi:shadowColor="@color/shadow"
		hidasi:paletteRadius="3" />
```

**Screenshot:**

![Screen 2][screen2] ![Screen 3][screen3]

HexagonalColorPickerExample
---------------------------

Provided example application demonstrates the usage of HexagonalColorPicker.

You can install the application from Google Play for easy access:

[![Get it on Google Play](http://www.android.com/images/brand/get_it_on_play_logo_small.png)](https://play.google.com/store/apps/details?id=sk.hidasi.hexagonalcolorpickerexample)

License
-------

Code is licensed under the Apache License, Version 2.0.

[screen1]: https://raw.githubusercontent.com/rhidasi/hexagonalcolorpicker/master/screen1.png
[screen2]: https://raw.githubusercontent.com/rhidasi/hexagonalcolorpicker/master/screen2.png
[screen3]: https://raw.githubusercontent.com/rhidasi/hexagonalcolorpicker/master/screen3.png
[screen4]: https://raw.githubusercontent.com/rhidasi/hexagonalcolorpicker/master/screen4.png