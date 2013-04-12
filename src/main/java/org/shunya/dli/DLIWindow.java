package org.shunya.dli;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.shunya.dli.DLIDownloader.DownloadRequest.request;
import static org.shunya.dli.DLIDownloader.instance;

public class DLIWindow extends JPanel {
    private final JFrame frame;
    private final JTable jTable;
    private JTextField jTextField;
    private JButton jAddButton;
    private SystemTray tray;
    private TrayIcon trayIcon;
    private final RunningTaskTableModel tableModel = new RunningTaskTableModel();
    private final String rootDirectory;
    private final ExecutorService executorService;
    private final WindowListener exitListener = new ExitListener();

    public boolean addTask(InteractiveTask task) {
        return getTableModel().addDownload(task);
    }

    public void cancelAll() {
        ArrayList<InteractiveTask> modelData = getTableModel().getModelData();
        for (InteractiveTask task : modelData) {
            task.stopAsync();
        }
        for (InteractiveTask task : modelData) {
            task.awaitShutdown();
        }
    }

    public DLIWindow() throws ClassNotFoundException {
        frame = new JFrame("DLI Downloader");
        Path path = FileSystems.getDefault().getPath(System.getProperty("user.dir"));
        rootDirectory=path.resolve("DLI").toString();
        this.executorService = Executors.newFixedThreadPool(2);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        jTextField = new JTextField(20);
        jTextField.setFont(new Font("Arial", Font.TRUETYPE_FONT, 18));
        jTextField.setPreferredSize(new Dimension(90, 35));
        jTextField.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });

        jAddButton = new JButton("Add");
        jAddButton.setPreferredSize(new Dimension(45, 35));
        jAddButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });
        jTable = new JTable(tableModel);
        jTable.setShowGrid(false);
        jTable.setPreferredScrollableViewportSize(new Dimension(480, 420));
        jTable.setFillsViewportHeight(true);
        jTable.setAutoCreateRowSorter(true);
        jTable.setRowHeight(25);
        jTable.setRowMargin(2);
        jTable.setDragEnabled(true);
        jTable.setIntercellSpacing(new Dimension(1, 1));
        jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        GUIUtils.initializeTableColumns(jTable, tableModel.width);
        jTable.getColumn("<html><b>Progress").setCellRenderer(new ProgressRenderer());
        jTable.setFont(new Font("Arial", Font.TRUETYPE_FONT, 15));
        TableCellRenderer dcr = jTable.getDefaultRenderer(String.class);
        if (dcr instanceof JLabel) {
            ((JLabel) dcr).setVerticalAlignment(SwingConstants.TOP);
            ((JLabel) dcr).setBorder(new EmptyBorder(0, 0, 0, 0));
        }
        JTableHeader header = jTable.getTableHeader();
        TableCellRenderer headerRenderer = header.getDefaultRenderer();
        if (headerRenderer instanceof JLabel) {
            ((JLabel) headerRenderer).setHorizontalAlignment(JLabel.CENTER);
        }
        header.setPreferredSize(new Dimension(30, 30));

        final JMenuItem stopTaskMenu, clearTaskMenu;
        final JPopupMenu popupTask = new JPopupMenu();
        stopTaskMenu = new JMenuItem("Stop Task");
        stopTaskMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jTable.getSelectedRow() != -1) {
                    try {
                        InteractiveTask td = getTableModel().getRow(jTable.convertRowIndexToModel(jTable.getSelectedRow()));
                        System.out.println(td);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
        });
        popupTask.add(stopTaskMenu);

        jTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = jTable.rowAtPoint(e.getPoint());
