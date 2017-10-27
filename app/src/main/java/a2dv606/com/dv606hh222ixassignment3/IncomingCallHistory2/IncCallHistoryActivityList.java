package a2dv606.com.dv606hh222ixassignment3.IncomingCallHistory2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import a2dv606.com.dv606hh222ixassignment3.R;

/**
 * A list activity to show the history of incoming calls
 *
 * Created by hatem on 2017-08-02.
 */

public class IncCallHistoryActivityList extends Activity {

    public static final String FILE_NAME = "incCallHistory.txt";
    public static String oldState;      //A string to check if the state of the phone has changed (the broadcast receiver gets called twice when the phone rings once)

    private ListView callsListView;
    private ArrayList<String> callsArrayList = new ArrayList<>();
    ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incoming_call_history_layout);

        callsArrayList.clear();
        callsListView = (ListView) findViewById(R.id.call_history_list);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_2, android.R.id.text1, callsArrayList);
        readItems();
        callsListView.setAdapter(listAdapter);

        //When an item in the listview is clicked
        callsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                //Show popup menu for options to call or message number
                PopupMenu popup = new PopupMenu(IncCallHistoryActivityList.this, view);
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        String phoneNo = callsListView.getItemAtPosition(i).toString();
                        String itemSelected = (String) item.getTitle();

                        switch (itemSelected) {
                            case "Call Number":
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setData(Uri.parse("tel:" + phoneNo));
                                startActivity(callIntent);
                                break;
                            case "Send a Message":
                                Intent msgIntent = new Intent(Intent.ACTION_SEND);
                                msgIntent.setType("text/plain");
                                msgIntent.putExtra(Intent.EXTRA_TEXT, phoneNo);
                                startActivity(Intent.createChooser(msgIntent, "Send a message via.."));
                                break;
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        readItems();
    }

    /**
     * Read the info from the call history file
     */
    private void readItems() {
        callsArrayList.clear();
        try {
            File file = new File(getFilesDir(), FILE_NAME);
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                callsArrayList.add(line);
            }
            reader.close();
            inputStream.close();
            listAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
