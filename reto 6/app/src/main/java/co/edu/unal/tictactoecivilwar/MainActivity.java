package co.edu.unal.tictactoecivilwar;


import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    private BoardView mBoardView;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                return true;
            case R.id.settings:
                startActivityForResult(new Intent(this, Settings.class), 0);
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
        }
        return false;
    }

    boolean mSoundOn;
    SharedPreferences mPrefs;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RESULT_CANCELED) {
            // Apply potentially new settings

            mSoundOn = mPrefs.getBoolean("sound", true);

            String difficultyLevel = mPrefs.getString("difficulty_level",
                    getResources().getString(R.string.difficulty_easy));

            if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
            else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
            else
                mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);

        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        switch(id) {
            case DIALOG_DIFFICULTY_ID:

                builder.setTitle(R.string.difficulty_choose);

                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)};

                int selected = mGame.getDifficultyLevel().getValue();

                // selected is the radio button that should be selected.

                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss();   // Close dialog

                                switch (item){
                                    case 0:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                                        startNewGame();
                                        break;
                                    case 1:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                                        startNewGame();
                                        break;
                                    case 2:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                                        startNewGame();
                                        break;
                                }

                                // Display the selected difficulty level
                                Toast.makeText(getApplicationContext(), levels[item],
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                dialog = builder.create();

                break;
            case DIALOG_QUIT_ID:
                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();
                break;
        }
        return dialog;
    }

    // Represents the internal state of the game
    private TicTacToeGame mGame;

    // Buttons making up the board
    private Button mBoardButtons[];

    private int[] history;
    // Various text displayed
    private TextView mInfoTextView, HumanWon, TiesNumber, ComputerWon;

    private boolean mGameOver;

    //sounds
    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;

    Handler handler = new Handler();

    // Set up the game board.
    private void startNewGame() {

        mGame.clearBoard();
        mBoardView.invalidate(); // Redraw the board
        mGameOver=false;

        // Human goes first
        mInfoTextView.setText(R.string.first_human);

    }    // End of startNewGame

    private int winner;

    private OnTouchListener mTouchListener = new OnTouchListener() {

        private boolean setMove(char player, int location) {
            if (mGame.setMove(player, location)) {
                mBoardView.invalidate();
                // Redraw the board
                return true;
            }
            return false;

        }

        // Listen for touches on the board
        public boolean onTouch(View v, MotionEvent event) {
            // Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;
            System.out.println("Human moving to pos: " + pos);
            if (!mGameOver && setMove(TicTacToeGame.HUMAN_PLAYER, pos)) {
                if(mSoundOn)mHumanMediaPlayer.start();

                handler.postDelayed(new Runnable() {
                    public void run() {

                        winner = mGame.checkForWinner();
                        if (winner == 0) {
                            mInfoTextView.setText(R.string.turn_computer);
                            int move = mGame.getComputerMove();
                            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                            mBoardView.invalidate();
                            if(mSoundOn)mComputerMediaPlayer.start();
                            winner = mGame.checkForWinner();
                        }

                        if (winner == 0)
                            mInfoTextView.setText(R.string.turn_human);
                        else if (winner == 1) {
                            mInfoTextView.setText(R.string.result_tie);
                            history[1]+=1;
                            TiesNumber.setText(Integer.toString(history[1]));
                        }
                        else if (winner == 2) {
                            String defaultMessage = getResources().getString(R.string.result_human_wins);
                            mInfoTextView.setText(mPrefs.getString("victory_message", defaultMessage));

                            history[0]+=1;
                            HumanWon.setText(Integer.toString(history[0]));
                            mGameOver = true;
                        } else {
                            mInfoTextView.setText(R.string.result_computer_wins);
                            history[2]+=1;
                            ComputerWon.setText(Integer.toString(history[2]));
                            mGameOver = true;
                        }
                    }
                }, 750);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoTextView = (TextView) findViewById(R.id.information);
        HumanWon = (TextView) findViewById(R.id.human_won);
        TiesNumber = (TextView) findViewById(R.id.finish_tie);
        ComputerWon = (TextView) findViewById(R.id.android_won);

        history = new int[]{0, 0, 0};

        mGame = new TicTacToeGame();
        mBoardView = (BoardView) findViewById(R.id.board);
        // Restore the scores from the persistent preference data source
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mSoundOn = mPrefs.getBoolean("sound", true);
        String difficultyLevel = mPrefs.getString("difficulty_level",
                getResources().getString(R.string.difficulty_easy));
        if (difficultyLevel.equals(getResources().getString(R.string.difficulty_easy)))
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
        else if (difficultyLevel.equals(getResources().getString(R.string.difficulty_harder)))
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
        else
            mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
        mBoardView.setGame(mGame);
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);
        startNewGame();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.cap1);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.iron1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }
}