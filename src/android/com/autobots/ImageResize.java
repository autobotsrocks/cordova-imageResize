package com.autobots;

import android.graphics.Bitmap;
import android.os.Environment;

import com.bumptech.glide.Glide;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutionException;


public class ImageResize extends CordovaPlugin {

    public static final String RESIZE_TYPE_MIN_PIXEL = "minPixelResize";
    public static final String RESIZE_TYPE_MAX_PIXEL = "maxPixelResize";

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("resize")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        JSONObject options = args.getJSONObject(0);
                        if (!options.has("source")) {
                            callbackContext.error("Please set the source.");
                            return;
                        }

                        File sourceFile = new  File(options.getString("source"));
                        Bitmap bitmap = Glide.with(cordova.getActivity()).load(sourceFile).asBitmap().centerCrop().into(
                                options.getInt("width"),
                                options.getInt("height")
                        ).get();

                        int quality = 75;
                        if (options.has("quality")) {
                            quality = options.getInt("quality");
                        }
                        String filePath = getTempDirectoryPath() + "/" + System.currentTimeMillis() + ".resize.jpg";
                        File file = new File(filePath);
                        OutputStream outStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
                        outStream.flush();
                        outStream.close();
                        JSONObject response = new JSONObject();
                        response.put("filePath", filePath);
                        response.put("width", bitmap.getWidth());
                        response.put("height", bitmap.getHeight());
                        callbackContext.success(response);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    } catch (ExecutionException e) {
                        callbackContext.error(e.getMessage());
                    } catch (InterruptedException e) {
                        callbackContext.error(e.getMessage());
                    } catch (IOException e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });
        }
        return true;
    }

    private String getTempDirectoryPath() {
        File cache = null;

        // SD Card Mounted
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            cache = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/Android/data/" + cordova.getActivity().getPackageName() + "/cache/"
            );
        } else {
            // Use internal storage
            cache = cordova.getActivity().getCacheDir();
        }
        cache.mkdirs();
        return cache.getAbsolutePath();
    }
}