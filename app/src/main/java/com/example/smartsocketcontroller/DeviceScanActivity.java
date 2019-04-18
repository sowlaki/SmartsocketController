package com.example.smartsocketcontroller;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zentri.zentri_ble_command.BLECallbacks;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;




import static android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED;



public class DeviceScanActivity extends FragmentActivity implements DeviceListAdapter.onItemClicked{

    private final static String TAG = DeviceScanActivity.class.getName();

    private BluetoothManager mBLEManager;
    private BluetoothAdapter mBLEAdapter;
    private BluetoothDevice mBLEDevice;
    private BluetoothGatt bluetoothGatt;
    private int connectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";





    private RecyclerView.Adapter recyclerAdapter;
    public int nDevice = 0;

    private static final int  MAX_DEVICE = 50;
    private static final int SCAN_MAXTIME = 10000; // Scan for devices maximum of x ms,

    private ArrayList<String> DeviceName;
    private ArrayList<String> DeviceAddress;
    private ArrayList<String> Connect_Address_Name;

    private AlertDialog alertDialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.devicescanactivity);



        // ToDo: Add a stop scan button that turns into a start scan button when pressed, vice versa.

        //Progress bar intermediete = true
        ProgressBar scan_bar = findViewById(R.id.scanning_progress);
        scan_bar.setMax(100); // Defined in xml too

        TextView toolbar_title = findViewById(R.id.toolbar_text);
        toolbar_title.setTextIsSelectable(false);

        Handler scan_handler = new Handler();


        // Dialog fragment init
        alertDialog = createDialog();
        // Initialize data array

        DeviceName = new ArrayList<>();
        DeviceName.add("");
        DeviceAddress = new ArrayList<>();
        DeviceAddress.add("" );
        Connect_Address_Name = new ArrayList<>();
        Connect_Address_Name.add("");


        RecyclerView recyclerView = findViewById(R.id.device_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        recyclerAdapter = new DeviceListAdapter(DeviceName, DeviceAddress, this);
        recyclerView.setAdapter(recyclerAdapter);

        BLE_init();
        BluetoothLeScanner mBLEScanner = mBLEAdapter.getBluetoothLeScanner();



            if ((nDevice == MAX_DEVICE) & (mBLEScanner != null)) {
                mBLEScanner.stopScan(leScanCallback);
                scan_bar.setProgress(0);
                scan_bar.setIndeterminate(false);
            }

            if (mBLEScanner != null) {
                mBLEScanner.startScan(leScanCallback);

                scan_handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothLeScanner mBLEScanner = mBLEAdapter.getBluetoothLeScanner();
                        mBLEScanner.stopScan(leScanCallback);
                    }
                }, SCAN_MAXTIME);

            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                int REQUEST_ENABLE_BT = 1;
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                mBLEScanner = mBLEAdapter.getBluetoothLeScanner();
                mBLEScanner.startScan(leScanCallback);

                scan_handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        BluetoothLeScanner mBLEScanner = mBLEAdapter.getBluetoothLeScanner();
                        mBLEScanner.stopScan(leScanCallback);
                    }
                }, SCAN_MAXTIME);
            }


    }
    // ToDo: Empty the Device Info arrays lists when scan activity closes, (software reset).
    public void onDestroy(){
        super.onDestroy();


        DeviceName = new ArrayList<>();
        for (int i = 0; i < DeviceName.size(); i++) {
            DeviceName.add("");
        }

        DeviceAddress = new ArrayList<>();
        for (int i = 0; i < DeviceAddress.size(); i++){
            DeviceAddress.add("" );
        }

        Connect_Address_Name = new ArrayList<>();
        for(int i = 0; i < Connect_Address_Name.size(); i++){
            Connect_Address_Name.add("");
        }

        mBLEAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
    }


        // True if duplicate
    public static boolean duplicate_string(String input, ArrayList<String> list){

        boolean DUPLICATE;

        Set<String> set = new HashSet<>(list);
        ArrayList<String> arrayList = new ArrayList<>(list);


        set.add(input);
        arrayList.add(input);

        if(arrayList.size() > set.size()){
            DUPLICATE = true;
        }
        else
            DUPLICATE = false;


        set.clear();
        arrayList.clear();

        return DUPLICATE;
    }


    public ScanCallback leScanCallback =
            new ScanCallback() {


                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    mBLEDevice = result.getDevice();

                    if (nDevice >= (MAX_DEVICE - 1)){
                        BluetoothLeScanner mBLEScanner = mBLEAdapter.getBluetoothLeScanner();
                        mBLEScanner.stopScan(leScanCallback);
                        nDevice = 0;
                    }
                    // Start of Device name
                    if(true) {
                        if ((mBLEDevice.getName() == null) && (!duplicate_string(mBLEDevice.getAddress(), DeviceAddress))) {

                            DeviceName.add("");
                            DeviceName.add(nDevice, "N/A");
                            recyclerAdapter.notifyItemInserted(nDevice);
                            DeviceName.remove(nDevice + 1);
                            recyclerAdapter.notifyItemRemoved(nDevice);
                        }
                        else if((!duplicate_string(mBLEDevice.getAddress(), DeviceAddress)))  {
                            Log.d("Device Name: " + nDevice, mBLEDevice.getName());
                            DeviceName.add("");
                            DeviceName.add(nDevice, mBLEDevice.getName());
                            recyclerAdapter.notifyItemInserted(nDevice);
                            DeviceName.remove(nDevice + 1);
                            recyclerAdapter.notifyItemRemoved(nDevice + 1);
                        }
                    }  // End of Device Name

                    //Start of Device Address
                    if((mBLEDevice.getAddress() != null) && (!duplicate_string(mBLEDevice.getAddress(), DeviceAddress))) {
                        Log.d("Device Address " + nDevice, mBLEDevice.getAddress());
                        DeviceAddress.add("");
                        DeviceAddress.add(nDevice,mBLEDevice.getAddress());
                        recyclerAdapter.notifyItemInserted(nDevice);
                        DeviceAddress.remove(nDevice + 1);
                        recyclerAdapter.notifyItemRemoved(nDevice +1);
                        ++nDevice;
                    }
                    // End off device address
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);

                }
            };



    public void BLE_init(){

        final BluetoothManager mBLEManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBLEAdapter = mBLEManager.getAdapter();
        mBLEAdapter.enable();

    }

    //Initialise clicklisteners in the device adapters
    @Override
    public void onItemClick(int position) {


        Connect_Address_Name.ensureCapacity(2);

        if (DeviceAddress.get(position) != null && DeviceName.get(position) != null) {
            Connect_Address_Name.add(1, DeviceAddress.get(position));
            Connect_Address_Name.add(2, DeviceName.get(position));
            Log.d("The device clicked:", Connect_Address_Name.get(0) + Connect_Address_Name.get(1));

            mBLEDevice = mBLEAdapter.getRemoteDevice( DeviceAddress.get(position));
            bluetoothGatt = mBLEDevice.connectGatt(this, false, gattCallback);
            alertDialog.show();

        }
    }

    // Gatt Client stuff
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
            if(newState == BluetoothProfile.STATE_CONNECTED){
                intentAction = ACTION_GATT_CONNECTED;
                connectionState = STATE_CONNECTED;
                bluetoothGatt.discoverServices();
                Log.d("Connection state: ", "Connected");

                updateDialogText("Connected");

                finish();



            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                intentAction = ACTION_GATT_DISCONNECTED;
                connectionState = STATE_DISCONNECTED;
                Log.d("Connection state: ", "Disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);



        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    // Connecting status dialog
    public AlertDialog createDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.connectingstatus, null))
                .setCancelable(true);

        bluetoothGatt.discoverServices();

        return builder.create();

    }

    public void updateDialogText(String text){

        TextView connectingString = findViewById(R.id.connectingTextView);
        if(connectingString != null)
        connectingString.setText(text);


    }

}


