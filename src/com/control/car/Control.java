package com.control.car;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import at.abraxas.amarino.Amarino;
import at.abraxas.amarino.AmarinoIntent;

import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

public class Control extends Activity implements OnClickListener, OnItemClickListener
		 {

	final static String BT_ADDRESS = "00:11:11:18:03:23";

	TextView climateIntTemperatureTextView,climateExtTemperatureTextView,
				heater1TextView,heater2TextView,currentLocation,gsm, tvDate, tvClock;
	
	ImageView image,imWeather;

	int Heater1CurrentTemp = 20;
	int Heater2CurrentTemp = 20;
	int Heater1State = 0;
	int Heater2State = 0;
	int ConnectionState;

	
	private GridView mAppGrid;
	private ArduinoReceiver arduinoReceiver = new ArduinoReceiver();
	
	TelephonyManager        Tel;
	MyPhoneStateListener    GSMListener;
	Button heater1TempUp;
	private ArrayList<Application> mApplications; 
	ApplicationsAdapter appAdapter;
	public class ApplicationsAdapter extends ArrayAdapter<Application> {
        private final Rect mOldBounds = new Rect();

        public ApplicationsAdapter(final Context context, final ArrayList<Application> apps) {
            super(context, 0, apps);
        }

        @Override
        public View getView(final int position, View convertView, final ViewGroup parent) {
            final Application info = mApplications.get(position);

            if (convertView == null) {
                final LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.application, parent, false);
            }

            Drawable icon = info.icon;

            if (!info.filtered) {
                int width = 100;
                int height = 100;

                final int iconWidth = icon.getIntrinsicWidth();
                final int iconHeight = icon.getIntrinsicHeight();

                if (icon instanceof PaintDrawable) {
                    final PaintDrawable painter = (PaintDrawable) icon;
                    painter.setIntrinsicWidth(width);
                    painter.setIntrinsicHeight(height);
                }

                if ((width > 0) && (height > 0) && ((width < iconWidth) || (height < iconHeight))) {
                    final float ratio = (float) iconWidth / iconHeight;

                    if (iconWidth > iconHeight) {
                        height = (int) (width / ratio);
                    } else if (iconHeight > iconWidth) {
                        width = (int) (height * ratio);
                    }

                    final Bitmap.Config c = icon.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                            : Bitmap.Config.RGB_565;
                    final Bitmap thumb = Bitmap.createBitmap(width, height, c);
                    final Canvas canvas = new Canvas(thumb);
                    canvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG, 0));

                    // Copy the old bounds to restore them later If we were to do oldBounds =
                    // icon.getBounds(), the call to setBounds() that follows would change the same
                    // instance and we would lose the old bounds
                    mOldBounds.set(icon.getBounds());
                    icon.setBounds(0, 0, width, height);
                    icon.draw(canvas);
                    icon.setBounds(mOldBounds);
                    icon = info.icon = new BitmapDrawable(thumb);
                    info.filtered = true;
                }
            }

            final TextView textView = (TextView) convertView.findViewById(R.id.label);
            textView.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
            textView.setText(info.title);

            return convertView;
        }
    }

    
    

    /**
     * Loads the list of installed applications in mApplications.
     */
   
   

	 
    PageIndicator mIndicator;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.startService( new Intent( this, GlobalService.class ) );
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.control);
		
		//mAppGrid = (GridView) findViewById(R.id.apps_grid);
		
		
		//уровень сигнала GSM
		GSMListener   = new MyPhoneStateListener();
		Tel       = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
		Tel.listen(GSMListener ,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		
		//скрываем статус-строку
		
		
		
		//TextView outHeader = (TextView)findViewById(R.id.textView1);
		
		//Typeface fontItal = Typeface.createFromAsset(getAssets(), "fonts/ital.ttf");
		//outHeader.setTypeface(fontItal);
	
		MyPagerAdapter adapter = new MyPagerAdapter();
        ViewPager myPager = (ViewPager) findViewById(R.id.pager);
		
        myPager.setAdapter(adapter);
        myPager.setCurrentItem(0);
        
        mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(myPager);
		
		
		
		gsm = (TextView)findViewById(R.id.gsmSignal);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		currentLocation = (TextView) findViewById(R.id.location);
		
		HideStatusBar();
		
		
		
		 
		
		
		
		
       
        

        
	   
	   //	climateIntTemperatureTextView = (TextView) findViewById(R.id.intTemp); // View
																				// температура
																				// в
																				// салоне
		climateExtTemperatureTextView = (TextView) findViewById(R.id.extTemp);// View
																				// температура
																				// на
																				// улице
		//heater1TextView = (TextView) findViewById(R.id.heater1Temp);// View
																	// температура
																	// подогрева
																	// пассажира
		//heater2TextView = (TextView) findViewById(R.id.heater2Temp);// View
																	// температура
																	// подогрева
																	// водителя

		//heater1TextView.setText("" + Heater1CurrentTemp + (char) 0x00B0);
		//heater2TextView.setText("" + Heater2CurrentTemp + (char) 0x00B0);
		//Amarino.sendDataToArduino(Control.this, BT_ADDRESS, 'J',
		//		Heater1CurrentTemp);
		//Amarino.sendDataToArduino(Control.this, BT_ADDRESS, 'I',
		//		Heater2CurrentTemp);
		
		
		
		
		
		
		
		
		currentLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				doNavitelAction();
			}
		});
		
		/*final Button heater2TempUp = (Button) findViewById(R.id.heater2TempUp);
		heater2TempUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Heater2CurrentTemp+=5;
				heater2TextView
						.setText("" + Heater2CurrentTemp + (char) 0x00B0);
				if (Heater2State == 1) {
					Amarino.sendDataToArduino(Control.this, BT_ADDRESS, 'I',
							Heater2CurrentTemp);
				}
			}
		}); */

		/*final Button heater2TempDown = (Button) findViewById(R.id.heater2TempDown);
		heater2TempDown.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Heater2CurrentTemp-=5;
				if (Heater2CurrentTemp <= 20) {
					Heater2CurrentTemp = 20;
				}
				heater2TextView
						.setText("" + Heater2CurrentTemp + (char) 0x00B0);
				if (Heater2State == 1) {
					Amarino.sendDataToArduino(Control.this, BT_ADDRESS, 'I',
							Heater2CurrentTemp);
				}
			}
		});
*/
		/*	final ToggleButton HeaterToggleRH = (ToggleButton) findViewById(R.id.toggleButton2);
		HeaterToggleRH.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (HeaterToggleRH.isChecked()) {
					Amarino.sendDataToArduino(Control.this, BT_ADDRESS, 'G',
							Heater2CurrentTemp);
					Heater2State = 1;

				} else {
					Amarino.sendDataToArduino(Control.this, BT_ADDRESS, 'G', 0);
					Heater2State = 0;
				}
			}
		});*/
		// Управление температурой подогрева пассажира
		/*	heater1TempUp = (Button) findViewById(R.id.heater1TempUp);
		heater1TempUp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				Heater1CurrentTemp+=5;
				heater1TextView
						.setText("" + Heater1CurrentTemp + (char) 0x00B0);
				if (Heater1State == 1) {
					Amarino.sendDataToArduino(Control.this, BT_ADDRESS, 'J',
							Heater1CurrentTemp);
				}
			}
		});*/

	/*	final Button heater1TempDown = (Button) findViewById(R.id.heater1TempDown);
		heater1TempDown.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Heater1CurrentTemp-=5;
				if (Heater1CurrentTemp <= 20) {
					Heater1CurrentTemp = 20;
				}
				heater1TextView
						.setText("" + Heater1CurrentTemp + (char) 0x00B0);
				if (Heater1State == 1) {
					Amarino.sendDataToArduino(Control.this, BT_ADDRESS, 'J',
							Heater1CurrentTemp);
				}
			}
		});
		final ToggleButton HeaterToggleLH = (ToggleButton) findViewById(R.id.toggleButton1);
		HeaterToggleLH.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (HeaterToggleLH.isChecked()) {
					Amarino.sendDataToArduino(Control.this, BT_ADDRESS, 'H',
							Heater1CurrentTemp);
					Heater1State = 1;
				} else {
					Amarino.sendDataToArduino(Control.this, BT_ADDRESS, 'H', 0);
					Heater1State = 0;
				}
			}
		});
		*/
		
		
		
		LocationManager locationManager;
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		String provider = locationManager.getBestProvider(criteria, true);

		Location location = locationManager.getLastKnownLocation(provider);
		updateWithNewLocation(location);

		locationManager.requestLocationUpdates(provider, 2000, 10,
				locationListener);
		
		
	}
	
	
	

	private final LocationListener locationListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null);

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}
	};

	private void updateWithNewLocation(Location location) {
		

		String addressString = "Нет данных";

		if (location != null) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();

			Geocoder gc = new Geocoder(this, Locale.getDefault());
			try {
				List<Address> addresses = gc.getFromLocation(lat, lng, 1);
				StringBuilder sb = new StringBuilder();
				if (addresses.size() > 0) {
					Address address = addresses.get(0);

					// for (int i = 0; i < address.getMaxAddressLineIndex();
					// i++)
					sb.append(address.getAddressLine(1)).append(", ");
					sb.append(address.getAddressLine(0));

				}
				addressString = sb.toString();
			} catch (IOException e) {
			}
		}
		currentLocation.setText(addressString);
	}

	
	
	
	public class ArduinoReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String data = null;
			final int dataType = intent.getIntExtra(
					AmarinoIntent.EXTRA_DATA_TYPE, -1);
			if (dataType == AmarinoIntent.STRING_EXTRA) {
				data = intent.getStringExtra(AmarinoIntent.EXTRA_DATA);

				if (data != null) {					//получаем данные с Arduino
					String[] value = data.split(":");
					
					String command = value[0];
					
					if (command.equals("climate")){ //если температурные датчики
				    
						String intTemp = value[1];// температура внутри салона
						String extTemp = value[2];// температура на улице extTemp

						
						climateExtTemperatureTextView.setText(extTemp.toString()
							+ (char) 0x00B0+ "C");
					
					}
					
					if (command.equals("telemetria")){// если двери
					    byte doorState = (byte) (Integer.parseInt(value[1]));
					//    image = (ImageView) findViewById(R.id.imageView1);
					   
					//	String sFRdoor,sFLdoor,sRRdoor,sRLdoor ="";
						if((doorState & 1)>0){
					//		image.setVisibility(View.VISIBLE);
						
						} else {
					//		image.setVisibility(View.INVISIBLE);
						}
						
						if((doorState & 2)>0){
							
							
						} else {
							
						}
						
						if((doorState & 4)>0){
							
						
						} else {
							
						}
						
						if((doorState & 8)>0){
							
							
						} else {
							
						}
						
						
						
					
						
						
						
					}
				}
					
				}

			}
		}
	
	
	
	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(arduinoReceiver, new IntentFilter(
				AmarinoIntent.ACTION_RECEIVED));
		Amarino.connect(this, BT_ADDRESS);
	}

	@Override
	protected void onStop() {
		super.onStop();
	//	Amarino.disconnect(this, BT_ADDRESS);
	//	unregisterReceiver(arduinoReceiver);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		View v = findViewById(R.id.mainLayout);
		v.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		
	 Tel.listen(GSMListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	
	}
	@Override
	   protected void onPause()
	    {
	      super.onPause();
	     Tel.listen(GSMListener, PhoneStateListener.LISTEN_NONE);
	   }

	
	
	

	

	
	 @Override
	    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
	        final Application app = (Application) parent.getItemAtPosition(position);
	        startActivity(app.intent);
	    }
	
	 public void doNavitelAction() {
	    	Intent intent = new Intent(Intent.ACTION_MAIN);
	    	intent.setComponent(new ComponentName("com.cdcom","com.cdcom.naviapps.progorod"));
	    	
	    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	startActivity(intent);
	    }
	 private class MyPhoneStateListener extends PhoneStateListener
	    {
	      /* Get the Signal strength from the provider, each time there is an update */
	      @Override
	      public void onSignalStrengthsChanged(SignalStrength signalStrength)
	      {
	         super.onSignalStrengthsChanged(signalStrength);
	         
	         gsm.setText(String.valueOf(signalStrength.getGsmSignalStrength()));
	      }

	    }
	 private class MyPagerAdapter extends PagerAdapter {

         public int getCount() {
                 return 2;
         }

         public Object instantiateItem(View collection, int position) {
        	
        	 LayoutInflater inflater = (LayoutInflater) Control.this
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                 
                 

                 int resId = 0;
                 switch (position) {
                 case 0:
                         resId = R.layout.main;
                         break;
                 case 1:
                         resId = R.layout.apps;
                         View v = inflater.inflate(resId, null, false);
                         mAppGrid=(GridView)v.findViewById(R.id.apps_grid);
                         ((ViewPager) collection).addView(v,0);
                         
                        

                         final PackageManager manager = getPackageManager();

                         final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                         mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

                         final List<ResolveInfo> apps = manager.queryIntentActivities(mainIntent, 0);
                         Collections.sort(apps, new ResolveInfo.DisplayNameComparator(manager));

                         if (apps != null) {
                             final int count = apps.size();

                             if (mApplications == null) {
                                 mApplications = new ArrayList<Application>(count);
                             }
                             mApplications.clear();

                             for (int i = 0; i < count; i++) {
                                 final Application application = new Application();
                                 final ResolveInfo info = apps.get(i);

                                 application.title = info.loadLabel(manager);
                                 application.setActivity(new ComponentName(info.activityInfo.applicationInfo.packageName,
                                         info.activityInfo.name), Intent.FLAG_ACTIVITY_NEW_TASK
                                         | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                                 application.icon = info.activityInfo.loadIcon(manager);

                                 mApplications.add(application);
                             }
                         }
                         mAppGrid.setAdapter(Control.this.appAdapter);
                         mAppGrid.setSelection(0);
                         mAppGrid.setVisibility(View.VISIBLE);
                         
                         break;
                
                 }

                 View view = inflater.inflate(resId, null);
                 
                
                 ((ViewPager) collection).addView(view, 0);

                 return view;
         }

         @Override
         public void destroyItem(View arg0, int arg1, Object arg2) {
                 ((ViewPager) arg0).removeView((View) arg2);

         }

         @Override
         public void finishUpdate(View arg0) {
                 // TODO Auto-generated method stub

         }

         @Override
         public boolean isViewFromObject(View arg0, Object arg1) {
                 return arg0 == ((View) arg1);

         }

         @Override
         public void restoreState(Parcelable arg0, ClassLoader arg1) {
                 // TODO Auto-generated method stub

         }

         @Override
         public Parcelable saveState() {
                 // TODO Auto-generated method stub
                 return null;
         }

         @Override
         public void startUpdate(View arg0) {
                 // TODO Auto-generated method stub

         }

 }


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	};
	public void HideStatusBar(){
		View v = findViewById(R.id.mainLayout);
		v.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}
	   
	 
}