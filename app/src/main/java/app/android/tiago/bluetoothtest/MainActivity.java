package app.android.tiago.bluetoothtest;

import android.app.Activity;
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
    public BluetoothDevice dispositivoSeleccionado = null;
    private TextView txtDispositivoNome;
    private TextView txtDispositivoAddress;

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

        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP);

        txtDispositivoNome = findViewById(R.id.txtDispositivoNome);
        txtDispositivoAddress = findViewById(R.id.txtDispositivoAddress);

        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
        }
    }

    // Desligar se o bluetooth não for ligado
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_ENABLE_BT){
            if(resultCode != Activity.RESULT_OK){
                Toast.makeText(this,
                        "É necessário ativar o bluetooth",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    //Funções
    public void escolherDispositivo(){

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            List<String> lista = new ArrayList<String>();
            List<String> listaNome = new ArrayList<String>();
            List<String> listaAddress = new ArrayList<String>();
            for (BluetoothDevice device : pairedDevices) {
                listaAddress.add(device.getAddress());
                listaNome.add(device.getName());
                lista.add(device.getName() + "\n" + device.getAddress());
            }

            final String[] arLista = lista.toArray(new String[lista.size()]);
            final String[] arListaNome = listaNome.toArray(new String[listaNome.size()]);
            final String[] arListaAddress = listaAddress.toArray(new String[listaAddress.size()]);

            AlertDialog.Builder dialogoA = new AlertDialog.Builder(MainActivity.this);
            dialogoA.setTitle("Escolher dispositivo")
                    .setItems(arLista, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            txtDispositivoNome.setText(arListaNome[which]);
                            txtDispositivoAddress.setText(arListaAddress[which]);
                            dispositivoSeleccionado = mBluetoothAdapter.getRemoteDevice(arListaAddress[which]);
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

    public void ligar() {
        if (dispositivoSeleccionado == null){
            AlertDialog.Builder dialogoA = new AlertDialog.Builder(MainActivity.this);
            dialogoA.setTitle("Erro")
                    .setMessage("Não foi escolhido nenhum dispositivo. Escolha um dispositivo primeiro.")
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        }
        else{
            //TODO em construção!
            BluetoothDevice device = dispositivoSeleccionado;
            Toast.makeText(MainActivity.this,
                    "Name: " + device.getName() + "\n"
                            + "Address: " + device.getAddress() + "\n"
                            + "BondState: " + device.getBondState() + "\n"
                            + "BluetoothClass: " + device.getBluetoothClass() + "\n"
                            + "Class: " + device.getClass(),
                    Toast.LENGTH_LONG).show();

            //textStatus.setText("start ThreadConnectBTdevice");
            myThreadConnectBTdevice = new ThreadConnectBTdevice(device);
            myThreadConnectBTdevice.start();


        }

    }

    public void desligar() {
        if(myThreadConnectBTdevice!=null){
            myThreadConnectBTdevice.cancel();
            myThreadConnectBTdevice = null;
        }
        else{
            Toast.makeText(getApplicationContext(),"Não existe ligação", Toast.LENGTH_LONG).show();
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
            ligar();
        }
    };

    private Button btnDesligar;

    private View.OnClickListener btnDesligar_click = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            desligar();
        }
    };




    // Implementação da comunicação bluetooth


    ThreadConnected myThreadConnected;
    ThreadConnectBTdevice myThreadConnectBTdevice;

    private UUID myUUID;

    private final String UUID_STRING_WELL_KNOWN_SPP =
            //"7f0795b6-d136-45f7-8776-a11afae79ebf";
            "00001101-0000-1000-8000-00805F9B34FB";


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
        private BluetoothDevice bluetoothDevice;


        private ThreadConnectBTdevice(BluetoothDevice device) {
            bluetoothDevice = device;

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID);
                Toast.makeText(getApplicationContext(),"Ponto 1", Toast.LENGTH_LONG).show();
                // TODO IMPLEMENTAR ISTO textStatus.setText("bluetoothSocket: \n" + bluetoothSocket);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Toast.makeText(getApplicationContext(),"Erro 1", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getApplicationContext(),"Erro 2 \n"+ eMessage, Toast.LENGTH_LONG).show();
                    }
                });

                try {
                    bluetoothSocket.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(),"Erro 3", Toast.LENGTH_LONG).show();
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
                    "Ligação Bluetooth terminada",
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
