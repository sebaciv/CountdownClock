import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;

/**
 * Class containing frame which display clock
 * Created by Sebastian on 2015-10-03.
 */
public class ClockUI {
    private final JFrame settingsFrame;
    private final JFrame clockFrame;
    private final Clip beepClip;
    private final JLabel timeLabel;
    private final int borderSize = 10;
    private CClock cClock;

    public ClockUI(JFrame settingsFrame, JFrame clockFrame, Clip beepClip) {
        this.settingsFrame = settingsFrame;
        this.clockFrame = clockFrame;
        this.beepClip = beepClip;
        timeLabel = createTimeDisplay();

        clockFrame.setLayout(new BorderLayout());
        clockFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        clockFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        clockFrame.setUndecorated(true);
        clockFrame.add(timeLabel, BorderLayout.CENTER);

        addButtonPanel();

        clockFrame.pack();
    }

    public void startClock(TimeDuration interval, TimeDuration delay) {
        cClock = new CClock(interval, delay, timeLabel, beepClip);
        cClock.startClock();
    }

    private JLabel createTimeDisplay() {
        JLabel timeDisplay = new JLabel("00:00");
        timeDisplay.setFont(new Font("Arial", Font.PLAIN, calculateFontSize(timeDisplay)));
        timeDisplay.setHorizontalAlignment(JLabel.CENTER);
        timeDisplay.setBorder(BorderFactory.createEmptyBorder(borderSize, borderSize, borderSize, borderSize));
        return timeDisplay;
    }

    private int calculateFontSize(JLabel timeDisplay) {
        Font font = new Font("Arial", Font.PLAIN, 14);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int textWidth = timeDisplay.getFontMetrics(font).stringWidth("000:00");
        double displayWidth = screenSize.getWidth() - 2*borderSize;
        double widthRatio = displayWidth / (double)textWidth;
        int fontSize = (int)(font.getSize() * widthRatio);
        int displayHeight = (int) screenSize.getHeight();
        return Math.min(fontSize, displayHeight - 4*borderSize);
    }

    private void addButtonPanel() {
        JPanel buttons = new JPanel();
        buttons.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.add(createBackButton());
        clockFrame.add(buttons, BorderLayout.SOUTH);
    }

    private JButton createBackButton() {
        JButton backToSettings = new JButton("Reset");
        backToSettings.setFont(new Font("Arial", Font.PLAIN, 26));
        backToSettings.setFocusable(false);
        backToSettings.addActionListener(e -> {
            clockFrame.setVisible(false);
            settingsFrame.setVisible(true);
            cClock.stopClock();
        });
        return backToSettings;
    }
}
