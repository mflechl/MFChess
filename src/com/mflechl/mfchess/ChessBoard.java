package com.mflechl.mfchess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
//import java.util.concurrent.TimeUnit;

//import javax.swing.Icon;


/**
 *
 *
 */
public class ChessBoard implements ActionListener {
    private Color color1; // Color of square 1
    private Color color2; // Color of square 2

    private static IBoard iBoard = new IBoard();
    //    static IBoard hypo_iBoard = new IBoard();
    static Tile[][] tiles = new Tile[8][8];

    static final int KING = 1, QUEEN = 2, ROOK = 3, BISHOP = 4, KNIGHT = 5, PAWN = 6; //better than enum (to be reevaluated)
    static final String[] lpieces = {"", "K", "Q", "R", "B", "N", "P"};
    static final int WHITE = +1, BLACK = -1;
    static ImageIcon[] wpieces = new ImageIcon[7];
    static ImageIcon[] bpieces = new ImageIcon[7];

    static State currentStaticState = new State();

    private static ArrayList<IBoardState> pastMoves = new ArrayList<>();

//    private boolean mate = false;
//    private boolean remis = false;

    // Constructor
    ChessBoard(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
        initBoard();
        pastMoves.add(new IBoardState(iBoard, currentStaticState));
    }

    void setMaxFontSize() {
        float mindim = Math.min(tiles[0][0].getSize().height, tiles[0][0].getSize().width);
        setFontSize(mindim * (float) 0.9);
        initPieces();
        fillTilesFromBoard();
    }

    static void fillTilesFromBoard() {
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
    }

    /* public float getFontSize() {
	return tiles[0][0].getFontSize();
    }*/

