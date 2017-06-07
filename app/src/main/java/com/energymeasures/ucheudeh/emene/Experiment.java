package com.energymeasures.ucheudeh.emene;

import android.provider.Settings;
import android.util.Log;

import java.io.IOException;

/**
 * Created by ucheudeh on 6/5/17. This is a class to conduct the experiment
 */

 abstract class Experiment {
    String seriType, ioMode;
    final int BUFFERSIZE = 1024;


    public Experiment(String jSeri, String individual_files) {
        this.seriType = jSeri;
        this.ioMode = individual_files;
    }

    public void commence() {
        Log.i(seriType+ioMode, "Starting");
        try{
            long startTime = System.nanoTime();
            Log.i(seriType+ioMode + "Start Time: ", Long.toString(startTime));
            try {
                readTest();
            } catch (ClassNotFoundException e) {
                Log.i("Read file", "class not found");
            }
            long endTime = System.nanoTime();
            Log.i(seriType+ioMode + "End Time: ", Long.toString(endTime));
            Log.i(seriType+ioMode + "Total_Duration: ", Double.toString(endTime-startTime));
        }catch(IOException e){
            Log.e(seriType+ioMode + "Error: ", e.toString());
        }


    }

    public abstract void readTest() throws IOException, ClassNotFoundException;



    public abstract void writeDataSingle(SnapshotsBasket numData) throws IOException;

    public abstract void writeDataOneFile(SnapshotsBasket numData) throws IOException;
}
