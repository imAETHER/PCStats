package im.aether;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;

public class GUI {
    private JPanel mainPanel;
    private JTabbedPane tabbedPane1;
    private JPanel rpcTab;
    private JPanel settingsTab;
    private JTextField appIdField;
    private JTextField bigImageField;
    private JTextField smallImageField;
    private JComboBox<RTextOptions> topTextOpt;
    private JComboBox<RTextOptions> bottomTextOpt;
    private JButton ohmDownload;
    private JButton toggleButton;
    private JLabel ohmStatusLabel;
    private JTextField topCustomText;
    private JTextField bottomCustomText;
    private JComboBox<String> activeGpus;

    public GUI() {
        final JFrame frame = new JFrame("PC Stats | by github.com/imAETHER");
        frame.setContentPane(mainPanel);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        if (SystemTray.isSupported()) {
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

            final SystemTray tray = SystemTray.getSystemTray();
            final Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/F2OB08zWgAUYmxZ.png"));
            final TrayIcon trayIcon = new TrayIcon(image, "RPCStats");
            trayIcon.setImageAutoSize(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    tray.remove(trayIcon);
                    frame.setVisible(true);
                }
            });
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(ComponentEvent e) {
                    super.componentHidden(e);
                    try {
                        // If the RPC is off, then allow for normal closing, otherwise add a tray icon
                        if (toggleButton.getText().contains("Start")) {
                            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                            return;
                        }
                        tray.add(trayIcon);
                        frame.setVisible(false);
                    } catch (AWTException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
        }

        for (RTextOptions option : RTextOptions.values()) {
            topTextOpt.addItem(option);
            bottomTextOpt.addItem(option);
        }
        bottomTextOpt.setSelectedIndex(1);

        // Default app id, shouldn't really be changed unless you want your own
        appIdField.setText("995501904685715476");

        topCustomText.setEnabled(false);
        bottomCustomText.setEnabled(false);

        toggleButton.setEnabled(false);
        toggleButton.putClientProperty("JButton.buttonType", "square");

        ohmDownload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(URI.create("https://openhardwaremonitor.org/downloads/"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        final ItemListener comboListener = (d) -> {
            if (d.getStateChange() != ItemEvent.SELECTED) return;

            final RTextOptions item = (RTextOptions) d.getItem();
            final JTextField toUpdate = d.getSource().equals(topTextOpt) ? topCustomText : bottomCustomText;
            toUpdate.setEnabled(item == RTextOptions.Custom);
        };

        topTextOpt.addItemListener(comboListener);
        bottomTextOpt.addItemListener(comboListener);
    }

    public JTextField getTopCustomText() {
        return topCustomText;
    }

    public JTextField getBigImageField() {
        return bigImageField;
    }

    public JTextField getSmallImageField() {
        return smallImageField;
    }

    public JTextField getAppIdField() {
        return appIdField;
    }

    public JTextField getBottomCustomText() {
        return bottomCustomText;
    }

    public JComboBox<RTextOptions> getBottomTextOpt() {
        return bottomTextOpt;
    }

    public JComboBox<RTextOptions> getTopTextOpt() {
        return topTextOpt;
    }

    public JLabel getOhmStatusLabel() {
        return ohmStatusLabel;
    }

    public JButton getToggleButton() {
        return toggleButton;
    }

    public JComboBox<String> getActiveGpus() {
        return activeGpus;
    }
}
