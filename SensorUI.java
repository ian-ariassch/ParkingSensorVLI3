package com.example.sensor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class SensorUI extends AppCompatActivity {
    ImageView imageDistance ;
    ImageView bluetoothConnection;
    ImageView carro;
    TextView distance;
    String readMessage = new String();
    String dataInPrint = new String();
    int value;
    int lastvalue;
    MediaPlayer player;
    int change;

    static Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    private static String address = null;
    //-------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_ui);
        imageDistance = findViewById(R.id.sensor);
        distance = findViewById(R.id.distance);
        bluetoothConnection = findViewById(R.id.bluetooth) ;
        carro = findViewById(R.id.carro);
        player = MediaPlayer.create(this,R.raw.ayuwoki);
        change = 0;
        lastvalue = 357;
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    readMessage = (String) msg.obj;
                    //Toast.makeText(UserInterfaz.this, readMessage, Toast.LENGTH_SHORT).show();


                    DataStringIN.append(readMessage);
                    value = converterInt(DataStringIN.toString());
                    distance.setText(DataStringIN.toString());
                    DataStringIN.delete(0, DataStringIN.length()-1);
                    setBackground(value);

                }
            }
        };
        btAdapter = BluetoothAdapter.getDefaultAdapter(); // get Bluetooth adapter
        VerificarEstadoBT();
    }
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        //crea un conexion de salida segura para el dispositivo
        //usando el servicio UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    protected void onStart() {
        super.onStart();
        player.start();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //Consigue la direccion MAC desde DeviceListActivity via intent
        Intent intent = getIntent();
        //Consigue la direccion MAC desde DeviceListActivity via EXTRA
        address = intent.getStringExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS);//<-<- PARTE A MODIFICAR >->->
        //Setea la direccion MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {}
        }
        MyConexionBT = new SensorUI.ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        { // Cuando se sale de la aplicación esta parte permite
            // que no se deje abierto el socket
            btSocket.close();
        } catch (IOException e2) {}
    }

    //Comprueba que el dispositivo Bluetooth Bluetooth está disponible y solicita que se active si está desactivado
    private void VerificarEstadoBT() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Crea la clase que permite crear el evento de conexion
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //Envio de trama
        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


    public void setBackground(int dist){
        if(dist>200){
            imageDistance.setBackgroundResource(R.drawable.ic_0);
            player.stop();
            player = MediaPlayer.create(this,R.raw.ayuwoki);
            player.start();


        }
        else if(dist<200 && dist >100 && change !=1){
            imageDistance.setBackgroundResource(R.drawable.ic_25);
            player.stop();
            player = MediaPlayer.create(this,R.raw.padoru);
            player.start();
            change = 1;

        }
        else if(dist<=100 && dist >50 && change !=3){
            imageDistance.setBackgroundResource(R.drawable.ic_50);
            player.stop();
            player = MediaPlayer.create(this,R.raw.gaa);
            player.start();
            change = 3;

        }
        else if(dist<=50 && dist >20 && change !=4){
            imageDistance.setBackgroundResource(R.drawable.ic_75);
            player.stop();
            player = MediaPlayer.create(this,R.raw.miau);
            player.start();
            change = 4;

        }
        else if(dist<20 && change !=5){
            imageDistance.setBackgroundResource(R.drawable.ic_100);
            player.stop();
            player = MediaPlayer.create(this,R.raw.ahhh);
            player.start();
            change = 5;
        }
    }

    public Integer converterInt(String data){
        if(data.length()==0 || data.length()>4)
            return 357;
        try {
            lastvalue = Integer.valueOf(data.substring(0, data.length()-1));
            return lastvalue;
        }
        catch (Exception e){
            return lastvalue;
        }


    }

}
