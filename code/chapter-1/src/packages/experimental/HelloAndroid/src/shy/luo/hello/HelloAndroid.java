package shy.luo.hello;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class HelloAndroid extends Activity {
	private final static String LOG_TAG = "shy.luo.hello.HelloAndroid";
   
	@Override
    	public void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	setContentView(R.layout.main);
        
        	Log.i(LOG_TAG, "HelloAndroid Activity Created.");
    	}
}
