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
//        String initialNotation = "1. d4  e5 2. e3 Bb4+ 3. Bd2 d6 4. Bb5+  c6  5. Bc4  a6  6. Qf3  a5  7. Qxf7#"; //checks and mate
//        String initialNotation = "1. a4 c6 2. h4 d6 3. Rh3 e6 4. Raa3 Be7 5. a5 Bd7 6. a6 Bf8 7. Ra5 Nh6 8. Rha3 g6 9. R5a4"; //line-ambiguity
//       String initialNotation = "1. g4 h5 2. f4 g5 3. gxh5 f6 4. fxg5 Na6 5. gxf6 Nb8 6. fxe7 Na6 7. h6 d6 8. h7 Kd7 9. exf8Q Nb8 10. hxg8Q Na6 11. Qxh8 Nb8 12. d3 Na6 13. Qd2 Nb8 14. Qd2h6 a6 15. Qh8f6 a5 16. Qf8h8 Qf8 17. Qh6xf8";
//        String initialNotation = "1. h4 a5 2. Na3 a4 3. Rb1 b5 4. Nxb5 a3 5. Ra1 axb2 6. Rb1 bxc1Q 7. Qxc1 Rxa2 8. Na7"; //test
//        String initialNotation = "1. h4 a5 2. Na3 a4 3. Rb1 b5 4. Nxb5 a3 5. Ra1 axb2 6. Rb1 bxc1Q 7. Qxc1 Rxa2 8. Na7 Ba6 9. Rb3 Bc4 10. Rb7 Ra4 11. d3 Bd5 12. Rb5 Ba2 13. Qe3 Be6"; //test
        String initialNotation = "1. h4 a5 2. Na3 a4 3. Rb1 b5 4. Nxb5 a3 5. Ra1 axb2 6. Rb1 bxc1Q 7. Qxc1 Rxa2 8. Na7 Ba6 9. Rb3 Bc4 10. Rb7 Ra4 11. d3 Bd5 12. Rb5 Ba2 13. Qe3 Be6 14. Kd2 f5 15. Rb2 f4 16. Qc5 g5 17. Qc3 Nf6 18. Nb5 Ra1 19. Nxc7+ Kf7 20. Nxe6 dxe6";

        // Run GUI codes on the Event-Dispatcher Thread for thread safety
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Chess(initialNotation); // Let the constructor do the job
            }
        });
    }
}
