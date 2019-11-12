package co.edu.unal.tictactoe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

public class BoardView extends View{

    // Width of the board grid lines
    public static final int GRID_WIDTH = 6;

    private Bitmap mHumanBitmap;
    private Bitmap mComputerBitmap;

    public void initialize() {
        mHumanBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.|);
        mComputerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.o_img);
    }
}