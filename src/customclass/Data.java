package customclass;

import gnu.trove.list.array.TLongArrayList;

/**
 * Data class is a wrapper class for "2 data" of the file.
 * Those 2 data are :
 *   ArrayList offsets - identify what blocks are belong to a particular file.
 *   long size - how big is the file.
 */
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
