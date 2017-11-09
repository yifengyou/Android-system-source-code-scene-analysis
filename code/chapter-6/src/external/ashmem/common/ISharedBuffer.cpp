#define LOG_TAG "ISharedBuffer"

#include <utils/Log.h>
#include <binder/MemoryBase.h>

#include "ISharedBuffer.h"

using namespace android;

enum 
{
	GET_BUFFER = IBinder::FIRST_CALL_TRANSACTION
};

class BpSharedBuffer: public BpInterface<ISharedBuffer>
{
public:
	BpSharedBuffer(const sp<IBinder>& impl) 
		: BpInterface<ISharedBuffer>(impl)
	{

	}

public:
	sp<IMemory> getBuffer()
	{
		Parcel data;
		data.writeInterfaceToken(ISharedBuffer::getInterfaceDescriptor());
		
		Parcel reply;
		remote()->transact(GET_BUFFER, data, &reply);

		sp<IMemory> buffer = interface_cast<IMemory>(reply.readStrongBinder());
		
		return buffer;
	}
};

IMPLEMENT_META_INTERFACE(SharedBuffer, "shy.luo.ISharedBuffer");

status_t BnSharedBuffer::onTransact(uint32_t code, const Parcel& data, Parcel* reply, uint32_t flags)
{
	switch(code)
	{
		case GET_BUFFER:
		{
			CHECK_INTERFACE(ISharedBuffer, data, reply);
			
			sp<IMemory> buffer = getBuffer();
			if(buffer != NULL)
			{
				reply->writeStrongBinder(buffer->asBinder());
			}
			
			return NO_ERROR;
		}
		default:
		{
			return BBinder::onTransact(code, data, reply, flags);
		}
	}
}
