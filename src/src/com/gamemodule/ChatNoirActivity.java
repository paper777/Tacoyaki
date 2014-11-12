package com.gamemodule;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gamemodule.chatnoir.Game;
import com.gamemodule.chatnoir.GameConfig;
import com.gamemodule.chatnoir.GameView;
import com.gamemodule.chatnoir.Player;


public class ChatNoirActivity extends Activity {

	private final static String TAG = "chatNoir activity";
	private final static int UNDONR = 1;
    
    private GameView mGameView;
    private Game mGame; 
    private Player me; //user
    
    private BootstrapButton restart;
    private BootstrapButton undo;
    private int undoNr = UNDONR;  
    
    private TextView steps;
    
    @SuppressLint("HandlerLeak") private Handler mRefreshHandler = new Handler(){

    	public void handleMessage(Message msg) {
    		Log.d(TAG, "refresh action="+ msg.what);
    		switch (msg.what) {
    		case GameConfig.GAME_OVER:
    			if (msg.arg1 == GameConfig.WIN){
    				showResultDialog("WIN! ");
    				me.win();
    			} else if (msg.arg1 == GameConfig.LOSE) {
    				showResultDialog("LOSE...");
    			} 
    			break;
    		case GameConfig.ADD_STEPS:
    			if(undoNr != 0)
    				undo.setBootstrapButtonEnabled(true);
    			updateSteps();
    			break;
    		default:
    			break;
    		}
    	};
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_noir);
        initView();
        initGame();
        undo.setBootstrapButtonEnabled(false);
    }
    
    private void initView(){
    	mGameView = (GameView) findViewById(R.id.game_view);
        restart = (BootstrapButton) findViewById(R.id.restart_button);
        undo = (BootstrapButton) findViewById(R.id.undo_button);
        
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"jockerman.ttf");
        steps = (TextView) findViewById(R.id.steps);
		steps.setTypeface(typeFace);
		TextView dis_step = (TextView) findViewById(R.id.cnSteps);
		dis_step.setText("STEPS: ");
		dis_step.setTypeface(typeFace);
		
		
	
        // restart button listener
        restart.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				mGame.reset();
				mGameView.drawGame();
				updateSteps();
				undoNr = UNDONR;
				undo.setBootstrapButtonEnabled(false);
			}
        	
        });
        
        // undo button listener
        undo.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Log.d(TAG, "undo pressed");
				if(Integer.parseInt(mGame.getSteps()) != 0 && undoNr != 0){
					Log.d(TAG, "undo nr: " + undoNr);
					rollback();
					if((--undoNr) == 0)
						undo.setBootstrapButtonEnabled(false);
				}// if
			}
        });
    }
    
    private void initGame(){
    	me = new Player("ALSO", 1);
        mGame = new Game(mRefreshHandler, me);
        mGameView.setGame(mGame);
    }
    
    /*
    @Override
    public void onClick(View v) {
        //switch (v.getId()) {
        //case R.id.restart_button:
    	if(v.getId() == R.id.restart_button){
            mGame.reset();
            mGameView.drawGame();
            updateSteps();
            undoNr = 1;
        	undo.setBootstrapButtonEnabled(true);
    	}
        //case R.id.undo_button:
    	else if(v.getId() == R.id.undo_button){
        	if(undoNr == 1 && rollback()){
        		undoNr = 0;
        		undo.setBootstrapButtonEnabled(false);
        	}
        }
    }
    */
    

    // show result win or lose
    private void showResultDialog(String message){
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setCancelable(false);
        b.setMessage(message);
        b.setPositiveButton("Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mGame.reset();
                mGameView.drawGame();
                undoNr = UNDONR; 
            }
        });
        
        b.setNegativeButton("Quit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        b.show();
    }
    
    private void updateSteps(){
    	steps.setText(mGame.getSteps());
    }
    
    private boolean rollback(){
    	if(mGame.rollback()){ 
    		mGameView.drawGame();
    		return true;
    	} else { return false; }
    }
    
}