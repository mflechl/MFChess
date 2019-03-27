package com.mflechl.mfchess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 *
 */
public class ChessBoard implements ActionListener, ThreadListener  {

    private static boolean autoComputerMove = false;
    private static boolean onlyComputerMove = false;
    private static final boolean USE_OPENINGS = false;
    static final boolean USE_THREAD = true;

    private Color color1; // Color of square 1
    private Color color2; // Color of square 2

    static IBoard iBoard = new IBoard(); //constructor sets up inital chess board state
    static Tile[][] tiles = new Tile[8][8];
    static MoveThread moveThread = new MoveThread();

    static final int KING = 1, QUEEN = 2, ROOK = 3, BISHOP = 4, KNIGHT = 5, PAWN = 6; //better than enum (to be reevaluated)
    static final String[] lpieces = {"", "K", "Q", "R", "B", "N", "P"};
    static final int WHITE = +1, BLACK = -1;
    static ImageIcon[] wpieces = new ImageIcon[7];
    static ImageIcon[] bpieces = new ImageIcon[7];

    static IState currentStaticState = new IState();
    static ArrayList<IBoardState> pastMoves = new ArrayList<>();
    static IBoardState currentBestMove;
//    static IBoardState nextBestMove;
    static String nextBestMove="";

    //    ReadOpenings readOpenings = new ReadOpenings();
    static List<String> openings;

    // Constructor
    ChessBoard(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
        initBoard();
        pastMoves.add(new IBoardState(iBoard, currentStaticState));

        ReadOpenings readOpenings = new ReadOpenings();
        openings = new ArrayList<String>(readOpenings.openings);

    }

    public static void toggleAutoComputerMove() {
        autoComputerMove = !autoComputerMove;
    }

