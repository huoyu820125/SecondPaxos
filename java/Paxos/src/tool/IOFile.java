/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Administrator
 */
public class IOFile {

    public IOFile(String name)
    {
        mFile = new File(name);
        try {
            mFW = new FileWriter(mFile);
        } catch (IOException ex) {
            Logger.getLogger(IOFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        mWriter = new BufferedWriter(mFW);
    }
    
    public void write(String text)
    {
        try {
            mWriter.write(text);
            mWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(IOFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close()
    {
        try {
            mWriter.close();
            mFW.close();
        } catch (IOException ex) {
            Logger.getLogger(IOFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private File mFile = null;
    private FileWriter mFW = null;
    private BufferedWriter mWriter = null;
}
