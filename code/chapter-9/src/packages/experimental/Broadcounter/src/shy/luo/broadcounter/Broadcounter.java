package shy.luo.broadcounter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Broadcounter extends Activity implements OnClickListener {
	private final static String LOG_TAG = "shy.luo.broadcounter.Broadcounter";
	   
	private Button startButton = null;
	private Button stopButton = null;
	private TextView counterText = null;
	
	private ICounterService counterService = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        startButton = (Button)findViewById(R.id.button_start);
        stopButton = (Button)findViewById(R.id.button_stop);
        counterText = (TextView)findViewById(R.id.textview_counter);
        
        startButton.setOnClickListener(this);
        stopButton.setOnClickListener(this);
        
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        
        Intent bindIntent = new Intent(Broadcounter.this, CounterService.class);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        Log.i(LOG_TAG, "Broadcounter Activity Created.");
    }
    
    @Override 
    public void onResume() {
    	super.onResume();
    	
    	IntentFilter counterActionFilter = new IntentFilter(CounterService.BROADCAST_COUNTER_ACTION);
		registerReceiver(counterActionReceiver, counterActionFilter);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	
    	unregisterReceiver(counterActionReceiver);
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	unbindService(serviceConnection);
    }
    
    @Override
    public void onClick(View v) {
    	if(v.equals(startButton)) {
    		if(counterService != null) {
    			counterService.startCounter(0);
    			
	    		startButton.setEnabled(false);
	            stopButton.setEnabled(true);
    		}
    	} else if(v.equals(stopButton)) {
    		if(counterService != null) {
    			counterService.stopCounter();
    			
    			startButton.setEnabled(true);
    			stopButton.setEnabled(false);
    		}
    	}
    }
    
    private BroadcastReceiver counterActionReceiver = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent) {
			int counter = intent.getIntExtra(CounterService.COUNTER_VALUE, 0);
			String text = String.valueOf(counter);
			counterText.setText(text);
		}
	};
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		counterService = ((CounterService.CounterBinder)service).getService();
    		
    		Log.i(LOG_TAG, "Counter Service Connected");
    	}
    	public void onServiceDisconnected(ComponentName className) {
    		counterService = null;
    		Log.i(LOG_TAG, "Counter Service Disconnected");
    	}
    };
}
