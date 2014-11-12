package com.gamemodule;

import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.gamemodule.tacoyaki.Game;
import com.gamemodule.tacoyaki.GameConfig;
import com.gamemodule.tacoyaki.GameView;
import com.github.amlcurran.showcaseview.ApiUtils;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

public class TacoyakiActivity extends Activity implements View.OnClickListener{ 

	private static final String TAG = "tacoyaki Activity";
	private static final boolean DEBUG = true;

	private GameView mGameView;
	private Game mGame;
	private int gameType; // tacoyaki OR tacoyaki plus
	private BootstrapButton tacoyaki1;
	private BootstrapButton tacoyaki2;
	
	// time counter
	private Chronometer timeCount;
	
	// store records
	private SharedPreferences sp;
	private TextView record;

	// first start using showcase
    private ShowcaseView showcaseView;
    private int scvCount = 0;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
		setContentView(R.layout.tacoyaki);
		
		// sharedpreferences instantiated
		sp = getSharedPreferences("tacoyaki_record", MODE_PRIVATE);
		initView();
		gameType = GameConfig.TA_DEFAULT;
		initGame(gameType, GameConfig.NORMAL_MOD);
		// time counter
		timeCount = (Chronometer) findViewById(R.id.chronometer);
	}

	private void initView() {
		/*
		// set game view's background color
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.tacoyaki);
		// random background color
		Random rd = new Random();
		rl.setBackgroundColor(Color.rgb(rd.nextInt(256), rd.nextInt(256), rd.nextInt(256)));
		*/
		
		mGameView = (GameView) findViewById(R.id.ta_view);
				 
		record = (TextView) findViewById(R.id.besttime);
		
		/* below is best & time count display area */
		// set font style
		TextView dis_best = (TextView) findViewById(R.id.dis_besttime);
		TextView dis_time = (TextView) findViewById(R.id.dis_timecount);
		Typeface typeFace = Typeface.createFromAsset(getAssets(),"jockerman.ttf");
		dis_best.setTypeface(typeFace);
		dis_time.setTypeface(typeFace);
		
		//display record
		String tmp = sp.getString("best"+GameConfig.TA_DEFAULT+GameConfig.NORMAL_MOD,"");
		if(tmp != "") record.setText(tmp+"s");
		/* end of display */
		
		
		/* below is game mode switch though these two button */
		tacoyaki1 = (BootstrapButton) findViewById(R.id.tacoyakiBtn1);
		tacoyaki1.setBootstrapButtonEnabled(false);
		tacoyaki2 = (BootstrapButton) findViewById(R.id.tacoyakiBtn2);
		// button clicked event
		tacoyaki1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gameType = mGame.getGameType();
				if(gameType != GameConfig.TA_DEFAULT){
					// change these two button style 
					tacoyaki1.setBootstrapButtonEnabled(false);
					tacoyaki2.setBootstrapButtonEnabled(true);
					// switch game in the game view layer, and game view will call game layer to change result detection
					mGameView.switchGame(GameConfig.TA_DEFAULT);
					// change background color
					RelativeLayout rl = (RelativeLayout) findViewById(R.id.tacoyaki);
					Random rd = new Random();
					rl.setBackgroundColor(Color.rgb(rd.nextInt(256), rd.nextInt(256), rd.nextInt(256)));
					// new game type. DO NOT forget update it
					gameType = mGame.getGameType();
					
					timeCount.stop();
					// reset time
    				timeCount.setBase(SystemClock.elapsedRealtime());
    				// display curr game record
    				String tmp = sp.getString("best"+gameType+mGame.getGameMod(),"");
    				if(tmp != "") record.setText(tmp+"s");
    				else record.setText("");
				}// if
			}
		});
		
		// same as tacoyaki1
		tacoyaki2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				gameType = mGame.getGameType();
				if(gameType != GameConfig.TA_PLUS){
					tacoyaki2.setBootstrapButtonEnabled(false);
					tacoyaki1.setBootstrapButtonEnabled(true);
					RelativeLayout rl = (RelativeLayout) findViewById(R.id.tacoyaki);
					// change background color
					Random rd = new Random();
					rl.setBackgroundColor(Color.rgb(rd.nextInt(256), rd.nextInt(256), rd.nextInt(256)));
					
					mGameView.switchGame(GameConfig.TA_PLUS);
					gameType = mGame.getGameType();
					timeCount.stop();
    				timeCount.setBase(SystemClock.elapsedRealtime());
    				String tmp = sp.getString("best"+gameType+mGame.getGameMod(),"");
    				Log.d(TAG, "temp: "+tmp);
    				if(tmp != "") record.setText(tmp+"s");
    				else record.setText("");
				}// if
			}// func onclick
		});

		// first start showCase
		int flag = sp.getInt("firstLaunch",0);
		if(flag == 0 || DEBUG) {
			startShowCaseView();
			sp.edit().putInt("firstLaunch", 1).commit();
		}
	}


	private void initGame(int type, int mod) {
		mGame = new Game(mRefreshHandler, type, mod);
		if(mGame == null){
			Log.d(TAG, "mgame is null");
		}
		mGameView.setGame(mGame);
	}
	
	private Handler mRefreshHandler = new Handler(){
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
    		case GameConfig.PASS:
    			int mod = mGame.getGameMod();
    			// time counter stop
    			timeCount.stop();
    			// compare curr and best
    			int currTime = (int) (SystemClock.elapsedRealtime() - timeCount.getBase()) / 1000;
    			String tmp = sp.getString("best"+gameType+mod,"");
    			// result of first play is also the first record
    			if(tmp == "") sp.edit().putString("best"+gameType+mod,String.valueOf(currTime)).commit();
    			else if(Integer.parseInt(tmp) > currTime){// new record!
    				// TODO new record dialog
    				sp.edit().putString("best"+gameType+mod,String.valueOf(currTime)).commit();
    			}
    			
    			if(mod == GameConfig.HARD_MOD){
    				showWinDialog();
    			}else{
    				mGameView.levelUP();
    				timeCount.setBase(SystemClock.elapsedRealtime());
    				String tmp1 = sp.getString("best"+gameType+mGame.getGameMod(),"");
    				if(tmp1 != "") 
    					record.setText(tmp1+"s") ;
    			}
    			break;
    		case GameConfig.STARTTIMER:
    			timeCount.setBase(SystemClock.elapsedRealtime());
    			timeCount.start();
    			break;
    		default:
    			break;
    		}
    	};
    };
    
    private void showWinDialog(){
    	// TODO 
    	// need to add some features 
    	AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setCancelable(false);
        b.setMessage("Congratulations!");
        b.setPositiveButton("Again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	gameType = mGame.getGameType();
				if(gameType == GameConfig.TA_PLUS){
					tacoyaki1.setBootstrapButtonEnabled(false);
					tacoyaki2.setBootstrapButtonEnabled(true);
					mGameView.switchGame(GameConfig.TA_DEFAULT);
				} else {
					tacoyaki1.setBootstrapButtonEnabled(true);
					tacoyaki2.setBootstrapButtonEnabled(false);
					mGameView.switchGame(GameConfig.TA_PLUS);
				}
            	
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
    
	private void startShowCaseView() {
		showcaseView = new ShowcaseView.Builder(this)
		.setOnClickListener(this)
		.build();
//		showcaseView.setTarget(new ViewTarget(R.id.ta_view, this));
		showcaseView.setTarget(Target.NONE);
		showcaseView.setStyle(R.style.CustomShowcaseTheme2);
		showcaseView.setContentTitle(getString(R.string.scv_title));
		showcaseView.setContentText(getString(R.string.ta_tutorial));
		showcaseView.setButtonText(getString(R.string.scv_next));	
		setAlpha(1.0f, mGameView, tacoyaki1);
	}

	@Override
	public void onClick(View v) {
		switch (scvCount) {
		case 0:
			showcaseView.setTarget(new ViewTarget(findViewById(R.id.tacoyakiBtn1)));
			showcaseView.setContentText(getString(R.string.ta_switchgame));
			showcaseView.setShowcase(new ViewTarget(tacoyaki1), true);
			break;

		case 1:
			showcaseView.setContentText(getString(R.string.ta_switchgame));
			showcaseView.setShowcase(new ViewTarget(tacoyaki2), true);
			break;
			
		case 2:
			showcaseView.hide();
			setAlpha(1.0f, mGameView, tacoyaki1);
			break;
		}
		scvCount++;
	}// func
	
    private void setAlpha(float alpha, View... views) {
    	ApiUtils apiUtils = new ApiUtils();
        if (apiUtils.isCompatWithHoneycomb()) {
            for (View view : views) {
                view.setAlpha(alpha);
            }
        }
    }
}
