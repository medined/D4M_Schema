package com.codebits.hadoop.util;

import java.io.IOException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CreateOrReplaceHadoopDirectoryTest {

    private CreateOrReplaceHadoopDirectory instance = null;
    
    @Before
    public void setUp() {
        instance = new CreateOrReplaceHadoopDirectory();
    }

    @Test
    public void testMkdirs() throws IOException {
        FileSystem mockFileSystem = mock(FileSystem.class);
        String directory = "a";
        instance.mkdirs(mockFileSystem, directory);
        verify(mockFileSystem).delete(any(Path.class), Matchers.anyBoolean());
        verify(mockFileSystem).mkdirs(any(Path.class));
        verifyNoMoreInteractions(mockFileSystem);
    }
    
}
