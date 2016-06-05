package customclass;

import gnu.trove.list.array.TLongArrayList;
import java.io.*;
import java.security.MessageDigest;
import java.util.zip.*;

/**
 *  Deduplication Class provides Write and Read functionality to do I/O operation to storage.
 *  This class also populate the ddt object, and gives an offset pointer to file with duplicated block.
 *  Deflate compression is used in this class in order to optimise the efficiency of the storage.
 */
public class Deduplication {
    private boolean compressed;
    private int blockSize;
    private File storageFile;
    private RandomAccessFile raf;
    private InputStream is;
    private OutputStream os;
    private BufferedInputStream bis;
    private String hashFunc;
    private Book book;
    private Ddt ddt;

    /**
     * Constructor to instantiate all the required attributes.
     * */
    public Deduplication(String storage, Book book, Ddt ddt, String hashFunc, int blockSize, boolean compressed){
        storageFile = new File(storage);
        this.book = book;
        this.ddt = ddt;
        this.hashFunc = hashFunc;
        this.blockSize = blockSize;
        this.compressed = compressed;
    }

    /**
     * Constructor Overloading for default compressed is true
     * */
    public Deduplication(String storage, Book book, Ddt ddt, String hashFunc, int blockSize){
        this(storage,book,ddt,hashFunc,blockSize,true);
    }

    /**
     * Instantiate the FileOutputStream for writing from file.
     */
    public void readyToWrite(){
        try {
            os = new FileOutputStream(storageFile,true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 1. Instantiate the RandomAccessFile once before a series of read operations
     * to avoid instantiating every time storage being read.
     * 2. Instantiate the FileInputStream for reading from file.
     */
    public void readyToRead(){
        try {
            raf = new RandomAccessFile(storageFile, "r");
            is = new FileInputStream(raf.getFD());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read procedure -> creating a file
     * @param target the path where the file will be saved
     * @param folderAndFileName the name of file and its folder container as identifier
     */
    public void read(String target, String folderAndFileName){
        try {
            byte[] block,uncompressedBlock;
            target = target+"/"+folderAndFileName;
            //prepare all the folders hierarchy and the file
            File fout = new File(target);
            File parent = fout.getParentFile();
            if(!parent.exists() && !parent.mkdirs()){
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }
            //make sure the record of all the blocks needed can be obtained to mold the file
            if(book.containsRecord(folderAndFileName)){
                //clean the file if exist
                PrintWriter pw = new PrintWriter(target);
                pw.close();
                //bufferedOutputStream as a writer
                BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream(fout, true));
                //get all the offsets of the blocks
                Data data = book.getRecord(folderAndFileName);
                long offset,filesize = data.getSize();
                int length,localBlockSize=blockSize;
                TLongArrayList offsets = data.getOffsets();
                for(int i=0;i<offsets.size();i+=2){
                    //get the offset and length of the block
                    offset = offsets.get(i);
                    length = (int) offsets.get(i+1);
                    block = new byte[length];
                    //seek to a different section of file, discard previous buffer
                    raf.seek(offset);
                    bis = new BufferedInputStream(is);
                    bis.read(block,0,length);
                    //keep track on the block size, since the last one most likely to be < blockSize
                    if(filesize<localBlockSize)
                        localBlockSize=(int)filesize;
                    //decompress the block
                    if(compressed) {
                        uncompressedBlock = decompress(block, length, localBlockSize);
                        block = uncompressedBlock;
                    }
                    //write the block size
                    bos.write(block,0,localBlockSize);
                    filesize -= localBlockSize;
                }
                bos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Write procedure : read a file -> divide into blocks -> hash it -> add into ddt* -> write into storage* -> save file record
     * *if it doesn't exist
     * @param folderAndFileName file name with its folder container as identifier
     * @param fileInput file input to be written
     */
    public long[] write(String folderAndFileName, File fileInput){
        //Data collection for statistic purpose
        long totalDuplicatedBlock = 0, totalBlock=0, totalSizeOfDuplicatedBlock=0, totalSizeOfBlock=0, totalCompress=0;
        try {
            //read the file and prepare the hashFunction
            InputStream fis = new FileInputStream(fileInput);
            MessageDigest digest = MessageDigest.getInstance(hashFunc);
            ByteWrapper hashedBlock;
            //arraylist as a collection of offsets where the blocks of a file are located
            TLongArrayList offsets = new TLongArrayList();
            //readBlock is blockSize block, writeBlock is for writing (especially needed for the compressed one)
            byte[] readBlock = new byte[blockSize],writeBlock;
            long offset = storageFile.length();
            long[] offsetAndLength;
            int i;
            //divide into blocks of "blockSize" size
            while((i = fis.read(readBlock)) != -1){
                totalBlock++;
                //hash the block
                digest.update(readBlock, 0, i);
                hashedBlock = new ByteWrapper(digest.digest());
                //duplicate block?
                if (ddt.containsKey(hashedBlock)) {
                    totalDuplicatedBlock++;
                    //get the offset pointer
                    long[] x = ddt.get(hashedBlock);
                    offsets.add(x);
                    totalSizeOfDuplicatedBlock += x[1];
                    i = (int) x[1];
                }else {
                    //compress if true
                    if(compressed) {
                        writeBlock = compress(readBlock);
                        i = writeBlock.length;
                    }else{
                        writeBlock = readBlock;
                    }
                    totalCompress += i;
                    //write block to storage
                    os.write(writeBlock, 0, i);
                    //save the offset and length then add it to records
                    offsetAndLength = new long[2];
                    offsetAndLength[0] = offset;
                    offsetAndLength[1] = i;
                    offsets.add(offsetAndLength);
                    //add the block into ddt
                    ddt.put(hashedBlock,offsetAndLength);
                    //increase the offset to be used by next block
                    offset += i;
                }
                totalSizeOfBlock+=i;
            }
            //add the file record
            Data data = new Data(offsets,fileInput.length());
            book.addRecord(folderAndFileName,data);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return the statistic data
        return new long[]{totalBlock,totalDuplicatedBlock,totalSizeOfBlock,totalSizeOfDuplicatedBlock,totalCompress};
    }

    /**
     * Close the FileOutputStream for writing.
     */
    public void finishWriting(){
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close RandomAccessMemory for reading (automatically will close the FileInputStream too).
     */
    public void finishReading(){
        try {
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to compress bytes into shorter length bytes.
     * @return compressedByte length
     */
    public byte[] compress(byte[] input) throws IOException {
        int i;
        byte[] buffer = new byte[blockSize];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater compressor = new Deflater();
        compressor.setInput(input);
        compressor.finish();
        while(!compressor.finished()){
            i = compressor.deflate(buffer);
            baos.write(buffer,0,i);
        }
        compressor.end();
        baos.close();
        return baos.toByteArray();
    }

    /**
     * Method to decompress bytes.
     * @return decompressed byte
     */
    public byte[] decompress(byte[] input, int compressedLen, int uncompressedLen) throws DataFormatException, UnsupportedEncodingException {
        //Decompresses the data
        Inflater decompressor = new Inflater();
        decompressor.setInput(input, 0, compressedLen);
        byte[] result = new byte[uncompressedLen];
        decompressor.inflate(result);
        decompressor.end();
        return result;
    }
}
