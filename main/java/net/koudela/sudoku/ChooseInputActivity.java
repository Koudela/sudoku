package net.koudela.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

/**
 * Created by lana on 05.09.2016.
 */
public class ChooseInputActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_input);
    }
    public void chooseInput(View view) {
        Intent resultIntent = new Intent();
        //resultIntent.putExtra("requestViewTag", getIntent().getStringExtra("requestViewTag"));
        resultIntent.putExtra("chooseInputViewTag", (String) view.getTag());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
