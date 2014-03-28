/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.GuiElements.MiscOverlays;

import DropboxGrader.Gui;
import DropboxGrader.GuiElements.ContentOverlay;
import DropboxGrader.Printing.Print;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author 141lyonsm
 */
public class PrintOverlay extends ContentOverlay implements DocumentListener,FocusListener{
    private Gui gui;
    private Print printer;
    private int currentPage;
    private PageFormat format;
    
    private JLabel iconLabel;
    private JButton print;
    private JComboBox mode;
    private JButton backButton;
    private JButton forwardButton;
    private JLabel pageLabel;
    private JScrollPane scroll;
    private JTextField nameField;
    
    private final String initialText="Student Name";
    
    public PrintOverlay(Gui gui){
        super("PrintOverlay");
        this.gui=gui;
        printer=new Print(gui);
    }
    @Override
    public void setup() {
        format=printer.getFormat();
        BufferedImage image=new BufferedImage((int)format.getWidth(),(int)format.getHeight(),BufferedImage.TYPE_INT_ARGB);
        printer.printPreview(image.getGraphics(), currentPage);
        
        JPanel panel=new JPanel();
        panel.setLayout(new GridBagLayout());
        ImageIcon icon=new ImageIcon(image);
        iconLabel=new JLabel(icon);
        print=new JButton("Print");
        print.addActionListener(this);
        mode=new JComboBox(Print.modes);
        mode.addActionListener(this);
        backButton=new JButton("Back");
        backButton.addActionListener(this);
        forwardButton=new JButton("Forward");
        forwardButton.addActionListener(this);
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
        add(print,cons);
        cons.anchor=GridBagConstraints.NORTH;
        cons.weightx=1;
        cons.gridx=1;
        add(mode,cons);
        cons.gridx=2;
        add(backButton,cons);
        cons.gridx=3;
        add(pageLabel,cons);
        cons.gridx=4;
        add(forwardButton,cons);
        
        changePage(0);
        
        setTitle("Print Preview");
        setResizable(true);
        setClosable(true);
        setMaximizable(true);
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
        setVisible(true);
        
    }
    private void changePage(int newPage){
        currentPage=newPage;
        BufferedImage image=new BufferedImage((int)format.getWidth(),(int)format.getHeight(),BufferedImage.TYPE_INT_ARGB);
        printer.printPreview(image.getGraphics(), currentPage);
        iconLabel.setIcon(new ImageIcon(image));
        pageLabel.setText("Page "+(currentPage+1)+"/"+printer.getNumPages());
        if(currentPage==0){
            backButton.setEnabled(false);
        }
        else{
            backButton.setEnabled(true);
        }
        int numPages=printer.getNumPages();
        if(currentPage==numPages-1){
            forwardButton.setEnabled(false);
        }
        else{
            forwardButton.setEnabled(true);
        }
    }
    @Override
    public void switchedTo() {}
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(print)){
            gui.getBackgroundThread().invokeLater(new Runnable() {
                @Override
                public void run() {
                    Print p=new Print(gui);
                    p.print();
                }
            });
        }
        else if(e.getSource().equals(backButton)){
            changePage(currentPage-1);
        }
        else if(e.getSource().equals(forwardButton)){
            changePage(currentPage+1);
        }
        else if(e.getSource().equals(mode)){
            printer.setMode(mode.getSelectedIndex());
            changePage(currentPage);
            if(Print.modes[mode.getSelectedIndex()].equals("Specific Student Report")&&
                    nameField==null){
                nameField=new JTextField(10);
                nameField.setText(initialText);
                nameField.setHorizontalAlignment(JTextField.CENTER);
                nameField.getDocument().addDocumentListener(this);
                nameField.addFocusListener(this);
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
            else if(!Print.modes[mode.getSelectedIndex()].equals("Specific Student Report")&&
                    nameField!=null){
                remove(nameField);
                nameField=null;
                revalidate();
            }
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
        if(Print.modes[mode.getSelectedIndex()].equals("Specific Student Report")){
            printer.setStudent(nameField.getText());
            changePage(currentPage);                
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if(e.getSource().equals(nameField)){
            if(nameField.getText().equals(initialText)){
                nameField.setSelectionStart(0);
                nameField.setSelectionEnd(initialText.length());
            }
        }
    }

    @Override
    public void focusLost(FocusEvent e) {}
    
}
