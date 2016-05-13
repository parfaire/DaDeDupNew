package controller;

import customclass.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import ui.MainWindow;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import javax.swing.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.Timer;

public class Controller {
    private String PATH = "/Users/parfaire/IdeaProjects/DataDeduplication/dd/";
    private String excel = PATH+"excel.csv";
    private String storage = PATH+"storage.txt";
    private String ddtfile = PATH+"ddt.txt";
    private String records = PATH+"records.txt";
    private String folderdetails = PATH+"folderdetails.txt";
    private String listfiles = PATH+"list.txt";
    private String hashFunc;
	private MainWindow mainWindow;
    private Ddt ddt = new Ddt();
    private Book book = new Book();
    private Deduplication deduplication;
    private int count,totalDuplicatedBlock, blockSize;
    private long totalDuplicatedBlockSize, totalBlock, totalCompress;
    private HSSFWorkbook workbook;
    private HSSFSheet sheet;

    public void setParameter(int blockSize,String hashFunc) {
        this.blockSize= blockSize;
        this.hashFunc = hashFunc;
        deduplication = new Deduplication(storage,book,ddt,hashFunc,blockSize);
    }

    public Controller(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
        try{
            book.readOffset(records,folderdetails);
            ddt.read(ddtfile);
            refreshStatus();
            Timer memoryTimer = new Timer();
            memoryTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    long mem  = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                    System.out.println("*Memory usage:"+NumberFormat.getInstance().format(mem/1024) + " KB");
                }
            },3*1000,3*1000);
            workbook = new HSSFWorkbook();
            sheet = workbook.createSheet("Deduplication");
            HSSFRow rowhead = sheet.createRow((short)0);
            rowhead.createCell(0).setCellValue("File Name");
            rowhead.createCell(1).setCellValue("Block Size");
            rowhead.createCell(2).setCellValue("Hash Function");
            rowhead.createCell(3).setCellValue("Hit Rate");
            rowhead.createCell(4).setCellValue("Duplicated Block");
            rowhead.createCell(5).setCellValue("Total Block");
            rowhead.createCell(6).setCellValue("Total Size");
            rowhead.createCell(7).setCellValue("Expected Total");
            rowhead.createCell(8).setCellValue("Save Ratio");
