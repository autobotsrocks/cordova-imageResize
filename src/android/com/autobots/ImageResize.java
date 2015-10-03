package com.autobots;

import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;


public class ImageResize extends CordovaPlugin {

    public static final String RESIZE_TYPE_MIN_PIXEL = "minPixelResize";
    public static final String RESIZE_TYPE_MAX_PIXEL = "maxPixelResize";

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("resize")) {
            cordova.getThreadPool().execute(new Runnable() {
                public void run() {
                    try {
                        JSONObject options = args.getJSONObject(0);
                        String source;
                        int sourceWidth;
                        int sourceHeight;
                        int quality = 75;

                        if (!options.has("source")) {
                            callbackContext.error("Please set the source.");
                            return;
                        }

                        source = options.getString("source").replace("file://", "");
                        Bitmap bitmap = BitmapFactory.decodeFile(source);
                        if (bitmap == null) {
                            callbackContext.error("Can't open file " + source);
                            return;
                        }

                        sourceWidth = bitmap.getWidth();
                        sourceHeight = bitmap.getHeight();
                        float [] factors = calculateFactors(options, sourceWidth, sourceHeight);

                        Matrix matrix = new Matrix();
                        matrix.postScale(factors[0], factors[1]);
                        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, sourceWidth, sourceHeight, matrix, false);

                        if (options.has("quality")) {
                            quality = options.getInt("quality");
                        }
                        String filePath = cordova.getActivity().getApplicationContext().getCacheDir().getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg";
                        File file = new File(filePath);
                        OutputStream outStream = new FileOutputStream(file);
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
                        outStream.flush();
                        outStream.close();
                        JSONObject response = new JSONObject();
                        response.put("filePath", filePath);
                        response.put("width", resizedBitmap.getWidth());
                        response.put("height", resizedBitmap.getHeight());
                        callbackContext.success(response);
                    } catch (JSONException e) {
                        callbackContext.error(e.getMessage());
                    } catch (FileNotFoundException e) {
                        callbackContext.error(e.getMessage());
                    } catch (IOException e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });
        }
        return true;
    }

    private float[] calculateFactors(JSONObject options, int width, int height) throws JSONException {
        float widthFactor;
        float heightFactor;
        String type = options.getString("type");
        float desiredWidth = (float)options.getDouble("width");
        float desiredHeight = (float)options.getDouble("height");

        if (type.equals(RESIZE_TYPE_MIN_PIXEL)) {
            widthFactor = desiredWidth / (float)width;
            heightFactor = desiredHeight / (float)height;
            if (widthFactor > heightFactor && widthFactor <= 1.0) {
                heightFactor = widthFactor;
            } else if (heightFactor <= 1.0) {
                widthFactor = heightFactor;
            } else {
                widthFactor = 1.0f;
                heightFactor = 1.0f;
            }
        } else if (type.equals(RESIZE_TYPE_MAX_PIXEL)) {
            widthFactor = desiredWidth / (float)width;
            heightFactor = desiredHeight / (float)height;
            if (widthFactor == 0.0) {
                widthFactor = heightFactor;
            } else if (heightFactor == 0.0) {
                heightFactor = widthFactor;
            } else if (widthFactor > heightFactor) {
                widthFactor = heightFactor; // scale to fit height
            } else {
                heightFactor = widthFactor; // scale to fit width
            }
        } else {
            widthFactor = 1.0f;
            heightFactor = 1.0f;
        }

        float[] factors = {widthFactor, heightFactor};
        return factors;
    }
}