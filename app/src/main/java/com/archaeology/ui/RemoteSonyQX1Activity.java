// Connect to Sony QX1 Wifi camera
// @author: Christopher Besser, msenol86, ygowda
package com.archaeology.ui;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.android.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;
import com.android.volley.toolbox.Volley;
import com.archaeology.R;
import com.archaeology.services.VolleyWrapper;
import com.archaeology.models.JSONObjectResponseWrapper;
import com.archaeology.util.StateStatic;
import static com.archaeology.util.StateStatic.cameraIPAddress;
public class RemoteSonyQX1Activity extends RemoteSonyCameraActivity
{
    /**
     * Launch activity
     * @param savedInstanceState - state from memory
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_camera);
        if (StateStatic.cameraIPAddress == null)
        {
            Toast.makeText(this, "Not Connected to Camera", Toast.LENGTH_SHORT).show();
            finish();
        }
        queue = Volley.newRequestQueue(this);
        requestID = 55;
        // setting up intent filter
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        disableAPIButtons();
        // Enable the API if the camera needs it enabled
        String URL = "http://" + cameraIPAddress + ":8080/sony/camera";
        initiateStartLiveView(URL);
    }

    /**
     * Start long series of API calls to start live view
     * @param URL - camera URL
     */
    @Override
    protected void initiateStartLiveView(String URL)
    {
        try
        {
            // Check supported API
            VolleyWrapper.getAPIList(URL, queue, requestID++, new JSONObjectResponseWrapper() {
                /**
                 * Response received
                 * @param response - camera response
                 */
                @Override
                public void responseMethod(JSONObject response)
                {
                    Log.v("API", response.toString());
                    if (response.toString().contains("getCameraFunction"))
                    {
                        getCameraFunction(URL);
                    }
                    if (response.toString().contains("startRecMode"))
                    {
                        startRecMode(URL);
                        // Wait for the API to update
                        try
                        {
                            Thread.sleep(5000);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                    if (response.toString().contains("getSupportedLiveviewSize"))
                    {
                        getSupportedLiveViewSizes(URL);
                    }
                    if (response.toString().contains("getPostviewImageSize"))
                    {
                        getPostViewImageSize(URL);
                    }
                }

                /**
                 * Connection failed
                 * @param error - failure
                 */
                @Override
                public void errorMethod(VolleyError error)
                {
                    Log.v("Camera Error", error.toString());
                }
            });
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Take a picture
     * @param view - camera view
     */
    @Override
    public void takePhoto(View view)
    {
        String URL = "http://" + cameraIPAddress + ":8080/sony/camera";
        try
        {
            VolleyWrapper.makeVolleySonyAPITakePhotoRequest(URL, queue, requestID++, new JSONObjectResponseWrapper() {
                /**
                 * Response received
                 * @param response - camera response
                 */
                @Override
                public void responseMethod(JSONObject response)
                {
                    try
                    {
                        Log.v("Camera take picture", response.toString());
                        // creating image URL from response
                        String imageURI = response.getJSONArray("result").getString(0);
                        imageURI = imageURI.substring(2, imageURI.length() - 2).replace("\\", "");
                        stopLiveView(findViewById(R.id.stop_live_view_button));
                        downloadFile(imageURI);
                    }
                    catch (JSONException e)
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
                    Log.v("Camera Error", error.toString());
                    Toast.makeText(getApplicationContext(), "Camera error", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show();
        }
    }
}