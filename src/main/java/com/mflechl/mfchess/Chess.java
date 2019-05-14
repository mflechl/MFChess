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
public class Chess extends JFrame  {
    // Define constants for the various dimensions
    private static final int CANVAS_WIDTH = 640;
    private static final int CANVAS_HEIGHT = 640;
    private static final int BUTTON_HEIGHT = 100;
    //    public static final Color CANVAS_BACKGROUND = Color.CYAN;
    private static final Color CANVAS_BACKGROUND = Color.WHITE;

    static final boolean COMPUTER_PLAY = true;

    private DrawCanvas canvas; // The custom drawing canvas (an inner class extends JPanel)
    private ChessBoard chessBoard;     // the 8x8 tiles
    private CoordBoard coordBoard;     // the coordinates at the edges

    static Notation notation;
    private static JScrollPane scrollPane;
    static JButton btnLastMove;
    static JButton btnNextMoves;

    // Constructor to set up the GUI components and event handlers
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

        //X JPanel btnPanel = new JPanel(new GridLayout(1, 0, 0, 0));
        JPanel btnPanel = new JPanel(new GridBagLayout());
        btnPanel.setPreferredSize(new Dimension(CANVAS_WIDTH, BUTTON_HEIGHT));

        GridBagConstraints c = new GridBagConstraints();
//        c.fill = GridBagConstraints.HORIZONTAL;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.gridx = 0;
        c.gridy = 0;
        c.ipady = 10;

        JButton btnBegin = new JButton("<<");
        btnPanel.add(btnBegin,c);
        btnBegin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ChessBoard.getBeginState();
                canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });

        c.gridx = 1;
        JButton btnPrev = new JButton("<");
        btnPanel.add(btnPrev,c);
        btnPrev.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                chessBoard.getPreviousState();
                canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });

        c.gridx = 2;
        btnLastMove = new JButton("1.");
        btnPanel.add(btnLastMove,c);
        btnLastMove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                chessBoard.computerMove();
                //canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });

        c.gridx = 3;
        JButton btnNext = new JButton(">");
        btnPanel.add(btnNext,c);
        btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                chessBoard.getNextState();
                canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });
        c.gridx = 4;
        JButton btnLast = new JButton(">>");
        btnPanel.add(btnLast,c);
        btnLast.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ChessBoard.getLastState();
                canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });
