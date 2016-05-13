package customclass;

import gnu.trove.THashMap;
import java.util.Map.Entry;

import java.io.*;

public class Ddt extends THashMap<ByteWrapper,long[]> {
    //long[0] = offset
    //long[1] = length
    public Ddt(){
        super();
    }
    public void write(String output) throws IOException{
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(output));
        // accessing keys/values through an iterator:
        for (Entry<ByteWrapper,long[]> e : entrySet()){
            out.writeObject(e.getKey());
            out.writeObject(e.getValue());
        }
        out.close();
    }

    public void read(String input) throws IOException{
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
