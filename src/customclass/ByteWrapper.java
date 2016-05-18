package customclass;

import java.io.*;
import java.util.Arrays;

/**
 * ByteWrapper is a wrapper class for byte[] to be used for comparison.
 * Java implementation of comparison byte[] is using its identifier and not its content,
 * This class is created, in purpose to precisely comparing the content instead.
 */

public class ByteWrapper implements Serializable
{
    private byte[] data;

    public ByteWrapper(byte[] data)
    {
        if (data == null)
        {
            throw new NullPointerException();
        }
        this.data = data;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ByteWrapper && Arrays.equals(data, ((ByteWrapper) other).data);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(data);
    }
}