/**
 *  @file GameView.java
 *  @author ALSO wu
 *  @description: 1) circle map init and the change after touching
 *  
 *  ANY BUGS PLEASE CONNECT ME: alsoblack222#gmail.com 
 *  
 *  */

package com.gamemodule.tacoyaki;

import java.util.Random;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Game {
	private static final String TAG = "TAGAME";
	private static final boolean DEBUG = false;
	
	
	private int width;   
	private Handler mNotify;
	private int[][] gameMap;
	private int mod = 0; // easy/normal/hard
	private int gameType = 0; // tacoyaki OR tacoyaki plus
	private boolean timerFlag = false; // flag used to start time counting up
	

	public Game(Handler handler, int type, int mod){
		this.mNotify = handler;
		this.gameType = type;
		this.mod =mod;
		initGameMap();
	}
	
	// after user win the current mode, this func will be executed
	public void levelUP(){
		if(mod == GameConfig.HARD_MOD)return;
		
		if(mod == GameConfig.EASY_MOD){
			mod = GameConfig.NORMAL_MOD;
		} else if(mod == GameConfig.NORMAL_MOD){
			mod = GameConfig.HARD_MOD;
		}
		initGameMap();
	}
	
	// switch game type between tacoyaki and tacoyaki+
	// actually tacoyaki is simpler
	public void switchGame(int type){
		if(type == gameType) return;
		gameType = type;
		if(type == GameConfig.TA_DEFAULT)
			mod = GameConfig.NORMAL_MOD;
		else
			mod = GameConfig.EASY_MOD;
		initGameMap();
	}

	/*
	 *  getters
	 */
	public int getWidth() {
		return width;
	}
	
	public int getGameMod(){
		return mod;
	}
	
	public int getGameType(){
		return gameType;
	}
	
	public int[][] getGameMap(){
		return gameMap;
	}
	
	// when a circle has been clicked, the game map need to be changed depending on game type
	public void updateGameMap(int x, int y){
		if(! checkPos(x, y)) return;
		
		// just change the type of each related circle in the map
		switch(gameType){
		case GameConfig.TA_DEFAULT: // tacoyaki type
			gameMap[x][y] = (gameMap[x][y] == 
				GameConfig.TYPE_BLACK ? GameConfig.TYPE_WHITE : GameConfig.TYPE_BLACK);
			if(x-1 >= 0)
				gameMap[x-1][y] = (gameMap[x-1][y] == 
					GameConfig.TYPE_BLACK ? GameConfig.TYPE_WHITE : GameConfig.TYPE_BLACK);
			if(x+1 < width)
				gameMap[x+1][y] = (gameMap[x+1][y] == 
					GameConfig.TYPE_BLACK ? GameConfig.TYPE_WHITE : GameConfig.TYPE_BLACK);
			if(y+1 < width)
				gameMap[x][y+1] = (gameMap[x][y+1] == 
					GameConfig.TYPE_BLACK ? GameConfig.TYPE_WHITE : GameConfig.TYPE_BLACK);
			if(y-1 >= 0)
				gameMap[x][y-1] = (gameMap[x][y-1] == 
					GameConfig.TYPE_BLACK ? GameConfig.TYPE_WHITE : GameConfig.TYPE_BLACK);
			break;
		case GameConfig.TA_PLUS: // tacoyaki plus
			for(int i = 0; i < width; i++){
				for(int j = 0; j < width; j++){
					if(Math.abs(i - x) == Math.abs(j - y)){
						gameMap[i][j] = (gameMap[i][j] == 
								GameConfig.TYPE_BLACK ? GameConfig.TYPE_WHITE : GameConfig.TYPE_BLACK);
					}
				}
			}
			break;
			default: break;
		}
		
		// judge whether the game is end.
		if(isGameEnd()){
			sendGameResult(GameConfig.PASS);
		} else if(timerFlag){
			sendCounterStart();
			Log.d(TAG, "timer start signal sent");
			timerFlag = false;
		}
	}
	
	private void initGameMap(){
		if(mod == GameConfig.EASY_MOD) width = GameConfig.SCALE_SMALL;
		else if(mod == GameConfig.NORMAL_MOD) width = GameConfig.SCALE_MEDIUM;
		else if(mod == GameConfig.HARD_MOD) width = GameConfig.SCALE_LARGE;
		
		timerFlag = true;
		gameMap = null; // ...
		gameMap = new int[width][width];
		
		if(DEBUG){
			if(gameType == 0)
				gameMap[0][0] = gameMap[0][1]=gameMap[1][0] = GameConfig.TYPE_BLACK;
			else
				for(int i = 0; i < width; i++)
					gameMap[i][i] = GameConfig.TYPE_BLACK;
			return;
		}
		
		
		// init the map
		int blackNr = (int) (Math.abs(new Random().nextInt() % (width * width))) + 1;
		Log.d(TAG, "black nr: " + blackNr);
		
		for(int i =0; i < blackNr; i++){
			gameMap[(int) (Math.abs(new Random().nextInt() % width))] 
				   [(int) (Math.abs(new Random().nextInt() % width))]
					= GameConfig.TYPE_BLACK;
		}
	}
	
	public boolean isGameEnd(){
		if(gameMap[0][0] == GameConfig.TYPE_BLACK){
			for(int i = 0; i < width; i++){
				for(int j = 0; j < width; j++){
					if(gameMap[i][j] == GameConfig.TYPE_WHITE)
						return false;
				}
			}
			return true;
		} else{
			for(int i = 0; i < width; i++){
				for(int j = 0; j < width; j++){
					if(gameMap[i][j] == GameConfig.TYPE_BLACK)
						return false;
				}
			}
			return true;
		}
	}// func ends
	
	private void sendCounterStart(){
		Message msg = Message.obtain();
		msg.what = GameConfig.STARTTIMER;
		mNotify.sendMessage(msg);
	}
		
	private void sendGameResult(int state) {
		Message msg = Message.obtain();
		msg.what = GameConfig.PASS;
		msg.arg1 = state;
		mNotify.sendMessage(msg);
	}
	
	private boolean checkPos(int x, int y){
		if(x < 0 || y >= width || y < 0 || y >= width)
			return false;
		return true;
	}

}
