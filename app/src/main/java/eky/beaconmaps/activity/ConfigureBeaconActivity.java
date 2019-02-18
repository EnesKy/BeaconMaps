package eky.beaconmaps.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.estimote.sdk.cloud.model.BroadcastingPower;
import com.estimote.sdk.connection.DeviceConnection;
import com.estimote.sdk.connection.DeviceConnectionCallback;
import com.estimote.sdk.connection.DeviceConnectionProvider;
import com.estimote.sdk.connection.exceptions.DeviceConnectionException;
import com.estimote.sdk.connection.scanner.ConfigurableDevice;
import com.estimote.sdk.connection.settings.SettingCallback;
import com.estimote.sdk.connection.settings.SettingsEditor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import eky.beaconmaps.FirebaseUtil;
import eky.beaconmaps.R;
import eky.beaconmaps.datamodel.BeaconData;

public class ConfigureBeaconActivity extends AppCompatActivity {

    private static final String TAG = "ConfigureBeaconActivity";
    private static String tag = "";
    private static String beaconUuid = "";

    private ConfigurableDevice configurableDevice;
    private DeviceConnection connection;
    private DeviceConnectionProvider connectionProvider;

    private LocationManager locationManager;
    private Location currentLocation;

    private TextView beaconIdTextView;
    private TextView geolocationValuesTextView;
    private Button saveButton;
    private ProgressDialog progressDialog;

    /*
    Here is a set of predefined UI elements for setting up tag, aisle number and placement.
    Based on tags different majors will be assigned (see tagsMajorsMapping).
    Based on placement apropriate broadcasting powers will be assigned (see placementPowerMapping).
    Based on tags you can also assign different minors by creating similar hash maps and modiffying the writeSettings method.
     */
    private Spinner tagsSpinner;
    private EditText aisleNumberTextField;
    private Spinner placementSpinner;
    Location fsmD;

    public static final HashMap<String, Integer> tagsMajorsMapping = new HashMap<String, Integer>() {{
        put("Store entrance", 100);
        put("Cash registers", 200);
        put("Customer service", 300);
        put("Electronics", 1002);
        put("Clothing", 1003);
        put("Groceries", 1005);
    }};

