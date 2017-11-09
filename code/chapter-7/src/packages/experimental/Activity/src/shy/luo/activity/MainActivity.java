package shy.luo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity  implements OnClickListener {
	private final static String LOG_TAG = "shy.luo.activity.MainActivity";
   
	private Button startInProcessButton = null;
	private Button startInNewProcessButton = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        startInProcessButton = (Button)findViewById(R.id.button_start_in_process);
	startInNewProcessButton = (Button)findViewById(R.id.button_start_in_new_process);
	
	startInProcessButton.setOnClickListener(this);
	startInNewProcessButton.setOnClickListener(this);
        
        Log.i(LOG_TAG, "Main Activity Created.");
    }
    
    @Override
    public void onClick(View v) {
    	if(v.equals(startInProcessButton)) {
		Intent intent = new Intent("shy.luo.activity.subactivity.in.process");
    		startActivity(intent);
    	} else if(v.equals(startInNewProcessButton)) {
		Intent intent = new Intent("shy.luo.activity.subactivity.in.new.process");
		startActivity(intent);
	}
    }
}