    private void initPieces() {
        for (int i = 1; i < 7; i++) {
            final boolean useGradient = true;
            wpieces[i] = new ImageIcon(ChessPieceImage.getImageForChessPiece(i, 0, useGradient));
            bpieces[i] = new ImageIcon(ChessPieceImage.getImageForChessPiece(i, 1, useGradient));
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

    private static void setAllBordersInactive() {
        for (Tile[] tiles : tiles) {
            for (Tile tile : tiles) {
                tile.setBorderInactive();
            }
        }
    }

    private static void promChooseFigure(int _l, int _r) {
        if (tiles[_l][_r].thisPromActive) {
            int newPiece = iBoard.setup[_l][_r] - currentStaticState.turnOf;
            if (Math.abs(newPiece) > Math.abs(KNIGHT)) newPiece = (int) Math.signum(newPiece) * QUEEN;
            setPieceBoard(iBoard, _l, _r, newPiece, false);
            //tiles[_l][_r].setPiece( tiles[_l][_r].getPiece()-currentStaticState.turnOf );
            //if ( Math.abs( iBoard.setup[_l][_r].getPiece() )>Math.abs(KNIGHT) ) tiles[_l][_r].setPiece( (int)Math.signum(tiles[_l][_r].getPiece())*QUEEN );
            Move.updateCheckState(currentStaticState, iBoard);
            IBoardState currentMove = new IBoardState(iBoard, currentStaticState);
            pastMoves.set(currentStaticState.nMoves, currentMove);
            String str = Notation.notationStrings.get(currentStaticState.nMoves);
            str = str.replaceAll("..$", ChessBoard.lpieces[Math.abs(newPiece)] + " ");
            Notation.notationStrings.set(currentStaticState.nMoves, str);
            Chess.notation.display();
        } else {
            tiles[Tile.promLine][Tile.promRow].setBorderInactive();
            tiles[Tile.promLine][Tile.promRow].thisPromActive = false;
            /*
            for (Tile[] itiles : tiles) {
                for (Tile itile : itiles) {
                    if (itile.thisPromActive) {
                        itile.setBorderInactive();
                        itile.thisPromActive = false;
                    }
                }
            }
            */
            Tile.promActive = false;
            Tile.promLine = -1;
            Tile.promRow = -1;
        }
    }


    private boolean tileActive = false;
    private int aLine = -1;
    private int aRow = -1;

    private void changeBoardState(int _l, int _r) {

        if (Tile.promActive) {
            promChooseFigure(_l, _r);
            if (!Tile.promActive) computerMove();
            return;
        }

        //piece of right color?; change setActiveBorder field if needed
        if (tiles[_l][_r].getPiece() * currentStaticState.turnOf > 0) {
//            if (aLine >= 0 && aRow >= 0) tiles[aLine][aRow].setBorderInactive();
            if (aLine >= 0 && aRow >= 0) setAllBordersInactive();
            tiles[_l][_r].setActiveBorder();
            aLine = _l;
            aRow = _r;
            tileActive = true;
            ArrayList<int[]> list = Move.legalDestination(iBoard, aLine, aRow, currentStaticState, false);
            for (int[] pos : list) {
                tiles[pos[0]][pos[1]].setDestinationBorder();
            }
        }
        //there is an setActiveBorder tile
        else if (tileActive) {
            if (_l == aLine && _r == aRow) {  //clicking on setActiveBorder tile: make it setBorderInactive
                //tiles[aLine][aRow].setBorderInactive();
                setAllBordersInactive();
                aLine = -1;
                aRow = -1;
                tileActive = false;
            } else { //not clicking on setActiveBorder tile: move?

                SpecialMove sMove = new SpecialMove(); //save currentStaticState: castling, en passant, ...
                if (!Move.isLegal(iBoard, sMove, aLine, aRow, _l, _r)) return;

                //Move!
                int movingPiece = tiles[aLine][aRow].getPiece();
                int eliminatedPiece = tiles[_l][_r].getPiece();

                processMove(iBoard, aLine, aRow, _l, _r, movingPiece, false, sMove);

                State updatedState = new State(currentStaticState);
                updatedState.update(movingPiece, aLine, _l, aRow);
                updateCastlingState(updatedState, movingPiece, aLine, aRow, _l, _r, sMove.castling);
                Move.updateCheckState(updatedState, iBoard);

                //fastest remis:
                //1. e3 a5 2. Qh5 Ra6 3. Qxa5 h5 4. h4 Rah6 5. Qxc7 f6 6. Qxd7+ Kf7 7. Qxb7 Qd3 8. Qxb8 Qh7 9. Qxc8 Kg6 10. Qe6

                removeFutureMoves(updatedState);

                Chess.notation.addMove(pastMoves.get(pastMoves.size() - 1), updatedState, currentStaticState, aLine, aRow, _l, _r,
                        movingPiece, eliminatedPiece, sMove);

                //save to history
//                IBoardState currentMove = new IBoardState(iBoard, updatedState);
//                pastMoves.add(updatedState.nMoves, currentMove);
                pastMoves.add(updatedState.nMoves, new IBoardState(iBoard, updatedState));

                //update currentStaticState
                currentStaticState = updatedState;
                System.out.println("hu: " + currentStaticState + " promActive" + Tile.promActive);

                setAllBordersInactive();
                tiles[aLine][aRow].setLastMoveBorder();
                tiles[_l][_r].setLastMoveBorder();
                if (Tile.promActive) tiles[Tile.promLine][Tile.promRow].setPromBorder();
                aLine = -1;
                aRow = -1;
                tileActive = false;
                Chess.btnLastMove.setText(getLastMoveString());

                if (!Tile.promActive) computerMove(); //by default, always answer a human move with a computer move

            }
        }

    }

    static String getLastMoveString() {
        return getMoveString(Notation.notationStrings.size() - 1);
    }

    static String getMoveString(int iMove) {
        String str = Notation.notationStrings.get(iMove);
        str = str.replaceAll("\\.", ". ");

        if (str.length() > 0) {
            String substr = str.substring(0, 1);
            if (!substr.equals("<")) {
                if (Notation.notationStrings.size() > 1) {
                    String prev = Notation.notationStrings.get(iMove - 1);
                    prev = prev.replaceAll("</font>.*", "</font> ... ");
                    str = prev + str;
                }
            }
        }
        str = "<html>" + str + "</html>";
        return str;
    }

    static void getPreviousState() {
        int gotoState = currentStaticState.nMoves - 1;
        if (gotoState >= 0) setActiveState(pastMoves.get(gotoState), gotoState);
    }

    static void getNextState() {
        System.out.println("gNS: " + currentStaticState.nMoves + " " + pastMoves.size());
        int gotoState = currentStaticState.nMoves + 1;
        if (gotoState < pastMoves.size()) setActiveState(pastMoves.get(gotoState), gotoState);
        else computerMove();
    }

    static void getBeginState() {
        int gotoState = 0;
        setActiveState(pastMoves.get(gotoState), gotoState);
    }

    static void getLastState() {
        int gotoState = pastMoves.size() - 1;
        setActiveState(pastMoves.get(gotoState), gotoState);
    }

    static void setActiveState(IBoardState boardState, int gotoState) {
        iBoard = new IBoard(boardState);
        currentStaticState = new State(boardState.state);

        fillTilesFromBoard();
        setLabelLastMove(gotoState);

        System.out.println("sAS: " + currentStaticState);
    }

    static void removeFutureMoves(State state) {
        if (pastMoves.size() != state.nMoves) {     //have moved back in history and adding moves...
            pastMoves.subList(state.nMoves, pastMoves.size()).clear();
            Notation.notationStrings.subList(state.nMoves, Notation.notationStrings.size()).clear();
        }
    }


    static void setLabelLastMove(int gotoState) {
        if (gotoState < 0) Chess.btnLastMove.setText(getLastMoveString().replaceAll("<font color='red'>.*</font>", ""));
        else if (gotoState == 0) Chess.btnLastMove.setText("");
        else Chess.btnLastMove.setText(getMoveString(gotoState).replaceAll("<font color='red'>.*</font>", ""));
    }

    //if some castling options get eliminated, check and set it here
    public static void updateCastlingState(State _state, int piece, int fromLine, int fromRow, int toLine, int toRow, boolean castlingDone) {
        int colIndex = 0, colIndexOther = 1; //black/white
        if (piece > 0) {
            colIndex = 1;
            colIndexOther = 0;
        } //white/black

        //       if (!_state.castlingPossibleQ[colIndex] && !_state.castlingPossibleK[colIndex]) return;

        //rooks eliminated
        if (toLine == (7 * (1 - colIndexOther)) && toRow == 0) {
            _state.castlingPossibleQ[colIndexOther] = false;
        } else if (toLine == (7 * (1 - colIndexOther)) && toRow == 7) {
            _state.castlingPossibleK[colIndexOther] = false;
        }
        //king moved, incl. castling
        else if (Math.abs(piece) == KING || castlingDone) {
            _state.castlingPossibleQ[colIndex] = false;
            _state.castlingPossibleK[colIndex] = false;
        }
        //rook was moved - only need to catch first move
        else if (Math.abs(piece) == ROOK) {
            if (fromLine == (7 * (1 - colIndex)) && fromRow == 0) _state.castlingPossibleQ[colIndex] = false;
            if (fromLine == (7 * (1 - colIndex)) && fromRow == 7) _state.castlingPossibleK[colIndex] = false;
        }
    }

    //hypo=check hypothetical move, do not execute
/*
    static void processMove(IBoard board, int fromLine, int fromRow, int toLine, int toRow, boolean hypo, int piece) {
        processMove( board, fromLine, fromRow, toLine, toRow, piece, hypo, Move.SDUMMY );
    }
*/
    //return value gives row where promotion happened, if applicable, otherwise -1
    static boolean processMove(IBoard board, int fromLine, int fromRow, int toLine, int toRow, int piece, boolean hypo, SpecialMove sMove) {
        boolean ret = false;

        if ((piece == BLACK * PAWN && toLine == 0) || (piece == WHITE * PAWN && toLine == 7)) {
            //System.out.println(toRow+" "+piece+" LLL "+hypo);
            piece = (int) Math.signum(piece) * QUEEN;
            if (!hypo) {
                tiles[toLine][toRow].setPromBorder();
                System.out.println("promActive = " + Tile.promActive);
            }
            ret = true;
        }

        //remove old piece
        setPieceBoard(board, fromLine, fromRow, 0, hypo);

        //put new piece
        setPieceBoard(board, toLine, toRow, piece, hypo);

        if (sMove.enPassant) setPieceBoard(board, toLine - (int) Math.signum(piece), toRow, 0, hypo);
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
            setPieceBoard(board, toLine, rookToRow, (int) Math.signum(piece) * ROOK, hypo);
            setPieceBoard(board, toLine, rookFromRow, 0, hypo);
        }
        return ret;
    }

    //set both iBoard int array and the content of the correspond tile "tiles"
    private static void setPieceBoard(IBoard board, int line, int row, int piece, boolean hypo) {
        if (line < 0 || line > 7 || row < 0 || row > 7)
            throw new ArrayIndexOutOfBoundsException("line: " + line + " row: " + row);

        board.setup[line][row] = piece;
        if (!hypo) tiles[line][row].setPiece(piece);

        /*
        if (hypo) {
            hypo_iBoard.setup[line][row] = piece;
        } else {
            iBoard.setup[line][row] = piece;
            tiles[line][row].setPiece(piece);
        }
        */
    }

    static void computerMove() {
        //TODO: check openings

        if (Tile.promActive) {
            promChooseFigure(4, 4); //cannot have an active promotion in line 4... just to trigger to move on.
        }

        setAllBordersInactive();

        //get list of all possible moves
        ArrayList<IBoardState> allMoves = Move.allLegalMoves(iBoard, currentStaticState);
        if (allMoves.isEmpty()) return;   //mate or remis

        //TODO: choose move
        IBoardState chosenMove = allMoves.get(0);
        System.out.println("chosenMove = \n " + chosenMove + "nLegalMoves=" + allMoves.size());

        removeFutureMoves(chosenMove.state);

        //append to notation
        Chess.notation.updateText(chosenMove.getNotation(), chosenMove.state.nMoves);

        //update currentStaticState
        pastMoves.add(chosenMove.state.nMoves, chosenMove);
        setActiveState(pastMoves.get(chosenMove.state.nMoves), chosenMove.state.nMoves);

        //highlight last move on board
        //TODO: find changes between boards; mark them (exceptions for en passant, castling? only case where >2 fields change!)

    }


    //click on a tile
    @Override
    public void actionPerformed(ActionEvent e) {
        //	System.out.println(iBoard.setup[0][0]);
        //Tile tmp=(Tile)e.getSource();
        changeBoardState(Tile.statLine, Tile.statRow); //line and row clicked as argument
    }


}


