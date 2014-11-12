/**
 *  @file GameView.java
 *  @author ALSO wu
 *  @description: 1) draw the game board
 *  			  2) game area touch event, and color change animation
 *  ANY BUGS PLEASE CONNECT ME: alsoblack222#gmail.com
 *  
 */
package com.gamemodule.tacoyaki;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class GameView extends SurfaceView implements SurfaceHolder.Callback{
	
	private static final String TAG = "Game view-ta"; 
	private static final boolean DEBUG = true;

	private SurfaceHolder mHolder = null;
	private Context mContext = null;
	
	private Paint blackPaint = new Paint();
	private Paint whitePaint = new Paint(); // white paint not white ^_^
	private Paint clear = new Paint();
	
	private Game mGame = null;
	private int gameType;
	
	private int boardWidth;
	private int circleSize;
	private int sepSize; // black space between these circles 
	private int startX;
	private int startY;
	
	private Coordinate currCenter;
	
	private int newrad;
	private int red;
	

	public GameView(Context context) {
		this(context, null);
	}
	public GameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		init();
	}
	
	public void setGame(Game game){
		this.mGame = game;
		gameType = mGame.getGameType();
		requestLayout();
	}
	
	private void init(){
		mHolder = this.getHolder();
        mHolder.addCallback(this);
        // …Ë÷√Õ∏√˜
        mHolder.setFormat(PixelFormat.TRANSLUCENT);
        setZOrderOnTop(true);
        clear.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        blackPaint.setColor(Color.BLACK);
        blackPaint.setAntiAlias(true);
        whitePaint.setAntiAlias(true);
        whitePaint.setStyle(Style.FILL);
        blackPaint.setStyle(Style.FILL);
        setFocusable(true); 
        whitePaint.setColor(Color.rgb(236, 173, 158));
	}
	
	public void levelUP() {
		Canvas canvas = mHolder.lockCanvas();
		canvas.drawPaint(clear);
		mHolder.unlockCanvasAndPost(canvas);
		// IMPORTANT! clear again to void view overlap
		canvas = mHolder.lockCanvas();
		canvas.drawPaint(clear);
		mHolder.unlockCanvasAndPost(canvas);
		
		// change the value of start point
		startX -= circleSize / 2;
		startY -= circleSize / 2;
		mGame.levelUP();
		boardWidth = mGame.getWidth();
		try{Thread.sleep(500);}catch(InterruptedException e){e.printStackTrace();};
		drawBoard();
	}
	
	public void switchGame(int type){
		if(type == gameType) return;
		mGame.switchGame(type);
		
		// clear canvas
		Canvas canvas = mHolder.lockCanvas();
		canvas.drawPaint(clear);
		mHolder.unlockCanvasAndPost(canvas);
		// IMPORTANT !! see func @levelUP();
		canvas = mHolder.lockCanvas();
		canvas.drawPaint(clear);
		mHolder.unlockCanvasAndPost(canvas);
		
		int tmp = boardWidth;
		boardWidth = mGame.getWidth();
		gameType = mGame.getGameType();
		startX += (tmp - boardWidth) * circleSize / 2;
		startY += (tmp - boardWidth) * circleSize / 2;
	
		// change the white Paint's color
        if(gameType == GameConfig.TA_DEFAULT) {
        	whitePaint.setColor(Color.rgb(236, 173, 158));
        } else {
        	whitePaint.setColor(Color.rgb(25, 202, 173));
        }
        drawBoard();
	}
		
	private void drawBoard(){
		Canvas canvas = mHolder.lockCanvas();
		canvas.drawPaint(clear);
		drawInitCircles(canvas);
		mHolder.unlockCanvasAndPost(canvas);
	}
	
	private void drawInitCircles(Canvas canvas){
		int[][] map = mGame.getGameMap();
		Log.d(TAG, "boardwidth: "+boardWidth+"  map length: "+map.length);
		int radius = circleSize / 2;
		int size = circleSize + sepSize;// 
		for(int i = 0; i < boardWidth; i++){
			for(int j = 0; j < boardWidth; j++){
				if(map[i][j] == GameConfig.TYPE_BLACK)
					canvas.drawCircle(startX + i * size, startY + j * size, radius, blackPaint);
				else
					canvas.drawCircle(startX + i * size, startY + j * size, radius, whitePaint);
			}
		}
	}
	
	// draw growing circles then thread sleep some time
	// ATTENTION: THERE STILL SOME PROBLEMS USING THIS METHOD!!!
	private void updateCircles() {
		mGame.updateGameMap(currCenter.x, currCenter.y);
		newrad = 0; 
		red = circleSize / 5;
		while (newrad < circleSize/2){
            try  {  
	           	change();
	           	newrad += red;
	           	Thread.sleep(100);
            } catch (Exception e)  {  
                e.printStackTrace();  
            }  
        } // while  
		drawBoard();
	}
 
	// animation func, change neighbor circles' scale.
	private void change(){
		int[][] map = mGame.getGameMap();
		int radius = circleSize / 2;
		int size = circleSize + sepSize;
		Canvas canvas = mHolder.lockCanvas();
		for(int i = 0; i < boardWidth; i++){
			for(int j = 0; j < boardWidth; j++){
				if((i == currCenter.x && j == currCenter.y) || isNeighbor(i, j, currCenter)){
					if(map[i][j] == GameConfig.TYPE_BLACK)
						canvas.drawCircle(startX + i * size, startY + j * size, newrad, blackPaint);
					else
						canvas.drawCircle(startX + i * size, startY + j * size, newrad, whitePaint);
				} else{
					if(map[i][j] == GameConfig.TYPE_BLACK)
						canvas.drawCircle(startX + i * size, startY + j * size, radius, blackPaint);
					else
						canvas.drawCircle(startX + i * size, startY + j * size, radius, whitePaint);
				}// else
			}//for
		}// for
		mHolder.unlockCanvasAndPost(canvas);
	}
	
	// check  whether the circle is the center circle's neighbor
	// in the default game type, it locates at the center's right/left/above/below 
	// tacoyaki+ circles in it's bevel edge 
	private boolean isNeighbor(int x, int y, Coordinate center){
		if(center == null) return false;
		switch(gameType){
		case GameConfig.TA_DEFAULT:
			if(x == center.x - 1 && y == center.y) return true;
			if(x == center.x + 1 && y == center.y) return true;
			if(y == center.y -1 && x == center.x) return true;
			if(y == center.y + 1 && x == center.x) return true;
			return false;
		case GameConfig.TA_PLUS:
			if(Math.abs(x - center.x) == Math.abs(y - center.y)) return true;
			else return false;
		default:break;
		}
		return false;
	}
	
	private boolean checkPos(Coordinate pos){
		if(pos.x < 0 || pos.x >= boardWidth || pos.y < 0 || pos.y >= boardWidth)
			return false;
		return true;
	}
	
	
	@SuppressLint("ClickableViewAccessibility") @Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			// calculate the gameMap pos
			// NO DOT forget (+circleSize/2) coze the start point is the center point of the first circle
			if(x < (startX - circleSize / 2) || y < (startY - circleSize / 2)) break;
			int tmpx = (int) ((x - startX + circleSize/2) / (circleSize + sepSize));
			int tmpy = (int) ((y - startY + circleSize/2) / (circleSize + sepSize));
			if(tmpx < 0 || tmpx > boardWidth-1 || tmpy < 0 || tmpy > boardWidth-1) break;
			Coordinate pos = new Coordinate(tmpx, tmpy);
			Log.d(TAG, "touch x: "+tmpx+" Y: "+tmpy); 
			if(checkPos(pos))
				currCenter = pos;
				updateCircles();
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		default:
			break;
		}
		return true;
	}
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = View.MeasureSpec.getSize(widthMeasureSpec);
        if(mGame != null){
            if (width % mGame.getWidth() == 0){
                setMeasuredDimension(width, width);
            } else {
                width = width / mGame.getWidth() * mGame.getWidth();
                setMeasuredDimension(width, width);
            }
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
 

    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
            int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        
        if (DEBUG) Log.d(TAG, "left="+left+"  top="+top+" right="+right+" bottom="+bottom);
        if (mGame != null) {
            boardWidth = mGame.getWidth();
            circleSize = (int) (right - left) / (GameConfig.SCALE_LARGE + 2);
            Log.d(TAG, "ta circle size is: " + circleSize);
            sepSize = circleSize / 12;
            startX = (right - left - circleSize * boardWidth) / 2 + circleSize / 4;
            startY = (bottom - top - circleSize * boardWidth) / 2 + circleSize / 4;
        }
    }
		
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		drawBoard();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}
	public class Coordinate {
		public int x;
		public int y;
		public Coordinate(int x, int y){
			this.x = x;
			this.y = y;
		}
	}	
}
