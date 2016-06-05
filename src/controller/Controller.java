package controller;

import customclass.*;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import ui.MainWindow;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import java.io.*;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.*;
import java.util.Timer;

public class Controller {
    private String PATH,excel,storage,ddtfile,records,folderdetails,listfiles,hashFunc;
	private MainWindow mainWindow;
    private Ddt ddt;
    private Book book;
    private Deduplication deduplication;
    private int totalDuplicatedBlock, blockSize;
    private long totalSizeOfBlock, totalSizeOfDuplicatedBlock, totalBlock, totalCompress;
    private HSSFWorkbook workbook;
    private HSSFSheet sheet;

    /**
     * Set the parameter of system.
     * @param blockSize block size of block level deduplication.
     * @param hashFunc hash function for fingerprinting.
     */
    public void setParameter(int blockSize,String hashFunc) {
        this.blockSize= blockSize;
        this.hashFunc = hashFunc;
        deduplication = new Deduplication(storage,book,ddt,hashFunc,blockSize);
    }

    /**
     * Constructor to prepare all the files and objects needed. Update the status and activate memory watcher.
     * @param mainWindow
     */
    public Controller(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
        try{
            //create all the files if not exist
            prepareFiles();
            book = new Book();
            book.readOffset(records,folderdetails);
            ddt = new Ddt();
            ddt.read(ddtfile);
            refreshStatus();
            monitorMemoryTimer();
        } catch(Exception e){
            System.err.println("There is no ddt and book to be loaded.("+e.getMessage()+")");
        }
	}

