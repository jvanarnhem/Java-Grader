/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DropboxGrader.TextGrader;

import DropboxGrader.Config;
import DropboxGrader.DbxSession;
import DropboxGrader.FileManager;
import static DropboxGrader.Config.CONFIGFILE;
import DropboxGrader.GuiHelper;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxWriteMode;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author 141lyonsm
 */
public class TextGrader {
    private DbxClient client;
    private FileManager manager;
    private String filenameRemote;
    private String downloadedRevision;
    private String filenameLocal;
    private File sheet;
    private TextSpreadsheet data;
    public TextGrader(FileManager manager,DbxClient client){
        this.client=client;
        this.manager=manager;
        data=new TextSpreadsheet();
        init();
    }
    private void init(){
        if(Config.dropboxSpreadsheetFolder==null){
            Config.reset(); //config corrupt
        }
        filenameLocal="Grades-Period"+Config.dropboxPeriod+".txt";
        filenameRemote="/"+Config.dropboxSpreadsheetFolder+"/"+filenameLocal;
        filenameLocal=manager.getDownloadFolder()+"/"+filenameLocal;
        sheet=new File(filenameLocal);
        downloadSheet();
    }
    private void downloadSheet(){
        try{
            DbxEntry.File entry=(DbxEntry.File)client.getMetadata(filenameRemote);
            if(entry!=null){ //file has already been created
                if(downloadedRevision!=null&&downloadedRevision.equals(entry.rev)){ //current version is downloaded, no need to do it again
                    return;
                }
                FileOutputStream f = new FileOutputStream(filenameLocal);
                client.getFile(filenameRemote, null, f); //download file
                f.close();
                downloadedRevision=entry.rev;
            }
            else{ //no spreadsheet file found
                createSheet();
            }
            //parse sheet into a 2d array;
            data.parse(sheet);
            
        } catch(DbxException | IOException e){
            System.err.println("An error occured while downloading the HTML spreadsheet. "+e);
        }
    }
    //make sheet, and save it to a file.
    private void createSheet(){
        try{
            File sheet=new File(filenameLocal);
            sheet.createNewFile();
            String code=TextSpreadsheet.COMMENTDELIMITER+"DO NOT EDIT THIS FILE MANUALLY.\n";
            DbxSession.writeToFile(sheet, code);
            upload();
        } catch(IOException e){
            System.err.println("An error occured while creating HTML spreadsheet. "+e);
        }
    }
    private boolean uploadTable(){
        data.writeToFile(sheet);
        return upload();
    }
    private boolean upload(){
        try{
            DbxEntry.File entry=(DbxEntry.File)client.getMetadata(filenameRemote);
            if(entry!=null&&downloadedRevision!=null){ //file has already been created
                if(!downloadedRevision.equals(entry.rev)){
                    //Merge changes.
                    //Need to write this part
                    System.err.println("A different revision was downloaded than was uploaded.");
                }
            }
            //upload to dropbox
            FileInputStream sheetStream = new FileInputStream(sheet);
            client.uploadFile(filenameRemote, DbxWriteMode.force(), sheet.length(), sheetStream);
            sheetStream.close();
        } catch(DbxException | IOException e){
            System.err.println("Error uploading spredsheet to dropbox. "+e);
            return false;
        }
        return true;
    }
    public static int indexOf(String substring,String str){
        boolean inSub=false;
        int subIndex=0;
        int startIndex=-1;
        for(int x=0;x<str.length();x++){
            char c=str.charAt(x);
            if(substring.charAt(subIndex)==c){
                if(!inSub){
                    startIndex=x;
                    inSub=true;
                    subIndex=0;
                }
                if(subIndex==substring.length()-1){
                    return startIndex; //will return at the first instance of substring
                }
                subIndex++;
            }
            else{
                inSub=false;
                subIndex=0;
                startIndex=-1;
            }
        }
        return -1;
    }
    public boolean setGrade(String name,int assignmentNum,String gradeNum,String comment,boolean overwrite){
        downloadSheet();
        if(!data.nameDefined(name)){ //need to put name in gradebook
            String[] nameParts=splitName(name);
            data.addName(nameParts[0],nameParts[1]);
            name=nameParts[0]+nameParts[1];
        }
        if(!data.assignmentDefined(assignmentNum)){ //need to create assignment in table
            String assignmentDescription=JOptionPane.showInputDialog("What is the name of assignment "+assignmentNum+"?");
            data.addAssignment(assignmentNum, assignmentDescription);
        }
        boolean gradeSet=data.setGrade(data.getName(name),data.getAssignment(assignmentNum), gradeNum, comment,overwrite);
        if(!gradeSet){
            return false;
        }
        //convert to code, write and upload
        return uploadTable();
    }
    public String getGrade(String name,int assignmentNum){
        TextGrade grade=data.getGrade(data.getName(name), data.getAssignment(assignmentNum));
        return grade==null? null:grade.GRADE;
    }
    public String getComment(String name,int assignmentNum){
        TextGrade grade=data.getGrade(data.getName(name), data.getAssignment(assignmentNum));
        return grade==null? null:grade.COMMENT;
    }
    public boolean gradeWritten(String name,int assignmentNum){
        return getGrade(name,assignmentNum)!=null;
    }
    
    public void refresh(){
        init();
    }
    private String[] splitName(String name){
        String firstName,lastName;
        int upercaseIndex=-1;
        char c;
        for(int x=0;x<name.length();x++){
            c=name.charAt(x);
            if(Character.isUpperCase(c)){
                if(x!=0){
                    upercaseIndex=x;
                    break;
                }
            }
        }
        if(upercaseIndex==-1){
            firstName=JOptionPane.showInputDialog(null,name+" does not follow proper capitalization.\n Please enter the FIRST name",name);
            int firstNameIndex=indexOf(firstName,name);
            if(firstNameIndex==0)
                lastName=name.substring(firstName.length());
            else if(firstNameIndex!=-1)
                lastName=name.substring(0,firstNameIndex);
            else
                lastName=JOptionPane.showInputDialog(null,name+" does not follow proper capitalization.\n Please enter the LAST name",name);
        }
        else{
            firstName=name.substring(0, upercaseIndex);
            lastName=name.substring(upercaseIndex, name.length());
        }
        
        return new String[] {firstName,lastName};
    }
}
