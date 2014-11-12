/**
 *  @file GameView.java
 *  @author ALSO wu
 *  @description: 1) 
 *  ANY BUGS PLEASE CONNECT ME: alsoblack222#gmail.com 
 *  */

package com.gamemodule.chatnoir;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Game {
	private static final String TAG = "ChatNoir Game";
	
	private int mode = 0;
	private int playerSteps = 0;
	private int mGameWidth = 0;
	private int mGameHeight = 0;
 
	private int[][] mGameMap = null;
	private Deque<Coordinate> mActions;

	private Player player;
	
	private Handler mNotify;
	
	private AiCat ai;
	private Coordinate catPos;


	public Game(Handler handle, Player me) {
		this(handle, me, GameConfig.SCALE_MEDIUM, GameConfig.SCALE_MEDIUM);
	}

	public Game(Handler handle, Player me, int width, int height) {
		this.player = me;
		this.mNotify = handle;
		this.mGameWidth = width;
		this.mGameHeight = height;
		this.mGameMap = new int[mGameHeight][mGameWidth];
		this.mGameMap[mGameHeight / 2][mGameWidth / 2] = GameConfig.TYPE_CAT;
		this.ai = new AiCat(mGameMap);  
		mActions = new LinkedList<Coordinate>();
		catPos = new Coordinate(mGameHeight/2, mGameWidth/2);
		obstacleCircleInit();
	}
	
	// getters
	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getMode() {
		return this.mode;
	}

	public int getBoardWidth() {
		return mGameWidth;
	}

	public int getBoardHeight() {
		return mGameHeight;
	}

	public int[][] getGameMap() {
		return mGameMap;
	}

	public String getSteps(){
		return String.valueOf(playerSteps);
	}
	public Deque<Coordinate> getActions() {
		return mActions;
	}
	
	public Coordinate getCatPos(){
		return catPos;
	}

	// reset the game
	public void reset() {
		mGameMap = new int[mGameHeight][mGameWidth];
		mActions.clear();
		catPos.x = mGameHeight / 2;  
		catPos.y = mGameWidth / 2;  
		this.mGameMap[mGameHeight / 2][mGameWidth / 2] = GameConfig.TYPE_CAT;
		obstacleCircleInit();
		playerSteps = 0;
	}
	
	public boolean rollback(){
		if(mActions.isEmpty()){
			return false;
		}
		// poll the cat position
		mGameMap[catPos.x][catPos.y] = GameConfig.TYPE_EMPTY;
		mActions.pollLast();
		
		Coordinate ob = mActions.pollLast();
		mGameMap[ob.x][ob.y] = GameConfig.TYPE_EMPTY;
		
		catPos = mActions.pollLast();
		if(catPos == null){
			catPos = new Coordinate(mGameHeight/2, mGameWidth/2);
			this.mGameMap[mGameHeight / 2][mGameWidth / 2] = GameConfig.TYPE_CAT;
		} else {
			mGameMap[catPos.x][catPos.y] = GameConfig.TYPE_CAT;
		}
		return true;
	}

	private void sendAddSteps() {
		Message msg = Message.obtain();
		msg.what = GameConfig.ADD_STEPS;
		mNotify.sendMessage(msg);
	
	}
		
	private void sendGameResult(int isWin) {
		player.incCount();
		if(isWin == GameConfig.WIN){
			player.win();
		}
		Message msg = Message.obtain();
		msg.what = GameConfig.GAME_OVER;
		msg.arg1 = isWin;
		mNotify.sendMessage(msg);
	}

	public void addObstacleCircle(int x, int y) {
		if (x < 0 || x == mGameHeight || y < 0 || y == mGameWidth) {
			return;
		}
		if (mGameMap[x][y] == GameConfig.TYPE_EMPTY) {
			mGameMap[x][y] = GameConfig.TYPE_OBSTACLE;
			playerSteps++;
			mActions.add(new Coordinate(x, y));

			ai.updateGameMap(mGameMap); // cat's turn
			Coordinate newPos = ai.getNextPos();// cat's new position
			
			switch(newPos.x){
			case GameConfig.WIN:
				sendGameResult(GameConfig.WIN);
				break;
			case GameConfig.LOSE:
				sendGameResult(GameConfig.LOSE);
				break;
			default:
				mGameMap[catPos.x][catPos.y] = GameConfig.TYPE_EMPTY; 
				mActions.add(newPos);
				catPos = newPos;
				mGameMap[catPos.x][catPos.y] = GameConfig.TYPE_CAT; 
				sendAddSteps();
				break;
			}
		}
	}

	//
	private void obstacleCircleInit() {
		// TODO
		int obNr = Math.abs(new Random().nextInt() % 12) + 3;
		for(int i = 0; i < obNr; i++){
			int x = Math.abs(new Random().nextInt() % mGameMap.length);
			int y = Math.abs(new Random().nextInt() % mGameMap.length);
			if(x == mGameMap.length/2 && x == y) continue;
			mGameMap[x][y] = GameConfig.TYPE_OBSTACLE;
		}
	}
}
