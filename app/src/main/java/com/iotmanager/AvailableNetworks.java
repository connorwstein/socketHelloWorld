package com.iotmanager;
import static com.iotmanager.Constants.*;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class AvailableNetworks extends AppCompatActivity {
    private static final String TAG="Connors Debug";
    private String selectedDevice;
    private String networkPassword="";
    private ListView networkListView;
    private WifiManager manager;
    private String espNetworkName;
    private String espNetworkPass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        selectedDevice=getIntent().getStringExtra("Name");
        setTitle(selectedDevice);
        espNetworkName=getIntent().getStringExtra("espNetworkName");
        espNetworkPass=getIntent().getStringExtra("espNetworkPass");

        Log.i(TAG, "Device: " + selectedDevice);
        manager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
        listAllNetworks();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.actionRefresh:
                listAllNetworks();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void listAllNetworks(){
        boolean scanSuccess=manager.startScan();
        if(!scanSuccess){
            Log.i(TAG,"Unable to scan.");
        }
        networkListView=(ListView)findViewById(R.id.listNetworks);
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                getAllSSIDs()
        );
        networkListView.setAdapter(arrayAdapter);
        networkListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                String selectedNetworkSSID = (String) networkListView.getItemAtPosition(position);
                final Network network = new Network(selectedNetworkSSID, getApplicationContext());

                final ProgressDialog progressDialog=new ProgressDialog(AvailableNetworks.this);
                progressDialog.setMessage("Telling device to connect ...");
                progressDialog.show();
                Thread sendConnectRequest=SocketClient.tcpSend("Connect:"+network.ssid+";"+network.password, DEFAULT_DEVICE_IP,DEFAULT_DEVICE_PORT, progressDialog,
                        new Handler(){
                            @Override
                            public void handleMessage(Message msg){
                                progressDialog.dismiss();
                                handlePostSend(msg,network);
                            }
                        });

                if (network.isEnterprise()) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "No support for enterprise networks", Toast.LENGTH_LONG).show();
                    return;
                }
                else if (network.hasPassword()) {
                    setNetworkPasswordThenSend(network, AvailableNetworks.this, progressDialog, sendConnectRequest); //reuse method
                }
                else{
                    sendConnectRequest.start();
                }

            }
        });
    }
    private void handlePostSend(Message msg,Network network){
        if(msg.getData().getInt("Error code")==0){
            Toast.makeText(AvailableNetworks.this,"Error sending data, verify connection to device",Toast.LENGTH_LONG).show();
        }
        else{
            connectAndroidToSameNetwork(network);
        }
    }

    private void reconnect(){
        final ProgressDialog progressDialog=new ProgressDialog(AvailableNetworks.this);
        progressDialog.setMessage("Reconnecting to device...");
        progressDialog.show();
        final Network network=new Network(espNetworkName,AvailableNetworks.this);
        network.setPassword(espNetworkPass);
        Thread connectThread=AndroidWifiHandler.connect(network,progressDialog, new Handler(){
            //Handle what happens when thread has completed
            @Override
            public void handleMessage(Message msg){
                progressDialog.dismiss();
                if(msg.getData().getInt("Error code")!=3){
                    Toast.makeText(AvailableNetworks.this,"Error reconnecting, ensure device is on and set up as an access point. Try refreshing.",Toast.LENGTH_LONG).show();
                    Intent returnToAvailableDevices=new Intent(AvailableNetworks.this,AvailableDevices.class);
                    startActivity(returnToAvailableDevices);
                }
                else{
                    Toast.makeText(AvailableNetworks.this, "Successfully reconnected. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handlePostConnection(Message msg, Network network){
        switch(msg.getData().getInt("Error Code")){
            case 0:
                Toast.makeText(AvailableNetworks.this,"Unable to add network, ensure password is correct and you are connected to the device",Toast.LENGTH_LONG).show();
                reconnect();
                break;
            case 1:
                Toast.makeText(AvailableNetworks.this,"Unable to connect to network, ensure password is correct and you are connected to the device",Toast.LENGTH_LONG).show();
                reconnect();
                break;
            case 2:
                Toast.makeText(AvailableNetworks.this,"Unable to get IP, ensure password is correct and you are connected to the device",Toast.LENGTH_LONG).show();
                reconnect();
                break;
            case 3:
                Intent mainActivityIntent=new Intent(AvailableNetworks.this,MainActivity.class);
                startActivity(mainActivityIntent);
                break;
        }
    }



   private void setNetworkPasswordThenSend(final Network network,Context context, final ProgressDialog progressDialog,final Thread sendConnectRequest){
        final EditText passwordInput=new EditText(context);
        passwordInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage("Enter password for network")
                .setView(passwordInput)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        network.setPassword(passwordInput.getText().toString());
                        dialog.cancel();
                        sendConnectRequest.start();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.dismiss();
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    private void connectAndroidToSameNetwork(final Network network){
        final ProgressDialog progressDialog=new ProgressDialog(AvailableNetworks.this);
        progressDialog.setMessage("Sent connection request. Connecting android to same network ...");
        progressDialog.show();
        Thread connectThread=AndroidWifiHandler.connect(network,progressDialog, new Handler(){
            //Handle what happens when thread has completed
            @Override
            public void handleMessage(Message msg){
                progressDialog.dismiss();
                handlePostConnection(msg, network);
            }
        });
        connectThread.start();
    }

    private List<String> getAllSSIDs(){
        List<ScanResult> networks=manager.getScanResults();
        List <String> ssids=new ArrayList<String>();
        for(int i=0;i<networks.size();i++){
            ssids.add(networks.get(i).SSID);

        }
        return ssids;
    }

}
