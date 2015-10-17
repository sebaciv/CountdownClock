import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;

/**
 * Main frame
 * Created by Sebastian on 2015-10-03.
 */
public class Main {

    private final JFrame settingsFrame;
    private final JFrame clockFrame;
    private final Clip beepClip;
    private final ClockUI clockUI;
    private JFormattedTextField delayField;
    private JFormattedTextField intervalField;
    private long beepLength;

    private Main() {
        settingsFrame = new JFrame("ClockS");
        clockFrame = new JFrame("Clock");
        beepClip = getClip();
        clockUI = new ClockUI(settingsFrame, clockFrame, beepClip);

        settingsFrame.getContentPane().setLayout(new BoxLayout(settingsFrame.getContentPane(),BoxLayout.PAGE_AXIS));

        settingsFrame.setJMenuBar(createMenuBar());

        settingsFrame.getContentPane().add(createBorderPanel());

        settingsFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        settingsFrame.pack();
        settingsFrame.setResizable(false);
        settingsFrame.setLocationByPlatform(true);
        settingsFrame.setVisible(true);
    }

    private Clip getClip() {
        Clip clip = null;
        try {
            URL url = this.getClass().getClassLoader().getResource("beep2.wav");
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
            clip = AudioSystem.getClip();
            clip.open(audioIn);
        } catch (UnsupportedAudioFileException |
                LineUnavailableException |
                IOException e) {
            e.printStackTrace();
        }
        assert clip != null;
        beepLength = getBeepLength();
        assert beepLength < 59;
        return clip;
    }

    private long getBeepLength() {
        return beepClip.getMicrosecondLength()/1000000 + 1;
    }

    private JPanel createBorderPanel() {
        JPanel borderPanel = new JPanel();
        borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.PAGE_AXIS));
        borderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        borderPanel.add(createSettingsPanel());
        // add appearance panel to do
        borderPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        borderPanel.add(createButtonPanel());
        return borderPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(createStartButton());
        return buttons;
    }

    private JButton createStartButton() {
        JButton startButton = new JButton("Start");
        startButton.setFont(new Font("Arial", Font.PLAIN, 14));
        startButton.addActionListener(new StartButtonListener());
        return startButton;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu aboutMenu = new JMenu("About");
        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> JOptionPane.showMessageDialog(settingsFrame,
                "CountdownClock\nAuthor: Sebastian Haracz\nVersion: 1.1",
                "About",
                JOptionPane.PLAIN_MESSAGE));
        aboutMenu.add(about);
        menuBar.add(aboutMenu);
        return menuBar;
    }

    private JPanel createSettingsPanel() {
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(0, 2, 10, 10));
        settingsPanel.setBorder(getCompoundBorder("Clock settings"));

        settingsPanel.add(getDescriptionLabel("Interval"));
        intervalField = getInputField();
        settingsPanel.add(intervalField);

        settingsPanel.add(getDescriptionLabel("Delay"));
        delayField = getInputField();
        settingsPanel.add(delayField);

        return settingsPanel;
    }

    private Border getCompoundBorder(String titleText) {
        Border loweredEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder title = BorderFactory.createTitledBorder(loweredEtched, titleText,
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.PLAIN, 12));
        return BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private JLabel getDescriptionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        return label;
    }

    private JFormattedTextField getInputField() {
        JFormattedTextField inputField = null;
        try {
            MaskFormatter formatter = new MaskFormatter("##:##");
            formatter.setPlaceholderCharacter('0');
            inputField = new JFormattedTextField(formatter);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert inputField != null;
        String s = "00:" + String.valueOf(beepLength / 10) + String.valueOf(beepLength % 10);
        inputField.setValue(s);
        inputField.setFont(new Font("Arial", Font.PLAIN, 16));
        inputField.addPropertyChangeListener("value", new TimeFieldPropertyChangeListener());
        return inputField;
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException |
                    UnsupportedLookAndFeelException |
                    InstantiationException |
                    IllegalAccessException e) {
                e.printStackTrace();
            }
            Main frame = new Main();
        });
    }

    class StartButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            TimeDuration interval = TimeDuration.parse((String) intervalField.getValue());
            TimeDuration delay = TimeDuration.parse((String) delayField.getValue());
            try {
                checkValues(interval, delay);
                settingsFrame.setVisible(false);
                clockFrame.setVisible(true);
                clockUI.startClock(interval, delay);
            } catch (Exception exp) {
                JOptionPane.showMessageDialog(settingsFrame,
                        exp.getMessage() + " must be at least " + beepLength + " seconds!",
                        "Input error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private void checkValues(TimeDuration interval, TimeDuration delay) {
            if (interval.getMinutes() == 0 && interval.getSeconds() < beepLength) {
                throw new IllegalArgumentException("Interval");
            } else if (delay.getMinutes() == 0 && delay.getSeconds() < beepLength) {
                throw new IllegalArgumentException("Delay");
            }
        }
    }

    class TimeFieldPropertyChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            JFormattedTextField field = (JFormattedTextField) evt.getSource();
            TimeDuration time = TimeDuration.parse((String) field.getValue());
            if (time.getSeconds() >= 60) {
                String s = String.valueOf(time.getMinutes() / 10) + String.valueOf(time.getMinutes() % 10) + ":00";
                field.setValue(s);
            }
        }
    }
}
