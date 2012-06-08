package com.control.car;



import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;



public class GlobalService extends Service{
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();
	final static String BT_ADDRESS = "00:11:11:18:03:23";
	
	@Override
    public void onCreate() {
        super.onCreate();
        connectToArduino();
    }
	
	
	@Override
    public void onDestroy() {
    	
    	super.onDestroy();
    	//disconnectArduino();
	}	
	
	
	@Override
	
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	public class ArduinoReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String data = null;
			
			final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
			
			if (dataType == AmarinoIntent.STRING_EXTRA){
				data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
				
				if (data != null){
					String[] value = data.split(":");
					
					String command = value[0];
				
					if (command.equals("cmd")){// обработка команды
					    byte cmd = (byte) (Integer.parseInt(value[1]));
					    
					    if(cmd ==1){
					    	doNavitelAction();
						
						}
					    if(cmd ==0){
					    	doHomeAction();
						
						}
					}  
					
				}
			}
		}
	}
	private void connectToArduino() {
		registerReceiver(arduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_RECEIVED));
    	Amarino.connect(this, BT_ADDRESS);
    }
	private void disconnectArduino() {
		Amarino.disconnect(this, BT_ADDRESS);
		unregisterReceiver(arduinoReceiver);
	}
	public void doNavitelAction() {
    	Intent intent = new Intent(Intent.ACTION_MAIN);
    	intent.setComponent(new ComponentName("com.navitel","com.navitel.Navitel"));
    	
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(intent);
    }
	public void doHomeAction() {
    	Intent intent = new Intent(Intent.ACTION_MAIN);
    	intent.setComponent(new ComponentName("com.control.car","com.control.car.Control"));
    	
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	startActivity(intent);
    }
	
}
