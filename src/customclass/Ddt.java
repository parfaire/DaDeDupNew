package customclass;

import gnu.trove.THashMap;
import java.util.Map.Entry;

import java.io.*;

/**
 * Ddt class is a HashMap class that contains all the blocks.
 * The existence of this class required to determine whether blocks are duplicated or not.
 * Key - ByteWrapper (hashed form of the block)
 * Value - long[] (1.Offset and 2.Length of the block)
 * Ddt class is customised with write and read procedure in order to do IO operation to disk
 * with certain format.
 */

public class Ddt extends THashMap<ByteWrapper,long[]> {
    public Ddt(){
        super();
    }

    /**
     * Write the records to file using ObjectOutputStream with format of : "key1 value1 key2 value2 ... keyN valueN"
     * @param output - DDT File to be written
     * @throws IOException - if file is not found
     */
    public void write(String output) throws IOException{
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(output));
        // accessing keys/values through an iterator:
        for (Entry<ByteWrapper,long[]> e : entrySet()){
            out.writeObject(e.getKey());
            out.writeObject(e.getValue());
        }
        out.close();
    }

    /**
     * Read the records to file using ObjectInputStream with format of : "key1 value1 key2 value2 ... keyN valueN"
     * @param input - DDT File to be read
     */
    public void read(String input){
        ByteWrapper key;
        long[] val;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(input))) {
            for (; ; ) {
                key = (ByteWrapper) in.readObject();
                val = (long[]) in.readObject();
                put(key, val);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}
