package com.energymeasures.ucheudeh.emene;

import android.content.Context;
import android.util.Log;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by ucheudeh on 6/7/17.
 */

public class ExperimentJavaSerialization extends Experiment {


    public ExperimentJavaSerialization() {
        this.seriType = "Java";
        this.ioMode = "test modes";

    }

    public ExperimentJavaSerialization(String seriType, String ioMode) {
        this.seriType= seriType;
        this.ioMode = ioMode;





    }


    //Random Access and MMAP granularity to vary in  experiments whole to numerical




    @Override
    public void readSingly(String filename, int numMatrix, int numVector,Context context) throws IOException, ClassNotFoundException{

    }


    @Override
    public void readDataOneFile(String filename, int numMatrix, int numVector,Context context) throws IOException, ClassNotFoundException {

                /*
                A read of the Random Access File - Java.io:
                - go to the end of the °File.
                - step back 8 bytes and cast last 8 bytes as Long i. This is the number of elements
                in the list.
                   k = seek(file.lenght()-8 ** note the number of elements was up casted to long in
                   write.
                - From the i start position, find the start of the List<Long>: List end is:
                seek(file.lenght()-4.
                List start = (seek(file.size()-8) - i*(8) // long is 64bits long. List lenght is:
                k-start.
                seek(start).
                readbyte( offset=start, lenght = k-start)
                Random reader will first read the last Integer.size() to obtain the size of the
                Array List. Then offset the file read by setting offset = fc.size()-Integer.SIZE(1 +
                i.longValue()).
                Random Access read of the Arraylist specifying the start and end of
                each entry is used to cast the objects back to Array2DMatrix or RealVector. This is
                 */

        // RANDOM ACCESS CODE BEGINS HERE

        Log.e("File Error RANDOM", "FILE DOE NOT EXIST IN THE WORKING DIRECTORY");
        //SET A DEFAULT ACTION HERE TO RECOVER GRACEFULLY




                /*

                First we retrieve the library. We get number of elements, then calculate the offset
                 */
        File fileR = new File (context.getFilesDir(),filename);
        Log.i(seriType+ioMode, " : Can read file : "+ filename+ fileR.canRead());
        RandomAccessFile multiRand = new RandomAccessFile(fileR, "r");
        long multiRandSize = multiRand.length();
        final int LENGHT = 8;
        int off1 = (int) multiRandSize - LENGHT;

        long librarySize = (long) getChunk(multiRand, getBuffer(LENGHT), off1, LENGHT);
        Log.i(seriType+ioMode, " : length of library is : "+ Long.toString(librarySize)) ;

        // get library
        int libLenght = (int) librarySize * 8;
        int libStart = (int) (multiRandSize - (libLenght + LENGHT));//locate

        long[] library = (long[]) getChunk(multiRand, getBuffer(libLenght), libStart, libLenght);

        Log.i(seriType+ioMode, " : Library retrieved second offset is "+ Long.toString(library[2]));
                /*
                Now we want a rightly size buffer to do all read transactions. It must at least hold
                the largest matrix. So we iterate on very second element of the library and keep a max


                 */
        long maxLenght  = 0L;

        for (int i = 1; i<librarySize;i+=2){
            maxLenght = Math.max(maxLenght,library[i]);
        }
        byte[] readBuff = getBuffer((int)maxLenght); // get a buffer of appropriate size

        // Read Matrix
        Array2DRowRealMatrix[] matrixArray = new Array2DRowRealMatrix[(int) librarySize / 2];
        for (int i = 0; i < librarySize / 2; i++) {// each matrix occupies exactly 2 cells in library
            int offsetIndex = (i + 1) * 2 - 2;
            int lenghtIndex = (i + 1) * 2 - 1;
            matrixArray[i] = (Array2DRowRealMatrix) getChunk(multiRand, readBuff, (int) library[offsetIndex], (int) library[lenghtIndex]);
        }


        Log.i(seriType + ioMode, "RandomAccessRead Complete: First entry os first matix: " +matrixArray[0].getEntry(1,1));


    }

