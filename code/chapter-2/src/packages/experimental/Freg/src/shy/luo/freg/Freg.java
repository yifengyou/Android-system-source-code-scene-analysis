package shy.luo.freg;

import android.app.Activity;
import android.os.ServiceManager;
import android.os.Bundle;
import android.os.IFregService;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Freg extends Activity implements OnClickListener {
	private final static String LOG_TAG = "shy.luo.freg.FregActivity";
	
	private IFregService fregService = null;

	private EditText valueText = null;
	private Button readButton = null;
	private Button writeButton = null;
	private Button clearButton = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

	fregService = IFregService.Stub.asInterface(
		ServiceManager.getService("freg"));
        
        valueText = (EditText)findViewById(R.id.edit_value);
        readButton = (Button)findViewById(R.id.button_read);
        writeButton = (Button)findViewById(R.id.button_write);
        clearButton = (Button)findViewById(R.id.button_clear);

	readButton.setOnClickListener(this);
	writeButton.setOnClickListener(this);
	clearButton.setOnClickListener(this);
        
        Log.i(LOG_TAG, "Freg Activity Created");
    }
    
    @Override
    public void onClick(View v) {
    	if(v.equals(readButton)) {
		try {
    			int val = fregService.getVal();
    			String text = String.valueOf(val);
    			valueText.setText(text);
		} catch (RemoteException e) {
			Log.e(LOG_TAG, "Remote Exception while reading value from freg service.");
		}		
    	}
    	else if(v.equals(writeButton)) {
		try {
    			String text = valueText.getText().toString();
    			int val = Integer.parseInt(text);
			fregService.setVal(val);
		} catch (RemoteException e) {
			Log.e(LOG_TAG, "Remote Exception while writing value to freg service.");
		}
    	}
    	else if(v.equals(clearButton)) {
    		String text = "";
    		valueText.setText(text);
    	}
    }
}
