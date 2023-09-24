package com.example.android.gymlogmulti.bluetooth;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.android.gymlogmulti.MainActivity;
import com.example.android.gymlogmulti.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class DoorSingleton {
        //TODO make singleton

        public static final int REQUEST_ENABLE_BT = 63;
        BluetoothAdapter bluetoothAdapter;
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        //ConnectThread connectThread;
        ConnectedThread mgmtThread;
        public volatile boolean isConnected = false;

        public interface BlueToothConnInterface {
            void postMsg(String msg);
            void setConnected(Boolean conn);
        }

        BlueToothConnInterface blueToothConnInterface;

        public void  setBtInterface(BlueToothConnInterface blueToothConnInterface){
            this.blueToothConnInterface = blueToothConnInterface;
        }

        private DoorSingleton() {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }

        private static DoorSingleton doorSingleton;

        public static DoorSingleton getInstance(){
            if (doorSingleton==null){
                doorSingleton=new DoorSingleton();
            }
            if (!doorSingleton.isConnected){
                doorSingleton.initializeBluetooth();
            }
            return doorSingleton;
        }




        public void initializeBluetooth() {
            isConnected=false;
            if (blueToothConnInterface!=null){
                blueToothConnInterface.setConnected(isConnected);
            }
            if (bluetoothAdapter == null && blueToothConnInterface != null) {
                blueToothConnInterface.postMsg("este dispositivo no tiene bluetooth");
                return;
            }
            if (getDoorFromPaired() == null && blueToothConnInterface != null) {
                blueToothConnInterface.postMsg("necesitás emparejar 'PuertaGym' en tus dispositivos de bluetooth");
                return;
            }
            ConnectThread connection = new ConnectThread(getDoorFromPaired());
            connection.start();
        }


        @SuppressLint("MissingPermission")
        public BluetoothDevice getDoorFromPaired() {
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().contentEquals("PuertaGym")) {
                        return device;
                    }
                }

            }
            return null;
        }

        public void sendOpenDoorMsg(String msg) {
            if (isConnected)
                mgmtThread.openDoor(msg);
        }

        private class ConnectThread extends Thread {
            private final BluetoothSocket mmSocket;

            @SuppressLint("MissingPermission")
            public ConnectThread(BluetoothDevice device) {
                // Use a temporary object that is later assigned to mmSocket
                // because mmSocket is final.
                BluetoothSocket tmp = null;

                try {
                    tmp = device.createRfcommSocketToServiceRecord(uuid);
                } catch (Exception e) {
                    Log.e("kherson", "Socket's create() method failed", e);
                }
                mmSocket = tmp;
            }

            @SuppressLint("MissingPermission")
            public void run() {
                // Cancel discovery because it otherwise slows down the connection.

                bluetoothAdapter.cancelDiscovery();

                try {
                    // Connect to the remote device through the socket. This call blocks
                    // until it succeeds or throws an exception.
                    mmSocket.connect();
                } catch (Exception connectException) {
                    // Unable to connect; close the socket and return.
                    try {
                        mmSocket.close();
                    } catch (Exception closeException) {
                        Log.d("kherson", "Could not close the client socket2");
                    }
                    if(blueToothConnInterface != null) {
                        if (getDoorFromPaired() == null ) {
                            blueToothConnInterface.postMsg("necesitás emparejar 'PuertaGym' en tus dispositivos de bluetooth");
                        }else{
                            blueToothConnInterface.postMsg("no se encontró puerta");
                        }
                    }
                    return;
                }

                // The connection attempt succeeded. Perform work associated with
                // the connection in a separate thread.
                isConnected=true;
                manageMyConnectedSocket(mmSocket);
                blueToothConnInterface.postMsg("conexión exitosa a la puerta");
                blueToothConnInterface.setConnected(true);

            }

            // Closes the client socket and causes the thread to finish.
            public void cancel() {
                try {
                    mmSocket.close();
                    isConnected=false;
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the client socket", e);
                }
            }
        }

        public void manageMyConnectedSocket(BluetoothSocket socket){
            mgmtThread=new ConnectedThread(socket);

        }

        private class ConnectedThread extends Thread {
            private final BluetoothSocket mmSocket;
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;


            public ConnectedThread(BluetoothSocket socket) {
                mmSocket = socket;
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                // Get the input and output streams; using temp objects because
                // member streams are final.
                try {
                    tmpIn = socket.getInputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating input stream", e);
                }
                try {
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                    Log.e(TAG, "Error occurred when creating output stream", e);
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run() {
                // mmBuffer store for the stream
                byte[] mmBuffer = new byte[1024];
                int numBytes; // bytes returned from read()

                // Keep listening to the InputStream until an exception occurs.
                while (true) {
                    try {
                        // Read from the InputStream.
                        numBytes = mmInStream.read(mmBuffer);
                        // do something later here
                    } catch (IOException e) {
                        isConnected=false;
                        Log.d(TAG, "Input stream was disconnected", e);
                        break;
                    }
                }
            }

            // Call this from the main activity to send data to the remote device.
            public void openDoor(String msg) {
                String open=msg;
                byte[] bytes=open.getBytes();
                if (mmSocket.isConnected()) {
                    try {
                        mmOutStream.write(bytes);
                    } catch (IOException e) {
                        Log.e(TAG, "Error occurred when sending data", e);
                        blueToothConnInterface.postMsg("conexión interrumpida");
                        blueToothConnInterface.setConnected(true);
                        initializeBluetooth();
                        }
                }else{
                    isConnected=false;
                    blueToothConnInterface.postMsg("conexión interrumpida");
                    blueToothConnInterface.setConnected(isConnected);
                }
            }
            // Call this method from the main activity to shut down the connection.
            public void cancel() {
                try {
                    mmSocket.close();
                    isConnected=false;
                    blueToothConnInterface.setConnected(false);
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the connect socket", e);
                }
            }
        }




}