    @Override
    public void writeDataSingle(SnapshotsBasket numData, Context context) throws IOException {
                /*
                Write each of the elements to an individual file after serializing: JAVA
                File name format: SinglyJSnapMatrix1.dat, SinglyJSnapVector1.dat
                 */

        String matrixFilename = "SinglyJSnapMatrix";
        //read each matrix from numData and write each to a file on the App dir.

        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        for (int i = 0; i < numData.getaMsize(); i++) {
            String filename = matrixFilename.concat(Integer.toString(i)).concat(".dat");
            try {
                File file = new File(context.getFilesDir(), filename); // new file created
                FileChannel fc = new FileOutputStream(file).getChannel();

                out.writeObject(numData.getaM(i));//writes Array2DMatrix
                out.flush();
                fc.write(ByteBuffer.wrap(byteOut.toByteArray())); // converts to Buffer
                Log.i(seriType+ioMode," : wrote to file: "+filename+" "+ fc.size());
                byteOut.reset(); // clear byteOutput stream for reuse by ObjectStreamwriter
            } catch (IOException e) {
                Log.e(seriType + ioMode, " : IO exception at creating file check permission");
            }


        }


        String vectorFilename = "SinglyJSnapVector";
        //read each snapshot vector from numData and write each to a file on the App dir.


        for (int i = 0; i < numData.getsnapshotSize(); i++) {
            String filename = vectorFilename.concat(Integer.toString(i)).concat(".dat");

            try {
                File file = new File(context.getFilesDir(), filename);
                FileChannel fc = new FileOutputStream(file).getChannel();

                out.writeObject(numData.getSnapshot(i));//writes Realvector
                out.flush();
                fc.write(ByteBuffer.wrap(byteOut.toByteArray())); // converts to Buffer
                Log.i(seriType+ioMode," : wrote to file: "+filename+" "+ fc.size());
                byteOut.reset();
                fc.close();
            } catch (IOException e) {
                Log.e(seriType + ioMode, " : IO exception at creating file check permission");
            }


        }
        out.close();// close Object stream


    }

    @Override
    public void writeDataOneFile(SnapshotsBasket numData, Context context) throws IOException {

        // RANDOM ACCESS SEEK() IMPLEMENTATION. Write one file containing all the matrices using RandomAccess

                /*
                        If read serially, all even indices on the randomInOut holds the start
                        position on file while the odd number indices hold the end positions.
                        If not read serially, then we can callup say the 3rd matrix by
                        start point = N*2-2 >, end point is N*2-1.
                        It is also possible to maintain a library of snapshots but that will require
                        some form of naming conventions from source.

                         */
                        /*
                        Future will seek to do a partial write of large vectors in fixed increments
                        and storing only the end positions of resulting sub blocks and the preceeding
                        full block counts. This is easily achieved by reading with a fixed buffer
                        size to the desired block size. Filling and flipping this buffer. Or just
                        plain incremental writing with offsets and lenth parameter passed no buffer
                        flipping

                         */

        String multiMatrix = "MultiJMatrix.dat";
        ArrayList<Long> randomInOut = new ArrayList<>();
        //attempt using Filechannel nio on Keep

        try {

            File fileMulti = new File(context.getFilesDir(), multiMatrix);
            //RandomAccessFile fcMulti = new RandomAccessFile(fileMulti, "rw")//java.io;
            FileChannel fcMulti = new RandomAccessFile(fileMulti, "rw").getChannel();//java.nio
            ByteArrayOutputStream byteMult = new ByteArrayOutputStream();
            ObjectOutputStream outMult = new ObjectOutputStream(byteMult);
            for (int i = 0; i < numData.getaMsize(); i++) { // write can go to method
                // add an object to the byteArrayOutputstream and then note the end point
                outMult.writeObject(numData.getaM(i));// °Serialization here
                outMult.flush();
                //the first write starts at the 0th position
                long k = fcMulti.position();// the desire read offset- java.nio
                //long k = fcMulti.getFilePointer();// the desire read offset java.io
                randomInOut.add(k);// offset fir this write
                fcMulti.write(ByteBuffer.wrap(byteMult.toByteArray()));// write java.nio
                //fcMulti.write(byteMult.toByteArray());// java.io write
                //fcMulti.position(fcMulti.size()) ; //define the new position as the end of write
                //randomInOut.add(fcMulti.getFilePointer() - k);//put the length - java.oi
                randomInOut.add(fcMulti.position() - k);//put the length - java.nio
                byteMult.reset();//clears the bytearrayoutputstream.

            }
            // Serialize  arrayList and its size to ByteArrayOutputStream
            long librarySize = randomInOut.size();// write can go to method
            outMult.writeObject(randomInOut.toArray());//now and array of long
            outMult.flush();
            fcMulti.write(ByteBuffer.wrap(byteMult.toByteArray()));// write java.nio
            byteMult.reset();
            outMult.writeLong(librarySize);// assumes the write sequence is respected
            outMult.flush();
            fcMulti.write(ByteBuffer.wrap(byteMult.toByteArray()));// write java.nio
            // write ByteArrayOutputStream to file
            //fcMulti.write(byteMult.toByteArray()); java.io

            outMult.close();
            Log.i(seriType+ioMode," : wrote to file: "+multiMatrix+" "+ fcMulti.size());


            fcMulti.close();


        } catch (IOException e) {
            Log.e(seriType + ioMode, " : Multi File IO error");
        }



    }

    private Object getChunk(RandomAccessFile multiRand, byte[] readBuff, int start, int lenght) throws IOException, ClassNotFoundException {
        //ByteBuffer[] buff = getBuffer(lenght);
        multiRand.read(readBuff,start,lenght);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(readBuff);
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        Object obj=  objIn.readObject();
        objIn.close();// can be GC'd
        return obj;

    }


/* possible to make the library and arraylist and obtain the largest size matrix and
make a buffer for this size this buffer is then flipped to read  several time until all is read less
GC for discarding buffers for every read.
 */

    private byte[] getBuffer(int i) {


        return new byte[i];
    }

}
