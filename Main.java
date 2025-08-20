import javax.swing.Timer;

public class Main {

    private static Timer timer;
    // private static WindowManager window;
    private static Simulation simulation;

    public static void main(String[] args){
        simulation = new Simulation();

        WindowManager.createWindow(simulation);

        timer = new Timer(50, simulation);
        timer.start();

    }
    
}
