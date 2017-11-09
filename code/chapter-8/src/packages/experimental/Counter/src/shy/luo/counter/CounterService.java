package shy.luo.counter;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class CounterService extends Service implements ICounterService {
    private final static String LOG_TAG = "shy.luo.counter.CounterService";
	
    private boolean stop = false;
    private ICounterCallback counterCallback = null;

    private final IBinder binder = new CounterBinder();
	
    public class CounterBinder extends Binder {
        public CounterService getService() {
            return CounterService.this;
        }
    }
	
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
	
    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(LOG_TAG, "Counter Service Created.");
    }
	
    public void startCounter(int initVal, ICounterCallback callback) {
        counterCallback = callback;

        AsyncTask<Integer, Integer, Integer> task = new AsyncTask<Integer, Integer, Integer>() {	
            @Override
            protected Integer doInBackground(Integer... vals) {
                Integer initCounter = vals[0];
				
                stop = false;
                while(!stop) {
                    publishProgress(initCounter);
					
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
					
                    initCounter++;
                }
				
                return initCounter;
            }
			
            @Override 
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
				
                int val = values[0];
                counterCallback.count(val);
            }
			
            @Override
            protected void onPostExecute(Integer val) {
                counterCallback.count(val);
            }
		
    	};
			
    	task.execute(initVal);	
    }

    public void stopCounter() {
	stop = true;
    }
}
