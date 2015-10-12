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
import java.text.ParseException;

/**
 * Main frame
 * Created by Sebastian on 2015-10-03.
 */
public class Main {

    private final JFrame settingsFrame;
    private final JFrame clockFrame;
    private final ClockUI clockUI;
    private JFormattedTextField delayField;
    private JFormattedTextField intervalField;

    private Main() {
        settingsFrame = new JFrame("ClockS");
        clockFrame = new JFrame("Clock");
        clockUI = new ClockUI(settingsFrame, clockFrame);

        settingsFrame.getContentPane().setLayout(new BoxLayout(settingsFrame.getContentPane(),BoxLayout.PAGE_AXIS));

        settingsFrame.setJMenuBar(createMenuBar());

        JPanel borderPanel = new JPanel();
        borderPanel.setLayout(new BoxLayout(borderPanel,BoxLayout.PAGE_AXIS));
        borderPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        settingsFrame.getContentPane().add(borderPanel);

        JPanel settingsPanel = createSettingsPanel();

        JLabel intervalLabel = new JLabel("Interval");
        intervalLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        settingsPanel.add(intervalLabel);
        intervalField = null;
        try {
            MaskFormatter formatter = new MaskFormatter("##:##");
            formatter.setPlaceholderCharacter('0');
            intervalField = new JFormattedTextField(formatter);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert intervalField != null;
        intervalField.setValue("00:05");
        intervalField.setFont(new Font("Arial", Font.PLAIN, 16));
        intervalField.addPropertyChangeListener("value", new TimeFieldPropertyChangeListener());
        settingsPanel.add(intervalField);

        JLabel delayLabel = new JLabel("Delay");
        delayLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        settingsPanel.add(delayLabel);
        delayField = null;
        try {
            MaskFormatter formatter = new MaskFormatter("##:##");
            formatter.setPlaceholderCharacter('0');
            delayField = new JFormattedTextField(formatter);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert delayField != null;
        delayField.setValue("00:05");
        delayField.setFont(new Font("Arial", Font.PLAIN, 16));
        delayField.addPropertyChangeListener("value",  new TimeFieldPropertyChangeListener());
        settingsPanel.add(delayField);

        borderPanel.add(settingsPanel);
        // add appearance panel to do
        borderPanel.add(Box.createRigidArea(new Dimension(0,5)));
        borderPanel.add(createButtonPanel(intervalField, delayField));

        settingsFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        settingsFrame.pack();
        settingsFrame.setResizable(false);
        settingsFrame.setLocationByPlatform(true);
        settingsFrame.setVisible(true);
    }

    private JPanel createButtonPanel(JFormattedTextField intervalField, JFormattedTextField delayField) {
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.LINE_AXIS));
        buttons.add(Box.createHorizontalGlue());
        buttons.add(createStartButton(intervalField, delayField));
        return buttons;
    }

    private JButton createStartButton(JFormattedTextField intervalField, JFormattedTextField delayField) {
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
        settingsPanel.setLayout(new GridLayout(0,2,10,10));
        Border loweredEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder title = BorderFactory.createTitledBorder(loweredEtched, "Clock settings",
                TitledBorder.LEFT, TitledBorder.TOP, new Font("Arial", Font.PLAIN, 12));
        Border compound = BorderFactory.createCompoundBorder(title, BorderFactory.createEmptyBorder(10, 10, 10, 10));
        settingsPanel.setBorder(compound);
        return settingsPanel;
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
                        exp.getMessage() + " must be at least 5 seconds!",
                        "Input error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        private void checkValues(TimeDuration interval, TimeDuration delay) {
            if (interval.getMinutes() == 0 && interval.getSeconds() < 5) {
                throw new IllegalArgumentException("Interval");
            } else if (delay.getMinutes() == 0 && delay.getSeconds() < 5) {
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
