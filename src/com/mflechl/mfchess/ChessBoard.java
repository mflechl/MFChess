package com.mflechl.mfchess;

import java.awt.*;   // Using AWT's Graphics and Color
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.awt.image.BufferedImage;
import java.awt.font.*;
import java.awt.geom.*;

//import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.util.*;


/**
 *
 *
 */
public class ChessBoard implements ActionListener {
    private Color color1; // Color of square 1
    private Color color2; // Color of square 2

    private static IBoard iBoard = new IBoard();
    static IBoard hypo_iBoard = new IBoard();
    static Tile[][] tiles = new Tile[8][8];

    static final int KING = 1, QUEEN = 2, ROOK = 3, BISHOP = 4, KNIGHT = 5, PAWN = 6; //TODO: enum
    private static final String[] upieces = {"", "\u2654", "\u2655", "\u2656", "\u2657", "\u2658", "\u2659"};
    //static final String[] bupieces = {  "", "\u265A", "\u265B",  "\u265C", "\u265D",   "\u265E",   "\u265F" };
    static final String[] lpieces = {"", "K", "Q", "R", "B", "N", "P"};
    private static final int WHITE = +1, BLACK = -1;
    static ImageIcon[] wpieces = new ImageIcon[7];
    static ImageIcon[] bpieces = new ImageIcon[7];

    static State state = new State();

    private static Font font = Tile.font;
    private static final Color outlineColor = Color.DARK_GRAY;
    private static final Color[] pieceColors = {
            new Color(213, 213, 187), new Color(80, 80, 80)
            //        new Color(203, 203, 197), new Color(192, 142, 60)
    };

    // Constructor
    ChessBoard(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
        initBoard();
    }

    void setMaxFontSize() {
        float mindim = Math.min(tiles[0][0].getSize().height, tiles[0][0].getSize().width);
        setFontSize(mindim * (float) 0.9);
        this.initPieces();
        for (int i = 0; i < iBoard.setup.length; i++) {
            for (int j = 0; j < iBoard.setup[i].length; j++) {
                tiles[i][j].setPiece(iBoard.setup[i][j]);
            }
        }

    }

    private void setFontSize(float fs) {
/*        for (Tile[] tiles : tiles) {
            for (Tile tile : tiles) {
                tile.setFontSize(fs);
            }
        }*/
        tiles[0][0].setFontSize(fs);
        font=Tile.font;
    }

    /* public float getFontSize() {
	return tiles[0][0].getFontSize();
    }*/

    private void initPieces() {
        for (int i = 1; i < 7; i++) {
            final boolean useGradient = true;
            wpieces[i] = new ImageIcon(getImageForChessPiece(i, 0, useGradient));
            bpieces[i] = new ImageIcon(getImageForChessPiece(i, 1, useGradient));
        }
    }

    private void initBoard() {

        initPieces();

        for (int j = 0; j < iBoard.setup[1].length; j++) {
            iBoard.setup[1][j] = WHITE * PAWN;
            iBoard.setup[6][j] = BLACK * PAWN;
            iBoard.setup[6][j] = BLACK * PAWN;
        }
        iBoard.setup[0][0] = WHITE * ROOK;
        iBoard.setup[0][7] = WHITE * ROOK;
        iBoard.setup[7][0] = BLACK * ROOK;
        iBoard.setup[7][7] = BLACK * ROOK;

        iBoard.setup[0][1] = WHITE * KNIGHT;
        iBoard.setup[0][6] = WHITE * KNIGHT;
        iBoard.setup[7][1] = BLACK * KNIGHT;
        iBoard.setup[7][6] = BLACK * KNIGHT;

        iBoard.setup[0][2] = WHITE * BISHOP;
        iBoard.setup[0][5] = WHITE * BISHOP;
        iBoard.setup[7][2] = BLACK * BISHOP;
        iBoard.setup[7][5] = BLACK * BISHOP;

        iBoard.setup[0][3] = WHITE * QUEEN;
        iBoard.setup[0][4] = WHITE * KING;
        iBoard.setup[7][3] = BLACK * QUEEN;
        iBoard.setup[7][4] = BLACK * KING;

        for (int i = 0; i < iBoard.setup.length; i++) {
            for (int j = 0; j < iBoard.setup[i].length; j++) {
                //		tiles[i][j] = new com.mflechl.mfchess.Tile(new ImageIcon(getImageForChessPiece(2, 0, false)),i,j);
                tiles[i][j] = new Tile("", i, j);
                tiles[i][j].addActionListener(this);
                tiles[i][j].setPiece(iBoard.setup[i][j]);
                if ((i + j) % 2 == 0) tiles[i][j].setBackground(color1);
                else tiles[i][j].setBackground(color2);
            }
        }

    }

