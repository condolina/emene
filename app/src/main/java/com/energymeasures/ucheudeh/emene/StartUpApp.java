package com.energymeasures.ucheudeh.emene;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class StartUpApp extends AppCompatActivity {


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = this.getApplicationContext();
        setContentView(R.layout.activity_start_up_app);
        String message = "Application Stable ";

        TextView textView = new TextView(this);
        textView.setText(message);
        setContentView(textView);
        Log.i("Emene", "Commencing Data creation");
        SnapshotsBasket numData = new SnapshotsBasket();// create the data

        Experiment[] allRuns = {new Experiment("jSeriWriter", "Normal, RandomAccess, MMAP"){
            //Random Access and MMAP granularity to vary in  experiments whole to numerical


            @Override
            public void readTest() throws IOException, ClassNotFoundException {

                /*
                A read of the Random Access File:
                - go to the end of the °File.
                - step back 8 bytes and cast last 8 bytes as Long i. This is the number of elements
                in the list.
                   k = seek(fileChannel.size()-8 ** note the number of elements was up casted to long in
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

                String multiMatrix = "MultiJMatrix.dat";

                if (!(new File(context.getFilesDir(), multiMatrix).exists())) {
                    Log.e("File Error RANDOM", "FILE DOE NOT EXIST IN THE WORKING DIRECTORY");
                    //SET A DEFAULT ACTION HERE TO RECOVER GRACEFULLY


                }

                /*
                First we retrieve the library. We get number of elements, then calculate the offset
                 */
                FileChannel multiRand = new RandomAccessFile(multiMatrix, "r").getChannel();
                long multiRandSize = multiRand.size();
                final int LENGHT = 8;
                int off1= (int)multiRandSize- LENGHT;

                long librarySize =(long)getChunk(multiRand,off1, LENGHT);

                // get library
                int libLenght = (int)librarySize*8;
                int libStart = (int)(multiRandSize-(libLenght+LENGHT));//locate
                long [] library = (long[])getChunk(multiRand, libStart, libLenght);

                // Read Matrix
                Array2DRowRealMatrix[] matrixArray = new Array2DRowRealMatrix[(int)librarySize/2];
                for (int i =0; i<librarySize/2;i++){// each matrix occupies exactly 2 cells in library
                    int offsetIndex = (i+1)*2-2;
                    int lenghtIndex = (i+1)*2-1;
                    matrixArray[i]= (Array2DRowRealMatrix)getChunk(multiRand,(int)library[offsetIndex],(int)library[lenghtIndex]);
                }

                Log.i(seriType+ioMode, "RandomAccessRead Complete");







            }
            @Override
            public void writeData(SnapshotsBasket numData) throws IOException {
                /*
                Write each of the elements to an individual file after serializing: JAVA
                File name format: SinglyJSnapMatrix1.dat, SinglyJSnapVector1.dat
                 */

                String matrixFilename = "SinglyJSnapMatrix";
                //read each matrix from numData and write each to a file on the App dir.
                for (int i=0; i<numData.getaMsize();i++){
                    String filename = matrixFilename.concat(Integer.toString(i)).concat(".dat");
                    try{
                        File file = new File(context.getFilesDir(),filename);
                        FileChannel fc = new FileOutputStream(file).getChannel();
                        ByteArrayOutputStream byteOut=new ByteArrayOutputStream();
                        ObjectOutputStream out = new ObjectOutputStream(byteOut);
                        out.writeObject(numData.getaM(i));//writes Array2DMatrix
                        fc.write(ByteBuffer.wrap(byteOut.toByteArray())); // converts to Buffer
                        out.close();
                    }catch (IOException e){
                        Log.e(seriType+ioMode, " : IO exception at creating file check permission");
                    }


                }
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
                ArrayList<Long> randomInOut = new ArrayList<Long>();
                //attempt using Filechannel nio on Keep

                try {

                    File fileMulti = new File(context.getFilesDir(), multiMatrix);
                    RandomAccessFile fcMulti = new RandomAccessFile(fileMulti,"rw");//randomaccess change to
                    ByteArrayOutputStream byteMult = new ByteArrayOutputStream();
                    ObjectOutputStream outMult = new ObjectOutputStream(byteMult);
                    for (int i = 0; i < 4; i++ ){
                        // add an object to the byteArrayOutputstream and then note the end point
                        outMult.writeObject(numData.getaM(i));// °Serialization here
                        //the first write starts at the 0th position
                        long k = fcMulti.getFilePointer();// the desire read offset for this matrix/vector
                        randomInOut.add(k);//every write has a start position ;-)
                        fcMulti.write(byteMult.toByteArray());
                        //fcMulti.position(fcMulti.size()) ; //define the new position as the end of write
                        randomInOut.add(fcMulti.getFilePointer()-k);//put the length of this matrix/vector
                        byteMult.reset();//clears the bytearrayoutputstream.

                    }
                    // Serialize  arrayList and its size to ByteArrayOutputStream
                    long librarySize= randomInOut.size();
                    outMult.writeObject(randomInOut.toArray());//now and array of long
                    outMult.writeLong(librarySize);// assumes the write sequence is respected
                    // write ByteArrayOutputStream to file
                    fcMulti.write(byteMult.toByteArray());

                    outMult.close();



                    fcMulti.close();


                }catch(IOException e){
                    Log.e(seriType+ioMode, " : Multi File IO error");
                }


                String vectorFilename = "SinglyJSnapVector";
                //read each snapshot vector from numData and write each to a file on the App dir.
                for (int i=0; i<4;i++){
                    String filename = matrixFilename.concat(Integer.toString(i)).concat(".dat");
                    try{
                        File file = new File(context.getFilesDir(),filename);
                        FileChannel fc = new FileOutputStream(file).getChannel();
                        ByteArrayOutputStream byteOut=new ByteArrayOutputStream();
                        ObjectOutputStream out = new ObjectOutputStream(byteOut);
                        out.writeObject(numData.getSnapshot(i));//writes Realvector
                        fc.write(ByteBuffer.wrap(byteOut.toByteArray())); // converts to Buffer
                        byteOut.close();
                    }catch (IOException e){
                        Log.e(seriType+ioMode, " : IO exception at creating file check permission");
                    }


                }
            }
        }};




    }

    private Object getChunk(FileChannel multiRand, int start, int lenght) throws IOException, ClassNotFoundException {
        ByteBuffer[] buff = getBuffer(lenght);
        multiRand.read(buff,start,lenght);
        buff[0].flip();
        ByteArrayInputStream byteIn = new ByteArrayInputStream(buff[0].array());
        ObjectInputStream objIn = new ObjectInputStream(byteIn);
        return objIn.readObject();

    }




    private ByteBuffer[] getBuffer(int i) {
        ByteBuffer[] buffArray = new ByteBuffer[1];
        buffArray[0]=ByteBuffer.allocate(i);
        return buffArray;
    }
    }