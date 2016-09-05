package net.koudela.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.IntegerRes;
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
    private final static int CHOOSE_INPUT_REQUEST = 1;
    private final static int DIM = 9;
    private View requestView;
    private Button[] mainButtons = new Button[DIM * DIM];
    private TextView[] helperTextViews = new TextView[DIM*DIM];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTableMain();
        initTableHelper();
    }
    // add the views populating the tableMain
    private void initTableMain() {
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
    private void initTableHelper() {
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

            for(int j=0; j<DIM; j++) {
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
            String requestViewTag = (String) requestView.getTag();
            String chooseInputViewTag = data.getStringExtra("chooseInputViewTag");
            Toast.makeText(getApplicationContext(), requestViewTag+";"+chooseInputViewTag, Toast.LENGTH_SHORT).show();

            int arr_id = Integer.valueOf(((String) requestView.getTag()).substring(4));
            if (chooseInputViewTag.substring(0,2).equals("is")) {
                String val = chooseInputViewTag.substring(2);
                // if the chosen number already populates the button we delete the text and replace otherwise
                if (mainButtons[arr_id].getText().equals(val)) {
                    mainButtons[arr_id].setText("");
                    // making the hint 'visible'; (hint is the background for button!)
                    helperTextViews[arr_id].setTextColor(Color.rgb(204,0,0));

                } else {
                    mainButtons[arr_id].setText(val);
                    // making the hint 'invisible'; (hint is the background for button!)
                    helperTextViews[arr_id].setTextColor(Color.rgb(255,255,255));
                }
            } else if (chooseInputViewTag.substring(0,3).equals("not")) {
                int val = Integer.valueOf(chooseInputViewTag.substring(3));
                int pos = (val <= 3?val-1:(val <= 6?val:val+1));
                String hint = (String) helperTextViews[arr_id].getText();
                // if the chosen number already populates the hint we replace it with a "-" and replace the "-" with the number otherwise
                hint = hint.substring(0, pos) + (hint.substring(pos, pos+1).equals(String.valueOf(val))?"-":String.valueOf(val)) + hint.substring(pos + 1);
                helperTextViews[arr_id].setText(hint);
            }
        }
    }
}
