package org.xjy.android.nova.transfer.common;

public class TransferException extends RuntimeException {
    public static final int TYPE_QUIT = 1;

    private int mType;

    public TransferException(int type) {
        mType = type;
    }

    public int getType() {
        return mType;
    }
}
