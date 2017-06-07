package com.energymeasures.ucheudeh.emene;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by ucheudeh on 6/5/17.
 */

abstract class FileInWriter {/*
        The reader for various formats: Normal stream reads, Randomized reads, MMAP reads.
        Implementing classes will fleshout the various read modes
         */

    public abstract void writeIn(String path) throws IOException, FileNotFoundException;
}
