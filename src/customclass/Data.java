package customclass;

import gnu.trove.TLongArrayList;

public class Data {
    private TLongArrayList offsets;
    private long size;

    public Data(TLongArrayList offsets, long size){
        this.offsets=offsets;
        this.size=size;
    }

    TLongArrayList getOffsets(){
        return offsets;
    }

    long getSize(){
        return size;
    }
}
