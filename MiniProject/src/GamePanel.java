import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public class GamePanel extends JPanel {

	private JLabel startText;
	private JTextField tf;

	private GameStart[] gs = new GameStart[4];
	private int[] delay = { 200, 400, 300, 500 };

	private EndGame eg = new EndGame();

	private JLabel policeLabel;
	
	private Clip clip;
	private Clip gameFrameClip;

	public GamePanel() {
		setLayout(new BorderLayout());
		add(new JGameGroundPanel(), BorderLayout.CENTER);
		add(new JInputPanel(), BorderLayout.SOUTH);
	}

	class JGameGroundPanel extends JPanel {
		private Image map = new ImageIcon("map.png").getImage();

		public JGameGroundPanel() {
			setLayout(null);
			startText = new JLabel("");
			startText.setLocation(80, 10);
			startText.setSize(34, 30);
			add(startText);
			
			int y[] = {50, 135 ,290, 370};

			for (int i = 0; i < gs.length; i++) {
				gs[i] = new GameStart(y[i], delay);
			}
			for (int i = 0; i < gs.length; i++) {
				add(gs[i].getQuizLabel());
			}
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(map, 0, 0, getWidth(), getHeight(), this);
		}
	}

	class JInputPanel extends JPanel {
		public JInputPanel() {
			setLayout(new FlowLayout());

			tf = new JTextField(43);
			tf.addKeyListener(new EnterKey());
			add(tf);
		}
	}
	
	public void setClip(Clip clip) {
		gameFrameClip = clip;
	}

	public void setPoliceLabel(JLabel policeLabel) {
		this.policeLabel = policeLabel;
	}

	public GameStart[] getGameStart() {
		return gs;
	}

	public EndGame getEndGame() {
		return eg;
	}

	class EnterKey extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			int code = e.getKeyCode();

			switch (code) {
			case KeyEvent.VK_ENTER:
				String str = tf.getText();
				for (int i = 0; i < gs.length; i++) {
					if (gs[i].getQuizLabel().getText().equals(str)) {
						int score = new Score().getScore();

						if (gs[i].getBooster()==true) {
							score += 50;
							new Score().setScore(score);
							gs[i].setBooster(false);
						} else {
							score += 10;
							new Score().setScore(score);
						}

						PoliceThread pt = new PoliceThread();
						pt.start();
						
						gs[i].getTextThread().reQuiz();
					}
				}
				tf.setText("");
			}
		}

		class PoliceThread extends Thread {
			private ImageIcon policeIcon;

			public PoliceThread() {
				ImageIcon icon = new ImageIcon("police.png");
				Image img = icon.getImage();
				img = img.getScaledInstance(130, 140, Image.SCALE_SMOOTH);
				policeIcon = new ImageIcon(img);
			}

			public void run() {
				ImageIcon icon = new ImageIcon("policeSuccess.png");
				Image img = icon.getImage();
				img = img.getScaledInstance(130, 140, Image.SCALE_SMOOTH);

				ImageIcon policeSuccess = new ImageIcon(img);
				policeLabel.setIcon(policeSuccess);
				try {
					sleep(1500);
					policeLabel.setIcon(policeIcon);
				} catch (InterruptedException e1) {
				}
			}
		}
	}

	class StartButton implements ActionListener {
		private boolean flag = true; // 버튼을 눌러 게임을 실행시킨 후 다시 버튼을 못 누르게 할 깃발

		public void actionPerformed(ActionEvent e) {
			if (flag) {
				try {
					gameFrameClip.stop();
					clip = AudioSystem.getClip();
					File audioFile = new File("choicebgm.wav");
					AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
					clip.open(audioStream);
				} catch (LineUnavailableException e1) {
					e1.printStackTrace();
				} catch (UnsupportedAudioFileException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				clip.loop(Clip.LOOP_CONTINUOUSLY);
				
				eg.setClip(gameFrameClip, clip);
				
				startText.setOpaque(true);
				startText.setForeground(Color.white);
				startText.setBackground(Color.black);

				startText.setText("61");
				startText.setLocation(230, 10);
				startText.setFont(new Font("Arial", Font.BOLD, 30));

				delay[0] = 200;
				delay[1] = 400;
				delay[2] = 300;
				delay[3] = 500;

				int score = new Score().getScore();
				score = 0;
				new Score().setScore(score);

				// tf.setFocusable(true);
				tf.requestFocus(); // 게임 시작 시 포커싱

				for (int i = 0; i < gs.length; i++) {
					gs[i].gameStart();
				}
				TimerThread tt = new TimerThread();
				tt.start();
			}
			flag = false;
		}

		class TimerThread extends Thread {
			private int time;
			private boolean flag2 = true; // thread flag1
			private boolean flag3 = true; // thread flag2

			public TimerThread() {
				time = Integer.parseInt(startText.getText());
			}

			public void run() {
				while (true) {
					time--;
					startText.setText(Integer.toString(time));
					if (time <= 0) {
						interrupt();
					}
					if (time <= 40) {
						if (flag2) {
							for (int i = 0; i < gs.length; i++) {
								delay[i] = delay[i] / 2;
								flag2 = false;
							}
						}
					}
					if (time <= 20) {
						if (flag3) {
							for (int i = 0; i < gs.length; i++) {
								delay[i] = delay[i] / 2;
								flag3 = false;
							}
						}
					}
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						for (int i = 0; i < gs.length; i++) {
							gs[i].getTextThread().interrupt();
							flag = true;
						}
						eg.endGame();
						return;
					}
				}
			}
		}
	}
}

class GameStart {
	private ImageIcon quizIcon[] = new ImageIcon[9];
	private int imgRandom;
	private boolean booster=false;

