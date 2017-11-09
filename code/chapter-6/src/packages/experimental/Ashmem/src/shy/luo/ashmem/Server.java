package shy.luo.ashmem;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.os.ServiceManager;

public class Server extends Service {
    private final static String LOG_TAG = "shy.luo.ashmem.Server";

    private MemoryService memoryService = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
	Log.i(LOG_TAG, "Create Memory Service...");

	memoryService = new MemoryService();

        try {
            ServiceManager.addService("AnonymousSharedMemory", memoryService);
            Log.i(LOG_TAG, "Succeed to add memory service.");
        } catch (RuntimeException ex) {
            Log.i(LOG_TAG, "Failed to add Memory Service.");
            ex.printStackTrace();
        }

	//int div = 0;
	//int sum = 10;

	//int ave = sum / div;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.i(LOG_TAG, "Start Memory Service.");
    }

    @Override
    public void onDestroy() {
	Log.i(LOG_TAG, "Destroy Memory Service.");
    }
}
