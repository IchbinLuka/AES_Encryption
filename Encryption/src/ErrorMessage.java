import java.awt.*;
import java.awt.event.*;

public class ErrorMessage extends Frame {

    Label textField;
    public ErrorMessage(String exeptionString)
    {
        super("Error");
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent evt) { dispose(); }
            });
        setSize(300, 120);
        setResizable(false);
        
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        int x = (d.width - getSize().width) / 2;
        int y = (d.height - getSize().height) / 2;

        setLocation(x, y);
        setResizable(false);

        Panel cp = new Panel(null);
        add(cp);

        textField = new Label();
        textField.setBounds(15, 20, 280, 30);
        textField.setText("Error: " + exeptionString);
        cp.add(textField);

        setVisible(true);
    }
}
