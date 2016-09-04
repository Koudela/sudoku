package net.koudela.sudoku;

import android.graphics.Color;
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
    private final static int DIM = 9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTableMain();
        initTableHelper();
    }
    // add the views populating the tableMain
    private void initTableMain() {
        Button[] mainButtons = new Button[DIM * DIM];
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
                String id = "main" + (i + 1) + "" + (j + 1);
                int arr_id = i * DIM + j;
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
        TextView[] helperTextViews = new TextView[DIM*DIM];
        LinearLayout[] helperLayoutCols = new LinearLayout[DIM];

        LinearLayout tableHelper = (LinearLayout) findViewById(R.id.tableHelper);
        LinearLayout.LayoutParams helperLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
        LinearLayout.LayoutParams[] helperTextViewParams = new LinearLayout.LayoutParams[DIM];

        String text = "1-3\n-56\n--9";

        for (int i=0; i<DIM; i++) {
            helperLayoutCols[i] = new LinearLayout(this);
            helperLayoutCols[i].setOrientation(LinearLayout.VERTICAL);
            helperLayoutCols[i].setTag("column"+i+"Helper");
            helperLayoutCols[i].setBackgroundColor(Color.rgb(0,0,0));
            tableHelper.addView(helperLayoutCols[i], helperLayoutParams);

            for(int j=0; j<DIM; j++) {
                helperTextViewParams[j] = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
                helperTextViewParams[j].setMargins((i % 3) == 0?3:1, (j % 3) == 0?3:1, 0, 0);
                String id = "helper"+(i+1)+""+(j+1);
                int arr_id = i * DIM + j;
                helperTextViews[arr_id] = new TextView(this);
                helperTextViews[arr_id].setTag(id);
                helperTextViews[arr_id].setText(text);
                helperTextViews[arr_id].setGravity(Gravity.CENTER);
                helperTextViews[arr_id].setTextColor(Color.rgb(255,0,0));
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
        Toast.makeText(getApplicationContext(), (String) view.getTag(), Toast.LENGTH_SHORT).show();
    }

}
