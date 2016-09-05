package net.koudela.sudoku;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class ChooseInputActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_input);
    }
    public void chooseInput(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("chooseInputViewTag", (String) view.getTag());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}
