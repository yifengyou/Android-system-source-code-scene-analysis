package shy.luo.counter;

public interface ICounterService {
	public void startCounter(int initVal, ICounterCallback callback);
	public void stopCounter();
}
