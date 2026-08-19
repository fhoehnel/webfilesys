package de.webfilesys;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;

/**
 * Updates the links to all files in a renamed folder tree.
 * Makes only sense with reverse file linking enabled.
 * 
 * @author Frank Hoehnel
 */
public class UpdateLinksAfterDirRenameThread extends Thread {
    private final String newDirPath;
    private final String uid;
    
    private MetaInfManager metaInfMgr = null;
    
    public UpdateLinksAfterDirRenameThread(String newDirPath, String uid) {
        this.newDirPath = newDirPath;   
        this.uid = uid;
        metaInfMgr = MetaInfManager.getInstance();
    }
    
    public void run() {
         updateLinks(newDirPath);
    }

    private void updateLinks(String newDirPath) {
        try {
            Files.walkFileTree(Paths.get(newDirPath), new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                    File file = path.toFile();

                    if (file.isFile() && file.canRead()) {
                        String newFilePath = file.getAbsolutePath();
                        ArrayList<String> linkingFiles = metaInfMgr.getLinkingFiles(newFilePath);
                        if (linkingFiles != null) {
                            for (String linkingFilePath : linkingFiles) {
                                metaInfMgr.updateLinkTarget(linkingFilePath, newFilePath, uid);
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path path, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            LogManager.getLogger(getClass()).warn("failed to traverse folder tree " + newDirPath, ex);
        }
    }
}
