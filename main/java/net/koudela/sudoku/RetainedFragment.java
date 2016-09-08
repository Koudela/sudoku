package net.koudela.sudoku;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class RetainedFragment extends Fragment {
    private SudokuData sudokuData = SudokuData.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }
}