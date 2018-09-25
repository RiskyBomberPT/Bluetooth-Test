package app.android.tiago.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    // Amanhã continuar em http://android-er.blogspot.com/2015/10/android-communicate-with-arduino-hc-06.html para implementar a comunicação

    //Variaveis
    private final int REQUEST_ENABLE_BT = 0;
    public BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private TextView txtDispositivo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Definir Butoes
        btnEscolher = findViewById(R.id.btnEscolher);
        btnEscolher.setOnClickListener(btnEscolher_click);
        btnLigar = findViewById(R.id.btnLigar);
        btnLigar.setOnClickListener(btnLigar_click);
        btnDesligar = findViewById(R.id.btnDesligar);
        btnDesligar.setOnClickListener(btnDesligar_click);

        txtDispositivo = findViewById(R.id.txtDispositivo);

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }


    //Funções
    public void escolherDispositivo(){

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            List<String> lista = new ArrayList<String>();
            for (BluetoothDevice device : pairedDevices) {
                lista.add(device.getName() + "\n" + device.getAddress());
            }

            final String[] arLista = lista.toArray(new String[lista.size()]);

            AlertDialog.Builder dialogoA = new AlertDialog.Builder(MainActivity.this);
            dialogoA.setTitle("Escolher")
                    //.setMessage("Escolha o dispositivo")
                    .setItems(arLista, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            txtDispositivo.setText(arLista[which]);
                        }
                    })
                    .show();
        }
        else{
            AlertDialog.Builder dialogoA = new AlertDialog.Builder(MainActivity.this);
            dialogoA.setTitle("Erro")
                    .setMessage("Não há dispositivos emparelhados. Por favor emparelhe um dispositivo primeiro.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();

        }

    }


    //Click listeners

    private Button btnEscolher;

    private View.OnClickListener btnEscolher_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            escolherDispositivo();
        }
    };

    private Button btnLigar;

    private View.OnClickListener btnLigar_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //codigo pendente
        }
    };

    private Button btnDesligar;

    private View.OnClickListener btnDesligar_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //condigo pendente
        }
    };




    // Implementação da comunicação bluetooth

    ThreadConnected myThreadConnected;
    private UUID myUUID;

    //Called in ThreadConnectBTdevice once connect successed
    //to start ThreadConnected
    private void startThreadConnected(BluetoothSocket socket){

        myThreadConnected = new ThreadConnected(socket);
        myThreadConnected.start();
    }

    /*
    ThreadConnectBTdevice:
    Background Thread to handle BlueTooth connecting
    */
    private class ThreadConnectBTdevice extends Thread {

        private BluetoothSocket bluetoothSocket = null;
        private final BluetoothDevice bluetoothDevice;


        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                // TODO IMPLEMENTAR ISTO textStatus.setText("bluetoothSocket: \n" + bluetoothSocket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            boolean success = false;
            try {
                bluetoothSocket.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();

                final String eMessage = e.getMessage();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        //TODO IMPLEMENTAR ISTO textStatus.setText("something wrong bluetoothSocket.connect(): \n" + eMessage);
                    }
                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }

            if(success){
                //connect successful
                final String msgconnected = "connect successful:\n"
                        + "BluetoothSocket: " + bluetoothSocket + "\n"
                        + "BluetoothDevice: " + bluetoothDevice;

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        /* TODO IMPLEMENTAR
                        textStatus.setText("");
                        textByteCnt.setText(""); */
                        Toast.makeText(MainActivity.this, msgconnected, Toast.LENGTH_LONG).show();
                        /* TODO IMPLEMENTAR
                        listViewPairedDevice.setVisibility(View.GONE);
                        inputPane.setVisibility(View.VISIBLE);
                        */
                    }
                });

                startThreadConnected(bluetoothSocket);

            }else{
                //fail
            }
        }

        public void cancel() {

            Toast.makeText(getApplicationContext(),
                    "close bluetoothSocket",
                    Toast.LENGTH_LONG).show();

            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

    }

    /*
    ThreadConnected:
    Background Thread to handle Bluetooth data communication
    after connected
     */
    private class ThreadConnected extends Thread {
        private final BluetoothSocket connectedBluetoothSocket;
        private final InputStream connectedInputStream;
        private final OutputStream connectedOutputStream;

        public ThreadConnected(BluetoothSocket socket) {
            connectedBluetoothSocket = socket;
            InputStream in = null;
            OutputStream out = null;

            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            connectedInputStream = in;
            connectedOutputStream = out;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            String strRx = "";

            while (true) {
                try {
                    bytes = connectedInputStream.read(buffer);
                    final String strReceived = new String(buffer, 0, bytes);
                    final String strByteCnt = String.valueOf(bytes) + " bytes received.\n";

                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            /* TODO IMPLEMENTAR
                            textStatus.append(strReceived);
                            textByteCnt.append(strByteCnt);
                            */
                        }});

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    final String msgConnectionLost = "Connection lost:\n"
                            + e.getMessage();
                    runOnUiThread(new Runnable(){

                        @Override
                        public void run() {
                            /* TODO IMLPEMENTAR
                            textStatus.setText(msgConnectionLost);
                            */
                        }});
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                connectedOutputStream.write(buffer);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                connectedBluetoothSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
