package com.gamemodule;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button tacoyaki = (Button) findViewById(R.id.tacoyaki);
		Button chatNoir = (Button) findViewById(R.id.chat_noir);
		//gameAbout = (Button) findViewById(R.id.game_about);
		
		tacoyaki.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				startActivity(new Intent(MainActivity.this, TacoyakiActivity.class));
			}
		});
		
		chatNoir.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				startActivity(new Intent(MainActivity.this, ChatNoirActivity.class));
			}
		});
		
	
		/*
		gameAbout.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				// TODO
			}
		});
		*/

	}// func
		

}// class ends
