package com.scott.plugin;

import org.apache.cordova.*;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Scanner;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class SimpleSD extends CordovaPlugin {

    public static final String DEVICE_MEDIA_SERVER_1 = "urn:schemas-upnp-org:device:MediaServer:1";   
    public static final String SERVICE_CONTENT_DIRECTORY_1 = "urn:schemas-upnp-org:service:ContentDirectory:1";
    public static final String SERVICE_CONNECTION_MANAGER_1 = "urn:schemas-upnp-org:service:ConnectionManager:1";
    public static final String SERVICE_AV_TRANSPORT_1 = "urn:schemas-upnp-org:service:AVTransport:1";
    public static final String SERVICE_NAGRA_REMOTE_CONTROL = "urn:nagra:service:NagraRemoteControl:1";

    private static final String TAG = "scott.plugin.SimpleSD";

    public static String parseHeaderValue(String content, String headerName) {
        Scanner s = new Scanner(content);
        s.nextLine();
        while (s.hasNextLine()) {
          String line = s.nextLine();
          int index = line.indexOf(':');

          if (index == -1) {
            return null;
          }
          String header = line.substring(0, index);
          if (headerName.equalsIgnoreCase(header.trim())) {
            return line.substring(index + 1).trim();
          }
        }
        return null;
    }

    public static String getResponseFromUrl(String url) {
        String xml = null;
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            xml = EntityUtils.toString(httpEntity);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xml;
    }

    public void search(String service, CallbackContext callbackContext) throws IOException {
        final int SSDP_PORT = 1900;
        final int SSDP_SEARCH_PORT = 1901;
        final String SSDP_IP = "239.255.255.250";
        int TIMEOUT = 3000;

        InetSocketAddress srcAddress = new InetSocketAddress(SSDP_SEARCH_PORT);
        // Send to 239.255.255.250:1900
        InetSocketAddress dstAddress = new InetSocketAddress(InetAddress.getByName(SSDP_IP), SSDP_PORT);

        JSONArray deviceList = new JSONArray();

        Log.v(TAG, "srcAddress" + srcAddress);

        // Contruct M-Search Packet
        StringBuffer discoveryMessage = new StringBuffer();
        discoveryMessage.append("M-SEARCH * HTTP/1.1\r\n");
        discoveryMessage.append("HOST: " + SSDP_IP + ":" + SSDP_PORT + "\r\n");
        
        discoveryMessage.append("ST:"+service+"\r\n");
        //discoveryMessage.append("ST:ssdp:all\r\n");
        discoveryMessage.append("MAN: \"ssdp:discover\"\r\n");
        discoveryMessage.append("MX: 2\r\n");
        discoveryMessage.append("\r\n");
        System.out.println("Request: " + discoveryMessage.toString() + "\n");
        byte[] discoveryMessageBytes = discoveryMessage.toString().getBytes();
        DatagramPacket discoveryPacket = new DatagramPacket(discoveryMessageBytes, discoveryMessageBytes.length, dstAddress);

        // Send multi-cast packet
        MulticastSocket multicast = null;
        try {
            multicast = new MulticastSocket(null);
            multicast.bind(srcAddress);
            multicast.setTimeToLive(4);
            Log.v(TAG, "Send multicast request...");
            // ----- Sending multi-cast packet ----- //
            multicast.send(discoveryPacket);
        } finally {
            Log.v(TAG, "Multicast ends. Close connection....");
            multicast.disconnect();
            multicast.close();
        }

        // Create a socket and cross your fingers for a response
        DatagramSocket wildSocket = null;
        DatagramPacket receivePacket = null;
        try {
            wildSocket = new DatagramSocket(SSDP_SEARCH_PORT);
            wildSocket.setSoTimeout(TIMEOUT);

            while (true) {
                try {
                    Log.v(TAG, "Receive ssdp");
                    receivePacket = new DatagramPacket(new byte[1536], 1536);
                    wildSocket.receive(receivePacket);
                    String message = new String(receivePacket.getData());   
                    try {
                        JSONObject device = new JSONObject();
                        device.put("id", parseHeaderValue(message, "USN"));
                        device.put("serviceURL", parseHeaderValue(message, "LOCATION"));
                        device.put("type", parseHeaderValue(message, "ST"));
                        device.put("server", parseHeaderValue(message, "Server"));
                        //device.put("xml", getResponseFromUrl(parseHeaderValue(message, "LOCATION")));
                        deviceList.put(device);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (SocketTimeoutException e) {
                    Log.v(TAG, "Time out");
                    Log.v(TAG, "" + deviceList);
                    callbackContext.success(deviceList);
                    break;
                }
            }
        } finally {
            if (wildSocket != null) {
                wildSocket.disconnect();
                wildSocket.close();
            }
        }
    }

}
