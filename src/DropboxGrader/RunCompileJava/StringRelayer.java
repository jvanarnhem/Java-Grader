/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.RunCompileJava;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author matt
 */
public class StringRelayer implements Runnable{
    private String output;
    private String error;
    private BufferedReader outputStream;
    private BufferedReader inputStream;
    private boolean keepRunning;
    private Thread thread;
    
    public StringRelayer(InputStream out,InputStream err){
        keepRunning=true;
        output="";
        error="";
        outputStream=new BufferedReader(new InputStreamReader(out));
        inputStream=new BufferedReader(new InputStreamReader(err));
        
        thread=new Thread(this);
        thread.setName("StringRelayer");
        thread.start();
    }
    public void stop(){
        keepRunning=false;
    }       
    public String getOutput(){
        return output;
    }
    public String getError(){
        return error;
    }
    public Thread getProc(){
        return thread;
    }
    @Override
    public void run() {
        while(outputStream!=null&&keepRunning){
            try{
                String line=null;
                int out=-2;
                while(outputStream.ready()&&out!=-1){
                    out=outputStream.read();
                    if(line==null)
                        line="";
                    if(out!=-1)
                        line+=(char)out;
                    if(out==-2)
                        out=-1;
                    if(line!=null)
                        output+=line;
                }
                out=-2;
                line=null;
                while(inputStream.ready()&&out!=-1){
                    out=inputStream.read();
                    if(line==null)
                        line="";
                    if(out!=-1)
                        line+=(char)out;
                    if(out==-2)
                        out=-1;
                    if(line!=null)
                        error+=line;
                }
            } catch(Exception e){
                Logger.getLogger(InputRelayer.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        keepRunning=false;
        outputStream=null;
    }
    
}