    /**
     * A timer that ticking every 3s to update the memory usage status.
     */
    private void monitorMemoryTimer() {
        Timer memoryTimer = new Timer();
        memoryTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                monitorMemory();
            }
        },3*1000,3*1000);
    }

    /**
     * Prepare the excel file to where we store all the result observation.
     */
    private void prepareExcel() {
        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet("Deduplication");
        HSSFRow rowhead = sheet.createRow((short)0);
        rowhead.createCell(0).setCellValue("File Name");
        rowhead.createCell(1).setCellValue("Block Size");
        rowhead.createCell(2).setCellValue("Hash Function");
        rowhead.createCell(3).setCellValue("Dedup Hit Rate (%)");
        rowhead.createCell(4).setCellValue("Duplicated Block");
        rowhead.createCell(5).setCellValue("Total Block");
        rowhead.createCell(6).setCellValue("Actual Size (MB)");
        rowhead.createCell(7).setCellValue("Original Size (MB)");
        rowhead.createCell(8).setCellValue("Save Ratio");
        rowhead.createCell(9).setCellValue("Compression Rate (%)");
        rowhead.createCell(10).setCellValue("Deduplication Ratio (%)");
        rowhead.createCell(11).setCellValue("Duration (s)");
        rowhead.createCell(12).setCellValue("Storage Size (MB)");
        rowhead.createCell(13).setCellValue("DDT+Record Size (MB)");
    }

    /**
     * Configure the dynamic path on where the system located. Create all the system files if they dont exist.
     * @throws IOException
     */
    private void prepareFiles() throws IOException {
        PATH = Paths.get("").toAbsolutePath().toString()+"/dd/";
        excel = PATH+"excel.csv";
        storage = PATH+"storage.txt";
        ddtfile = PATH+"ddt.txt";
        records = PATH+"records.txt";
        folderdetails = PATH+"folderdetails.txt";
        listfiles = PATH+"list.txt";
        File f = new File(PATH);
        if(!f.exists())
            f.mkdir();
        f = new File(excel);
        if(!f.exists())
            f.createNewFile();
        f = new File(storage);
        if(!f.exists())
            f.createNewFile();
        f = new File(ddtfile);
        if(!f.exists())
            f.createNewFile();
        f = new File(records);
        if(!f.exists())
            f.createNewFile();
        f = new File(folderdetails);
        if(!f.exists())
            f.createNewFile();
        f = new File(listfiles);
        if(!f.exists())
            f.createNewFile();

        prepareExcel();
    }

    /**
     * function to get total of the system's space
     * @return total of system files.
     */
    public long getActualTotal(){
        long d = new File(ddtfile).length();
        long r = new File(records).length();
        long f = new File(folderdetails).length();
        long s = new File(storage).length();
        long tot = d+r+f+s;
        return tot;
    }

    /**
     * Update the status panel to show the up-to-date information.
     */
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
     * To be called after an entire write operation of file/folder.
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

    /**
     * flushBookOnly is a function to write the book(record&folder) into disk and free the memory allocation.
     * To be called after a single write operation of a file.
     */
    public void flushBookOnly() {
        try {
            book.writeRecords(records);
            book.writeFolderDetails(folderdetails);
            book.clean();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * function to read a file/folder from the system and retrieve it back.
     * @param target the path where we want to retrieve the file/folder.
     * @param folderAndFileName the file/folder name.
     */
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

    /**
     * function to handle reading directory (multiple files). This triggers the recursive reading: readDirectoryRec().
     * @param target the path where we want to retrieve the folder.
     * @param folderName the folder name.
     */
    public void readDirectory(String target, String folderName){
        List<String> al = book.getFolderDetailsOf(folderName);
        for( String folderAndFileName : al ){
            readDirectoryRec(target,folderAndFileName);
        }
    }

    /**
     * recursive function to recursively read the files, iterate for each file in the folders until finishes.
     * @param target the path where we want to retrieve the folder.
     * @param folderAndFileName the file name with its folder structure.
     */
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

    /**
     * write function to simply write a single file or a folder to the system.
     * @param folderAndFileName file/folder name.
     * @param f file/folder itself.
     * @throws IOException
     */
    public void write(String folderAndFileName, File f) throws IOException {
        long[] longArray;
        long startTime,endTime,duration;
        totalBlock=0;
        totalDuplicatedBlock=0;
        totalSizeOfBlock=0;
        totalSizeOfDuplicatedBlock=0;
        totalCompress=0;
        deduplication.readyToWrite();
        startTime = System.nanoTime();
        if(!f.isDirectory()) {
            longArray = deduplication.write(folderAndFileName, f);
            totalBlock += longArray[0];
            totalDuplicatedBlock += longArray[1];
            totalSizeOfBlock += longArray[2];
            totalSizeOfDuplicatedBlock += longArray[3];
            totalCompress += longArray[4];
            writeDirInfo(folderAndFileName,f.length());
        }
        else
            writeDirectory(folderAndFileName+"/",f); //add "/" to its name to indicates its a folder, the reason why we need a separated parameter just for a name
        deduplication.finishWriting();
        endTime = System.nanoTime();
        duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        flushToDisk();
        //OUTPUT
        printOutput(f.getName(),duration);

    }

    /**
     * recursive function to recursively write the files, iterate for each file in the folders until finishes.
     * @param folderName the name of folder/directory
     * @param directory the folder/directory itself
     * @param listOfFiles to populate the structure of folder and to be saved in folderDetails.
     * @param s size counter to count the total size of the folder/directory.
     * @return
     */
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
                totalSizeOfBlock += longArray[2];
                totalSizeOfDuplicatedBlock += longArray[3];
                totalCompress += longArray[4];
                size+= f.length();
                //flush the books
                flushBookOnly();
            }
        }
        return size;
    }

    /**
     * function to handle writing directory (multiple files). This triggers the recursive writing: writeDirectoryRec().
     * @param folderName the name of folder/directory
     * @param directory the folder/directory itself
     */
    public void writeDirectory(String folderName, File directory){
        List<String> listOfFiles = new ArrayList<>();
        long size = writeDirectoryRec(folderName,directory,listOfFiles,0);
        book.addFolderDetails(folderName,listOfFiles);
        writeDirInfo(folderName,size);
    }

    /**
     * Print the output of file/folder that has been sucessfully written.
     * the output consists of its statistics: its efficiency,performance,effectiveness.
     * @param filename name of the file.
     * @param duration the duration of its writing process.
     * @throws IOException
     */
    private void printOutput(String filename,long duration) throws IOException {
        //get the information
        long actualTotal = getActualTotal();
        long expectedTotal = readDirInfo();
        double hitRate = (double)totalDuplicatedBlock/(double)totalBlock*100;
        double saveRatio = (double)expectedTotal/(double)actualTotal;
        double compressionRate = ((double)1-((double)totalCompress/(double)expectedTotal))*100;
        double dedupRate = ((double)totalSizeOfDuplicatedBlock/(double)totalSizeOfBlock)*100;

        //put all the information to excel row
        HSSFRow row = sheet.createRow(sheet.getLastRowNum()+1);
        row.createCell(0).setCellValue(filename);
        row.createCell(1).setCellValue(blockSize);
        row.createCell(2).setCellValue(hashFunc);
        row.createCell(3).setCellValue((double)Math.round(hitRate*100)/100);
        row.createCell(4).setCellValue(totalDuplicatedBlock);
        row.createCell(5).setCellValue(totalBlock);
        row.createCell(6).setCellValue((double)Math.round(((double)actualTotal/1024/1024)*100)/100);
        row.createCell(7).setCellValue((double)Math.round(((double)expectedTotal/1024/1024)*100)/100);
        row.createCell(8).setCellValue((double)Math.round(saveRatio*100)/100);
        row.createCell(9).setCellValue((double)Math.round(compressionRate*100)/100);
        row.createCell(10).setCellValue((double)Math.round(dedupRate*100)/100);
        row.createCell(11).setCellValue((double)Math.round((double)duration/1000*10)/10);
        row.createCell(12).setCellValue((double)Math.round(((double)totalCompress/1024/1024)*100)/100); //storage
        row.createCell(13).setCellValue((double)Math.round(((double)(actualTotal-totalCompress)/1024/1024)*100)/100); //ddt+record+folder
        //write the row to excel file
        FileOutputStream fileOut = new FileOutputStream(excel);
        workbook.write(fileOut);
        fileOut.close();

        //give notification that process is done
        System.out.println("File : " + filename +" process has finished - BlockSize: "+blockSize+" | HashFunc: "+hashFunc);
    }

    /**
     * Write the information of written file to the disk.
     * @param s file name.
     * @param l file size.
     */
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

    /**
     * Read the size of each file in the system then return it.
     * @return total original size of all the files stored in the system.
     */
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

    /**
     * populate the ui list with the names of all the files that have written.
     */
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

    /**
     * Clear all the deduplication system data.
     */
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

    /**
     * Obtain the memory status from JVM. Print it to console and update the one in UI.
     */
    public void monitorMemory(){
        Runtime runtime = Runtime.getRuntime();
        NumberFormat format = NumberFormat.getInstance();
        StringBuilder sb = new StringBuilder();
        long used  = runtime.totalMemory() - runtime.freeMemory();
        sb.append("Used memory: " + format.format(used / 1024) + "KB");
        System.out.println("Used memory: " + format.format(used / 1024) + "KB");
        mainWindow.getStatusPanel().setLblMemory(sb.toString());
    }
}
