/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import DropboxGrader.FileManagement.FileManager;
import DropboxGrader.FileManagement.DbxFile;
import DropboxGrader.GuiElements.MiscOverlays.ClosingOverlay;
import DropboxGrader.Util.NamedRunnable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Matt
 */
public class WorkerThread implements Runnable{
    private Gui gui;
    private FileManager manager;
    private ArrayList<Runnable> queue;
    private boolean closeAfterDone;
    private ClosingOverlay closingOverlay;
    private Thread workerThread;
    
    public static final String threadName="BackgroundTasksThread";
    
    public WorkerThread(Gui gui){
        this.gui=gui;
        queue=new ArrayList();
        closeAfterDone=false;
        createThread();
    }
    private void createThread(){
        workerThread=new Thread(this);
        workerThread.setName(threadName);
    }
    @Override
    public void run() {
        try{
            while(!queue.isEmpty()){                
                Runnable r=queue.remove(0);
                try{
                    r.run();
                    if(closingOverlay!=null)
                        closingOverlay.setTasksLeft(queue.size());
                } catch(Exception e){
                    System.err.println("Exception was logged running queued code on the backgroundThread.");
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                Logger.getLogger(WorkerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(closeAfterDone){
                gui.getListener().windowClosing(null);
            }
        } catch(Exception e){
            System.err.println("An exception occured on the background thread and it was caught from crashing.");
            e.printStackTrace();
        }
        if(!queue.isEmpty())
            run();
    }
    public void download(ArrayList<Integer> fileIndexes,final boolean gradeAfter){
        final ArrayList<DbxFile> files=new ArrayList();
        for(int i=0;i<fileIndexes.size();i++){
            files.add(manager.getFile(fileIndexes.get(i)));
        }
        invokeLater(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<files.size();i++){
                    DbxFile f=files.get(i);
                    if(f!=null){
                        gui.setStatus("Downloading "+f.getFileName());
                        int progress=(int)((double)(i+1)/files.size()*100);
                        gui.updateProgress(progress);
                        if(files.size()==1){
                            gui.updateProgress(50);
                        }
                        f.download();
                        gui.repaintTable();
                        gui.setStatus("");
                    }
                }
                if(gradeAfter){
                    gui.setupGraderGui();
                }
            }
        });
    }
    public void runFile(int file,final int times){
        final DbxFile f=manager.getFile(file);
        invokeLater(new Runnable() {
            @Override
            public void run() {
                boolean success=f.run(times,gui.getCodeBrowser());
                if(!success){
                    gui.proccessEnded();
                }
            }
        });
    }
    public void delete(DbxFile file,final int progress){
        if(file==null){
            return;
        }
        final DbxFile f=file;
        invokeLater(new Runnable() {
            @Override
            public void run() {
                if(f!=null){
                    gui.updateProgress(progress);
                    f.delete();
                    try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(WorkerThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
            }
        });
    }
    public void invokeLater(Runnable run){
        if(run!=null)
            queue.add(run);
        if(!workerThread.isAlive()){
            createThread();
            workerThread.start();
        }
    }
    public void refreshData(){
        invokeLater(new Runnable() {
            @Override
            public void run() {
                if(manager!=null){
                    manager.refresh();
                    if(gui.getGrader()!=null){
                        gui.getGrader().refresh();
                    }
                    if(gui.getGradebook()!=null)
                        gui.getGradebook().dataChanged();
                    gui.refreshFinished();
                }
            }
        });
    }
    public void setFileManager(FileManager man){
        manager=man;
    }
    public boolean hasWork(){
        invokeLater(null);
        return !queue.isEmpty();
    }
    public void setCloseAfterDone(boolean close,ClosingOverlay o){
        closeAfterDone=close;
        closingOverlay=o;
    }
    public void removeQueued(String name){
        for(int i=0;i<queue.size();i++){
            if(queue.get(i) instanceof NamedRunnable){
                if(((NamedRunnable)queue.get(i)).name().equals(name)){
                    queue.remove(i);
                    i--;
                }
            }
        }
    }
}
