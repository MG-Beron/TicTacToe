import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

public class TicTacToe extends JFrame implements ChangeListener, ActionListener {

    private Boolean playerO = true;
    private JSlider slider;
    private JButton oButton, xButton;
    private Board board;
    private int lineThickness = 4;
    private Color oColor = Color.BLUE, xColor = Color.RED;
    static final char BLANK = ' ', O = 'O', X = 'X';
    private char position[] = { // Board position (BLANK, O, or X)
            BLANK, BLANK, BLANK,
            BLANK, BLANK, BLANK,
            BLANK, BLANK, BLANK};
    private int wins = 0, losses = 0, draws = 0;  // game count by user

    // Start the game
    public static void main(String args[]) {
        new TicTacToe();
    }

    // Initialize
    public TicTacToe() {
        super("Tic Tac Toe demo");
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        topPanel.add(new JLabel("Line Thickness:"));
        topPanel.add(slider = new JSlider(SwingConstants.HORIZONTAL, 1, 20, 4));
        slider.setMajorTickSpacing(1);
        slider.setPaintTicks(true);
        slider.addChangeListener(this);
        topPanel.add(oButton = new JButton("O Color"));
        topPanel.add(xButton = new JButton("X Color"));
        oButton.addActionListener(this);
        xButton.addActionListener(this);
        add(topPanel, BorderLayout.NORTH);
        add(board = new Board(), BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 500);
        setVisible(true);
    }

    // Change line thickness
    public void stateChanged(ChangeEvent e) {
        lineThickness = slider.getValue();
        board.repaint();
    }

    // Change color of O or X
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == oButton) {
            Color newColor = JColorChooser.showDialog(this, "Choose a new color for O", oColor);
            if (newColor != null) {
                oColor = newColor;
            }
        } else if (e.getSource() == xButton) {
            Color newColor = JColorChooser.showDialog(this, "Choose a new color for X", xColor);
            if (newColor != null) {
                xColor = newColor;
            }
        }
        board.repaint();
    }

    // Board is what actually plays and displays the game
    private class Board extends JPanel implements MouseListener {

        private Random random = new Random();
        private int rows[][] = {{0, 2}, {3, 5}, {6, 8}, {0, 6}, {1, 7}, {2, 8}, {0, 8}, {2, 6}};
        // Endpoints of the 8 rows in position[] (across, down, diagonally)

        public Board() {
            addMouseListener(this);
        }

        // Redraw the board
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth();
            int h = getHeight();
            Graphics2D g2d = (Graphics2D) g;

            // Draw the grid
            g2d.setPaint(Color.WHITE);
            g2d.fill(new Rectangle2D.Double(0, 0, w, h));
            g2d.setPaint(Color.BLACK);
            g2d.setStroke(new BasicStroke(lineThickness));
            g2d.draw(new Line2D.Double(0, h / 3, w, h / 3));
            g2d.draw(new Line2D.Double(0, h * 2 / 3, w, h * 2 / 3));
            g2d.draw(new Line2D.Double(w / 3, 0, w / 3, h));
            g2d.draw(new Line2D.Double(w * 2 / 3, 0, w * 2 / 3, h));

            // Draw the Os and Xs
            for (int i = 0; i < 9; ++i) {
                double xpos = (i % 3 + 0.5) * w / 3.0;
                double ypos = (i / 3 + 0.5) * h / 3.0;
                double xr = w / 8.0;
                double yr = h / 8.0;
                if (position[i] == O) {
                    g2d.setPaint(oColor);
                    g2d.draw(new Ellipse2D.Double(xpos - xr, ypos - yr, xr * 2, yr * 2));
                } else if (position[i] == X) {
                    g2d.setPaint(xColor);
                    g2d.draw(new Line2D.Double(xpos - xr, ypos - yr, xpos + xr, ypos + yr));
                    g2d.draw(new Line2D.Double(xpos - xr, ypos + yr, xpos + xr, ypos - yr));
                }
            }
        }

        // Draw an O where the mouse is clicked
        public void mouseClicked(MouseEvent e) {
            if (playerO) {
                int xpos = e.getX() * 3 / getWidth();
                int ypos = e.getY() * 3 / getHeight();
                int pos = xpos + 3 * ypos;
                if (pos >= 0 && pos < 9 && position[pos] == BLANK) {
                    position[pos] = O;
                    playerO = false;
                    checkGame();
                    repaint();
                }
            } else {
                int xpos = e.getX() * 3 / getWidth();
                int ypos = e.getY() * 3 / getHeight();
                int pos = xpos + 3 * ypos;
                if (pos >= 0 && pos < 9 && position[pos] == BLANK) {
                    position[pos] = X;
                    playerO = true;
                    checkGame();
                    repaint();
                }
            }

        }

        // Ignore other mouse events
        public void mousePressed(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        // Computer plays X
        private void checkGame() {
            // Check if game is over
            if (won(O)) {
                newGame(O);
            } else if (isDraw()) {
                newGame(BLANK);
            } // Play X, possibly ending the game
            else {
                if (won(X)) {
                    newGame(X);
                } else if (isDraw()) {
                    newGame(BLANK);
                }
            }
        }

        // Return true if player has won
        private boolean won(char player) {
            for (int i = 0; i < 8; ++i) {
                if (testRow(player, rows[i][0], rows[i][1])) {
                    return true;
                }
            }
            return false;
        }

        // Has player won in the row from position[a] to position[b]?
        private boolean testRow(char player, int a, int b) {
            return position[a] == player && position[b] == player && position[(a + b) / 2] == player;
        }

        // Are all 9 spots filled?
        private boolean isDraw() {
            for (int i = 0; i < 9; ++i) {
                if (position[i] == BLANK) {
                    return false;
                }
            }
            return true;
        }

        private void newGame(char winner) {
            repaint();

            String result;
            if (winner == O) {
                ++wins;
                result = "Player O wins!";
            } else if (winner == X) {
                ++losses;
                result = "Player X wins!";
            } else {
                result = "Tie";
                ++draws;
            }
            if (JOptionPane.showConfirmDialog(null,
                    "Player O: " + wins + " \nPlayer X: " + losses + " \nDraws: " + draws + "\nPlay again?", result,
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                System.exit(0);
            }

            for (int j = 0; j < 9; ++j) {
                position[j] = BLANK;
            }

            if ((wins + losses + draws) % 2 == 1) {
                playerO = false;
            }
        }
    }
}
