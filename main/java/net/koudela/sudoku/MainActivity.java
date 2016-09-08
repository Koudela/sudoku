package net.koudela.sudoku;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {
    protected final static int DIM = 9;
    protected final static int CHOOSE_INPUT_REQUEST = 1;
    protected Button[] mainButtons = new Button[DIM * DIM];
    protected TextView[] helperTextViews = new TextView[DIM*DIM];
    // create or get the retained data object
    protected SudokuData sudokuData = SudokuData.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag("data") == null) {
            // create the fragment the first time
            fm.beginTransaction().add(new RetainedFragment(), "data").commit();
        }
        // add first(!) the views populating the tableHelper
        initLayoutLayer("Helper");
        setTextSizeHelperTextViews();
        // add second(!) the views populating the tableMain <- sudokuData.setMainButtonsText(...) needs helperTextViews populated
        initLayoutLayer("Main");
        setTextSizeMainButtons();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    public void initLayoutLayer(String type) {
        LinearLayout[] layoutCols = new LinearLayout[DIM];
        LinearLayout.LayoutParams[] childViewParams = new LinearLayout.LayoutParams[DIM];

        LinearLayout table = (LinearLayout) findViewById(type.equals("Main")?R.id.tableMain:R.id.tableHelper);
        LinearLayout.LayoutParams tableLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);

        for (int i = 0; i < DIM; i++) {
            layoutCols[i] = new LinearLayout(this);
            layoutCols[i].setOrientation(LinearLayout.VERTICAL);
            layoutCols[i].setTag("column" + i + type);
            if (type.equals("Helper")) layoutCols[i].setBackgroundColor(ContextCompat.getColor(this, R.color.gridColor));
            table.addView(layoutCols[i], tableLayoutParams);

            for (int j = 0; j < DIM; j++) {
                int arrId = i * DIM + j;
                if (type.equals("Main")) initMainButtons(arrId);
                else initHelperTextViews(arrId);
                childViewParams[j] = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
                childViewParams[j].setMargins((i % 3) == 0 ? 3 : 1, (j % 3) == 0 ? 3 : 1, 0, 0);
                layoutCols[i].addView(type.equals("Main") ? mainButtons[arrId] : helperTextViews[arrId], childViewParams[j]);
            }
        }
    }

    public void initMainButtons(int arrId) {
        mainButtons[arrId] = new Button(this);
        mainButtons[arrId].setTag("main" + arrId);
        mainButtons[arrId].setBackgroundResource(0);
        mainButtons[arrId].setOnClickListener(this);
        mainButtons[arrId].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        sudokuData.setMainButtonsText(-1, arrId, mainButtons[arrId], helperTextViews, this);
    }

    public void initHelperTextViews(int arrId) {
        helperTextViews[arrId] = new TextView(this);
        helperTextViews[arrId].setTag("helper" + arrId);
        helperTextViews[arrId].setGravity(Gravity.CENTER);
        helperTextViews[arrId].setBackgroundColor(ContextCompat.getColor(this,R.color.backgroundUntouched));
        sudokuData.setHelperTextViewText(arrId, helperTextViews[arrId], this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_info:
                Toast.makeText(this, "Info selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                Intent intent = new Intent (this, PreferencesActivity.class);
                startActivity(intent);
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        boolean easyTouch = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferencesFragment.KEY_PREF_EASY_TOUCH, false);
        int arrId = Integer.valueOf(((String) view.getTag()).substring(4));
        sudokuData.setRequestViewId(arrId);

        if (easyTouch) {
            if (sudokuData.arrIdEasyTouchButton != arrId) {
                setEasyTouchArea(arrId);
                return;
            }
        } else setEasyTouchArea(0);
        Intent intent = new Intent(this, ChooseInputActivity.class);
        startActivityForResult(intent, CHOOSE_INPUT_REQUEST);
    }

    public void setEasyTouchArea(int arrId) {
        if (sudokuData.arrIdEasyTouchButton != arrId) {
            helperTextViews[sudokuData.arrIdEasyTouchButton].setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundUntouched));
            if (sudokuData.isBlocked(arrId)) sudokuData.arrIdEasyTouchButton = 0;
            else sudokuData.arrIdEasyTouchButton = arrId;
            if (sudokuData.arrIdEasyTouchButton != 0) {
                helperTextViews[sudokuData.arrIdEasyTouchButton].setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundTouched));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_INPUT_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            setEasyTouchArea(0);
            String chooseInputViewTag = data.getStringExtra("chooseInputViewTag");
            int arrId = sudokuData.getRequestViewId();

            if (chooseInputViewTag.substring(0,2).equals("is")) {
                String value = chooseInputViewTag.substring(2);
                int number = Integer.valueOf(value);
                // if the chosen number already populates the button we delete the text and replace otherwise
                if (mainButtons[arrId].getText().equals(value)) {
                    sudokuData.setMainButtonsText(0, arrId, mainButtons[arrId], helperTextViews, this);
                } else {
                    sudokuData.setMainButtonsText(number, arrId, mainButtons[arrId], helperTextViews, this);
                }
            } else if (chooseInputViewTag.substring(0,3).equals("not")) {
                sudokuData.setUserHint(Integer.valueOf(chooseInputViewTag.substring(3)), arrId, helperTextViews[arrId], this);
            }
        }
    }

    public void setTextSizeMainButtons() {
        float textSize = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(PreferencesFragment.KEY_PREF_FONT_SIZE_MAIN, "20"));
        for (Button button: mainButtons) {
            button.setTextSize(textSize);
        }
    }

    public void setTextSizeHelperTextViews() {
        float textSize = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(this).getString(PreferencesFragment.KEY_PREF_FONT_SIZE_HELPER, "13"));
        for (TextView textView: helperTextViews) {
            textView.setTextSize(textSize);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case (PreferencesFragment.KEY_PREF_AUTO_HINT):
                boolean autoHint = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT, false);
                if (autoHint) sudokuData.initAutoHints(helperTextViews, this);
                // TODO: "else" if an advanced auto hint is selected recalculate else hide auto hints;
                break;
            case (PreferencesFragment.KEY_PREF_FONT_SIZE_MAIN):
                setTextSizeMainButtons();
                break;
            case (PreferencesFragment.KEY_PREF_FONT_SIZE_HELPER):
                setTextSizeHelperTextViews();
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_HINT_ADV1):
                Log.v(key, "" + sharedPreferences.getBoolean(key, false));
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_HINT_ADV2):
                Log.v(key, "" + sharedPreferences.getBoolean(key, false));
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_INSERT1):
                Log.v(key, "" + sharedPreferences.getBoolean(key, false));
                break;
            case (PreferencesFragment.KEY_PREF_AUTO_INSERT2):
                Log.v(key, "" + sharedPreferences.getBoolean(key, false));
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }
}
