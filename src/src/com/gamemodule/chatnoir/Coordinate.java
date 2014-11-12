/**
 * @file Coordiante.java
 * @author Also wu
 * 
 */
package com.gamemodule.chatnoir;


public class Coordinate {
	public int x;
	public int y;
	public Coordinate parent = null;
	
	public Coordinate(){}
	
	public Coordinate(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void set(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	
	public Coordinate getParent(){
		return parent;
	}
	
	public void setParent(Coordinate p){
		parent = p;
	}

}
