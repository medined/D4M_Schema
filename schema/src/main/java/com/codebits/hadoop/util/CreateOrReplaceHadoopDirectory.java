package com.codebits.hadoop.util;

import com.codebits.d4m.D4MException;
import java.io.IOException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.AccessControlException;

/* This class exists to isolate that pesky empty catch block. There is no 
 * reason to copy 'bad' practice throughout the code base.
*/
@SuppressWarnings("PMD.EmptyCatchBlock")
public class CreateOrReplaceHadoopDirectory {

    public void mkdirs(final FileSystem fileSystem, final String directory) {
        try {
            fileSystem.delete(new Path(directory), true);
        } catch (AccessControlException e) {
            // We don't care of the file is not found.
        } catch (IOException e) {
            throw new D4MException("Unable to delete "  + directory, e);
        }
        try {
            fileSystem.mkdirs(new Path(directory));
        } catch (AccessControlException e) {
            throw new D4MException("Unable to create "  + directory + ". Please fix the permissions. Perhaps create parent directories?", e);
        } catch (IOException e) {
            throw new D4MException("Unable to create "  + directory, e);
        }
    }
    
}
