package UI.LeTan;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;

public class LeTan_GUI extends JFrame implements ActionListener{
	public LeTan_GUI() {
		setTitle("^-^");
		setSize(1100, 800);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	public static void main(String[] args) {
		new LeTan_GUI().setVisible(true);
	}
}