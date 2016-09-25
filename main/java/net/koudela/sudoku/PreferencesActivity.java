package net.koudela.sudoku;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * This activity handles in combination with {@link  PreferencesFragment} the settings UI.
 *
 * @author Thomas Koudela
 * @version 1.0 stable
 */
public class PreferencesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content, new PreferencesFragment())
                .commit();
    }
}
