// Object information
// @author: msenol86, ygowda
package com.archaeology.ui;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import com.archaeology.R;
import com.archaeology.models.StringObjectResponseWrapper;
import com.archaeology.services.BluetoothService;
import com.archaeology.services.NutriScaleBroadcastReceiver;
import com.archaeology.util.CheatSheet;
import com.archaeology.util.StateStatic;
import static com.archaeology.services.VolleyStringWrapper.makeVolleyStringObjectRequest;
import static com.archaeology.util.CheatSheet.deleteOriginalAndThumbnailPhoto;
import static com.archaeology.util.CheatSheet.getOutputMediaFile;
import static com.archaeology.util.CheatSheet.goToSettings;
import static com.archaeology.util.StateStatic.ALL_FIND_NUMBER;
import static com.archaeology.util.StateStatic.LOG_TAG_BLUETOOTH;
import static com.archaeology.util.StateStatic.MARKED_AS_ADDED;
import static com.archaeology.util.StateStatic.MARKED_AS_TO_DOWNLOAD;
import static com.archaeology.util.StateStatic.MESSAGE_STATUS_CHANGE;
import static com.archaeology.util.StateStatic.MESSAGE_WEIGHT;
import static com.archaeology.util.StateStatic.REQUEST_ENABLE_BT;
import static com.archaeology.util.StateStatic.REQUEST_IMAGE_CAPTURE;
import static com.archaeology.util.StateStatic.REQUEST_REMOTE_IMAGE;
import static com.archaeology.util.StateStatic.cameraIPAddress;
import static com.archaeology.util.StateStatic.getGlobalCameraMAC;
import static com.archaeology.util.StateStatic.getGlobalPhotoSavePath;
import static com.archaeology.util.StateStatic.getGlobalWebServerURL;
import static com.archaeology.util.StateStatic.getTimeStamp;
import static com.archaeology.util.StateStatic.isBluetoothEnabled;
import static com.archaeology.util.StateStatic.isRemoteCameraSelected;
public class ObjectDetailActivity extends AppCompatActivity
{
    IntentFilter mIntentFilter;
    // to handle python requests
    RequestQueue queue;
    // handler used to process messages. will either set weight or inform of changes in bluetooth connection status
    Handler handler = new Handler() {
        /**
         * Message received
         * @param msg - message
         */
        @Override
        public void handleMessage(Message msg)
        {
            if (msg.what == MESSAGE_WEIGHT)
            {
                setCurrentScaleWeight(Integer.parseInt(msg.obj.toString().trim()) + "");
            }
            else if (msg.what == MESSAGE_STATUS_CHANGE)
            {
                setBluetoothConnectionStatus(msg.obj.toString().trim());
            }
        }
    };
    private String currentScaleWeight = "";
    private String bluetoothConnectionStatus = "";
    private Uri fileURI;
    boolean dialogVisible = false;
    // dialogs set up in order to provide interface to interact with other devices
    AlertDialog weightDialog, pickPeersDialog;
    // broadcast receiver objects used to receive messages from other devices
    BroadcastReceiver nutriScaleBroadcastReceiver;
    // correspond to columns in database associated with finds
    int easting, northing, findNumber, imageNumber;
    public BluetoothService bluetoothService;
    public BluetoothDevice device = null;
    /**
     * Launch the activity
     * @param savedInstanceState - activity from memory
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_detail);
        if (bluetoothService != null)
        {
            bluetoothService.closeThread();
        }
        bluetoothService = null;
        device = null;
        mIntentFilter = new IntentFilter();
        queue = Volley.newRequestQueue(this);
        // getting object data from previous activity
        Bundle myBundle = getIntent().getExtras();
        easting = Integer.parseInt(myBundle.getString("easting"));
        northing = Integer.parseInt(myBundle.getString("northing"));
        findNumber = Integer.parseInt(myBundle.getString("find_number"));
        // adding info about object to text field in view
        fillFindInfo(easting + "", northing + "");
        fillFindNumberSpinner();
        ((Spinner) findViewById(R.id.find_spinner)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /**
             * User selected item
             * @param parent - spinner
             * @param view - item selected
             * @param position - item position
             * @param id - item id
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                // if the item you selected from the spinner has a different find number than the
                // item returned from the last intent then cancel the request.
                String x = ((Spinner) findViewById(R.id.find_spinner)).getItemAtPosition(position).toString();
                int tmpFindNumber = Integer.parseInt(x);
                if (findNumber != tmpFindNumber)
                {
                    findNumber = tmpFindNumber;
                    queue.cancelAll(new RequestQueue.RequestFilter() {
                        /**
                         * Cancel all requests
                         * @param request - request to cancel requests
                         * @return Returns true
                         */
                        @Override
                        public boolean apply(Request<?> request)
                        {
                            return true;
                        }
                    });
                    asyncPopulateFieldsFromDB(easting, northing, findNumber);
                    clearCurrentPhotosOnLayoutAndFetchPhotosAsync();
                    fillFindNumberSpinner();
                }
            }

            /**
             * Nothing selected
             * @param parent - spinner
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });
        // populate fields with information about object
        asyncPopulateFieldsFromDB(easting, northing, findNumber);
        asyncPopulatePhotos();
//        toggleAddPhotoButton();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // check to see if bluetooth is enabled
        if (mBluetoothAdapter == null)
        {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        }
        else if (!isBluetoothEnabled())
        {
            Toast.makeText(this, "Bluetooth disabled in settings", Toast.LENGTH_SHORT).show();
        }
        else
        {
            // checking once again if not enabled. if it is not then enable it
            if (!mBluetoothAdapter.isEnabled())
            {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
            }
            nutriScaleBroadcastReceiver = new NutriScaleBroadcastReceiver(handler);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        // Inflate and set the layout for the weight dialog. Pass null as the parent view because
        // its going in the dialog layout save or cancel weight data
        builder.setView(inflater.inflate(R.layout.record_weight_dialog, null))
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            /**
             * User clicked Cancel
             * @param dialog - alert dialog
             * @param id - selection
             */
            public void onClick(DialogInterface dialog, int id)
            {
                dialogVisible = false;
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            /**
             * Action cancelled
             * @param dialog - dialog window
             */
            @Override
            public void onCancel(DialogInterface dialog)
            {
                dialogVisible = false;
            }
        });
        try
        {
            Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
            if (pairedDevices.size() > 0)
            {
                // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice pairedDv: pairedDevices)
                {
                    String deviceName = pairedDv.getName();
                    if (deviceName.equals(StateStatic.deviceName))
                    {
                        device = pairedDv;
                        bluetoothService = new BluetoothService(this);
                        bluetoothService.reconnect(device);
                    }
                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Phone does not support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        weightDialog = builder.create();
        // creating the camera dialog and setting up photo fragment to store the photos
//        remoteCameraDialog = CameraDialog.createCameraDialog(this);
        PhotoFragment photoFragment = (PhotoFragment) getFragmentManager().findFragmentById(R.id.fragment);
        if (savedInstanceState == null)
        {
            photoFragment.prepareFragmentForNewPhotosFromNewItem();
        }
        cameraIPAddress = CheatSheet.findIPFromMAC(getGlobalCameraMAC());
        if (cameraIPAddress != null)
        {
            ((TextView) findViewById(R.id.connectToCameraText)).setText(getString(R.string.ip_connection, cameraIPAddress));
        }
    }

    /**
     * Populate action overflow
     * @param menu - overflow actions
     * @return Returns true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_object_detail, menu);
        return true;
    }

    /**
     * Overflow action selected
     * @param item - selected action
     * @return Returns whether the action succeeded
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                goToSettings(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Activity finished
     * @param requestCode - data request code
     * @param resultCode - result code
     * @param data - result of activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        String captureFile = fileURI.toString();
        final Uri FILE_URI;
        final String ORIGINAL_FILE_NAME = captureFile.substring(captureFile.lastIndexOf('/') + 1);
        // action to be performed when request is sent to take photo
        if (requestCode == REQUEST_IMAGE_CAPTURE)
        {
            // creating URI to save photo to once taken
            FILE_URI = CheatSheet.getThumbnail(ORIGINAL_FILE_NAME);
        }
        else
        {
            FILE_URI = Uri.parse(data.getStringExtra("location"));
        }
        if (resultCode == RESULT_OK)
        {
            // ApproveDialogCallback is an interface. see CameraDialog class
            CameraDialog.ApproveDialogCallback approveDialogCallback = new CameraDialog.ApproveDialogCallback() {
                /**
                 * User pressed save
                 */
                @Override
                public void onSaveButtonClicked()
                {
                    // store image data into photo fragments
                    loadPhotoIntoPhotoFragment(FILE_URI, MARKED_AS_ADDED);
                }

                /**
                 * User cancelled picture
                 */
                @Override
                public void onCancelButtonClicked()
                {
                    deleteOriginalAndThumbnailPhoto(FILE_URI);
                }
            };
            // set up camera dialog
            AlertDialog approveDialog = CameraDialog.createPhotoApprovalDialog(this, approveDialogCallback);
            approveDialog.show();
            // view photo you are trying to approve
            ImageView approvePhotoImage = (ImageView) approveDialog.findViewById(R.id.approvePhotoImage);
            approvePhotoImage.setImageURI(FILE_URI);
        }
    }

    /**
     * Breaks connections with nutriscale and connections with camera and other external devices
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (pickPeersDialog != null)
        {
            pickPeersDialog.dismiss();
        }
        try
        {
            unregisterReceiver(nutriScaleBroadcastReceiver);
        }
        catch (IllegalArgumentException e)
        {
            Log.v(LOG_TAG_BLUETOOTH, "Trying to unregister non-registered receiver");
        }
    }

    /**
     * Switch activity into context
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(nutriScaleBroadcastReceiver, mIntentFilter);
    }

    /**
     * Break connections with other devices
     */
    @Override
    protected void onStop()
    {
        super.onStop();
        if (pickPeersDialog != null)
        {
            pickPeersDialog.dismiss();
        }
        try
        {
            unregisterReceiver(nutriScaleBroadcastReceiver);
        }
        catch (IllegalArgumentException e)
        {
            Log.v(LOG_TAG_BLUETOOTH, "Trying to unregister non-registered receiver");
        }
    }

    /**
     * App is paused
     */
    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            unregisterReceiver(nutriScaleBroadcastReceiver);
        }
        catch (IllegalArgumentException e)
        {
            Log.v(LOG_TAG_BLUETOOTH, "Trying to unregister non-registered receiver");
        }
    }

    /**
     * Updates database with changes in weight data for object
     * @param weightInGrams - weight from scale
     * @param easting - find easting
     * @param northing - find northing
     * @param findNumber - find
     */
    public void asyncModifyWeightFieldInDB(double weightInGrams, int easting, int northing, int findNumber)
    {
        double weightInKg = weightInGrams / 1000.0;
        // making Python request to call the update method with updated params
        makeVolleyStringObjectRequest(getGlobalWebServerURL() + "/set_weight?easting="
                        + easting + "&northing=" + northing + "&find=" + findNumber
                        + "&weight=" + weightInKg, queue,
                new StringObjectResponseWrapper(this) {
            /**
             * Response received
             * @param response - database response
             */
            @Override
            public void responseMethod(String response)
            {
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                Log.v("Weight", response);
            }

            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void errorMethod(VolleyError error)
            {
                error.printStackTrace();
            }
        });
    }

    /**
     * Populates exterior color fields for finds with Munsell color values
     * @param easting - find easting
     * @param northing - find northing
     * @param findNumber - find
     */
    public void asyncPopulateFieldsFromDB(int easting, int northing, int findNumber)
    {
        makeVolleyStringObjectRequest(getGlobalWebServerURL() + "/get_find_colors/?easting=" +
                        easting + "&northing=" + northing + "&find=" + findNumber +
                        "&location=exterior", queue, new StringObjectResponseWrapper(this) {
            /**
             * Response received
             * @param response - database response
             */
            @Override
            public void responseMethod(String response)
            {
                try
                {
                    // Response Schema: munsell_hue_number, munsell_hue_letter, munsell_lightness_value,
                    // munsell_chroma, rgb_red_256_bit, rgb_green_256_bit, rgb_blue_256_bit
                    String[] obj = response.split("\n")[1].split(" \\| ");
                    populateExteriorColorFields(obj[0] + obj[1], obj[2], obj[3]);
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    e.printStackTrace();
                }
            }

            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void errorMethod(VolleyError error)
            {
                error.printStackTrace();
            }
        });
        makeVolleyStringObjectRequest(getGlobalWebServerURL() + "/get_find_colors/?easting=" +
                easting + "&northing=" + northing + "&find=" + findNumber +
                "&location=interior", queue, new StringObjectResponseWrapper(this) {
            /**
             * Response received
             * @param response - database response
             */
            @Override
            public void responseMethod(String response)
            {
                try
                {
                    // Response Schema: munsell_hue_number, munsell_hue_letter, munsell_lightness_value,
                    // munsell_chroma, rgb_red_256_bit, rgb_green_256_bit, rgb_blue_256_bit
                    String[] obj = response.split("\n")[1].split(" \\| ");
                    populateInteriorColorFields(obj[0] + obj[1], obj[2], obj[3]);
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    e.printStackTrace();
                }
            }

            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void errorMethod(VolleyError error)
            {
                error.printStackTrace();
            }
        });
        makeVolleyStringObjectRequest(getGlobalWebServerURL() + "/get_find/?easting=" +
                easting + "&northing=" + northing + "&find=" + findNumber, queue,
                new StringObjectResponseWrapper(this) {
            /**
             * Response received
             * @param response - database response
             */
            @Override
            public void responseMethod(String response)
            {
                try
                {
                    // Response Schema: longitude_decimal_degrees, latitude_decimal_degrees,
                    // utm_easting_meters, utm_northing_meters, material_general, material_specific,
                    // category_general, category_specific,  weight_kilograms
                    String[] obj = response.split("\n")[1].split(" \\| ");
                    if (obj[8].equals("null") || obj[8].equals(""))
                    {
                        getWeightInputText().setText(getString(R.string.nil));
                    }
                    else
                    {
                        BluetoothService.currWeight = (int) (1000 * Double.parseDouble(obj[8]));
                        getWeightInputText().setText(String.valueOf(BluetoothService.currWeight));
                    }
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    e.printStackTrace();
                }
            }

            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void errorMethod(VolleyError error)
            {
                error.printStackTrace();
            }
        });
    }

    /**
     * Get images from response and store so that they can be viewed later
     */
    public void asyncPopulatePhotos()
    {
        String URL = getGlobalWebServerURL() + "/get_image_urls/?easting=" + easting +
                "&northing=" + northing + "&find=" + findNumber;
        makeVolleyStringObjectRequest(URL, queue, new StringObjectResponseWrapper(this) {
            /**
             * Database response
             * @param response - response received
             */
            @Override
            public void responseMethod(String response)
            {
                // get images from response array
                ArrayList<String> photoList = CheatSheet.convertImageLinkListToArray(response);
                for (String photoURL: photoList)
                {
                    loadPhotoIntoPhotoFragment(Uri.parse(photoURL), MARKED_AS_TO_DOWNLOAD);
                }
            }

            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void errorMethod(VolleyError error)
            {
                error.printStackTrace();
            }
        });
    }

    /**
     * Populate text fields with interior color
     * @param hue - image hue
     * @param lightness - image lightness
     * @param chroma - image chroma
     */
    public void populateInteriorColorFields(String hue, String lightness, String chroma)
    {
        ((TextView) findViewById(R.id.interior_color_hue)).setText(hue.trim());
        ((TextView) findViewById(R.id.interior_color_lightness)).setText(lightness.trim());
        ((TextView) findViewById(R.id.interior_color_chroma)).setText(chroma.trim());
    }

    /**
     * Populate text fields with exterior color
     * @param hue - image hue
     * @param lightness - image lightness
     * @param chroma - image chroma
     */
    public void populateExteriorColorFields(String hue, String lightness, String chroma)
    {
        ((TextView) findViewById(R.id.exterior_color_hue)).setText(hue.trim());
        ((TextView) findViewById(R.id.exterior_color_lightness)).setText(lightness.trim());
        ((TextView) findViewById(R.id.exterior_color_chroma)).setText(chroma.trim());
    }

    /**
     * Get weight
     * @return Returns weight
     */
    public TextView getWeightInputText()
    {
        return (TextView) findViewById(R.id.weightInput);
    }

    /**
     * Set scale weight
     * @param currentScaleWeight - reset weight
     */
    public void setCurrentScaleWeight(String currentScaleWeight)
    {
        this.currentScaleWeight = currentScaleWeight;
        if (dialogVisible)
        {
            ((EditText) weightDialog.findViewById(R.id.dialogCurrentWeightInDBText)).setText(currentScaleWeight.trim());
        }
    }

    /**
     * Change bluetooth connection
     * @param bluetoothConnectionStatus - new connection status
     */
    public void setBluetoothConnectionStatus(String bluetoothConnectionStatus)
    {
        this.bluetoothConnectionStatus = bluetoothConnectionStatus;
        if (dialogVisible)
        {
            ((TextView) weightDialog.findViewById(R.id.btConnectionStatusText)).setText(bluetoothConnectionStatus);
        }
    }

    /**
     * Save the weight
     * @param weight - weight of object
     */
    public void saveWeight(String weight)
    {
        try
        {
            ((TextView) findViewById(R.id.weightInput)).setText(weight);
            asyncModifyWeightFieldInDB(Double.parseDouble(getWeightInputText().getText().toString()),
                    easting, northing, findNumber);
        }
        catch (NumberFormatException e)
        {
            Toast.makeText(getApplicationContext(), "Invalid Weight", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Makes weight dialog visible and add functionality to buttons
     * @param view - weight dialog
     */
    public void startRecordWeight(View view)
    {
        weightDialog.show();
        dialogVisible = true;
        // set up buttons on record_weight_dialog.xml so that you can view and save weight information
        weightDialog.findViewById(R.id.dialogSaveWeightButton).setOnClickListener(new View.OnClickListener() {
            /**
             * User clicked save
             * @param v - dialog
             */
            @Override
            public void onClick(View v)
            {
                saveWeight((((EditText) weightDialog.findViewById(R.id.dialogCurrentWeightInDBText))
                        .getText().toString().trim()));
                weightDialog.dismiss();
            }
        });
        weightDialog.findViewById(R.id.update_bluetooth).setOnClickListener(new View.OnClickListener() {
            /**
             * User clicked copy weight
             * @param v - dialog
             */
            @Override
            public void onClick(View v)
            {
                try
                {
                    runBluetooth();
                    weightDialog.dismiss();
                }
                catch (NullPointerException e)
                {
                    e.printStackTrace();
                }
            }
        });
        ((EditText) weightDialog.findViewById(R.id.dialogCurrentWeightInDBText)).setText(getCurrentScaleWeight());
        ((TextView) weightDialog.findViewById(R.id.btConnectionStatusText)).setText(getBluetoothConnectionStatus());
    }

    /**
     * Connect to Bluetooth
     */
    public void runBluetooth()
    {
        if (bluetoothService == null)
        {
            Toast.makeText(getApplicationContext(), "Not connected to scale", Toast.LENGTH_SHORT).show();
            return;
        }
        bluetoothService.runService();
        ((TextView) findViewById(R.id.weightInput)).setText(String.valueOf(BluetoothService.currWeight));
        asyncModifyWeightFieldInDB(BluetoothService.currWeight, easting, northing, findNumber);
    }

    /**
     * Called from add photo button. shows remoteCameraDialog, which is used to open camera view
     * and take picture
     * @param view - add photo button
     */
    public void addPhotoAction(View view)
    {
        imageNumber++;
        if (isRemoteCameraSelected())
        {
            // Just connect to found IP
            cameraIPAddress = CheatSheet.findIPFromMAC(getGlobalCameraMAC());
            goToWiFiActivity();
        }
        else
        {
            startLocalCameraIntent();
        }
    }

//    /**
//     * Open camera dialog
//     * @param VIEW - camera view
//     */
//    public void showRemoteCameraDialog(final View VIEW)
//    {
//        remoteCameraDialog.show();
//        remoteCameraDialog.getWindow().setLayout(convertDPToPixel(700), WindowManager.LayoutParams.WRAP_CONTENT);
//        final Activity PARENT_ACTIVITY = this;
//        remoteCameraDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            /**
//             * User cancelled camera
//             * @param dialog - alert window
//             */
//            @Override
//            public void onCancel(DialogInterface dialog)
//            {
//                CameraDialog.stopLiveView(PARENT_ACTIVITY, queue, sonyAPIRequestID++, getLiveViewSurface());
//            }
//        });
//        remoteCameraDialog.findViewById(R.id.take_photo).setOnClickListener(new View.OnClickListener() {
//            /**
//             * User clicked take photo
//             * @param v - camera view
//             */
//            @Override
//            public void onClick(View v)
//            {
//                if (!isTakePhotoButtonClicked)
//                {
//                    isTakePhotoButtonClicked = true;
//                    remoteCameraDialog.findViewById(R.id.take_photo).setEnabled(false);
//                    CameraDialog.takePhoto(PARENT_ACTIVITY, queue, sonyAPIRequestID++,
//                            getTimeStamp(), new AfterImageSavedMethodWrapper() {
//                        /**
//                         * Process saved image
//                         * @param THUMBNAIL_IMAGE_URL - image URI
//                         */
//                        @Override
//                        public void doStuffWithSavedImage(final Uri THUMBNAIL_IMAGE_URL)
//                        {
//                            final Uri ORIGINAL_IMAGE_URI = CheatSheet.getOriginalImageURI(THUMBNAIL_IMAGE_URL);
//                            // implementing interface from CameraDialog class
//                            CameraDialog.ApproveDialogCallback approveDialogCallback =
//                                    new CameraDialog.ApproveDialogCallback() {
//                                /**
//                                 * Save button pressed
//                                 */
//                                @Override
//                                public void onSaveButtonClicked()
//                                {
//                                    // take picture and add as photo fragment
//                                    loadPhotoIntoPhotoFragment(ORIGINAL_IMAGE_URI, MARKED_AS_ADDED);
//                                    isTakePhotoButtonClicked = false;
//                                    remoteCameraDialog.findViewById(R.id.take_photo).setEnabled(true);
//                                }
//
//                                /**
//                                 * Cancel picture
//                                 */
//                                @Override
//                                public void onCancelButtonClicked()
//                                {
//                                    deleteOriginalAndThumbnailPhoto(ORIGINAL_IMAGE_URI);
//                                    isTakePhotoButtonClicked = false;
//                                    remoteCameraDialog.findViewById(R.id.take_photo).setEnabled(true);
//                                }
//                            };
//                            // create dialog to view and approve photo
//                            AlertDialog approveDialog = CameraDialog.createPhotoApprovalDialog(PARENT_ACTIVITY,
//                                    approveDialogCallback);
//                            approveDialog.show();
//                            ImageView approvePhotoImage = (ImageView) approveDialog.findViewById(R.id.approvePhotoImage);
//                            approvePhotoImage.setImageURI(THUMBNAIL_IMAGE_URL);
//                        }
//                    }, getLiveViewSurface());
//                }
//            }
//        });
//        remoteCameraDialog.findViewById(R.id.zoom_in).setOnClickListener(new View.OnClickListener() {
//            /**
//             * User pressed zoom in
//             * @param v - camera view
//             */
//            @Override
//            public void onClick(View v)
//            {
//                CameraDialog.zoomIn(PARENT_ACTIVITY, queue, sonyAPIRequestID++);
//            }
//        });
//        remoteCameraDialog.findViewById(R.id.zoom_out).setOnClickListener(new View.OnClickListener() {
//            /**
//             * User pressed zoom out
//             * @param v - camera view
//             */
//            @Override
//            public void onClick(View v)
//            {
//                CameraDialog.zoomOut(PARENT_ACTIVITY, queue, sonyAPIRequestID++);
//            }
//        });
//        // should allow you to see what the camera is seeing
//        CameraDialog.startLiveView(this, queue, sonyAPIRequestID++, getLiveViewSurface());
//    }

    /**
     * Starts the local camera
     */
    public void startLocalCameraIntent()
    {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String stamp = getTimeStamp();
        // create a file to save the image
        Context context = getApplicationContext();
        fileURI = FileProvider.getUriForFile(context, context.getPackageName()
                + ".my.package.name.provider", getOutputMediaFile(stamp));
        photoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileURI);
        photoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(photoIntent, REQUEST_IMAGE_CAPTURE);
    }

//    /**
//     * Allows you to see what camera is seeing. used in remote_camera_layout.xml and
//     * activity_my_wifi.xml
//     * @return Returns the camera view
//     */
//    public SimpleStreamSurfaceView getLiveViewSurface()
//    {
//        return (SimpleStreamSurfaceView) remoteCameraDialog.findViewById(R.id.surfaceview_liveview);
//    }

    /**
     * Read from scale
     * @return Returns weight
     */
    public String getCurrentScaleWeight()
    {
        return currentScaleWeight;
    }

    /**
     * Check bluetooth status
     * @return Returns connection status
     */
    public String getBluetoothConnectionStatus()
    {
        return bluetoothConnectionStatus;
    }

    /**
     * Fill photo fragment
     * @param imageURI - image location
     * @param SYNC_STATUS - how the camera is syncing
     */
    public void loadPhotoIntoPhotoFragment(Uri imageURI, final String SYNC_STATUS)
    {
        // loading PhotoFragment class to add photo URIs
        PhotoFragment photoFragment = (PhotoFragment) getFragmentManager().findFragmentById(R.id.fragment);
        if (photoFragment != null)
        {
            // photo URIs are added to hashmap in PhotoFragment class
            photoFragment.addPhoto(imageURI, SYNC_STATUS);
        }
    }

    /**
     * Display find number of items in spinner
     */
    public void fillFindNumberSpinner()
    {
        Bundle myBundle = getIntent().getExtras();
        String[] findNumbers = myBundle.getStringArray(ALL_FIND_NUMBER);
        Spinner findSpinner = findViewById(R.id.find_spinner);
        CheatSheet.setSpinnerItems(this, findSpinner, Arrays.asList(findNumbers),
                findNumber + "", R.layout.spinner_item);
    }

    /**
     * Populate text fields with info about find
     * @param easting - find easting
     * @param northing - find northing
     */
    public void fillFindInfo(String easting, String northing)
    {
        ((TextView) findViewById(R.id.findInfo)).setText(getString(R.string.find_frmt, easting, northing));
    }

    /**
     * Clear current photos and populate with photos from database
     */
    public void clearCurrentPhotosOnLayoutAndFetchPhotosAsync()
    {
        PhotoFragment photoFragment = (PhotoFragment) getFragmentManager().findFragmentById(R.id.fragment);
        photoFragment.prepareFragmentForNewPhotosFromNewItem();
        asyncPopulatePhotos();
    }

    /**
     * Navigate through items in spinner
     * @param view - button
     */
    public void goToNextItemIfAvailable(View view)
    {
        Spinner find = findViewById(R.id.find_spinner);
        int selectedItemPos = find.getSelectedItemPosition();
        int itemCount = find.getCount();
        if (selectedItemPos + 1 <= itemCount - 1)
        {
            find.setSelection(selectedItemPos + 1);
        }
    }

    /**
     * Navigate through items in spinner
     * @param view - button
     */
    public void goToPreviousItemIfAvailable(View view)
    {
        Spinner find = findViewById(R.id.find_spinner);
        int selectedItemPos = find.getSelectedItemPosition();
        if (selectedItemPos - 1 >= 0)
        {
            find.setSelection(selectedItemPos - 1);
        }
    }

//    /**
//     * Enable buttons depending on connection with remote camera
//     */
//    public void toggleAddPhotoButton()
//    {
//        if (isRemoteCameraSelected() && cameraIPAddress == null)
//        {
//            findViewById(R.id.button26).setEnabled(false);
//        }
//        else
//        {
//            findViewById(R.id.button26).setEnabled(true);
//        }
//    }

    /**
     * Open gallery
     * @param v - gallery view
     */
    public void goToImageGallery(View v)
    {
        Intent photosActivity = new Intent(this, PhotosActivity.class);
        photosActivity.putExtra("easting", "" + easting);
        photosActivity.putExtra("northing", "" + northing);
        photosActivity.putExtra("find", "" + findNumber);
        photosActivity.putExtra("number", "" + imageNumber);
        startActivity(photosActivity);
    }

    /**
     * Open WiFi
     */
    public void goToWiFiActivity()
    {
        if (cameraIPAddress == null)
        {
            Toast.makeText(getApplicationContext(), "Not connected to camera", Toast.LENGTH_LONG).show();
            return;
        }
        try
        {
            unregisterReceiver(nutriScaleBroadcastReceiver);
        }
        catch (IllegalArgumentException e)
        {
            Log.v(LOG_TAG_BLUETOOTH, "Trying to unregister non-registered receiver");
        }
        Intent wifiActivity = new Intent(this, MyWiFiActivity.class);
        startActivityForResult(wifiActivity, REQUEST_REMOTE_IMAGE);
    }
}