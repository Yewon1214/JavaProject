import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.*;

import java.io.*;

public class EditPanel extends JPanel {
	private Vector<String> word = new WordFile().getWord();
	private JTextField tf;
	private JLabel wordCount;
	private JLabel saveText;
	private JTextField findtf;
	private JLabel findLabel;
	private JLabel findSuccess;

	public EditPanel() {
		setBackground(Color.YELLOW);
		setLayout(new FlowLayout(FlowLayout.CENTER, 25, 10));

		tf = new JTextField(21);
		add(tf);

		JButton b1 = new JButton("Add");
		b1.addActionListener(new AddButton());
		JButton b2 = new JButton("Save");
		b2.addActionListener(new SaveWordFile());
		add(b1);
		add(b2);

		wordCount = new JLabel("Word Count = ".concat(Integer.toString(word.size())));
		wordCount.setHorizontalAlignment(SwingConstants.CENTER);
		add(wordCount);

		saveText = new JLabel("                        ");
		add(saveText);

		findLabel = new JLabel("Find:");
		findSuccess = new JLabel("                        ");
		findtf = new JTextField(15);
		findtf.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();

				switch (code) {
				case KeyEvent.VK_ENTER:
					if (!findtf.getText().equals("")) {
						Iterator<String> it = new WordFile().getWord().iterator();
						boolean flag = false;
						while (it.hasNext()) {
							String w = it.next();
							if (w.equals(findtf.getText())) {
								flag = true;
								break;
							}
						}

						if (flag) {
							TextDelay td = new TextDelay(findSuccess, "Find Successful!");
							td.start();
						} else {
							TextDelay td = new TextDelay(findSuccess, "Find Fail!");
							td.start();
						}

						findtf.setText("");
					}
				}
			}
		});

		add(findLabel);
		add(findtf);
		add(findSuccess);
	}

	public void setFont(String fontname, int fontsize) {
		saveText.setFont(new Font(fontname, Font.BOLD, fontsize));
		wordCount.setFont(new Font(fontname, Font.BOLD, fontsize));
		findLabel.setFont(new Font(fontname, Font.BOLD, fontsize));
		findSuccess.setFont(new Font(fontname, Font.BOLD, fontsize));
	}

	public void setForeground(int r, int g, int b) {
		Color c = new Color(r, g, b);
		saveText.setForeground(c);
		wordCount.setForeground(c);
		findLabel.setForeground(c);
		findSuccess.setForeground(c);
	}

	public void setBackground(int r, int g, int b) {
		Color c = new Color(r, g, b);
		setBackground(c);
	}

	class AddButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (!tf.getText().equals("")) {
				word.add(tf.getText());
				tf.setText("");

				wordCount.setText("Word Count = ".concat(Integer.toString(word.size())));
			}
		}
	}

	class SaveWordFile implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				FileWriter fout = new FileWriter("word.txt");

				for (int i = 0; i < word.size(); i++) {
					fout.write(word.get(i));
					fout.write("\r\n", 0, 2);
				}
				fout.close();
				tf.setText("");

				TextDelay td = new TextDelay(saveText, "Save Successful!");
				td.start();
			} catch (IOException e1) {
				System.out.println("입출력 오류");
				System.exit(0);
			}
		}
	}

	class TextDelay extends Thread {
		private JLabel label;
		private String str;

		public TextDelay(JLabel label, String str) {
			this.label = label;
			this.str = str;
		}

		public void run() {
			label.setText(str);
			try {
				sleep(2000);
				label.setText("                        ");
			} catch (InterruptedException e) {
			}
		}
	}
}
