package com.mflechl.mfchess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

/**
 * MFChess: A ...
 */

@SuppressWarnings("serial")
public class Chess extends JFrame {
    // Define constants for the various dimensions
    private static final int CANVAS_WIDTH = 640;
    private static final int CANVAS_HEIGHT = 640;
    private static final int BUTTON_HEIGHT = 40;
    //    public static final Color CANVAS_BACKGROUND = Color.CYAN;
    private static final Color CANVAS_BACKGROUND = Color.WHITE;

    private DrawCanvas canvas; // The custom drawing canvas (an inner class extends JPanel)
    private ChessBoard chessBoard;     // the 8x8 tiles
    private CoordBoard coordBoard;     // the coordinates at the edges

    static Notation notation;
    private static JScrollPane scrollPane;
    static JButton btnLastMove;

    String autoComputer = "no";

    // Constructor to set up the GUI components and event handlers
/*
    private Chess(){
        this("");
    }
*/
    private Chess(String initialNotation) {
        // Construct a sprite given x, y, width, height, color
        Color ColorLight = new Color(255, 206, 158);
        Color ColorDark = new Color(209, 139, 71);
        //	chessBoard = new com.mflechl.mfchess.ChessBoard(Color.ORANGE, Color.WHITE);
        chessBoard = new ChessBoard(ColorDark, ColorLight);
        coordBoard = new CoordBoard();

        // Set up a panel for the buttons
        //	JPanel btnPanel = new JPanel(new FlowLayout());

        JPanel emptyPanel1 = new JPanel();
        emptyPanel1.setPreferredSize(new Dimension(CANVAS_WIDTH / 15, BUTTON_HEIGHT));
        JPanel emptyPanel2 = new JPanel();
        emptyPanel2.setPreferredSize(new Dimension(CANVAS_WIDTH / 15, BUTTON_HEIGHT));

        JPanel btnPanel = new JPanel(new GridLayout(1, 0, 0, 0));
        //JPanel btnPanel = new JPanel();
        btnPanel.setPreferredSize(new Dimension(CANVAS_WIDTH, BUTTON_HEIGHT));

        //btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.PAGE_AXIS));
//X        btnPanel.add(new JLabel()); //empty cell

        JButton btnBegin = new JButton("<<");
        btnPanel.add(btnBegin);
        btnBegin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ChessBoard.getBeginState();
                canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });

        JButton btnPrev = new JButton("<");
        btnPanel.add(btnPrev);
        btnPrev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ChessBoard.getPreviousState();
                canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });

        btnLastMove = new JButton("");
        btnPanel.add(btnLastMove);
        btnLastMove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ChessBoard.computerMove();
                //canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
                System.out.println("DDDDDDD");
            }
        });

        JButton btnNext = new JButton(">");
        btnPanel.add(btnNext);
        btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ChessBoard.getNextState();
                canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });
        JButton btnLast = new JButton(">>");
        btnPanel.add(btnLast);
        btnLast.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ChessBoard.getLastState();
                canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });
//X        btnPanel.add(new JLabel()); //empty cell

        // Set up the tiles plus coordinates at the edges
        canvas = new DrawCanvas();
        canvas.setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        canvas.setLayout(new GridLayout(10, 10));

        for (int i = 9; i >= 0; i--) {
            for (int j = 0; j < 10; j++) {
                if (i == 0 || j == 0 || i == 9 || j == 9) {
                    canvas.add(coordBoard.coordTiles[i][j]);
                } else {
                    canvas.add(ChessBoard.tiles[i - 1][j - 1]);
                }
            }
        }

        // Set up panel for notation

        JPanel notationPanel = new JPanel();
        //	notationPanel.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        //	notationPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 5));
        //        notationPanel.setPreferredSize(new Dimension(CANVAS_WIDTH+BUTTON_WIDTH, CANVAS_HEIGHT/10));
        notationPanel.setBackground(Color.WHITE);
        notationPanel.setLayout(new BorderLayout());

        notation = new Notation();
        //	notation.setPreferredSize(new Dimension(CANVAS_WIDTH+BUTTON_WIDTH, CANVAS_HEIGHT/5));
        notationPanel.add(notation, BorderLayout.NORTH);

        scrollPane = new JScrollPane(notationPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(notationPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));

        Dimension d = scrollPane.getPreferredSize();
        d.height = 85;
        scrollPane.setPreferredSize(d);

        // Add panels to the JFrame's content-pane
        Container cp = getContentPane();
        cp.setLayout(new BorderLayout());

        JPanel cButton = new JPanel();
        cButton.setLayout(new BorderLayout());
        cButton.add(emptyPanel1, BorderLayout.LINE_START);
        cButton.add(btnPanel, BorderLayout.CENTER);
        cButton.add(emptyPanel2, BorderLayout.LINE_END);

        cp.add(cButton, BorderLayout.NORTH);
