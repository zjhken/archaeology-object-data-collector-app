// URL communication wrapper
// @author: msenol
package com.archaeology.services;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.archaeology.models.ImageResponseWrapper;
import com.archaeology.models.JSONArrayResponseWrapper;
import com.archaeology.models.JSONObjectResponseWrapper;
import static com.android.volley.Request.Method;
import static com.archaeology.util.StateStatic.DEFAULT_VOLLEY_TIMEOUT;
import static com.archaeology.util.StateStatic.LOG_TAG;
import static com.archaeology.util.StateStatic.LOG_TAG_WIFI_DIRECT;
public class VolleyWrapper
{
    /**
     * get the available APIs to be used for camera
     * @param URL - camera URL
     * @param queue - request queue
     * @param id - request id
     * @param lambdaWrapper - request wrapper
     * @throws JSONException if response is malformed
     */
    public static void makeVolleySonyAPIGetAPICommands(final String URL, RequestQueue queue,
                                                       final int id,
                                                       final JSONObjectResponseWrapper lambdaWrapper)
            throws JSONException
    {
        final String postBody = new JSONObject().put("method", "getAvailableApiList")
                .put("params", new JSONArray()).put("id", id)
                .put("version", "1.0").toString();
        JSONObject jsonPostBody = new JSONObject(postBody);
        Log.v(LOG_TAG_WIFI_DIRECT, "request body: " +  postBody);
        JsonObjectRequest myRequest = new JsonObjectRequest(Method.POST, URL, jsonPostBody,
                new Response.Listener<JSONObject>() {
            /**
             * Response received
             * @param response - database response
             */
            @Override
            public void onResponse(JSONObject response)
            {
                lambdaWrapper.responseMethod(response);
            }
        }, new Response.ErrorListener() {
            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void onErrorResponse(VolleyError error)
            {
                lambdaWrapper.errorMethod(error);
            }
        });
        myRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(myRequest);
    }

    /**
     * Send photo request
     * @param URL - camera URL
     * @param queue - request queue
     * @param id - request id
     * @param lambdaWrapper - request wrapper
     * @throws JSONException if the JSON is malformed
     */
    public static void makeVolleySonyAPITakePhotoRequest(final String URL, RequestQueue queue,
                                                         final int id,
                                                         final JSONObjectResponseWrapper lambdaWrapper)
            throws JSONException
    {
        final String postBody = new JSONObject().put("method",
                "actTakePicture").put("params", new JSONArray())
                .put("id", id).put("version", "1.0").toString();
        JSONObject jsonPostBody = new JSONObject(postBody);
        Log.v(LOG_TAG_WIFI_DIRECT, "request body: " +  postBody);
        JsonObjectRequest myRequest = new JsonObjectRequest(Method.POST, URL, jsonPostBody,
                new Response.Listener<JSONObject>() {
            /**
             * Response received
             * @param response - database response
             */
            @Override
            public void onResponse(JSONObject response)
            {
                lambdaWrapper.responseMethod(response);
            }
        }, new Response.ErrorListener() {
            /**
             * Connection error
             * @param error - failure
             */
            @Override
            public void onErrorResponse(VolleyError error)
            {
                lambdaWrapper.errorMethod(error);
            }
        });
        myRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(myRequest);
    }

    /**
     * Request camera feed
     * @param URL - camera URL
     * @param queue - request queue
     * @param id - request id
     * @param lambdaWrapper - request wrapper
     * @throws JSONException if the JSON is malformed
     */
    public static void makeVolleySonyAPIStartLiveViewRequest(final String URL, RequestQueue queue,
                                                             final int id,
                                                             final JSONObjectResponseWrapper lambdaWrapper)
            throws JSONException
    {
        // setting up with params for json object
        final String postBody = new JSONObject().put("method", "startLiveview")
                .put("params", new JSONArray())
                .put("id", id).put("version", "1.0").toString();
        JSONObject jsonPostBody = new JSONObject(postBody);
        Log.v(LOG_TAG_WIFI_DIRECT, "request body: " +  postBody);
        // making request
        JsonObjectRequest myRequest = new JsonObjectRequest(Method.POST, URL, jsonPostBody,
                new Response.Listener<JSONObject>() {
            /**
             * Response received
             * @param response - camera response
             */
            @Override
            public void onResponse(JSONObject response)
            {
                lambdaWrapper.responseMethod(response);
            }
        }, new Response.ErrorListener() {
            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void onErrorResponse(VolleyError error)
            {
                lambdaWrapper.errorMethod(error);
            }
        });
        //setting up retry policy in case of failure
        myRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(myRequest);
    }

