package com.energymeasures.ucheudeh.emene;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by ucheudeh on 6/5/17.
 */

abstract class FileOutReader {
    /*
    This class is the write class for the storage method. Implementing classes will do
    one of three options: Normal file input stream read of an entire snapshot file, or
    individual read of multiple snapshot files, Random Read: a Snapshot is further
    clunked into predetermined sizes, but all snapshots are aggregated into one single
    file, MMAP: the Randomized single file or multiple files are mapped to memory
    It really doesn't matter how the file is written.
     */


    public abstract void readOut(String path) throws IOException;
}