    private boolean tileActive = false;
    private int aLine = -1;
    private int aRow = -1;

    private void changeBoardState(int _l, int _r) {
        //TODO: Special cases - check mate, promotion: ask for piece; notation: origin field if ambiguous

        //piece of right color?; change active field if needed
        if (tiles[_l][_r].getPiece() * state.turnOf > 0) {
            if (aLine >= 0 && aRow >= 0) tiles[aLine][aRow].inactive();
            tiles[_l][_r].active();
            aLine = _l;
            aRow = _r;
            tileActive = true;
        }
        //there is an active tile
        else if (tileActive) {
            if (_l == aLine && _r == aRow) {  //clicking on active tile: make it inactive
                tiles[aLine][aRow].inactive();
                aLine = -1;
                aRow = -1;
                tileActive = false;
            } else { //not clicking on active tile: move?

                SpecialMove sMove = new SpecialMove(); //save state: castling, en passant, ...
                if (!Move.isLegal(iBoard, sMove, aLine, aRow, _l, _r)) return;

                //Move!
                int movingPiece = tiles[aLine][aRow].getPiece();
                int eliminatedPiece = tiles[_l][_r].getPiece();

                //promotion ?
                int toPiece = movingPiece;
                if ((movingPiece == BLACK * PAWN && _l == 0) || (movingPiece == WHITE * PAWN && _l == 7)) {
                    toPiece = (int) Math.signum(movingPiece) * QUEEN; //TODO: choose piece
                }
                processMove(aLine, aRow, _l, _r, toPiece, false, sMove);
                updateCastlingState(movingPiece, aLine, aRow, sMove.castling);

                state.check = Move.isChecked(iBoard, state.turnOf * -1); //check of opponent result of the move?

                Chess.notation.addMove(state.moveNumber, aLine, aRow, _l, _r, movingPiece, eliminatedPiece, sMove.enPassant, sMove.castling, state.check);

                ArrayList<IBoard> moveList= Move.allLegalMoves(iBoard);
                System.out.println("move list 0 item  "+moveList.get(0).setup[0][0]);

                //update state
                state.turnOf *= -1;
                if (state.turnOf == WHITE) state.moveNumber++;
                if (Math.abs(movingPiece) == PAWN && Math.abs(aLine - _l) == 2) state.enPassantPossible = aRow;
                else state.enPassantPossible = -1;

                tiles[aLine][aRow].inactive();
                aLine = -1;
                aRow = -1;
                tileActive = false;

            }
        }

    }

    //moving piece
    private void updateCastlingState(int piece, int fromLine, int fromRow, boolean castlingDone) {
        int colIndex = 0; //black
        if (piece > 0) colIndex = 1; //white

        if (!state.castlingPossibleQ[colIndex] && !state.castlingPossibleK[colIndex]) return;

        if (Math.abs(piece) == KING || castlingDone) { //king was moved (incl. castling happened)
            state.castlingPossibleQ[colIndex] = false;
            state.castlingPossibleK[colIndex] = false;
        } else if (Math.abs(piece) == ROOK) { //rook was moved - only need to catch first move
            if (fromLine == (7 * (1 - colIndex)) && fromRow == 0) state.castlingPossibleQ[colIndex] = false;
            if (fromLine == (7 * (1 - colIndex)) && fromRow == 7) state.castlingPossibleK[colIndex] = false;
        }
    }

    //hypo=check hypothetical move, do not execute
    static void processMove(int fromLine, int fromRow, int toLine, int toRow, int piece, boolean hypo, SpecialMove sMove) {

        setPieceBoard(toLine, toRow, piece, hypo);
        setPieceBoard(fromLine, fromRow, 0, hypo);

        if (sMove.enPassant) setPieceBoard(toLine - (int) Math.signum(piece), toRow, 0, hypo);
        if (sMove.castling) {
            int rookToRow = -1, rookFromRow = -1;
            if (toRow == 2) {
                rookToRow = 3;
                rookFromRow = 0;
            }
            if (toRow == 6) {
                rookToRow = 5;
                rookFromRow = 7;
            }
            setPieceBoard(toLine, rookToRow, (int) Math.signum(piece) * ROOK, hypo);
            setPieceBoard(toLine, rookFromRow, 0, hypo);
        }
    }

    //set both iBoard int array and the content of the correspond tile "tiles"
    private static void setPieceBoard(int line, int row, int piece, boolean hypo) {
        if (hypo) {
            hypo_iBoard.setup[line][row] = piece;
        } else {
            iBoard.setup[line][row] = piece;
            tiles[line][row].setPiece(piece);
        }
    }