//            rowhead.createCell(9).setCellValue("Compress Ratio");
//            rowhead.createCell(10).setCellValue("Deduplication Ratio");
            rowhead.createCell(9).setCellValue("Duration");



        } catch(Exception e){
            System.err.println("There is no ddt and book to be loaded.");
        }
	}
	public long getActualTotal(){
        long d = new File(ddtfile).length();
        long r = new File(records).length();
        long f = new File(folderdetails).length();
        long s = new File(storage).length();
        long tot = d+r+f+s;
        return tot;
    }
	public void refreshStatus() {
        setList();
        monitorMemory();
        Long d = new File(ddtfile).length();
        Long r = new File(records).length();
        Long f = new File(folderdetails).length();
        Long s = new File(storage).length();
        Long tot = d+r+f+s;
		mainWindow.getStatusPanel().setLblDdt(d);
        mainWindow.getStatusPanel().setLblRecord(r);
        mainWindow.getStatusPanel().setLblFolder(f);
        mainWindow.getStatusPanel().setLblStorage(s);
        mainWindow.getStatusPanel().setLblTotal(tot);
        mainWindow.getStatusPanel().setTfDdt(ddtfile);
        mainWindow.getStatusPanel().setTfRecord(records);
        mainWindow.getStatusPanel().setTfFolder(folderdetails);
        mainWindow.getStatusPanel().setTfStorage(storage);
        mainWindow.getStatusPanel().setLblDedup();
        mainWindow.getStatusPanel().updateUI();
	}

    /**
     * flushToDisk is a function to write the ddt, book(record&folder) into disk and free the memory allocation.
     * To be called after write operation of deduplication (in the InterfacePanel).
     */
    public void flushToDisk() {
        try {
            ddt.write(ddtfile);
            book.writeRecords(records);
            book.writeFolderDetails(folderdetails);
            book.clean();
        }catch (Exception e){
            e.printStackTrace();
        }
        refreshStatus();
    }

    public void read(String target, String folderAndFileName){
        try{
            deduplication.readyToRead();
            if(folderAndFileName.contains("/")) { //if directory
                book.readFolderDetails(folderdetails,folderAndFileName);
                readDirectory(target, folderAndFileName);
            }
            else{
                book.readRecords(records,folderAndFileName);
                deduplication.read(target, folderAndFileName);
            }
            deduplication.finishReading();
            book.clean();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    public void readDirectory(String target, String folderName){
        List<String> al = book.getFolderDetailsOf(folderName);
        for( String folderAndFileName : al ){
            readDirectoryRec(target,folderAndFileName);
        }
    }

    public void readDirectoryRec(String target, String folderAndFileName) {
        try {
            if(folderAndFileName.substring(folderAndFileName.length()-1).equals("/"))//if directory (last char = "/")
                readDirectory(target, folderAndFileName);
            else{
                book.readRecords(records, folderAndFileName);
                deduplication.read(target, folderAndFileName);
            }
            book.cleanRecordOnly();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String folderAndFileName, File f) throws IOException {
        long[] longArray;
        long startTime,endTime,duration,actualTotal,expectedTotal;
        totalBlock=0;
        totalDuplicatedBlock=0;
        totalDuplicatedBlockSize=0;
        count=0;
        deduplication.readyToWrite();
        startTime = System.nanoTime();
        if(!f.isDirectory()) {
            longArray = deduplication.write(folderAndFileName, f);
            totalBlock += longArray[0];
            totalDuplicatedBlock += longArray[1];
            totalDuplicatedBlockSize += longArray[2];
            totalCompress += longArray[3];
//            System.out.println(longArray[2]);
//            System.out.println(longArray[3]);
            count += 1;
            writeDirInfo(folderAndFileName,f.length());
        }
        else
            writeDirectory(folderAndFileName+"/",f);
        deduplication.finishWriting();
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        flushToDisk();

        //OUTPUT

        actualTotal = getActualTotal();
        expectedTotal = readDirInfo();
        double hitRate = (double)totalDuplicatedBlock/(double)totalBlock*100;

        HSSFRow row = sheet.createRow(sheet.getLastRowNum()+1);
        row.createCell(0).setCellValue(f.getName());
        row.createCell(1).setCellValue(blockSize);
        row.createCell(2).setCellValue(hashFunc);
        row.createCell(3).setCellValue(hitRate);
        row.createCell(4).setCellValue(totalDuplicatedBlock);
        row.createCell(5).setCellValue(totalBlock);
        row.createCell(6).setCellValue(actualTotal);
        row.createCell(7).setCellValue(expectedTotal);
        row.createCell(8).setCellValue((double)expectedTotal/(double)actualTotal);
        //row.createCell(9).setCellValue((double)expectedTotal/(double)totalDuplicatedBlockSize);
        //row.createCell(10).setCellValue((double)expectedTotal/(double)totalCompress);
        row.createCell(9).setCellValue((double)duration/1000);

        System.out.println(totalDuplicatedBlockSize);
        System.out.println(totalCompress);
        FileOutputStream fileOut = new FileOutputStream(excel);
        workbook.write(fileOut);
        fileOut.close();
        System.out.println("File : " + f.getName() +" process has finished");
        //System.out.println("Your excel file has been generated!");

//        System.out.format("Parameter : (Block Size : %d | Hash Function : %s)\n", blockSize, hashFunc);
//        System.out.format("Number of files : %d\n",count);
//        System.out.format("Hit Rate : %f",(double)totalDuplicatedBlock/(double)totalBlock*100);
//        System.out.println("% (Number Duplicated Blocks : "+totalDuplicatedBlock+" | Total Blocks : "+totalBlock+")");
//        System.out.format("Expected Total : %d KB\n",expectedTotal/1024);
//        System.out.format("Actual Total : %d KB\n",actualTotal/1024);
//        System.out.format("Total Saving Ratio : %fx\n",(double)expectedTotal/(double)actualTotal);
//        System.out.format("Duration : %fs\n\n\n",(double)duration/1000);
    }

    public long writeDirectoryRec(String folderName, File directory, List<String> listOfFiles, long s){
        long[] longArray;
        long size = s;
        for( File f : directory.listFiles() ){
            if(f.isDirectory()) {
                size += writeDirectoryRec(folderName,f, listOfFiles,0);
            }else{
                int startIdx = f.getAbsolutePath().indexOf(folderName);
                String folderAndFileName = f.getAbsolutePath().substring(startIdx);
                listOfFiles.add(folderAndFileName);
                longArray = deduplication.write(folderAndFileName,f); //[0] = total, [1]=duplicated
                totalBlock += longArray[0];
                totalDuplicatedBlock += longArray[1];
                count += 1;
                size+= f.length();
            }
        }
        return size;
    }
    public void writeDirectory(String folderName, File directory){
        List<String> listOfFiles = new ArrayList<>();
        long size = writeDirectoryRec(folderName,directory,listOfFiles,0);
        book.addFolderDetails(folderName,listOfFiles);
        writeDirInfo(folderName,size);
    }

    private void writeDirInfo(String s, long l){
        try {
            PrintWriter pw = new PrintWriter(new FileOutputStream(new File(listfiles),true));
            pw.println(s);
            pw.println(l);
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private long readDirInfo(){
        long total=0;
        try {
            Scanner sc = new Scanner(new FileInputStream(listfiles));
            while(sc.hasNextLine()){
                sc.nextLine();
                total += sc.nextLong(); sc.nextLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    private void setList(){
        long total = 0;
        NumberFormat format = NumberFormat.getInstance();
        List<String> filenames = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new FileInputStream(listfiles));
            while(sc.hasNextLine()){
                String name = sc.nextLine();
                long size = sc.nextLong(); sc.nextLine();
                total += size;
                filenames.add(name + " (" + format.format(size/1024)+ " KB)");
            }
            sc.close();
            String[] fnArray = new String[filenames.size()];
            filenames.toArray(fnArray);
            mainWindow.getInterfacePanel().getList().setListData(fnArray);
            mainWindow.getStatusPanel().setLblExpectedTotal(total);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void clearData(){
        try{
            PrintWriter pw = new PrintWriter(records);pw.close();
            pw = new PrintWriter(folderdetails);pw.close();
            pw = new PrintWriter(ddtfile);pw.close();
            pw = new PrintWriter(storage);pw.close();
            pw = new PrintWriter(listfiles);pw.close();
            ddt = new Ddt();
            book = new Book();
            deduplication = new Deduplication(storage,book,ddt,hashFunc,blockSize);
            refreshStatus();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public void compareTwoDir(String dir1, String dir2){
        String x = calcMD5HashForDir(new File(dir1),true);
        String y = calcMD5HashForDir(new File(dir2),true);
        if(x.equals(y)){
            JOptionPane.showMessageDialog(null,"Two directories contain the same files\n"+dir1+" = "+x+"\n"+dir2+" = "+y);
        }else{
            JOptionPane.showMessageDialog(null,"Two directories are different\n"+dir1+" = "+x+"\n"+dir2+" = "+y);
        }
    }

    public String calcMD5HashForDir(File dirToHash, boolean includeHiddenFiles) {

        assert (dirToHash.isDirectory());
        Vector<FileInputStream> fileStreams = new Vector<>();

        collectInputStreams(dirToHash, fileStreams, includeHiddenFiles);

        SequenceInputStream seqStream =
                new SequenceInputStream(fileStreams.elements());

        try {
            String md5Hash = DigestUtils.md5Hex(seqStream);
            seqStream.close();
            return md5Hash;
        }
        catch (IOException e) {
            throw new RuntimeException("Error reading files to hash in "
                    + dirToHash.getAbsolutePath(), e);
        }

    }

    private void collectInputStreams(File dir,
                                     List<FileInputStream> foundStreams,
                                     boolean includeHiddenFiles) {

        File[] fileList = dir.listFiles();
        Arrays.sort(fileList,               // Need in reproducible order
                new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return f1.getName().compareTo(f2.getName());
                    }
                });

        for (File f : fileList) {
            if (!includeHiddenFiles && f.getName().startsWith(".")) {
                // Skip it
            } else if (f.isDirectory()) {
                collectInputStreams(f, foundStreams, includeHiddenFiles);
            } else {
                try {
                    //System.out.println("\t" + f.getAbsolutePath());
                    foundStreams.add(new FileInputStream(f));
                } catch (FileNotFoundException e) {
                    throw new AssertionError(e.getMessage()
                            + ": file should never not be found!");
                }
            }
        }
    }

    public void monitorMemory(){
        Runtime runtime = Runtime.getRuntime();
        NumberFormat format = NumberFormat.getInstance();
        StringBuilder sb = new StringBuilder();
        long total = runtime.totalMemory();
        long used  = runtime.totalMemory() - runtime.freeMemory();
        sb.append("Total memory: " + format.format(total / 1024) + "KB | ");
        sb.append("Used memory: " + format.format(used / 1024) + "KB | ");
        mainWindow.getStatusPanel().setLblMemory(sb.toString());
    }
}
