package app.android.tiago.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

}
