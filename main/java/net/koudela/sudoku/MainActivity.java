package net.koudela.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    protected final static int CHOOSE_INPUT_REQUEST = 1;
    protected final static int DIM = 9;
    protected View requestView;
    protected Button[] mainButtons = new Button[DIM * DIM];
    protected TextView[] helperTextViews = new TextView[DIM*DIM];
    protected int[][] sudokuGroups = {
            {0,1,2,9,10,11,18,19,20},
            {27,28,29,36,37,38,45,46,47},
            {54,55,56,63,64,65,72,73,74},
            {3,4,5,12,13,14,21,22,23},
            {30,31,32,39,40,41,48,49,50},
            {57,58,59,66,67,68,75,76,77},
            {6,7,8,15,16,17,24,25,26},
            {33,34,35,42,43,44,51,52,53},
            {60,61,62,69,70,71,78,79,80}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTableMain();
        initTableHelper();
    }
    // add the views populating the tableMain
    protected void initTableMain() {
        LinearLayout[] mainLayoutCols = new LinearLayout[DIM];
        LinearLayout.LayoutParams[] mainButtonParams = new LinearLayout.LayoutParams[DIM];

        LinearLayout tableMain = (LinearLayout) findViewById(R.id.tableMain);
        LinearLayout.LayoutParams mainLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);

        for (int i = 0; i < DIM; i++) {
            mainLayoutCols[i] = new LinearLayout(this);
            mainLayoutCols[i].setOrientation(LinearLayout.VERTICAL);
            mainLayoutCols[i].setTag("column" + i + "Main");
            tableMain.addView(mainLayoutCols[i], mainLayoutParams);

            for (int j = 0; j < DIM; j++) {
                mainButtonParams[j] = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
                mainButtonParams[j].setMargins((i % 3) == 0 ? 3 : 1, (j % 3) == 0 ? 3 : 1, 0, 0);
                int arr_id = i * DIM + j;
                String id = "main" + arr_id;
                mainButtons[arr_id] = new Button(this);
                mainButtons[arr_id].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));
                mainButtons[arr_id].setTag(id);
                mainButtons[arr_id].setText("");
                mainButtons[arr_id].setBackgroundResource(0);
                mainButtons[arr_id].setOnClickListener(this);
                mainLayoutCols[i].addView(mainButtons[arr_id], mainButtonParams[j]);
            }
        }
    }
    // add the views populating the tableHelper
    protected void initTableHelper() {
        LinearLayout[] helperLayoutCols = new LinearLayout[DIM];

        LinearLayout tableHelper = (LinearLayout) findViewById(R.id.tableHelper);
        LinearLayout.LayoutParams helperLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        LinearLayout.LayoutParams[] helperTextViewParams = new LinearLayout.LayoutParams[DIM];

        String text = "---\n---\n---";

        for (int i=0; i<DIM; i++) {
            helperLayoutCols[i] = new LinearLayout(this);
            helperLayoutCols[i].setOrientation(LinearLayout.VERTICAL);
            helperLayoutCols[i].setTag("column"+i+"Helper");
            helperLayoutCols[i].setBackgroundColor(Color.rgb(0,0,0));
            tableHelper.addView(helperLayoutCols[i], helperLayoutParams);

            for (int j=0; j<DIM; j++) {
                helperTextViewParams[j] = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
                helperTextViewParams[j].setMargins((i % 3) == 0?3:1, (j % 3) == 0?3:1, 0, 0);
                int arr_id = i * DIM + j;
                String id = "helper"+arr_id;
                helperTextViews[arr_id] = new TextView(this);
                helperTextViews[arr_id].setTag(id);
                helperTextViews[arr_id].setText(text);
                helperTextViews[arr_id].setGravity(Gravity.CENTER);
                helperTextViews[arr_id].setTextColor(Color.rgb(204,0,0));
                helperTextViews[arr_id].setBackgroundColor(Color.rgb(255,255,255));
                helperTextViews[arr_id].setOnClickListener(this);
                helperLayoutCols[i].addView(helperTextViews[arr_id], helperTextViewParams[j]);
            }
        }
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
        Intent intent = new Intent(this, ChooseInputActivity.class);
        requestView = view;
        startActivityForResult(intent, CHOOSE_INPUT_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_INPUT_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            String chooseInputViewTag = data.getStringExtra("chooseInputViewTag");

            boolean autoHint = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PreferencesFragment.KEY_PREF_AUTO_HINT, false);
            int arr_id = Integer.valueOf(((String) requestView.getTag()).substring(4));
            if (chooseInputViewTag.substring(0,2).equals("is")) {
                String value = chooseInputViewTag.substring(2);
                // if the chosen number already populates the button we delete the text and replace otherwise
                if (mainButtons[arr_id].getText().equals(value)) {
                    mainButtons[arr_id].setText("");
                    // making the hint 'visible'; (hint is the background for button!)
                    helperTextViews[arr_id].setTextColor(Color.rgb(204,0,0));
                } else {
                    mainButtons[arr_id].setText(value);
                    // making the hint 'invisible'; (hint is the background for button!)
                    helperTextViews[arr_id].setTextColor(Color.rgb(255,255,255));
                    if (autoHint) setAutoHints(arr_id, value);
                }
            } else if (chooseInputViewTag.substring(0,3).equals("not")) {
                setHint(arr_id, chooseInputViewTag.substring(3), true);
            }
        }
    }
    // delete == false: we replace the old entry with the value on the corresponding position
    // delete == true: if the value already populates the hint we replace it with a "-" and replace the "-" with value otherwise
    protected void setHint (int arr_id, String value, boolean delete) {
        int val = Integer.valueOf(value);
        int pos = (val <= 3?val-1:(val <= 6?val:val+1));
        String hint = (String) helperTextViews[arr_id].getText();
        hint = hint.substring(0, pos) + (delete && hint.substring(pos, pos+1).equals(value)?"-":value) + hint.substring(pos + 1);
        helperTextViews[arr_id].setText(hint);
    }
    protected void setAutoHints (int arr_id, String value) {
        int ii = arr_id / DIM;
        int jj = arr_id % DIM;
        int[] sudokuGroup = getSudokuGroup(arr_id);
        for (int i=0; i<sudokuGroup.length; i++) {
            setHint(sudokuGroup[i], value, false);
        }
        for (int i=0; i<DIM; i++) {
            arr_id = i * DIM + jj;
            setHint(arr_id, value, false);
        }
        for (int j=0; j<DIM; j++) {
            arr_id = ii * DIM + j;
            setHint(arr_id, value, false);
        }
    }
    protected int[] getSudokuGroup(int arr_id) {
        for (int groupId=0; groupId<DIM; groupId++) {
            for (int index=0; index<DIM; index++) {
                if (sudokuGroups[groupId][index] != arr_id) continue;
                return sudokuGroups[groupId];
            }
        }
        return sudokuGroups[-1];
    }
}
