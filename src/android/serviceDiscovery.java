package com.scott.plugin;

import org.apache.cordova.*;
import android.content.Context;
import android.os.Bundle;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.IOException;
import android.util.Log;
import org.json.JSONObject;
import com.scott.plugin.SimpleSD;

public class serviceDiscovery extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray data, final CallbackContext callbackContext) throws JSONException {

        final SimpleSD mSimpleSD = new SimpleSD();

        if (action.equals("getNetworkServices")) {

            final String service = data.getString(0);

            cordova.getThreadPool().execute(new Runnable() {
               @Override
               public void run() {
                  try{
                      mSimpleSD.search(service,callbackContext);
                    }catch(IOException e){
                      e.printStackTrace();
                    }
               }
            });  
            return true;

        } else {

            return false;

        }
    }
}