    /**
     * Stopping live view usually in case of error or when image has been captured
     * @param URL - camera URL
     * @param queue - request queue
     * @param id - request id
     * @param lambdaWrapper - request wrapper
     */
    public static void makeVolleySonyAPIStopLiveViewRequest(final String URL, RequestQueue queue,
                                                            final int id,
                                                            final JSONObjectResponseWrapper lambdaWrapper)
            throws JSONException
    {
        // adding params for json object
        final String postBody = new JSONObject().put("method", "stopLiveview")
                .put("params", new JSONArray())
                .put("id", id).put("version", "1.0").toString();
        JSONObject jsonPostBody = new JSONObject(postBody);
        Log.v(LOG_TAG_WIFI_DIRECT, "request body: " +  postBody);
        // making request
        JsonObjectRequest myRequest = new JsonObjectRequest(Method.POST, URL, jsonPostBody,
                new Response.Listener<JSONObject>() {
            /**
             * Response received
             * @param response - camera response
             */
            @Override
            public void onResponse(JSONObject response)
            {
                lambdaWrapper.responseMethod(response);
            }
        }, new Response.ErrorListener() {
            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void onErrorResponse(VolleyError error)
            {
                lambdaWrapper.errorMethod(error);
            }
        });
        myRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(myRequest);
    }

    /**
     * Calls actZoom API to the target server. Request JSON data is such like as below.
     * {
     *   "method": "actZoom",
     *   "params": ["in","stop"],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * @param direction - direction of zoom ("in" or "out")
     * @param queue - request queue
     * @param URL - camera URL
     * @param id - request id
     * @param lambdaWrapper - request wrapper
     * @throws JSONException if the JSON is malformed
     */
    public static void makeVolleySonyAPIActZoomRequest(String direction, RequestQueue queue,
                                                       final String URL, final int id,
                                                       final JSONObjectResponseWrapper lambdaWrapper)
            throws JSONException
    {
        final String postBody = new JSONObject().put("method", "actZoom")
                .put("params", new JSONArray().put(direction).put("1shot"))
                .put("id", id).put("version", "1.0").toString();
        JSONObject jsonPostBody = new JSONObject(postBody);
        Log.v(LOG_TAG_WIFI_DIRECT, "request body: " +  postBody);
        JsonObjectRequest myRequest = new JsonObjectRequest(Method.POST, URL, jsonPostBody,
                new Response.Listener<JSONObject>() {
            /**
             * Response received
             * @param response - camera response
             */
            @Override
            public void onResponse(JSONObject response)
            {
                lambdaWrapper.responseMethod(response);
            }
        }, new Response.ErrorListener() {
            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void onErrorResponse(VolleyError error)
            {
                lambdaWrapper.errorMethod(error);
            }
        });
        myRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(myRequest);
    }

    /**
     * Custom camera request
     * @param methodName - function name
     * @param URL - camera URL
     * @param queue - request queue
     * @param id - request id
     * @param lambdaWrapper - request wrapper
     * @throws JSONException if the JSON is malformed
     */
    public static void makeVolleySonyAPICustomFunctionCall(String methodName, final String URL,
                                                           RequestQueue queue, final int id,
                                                           final JSONObjectResponseWrapper lambdaWrapper)
            throws JSONException
    {
        final String postBody = new JSONObject().put("method", methodName)
                .put("params", new JSONArray()).put("id", id)
                .put("version", "1.0").toString();
        JSONObject jsonPostBody = new JSONObject(postBody);
        Log.v(LOG_TAG_WIFI_DIRECT, "request body: " +  postBody);
        JsonObjectRequest myRequest = new JsonObjectRequest(Method.POST, URL, jsonPostBody,
                new Response.Listener<JSONObject>() {
            /**
             * Response received
             * @param response - camera response
             */
            @Override
            public void onResponse(JSONObject response)
            {
                lambdaWrapper.responseMethod(response);
            }
        }, new Response.ErrorListener() {
            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void onErrorResponse(VolleyError error)
            {
                lambdaWrapper.errorMethod(error);
            }
        });
        myRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(myRequest);
    }