//X        btnPanel.add(new JLabel()); //empty cell

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 5;
        btnNextMoves = new JButton("-");
        btnPanel.add(btnNextMoves,c);
        btnNextMoves.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //chessBoard.computerMove();
                //canvas.repaint();
                requestFocus(); // change the focus to JFrame to receive KeyEvent
            }
        });

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
                        chessBoard.getPreviousState();
                        repaint();
                        break;
                    case KeyEvent.VK_RIGHT:
                        chessBoard.getNextState();
                        repaint();
                        break;
                    case KeyEvent.VK_UP:
                        chessBoard.computerMove();
                        repaint();
                        break;
                    case KeyEvent.VK_SPACE:
                        ChessBoard.toggleAutoComputerMove();
                        break;
                    case KeyEvent.VK_C:
                        ChessBoard.toggleOnlyComputerMove();
                        computerPlay();
                        /*
                        chessBoard.computerMove();
//                        while (ChessBoard.computerMove() == 0){ repaint(); }
                        while (chessBoard.computerMove() == 0) {
                            paint(getGraphics());
                        }
                        */
                        break;
                    case KeyEvent.VK_E:
                        System.out.println( "EVAL current board: " + EvaluateBoard.eval(ChessBoard.iBoard, ChessBoard.currentStaticState) );
                        break;
                }
            }
        });

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

        System.out.println("eval init = " + EvaluateBoard.eval(ChessBoard.iBoard,ChessBoard.currentStaticState) );

        if (!initialNotation.equals("")) {
            chessBoard.setStateFromNotation(initialNotation);
        }

        if (COMPUTER_PLAY) computerPlay();
        else chessBoard.findDeeperMove( Move.DEFAULT_START_DEPTH, false );

    }

    void computerPlay(){
        long startTime = System.currentTimeMillis();
//            while (chessBoard.computerMove() >= 0) {c
        boolean moveReturn = true;
        while (moveReturn) {
            chessBoard.computerMove();

            try { ChessBoard.moveThread.join(); }
            catch (InterruptedException e) { e.printStackTrace(); }

            if ( ChessBoard.currentBestMove != null ) ChessBoard.updateMove();
            paint(getGraphics());

            if (ChessBoard.currentStaticState.mate) moveReturn = false;
            else if (ChessBoard.currentStaticState.remis) moveReturn = false;
            else if (ChessBoard.currentStaticState.nMoves > 80) moveReturn = false;
        }
        ChessBoard.moveThread.move.stopBestMove = true;

        try { ChessBoard.moveThread.join(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        System.out.println("VVV "+ChessBoard.moveThread.getState());

        //System.out.println(ChessBoard.iBoard);

        long finishTime = System.currentTimeMillis();
        System.out.println("The game took: " + (finishTime - startTime) + " ms   nALMCalls =" + Move.nALMCalls + "  number of moves = " + ChessBoard.currentStaticState.nMoves);

    }

    //void close() { dicspose(); //Chess.dispatchEvent(new WindowEvent(chess, WindowEvent.WINDOW_CLOSING)); }

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

    //TODO: weights: give some weight also to board after next move to break ties (promote now, not later!)
    //TODO: check for three-times repetition ( Arrays.hashCode(myArray); )
    //TODO: Quiescence Searching
    //TODO: improve evaluation
    //TODO: sorting for alphabeta
    //TODO: computerPlay: update notation; and update rest of board properly
    //TODO: pick shortest path to mate, they are not equivalent

    // The entry main() method
    public static void main(String[] args) {
        //String initialNotation = "";
        //String initialNotation = "1. e4 Nc6 2. Nc3 e6 3. Nf3 Qf6 4. Bc4 Kd8 5. 0-0 Bb4 6. d4 h6 7. Bb5 Nce7 8. e5 Qf5 9. Ng5 Nd5 10. Nxd5 Be1 11. Bd3 Qxg5 12. Bxg5+ hxg5 13. Nxc7 Kxc7 14. Qxe1 f6 15. Qa5+ b6 16. Qc3+ Kd8 17. Be4 Rb8 18. Qa3 fxe5 19. Qxa7 Nf6 20. Qxb8 Nxe4 21. dxe5 b5 22. Qxb5 Ke8 23. Qb8 Kf7 24. Rfe1 Nc5 25. Rad1 Na6 26. Qb6 Ke8 27. Rd4 Kf7 28. Rc4 Re8 29. Rg4 Kg8 30. Rxg5 Kh7 31. Rg4 Rf8 32. Rc4 d5 33. exd6 e5 34. Rxe5 Kg8 35. Qb3 Kh7 36. Re7 Bf5 37. Rh4+ Kg6 38. Qg3+"; //close to mate
          String initialNotation = "1. h4"; //avoid openings
//test if CM continues after mate          String initialNotation = "1. h4 e5 2. f3 d5 3. c3 Bd7 4. e3 Qf6 5. Qb3 Qg6 6. Qxb7 Ba3 7. Qxa8 Qg3+ 8. Ke2 Ke7 9. d4 a5 10. c4 Qe1+ 11. Kxe1 Bc6 12. Qxb8 Bb4+ 13. Ke2 Bd6 14. dxe5 Bxe5 15. f4 dxc4 16. fxe5 a4 17. b3 f6 18. exf6+ Kd7 19. fxg7 cxb3 20. gxh8Q bxa2 21. Qxh7+ Ne7 22. Rxa2 Ke6 23. Bd2 Bd5 24. Rxa4 c6 25. Ra1 Bc4+ 26. Ke1 Bxf1 27. Kxf1 Nd5 28. Nc3 Nf6 29. Qc8+";
//        String initialNotation = "1. h4 g6 2. b3 Bg7 3. Nc3 g5 4. hxg5 Bd4 5. e3 Bg7 6. d4 b6 7. Bb5 Kf8 8. Qd3 Qe8 9. Qe2 Qd8 10. Qd1 a5 11. Qf3 c6 12. Na4 Qe8 13. Nxb6 Ra7 14. Nxc8 Qxc8 15. Bf1 d6 16. Be2 Qc7 17. Rh5 Nd7 18. Bc4 d5 19. Bf1 Bxd4 20. exd4 Ndf6 21. Bf4 Qc8 22. Be5 Qb7 23. a3 Nh6 24. Rxh6 Ng8 25. Bxh8 Nxh6 26. gxh6 Kg8 27. Bg7 c5 28. dxc5 a4 29. b4 Ra5 30. bxa5 Qb8 31. Qh5 f5 32. Qh4 f4 33. 0-0-0 f3 34. gxf3 e6 35. Qxa4 d4 36. Qxd4 Qc7 37. a6 Qb8 38. Re1 Qc7 39. Re5 Qb8 40. Qe4 Qd8 41. c6 Qe7 42. Kb2 Qd8 43. Qb4 Kf7 44. Bd3 Kg8 45. Re1 Qc8 46. Qb5 Qd8 47. a7 Qa8 48. Qb8+ Qxb8+ 49. axb8Q+ Kf7 50. Qc7+ Kg8 51. Re5 &#189; - &#189;";
//        String initialNotation = "1. e4 Na6 2. Bxa6 ";  //simple test
//        String initialNotation = "1. a4 g5 2. a5 g4 3. f4 gxf3 4. exf3 b5 5. axb6 a5 "; //en passant
//        String initialNotation = "1. g3 g6 2. Bh3 Bh6 3. Nf3 Nf6 4. 0-0 0-0 "; //castling king-side
//        String initialNotation = "1. d4 d5 2. Bg5 Bg4 3. Na3 Na6 4. Qd3 Qd6 5. 0-0-0 0-0-0"; //castling queen-side
//        String initialNotation = "1. f4 g5 2. fxg5 h6 3. gxh6 a5 4. h7 a4 5. hxg8R a3 6. Nh3 axb2 7. g4 bxa1B "; //promotion white & black
//        String initialNotation = "1. d4  e5 2. e3 Bb4+ 3. Bd2 d6 4. Bb5+  c6  5. Bc4  a6  6. Qf3  a5  7. Qxf7# 1-0"; //checks and mate
//          String initialNotation = "1. e3 a5 2. Qh5 Ra6 3. Qxa5 h5 4. h4 Rah6 5. Qxc7 f6 6. Qxd7+ Kf7 7. Qxb7 Qd3 8. Qxb8 Qh7 9. Qxc8 Kg6 10. Qe6"; //remis (fastest)
//        String initialNotation = "1. a4 c6 2. h4 d6 3. Rh3 e6 4. Raa3 Be7 5. a5 Bd7 6. a6 Bf8 7. Ra5 Nh6 8. Rha3 g6 9. R5a4"; //line-ambiguity
//        String initialNotation = "1. g4 h5 2. f4 g5 3. gxh5 f6 4. fxg5 Na6 5. gxf6 Nb8 6. fxe7 Na6 7. h6 d6 8. h7 Kd7 9. exf8Q Nb8 10. hxg8Q Na6 11. Qxh8 Nb8 12. d3 Na6 13. Qd2 Nb8 14. Qd2h6 a6 15. Qh8f6 a5 16. Qf8h8 Qf8 17. Qh6xf8";
//        String initialNotation = "1. h4 a5 2. Na3 a4 3. Rb1 b5 4. Nxb5 a3 5. Ra1 axb2 6. Rb1 bxc1Q 7. Qxc1 Rxa2 8. Na7"; //test
//        String initialNotation = "1. h4 a5 2. Na3 a4 3. Rb1 b5 4. Nxb5 a3 5. Ra1 axb2 6. Rb1 bxc1Q 7. Qxc1 Rxa2 8. Na7 Ba6 9. Rb3 Bc4 10. Rb7 Ra4 11. d3 Bd5 12. Rb5 Ba2 13. Qe3 Be6"; //test
//        String initialNotation = "1. h4 a5 2. Na3 a4 3. Rb1 b5 4. Nxb5 a3 5. Ra1 axb2 6. Rb1 bxc1Q 7. Qxc1 Rxa2 8. Na7 Ba6 9. Rb3 Bc4 10. Rb7 Ra4 11. d3 Bd5 12. Rb5 Ba2 13. Qe3 Be6 14. Kd2 f5 15. Rb2 f4 16. Qc5 g5 17. Qc3 Nf6 18. Nb5 Ra1 19. Nxc7+ Kf7 20. Nxe6 dxe6 21. Rxb8";
//        String initialNotation = "1. d4 Nf6 2. c4 g6 3. Nc3 Bg7 4. Nf3 d6 5. g3 0-0 6. Bg2 c5 7. 0-0 Nc6 8. d5 Nb8 9. Qa4 h6 10. Ne1 g5 11. Qa3 g4 12. Be3 h5 13. Bc1 b6 14. Bd2 Qe8 15. Bc1 Bh8 16. Bh6 Bg7 17. Be3 Bd7 18. Kh1 a5 19. Nc2 Qd8 20. Rab1 Bf5 21. Rfc1 Qc7 22. Nb5 Qd7 23. Nc3 Ne8 24. Kg1 Nf6 25. Ra1 Ne8 26. Ne1 Qa7 27. Nb5 Qd7 28. Kf1 Kh8 29. Kg1 Be5 30. Kf1 Kg8 31. Bh6 Bxb2 32. Qxb2 Ng7 33. Bxg7 Re8 34. e3 f6 35. Bh6 Kh7 36. Bf4 e5 37. dxe6 Bd3+ 38. Nxd3 Rxe6 39. Bxa8 Qd8 40. Bxd6 Nd7 41. Bc6 Nb8 42. Bd5 Rxd6 43. Be4+ Kh6 44. Nxc5 Rd2 45. Qc3 bxc5 46. Bf5 a4 47. a3 Kg5 48. Be6 Kg6 49. Ke1 Kg5 50. Qxd2 Qf8 51. Rcb1 Kg6 52. Rd1 Nc6 53. Qd3+ Kg7 54. Qd7+ Qe7 55. Qxc6 Kh6 56. Rd5 Kg7 57. Rd7 Kf8 58. Qa8+ Qe8 59. Rf7+ Kg8 60. Rxf6+ Kg7 61. Qxe8 Kxf6 62. Rb1 Kg5 63. Qc6 Kg6 64. Qxc5 Kh6 65. Qf8+ Kh7 66. Kf1 Kg6 67. Qf7+ Kg5 68. Nd4 Kh6 69. Qe8 Kh7 70. Qxh5+ Kg7 71. Kg2 Kf6 72. e4 Kg7 73. Bd7 Kf6 74. Qxg4 Ke5 75. Nc2 Kd6 76. Qd1+ Kc5 77. Qe2 Kd6 78. Qg4 Kc5 79. Bb5 Kd6 80. Be8 Kc5 81. Qg8 Kd6 82. Qg5 Ke6 83. Rh1 Kd6 84. Qc1 Ke5 85. f3 Kd6 86. Nb4 Kc7 87. Rd1 Kb7 88. Rh1 Ka7 89. Qg5 Kb7 90. Qe7+ Kc8 91. Qf8 Kd8 92. Rg1 Kc7 93. Nd5+ Kd8 94. Qf7 Kc8 95. Qf4 Kb7 96. Kf2 Ka8 97. Qh6 Ka7 98. Qh5 Ka8 99. Bd7 Kb8 100. Rg2 Kb7 101. Ke1 Ka6 102. Nc7+ Kb7 103. Qc5 Kb8";
//        String initialNotation = "1. d4 e5 2. e3 Bb4+ 3. Bd2 d6 4. Bb5+ c6 5. Bxb4 cxb5 6. Qd3 Qb6 7. Qe4 f5 8. Qh4 exd4 9. exd4 h5 10. Na3 Qc6 11. f3 Be6 12. Bc3 Bc8 13. Kf1 Be6 14. Ke1 Bf7 15. Qf4 Ne7 16. Qh4 Rh7 17. Qf4 Nd5 18. Qxf5 Nxc3 19. Qxh7 Kd8 20. bxc3 g5 21. Qxf7 Qxc3+ 22. Kf2 Qd2+ 23. Ne2 b6 24. d5 Nd7 25. Rag1 a6 26. Rf1 Kc7 27. Qf5 Kb8 28. Rd1 Qb4 29. Nxb5 Qxb5 30. Qxg5 Nf6 31. Nd4 Ng4+ 32. fxg4 Qe8 33. Qxh5 Qf8+ 34. Ke2 Kb7 35. c4 Ra7 36. Nc6 Kc7 37. Qh7+ Kc8 38. Qxa7 Qe8+ 39. Ne7+ Kd8 40. Qxb6+ Kxe7 41. Kd2 Kd7 42. h3 Qa8 43. Rb1 Kc8 44. Rbf1 Kd7 45. Rd1 Kc8 46. g3 a5 47. a3 a4 48. Qxd6 Qa5+ 49. Ke2 Qc7 50. Qc6 Kd8 51. Qb5 Qe7+ 52. Kd2 Qg5+ 53. Ke2 Qe5+ 54. Kd2 Qg5+ 55. Kd3 Qg6+ 56. Kc3 Qg7+ 57. Rd4 Qa7 58. Rf1 Qc7 59. Rf8+ Ke7 60. d6+ Kxf8 61. dxc7 Ke7 62. c8Q Kf6 63. Qcd7";
//        String initialNotation = "1. h4 h6 2. c3 g6 3. Qb3 Bg7 4. c4 h5 5. f3 c6 6. Qa4 c5 7. Qa3 Bd4 8. e3 Qc7 9. Kd1 Bf6 10. d3 a5 11. Kd2 Kd8 12. Be2 Ra7 13. Kc2 Ra8 14. Rh3 Ke8 15. Qb3 Qd6 16. Nd2 e5 17. Bf1 Kf8 18. e4 b6 19. Kb1 Kg7 20. Kc2 Qe6 21. Qb5 Ba6 22. Qb3 Bxc4 23. Nxc4 Kh7 24. Kb1 a4 25. Qb5 d5 26. Nxb6 d4 27. Nxa8 g5 28. Nc7 Qc8 29. Nd5 Kg6 30. Nxf6 Qe6 31. Nxg8 Rxg8 32. Qb7 Nd7 33. hxg5 Rd8 34. Qc7 f6 35. Qxd8 h4 36. f4 exf4 37. Rxh4 fxg5 38. Rh1 Ne5 39. Qa5 a3 40. Qxa3 Nd7 41. Qa5 g4 42. Bxf4 Kf6 43. Be2 g3 44. Qa7 Nf8 45. Qxc5 Kf7 46. Qxd4 Qe8 47. Qe3 Qa8 48. Nf3 Kg8 49. Rf1 Qa6 50. d4 Qe6 51. Rc1 Ng6 52. Bxg3 Nf4 53. Qxf4 Qb6 54. d5 Qb4 55. Ne5 Qa5 56. Qf5 Kh8 57. b3 Kg7 58. Re1 Qb6 59. Qf2 Qd8 60. Qe3 Qe7 61. Nf3 Qb4 62. Bh4 Qd6 63. Bc4 Kf8 64. Qc3 Kg8 65. Bb5 Qb8 66. Be2 Qe8 67. Ng5 Qd7 68. Qf6 Qa7 69. Qf7+ Qxf7 70. Nxf7 Kxf7 71. g4 Ke8 72. Bf6 Kf8 73. Bg5 Kf7 74. Rf1+ Kg6 75. Bd8 Kh6 76. a3 Kg7 77. Bd1 Kh6 78. Rg1 Kg6 79. Rf1 Kh6 80. Be7 Kg7 81. Re1 Kh7 82. Rh1+ Kg8 83. g5 Kf7 84. d6 Kg7 85. e5 Kf7 86. Rg1 Ke6 87. Re1 Kd7 88. Rh1 Kc8 89. Bg4+ Kb7 90. e6 Kb8 91. Rd1 Kb7 92. Rc1 Ka7 93. Bd8 Ka6 94. Rc3 Kb7 95. e7"; //chance for promotion
//        String initialNotation = "1. d4 Nf6 2. c4 g6 3. Nc3 Bg7 4. Nf3 d6 5. g3 0-0 6. Bg2 c5 7. 0-0 Nc6 8. d5 Nb8 9. Qa4 h6 10. Ne1 g5 11. Qa3 g4 12. Be3 h5 13. Bc1 b6 14. Bd2 Qe8 15. Bc1 Bh8 16. Bh6 Bg7 17. Be3 Bd7 18. Kh1 a5 19. Nc2 Qd8 20. Rab1 Bf5 21. Rfc1 Qc7 22. Nb5 Qd7 23. Nc3 Ne8 24. Kg1 Nf6 25. Ra1 Ne8 26. Ne1 Qa7 27. Nb5 Qd7 28. Kf1 Kh8 29. Kg1 Be5 30. Kf1 Kg8 31. Bh6 Bxb2 32. Qxb2 Ng7 33. Bxg7 Re8 34. e3 f6 35. Bh6 Kh7 36. Bf4 e5 37. dxe6 Bd3+ 38. Nxd3 Rxe6 39. Bxa8 Qd8 40. Bxd6 Nd7 41. Bc6 Nb8 42. Bd5 Rxd6 43. Be4+ Kh6 44. Nxc5 Rd2 45. Qc3 bxc5 46. Bf5 a4 47. a3 Kg5 48. Be6 Kg6 49. Ke1 Kg5 50. Qxd2 Qf8 51. Rcb1 Kg6 52. Rd1 Nc6 53. Qd3+ Kg7 54. Qd7+ Qe7 55. Qxc6 Kh6 56. Rd5 Kg7 57. Rd7 Kf8 58. Qa8+ Qe8 59. Rf7+ Kg8 60. Rxf6+ Kg7 61. Qxe8 Kxf6 62. Rb1 Kg5 63. Qc6 Kg6 64. Qxc5 Kh6 65. Qf8+ Kh7 66. Kf1 Kg6 67. Qf7+ Kg5 68. Nd4 Kh6 69. Qe8 Kh7 70. Qxh5+ Kg7 71. Kg2 Kf6 72. e4 Kg7 73. Bd7 Kf6 74. Qxg4 Ke5 75. Nc2 Kd6 76. Qd1+ Kc5 77. Qe2 Kd6 78. Qg4 Kc5 79. Bb5 Kd6 80. Be8 Kc5 81. Qg8 Kd6 82. Qg5 Ke6 83. Rh1 Kd6 84. Qc1 Ke5 85. f3 Kd6 86. Nb4 Kc7 87. Rd1 Kb7 88. Rh1 Ka7 89. Qg5 Kb7 90. Qe7+ Kc8 91. Qf8 Kd8 92. Rg1 Kc7 93. Nd5+ Kd8 94. Qf7 Kc8 95. Qf4 Kb7 96. Kf2 Ka8 97. Qh6 Ka7 98. Qh5 Ka8 99. Bd7 Kb8 100. Rg2 Kb7 101. Ke1 Ka6 102. Nc7+ Kb7 103. Qc5 Kb8 104. Qa7+ Kxa7 105. Na8 Kxa8 106. Kd2 Kb7 107. Kc3 Kc7 108. Kb4 Kxd7 109. Kxa4 Kd6 110. c5+ Kxc5 111. Ka5 Kc4 112. Kb6 Kb3 113. a4 Kxa4 114. h3 Kb4 115. Kc6 Kc4 116. Kd6 Kd4 117. Ke6 Ke3 118. Kf6 Kxf3 119. Kg6 Kxe4 120. Kg5 Kf3 121. Re2 Kxg3 122. Kf5 Kxh3 123. Kf4 Kh4 124. Rf2 Kh5 125. Rd2 Kh6 126. Re2 Kh5 127. Rf2 Kh4";

        // Run GUI codes on the Event-Dispatcher Thread for thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Chess(initialNotation); // Let the constructor do the job
            }
        });
    }
}
