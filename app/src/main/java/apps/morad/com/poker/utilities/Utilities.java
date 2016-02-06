package apps.morad.com.poker.utilities;

import android.database.Cursor;
import android.util.Log;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Morad on 12/16/2015.
 */
public class Utilities {

    public static ObjectMapper mapper = new ObjectMapper();

    public static Cursor fetchResultCursor(Class<? extends Model> _class) {

        return fetchResultCursor(_class, false, null, null);
    }

    public static Cursor fetchResultCursor(Class<? extends Model> _class, boolean sort, String column) {

        return fetchResultCursor(_class, sort, column, null);
    }

    public static Cursor fetchResultCursor(Class<? extends Model> _class, boolean sort, String column, String where, String...args) {
        String tableName = Cache.getTableInfo(_class).getTableName();
        // Query all items without any conditions
        String resultRecords;

        From select = new Select(tableName + ".*, " + tableName + ".Id as _id").from(_class);

        if(where != null && !where.isEmpty()){
            select.where(where, args);
        }
        if(sort) {
            resultRecords = select.orderBy(column + " DESC").toSql();
        }
        else {
            resultRecords = select.toSql();
        }

        // Execute query on the underlying ActiveAndroid SQLite database
        Cursor resultCursor = Cache.openDatabase().rawQuery(resultRecords, args);

        return resultCursor;
    }

    public static JSONObject sendRequest(String urlStr, String method, JSONObject params){
        JSONObject result = new JSONObject();
        StringBuffer chaine = new StringBuffer("");
        HttpURLConnection connection = null;
        try{
            URL url = new URL(urlStr);
            connection = (HttpURLConnection)url.openConnection();


            if(!method.equals("GET")) {
                connection.setRequestMethod(method);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.setUseCaches(false);
                connection.setChunkedStreamingMode(0);
                connection.setDoInput(true);
                OutputStreamWriter printout = new OutputStreamWriter(connection.getOutputStream());
                printout.write(params.toString());
                printout.flush();
                printout.close();
            }

            connection.connect();

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            while ((line = rd.readLine()) != null) {
                chaine.append(line);
            }
            rd.close();

            result = new JSONObject(chaine.toString());

        } catch (Exception e) {
            Log.e("Request", e.getMessage());
        }
        finally {
            if(connection != null){
                connection.disconnect();
            }
        }

        return result;
    }
}