    /**
     * setting the image to the original size
     * @param URL - camera URL
     * @param queue - request queue
     * @param id - request id
     * @param lambdaWrapper - request wrapper
     * @throws JSONException if the JSON is malformed
     */
    public static void makeVolleySonyAPISetImageSizeToOriginal(final String URL,
                                                               RequestQueue queue, final int id,
                                                               final JSONObjectResponseWrapper lambdaWrapper)
            throws JSONException
    {
        final String postBody = new JSONObject().put("method", "setPostviewImageSize")
                .put("params", new JSONArray().put("Original")).put("id", id)
                .put("version", "1.0").toString();
        JSONObject jsonPostBody = new JSONObject(postBody);
        Log.v(LOG_TAG_WIFI_DIRECT, "request body: " +  postBody);
        JsonObjectRequest myRequest = new JsonObjectRequest(Method.POST, URL, jsonPostBody,
                new Response.Listener<JSONObject>() {
            /**
             * Response received
             * @param response - camera response
             */
            @Override
            public void onResponse(JSONObject response)
            {
                lambdaWrapper.responseMethod(response);
            }
        }, new Response.ErrorListener() {
            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void onErrorResponse(VolleyError error)
            {
                lambdaWrapper.errorMethod(error);
            }
        });
        myRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(myRequest);
    }

    /**
     * Change picture quality to fine
     * @param URL - camera URL
     * @param queue - request queue
     * @param id - request id
     * @param lambdaWrapper - request wrapper
     * @throws JSONException if the JSON is malformed
     */
    public static void makeVolleySonyAPISetJPEGQualityToFine(final String URL, RequestQueue queue,
                                                             final int id,
                                                             final JSONObjectResponseWrapper lambdaWrapper)
            throws JSONException
    {
        final String postBody = new JSONObject().put("method", "setStillQuality")
                .put("params", new JSONArray().put("Fine")).put("id", id)
                .put("version", "1.0").toString();
        JSONObject jsonPostBody = new JSONObject(postBody);
        Log.v(LOG_TAG_WIFI_DIRECT, "request body: " +  postBody);
        JsonObjectRequest myRequest = new JsonObjectRequest(Method.POST, URL, jsonPostBody,
                new Response.Listener<JSONObject>() {
            /**
             * Response received
             * @param response - camera response
             */
            @Override
            public void onResponse(JSONObject response)
            {
                lambdaWrapper.responseMethod(response);
            }
        }, new Response.ErrorListener() {
            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void onErrorResponse(VolleyError error)
            {
                lambdaWrapper.errorMethod(error);
            }
        });
        myRequest.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(myRequest);
    }

    /**
     * Request JSON array
     * @param URL - camera URL
     * @param queue - request queue
     * @param lambdaWrapper - request wrapper
     */
    public static void makeVolleyJSONArrayRequest(final String URL, RequestQueue queue,
                                                  final JSONArrayResponseWrapper lambdaWrapper)
    {
        Log.v(LOG_TAG, "volley URL:" + URL);
        JsonArrayRequest myRequest = new JsonArrayRequest(URL, new Response.Listener<JSONArray>() {
            /**
             * Response received
             * @param response - camera response
             */
            @Override
            public void onResponse(JSONArray response)
            {
                try
                {
                    JSONArray responseArray = new JSONArray();
                    for (int i = 0; i < response.length(); i++)
                    {
                        responseArray.put(response.getString(i));
                        Log.v(LOG_TAG, "the response is " + response.getString(i));
                    }
                    lambdaWrapper.responseMethod(responseArray);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    Log.v(LOG_TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {
            /**
             * Connection failed
             * @param error - failure
             */
            @Override
            public void onErrorResponse(VolleyError error)
            {
                lambdaWrapper.errorMethod(error);
            }
        });
        // Add the request to the RequestQueue.
        myRequest.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_VOLLEY_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(myRequest);
    }

    /**
     * Request image
     * @param URL - camera URL
     * @param queue - request queue
     * @param lambdaWrapper - request wrapper
     */
    public static void makeVolleyImageRequest(final String URL, RequestQueue queue,
                                              final ImageResponseWrapper lambdaWrapper)
    {
        Log.v(LOG_TAG, "volley URL:" + URL);
        ImageRequest request = new ImageRequest(URL, new Response.Listener<Bitmap>() {
            /**
             * Response received
             * @param bitmap - image
             */
            @Override
            public void onResponse(Bitmap bitmap)
            {
                lambdaWrapper.responseMethod(bitmap);
            }
        }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
            /**
             * Connection failed
             * @param error - failure
             */
            public void onErrorResponse(VolleyError error)
            {
                lambdaWrapper.errorMethod(error);
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(8000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Add the request to the RequestQueue.
        request.setRetryPolicy(new DefaultRetryPolicy(DEFAULT_VOLLEY_TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    /**
     * Cancel camera requests
     * @param aQueue - request queue
     */
    public static void cancelAllVolleyRequests(RequestQueue aQueue)
    {
        aQueue.cancelAll(new RequestQueue.RequestFilter() {
            /**
             * Cancel the request
             * @param request - request to cancel
             * @return Returns true
             */
            @Override
            public boolean apply(Request<?> request)
            {
                return true;
            }
        });
    }
}