import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import World.Simulation;

public class WindowManager{

    private static JFrame frame;
    public static JButton start = new JButton("Start");
    public static JButton pause = new JButton("Pause");

    

    public static void createWindow(Simulation simulation){

        frame = new JFrame("Prey and Predator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(simulation, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        start.setActionCommand("START");
        pause.setActionCommand("PAUSE");
        start.addActionListener(simulation);
        pause.addActionListener(simulation);
        
        controlPanel.add(start);
        controlPanel.add(Box.createHorizontalStrut(10));
        controlPanel.add(pause);

        
        JPanel fitnessPanel = new JPanel();
        fitnessPanel.add(Simulation.graph);
        controlPanel.add(fitnessPanel, BorderLayout.EAST);

        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);
        

    }

}
