/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.TextGrader;

/**
 *
 * @author Matt
 */
public class TextGrade {
    public String grade;
    public String comment;
    //public final Date DATEGRADED;
    //public final boolean INGRADEBOOK
    
    public TextGrade(String grade,String comment){
        this.grade=grade;
        this.comment=comment;
    }
    public TextGrade(String fromText){
        String[] text=fromText.split(TextSpreadsheet.INDIVIDUALDELIMITER);
        try{
            grade=text[0];
            comment=text[1];
        } catch(Exception e){
            //System.err.println("Error reading grade from \""+fromText+"\": "+e);
            //it is entirely normal to get this error, so we don't need to output anything
            //it will be thrown if someone chooses not to write a comment for a grade
        }
    }
    public String toText(){
        String text="";
        text+=grade+TextSpreadsheet.INDIVIDUALDELIMITER;
        text+=comment;
        
        return text;
    }
    @Override
    public String toString(){
        return "Grade "+grade+" "+comment;
    }
}