//        cp.add(btnPanel, BorderLayout.NORTH);
        cp.add(canvas, BorderLayout.CENTER);
        //	cp.add(notationPanel, BorderLayout.SOUTH);
        cp.add(scrollPane, BorderLayout.SOUTH);

        // "super" JFrame fires KeyEvent
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                switch (evt.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        ChessBoard.getPreviousState();
                        repaint();
                        break;
                    case KeyEvent.VK_RIGHT:
                        ChessBoard.getNextState();
                        repaint();
                        break;
                    case KeyEvent.VK_UP:
                        ChessBoard.computerMove();
                        repaint();
                        break;
                    case KeyEvent.VK_SPACE:
                        ChessBoard.toggleAutoComputerMove();
                        break;
                    case KeyEvent.VK_C:
                        ChessBoard.toggleOnlyComputerMove();
                        ChessBoard.computerMove();
//                        while (ChessBoard.computerMove() == 0){ repaint(); }
                        while (ChessBoard.computerMove() == 0) {
                            paint(getGraphics());
                        }
                        break;
                }
            }
        });

        /*
        addPropertyChangeListener("title", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                System.out.println("!!! ABC !!!");
                ChessBoard.computerMove();
                setTitle(getTitle()+"1");
            }
        });
        */

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Handle the CLOSE button

        //title and icon
        setTitle("MFChess");
        URL url = ClassLoader.getSystemClassLoader().getResource("chess-icon.png");

        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            setIconImage(icon.getImage());
        }

        pack();           // pack all the components in the JFrame - this triggers the drawing!
        setVisible(true); // show it
        requestFocus();   // set the focus to JFrame to receive KeyEvent

        if (!initialNotation.equals("")) {
            chessBoard.setStateFromNotation(initialNotation);
        }
    }

    /**
     * Define inner class DrawCanvas, which is a JPanel used for custom drawing.
     */
    class DrawCanvas extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(CANVAS_BACKGROUND);
            //	  System.out.println("YY "+scrollPane.getSize().width);
            notation.setHTML(scrollPane.getSize().width * 3 / 4);
            //	  System.out.println( "X " + chessBoard.tiles[0][0].getSize().width );
            chessBoard.setMaxFontSize();
            coordBoard.setMaxFontSize();

        }
    }


    // The entry main() method
    public static void main(String[] args) {
//        String initialNotation = "";
//        String initialNotation = "1. e4 Na6 2. Bxa6 ";  //simple test
//        String initialNotation = "1. a4 g5 2. a5 g4 3. f4 gxf3 4. exf3 b5 5. axb6 a5 "; //en passant
//        String initialNotation = "1. g3 g6 2. Bh3 Bh6 3. Nf3 Nf6 4. 0-0 0-0 "; //castling king-side
//        String initialNotation = "1. d4 d5 2. Bg5 Bg4 3. Na3 Na6 4. Qd3 Qd6 5. 0-0-0 0-0-0"; //castling queen-side
//        String initialNotation = "1. f4 g5 2. fxg5 h6 3. gxh6 a5 4. h7 a4 5. hxg8R a3 6. Nh3 axb2 7. g4 bxa1B "; //promotion white & black
        //String initialNotation = "1. d4  e5 2. e3 Bb4+ 3. Bd2 d6 4. Bb5+  c6  5. Bc4  a6  6. Qf3  a5  7. Qxf7#"; //checks and mate
//        String initialNotation = "1. a4 c6 2. h4 d6 3. Rh3 e6 4. Raa3 Be7 5. a5 Bd7 6. a6 Bf8 7. Ra5 Nh6 8. Rha3 g6 9. R5a4"; //line-ambiguity
//        String initialNotation = "1. g4 h5 2. f4 g5 3. gxh5 f6 4. fxg5 Na6 5. gxf6 Nb8 6. fxe7 Na6 7. h6 d6 8. h7 Kd7 9. exf8Q Nb8 10. hxg8Q Na6 11. Qxh8 Nb8 12. d3 Na6 13. Qd2 Nb8 14. Qd2h6 a6 15. Qh8f6 a5 16. Qf8h8 Qf8 17. Qh6xf8";
//        String initialNotation = "1. h4 a5 2. Na3 a4 3. Rb1 b5 4. Nxb5 a3 5. Ra1 axb2 6. Rb1 bxc1Q 7. Qxc1 Rxa2 8. Na7"; //test
//        String initialNotation = "1. h4 a5 2. Na3 a4 3. Rb1 b5 4. Nxb5 a3 5. Ra1 axb2 6. Rb1 bxc1Q 7. Qxc1 Rxa2 8. Na7 Ba6 9. Rb3 Bc4 10. Rb7 Ra4 11. d3 Bd5 12. Rb5 Ba2 13. Qe3 Be6"; //test
//        String initialNotation = "1. h4 a5 2. Na3 a4 3. Rb1 b5 4. Nxb5 a3 5. Ra1 axb2 6. Rb1 bxc1Q 7. Qxc1 Rxa2 8. Na7 Ba6 9. Rb3 Bc4 10. Rb7 Ra4 11. d3 Bd5 12. Rb5 Ba2 13. Qe3 Be6 14. Kd2 f5 15. Rb2 f4 16. Qc5 g5 17. Qc3 Nf6 18. Nb5 Ra1 19. Nxc7+ Kf7 20. Nxe6 dxe6 21. Rxb8";
//        String initialNotation = "1. d4 Nf6 2. c4 g6 3. Nc3 Bg7 4. Nf3 d6 5. g3 0-0 6. Bg2 c5 7. 0-0 Nc6 8. d5 Nb8 9. Qa4 h6 10. Ne1 g5 11. Qa3 g4 12. Be3 h5 13. Bc1 b6 14. Bd2 Qe8 15. Bc1 Bh8 16. Bh6 Bg7 17. Be3 Bd7 18. Kh1 a5 19. Nc2 Qd8 20. Rab1 Bf5 21. Rfc1 Qc7 22. Nb5 Qd7 23. Nc3 Ne8 24. Kg1 Nf6 25. Ra1 Ne8 26. Ne1 Qa7 27. Nb5 Qd7 28. Kf1 Kh8 29. Kg1 Be5 30. Kf1 Kg8 31. Bh6 Bxb2 32. Qxb2 Ng7 33. Bxg7 Re8 34. e3 f6 35. Bh6 Kh7 36. Bf4 e5 37. dxe6 Bd3+ 38. Nxd3 Rxe6 39. Bxa8 Qd8 40. Bxd6 Nd7 41. Bc6 Nb8 42. Bd5 Rxd6 43. Be4+ Kh6 44. Nxc5 Rd2 45. Qc3 bxc5 46. Bf5 a4 47. a3 Kg5 48. Be6 Kg6 49. Ke1 Kg5 50. Qxd2 Qf8 51. Rcb1 Kg6 52. Rd1 Nc6 53. Qd3+ Kg7 54. Qd7+ Qe7 55. Qxc6 Kh6 56. Rd5 Kg7 57. Rd7 Kf8 58. Qa8+ Qe8 59. Rf7+ Kg8 60. Rxf6+ Kg7 61. Qxe8 Kxf6 62. Rb1 Kg5 63. Qc6 Kg6 64. Qxc5 Kh6 65. Qf8+ Kh7 66. Kf1 Kg6 67. Qf7+ Kg5 68. Nd4 Kh6 69. Qe8 Kh7 70. Qxh5+ Kg7 71. Kg2 Kf6 72. e4 Kg7 73. Bd7 Kf6 74. Qxg4 Ke5 75. Nc2 Kd6 76. Qd1+ Kc5 77. Qe2 Kd6 78. Qg4 Kc5 79. Bb5 Kd6 80. Be8 Kc5 81. Qg8 Kd6 82. Qg5 Ke6 83. Rh1 Kd6 84. Qc1 Ke5 85. f3 Kd6 86. Nb4 Kc7 87. Rd1 Kb7 88. Rh1 Ka7 89. Qg5 Kb7 90. Qe7+ Kc8 91. Qf8 Kd8 92. Rg1 Kc7 93. Nd5+ Kd8 94. Qf7 Kc8 95. Qf4 Kb7 96. Kf2 Ka8 97. Qh6 Ka7 98. Qh5 Ka8 99. Bd7 Kb8 100. Rg2 Kb7 101. Ke1 Ka6 102. Nc7+ Kb7 103. Qc5 Kb8";
        String initialNotation = "1. d4 e5 2. e3 Bb4+ 3. Bd2 d6 4. Bb5+ c6 5. Bxb4 cxb5 6. Qd3 Qb6 7. Qe4 f5 8. Qh4 exd4 9. exd4 h5 10. Na3 Qc6 11. f3 Be6 12. Bc3 Bc8 13. Kf1 Be6 14. Ke1 Bf7 15. Qf4 Ne7 16. Qh4 Rh7 17. Qf4 Nd5 18. Qxf5 Nxc3 19. Qxh7 Kd8 20. bxc3 g5 21. Qxf7 Qxc3+ 22. Kf2 Qd2+ 23. Ne2 b6 24. d5 Nd7 25. Rag1 a6 26. Rf1 Kc7 27. Qf5 Kb8 28. Rd1 Qb4 29. Nxb5 Qxb5 30. Qxg5 Nf6 31. Nd4 Ng4+ 32. fxg4 Qe8 33. Qxh5 Qf8+ 34. Ke2 Kb7 35. c4 Ra7 36. Nc6 Kc7 37. Qh7+ Kc8 38. Qxa7 Qe8+ 39. Ne7+ Kd8 40. Qxb6+ Kxe7 41. Kd2 Kd7 42. h3 Qa8 43. Rb1 Kc8 44. Rbf1 Kd7 45. Rd1 Kc8 46. g3 a5 47. a3 a4 48. Qxd6 Qa5+ 49. Ke2 Qc7 50. Qc6 Kd8 51. Qb5 Qe7+ 52. Kd2 Qg5+ 53. Ke2 Qe5+ 54. Kd2 Qg5+ 55. Kd3 Qg6+ 56. Kc3 Qg7+ 57. Rd4 Qa7 58. Rf1 Qc7 59. Rf8+ Ke7 60. d6+ Kxf8 61. dxc7 Ke7 62. c8Q Kf6 63. Qcd7";
        // Run GUI codes on the Event-Dispatcher Thread for thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Chess(initialNotation); // Let the constructor do the job
            }
        });
    }
}
