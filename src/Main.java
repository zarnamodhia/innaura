
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import ui.ServicePanel;
import ui.StaffPanel;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("Hotel Management System");

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1100, 750);
            frame.setLocationRelativeTo(null);

            JTabbedPane tabs = new JTabbedPane();

            tabs.addTab("Hotel Services", new ServicePanel());
            tabs.addTab("Staff Management", new StaffPanel());

            frame.setContentPane(tabs);
            frame.setVisible(true);
        });
    }
}
