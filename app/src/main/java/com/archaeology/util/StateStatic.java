// Static stuff
// @author: Christopher Besser and msenol
package com.archaeology.util;
import android.bluetooth.BluetoothAdapter;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class StateStatic
{
    // This class holds global state variables. This class should be only used in a static way
    public static final String LOG_TAG_BLUETOOTH = "BLUETOOTH";
    public static final int REQUEST_IMAGE_CAPTURE = 301;
    public static final int REQUEST_REMOTE_IMAGE = 302;
    public static final int MESSAGE_WEIGHT = 501;
    public static final int MESSAGE_STATUS_CHANGE = 502;
    public static final String DEFAULT_WEB_SERVER_URL = "https://object-data-collector-service.herokuapp.com";
    // Peter's camera
    public static final String SONY_QX1_MAC_ADDRESS = "fe:c2:de:31:0a:e1";
    // Library's alpha 7
    public static final String SONY_ALPHA_7_MAC_ADDRESS = "b2:72:bf:cd:74:61";
    // 30 minutes
    public static final long DEFAULT_CALIBRATION_INTERVAL = 1800000;
    private static final String DEFAULT_PHOTO_PATH = "Archaeology";
    public static final String THUMBNAIL_EXTENSION_STRING = "thumb.jpg";
    public static final int DEFAULT_VOLLEY_TIMEOUT = 7000;
    public static final String MARKED_AS_ADDED = "A";
    public static final String MARKED_AS_TO_DOWNLOAD = "D";
    public static final String HEMISPHERE = "hemisphere";
    public static final String ZONE = "zone";
    public static final String EASTING = "easting";
    public static final String NORTHING = "northing";
    public static final String FIND_NUMBER = "find_number";
    public static final String ALL_FIND_NUMBER = "all_available_find_number";
    public static String deviceName = "nutriscale_1910";
    public static String globalWebServerURL = DEFAULT_WEB_SERVER_URL;
    public static String cameraMACAddress = SONY_QX1_MAC_ADDRESS;
    public static long remoteCameraCalibrationInterval = DEFAULT_CALIBRATION_INTERVAL;
    public static long tabletCameraCalibrationInterval = DEFAULT_CALIBRATION_INTERVAL;
    public static final int DEFAULT_SELECTED_CAMERA_POSITION = 1;
    public static int selectedCameraPosition = DEFAULT_SELECTED_CAMERA_POSITION;
    public static String DEFAULT_SELECTED_CAMERA = "Sony QX1";
    public static String selectedCameraName = DEFAULT_SELECTED_CAMERA;
    public static String cameraIPAddress = null;
    public static final boolean DEFAULT_CORRECTION_SELECTION = true;
    public static boolean colorCorrectionEnabled = DEFAULT_CORRECTION_SELECTION;
    /**
     * Set MAC address
     * @param globalCameraMAC - new MAC address
     */
    public static void setGlobalCameraMAC(String globalCameraMAC)
    {
        cameraMACAddress = globalCameraMAC;
        cameraIPAddress = CheatSheet.findIPFromMAC(cameraMACAddress);
    }

    /**
     * Get photo save location
     * @return Returns photo save location
     */
    public static String getGlobalPhotoSavePath()
    {
        return DEFAULT_PHOTO_PATH;
    }

    /**
     * Is Bluetooth on?
     * @return Returns if Bluetooth is on
     */
    public static boolean isBluetoothEnabled()
    {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    /**
     * Convert dp to px
     * @param dp - pixel
     * @return - return px of dp
     */
    public static float convertDPToPixel(float dp)
    {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    /**
     * Convert dp to px
     * @param dp - pixel
     * @return Returns px of dp
     */
    public static int convertDPToPixel(int dp)
    {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        return dp * (metrics.densityDpi / 160);
    }

    /**
     * Get current date
     * @return Returns timestamp
     */
    public static String getTimeStamp()
    {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
    }
}