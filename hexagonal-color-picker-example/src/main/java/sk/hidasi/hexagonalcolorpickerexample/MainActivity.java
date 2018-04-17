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

import sk.hidasi.hexagonalcolorpicker.HexagonalColorPicker;
import sk.hidasi.hexagonalcolorpicker.HexagonalColorPicker.OnColorSelectedListener;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements OnColorSelectedListener {

    private static final int PALETTE_RADIUS = 3;
    private static final int SEEK_MINIMUM = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SeekBar seekPaletteRadius = findViewById(R.id.seekPaletteRadius);
        TextView editPaletteRadius = findViewById(R.id.editPaletteRadius);

        bindControls(seekPaletteRadius, editPaletteRadius, PALETTE_RADIUS - SEEK_MINIMUM);

        HexagonalColorPicker colorPicker = findViewById(R.id.hexagonalColorPicker);
        colorPicker.setAttrs(PALETTE_RADIUS, Color.WHITE, this);
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onUpdateClick(View v) {

        SeekBar seekPaletteRadius = findViewById(R.id.seekPaletteRadius);

        final int paletteRadius = SEEK_MINIMUM + seekPaletteRadius.getProgress();

        HexagonalColorPicker colorPicker = findViewById(R.id.hexagonalColorPicker);
        colorPicker.setAttrs(paletteRadius, Color.WHITE, this);

        TextView textView = findViewById(R.id.hello);
        textView.setTextColor(Color.BLACK);
    }

    @Override
    public void onColorSelected(int color) {

        TextView textView = findViewById(R.id.hello);
        textView.setTextColor(color);
    }
}
