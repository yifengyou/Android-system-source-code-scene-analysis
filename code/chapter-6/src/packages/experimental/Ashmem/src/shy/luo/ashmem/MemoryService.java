package shy.luo.ashmem;

import java.io.FileDescriptor;
import java.io.IOException;

import android.os.Parcel;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class MemoryService extends IMemoryService.Stub {
	private final static String LOG_TAG = "shy.luo.ashmem.MemoryService";
	private MemoryFile file = null;
	
	public MemoryService() {
		try {
                        file = new MemoryFile("Ashmem", 4);
                        setValue(0);
                }
                catch(IOException ex) {
                        Log.i(LOG_TAG, "Failed to create memory file.");
                        ex.printStackTrace();
                }
	}

	public ParcelFileDescriptor getFileDescriptor() {
		Log.i(LOG_TAG, "Get File Descriptor.");

		ParcelFileDescriptor pfd = null;

		try {
			pfd = file.getParcelFileDescriptor();
		} catch(IOException ex) {
			Log.i(LOG_TAG, "Failed to get file descriptor.");
			ex.printStackTrace();
		}

		return pfd;
	}
	
	public void setValue(int val) {
		if(file == null) {
			return;
		}

		byte[] buffer = new byte[4];   
		buffer[0] = (byte)((val >>> 24) & 0xFF);
		buffer[1] = (byte)((val >>> 16) & 0xFF);
		buffer[2] = (byte)((val >>> 8) & 0xFF); 
		buffer[3] = (byte)(val & 0xFF);
		
		try {
			file.writeBytes(buffer, 0, 0, 4);
			Log.i(LOG_TAG, "Set value " + val + " to memory file. ");
		}
		catch(IOException ex) {
			Log.i(LOG_TAG, "Failed to write bytes to memory file.");
			ex.printStackTrace();
		}
	}
}
