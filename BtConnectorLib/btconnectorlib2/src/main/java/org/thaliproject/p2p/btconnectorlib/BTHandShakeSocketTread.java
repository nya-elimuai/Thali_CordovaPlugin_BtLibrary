// Copyright (c) Microsoft. All Rights Reserved. Licensed under the MIT License. See license.txt in the project root for further information.
package org.thaliproject.p2p.btconnectorlib;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by juksilve on 11.3.2015.
 */

class BTHandShakeSocketTread extends Thread {

    public static final int MESSAGE_READ         = 0x11;
    public static final int MESSAGE_WRITE        = 0x22;
    public static final int SOCKET_DISCONNEDTED  = 0x33;

    private BluetoothSocket mmSocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private final Handler mHandler;

    private final String TAG  = "BTHandShakeSocketTread";

    public BTHandShakeSocketTread(BluetoothSocket socket, Handler handler) {
        Log.d(TAG, "Creating BTHandShakeSocketTread");
        mHandler = handler;
        mmSocket = socket;

        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        // Get the BluetoothSocket input and output streams
        try {
            if(mmSocket != null) {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            }
        } catch (IOException e) {
            Log.e(TAG, "Creating temp sockets failed: ", e);
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
    public void run() {
        Log.i(TAG, "BTHandShakeSocketTread started");
        byte[] buffer = new byte[255];
        int bytes;

        try {
            bytes = mmInStream.read(buffer);
            //Log.d(TAG, "ConnectedThread read data: " + bytes + " bytes");
            mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "BTHandShakeSocketTread disconnected: ", e);
            mHandler.obtainMessage(SOCKET_DISCONNEDTED, -1, -1, e).sendToTarget();
        }
        Log.i(TAG, "BTHandShakeSocketTread fully stopped");
    }
    /**
     * Write to the connected OutStream.
     * @param buffer The bytes to write
     */
    public void write(byte[] buffer) {

        if (mmOutStream == null) {
            return;
        }

        try {
            mmOutStream.write(buffer);
            mHandler.obtainMessage(MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
        } catch (IOException e) {
            Log.e(TAG, "BTHandShakeSocketTread  write failed: ", e);
        }
    }

    public void CloseSocket() {

        InputStream tmpIn = mmInStream;
        mmInStream = null;
        if (tmpIn != null) {
            try {tmpIn.close();} catch (Exception e) {e.printStackTrace();}
        }

        OutputStream tmpPOut = mmOutStream;
        mmOutStream = null;
        if (tmpPOut != null) {
            try {tmpPOut.close();} catch (Exception e) {e.printStackTrace();}
        }

        BluetoothSocket tmpSocket = mmSocket;
        mmSocket = null;
        if (tmpSocket != null) {
            try {tmpSocket.close();} catch (Exception e) {e.printStackTrace();}
        }
    }
}
