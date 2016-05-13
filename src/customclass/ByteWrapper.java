package customclass;

import java.io.*;
import java.util.Arrays;

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