//	        	  System.out.println(selRow);
                if (selRow != -1) {
                    jTable.setRowSelectionInterval(selRow, selRow);
                    int selRowInModel = jTable.convertRowIndexToModel(selRow);
                }
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (jTable.getSelectedRow() == -1 || selRow == -1) {
                        stopTaskMenu.setEnabled(false);
                    } else {
                        stopTaskMenu.setEnabled(true);
                    }
                    popupTask.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        PopupMenu popup = new PopupMenu();
        MenuItem openPunterMenuItem = new MenuItem("My DLI");
        openPunterMenuItem.setFont(new Font("Tahoma", Font.BOLD, 12));
        openPunterMenuItem.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setExtendedState(Frame.NORMAL);
                frame.setVisible(true);
            }});
        popup.add(openPunterMenuItem);
        popup.addSeparator();

        MenuItem defaultItem = new MenuItem("Exit");
        defaultItem.addActionListener((ActionListener) exitListener);
        popup.add(defaultItem);

        if (SystemTray.isSupported()) {
            tray = SystemTray.getSystemTray();
            BufferedImage busyImage;
            try {
                busyImage = ImageIO.read(DLIWindow.class.getResourceAsStream("/images/dli.png"));
                frame.setIconImage(busyImage);
                trayIcon = new TrayIcon(busyImage, "My Assistant", popup);
                trayIcon.setToolTip("My Assistant started.");
                trayIcon.setImageAutoSize(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            ActionListener actionListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    frame.setExtendedState(Frame.NORMAL);
                    frame.setVisible(true);
                }
            };

            MouseListener mouseListener = new MouseListener() {
                public void mouseClicked(MouseEvent e) {
                    if(e.getButton()==MouseEvent.BUTTON1){
                        frame.setExtendedState(Frame.NORMAL);
                        frame.setVisible(true);
                    }
                }

                public void mouseEntered(MouseEvent e) {
                }

                public void mouseExited(MouseEvent e) {
                }

                public void mousePressed(MouseEvent e) {
                }

                public void mouseReleased(MouseEvent e) {
                }
            };

            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(actionListener);
            trayIcon.addMouseListener(mouseListener);
            try {
                tray.add(trayIcon);
                trayIcon.displayMessage("DLI Downloader",
                        "Click here to launch DLI Downloader",
                        TrayIcon.MessageType.INFO);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }
        }

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.weightx = 0.8;
        c.gridx = 0;
        c.gridy = 0;
        add(jTextField, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
        c.weightx = 0.2;
        c.gridx = 1;
        c.gridy = 0;
        add(jAddButton, c);

        c.fill = GridBagConstraints.BOTH;
        c.ipady = 0;      //make this component tall
        c.weightx = 0.0;
        c.weighty = 0.9;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        add(new JScrollPane(jTable), c);
        setOpaque(true);
    }

    private void addTask() {
        try {
            final DLIDownloader task = instance(request(jTextField.getText(), rootDirectory));
            if (addTask(task)) {
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            task.download();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                        }
                    }
                });
            } else {
                JOptionPane.showMessageDialog(frame, "Barcode already added for download : " + jTextField.getText(), "Duplicate Job", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public RunningTaskTableModel getTableModel() {
        return tableModel;
    }

    public void createAndShowGUI() throws Exception {


        JFrame.setDefaultLookAndFeelDecorated(true);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowIconified(WindowEvent e) {
                frame.setVisible(false);
                frame.dispose();
            }
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
                frame.dispose();
//			displayMsg("Personal Assistant has been minimized to System Tray",TrayIcon.MessageType.INFO);
            }
        });
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(this);
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    DLIWindow window = new DLIWindow();
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                    window.createAndShowGUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    class ExitListener extends WindowAdapter implements ActionListener{
        @Override
        public void windowClosing(WindowEvent e) {
            exit();
        }

        private void exit() {
            int confirm = JOptionPane.showOptionDialog(frame,
                    "Are You Sure to Close this Application?",
                    "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (confirm == JOptionPane.YES_OPTION) {
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        cancelAll();
                        tray.remove(trayIcon);
                        System.exit(0);
                    }
                };
                t.start();
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            exit();
        }
    }

    static class ProgressRenderer extends JProgressBar implements TableCellRenderer {
        public ProgressRenderer() {
            super();
            this.setStringPainted(true);
            //  this.setIndeterminate(true);
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column) {
            int val = Integer.parseInt(value.toString());
            this.setValue(val);
            return this;
        }

        public boolean isDisplayable() {
            // This does the trick. It makes sure animation is always performed
            return true;
        }

        public void repaint() {
            // If you have access to the table you can force repaint like this.
            //Otherwize, you could trigger repaint in a timer at some interval
            try {
                //   theTable.repaint();
            } catch (Exception e) {
                System.out.println("1111");
            }
        }
    }
}
