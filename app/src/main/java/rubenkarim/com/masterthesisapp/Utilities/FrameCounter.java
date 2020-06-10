package rubenkarim.com.masterthesisapp.Utilities;

import java.io.Closeable;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FrameCounter {

    private AtomicInteger counter = new AtomicInteger(0);
    private AtomicInteger FPS = new AtomicInteger(0);

    public FrameCounter(){
        timeCounter();
    }

    public void tick() {
        counter.getAndIncrement();
    }

    private void timeCounter() {
        int delay = 1000; // delay for 1 sec.
        int period = 1000; // repeat every 30 sec.
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                FPS.set(counter.get());
                counter.set(0);
            }
        }, delay, period);


    }

    public int getFPS() {
        return FPS.get();
    }

}
