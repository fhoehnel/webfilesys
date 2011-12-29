package de.webfilesys.user;

import java.io.File;

/**
 * Extend this class to implement your own user manager 
 * (for example to access an existing user database).
 *
 * For a description of the methods that must be implemented
 * see the UserManager interface.
 *
 * The implementing class derived from this base class must call
 * this.start() in the constructor !!!
 */
public abstract class UserManagerBase extends Thread implements UserManager
{
    public boolean readyForShutdown=false;

    public String getLowerCaseDocRoot(String userId)
    {
        return(getDocumentRoot(userId).toLowerCase());
    }

    /**
     * Do not overwrite this method !!
     */
    public String normalizeDocRoot(String documentRoot)
    {
        documentRoot=documentRoot.replace('\\','/');

        if (documentRoot.endsWith("/"))
        {
            if (documentRoot.length()>1)
            {
                documentRoot=documentRoot.substring(0,documentRoot.length()-1);
            }
        }

        if ((documentRoot.charAt(0)!='*') || (File.separatorChar=='/'))
        {
            File docRootFile=new File(documentRoot);
            if ((!docRootFile.exists()) || (!docRootFile.isDirectory()))
            {
                System.out.println("*** the document root directory " + documentRoot + " does not exist!");
            }
        }

        if (File.separatorChar=='\\')
        {
            if (documentRoot.charAt(0)=='*')             
            {
                documentRoot="*:";
            }
            else
            {
                if ((documentRoot.charAt(0)<'A') || (documentRoot.charAt(0)>'Z'))
                {
                    String upperCaseDriveLetter=documentRoot.substring(0,1).toUpperCase();
                    documentRoot=upperCaseDriveLetter + documentRoot.substring(1);
                }
            }
        }

        return(documentRoot);
    }

    /**
     * Overwrite this method if your UserManager has to do some
     * actions periodically and/or before server shutdown.
     * Otherwise leave the default implementation of this method untouched.
     */
    public synchronized void run()
    {
        boolean exitFlag=false;

        while (!exitFlag)
        {
            try
            {
                this.wait(60000);

            }
            catch (InterruptedException e)
            {
                readyForShutdown=true;
                
                exitFlag=true;
            }
        }
    }

    public boolean isReadyForShutdown()
    {
        return(readyForShutdown);
    }
}
