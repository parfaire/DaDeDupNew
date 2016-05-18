package customclass;

import gnu.trove.THashMap;
import gnu.trove.TLongArrayList;
import gnu.trove.TObjectLongHashMap;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Book class contains all the records of the "FILE" (individual file / folder with many files).
 * HashMap<String,Data> records = map of FILENAME and its content/data (see 'Data' class for more details).
 * HashMap<String,List<String>> folderDetails = map of DIRECTORYNAME and all of FILENAMES that belongs to it.
 * HashMap<ByteWrapper,Longr> offset = due to efficiency, records and folderDetails are not populated every time.
 *                                     This offset is a map of their address of where to obtain them.
 * String hashFunc = "MD5" = in "HashMap<ByteWrapper,Longr> offset": instead of storing filename,
 *                           storing its hash will be much more efficient.
 */
public class Book{
    private THashMap<String,Data> records;
    private THashMap<String,List<String>> folderDetails;
    private TObjectLongHashMap<ByteWrapper> offset;
    private String hashFunc = "MD5";

    /**
     * getRecord : Used in Deduplication-Read, to get the record data.
     * @param filename - name of the file.
     */
    public Data getRecord(String filename){
        return records.get(filename);
    }

    /**
     * addRecord : Used in Deduplication-Write, to add the record data.
     * @param filename - name of the file.
     * @param data - data of the file.
     */
    public void addRecord(String filename, Data data){
        records.put(filename,data);
    }

    /**
     * containsRecord : Used in Deduplication-Write, to get boolean whether the record already exist,
     *                                               making sure there is no duplication before adding it.
     * @param filename - name of the file.
     * @return - true or false.
     */
    public boolean containsRecord(String filename){
        return records.containsKey(filename);
    }

    /**
     * Constructor to instantiate the book class.
     */
    public Book(){
        records = new THashMap<>();
        folderDetails = new THashMap<>();
        offset = new TObjectLongHashMap<>();
    }

    /**
     * addFolderDetails, Used in Controller, to keep the folder structure.
     * @param s - folder name.
     * @param files - collection of its filenames.
     */
    public void addFolderDetails(String s, List<String> files){
        folderDetails.put(s,files);
    }

    /**
     * getFolderDetailsOf, Used in Controller, to get the folder structure.
     * @param s - folder name.
     * @return collection of the folder's filenames.
     */
    public List<String> getFolderDetailsOf(String s){
        return folderDetails.get(s);
    }

    /**
     * clean the records and folderDetails to optimise the memory efficiency.
     */
    public void clean(){
        records = new THashMap<>();
        folderDetails = new THashMap<>();
    }

    /**
     * clean the records only.
     */
    public void cleanRecordOnly(){
        records = new THashMap<>();
    }

    /**
     * writeRecords : Write the records into file.
     * @param output - a filename of where the records to be written.
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public void writeRecords(String output) throws IOException, NoSuchAlgorithmException {
        ByteWrapper hash;
        File fileOutput = new File(output);
        RandomAccessFile raf = new RandomAccessFile(fileOutput,"rw");
        raf.seek(fileOutput.length()); //append mode
        BufferedWriter bw = new BufferedWriter(new FileWriter(raf.getFD()));
        String filename;
        Data data;
        long thisOffset;
        for(Map.Entry<String, Data> entry : records.entrySet()) {
            filename = entry.getKey();
            data = entry.getValue();
            thisOffset = raf.getFilePointer();
            hash = convertToHash(filename);
            offset.put(hash, thisOffset);
            bw.write(filename+"\n");
            bw.write(thisOffset+" "+data.getSize()+"\n");
            TLongArrayList offsets = data.getOffsets();
            for(int i=0;i<offsets.size();i++){
                long l = offsets.get(i);
                bw.write(l+" ");
            }
            bw.write("\n");
            bw.flush();
        }
        bw.close();
    }

    /**
     * readRecords : Read the records from the file
     * @param input - a filename of where the records to be read
     * @param filename - what particular file's record to be obtained.
     * @throws IOException
     */
    public void readRecords(String input, String filename) throws IOException {
        long thisOffset,size;
        TLongArrayList offsets;
        try{
            FileInputStream fis = new FileInputStream(input);
            Scanner sc = new Scanner(fis);
            ByteWrapper hash = convertToHash(filename);
            thisOffset = offset.get(hash);
            fis.skip(thisOffset);
            sc.nextLine();
            sc.nextLong();
            size = sc.nextLong();
            sc.nextLine();
            offsets = new TLongArrayList();
            while (sc.hasNextLong()) {
                Long l = sc.nextLong();
                offsets.add(l);
            }
            records.put(filename, new Data(offsets,size));
            sc.close();
        }
        catch (Exception e){e.printStackTrace();}
    }

