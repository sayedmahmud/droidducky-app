package com.draguve.droidducky;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;


/**
 * Created by Draguve on 9/23/2017.
 */

public class TheExecuter {

    private final static String TAG = "TheExecutor";
    static volatile boolean sendKeystokes = false;

    //Sends keystokes to the hostdevice with hid-gadget async
    public static void sendKeyStrokes(final ArrayList<String> keys) {
        new Thread(new Runnable() {
            public void run() {
                sendKeystokes = true;
                try {
                    Process process = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes("cd " + DUtils.binHome + '\n');
                    for(String key : keys){
                        if(!sendKeystokes){
                            return;
                        }
                        String command = "echo " + key +" | ./hid-gadget-test /dev/hidg0 keyboard" + '\n';
                        os.writeBytes(command);
                    }
                    os.writeBytes("exit\n");
                    os.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stopSendingKeystrokes(){
        sendKeystokes = false;
    }

    public static void runAsRoot(String[] command) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            for (String tmpmd : command) {
                os.writeBytes(tmpmd + '\n');
            }
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void runAsRoot(String command){
        try{
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + '\n');
            os.writeBytes("exit\n");
            os.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static String runAsRootOutput(String command) {
        String output = "";
        String line;
        try {
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            stdin.write((command + '\n').getBytes());
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            while ((line = br.readLine()) != null) {
                output = output + line;
            }
            br.close();
            br = new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("Shell Error:", line);
            }
            br.close();
            process.waitFor();
            process.destroy();
        } catch (IOException e) {
            Log.d(TAG, "An IOException was caught: " + e.getMessage());
        } catch (InterruptedException ex) {
            Log.d(TAG, "An InterruptedException was caught: " + ex.getMessage());
        }
        return output;
    }
}
