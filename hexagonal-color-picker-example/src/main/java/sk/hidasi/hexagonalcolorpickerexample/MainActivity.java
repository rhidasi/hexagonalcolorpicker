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

package sk.hidasi.hexagonalcolorpickerexample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import sk.hidasi.hexagonalcolorpicker.HexagonalColorPicker;
import sk.hidasi.hexagonalcolorpicker.HexagonalColorPicker.OnColorSelectedListener;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements OnColorSelectedListener {

    private static final int SEEK_MINIMUM = 1;
    private int paletteRadius = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            paletteRadius = savedInstanceState.getInt("paletteRadius");
        }

        SeekBar seekPaletteRadius = findViewById(R.id.seekPaletteRadius);
        TextView editPaletteRadius = findViewById(R.id.editPaletteRadius);

        bindControls(seekPaletteRadius, editPaletteRadius, paletteRadius - SEEK_MINIMUM);

        HexagonalColorPicker colorPicker = findViewById(R.id.hexagonalColorPicker);
        colorPicker.setAttrs(paletteRadius, Color.WHITE, this);
    }

    private void bindControls(final SeekBar seek, final TextView text, final int initValue) {

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                text.setText(Integer.toString(progress + SEEK_MINIMUM));
            }
        });

        seek.setProgress(initValue);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, PreferencesActivity.class));
                return true;
            case R.id.action_switch_theme:
                if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onUpdateClick(View v) {

        SeekBar seekPaletteRadius = findViewById(R.id.seekPaletteRadius);

        paletteRadius = SEEK_MINIMUM + seekPaletteRadius.getProgress();

        HexagonalColorPicker colorPicker = findViewById(R.id.hexagonalColorPicker);
        colorPicker.setAttrs(paletteRadius, Color.WHITE, this);

        TextView textView = findViewById(R.id.tapToChange);
        textView.setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onColorSelected(int color) {

        TextView textView = findViewById(R.id.tapToChange);
        textView.setBackgroundColor(color);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("paletteRadius", paletteRadius);
    }
}
