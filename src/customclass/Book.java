package customclass;

import gnu.trove.THashMap;
import gnu.trove.TLongArrayList;
import gnu.trove.TObjectLongHashMap;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Book{
    private THashMap<String,Data> records;
    private THashMap<String,List<String>> folderDetails;
    private TObjectLongHashMap<ByteWrapper> offset;
    private String hashFunc = "MD5";

    Data getRecord(String filename){
        return records.get(filename);
    }

    void addRecord(String filename, Data data){
        records.put(filename,data);
    }

    boolean containsRecord(String filename){
        return records.containsKey(filename);
    }

    public Book(){
        records = new THashMap<>();
        folderDetails = new THashMap<>();
        offset = new TObjectLongHashMap<>();
    }

    public void addFolderDetails(String s, List<String> files){
        folderDetails.put(s,files);
    }

    public List<String> getFolderDetailsOf(String s){
        return folderDetails.get(s);
    }

    public void clean(){
        records = new THashMap<>();
        folderDetails = new THashMap<>();
    }

    public void cleanRecordOnly(){
        records = new THashMap<>();
    }

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

    private ByteWrapper convertToHash(String s) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance(hashFunc);
        ByteWrapper x = new ByteWrapper(md.digest(s.getBytes("UTF-8")));
        return x;
    }
}
