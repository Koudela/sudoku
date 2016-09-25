package net.koudela.sudoku;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Is used to retain {@link SudokuData} when the MainActivity gets destroyed.
 *
 * @author Thomas Koudela
 * @version 1.0 stable
 */
public class RetainedFragment extends Fragment {
    @SuppressWarnings("unused")
    private SudokuData sudokuData = SudokuData.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }
}