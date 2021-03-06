/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscOverlays;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.GuiElements.GradebookBrowser.GradebookView;
import DropboxGrader.GuiElements.MiscComponents.JGhostTextField;
import DropboxGrader.Printing.Print;
import DropboxGrader.Printing.PrintGradebook;
import DropboxGrader.Util.NamedRunnable;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author 141lyonsm
 */
public class PrintOverlay extends ContentOverlay implements DocumentListener,ChangeListener{
    private Gui gui;
    private GradebookView gradebookView;
    private Print printer;
    private PrintGradebook gradebookPrinter;
    private int currentPage;
    private PageFormat format;
    
    private JLabel iconLabel;
    private JButton printButton;
    private JComboBox modeField;
    private JButton backButton;
    private JButton forwardButton;
    private JLabel pageLabel;
    private JScrollPane scroll;
    private JTextField nameField;
    private JCheckBox wrapCells;
    private JComboBox orientation;
    private JPanel gradebookSettings;
    private JSlider scaleSlider;
    
    private JLabel spinnerLabel;
    private JLabel spinnerDescription;
    private JPanel loaderPanel;
    
    public PrintOverlay(Gui gui,GradebookView view){
        super("PrintOverlay");
        this.gui=gui;
        gradebookView=view;
        //init loader
        ImageIcon loader=new ImageIcon(getClass().getResource("/Resources/ajax-loader.gif"));
        spinnerLabel=new JLabel(loader);
        spinnerLabel.setOpaque(false);
        spinnerDescription=new JLabel("Generating Print Preview...");
        loaderPanel=new JPanel();
        loaderPanel.setLayout(new GridBagLayout());
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weightx=0;
        cons.weighty=0;
        loaderPanel.add(spinnerLabel,cons);
        cons.gridy++;
        loaderPanel.add(spinnerDescription,cons);
        }
    private void showLoader(){
        if(scroll!=null)
            remove(scroll);
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weighty=99;
        cons.weightx=1;
        cons.gridwidth=5;
        cons.fill=GridBagConstraints.BOTH;
        cons.insets=new Insets(5,5,0,5);
        add(loaderPanel,cons);
        revalidate();
        repaint();
    }
    private void hideLoader(){
        remove(loaderPanel);
        GridBagConstraints cons=new GridBagConstraints();
        cons.gridx=0;
        cons.gridy=0;
        cons.weighty=99;
        cons.weightx=1;
        cons.gridwidth=5;
        cons.fill=GridBagConstraints.BOTH;
        cons.insets=new Insets(5,5,0,5);
        add(scroll,cons);
        revalidate();
        repaint();
    }
    @Override
    public void setup() {
        showLoader();
        gui.getBackgroundThread().invokeLater(new Runnable() {
            @Override
            public void run() {
                gradebookPrinter=new PrintGradebook(gradebookView.getGradebookTable());
                printer=new Print(gui);
                format=printer.getFormat();
                int width=(int)(format.getWidth()*1.1f);
                int height=(int)(format.getHeight()*1.1f);
                if(height>=gui.getContentPane().getHeight()){
                    height=gui.getContentPane().getHeight();
                }
                if(width>=gui.getContentPane().getWidth()){
                    width=gui.getContentPane().getWidth();
                }
                setSize(width,height);
                setLocation((gui.getContentPane().getWidth()-width)/2,(gui.getContentPane().getHeight()-height)/2);
                
                BufferedImage image=new BufferedImage((int)format.getWidth(),(int)format.getHeight(),BufferedImage.TYPE_INT_ARGB);
                printer.printPreview(image.getGraphics(), currentPage);
                JPanel panel=new JPanel();
                panel.setLayout(new GridBagLayout());
                panel.setBackground(Color.LIGHT_GRAY);
                ImageIcon icon=new ImageIcon(image);
                iconLabel=new JLabel(icon);
                printButton=new JButton("Print");
                printButton.addActionListener(PrintOverlay.this);
                modeField=new JComboBox(Print.modes);
                modeField.addActionListener(PrintOverlay.this);
                backButton=new JButton("Back");
                backButton.addActionListener(PrintOverlay.this);
                forwardButton=new JButton("Forward");
                forwardButton.addActionListener(PrintOverlay.this);
                pageLabel=new JLabel();
                
                
                GridBagConstraints cons=new GridBagConstraints();
                cons.insets=new Insets(5,5,5,5);
                cons.gridx=0;
                cons.gridy=0;
                cons.weightx=1;
                cons.weighty=1;
                cons.gridwidth=4;
                cons.fill=GridBagConstraints.NONE;
                cons.anchor=GridBagConstraints.NORTH;
                panel.add(iconLabel,cons);

                scroll=new JScrollPane(panel);

                cons.gridx=0;
                cons.gridy=0;
                cons.weighty=99;
                cons.gridwidth=5;
                cons.fill=GridBagConstraints.BOTH;
                cons.insets=new Insets(5,5,0,5);
                add(scroll,cons);
                cons.fill=GridBagConstraints.NONE;
                cons.gridwidth=1;
                cons.gridy=1;
                cons.weighty=1;
                cons.anchor=GridBagConstraints.NORTHWEST;
                cons.weightx=5;
                add(printButton,cons);
                cons.anchor=GridBagConstraints.NORTH;
                cons.weightx=1;
                cons.gridx=1;
                add(modeField,cons);
                cons.gridx=2;
                add(backButton,cons);
                cons.gridx=3;
                add(pageLabel,cons);
                cons.gridx=4;
                add(forwardButton,cons);

                changePage(0);
            }
        });
        
        setTitle("Print Preview");
        int width=(int)(gui.getContentPane().getWidth()*0.4f);
        int height=(int)(gui.getContentPane().getHeight()*0.75f);
        setSize(width,height);
        setLocation((gui.getContentPane().getWidth()-width)/2,(gui.getContentPane().getHeight()-height)/2);
        setVisible(true);
        
    }
    private void changePage(final int newPage){
        gui.getBackgroundThread().removeQueued("GradebookPrintPreviewChangePage");
        gui.getBackgroundThread().invokeLater(new NamedRunnable() {
            @Override
            public void run() {
                currentPage=newPage;
                BufferedImage image=new BufferedImage((int)format.getImageableWidth(),(int)format.getImageableHeight(),BufferedImage.TYPE_INT_ARGB);
                if(modeField.getSelectedIndex()!=2)
                    printer.printPreview(image.getGraphics(), currentPage);
                else if(modeField.getSelectedIndex()==2){
                    gradebookPrinter.printPreview(image.getGraphics(), currentPage,Color.LIGHT_GRAY);
                }
                iconLabel.setIcon(new ImageIcon(image));
                int numPages=modeField.getSelectedIndex()!=2?printer.getNumPages():gradebookPrinter.getNumPages();
                if(currentPage>=numPages)
                    changePage(currentPage-1);
                else if(currentPage<0&&numPages>0)
                    changePage(currentPage+1);
                pageLabel.setText("Page "+(currentPage+1)+"/"+numPages);
                if(currentPage<=0){
                    backButton.setEnabled(false);
                }
                else{
                    backButton.setEnabled(true);
                }
                if(currentPage==numPages-1||numPages==0){
                    forwardButton.setEnabled(false);
                }
                else{
                    forwardButton.setEnabled(true);
                }
                hideLoader();
            }

            @Override
            public String name() {
                return "GradebookPrintPreviewChangePage";
            }
        });
    }
    @Override
    public void switchedTo() {}
    @Override
    public boolean isClosing(){return true;}
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(printButton)){
            gui.getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    if(modeField.getSelectedIndex()!=2){
                        Print p=new Print(gui);
                        p.setMode(printer.getMode());
                        p.setStudent(printer.getStudent());
                        p.print();
                    } else if(modeField.getSelectedIndex()==2){
                        gradebookPrinter.print();
                    }
                }
            });
        }
        else if(e.getSource().equals(backButton)){
            changePage(currentPage-1);
        }
        else if(e.getSource().equals(forwardButton)){
            changePage(currentPage+1);
        }
        else if(e.getSource().equals(modeField)){
            if(modeField.getSelectedIndex()!=2) //since we draw the jtable not manually, no need to tell the manual printer we're donig it.
                printer.setMode(modeField.getSelectedIndex());
            currentPage=0;
            showLoader();
            changePage(currentPage);
            if(Print.modes[modeField.getSelectedIndex()].equals("Specific Student Report")&&
                    nameField==null){
                nameField=new JGhostTextField(10,"Student Name");
                nameField.getDocument().addDocumentListener(this);
                GridBagConstraints cons=new GridBagConstraints();
                cons.anchor=GridBagConstraints.NORTH;
                cons.fill=GridBagConstraints.HORIZONTAL;
                cons.gridx=1;
                cons.gridy=2;
                cons.weightx=1;
                cons.weighty=0.1;
                add(nameField,cons);
                revalidate();
            }
            else if(modeField.getSelectedIndex()==2&&wrapCells==null){
                wrapCells=new JCheckBox("Wrap Cells");
                wrapCells.setToolTipText("Avoid cutting off cells.");
                wrapCells.setSelected(gradebookPrinter.getWrapMode());
                wrapCells.addActionListener(this);
                orientation=new JComboBox(new String[]{"Portrait","Landscape"});
                orientation.setToolTipText("Orientation");
                orientation.setSelectedIndex(gradebookPrinter.getLandscape()?1:0);
                orientation.addActionListener(this);
                scaleSlider=new JSlider(5,100,(int)(gradebookPrinter.getScale()*100));
                scaleSlider.addChangeListener(this);
                                
                GridBagConstraints cons=new GridBagConstraints();
                cons.anchor=GridBagConstraints.NORTH;
                cons.fill=GridBagConstraints.NONE;
                cons.gridx=0;
                cons.gridy=0;
                cons.weightx=1;
                cons.weighty=0.1;
                
                gradebookSettings=new JPanel();
                gradebookSettings.setLayout(new GridBagLayout());
                gradebookSettings.add(orientation,cons);
                cons.gridx++;
                gradebookSettings.add(wrapCells,cons);
                
                cons.gridx=0;
                cons.gridy=2;
                add(scaleSlider,cons);
                cons.gridx=1;
                add(gradebookSettings,cons);
                
                revalidate();
            }
                
            if(!Print.modes[modeField.getSelectedIndex()].equals("Specific Student Report")&&
                    nameField!=null){
                remove(nameField);
                nameField=null;
                revalidate();
            }
            if(modeField.getSelectedIndex()!=2&&wrapCells!=null){
                remove(gradebookSettings);
                remove(scaleSlider);
                wrapCells=null;
                orientation=null;
                scaleSlider=null;
                revalidate();
            }
        }
        else if(e.getSource().equals(orientation)){
            showLoader();
            gui.getBackgroundThread().removeQueued("GradebookPrintChangeOrientation");
            gui.getBackgroundThread().invokeLater(new NamedRunnable() {
                @Override
                public void run() {
                    gradebookPrinter.setLandscape(orientation.getSelectedIndex()==1);
                    changePage(currentPage);
                }

                @Override
                public String name() {
                    return "GradebookPrintChangeOrientation";
                }
            });
        }
        else if(e.getSource().equals(wrapCells)){
            showLoader();
            gui.getBackgroundThread().removeQueued("GradebookPrintChangeWrap");
            gui.getBackgroundThread().invokeLater(new NamedRunnable() {
                @Override
                public void run() {
                    gradebookPrinter.setWrap(wrapCells.isSelected());
                    changePage(currentPage);
                }

                @Override
                public String name() {
                    return "GradebookPrintChangeWrap";
                }
            });
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if(Print.modes[modeField.getSelectedIndex()].equals("Specific Student Report")){
            printer.setStudent(nameField.getText());
            changePage(currentPage);                
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(e.getSource().equals(scaleSlider)){
            showLoader();
            gui.getBackgroundThread().removeQueued("GenerateGradebookPrintPreview");
            gui.getBackgroundThread().invokeLater(new NamedRunnable() {
                @Override
                public void run() {
                    gradebookPrinter.setScale(scaleSlider.getValue()/100f);
                    changePage(currentPage);
                }

                @Override
                public String name() {
                    return "GenerateGradebookPrintPreview";
                }
            });
        }
    }
    
}