    public static void toggleOnlyComputerMove() {
        onlyComputerMove = !onlyComputerMove;
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
            Move move = new Move();
            move.updateCheckState(currentStaticState, iBoard);
            IBoardState currentMove = new IBoardState(iBoard, currentStaticState);
            pastMoves.set(currentStaticState.nMoves, currentMove);
            String str = Notation.notationStrings.get(currentStaticState.nMoves);
            str = str.replaceAll("..$", ChessBoard.lpieces[Math.abs(newPiece)] + " ");
            Notation.notationStrings.set(currentStaticState.nMoves, str);
            Chess.notation.display();
        } else {
            tiles[Tile.promLine][Tile.promRow].thisPromActive = false;
            Tile.promActive = false;
//            tiles[Tile.promLine][Tile.promRow].setBorderInactive();
            tiles[Tile.promLine][Tile.promRow].setLastMoveBorder();

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
            if (autoComputerMove && !Tile.promActive) computerMove();
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

            ArrayList<Ply> plies = Move.listAllMovesSquare(iBoard, currentStaticState, aLine, aRow);
            for (Ply p : plies) {
                tiles[p.getToLine()][p.getToRow()].setDestinationBorder();
                System.out.println("XXXXX ply " + p);
            }

            //ArrayList<int[]> list = Move.legalDestination(iBoard, aLine, aRow, currentStaticState, false);
            //for (int[] pos : list) {
                //tiles[pos[0]][pos[1]].setDestinationBorder();
            //}
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

                removeFutureMoves(currentStaticState);

                IState updatedState = new IState(currentStaticState);
                updatedState.update(movingPiece, aLine, _l, aRow);
                updateCastlingState(updatedState, movingPiece, aLine, aRow, _l, _r, sMove.castling);
                Move move = new Move();
                move.updateCheckState(updatedState, iBoard);

                IBoardState chosenMove=new IBoardState(iBoard, updatedState);
                String notation=Notation.getMoveNotation(chosenMove, chosenMove.state, currentStaticState, aLine, aRow, _l, _r, movingPiece, eliminatedPiece, sMove);
                chosenMove.setNotation(notation);

                updateMove(chosenMove);

                if (Tile.promActive) tiles[Tile.promLine][Tile.promRow].setPromBorder();
                aLine = -1;
                aRow = -1;
                tileActive = false;

                if (autoComputerMove && !Tile.promActive)
                    computerMove(); //answer human move with computer move
                else if (USE_THREAD){     //calculate a computer move, but only exectute it if user wants it
                    findDeeperMove( Move.DEFAULT_START_DEPTH, false );
                    //return;
                }

            }
        }

    }

    void findDeeperMove(int newDepth, boolean executeNow){
        //System.out.println("fDM: "+newDepth+" "+moveThread.isAlive() +" " + moveThread.getState() );
        //if calling itself to add one depth, thread is still alive - ignore it...
        moveThread.move.stopBestMove = true;

        while (moveThread.isAlive()) { //TODO: use join?
            try {
                Thread.sleep(50);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        //if ( checkAlive && moveThread.isAlive() ) return; //do nothing if already running
        int currentDepth = newDepth;
        if (currentDepth<0) currentDepth=moveThread.getDepth();
        if (currentDepth<1) return;

        moveThread = new MoveThread( new IBoardState(iBoard, currentStaticState), executeNow );
//        moveThread.executeNow = executeNow;
//        moveThread.boardState= new IBoardState(iBoard, currentStaticState);
        moveThread.addListener(this);
        moveThread.setGoDeeper(true); //try next-higher deepness next
        moveThread.start();
    }

    static String getLastMoveString() {
        return getMoveString(Notation.notationStrings.size() - 1);
    }

    static String getMoveString(int iMove) {
        String str = Notation.notationStrings.get(iMove);
        str = str.replaceAll("\\.", ". ");

        if (str.length() > 0) {
            if (!str.matches("\\d+\\. .*")) {
                if (Notation.notationStrings.size() > 1) {
                    String prev = Notation.notationStrings.get(iMove - 1);
                    prev = prev.replaceAll("(\\d+\\. ).*", "$1 ... ");
                    str = prev + str;
                }
            }
        }
        str = "<html>" + str + "</html>";
        return str;
    }

    static void getPreviousState() {
        moveThread.move.stopBestMove = true;
        int gotoState = currentStaticState.nMoves - 1;
        if (gotoState >= 0) setActiveState(pastMoves.get(gotoState), gotoState);
    }

    void getNextState() {
        moveThread.move.stopBestMove = true;
        int gotoState = currentStaticState.nMoves + 1;
        if (gotoState < pastMoves.size()) setActiveState(pastMoves.get(gotoState), gotoState);
        else computerMove();
    }

    static void getBeginState() {
        moveThread.move.stopBestMove = true;
        int gotoState = 0;
        setActiveState(pastMoves.get(gotoState), gotoState);
    }

    static void getLastState() {
        moveThread.move.stopBestMove = true;
        int gotoState = pastMoves.size() - 1;
        setActiveState(pastMoves.get(gotoState), gotoState);
    }

    static void setActiveState(IBoardState boardState, int gotoState) {
        setAllBordersInactive();

        iBoard = new IBoard(boardState);
        currentStaticState = new IState(boardState.state);

        fillTilesFromBoard();
//        String nextMovesLabel=boardState.getNextMovesNotation().replaceAll("(\\d+\\.)* \\w\\S","...");
        String nextMovesLabel=boardState.getNextMovesNotation().replaceAll("^\\d+\\. ","");
        nextMovesLabel = nextMovesLabel.replaceAll("^\\S* ","");
        if ( !nextMovesLabel.matches("^\\d+\\..*") ) nextMovesLabel = "... "+nextMovesLabel;
        setLabelLastandNextMove( gotoState, boardState.getEval(), nextMovesLabel );

        if (boardState.state.nMoves > 0 && pastMoves.size() >= boardState.state.nMoves)
            findAndSetLastMoveBorder(boardState, pastMoves.get(boardState.state.nMoves - 1));

        System.out.println("sAS: " + currentStaticState + "   EVAL = " + boardState.getEval() + "   EVAL curr = " + EvaluateBoard.eval(iBoard,currentStaticState) );
        System.out.println("sA2: " + boardState.getNextMovesNotation());
        //System.out.println("sA2: " + boardState.getNotation());

//        System.out.println(iBoard);
        nextBestMove = boardState.getNextMovesNotation().replaceFirst("^\\d+\\. ","")
                .replaceAll("^(.*? )","").replaceFirst("^\\d+\\. ","").replaceAll(" .*","");
    }

    static void removeFutureMoves(IState state) {
        if (pastMoves.size() != (state.nMoves + 1)) {     //have moved back in history and adding moves...
            pastMoves.subList(state.nMoves + 1, pastMoves.size()).clear();
            Notation.notationStrings.subList(state.nMoves + 1, Notation.notationStrings.size()).clear();
        }
    }

    //translate notation into board states
    void setStateFromNotation(String notation) {
        ArrayList<IBoardState> list = NotationToState.translate(notation);
        if (list.size() > 0) pastMoves = list;
        for (IBoardState move : pastMoves) {
            setActiveState(move, move.state.nMoves);
        }
        System.out.println("init: " + notation);
    }

    static void setLabelLastandNextMove(int gotoState, int eval, String nextMoves) {
        String text;
        if (gotoState < 0) text=getLastMoveString().replaceAll("<font color='red'>.*</font>", "");
        else if (gotoState == 0) text="";
        else text=getMoveString(gotoState).replaceAll("<font color='red'>.*</font>", "");
        Chess.btnLastMove.setText(text);

        setLabelNextMoves(eval, nextMoves);
    }

    static void setLabelNextMoves( int eval, String nextMoves ){
        nextMoves = nextMoves.replaceAll("\\.\\.\\.\\s*","");
        if (!nextMoves.matches("^\\d+\\..*")) nextMoves = "... " + nextMoves;
        nextMoves=nextMoves.replaceFirst("(\\w+[#+\\s])","<font color='red'>$1</font>");

        nextMoves = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + nextMoves;
        if (eval<-10000) nextMoves="- "+nextMoves;
        else nextMoves=eval/100.0 + nextMoves;

        nextMoves = "<html> " + nextMoves + "</html>";

        Chess.btnNextMoves.setText(nextMoves);
    }


    //if some castling options get eliminated, check and set it here
    public static void updateCastlingState(IState _state, int piece, int fromLine, int fromRow, int toLine, int toRow, boolean castlingDone) {
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
            piece = (int) (Math.signum(piece) * QUEEN);
            if (!hypo) {
                tiles[toLine][toRow].setPromBorder();
                System.out.println("promActive = " + Tile.promActive);
            }
            ret = true; //indicate that prom has happened
        }

        //remove old piece
        setPieceBoard(board, fromLine, fromRow, 0, hypo);

        //put new piece
        setPieceBoard(board, toLine, toRow, piece, hypo);

        if (sMove.enPassant) setPieceBoard(board, (int) (toLine - Math.signum(piece)), toRow, 0, hypo);
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
            setPieceBoard(board, toLine, rookToRow, (int) (Math.signum(piece) * ROOK), hypo);
            setPieceBoard(board, toLine, rookFromRow, 0, hypo);
        }
        return ret;
    }

    //set both iBoard byte array and the content of the correspond tile "tiles"
    private static void setPieceBoard(IBoard board, int line, int row, int piece, boolean hypo) {
        if (line < 0 || line > 7 || row < 0 || row > 7)
            throw new ArrayIndexOutOfBoundsException("line: " + line + " row: " + row);

        board.setup[line][row] = (byte) piece;
        if (!hypo) tiles[line][row].setPiece(piece);
    }

    void computerMove() {

        //cannot move
        if (currentStaticState.mate) return;
        else if (currentStaticState.remis) return;

        if ( currentBestMove != null ){

            updateMove(currentBestMove);

            if (USE_THREAD){     //calculate a computer move, but only execute it if user wants it
                findDeeperMove(Move.DEFAULT_START_DEPTH, Chess.COMPUTER_PLAY ); //execute now if COMPUTER_PLAY
            }
            return;
        }

        //reset tile borders
        if (Tile.promActive) {
            promChooseFigure(4, 4); //cannot have an active prom in line 4, just triggers removal of prom status.
        }
        setAllBordersInactive();
        removeFutureMoves(currentStaticState);

        //compare to openings
        //System.out.println("CM" + Notation.getNotationString() + "CM");
        String notationString = Notation.getNotationString();
        List<String> matchingOpenings = new ArrayList<>();

        if ( USE_OPENINGS ) {
            for (String line : openings) {
                if (line.startsWith(notationString)) {
                    //System.out.println("O:|"+line+"|X|"+line.replaceAll(notationString,"")+"|Y|"+nextMove+"|");
                    //replace takes a literal, replaceAll a regex, but both replace *all* occurrences
                    String[] nm = line.replace(notationString, "").replaceAll("^ *", "").split(" ");
                    if (nm.length == 0) continue;
                    String nextMove = nm[0];
                    if (nextMove.matches("\\d+\\.")) {
                        if (nm.length == 1) continue;
                        nextMove = nm[1];
                    }
                    if (nextMove.length() > 1)
                        matchingOpenings.add(nextMove); //only add if opening has *additional* moves
                }
            }
        }
//        System.out.println("#Openings: " + matchingOpenings.size());

        IBoardState chosenMove;
        //take random known opening move
        if (matchingOpenings.size() > 0) {
            Random r = new Random();
            int rnd = r.nextInt(matchingOpenings.size());
            String chosenOpening = matchingOpenings.get(rnd);

//            System.out.println("XXX" + chosenOpening + "XXX");
            chosenMove = NotationToState.noteToBoard(chosenOpening, pastMoves.get(pastMoves.size() - 1));
        }
        //evaluate best move
        else {
            //get list of all possible moves
            long startTime = System.currentTimeMillis();

            if (USE_THREAD) {
                findDeeperMove(Move.DEFAULT_START_DEPTH, Chess.COMPUTER_PLAY); //execute now if computer play
                return;
            } else {
                Move move = new Move();
                chosenMove = move.bestMove( new IBoardState(iBoard, currentStaticState) );
            }

            long finishTime = System.currentTimeMillis();
            System.out.println("That took: " + (finishTime - startTime) + " ms");

            //System.out.println("Next Moves: " + chosenMove.nextMoveNotation);

        }
        if (chosenMove != null){
            updateMove(chosenMove);
        }
    }

    static void updateMove() {
        if ( currentBestMove == null ) System.out.println("ERROR: ChessBoard.updateMove - No valid move");
        updateMove(currentBestMove);
    }

    static void updateMove(IBoardState chosenMove){
        //append to notation
        Chess.notation.updateText(chosenMove.getNotation(), chosenMove.state.nMoves);

        //update currentStaticState
        pastMoves.add(chosenMove.state.nMoves, chosenMove);
        setActiveState(pastMoves.get(chosenMove.state.nMoves), chosenMove.state.nMoves);
        currentBestMove = null;
    }

    static void findAndSetLastMoveBorder(IBoard bCurr, IBoard bPrev) {
        ArrayList<int[]> boardDiff = IBoard.diff(bCurr, bPrev);

        //can only happen for en passant or castling
        if (boardDiff.size() > 2) {
            //en passant: remove field not touched by move
            boardDiff.removeIf(aDiff -> bPrev.setup[aDiff[0]][aDiff[1]] == currentStaticState.turnOf * ChessBoard.PAWN);
            //castling: keep only starting and end field of the king
            boardDiff.removeIf(aDiff -> bPrev.setup[aDiff[0]][aDiff[1]] != currentStaticState.turnOf * -1 * ChessBoard.KING &&
                    bCurr.setup[aDiff[0]][aDiff[1]] != currentStaticState.turnOf * -1 * ChessBoard.KING);
        }

        for (int[] pos : boardDiff) {
            tiles[pos[0]][pos[1]].setLastMoveBorder();
        }
    }

    //move thread done
    @Override
    public void onBestMoveAvailable(IBoardState chosenMove, boolean executeNow ) {
        //System.out.println("BESTMOVE "+chosenMove);
        if ( chosenMove == null ) return;
        currentBestMove = new IBoardState(chosenMove);
        if (executeNow){
//            computerMove();
            updateMove(chosenMove);
        }
    }

    //click on a tile
    @Override
    public void actionPerformed(ActionEvent e) {
        //	System.out.println(iBoard.setup[0][0]);
        //Tile tmp=(Tile)e.getSource();
        moveThread.move.stopBestMove = true;
        while (moveThread.isAlive()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        changeBoardState(Tile.statLine, Tile.statRow); //line and row clicked as argument
    }


}


