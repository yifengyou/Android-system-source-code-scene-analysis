package shy.luo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SubActivityInNewProcess extends Activity implements OnClickListener {
	private final static String LOG_TAG = "shy.luo.activity.SubActivityInNewProcess";
	   
	private Button finishButton = null;
	
    	@Override
    	public void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	setContentView(R.layout.sub);
        
       	 	finishButton = (Button)findViewById(R.id.button_finish);
		finishButton.setOnClickListener(this);

        	Log.i(LOG_TAG, "Sub Activity In New Process Created.");
    	}
    
    @Override
    public void onClick(View v) {
    	if(v.equals(finishButton)) {
    		finish();
    	}
    }
}
