package com.gamemodule.chatnoir;

public class Player {
	
	private int type; // 1->player; 2->cat;
	private String username;
	private int winNr = 0;
	private int gameCount = 0;
	
	public Player(String name, int type){
		this.username = name;
		this.type = type;
	} 
	
	public String getPlayerName(){
		return username;
	}
	
	public int getType(){
		return type;
	}
	
	public int getWinNr(){
		return winNr;
	}
	
	public int getGameCount(){
		return gameCount;
	}
	
	public void win(){
		winNr++;
	}
	
	public void incCount(){
		gameCount++;
	}
}
