/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.example.forumsladersimulator.gattserver;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.androidthings.gattserver.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GattServerActivity extends Activity {
    private static final String TAG = GattServerActivity.class.getSimpleName();

    private TextView mCurrentDatagramView;
    private BluetoothManager mBluetoothManager;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;
    private Set<BluetoothDevice> mRegisteredDevices = new HashSet<>();

    private int ticks = 0;
    private String[] datagram = {
            "$FL5,00C000,0,0,4112,4112,4114,-10,0,282,1,113,288,222,469*51",
            "$FLB,65,94515,5830,0*49",
            "$FL5,00C000,0,0,4112,4112,4115,-10,0,282,1,112,288,222,469*51",
            "$FLC,4,256,256,7,7,0*4D",
            "$FL5,00C000,0,0,4112,4114,4115,-10,0,282,1,111,288,222,469*54",
            "$FLB,65,94509,5835,0*41",
            "$FL5,00C000,0,0,4112,4114,4115,-10,0,282,1,110,288,222,469*55",
            "$FLC,5,96,100,507,2,20837689*4B",
            "$FL5,00C000,0,0,4112,4112,4115,-10,0,282,1,109,288,222,469*5B",
            "$FLB,65,94509,5835,0*41",
            "$FL5,00C000,0,0,4111,4112,4115,-10,0,282,1,108,288,222,469*59",
            "$FLC,0,525,26,150,6016,53*7C",
            "$FL5,00C000,0,0,3787,3784,3797,-532,507,286,1,130,655,278,579*61",
            "$FLB,75,93576,6660,0*42",
            "$FL5,00C000,0,0,3787,3784,3796,-532,507,286,1,129,655,278,579*68",
            "$FLC,1,10,9,80,6667,30*4A",
            "$FL5,00C000,0,0,3789,3784,3796,-531,507,286,1,128,655,278,579*64",
            "$FLB,75,93581,6656,0*4F",
            "$FL5,00C000,0,0,3787,3784,3796,-531,500,286,1,127,655,278,579*62",
            "$FLC,2,4502,0,70,0,70*78",
            "$FL5,00C000,0,0,3787,3783,3796,-531,500,286,1,126,655,278,579*64",
            "$FLB,75,93577,6660,0*43",
            "$FL5,00C000,0,0,3787,3784,3796,-531,507,286,1,125,655,278,579*67",
            "$FLC,3,274,0,0,76,0*7A",
            "$FL5,00C000,0,0,3787,3784,3796,-531,500,286,1,124,655,278,579*61",
            "$FLB,75,93582,6655,0*4F",
            "$FL5,00C000,0,0,3787,3783,3796,-531,507,286,1,123,655,278,579*66",
            "$FLC,4,86,86,9,9,0*4D",
            "$FL5,00C000,0,0,3787,3783,3796,-528,507,286,1,122,655,278,579*6F",
            "$FLB,75,93577,6660,0*43",
            "$FL5,00C000,0,0,3787,3783,3795,-529,507,286,1,121,655,278,579*6E",
            "$FLC,5,106,69,507,2,15632917*48",
            "$FL5,00C000,0,0,3787,3783,3796,-529,507,286,1,120,655,278,579*6C",
            "$FLB,75,93579,6658,0*46",
            "$FL5,00C000,0,0,3786,3783,3796,-530,500,286,1,119,655,278,579*68",
            "$FLC,0,10,9,80,6667,30*4B",
            "$FL5,00C000,0,0,3786,3783,3796,-529,507,286,1,118,655,278,579*66",
            "$FLB,75,93575,6661,0*40",
            "$FL5,00C000,0,0,3787,3783,3795,-528,500,286,1,117,655,278,579*6D",
            "$FLC,1,10,9,80,6667,30*4A",
            "$FL5,00C000,0,0,3786,3781,3796,-528,507,286,1,116,655,278,579*6B",
            "$FLB,75,93579,6658,0*46",
            "$FL5,00C000,0,0,3786,3781,3796,-529,500,286,1,115,655,278,579*6E",
            "$FLC,2,4502,0,70,0,70*78",
            "$FL5,00C000,0,0,3786,3783,3793,-529,500,286,1,114,655,278,579*68",
            "$FLB,75,93578,6659,0*46",
            "$FL5,00C000,0,0,3786,3783,3795,-529,507,286,1,113,655,278,579*6E",
            "$FLC,3,274,0,0,76,0*7A",
            "$FL5,00C000,0,0,3785,3781,3795,-529,500,286,1,112,655,278,579*69",
            "$FLB,75,93578,6659,0*46",
            "$FL5,00C000,0,0,3786,3783,3795,-529,507,286,1,111,655,278,579*6C",
            "$FLC,4,86,86,9,9,0*4D",
            "$FL5,00C000,0,0,3786,3781,3795,-529,500,286,1,110,655,278,579*68",
            "$FLB,75,93577,6660,0*43",
            "$FL5,00C000,0,0,3785,3781,3793,-530,500,286,1,109,655,278,579*6D",
            "$FLC,5,106,69,507,2,15565301*44",
            "$FL5,00C000,0,0,3785,3783,3795,-530,500,286,1,108,655,278,579*68",
            "$FLB,75,93581,6656,0*4F",
            "$FL5,00C000,0,0,3785,3781,3793,-529,500,286,1,107,655,278,579*6B",
            "$FLC,0,10,9,80,6667,30*4B",
            "$FL5,00C000,0,0,3785,3781,3795,-529,507,286,1,106,655,278,579*6B",
            "$FLB,75,93580,6657,0*4F",
            "$FL5,00C000,0,0,3785,3781,3793,-529,507,286,1,105,655,278,579*6E",
            "$FLC,1,10,9,80,6667,30*4A",
            "$FL5,00C000,0,0,3785,3781,3793,-529,500,286,1,104,655,278,579*68",
            "$FLB,75,93581,6656,0*4F",
            "$FL5,00C000,0,0,3785,3781,3793,-529,500,286,1,103,655,278,579*6F",
            "$FLC,2,4502,0,70,0,70*78",
            "$FL5,00C000,0,0,3783,3780,3793,-527,500,286,1,102,655,278,579*67",
            "$FLB,75,93579,6658,0*46",
            "$FL5,00C000,0,0,3785,3781,3793,-527,500,286,1,101,655,278,579*63",
            "$FLC,3,274,0,0,76,0*7A",
            "$FL5,00C000,0,0,3785,3781,3793,-527,500,286,1,100,655,278,579*62",
            "$FLB,75,93579,6658,0*46",
            "$FL5,00C000,0,0,3783,3780,3792,-527,500,286,1,99,655,278,579*55",
            "$FLC,4,86,86,9,9,0*4D",
            "$FL5,00C000,0,0,3783,3780,3792,-528,500,286,1,98,655,278,579*5B",
            "$FLB,75,93577,6660,0*43",
            "$FL5,00C000,0,0,3785,3780,3792,-527,500,286,1,97,655,278,579*5D",
            "$FLC,5,106,69,507,2,15498209*4E",
            "$FL5,00C000,0,0,3783,3780,3792,-527,500,286,1,96,655,278,579*5A",
            "$FLB,75,93573,6663,0*44",
            "$FL5,00C000,0,0,3783,3780,3793,-526,500,286,1,95,655,278,579*59",
            "$FLC,0,10,9,80,6667,30*4B",
            "$FL5,00C000,0,0,3783,3778,3792,-527,500,286,1,94,655,278,579*5F",
            "$FLB,75,93579,6658,0*46",
            "$FL5,00C000,0,0,3783,3780,3792,-527,500,286,1,93,655,278,579*5F",
            "$FLC,1,10,9,80,6667,30*4A",
            "$FL5,00C000,0,0,3783,3778,3792,-529,507,286,1,92,655,278,579*50",
            "$FLB,75,93578,6659,0*46",
            "$FL5,00C000,0,0,3782,3780,3792,-528,500,286,1,91,655,278,579*53",
            "$FLC,2,4502,0,70,0,70*78",
            "$FL5,00C000,0,0,3783,3780,3792,-528,500,286,1,90,655,278,579*53",
            "$FLB,75,93578,6659,0*46",
            "$FL5,00C000,0,0,3782,3778,3790,-528,500,286,1,89,655,278,579*5F",
            "$FLC,3,274,0,0,76,0*7A",
            "$FL5,00C000,0,0,3782,3778,3790,-529,500,286,1,88,655,278,579*5F",
            "$FLB,75,93580,6657,0*4F",
            "$FL5,00C000,0,0,3782,3778,3790,-529,500,286,1,87,655,278,579*50",
            "$FLC,4,86,86,9,9,0*4D",
            "$FL5,00C000,0,0,3782,3778,3790,-528,500,286,1,86,655,278,579*50",
            "$FLB,75,93582,6655,0*4F",
            "$FL5,00C000,0,0,3782,3778,3790,-527,500,286,1,85,655,278,579*5C",
            "$FLC,5,106,69,507,2,15431632*41",
            "$FL5,00C000,0,0,3782,3778,3790,-528,507,286,1,84,655,278,579*55",
            "$FLB,75,93580,6657,0*4F",
            "$FL5,00C000,0,0,3782,3778,3790,-528,500,286,1,83,655,278,579*55",
            "$FLC,0,10,9,80,6667,30*4B",
            "$FL5,00C000,0,0,3782,3778,3790,-526,500,286,1,82,655,278,579*5A",
            "$FLB,75,93577,6660,0*43",
            "$FL5,00C000,0,0,3782,3778,3790,-526,500,286,1,81,655,278,579*59",
            "$FLC,1,10,9,80,6667,30*4A",
            "$FL5,00C000,0,0,3782,3777,3789,-525,500,286,1,80,655,278,579*5C",
            "$FLB,75,93581,6656,0*4F",
            "$FL5,00C000,0,0,3782,3778,3789,-526,500,286,1,79,655,278,579*56",
            "$FLC,2,4502,0,70,0,70*78",
            "$FL5,00C000,0,0,3782,3777,3790,-526,500,286,1,78,655,278,579*50",
            "$FLB,75,93572,6664,0*42",
            "$FL5,00C000,0,0,3780,3777,3789,-526,500,286,1,77,655,278,579*55",
            "$FLC,3,274,0,0,76,0*7A",
            "$FL5,00C000,0,0,3782,3777,3790,-526,500,286,1,76,655,278,579*5E",
            "$FLB,75,93576,6660,0*42",
            "$FL5,00C000,0,0,3780,3777,3790,-525,500,286,1,75,655,278,579*5C",
            "$FLC,4,86,86,9,9,0*4D",
            "$FL5,00C000,0,0,3780,3777,3787,-525,500,286,1,74,655,278,579*5B",
            "$FLB,75,93578,6659,0*46",
            "$FL5,00C000,0,0,3780,3777,3789,-525,507,286,1,73,655,278,579*55",
            "$FLC,5,106,69,507,2,15363979*41",
            "$FL5,00C000,0,0,3778,3773,3786,-525,500,286,1,47,655,278,579*59",
            "$FLC,0,10,9,80,6667,30*4B",
            "$FL5,00C000,0,0,3776,3773,3785,-525,507,286,1,46,655,278,579*52",
            "$FLB,75,93578,6659,0*46",
            "$FL5,00C000,0,0,3778,3773,3785,-525,500,286,1,45,655,278,579*58",
            "$FLC,1,10,9,80,6667,30*4A",
            "$FL5,00C000,0,0,3776,3773,3785,-525,500,286,1,44,655,278,579*57",
            "$FLB,75,93575,6661,0*40",
            "$FL5,00C000,0,0,3776,3773,3785,-524,500,286,1,43,655,278,579*51",
            "$FLC,2,4502,0,70,0,70*78",
            "$FL5,00C000,0,0,3776,3771,3785,-523,500,286,1,42,655,278,579*55",
            "$FLB,75,93576,6660,0*42",
            "$FL5,00C000,0,0,3776,3771,3783,-524,500,286,1,41,655,278,579*57",
            "$FLC,3,274,0,0,76,0*7A",
            "$FL5,00C000,0,0,3776,3773,3785,-524,500,285,1,40,655,278,579*51",
            "$FLB,75,93579,6658,0*46",
            "$FL5,00C000,0,0,3776,3771,3785,-523,500,286,1,39,655,278,579*59",
            "$FLC,4,86,86,9,9,0*4D",
            "$FL5,00C000,0,0,3775,3773,3785,-523,500,286,1,38,655,278,579*59",
            "$FLB,75,93575,6661,0*40",
            "$FL5,00C000,0,0,3775,3771,3783,-523,500,286,1,37,655,278,579*52",
            "$FLC,5,106,68,507,2,15164399*41",
            "$FL5,00C000,0,0,3775,3771,3783,-523,500,286,1,36,655,278,579*53",
            "$FLB,75,93578,6659,0*46",
            "$FL5,00C000,0,0,3775,3771,3783,-524,500,285,1,35,655,278,579*54",
            "$FLC,0,10,9,80,6667,30*4B"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        mCurrentDatagramView = (TextView) findViewById(R.id.text_time);

        // Devices with a display should not go to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final String value = datagram[ticks++ % datagram.length];
                mCurrentDatagramView.setText(value);

                notifyRegisteredDevices(value);
                handler.postDelayed(this, 1000);
            }
        }, 1000);

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        // We can't continue without proper Bluetooth support
        if (bluetoothAdapter == null || !checkBluetoothSupport(bluetoothAdapter)) {
            finish();
        }

        // Register for system Bluetooth events
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);
        if (!bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth is currently disabled...enabling");
            bluetoothAdapter.enable();
        } else {
            Log.d(TAG, "Bluetooth enabled...starting services");
            startAdvertising();
            startServer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        if (bluetoothAdapter.isEnabled()) {
            stopServer();
            stopAdvertising();
        }

        unregisterReceiver(mBluetoothReceiver);
    }

    /**
     * Verify the level of Bluetooth support provided by the hardware.
     * @param bluetoothAdapter System {@link BluetoothAdapter}.
     * @return true if Bluetooth is properly supported, false otherwise.
     */
    private boolean checkBluetoothSupport(BluetoothAdapter bluetoothAdapter) {

        if (bluetoothAdapter == null) {
            Log.w(TAG, "Bluetooth is not supported");
            return false;
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.w(TAG, "Bluetooth LE is not supported");
            return false;
        }

        return true;
    }

    /**
     * Listens for Bluetooth adapter events to enable/disable
     * advertising and server functionality.
     */
    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    startAdvertising();
                    startServer();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopServer();
                    stopAdvertising();
                    break;
                default:
                    // Do nothing
            }

        }
    };

    /**
     * Begin advertising over Bluetooth that this device is connectable
     * and supports the Current Time Service.
     */
    private void startAdvertising() {
        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothLeAdvertiser == null) {
            Log.w(TAG, "Failed to create advertiser");
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build();

        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .setIncludeTxPowerLevel(false)
                .addServiceUuid(new ParcelUuid(ForumsladerProfile.RX_TX_SERVICE))
                .build();

        mBluetoothLeAdvertiser
                .startAdvertising(settings, data, mAdvertiseCallback);
    }

    /**
     * Stop Bluetooth advertisements.
     */
    private void stopAdvertising() {
        if (mBluetoothLeAdvertiser == null) return;

        mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
    }

    /**
     * Initialize the GATT server instance with the services/characteristics
     * from the Time Profile.
     */
    private void startServer() {
        mBluetoothGattServer = mBluetoothManager.openGattServer(this, mGattServerCallback);
        if (mBluetoothGattServer == null) {
            Log.w(TAG, "Unable to create GATT server");
            return;
        }

        mBluetoothGattServer.addService(ForumsladerProfile.createTimeService());
    }

    /**
     * Shut down the GATT server.
     */
    private void stopServer() {
        if (mBluetoothGattServer == null) return;

        mBluetoothGattServer.close();
    }

    /**
     * Callback to receive information about the advertisement process.
     */
    private AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.i(TAG, "LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Log.w(TAG, "LE Advertise Failed: "+errorCode);
        }
    };

    /**
     * Send a time service notification to any devices that are subscribed
     * to the characteristic.
     */
    private void notifyRegisteredDevices(String value) {
        if (mRegisteredDevices.isEmpty()) {
            Log.i(TAG, "No subscribers registered");
            return;
        }
        Log.i(TAG, "Sending update to " + mRegisteredDevices.size() + " subscribers");

        for (BluetoothDevice device : mRegisteredDevices) {

            BluetoothGattCharacteristic testCharacteristic = mBluetoothGattServer
                    .getService(ForumsladerProfile.RX_TX_SERVICE)
                    .getCharacteristic(ForumsladerProfile.RX_TX_SERVICE_CHAR);
            testCharacteristic.setValue(value);
            mBluetoothGattServer.notifyCharacteristicChanged(device, testCharacteristic, false);
        }
    }

    /**
     * Callback to handle incoming requests to the GATT server.
     * All read/write requests for characteristics and descriptors are handled here.
     */
    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            Log.d(TAG, "onConnectionStateChange");
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "BluetoothDevice CONNECTED: " + device);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "BluetoothDevice DISCONNECTED: " + device);
                //Remove device from any active subscriptions
                mRegisteredDevices.remove(device);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicReadRequest");
            if (ForumsladerProfile.RX_TX_SERVICE_CHAR.equals(characteristic.getUuid())) {
                Log.i(TAG, "Read Test");
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_SUCCESS,
                        0,
                        "...world...".getBytes());
            } else {
                // Invalid characteristic
                Log.w(TAG, "Invalid Characteristic Read: " + characteristic.getUuid());
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                            BluetoothGattDescriptor descriptor) {
            Log.d(TAG, "onDescriptorReadRequest");
            if (ForumsladerProfile.CLIENT_CONFIG.equals(descriptor.getUuid())) {
                Log.d(TAG, "Config descriptor read");
                byte[] returnValue;
                if (mRegisteredDevices.contains(device)) {
                    returnValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                } else {
                    returnValue = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                }
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        returnValue);
            } else {
                Log.w(TAG, "Unknown descriptor read request");
                mBluetoothGattServer.sendResponse(device,
                        requestId,
                        BluetoothGatt.GATT_FAILURE,
                        0,
                        null);
            }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
            Log.d(TAG, "onDescriptorWriteRequest with descriptor " + descriptor.toString());
            if (ForumsladerProfile.CLIENT_CONFIG.equals(descriptor.getUuid())) {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(TAG, "Subscribe device to notifications: " + device);
                    mRegisteredDevices.add(device);
                } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(TAG, "Unsubscribe device from notifications: " + device);
                    mRegisteredDevices.remove(device);
                }

                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            null);
                }
            } else if (ForumsladerProfile.RX_TX_SERVICE_CHAR.equals(descriptor.getUuid())) {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(TAG, "Subscribe device to notifications: " + device);
                    mRegisteredDevices.add(device);
                } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
                    Log.d(TAG, "Unsubscribe device from notifications: " + device);
                    mRegisteredDevices.remove(device);
                }

                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_SUCCESS,
                            0,
                            null);
                }
            } else {
                Log.w(TAG, "Unknown descriptor write request");
                if (responseNeeded) {
                    mBluetoothGattServer.sendResponse(device,
                            requestId,
                            BluetoothGatt.GATT_FAILURE,
                            0,
                            null);
                }
            }
        }
    };
}
