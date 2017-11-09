#define LOG_TAG "SharedBufferClient"

#include <utils/Log.h>
#include <binder/MemoryBase.h>
#include <binder/IServiceManager.h>

#include "../common/ISharedBuffer.h"

int main()
{
	sp<IBinder> binder = defaultServiceManager()->getService(String16(SHARED_BUFFER_SERVICE));
	if(binder == NULL)
	{
		printf("Failed to get service: %s.\n", SHARED_BUFFER_SERVICE);
		return -1;
	}

	sp<ISharedBuffer> service = ISharedBuffer::asInterface(binder);
	if(service == NULL)
	{
		return -2;
	}

	sp<IMemory> buffer = service->getBuffer();
	if(buffer == NULL)
	{
		return -3;
	}

	int32_t* data = (int32_t*)buffer->pointer();
	if(data == NULL)
	{
		return -4;
	}

	printf("The value of the shared buffer is %d.\n", *data);		

	*data = *data + 1;
	
	printf("Add value 1 to the shared buffer.\n");
	
	return 0;
}
