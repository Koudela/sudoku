package net.koudela.sudoku;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
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

import java.lang.ref.WeakReference;

import static android.R.attr.data;
import static net.koudela.sudoku.SudokuGroups.DIM;

//TODO: Info action
//TODO: hint adv1 can be generalized
//TODO: Show banner at start (during loading)
//TODO: get hard/sudokuLevel4 + solution from server/upload to server
//TODO: save only sudokuLevel4 + solution / make sudokuLevel1/2/3 on the fly
//DONE: score
// UserHint: if (isHint) before 0 points;
// if (would be plain hint) 1 point;
// else if (would be adv1) 3 points;
// else if (would be adv2 relaxed || adv3 relaxed) 6 points;
// else if (would be adv2 hard || adv3 hard) 10 points;
// else 2 points
// Remove: - (valueGivenOnSet) point
// Insert value: number of new plain hints on a hint field that is not set + number of not set hints
// on value field;
// Remove value: negative value of Insert;
// if hint action used -1
//TODO: personal max score

//TODO: german localization
//TODO: advertising adds
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    protected final static int CHOOSE_INPUT_REQUEST = 1;
    private static WeakReference<Context> context;
    protected static Button[] mainButtons = new Button[DIM * DIM];
    protected static TextView[] helperTextViews = new TextView[DIM*DIM];
    protected SudokuData sudokuData;

    public static Context getContext() {
        return context.get();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean firstLaunch = false;
        context = new WeakReference<Context>(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        for (int arrId : Sudoku.ALL_ARR_IDS) {
            initMainButtons(arrId, true);
            initHelperTextViews(arrId, true);
        }
        // find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag("data") == null) {
            firstLaunch = true;
            // create the fragment the first time
            fm.beginTransaction().add(new RetainedFragment(), "data").commit();
        }
        // create or get the retained data object
        sudokuData = SudokuData.getInstance();
        if (firstLaunch && !sudokuData.isOldGame()) {
            sudokuData.resetGame(true);
        } else {
            for (int arrId : Sudoku.ALL_ARR_IDS) {
                sudokuData.setMainButtonsContent(arrId, false);
                sudokuData.setHelperTextViewContent(arrId);
            }
            sudokuData.setScore();
        }

        setTextSizeHelperTextViews();
        setTextSizeMainButtons();
        // add first(!) the views populating the tableHelper
        initLayoutLayer("Helper");
        // add second(!) the views populating the tableMain <- sudokuData.setMainButtonsText(...) needs helperTextViews populated
        initLayoutLayer("Main");
    }

    @Override
    protected void onResume() {
        super.onResume();
        sudokuData.startBuilder();
    }

    public void initLayoutLayer(final String type) {
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
                childViewParams[j] = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
                childViewParams[j].setMargins((i % 3) == 0 ? 3 : 1, (j % 3) == 0 ? 3 : 1, 0, 0);
                layoutCols[i].addView(type.equals("Main") ? mainButtons[arrId] : helperTextViews[arrId], childViewParams[j]);
            }
        }
    }

    public void initMainButtons(final int arrId, boolean firstRun) {
        mainButtons[arrId] = new Button(this);
        mainButtons[arrId].setTag("main" + arrId);
        mainButtons[arrId].setBackgroundResource(0);
        mainButtons[arrId].setOnClickListener(this);
        mainButtons[arrId].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
        if (!firstRun) sudokuData.setMainButtonsContent(arrId, true);
    }

    public void initHelperTextViews(final int arrId, boolean firstRun) {
        helperTextViews[arrId] = new TextView(this);
        helperTextViews[arrId].setTag("helper" + arrId);
        helperTextViews[arrId].setGravity(Gravity.CENTER);
        helperTextViews[arrId].setBackgroundColor(ContextCompat.getColor(this,R.color.backgroundUntouched));
        if (!firstRun) sudokuData.setHelperTextViewContent(arrId);
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
                sudokuData.logInfo();
                Toast.makeText(this, "Info selected", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:
                Intent intent = new Intent (this, PreferencesActivity.class);
                startActivity(intent);
                break;
            case R.id.action_new_game:
                Toast.makeText(this, "New game selected", Toast.LENGTH_SHORT).show();
                sudokuData.newGame();
                break;
            case R.id.action_reset:
                Toast.makeText(this, "Restart selected", Toast.LENGTH_SHORT).show();
                sudokuData.resetGame(false);
                break;
            case R.id.action_suggest_field:
                sudokuData.suggestField(true);
                break;
            case R.id.action_back:
                sudokuData.goBack();
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
    public void onClick(final View view) {
        boolean easyTouch = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferencesFragment.KEY_PREF_EASY_TOUCH, false);
        int arrId = Integer.valueOf(((String) view.getTag()).substring(4));
        if (sudokuData.setRequestViewId(arrId)) {
            sudokuData.setEasyTouchArea(-1);
            return;
        }

        if (easyTouch) {
            if (sudokuData.getArrIdEasyTouchButton() != arrId) {
                sudokuData.setEasyTouchArea(arrId);
                return;
            }
        } else sudokuData.setEasyTouchArea(-1);
        Intent intent = new Intent(this, ChooseInputActivity.class);
        startActivityForResult(intent, CHOOSE_INPUT_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_INPUT_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            sudokuData.setEasyTouchArea(-1);
            String chooseInputViewTag = data.getStringExtra("chooseInputViewTag");
            int arrId = sudokuData.getRequestViewId();

            if (chooseInputViewTag.substring(0,2).equals("is")) {
                String value = chooseInputViewTag.substring(2);
                int number = Integer.valueOf(value);
                // if the chosen number already populates the button we delete the text and replace otherwise
                if (mainButtons[arrId].getText().equals(value)) sudokuData.updateSudoku(0, arrId);
                else {
                    // if the button is already populated by another number we delete it first
                    // otherwise the hints would not get updated
                    if (!mainButtons[arrId].getText().equals("")) sudokuData.updateSudoku(0, arrId);
                    sudokuData.updateSudoku(number, arrId);
                }
            } else if (chooseInputViewTag.substring(0,3).equals("not")) {
                int num = Integer.valueOf(chooseInputViewTag.substring(3)) - 1;
                sudokuData.setUserHint(arrId, num);
                sudokuData.setHelperTextViewContent(arrId);
                sudokuData.updateSudokuHintVersion(arrId, num);
            }

        }
    }

    public static void setTextSizeMainButtons() {
        float textSize = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).getString(PreferencesFragment.KEY_PREF_FONT_SIZE_MAIN, "20"));
        for (Button button: mainButtons) button.setTextSize(textSize);
    }

    public static void setTextSizeHelperTextViews() {
        float textSize = Float.valueOf(PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext()).getString(PreferencesFragment.KEY_PREF_FONT_SIZE_HELPER, "13"));
        for (TextView textView: helperTextViews) textView.setTextSize(textSize);
    }
}
