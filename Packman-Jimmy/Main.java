import java.awt.EventQueue;
import javax.swing.JFrame;

public class Main {
  public static void main(String[] args) {

      EventQueue.invokeLater(() -> {
            Pacman ex = new Pacman();
            ex.setVisible(true);
        });
  }
}

class Pacman extends JFrame {

    public Pacman() {
        
        initUI();
    }
    
    private void initUI() {
        
        add(new Board());
        
        setTitle("Pacman");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(750, 750);//changed the size so it can fit the newly sized map
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {

  
    }
}