    /**
     * readOffset : read the offsets of records and folderDetails to populate them.
     * @param recordFile - a filename of where the records to be read
     * @param folderFile - a foldername of where the folderDetails to be read
     * @throws IOException
     */
    public void readOffset(String recordFile, String folderFile) throws IOException {
        ByteWrapper hash;
        long thisOffset;
        int numberOfFiles;
        String filename,folderName;
        try{
            Scanner sc = new Scanner(new FileInputStream(recordFile));
            while(sc.hasNextLine()) {
                filename = sc.nextLine();
                thisOffset = sc.nextLong();
                sc.nextLine();
                if(sc.hasNextLine())
                    sc.nextLine();
                hash = convertToHash(filename);
                offset.put(hash,thisOffset);
            }
            sc.close();
            sc = new Scanner(new FileInputStream(folderFile));
            while(sc.hasNextLine()) {
                folderName = sc.nextLine();
                thisOffset = sc.nextLong();
                numberOfFiles = sc.nextInt();
                sc.nextLine();
                for(int i=0;i<numberOfFiles;i++)
                    sc.nextLine();
                hash = convertToHash(folderName);
                offset.put(hash,thisOffset);
            }
            sc.close();
        }
        catch (Exception e){e.printStackTrace();}
    }

    /**
     * writeFolderDetails :  write the folderDetails from the files
     * @param fileFolderDetails - a foldername of where folderDetails to be read
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public void writeFolderDetails(String fileFolderDetails) throws IOException, NoSuchAlgorithmException {
        ByteWrapper hash;
        File fileOutput = new File(fileFolderDetails);
        RandomAccessFile raf = new RandomAccessFile(fileOutput,"rw");
        raf.seek(fileOutput.length()); //append mode
        BufferedWriter bw = new BufferedWriter(new FileWriter(raf.getFD()));
        long thisOffset;
        for(Map.Entry<String, List<String>> entry : folderDetails.entrySet()){
            String folderName = entry.getKey();
            List<String> files = entry.getValue();
            thisOffset = raf.getFilePointer();
            hash = convertToHash(folderName);
            offset.put(hash, thisOffset);
            bw.write(folderName+"\n");
            bw.write(thisOffset+" "+files.size()+"\n");
            for (String s : files) bw.write(s + "\n");
            bw.flush();
        }
        bw.close();
    }

    /**
     * readFolderDetails : read the folderDetails to file
     * @param input - a filename of where the folderDetails to be read
     * @param folderName - what particular folderDetails to be obtained.
     * @throws IOException
     */
    public void readFolderDetails(String input, String folderName) throws IOException {
        long thisOffset;
        int numberOfFiles;
        List<String> files;
        try{
            FileInputStream fis = new FileInputStream(input);
            Scanner sc = new Scanner(fis);
            ByteWrapper hash = convertToHash(folderName);
            thisOffset = offset.get(hash);
            fis.skip(thisOffset);
            sc.nextLine();
            sc.nextLong();
            numberOfFiles = sc.nextInt(); sc.nextLine();
            files = new ArrayList<>();
            for(int i=0;i<numberOfFiles;i++){
                files.add(sc.nextLine());
            }
            folderDetails.put(folderName,files);
            sc.close();
        }
        catch (Exception e){e.printStackTrace();}
    }

    /**
     * convertToHash - convert string to byte[].
     * @param s - file name
     * @return hashedBlock
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    private ByteWrapper convertToHash(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance(hashFunc);
        ByteWrapper x = new ByteWrapper(md.digest(s.getBytes("UTF-8")));
        return x;
    }
}
