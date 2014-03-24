package DropboxGrader.Printing;

/*
 * Modified heavilly from http://docs.oracle.com/javase/tutorial/2d/printing/examples/HelloWorldPrinter.java
 */ 


import DropboxGrader.Gui;
import DropboxGrader.TextGrader.TextAssignment;
import DropboxGrader.TextGrader.TextGrade;
import DropboxGrader.TextGrader.TextGrader;
import DropboxGrader.TextGrader.TextSpreadsheet;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Print implements Printable {
    private Gui gui;
    private Pageable pageable;
    private PrinterJob job;
    
    private int cachedPages;
    
    private int previousPage=-1;
    private int previousStudent=-1;
    private int previousMultiPageIndex=-1;
    private int previousAssignmentIndex=-1;
    private int previousAssignmentLine=-1;
    
    public Print(final Gui gui){
        this.gui=gui;
        
        job=PrinterJob.getPrinterJob();
        calcTotalPages();
        pageable=new Pageable() {
            @Override
            public int getNumberOfPages() {
                return cachedPages;
            }
            @Override
            public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
                return job.defaultPage();
            }
            @Override
            public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
                return Print.this;
            }
        };
    }
    public Print(Gui gui,boolean needPages){
        this.gui=gui;
        job=PrinterJob.getPrinterJob();
    }
    @Override
    public int print(Graphics g,PageFormat pf,int page) throws PrinterException {
        return printData(g,pf,page);
    }
    public void printPreview(Graphics g,int page){
        try{
            printData(g,job.defaultPage(),page);
        } catch(PrinterException e){
            System.err.println("Error generating print preview.\n"+e);
        }
    }
    private int printData(Graphics g, PageFormat pf, int pageNum) throws PrinterException {
        if(0>pageNum){
            return NO_SUCH_PAGE;
        }
        g.setColor(Color.black);

        /* User (0,0) is typically outside the imageable area, so we must
         * translate by the X and Y values in the PageFormat to avoid clipping
         */
        TextGrader grader=gui.getGrader();
        if(previousPage!=pageNum-1||pageNum==0){
            previousAssignmentIndex=-1;
            previousMultiPageIndex=-1;
            previousStudent=-1;
            previousAssignmentLine=0;
            previousAssignmentIndex=0;
            if(pageNum!=pageNum-1){
                BufferedImage i=new BufferedImage(1,1,BufferedImage.TYPE_3BYTE_BGR);
                printData(i.getGraphics(),pf,pageNum-1);
            }
        }
        if(previousStudent>=gui.getGrader().getSpreadsheet().numNames()){
            return NO_SUCH_PAGE;
        }
        Graphics2D g2d = (Graphics2D)g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        /* Now we perform our rendering */
        if(previousMultiPageIndex<1){
            if(previousStudent==-1){
                previousStudent++;
            }
            boolean finished=renderStudent(g2d,previousStudent,grader,pf,0);
            if(!finished){
                previousMultiPageIndex=1;
            }
            else{
                previousStudent++;
                previousMultiPageIndex=0;
                previousAssignmentIndex=-1;
                previousAssignmentLine=0;
            }
        }
        else if(previousPage==pageNum-1){
            boolean finished=renderStudent(g2d,previousStudent,grader,pf,previousMultiPageIndex);
            if(!finished){
                previousMultiPageIndex++;
            }
            else{
                previousStudent++;
                previousMultiPageIndex=0;
                previousAssignmentIndex=-1;
                previousAssignmentLine=0;
            }
        }
        previousPage=pageNum;
        /* tell the caller that this page is part of the printed document */
        return PAGE_EXISTS;
    }
    private boolean renderStudent(Graphics2D g,int studentIndex,TextGrader grader,PageFormat pf,int page){
        int marginY=10;
        double centerX=(int)(pf.getImageableWidth()/2.0)-pf.getImageableX()*0.5;
        TextSpreadsheet sheet=grader.getSpreadsheet();
        g.drawString(new Date().toString(),0, marginY);
        marginY+=25;
        if(page==0){
            g.setFont(g.getFont().deriveFont(Font.BOLD));
            g.drawString(sheet.getNameAt(studentIndex).toString(), (int)centerX, (int)marginY);
            g.setFont(g.getFont().deriveFont(Font.PLAIN));
            marginY+=25;
            
            previousAssignmentIndex=0;
            previousAssignmentLine=0;
        }
        for(int i=previousAssignmentIndex;i<sheet.numAssignments();i++){
            TextAssignment assign=sheet.getAssignmentAt(i);
            TextGrade grade=sheet.getGradeAt(i, studentIndex);
            if(previousAssignmentLine==0)
                g.drawString("Assignment "+assign.number+": "+assign.name,0,(int)marginY);
            else
                g.drawString("Assignment "+assign.number+" Continued:",0,(int)marginY);
            marginY+=20;
            if(grade!=null){
                if(!grade.comment.equals("")){
                    String str="Grade: "+grade.grade+" Comment: "+grade.comment;
                    String[] stringLines=lineWrap(str,(int)pf.getImageableWidth(),g);
                    for(int line=previousAssignmentLine;line<stringLines.length;line++){
                        String s=stringLines[line];
                        g.drawString(s,0,(int)marginY);
                        marginY+=15;
                        if(marginY>=pf.getImageableHeight()){
                            previousAssignmentIndex=i;
                            previousAssignmentLine=line+1;
                            return false;
                        }
                    }
                } else{
                    g.drawString("Grade: "+grade.grade,0,(int)marginY);
                }
                previousAssignmentLine=0;
            }
            else{
                g.setColor(Color.red);
                g.setFont(g.getFont().deriveFont(Font.BOLD));
                g.drawString("Missing",0,(int)marginY);
                g.setColor(Color.black);
                g.setFont(g.getFont().deriveFont(Font.PLAIN));
            }
            marginY+=25;
            if(marginY>=pf.getImageableHeight()){
                previousAssignmentIndex=i+1;
                previousAssignmentLine=0;
                return false;
            }
        }
        previousAssignmentLine=0;
        return true;
    }
    private String[] lineWrap(String str,int width,Graphics2D g){
        int lineWidth=g.getFontMetrics().stringWidth(str);
        double lines=lineWidth/(double)width;
        if(lines<=1){
            return new String[]{str};
        }
        
        String lastString=null;
        String[] split=str.split(" ");
        for(int i=0;i<split.length;i++){
            String combined="";
            for(int index=0;index<i;index++){
                if(index!=0){
                    combined+=" ";
                }
                combined+=split[index];
            }
            int curWidth=g.getFontMetrics().stringWidth(combined);
            if(curWidth<=width){
                lastString=combined;
            }
            else{
                if(lastString==null){ //there were no spaces so we will just cut it off
                    lastString=str;
                }
                break;
            }
        }
        String[] otherLines=lineWrap(str.substring(lastString.length()),width,g);
        String[] merged=new String[otherLines.length+1];
        for(int i=0;i<merged.length;i++){
            if(i==0){
                merged[i]=lastString;
            }
            else{
                merged[i]=otherLines[i-1];
            }
        }
        return merged;
        
    }
    public boolean print(){
        job.setPrintable(this);
        job.setJobName("Grade Reports");
        job.setPageable(pageable);
        
        boolean accepted=job.printDialog();
        if(accepted){
            try {
                job.print();
            } catch (PrinterException ex) {
                  /* The job did not successfully complete */
                return false;
            }
        }
        else{
            return false;
        }
        return true;
        
    }
    public PageFormat getFormat(){
        return job.defaultPage();
    }
    public int getNumPages(){
        return pageable.getNumberOfPages();
    }
    private void calcTotalPages(){
        int pages=0;
        BufferedImage i=new BufferedImage(1,1,BufferedImage.TYPE_3BYTE_BGR);
        boolean done=false;
        Print p=new Print(gui,false);
        while(!done){
            try {
                int value=p.printData(i.getGraphics(),job.defaultPage(),pages);
                if(value==Print.NO_SUCH_PAGE){
                    done=true;
                }
                else{
                    pages++;
                }
            } catch (PrinterException ex) {
                Logger.getLogger(Print.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        cachedPages=pages;
    }
}