    public static final HashMap<String, BroadcastingPower> placementPowerMapping = new HashMap<String, BroadcastingPower>() {{
        put("Open area", BroadcastingPower.LEVEL_2);
        put("Short aisle", BroadcastingPower.LEVEL_2);
        put("Long aisle", BroadcastingPower.LEVEL_3);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_beacon);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = toolbar.findViewById(R.id.tv_toolbar_title);
        toolbarTitle.setText(R.string.title_activity_configure_beacon);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        Intent intent = getIntent();
        configurableDevice = intent.getParcelableExtra(ConnectBeaconActivity.EXTRA_SCAN_RESULT_ITEM_DEVICE);

        beaconIdTextView = findViewById(R.id.tv_beacon_id);
        beaconIdTextView.setText(configurableDevice.deviceId.toString());

        geolocationValuesTextView = findViewById(R.id.tv_geo_value);

        if (MapsActivity.getmLastKnownLocation() != null) {
            currentLocation = MapsActivity.getmLastKnownLocation();
            geolocationValuesTextView.setText("" + currentLocation.getLatitude()
                    + ", " + currentLocation.getLongitude()
                    + " ±" + currentLocation.getAccuracy());
        } else {
            geolocationValuesTextView.setText("Searching");
        }

        fsmD = new Location(LocationManager.GPS_PROVIDER);
        fsmD.setLatitude(41.046566);
        fsmD.setLongitude(28.943399);

        saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAction();
            }
        });

        tagsSpinner = findViewById(R.id.spn_tag);
        aisleNumberTextField = findViewById(R.id.tv_aisle);
        placementSpinner = findViewById(R.id.spn_placement);

        ArrayAdapter<String> adapterTags = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                tagsMajorsMapping.keySet().toArray(new String[tagsMajorsMapping.keySet().size()]));
        adapterTags.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tagsSpinner.setAdapter(adapterTags);

        ArrayAdapter<String> adapterPlacement = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                placementPowerMapping.keySet().toArray(new String[placementPowerMapping.keySet().size()]));
        adapterPlacement.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placementSpinner.setAdapter(adapterPlacement);

        connectionProvider = new DeviceConnectionProvider(this);
        connectToDevice();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectToDevice();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (connection != null && connection.isConnected())
            connection.close();
        locationManager.removeUpdates(locationListener);
    }

    private void connectToDevice() {
        if (connection == null || !connection.isConnected()) {
            connectionProvider.connectToService(new DeviceConnectionProvider.ConnectionProviderCallback() {
                @Override
                public void onConnectedToService() {
                    connection = connectionProvider.getConnection(configurableDevice);
                    connection.connect(new DeviceConnectionCallback() {
                        @Override
                        public void onConnected() { }

                        @Override
                        public void onDisconnected() { }

                        @Override
                        public void onConnectionFailed(DeviceConnectionException e) {
                            Log.d(TAG, e.getMessage());
                        }
                    });
                }
            });
        }
    }

    private void saveAction() {
        if (!connection.isConnected()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.wait_until_beacon_connected);
            builder.setCancelable(true);
            builder.setPositiveButton(
                    R.string.alert_ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            progressDialog = ProgressDialog.show(this, ".", ".");
            writeSettings();
        }
    }

    /*
    Prepare set of settings based on the default or your custom UI.
    Here is also a place to fetch apropriate settings for your device from your custom CMS
    or to save those that were be saved in the onSuccess block before calling displaySuccess.
    */

    /**
     *  todo: MapsActivity açılışında databaseden bilgileri çekerek haritaya pin ekle.
     */
    private void writeSettings() {
        SettingsEditor edit = connection.edit();
        edit.set(connection.settings.beacon.enable(), true);
        edit.set(connection.settings.deviceInfo.tags(), new HashSet<String>(Arrays.asList((String) tagsSpinner.getSelectedItem())));
        // You might want all your beacons to have a certain UUID.
        edit.set(connection.settings.beacon.proximityUUID(), UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"));
        edit.set(connection.settings.beacon.major(), tagsMajorsMapping.get((String) tagsSpinner.getSelectedItem()));
        tag = (String) tagsSpinner.getSelectedItem();
        edit.set(connection.settings.beacon.transmitPower(), placementPowerMapping.get(placementSpinner.getSelectedItem().toString()).powerInDbm);
        if (currentLocation != null) {
            edit.set(connection.settings.deviceInfo.geoLocation(), currentLocation);
        }
        progressDialog.setTitle(R.string.writing_settings);
        progressDialog.setMessage(getString(R.string.please_wait));
        edit.commit(new SettingCallback() {
            @Override
            public void onSuccess(Object o) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        BeaconData beaconData = new BeaconData( configurableDevice.deviceId.toString(),
                                                                "not available",
                                                                "not available",
                                currentLocation.getLatitude() + ", " + currentLocation.getLongitude());
                        FirebaseUtil.saveBeaconData(beaconData);
                        displaySuccess();
                    }
                });
            }

            @Override
            public void onFailure(DeviceConnectionException e) {
                final DeviceConnectionException eF = e;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        displayError(eF);
                    }
                });
            }
        });
    }

    private void displaySuccess() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Success");
        builder.setCancelable(true);
        builder.setPositiveButton(
                "Add Notification",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(ConfigureBeaconActivity.this, NotificationActivity.class);
                        //toDO: Beacon bilgisini bundle la taşı
                        startActivity(i);
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void displayError(DeviceConnectionException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(e.getLocalizedMessage());
        builder.setCancelable(true);
        builder.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            currentLocation = location;
            final Location locationF = location;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    geolocationValuesTextView.setText("" + locationF.getLatitude()
                            + ", " + locationF.getLongitude()
                            + " ±" + locationF.getAccuracy());
                }
            });
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            if (true) {
                //
            }
        }

        @Override
        public void onProviderEnabled(String s) {
            if (true) {
                //
            }
        }

        @Override
        public void onProviderDisabled(String s) {
            if (true) {
                //
            }
        }
    };

    public static String getTag() {
        return tag;
    }

    public static String getBeaconUuid() {
        return beaconUuid;
    }
}