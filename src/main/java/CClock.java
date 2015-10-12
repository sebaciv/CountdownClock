import javax.sound.sampled.*;
import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Class manages clock logic
 * Created by Sebastian on 2015-10-03.
 */
public class CClock {
    private final JLabel time;
    private final ScheduledExecutorService executorCountUp = Executors.newScheduledThreadPool(1);
    private final long delay;
    private final long intervalSec;
    private Timer timerCountDown;
    private Timer timerCountUp;
    private long currentSecond;
    private long currentMinute;
    private Instant startCount;

    public CClock(TimeDuration interval, TimeDuration startDelay, JLabel time) {
        this.time = time;
        currentMinute = startDelay.getMinutes();
        currentSecond = startDelay.getSeconds();
        intervalSec = interval.getDurationInSecond();
        delay = startDelay.getDurationInSecond();
    }

    public void startClock() {
        startCount = Instant.now();
        countDown();
        // schedule stop counting down
        executorCountUp.schedule(timerCountDown::stop, 1000*delay, TimeUnit.MILLISECONDS);
        playSound();
        countUp();
    }

    public void stopClock() {
        timerCountDown.stop();
        executorCountUp.shutdown();
        timerCountUp.stop();
    }

    private void resetCountDown() {
        Instant now = Instant.now();
        currentMinute = (delay + startCount.getEpochSecond() - now.getEpochSecond())/60;
        currentSecond = 59;
    }

    private void resetCountUp() {
        Instant now = Instant.now();
        currentMinute = (delay + now.getEpochSecond() - startCount.getEpochSecond())/60;
        currentSecond = 0;
    }

    private void playSound() {
        final Clip clip = getClip();
        assert clip != null;
        long clipLength = clip.getMicrosecondLength()/1000;
        executorCountUp.scheduleAtFixedRate(() -> {
            try {
                clip.loop(0);
                clip.setFramePosition(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000*delay - clipLength, 1000*intervalSec, TimeUnit.MILLISECONDS); // to adjust
    }

    private Clip getClip() {
        Clip clip = null;
        try {
            URL url = this.getClass().getClassLoader().getResource("beep.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch (UnsupportedAudioFileException |
                LineUnavailableException |
                IOException e) {
            e.printStackTrace();
        }
        return clip;
    }

    private void countDown() {
        timerCountDown = new Timer(1000, e -> {
            if (currentSecond < 0) {
                resetCountDown();
            }
            time.setText("-" + String.format("%02d:%02d", currentMinute, currentSecond));
            currentSecond--;
        });
        timerCountDown.setInitialDelay(0);
        timerCountDown.start();
    }

    private void countUp() {
        timerCountUp = new Timer(1000, e -> {
            if (currentSecond == 60 || currentSecond < 0) {
                resetCountUp();
            }
            time.setText(String.format("%02d:%02d", currentMinute, currentSecond));
            currentSecond++;
        });
        timerCountUp.setInitialDelay((int)(1000*delay));
        timerCountUp.start();
    }
}
