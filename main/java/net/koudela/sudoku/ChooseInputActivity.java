package net.koudela.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ChooseInputActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_input);
        setTextSize();
        setColor();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }
    public void chooseInput(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("chooseInputViewTag", (String) view.getTag());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(PreferencesFragment.KEY_PREF_FONT_SIZE_INPUT)) {
            setTextSize();
        }
    }

    protected void setTextSize() {
        float textSize = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(PreferencesFragment.KEY_PREF_FONT_SIZE_INPUT, "16"));
        ((TextView) findViewById(R.id.chooseInputIs0)).setTextSize(textSize);
        ((TextView) findViewById(R.id.chooseInputNot0)).setTextSize(textSize);
        for (int i=1; i<=9; i++) {
            ((Button) findViewById(getResources().getIdentifier("chooseInputIs" + i, "id", getPackageName()))).setTextSize(textSize);
            ((Button) findViewById(getResources().getIdentifier("chooseInputNot" + i, "id", getPackageName()))).setTextSize(textSize);
        }
    }

    protected  void setColor() {
        SudokuData sudokuData = SudokuData.getInstance();
        for (int i=1; i<=9; i++) {
            Button button = (Button) findViewById(getResources().getIdentifier("chooseInputIs" + i, "id", getPackageName()));
            int arrId = sudokuData.getRequestViewId();
            if (sudokuData.mainButtonsText[arrId] == i) {
                button.setTextColor(ContextCompat.getColor(this, R.color.textColorBadUserInput));
                button.setText(getResources().getString(R.string.delete));
            } else if (sudokuData.isHint(i, arrId)) {
                button.setTextColor(ContextCompat.getColor(this, R.color.userHints));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}
