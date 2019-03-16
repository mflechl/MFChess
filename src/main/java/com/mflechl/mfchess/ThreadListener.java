package com.mflechl.mfchess;

public interface ThreadListener {
    void onBestMoveAvailable(IBoardState chosenMove, boolean executeNow );
}
