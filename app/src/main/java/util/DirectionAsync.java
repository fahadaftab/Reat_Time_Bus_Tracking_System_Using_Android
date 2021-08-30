package util;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class DirectionAsync extends AsyncTask<Object, String, String> {
    Context c;
    String data = "";
    LatLng endLatLng;
    HttpURLConnection httpURLConnection = null;
    InputStream inputStream = null;
    GoogleMap mMap;
    Marker marker;
    String myurl;
    LatLng startLatLng;

    public DirectionAsync(Context context) {
        this.c = context;
    }

    /* Access modifiers changed, original: protected|varargs */
    public String doInBackground(Object... objArr) {
        this.mMap = (GoogleMap) objArr[0];
        this.myurl = (String) objArr[1];
        this.startLatLng = (LatLng) objArr[2];
        this.endLatLng = (LatLng) objArr[3];
        this.marker = (Marker) objArr[4];
        try {
            this.httpURLConnection = (HttpURLConnection) new URL(this.myurl).openConnection();
            this.httpURLConnection.connect();
            this.inputStream = this.httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    break;
                }
                stringBuffer.append(readLine);
            }
            this.data = stringBuffer.toString();
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.data;
    }

    /* Access modifiers changed, original: protected */
    public void onPostExecute(String str) {
        String str2 = "legs";
        String str3 = "routes";
        try {
            JSONObject jSONObject = new JSONObject(str);
            int i = 0;
            JSONArray jSONArray = jSONObject.getJSONArray(str3).getJSONObject(0).getJSONArray(str2).getJSONObject(0).getJSONArray("steps");
            str2 = jSONObject.getJSONArray(str3).getJSONObject(0).getJSONArray(str2).getJSONObject(0).getJSONObject("duration").getString("text");
            this.marker.setTitle(str2);
            Context context = this.c;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str2);
            stringBuilder.append(" away.");
            Toast.makeText(context, stringBuilder.toString(), Toast.LENGTH_LONG).show();
            int length = jSONArray.length();
            String[] strArr = new String[length];
            for (int i2 = 0; i2 < length; i2++) {
                strArr[i2] = jSONArray.getJSONObject(i2).getJSONObject("polyline").getString("points");
            }
            int length2 = strArr.length;
            while (i < length2) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(-16711936);
                polylineOptions.width(10.0f);
               //  polylineOptions.addAll(PolyUtil.decode(strArr[i]));
                this.mMap.addPolyline(polylineOptions);
                i++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
