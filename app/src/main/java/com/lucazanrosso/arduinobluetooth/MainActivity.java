package com.lucazanrosso.arduinobluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "Error";
    public final int MESSAGE_READ = 1;
    public final int REQUEST_ENABLE_BT = 2;

    static Handler mHandler;

    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;

    private ConnectedThread mConnectedThread;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not found in this device", Toast.LENGTH_SHORT).show();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                connect();
            }
        }

//        mHandler = new Handler() {
//            public void handleMessage(android.os.Message msg) {
//                switch (msg.what) {
//                    case MESSAGE_READ:
//                        byte[] readBuf = (byte[]) msg.obj;
//                        String readString = new String(readBuf, 0, msg.arg1);
//                        View inflatedLayout = getLayoutInflater().inflate(R.layout.text_view_received,conversationLayout, false);
//                        TextView textView = (TextView) inflatedLayout.findViewById(R.id.textview_receive);
//                        textView.setText(readString);
//                        conversationLayout.addView(inflatedLayout);
//                        break;
//                }
//            }
//        };
    }

    public void sendMessage(View view) {
        Button button = (Button) view;
        String message = button.getText().toString();
        mConnectedThread.write(message);
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        private ConnectedThread(BluetoothSocket socket) {
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

//        public void run() {
//            mmBuffer = new byte[1024];
//            int numBytes; // bytes returned from read()
//
//            // Keep listening to the InputStream until an exception occurs.
//            while (true) {
//                try {
//                    if (mmInStream.available() > 0) {
//                        // Read from the InputStream.
//                        numBytes = mmInStream.read(mmBuffer);
//                        // Send the obtained bytes to the UI activity.
//                        mHandler.obtainMessage(MESSAGE_READ, numBytes, -1, mmBuffer)
//                                .sendToTarget();
//                    } else SystemClock.sleep(100);
//                } catch (IOException e) {
//                    Log.d(TAG, "Input stream was disconnected", e);
//                    break;
//                }
//            }
//        }

        // Call this from the main activity to send data to the remote device.
        private void write(String message) {
            try {
                byte[] bytes = message.getBytes();
                mmOutStream.write(bytes);
                // Share the sent message with the UI activity.
//                    mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, mmBuffer)
//                            .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }

        // Call this method from the main activity to shut down the connection.
        private void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            connect();
        }
        if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    public void connect() {
        final String ADDRESS = "98:D3:32:20:68:87";
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(ADDRESS);
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            finish();
        }
        mBluetoothAdapter.cancelDiscovery();
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                finish();
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            try {
                btSocket.close();
            } catch (IOException e2) {
                finish();
            }
        }
    }
}