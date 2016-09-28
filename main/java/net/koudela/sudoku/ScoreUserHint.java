package net.koudela.sudoku;

import android.os.AsyncTask;

class ScoreUserHint extends AsyncTask<Void, Void, Integer> {
    private Hints hints;
    private Playground pField;
    private int arrId, num;

    ScoreUserHint(final int arrId, final  int num, final Hints hints, final Playground pField) {
        this.arrId = arrId;
        this.num = num;
        this.hints = hints.clone();
        this.pField = new Playground(pField);
    }

    @Override
    protected Integer doInBackground(Void... placeholder) {
        boolean[] usage = hints.getUsage();
        // the user hint is already set, thus we need the complement
        hints.setUserHint(arrId, num);
        if (hints.isUserHint(arrId, num)) return 0;
        if (!usage[0] && hints.getPlainHint(arrId, num) > 0) return 1;
        if (hints.isHint(arrId, num)) return 0;
        if (!usage[1]) {
            hints.setUseAdv1(true);
            hints.setAutoHintsAdv1(pField, false);
            if (hints.isHint(arrId, num)) return 3;
        }
        if (!usage[2]) {
            hints.setUseAdv2(true);
            hints.setAutoHintsAdv2(pField, false, true);
            if (hints.isHint(arrId, num)) return 6;
        }
        if (!usage[3]) {
            hints.setUseAdv3(true);
            hints.setAutoHintsAdv3(pField, false, true);
            if (hints.isHint(arrId, num)) return 6;
        }
        if (!usage[2]) {
            hints.setUseAdv2(true);
            hints.setAutoHintsAdv2(pField, false, false);
            if (hints.isHint(arrId, num)) return 10;
        }
        if (!usage[3]) {
            hints.setUseAdv3(true);
            hints.setAutoHintsAdv3(pField, false, false);
            if (hints.isHint(arrId, num)) return  10;
        }
        return 2;
    }

    // if a user is very very fast in setting/undoing hints/values there could be a
    // synchronization issue (the wrong history point gets updated);
    @Override
    protected void onPostExecute(final Integer points) {
        ((MainActivity) MainActivity.getContext()).sudokuData.setScore(arrId, points);
    }
}
