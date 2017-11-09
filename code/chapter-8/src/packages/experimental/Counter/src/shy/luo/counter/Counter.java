package shy.luo.counter;

import android.app.Activity;
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

public class Counter extends Activity implements OnClickListener, ICounterCallback {
    private final static String LOG_TAG = "shy.luo.counter.Counter";
	   
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
        
        Intent bindIntent = new Intent(Counter.this, CounterService.class);
        bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        
        Log.i(LOG_TAG, "Counter Activity Created.");
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
                counterService.startCounter(0, this);
    			
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

    @Override
    public void count(int val) {
        String text = String.valueOf(val);
        counterText.setText(text);
    }        
    
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