    //adapted from https://stackoverflow.com/questions/18686199/fill-unicode-characters-in-labels
    private static ArrayList<Shape> separateShapeIntoRegions(Shape shape) {
        ArrayList<Shape> regions = new ArrayList<Shape>();

        PathIterator pi = shape.getPathIterator(null);
//        int ii = 0;
        GeneralPath gp = new GeneralPath();
        while (!pi.isDone()) {
            double[] coords = new double[6];
            int pathSegmentType = pi.currentSegment(coords);
            int windingRule = pi.getWindingRule();
            gp.setWindingRule(windingRule);
            if (pathSegmentType == PathIterator.SEG_MOVETO) {
                gp = new GeneralPath();
                gp.setWindingRule(windingRule);
                gp.moveTo(coords[0], coords[1]);
            } else if (pathSegmentType == PathIterator.SEG_LINETO) {
                gp.lineTo(coords[0], coords[1]);
            } else if (pathSegmentType == PathIterator.SEG_QUADTO) {
                gp.quadTo(coords[0], coords[1], coords[2], coords[3]);
            } else if (pathSegmentType == PathIterator.SEG_CUBICTO) {
                gp.curveTo(
                        coords[0], coords[1],
                        coords[2], coords[3],
                        coords[4], coords[5]);
            } else if (pathSegmentType == PathIterator.SEG_CLOSE) {
                gp.closePath();
                regions.add(new Area(gp));
            } else {
                System.err.println("Unexpected value! " + pathSegmentType);
            }

            pi.next();
        }

        return regions;
    }

    private BufferedImage getImageForChessPiece(
            int piece, int side, boolean gradient) {
        int sz = font.getSize();
        //int sz = (int)this.getFontSize();
        BufferedImage bi = new BufferedImage(
                sz, sz, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(
                RenderingHints.KEY_DITHERING,
                RenderingHints.VALUE_DITHER_ENABLE);
        g.setRenderingHint(
                RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        FontRenderContext frc = g.getFontRenderContext();
        GlyphVector gv = font.createGlyphVector(frc, upieces[piece]);
        //Rectangle2D box1 = gv.getVisualBounds();

        Shape shape1 = gv.getOutline();
        Rectangle r = shape1.getBounds();
        int spaceX = sz - r.width;
        int spaceY = sz - r.height;
        AffineTransform trans = AffineTransform.getTranslateInstance(
                -r.x + (spaceX / 2f), -r.y + (spaceY / 2f));

        Shape shapeCentered = trans.createTransformedShape(shape1);

        Shape imageShape = new Rectangle2D.Double(0, 0, sz, sz);
        Area imageShapeArea = new Area(imageShape);
        Area shapeArea = new Area(shapeCentered);
        imageShapeArea.subtract(shapeArea);
        ArrayList<Shape> regions = separateShapeIntoRegions(imageShapeArea);
        g.setStroke(new BasicStroke(1));
        g.setColor(pieceColors[side]);
        Color baseColor = pieceColors[side];
        if (gradient) {
            GradientPaint gp = new GradientPaint(
                    sz / 2f - (r.width / 4f), sz / 2f - (r.height / 4f), baseColor.brighter(),
                    sz / 2f + (r.width / 4f), sz / 2f + (r.height / 4f), baseColor,
                    false);
            g.setPaint(gp);
        } else {
            g.setColor(baseColor);
        }

        for (Shape region : regions) {
            Rectangle r1 = region.getBounds();
            if (!(r1.getX() < 0.001) || !(r1.getY() < 0.001)) {
                g.fill(region);
            }
        }
        g.setColor(outlineColor);
        g.fill(shapeArea);
        g.dispose();

        return bi;
    }

    //click on a tile
    @Override
    public void actionPerformed(ActionEvent e) {
        //	System.out.println(iBoard.setup[0][0]);
        //Tile tmp=(Tile)e.getSource();
        changeBoardState(Tile.statLine, Tile.statRow); //line and row clicked as argument
    }

    static class SpecialMove {
        boolean castling = false;
        boolean enPassant = false;
    }

    static class State {
    /*    State() {
        }

	public State(State st){
	    this.turnOf=st.turnOf;
	    this.moveNumber =       st.moveNumber;
	    this.castlingPossibleQ= st.castlingPossibleQ.clone();
	    this.castlingPossibleK= st.castlingPossibleK.clone();
	    this.enPassantPossible=st.enPassantPossible;
	    this.check=st.check;
	}*/

        int turnOf = WHITE;
        int moveNumber = 1;
        boolean[] castlingPossibleQ = {true, true};  //true-castling possible, false-castling not possible anymore or done, for white/black, for Queen-side
        boolean[] castlingPossibleK = {true, true};  //same for king-side
        int enPassantPossible = -1;
        boolean check = false; //currently, checking the opponent
    }


}