	private JLabel quiz;
	private TextThread tt;

	private int y;
	private int delay[];

	public GameStart(int y, int delay[]) {
		this.y = y;
		this.delay = delay;

		ImageIcon icon[] = new ImageIcon[9];

		icon[0] = new ImageIcon("baeggi.png");
		icon[1] = new ImageIcon("digini.png");
		icon[2] = new ImageIcon("dao.png");
		icon[3] = new ImageIcon("caepi.png");
		icon[4] = new ImageIcon("mosee.png");
		icon[5] = new ImageIcon("wooni.png");
		icon[6] = new ImageIcon("eddi.png");
		icon[7] = new ImageIcon("marid.png");
		icon[8] = new ImageIcon("booster.jpg");

		for (int i = 0; i < icon.length; i++) {
			Image img = icon[i].getImage();
			img = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
			quizIcon[i] = new ImageIcon(img);
		}

		quiz = new JLabel();
		//quiz.setOpaque(true);
		//quiz.setBackground(Color.black);
		quiz.setForeground(Color.magenta);
		quiz.setFont(new Font("Arial", Font.BOLD, 15));
	}

	public void setBooster(boolean booster) {
		this.booster=booster;
	}
	
	public boolean getBooster() {
		return booster;
	}
	
	public void gameStart() {
		quiz.setVisible(true);

		imgRandom = (int) (Math.random() * quizIcon.length);
		quiz.setText(randomText());
		quiz.setIcon(quizIcon[imgRandom]);
		if(imgRandom==8) {booster=true;}

		quiz.setLocation(450, y);

		quiz.setSize(300, 50);

		int random = (int) (Math.random() * delay.length);

		tt = new TextThread(quiz, random);
		tt.start();
	}

	public void setFont(String fontName, int fontsize) {
		quiz.setFont(new Font(fontName, Font.BOLD, fontsize));
	}

	public JLabel getQuizLabel() {
		return quiz;
	}

	public String randomText() {
		Vector<String> word = new WordFile().getWord();
		int random = (int) (Math.random() * (word.size()));
		return word.get(random);
	}

	public TextThread getTextThread() {
		return tt;
	}

	class TextThread extends Thread {
		private JLabel quiz;
		private int random;

		public TextThread(JLabel quiz, int random) {
			this.quiz = quiz;
			this.random = random;
		}

		public void reQuiz() {
			imgRandom = (int) (Math.random() * quizIcon.length);
			int y = quiz.getY();
			quiz.setLocation(470, y);
			quiz.setText(randomText());
			quiz.setIcon(quizIcon[imgRandom]);
			if(imgRandom==8) {booster=true;}
			// quiz.setHorizontalAlignment(SwingConstants.CENTER);
		}

		public void run() {
			while (true) {
				int x = quiz.getX();
				int y = quiz.getY();

				if (x <= -50) {
					x = 470;
					imgRandom = (int) (Math.random() * quizIcon.length);
					quiz.setText(randomText());
					quiz.setIcon(quizIcon[imgRandom]);
					if(imgRandom==8) {booster=true;}
					//quiz.setHorizontalAlignment(SwingConstants.CENTER);

					random = (int) (Math.random() * delay.length);

					int score = new Score().getScore();
					score -= 10;
					if (score < 0)
						score = 0;
					new Score().setScore(score);
				}

				x -= 10;

				quiz.setLocation(x, y);

				try {
					sleep(delay[random]);
				} catch (InterruptedException e) {
					quiz.setVisible(false);
					return;
				}
			}
		}
	}
}

class EndGame {
	private Rank r = new Rank();
	private Vector<String> nameRank = r.getNameRank();
	private Vector<Integer> scoreRank = r.getScoreRank();
	private Clip clip;
	private Clip gameFrameClip;

	public void endGame() {
		int index = 0; // 저장되어 있는 Vector의 가장 마지막 index 번호
		boolean newRecord = false;

		if (scoreRank.size() == 1) {
			newRecord = true;
		} else {
			for (int i = (scoreRank.size() - 2); i >= 0; i--) { // 맨 뒷 등수부터 비교함
				if (scoreRank.get(i) <= new Score().getScore()) { // 랭킹에 들었을 때
					index = i;
					newRecord = true;
				} else
					break;
			}
		}

		if (newRecord) { // 랭킹에 등록
			if (scoreRank.size() < 6) { // 랭킹에 5명이 없을 때
				scoreRank.add(index, new Score().getScore());
				nameRank.add(index, new UserName().getUserName());
				JOptionPane.showMessageDialog(null,
						new UserName().getUserName() + " : " + new Score().getScore() + "  ! New Record !",
						"Game Over!", JOptionPane.INFORMATION_MESSAGE);
			} else { // 랭킹에 5명이 있을 때
				scoreRank.add(index, new Score().getScore());
				nameRank.add(index, new UserName().getUserName());

				scoreRank.remove(scoreRank.size() - 1);
				nameRank.remove(scoreRank.size() - 1);

				JOptionPane.showMessageDialog(null,
						new UserName().getUserName() + " : " + new Score().getScore() + "  ! New Record !",
						"Game Over!", JOptionPane.INFORMATION_MESSAGE);
			}
		} else // 랭킹 등록 실패
			JOptionPane.showMessageDialog(null, new UserName().getUserName() + " : " + new Score().getScore(),
					"Game Over!", JOptionPane.INFORMATION_MESSAGE);
		r.writeRanking(); // 랭킹 파일에 저장
		clip.stop();
		gameFrameClip.loop(Clip.LOOP_CONTINUOUSLY);
	}
	public void setClip(Clip gameFrameClip, Clip clip) {
		this.clip = clip;
		this.gameFrameClip = gameFrameClip; 
	}
}