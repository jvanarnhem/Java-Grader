/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.Printing;

import DropboxGrader.Gui;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import javax.swing.JTable;
import javax.swing.JViewport;

/**
 *
 * @author matt
 */
public class PrintGradebook {
    private Gui gui;
    private JTable table;
    private JViewport columns;
    private boolean landscapeMode;
    
    private PrinterJob job;
    public PrintGradebook(Gui gui,JTable gradebook,JViewport columnHeader){
        this.gui=gui;
        table=gradebook;
        columns=columnHeader;
        job=PrinterJob.getPrinterJob();
    }
    public void printPreview(Graphics g,int pageNum){
        BufferedImage columnsImage=new BufferedImage(columns.getBounds().width,columns.getBounds().height,BufferedImage.TYPE_INT_ARGB);
        columns.paint(columnsImage.getGraphics());
        BufferedImage tableImage=new BufferedImage(table.getBounds().width,table.getBounds().height,BufferedImage.TYPE_INT_ARGB);
        table.paint(tableImage.getGraphics());
        
        BufferedImage combinedImage=new BufferedImage(columnsImage.getWidth(),columnsImage.getHeight()+tableImage.getHeight(),
            BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2=(Graphics2D)combinedImage.getGraphics();
        g2.drawImage(columnsImage, null, null);
        g2.drawImage(tableImage, 0,columnsImage.getHeight(), null);
        Rectangle oldHorizontalCell=table.getCellRect(0, getNumHorizontalCells(pageNum-2), true);
        Rectangle oldVerticalCell=table.getCellRect(0, getNumVerticalCells(pageNum-2), true);
        Rectangle horizontalCell=table.getCellRect(0, getNumHorizontalCells(pageNum-1), true);
        Rectangle verticalCell=table.getCellRect(0, getNumVerticalCells(pageNum-1), true);
        g2.fillRect(horizontalCell.width+horizontalCell.x, 0, combinedImage.getWidth(), combinedImage.getHeight());
        //g2.clearRect(0, verticalCell.height+verticalCell.y, combinedImage.getWidth(), combinedImage.getHeight());
        
        g.translate(-(oldHorizontalCell.width+oldHorizontalCell.x), -0);
        if(landscapeMode){
            ((Graphics2D)g).rotate(Math.toRadians(90));
            g.drawImage(combinedImage, 0, 0, null);
        } else
            g.drawImage(combinedImage, 0, 0, null);
        g.translate(oldHorizontalCell.width+oldHorizontalCell.x,0);
    }
    private int getNumHorizontalCells(int page){
        if(page<0)
            return 0;
        int width=(int)job.defaultPage().getImageableWidth()*(page+1);
        
        int totalWidth=0;
        int startCell=0;
        if(page>0)
            startCell=getNumHorizontalCells(page-1);
        for(int i=startCell;i<table.getModel().getColumnCount();i++){
            totalWidth+=table.getCellRect(0, i, true).width;
            if(totalWidth>width){//one lower than our current index, but since we return size we don't subtract one
                return i-1;
            }
            if(totalWidth==width)
                return i; //current one, but since we are returning the number not the index add one
        }
        return totalWidth;
    }
    private int getNumVerticalCells(int page){
        if(page<0)
            return 0;
        int height=(int)job.defaultPage().getImageableHeight();
        
        int lastHeight;
        int totalHeight=columns.getHeight();
        for(int i=0;i<table.getModel().getRowCount();i++){
            lastHeight=totalHeight;
            totalHeight+=table.getCellRect(i,0,true).height;
            if(totalHeight>height)
                return lastHeight; //one lower than our current index, but since we return size we don't subtract one
            if(totalHeight==height)
                return totalHeight; //current one, but since we are returning the number not the index add one
        }
        return totalHeight;
    }
    public void setLandscape(boolean b){
        landscapeMode=b;
    }
}