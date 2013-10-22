package no.codebox.gcmreciever;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import no.codebox.gcmreciever.adapter.MessageAdapter;
import no.codebox.gcmreciever.db.HeartbeatAsyncHandler;
import no.codebox.gcmreciever.db.MessageContentProvider;
import no.codebox.gcmreciever.events.LogMessage;
import no.codebox.gcmreciever.events.RegisterEvent;
import no.codebox.gcmreciever.helpers.GCMRegister;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    private static final String TAG = MainActivity.class.getName();

    private final Bus bus = new Bus();
    private GCMRegister gcmRegister;
    private TextView textView;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.info);
        ListView listView = (ListView) findViewById(android.R.id.list);


        new BusEvents();
        gcmRegister = new GCMRegister(this, bus);
        getLoaderManager().initLoader(0, null, this);
        messageAdapter = new MessageAdapter(this);
        listView.setAdapter(messageAdapter);
        listView.setOnItemClickListener(this);
        new HeartbeatAsyncHandler(getContentResolver()).updateLastseen();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gcmRegister.registerIfNeccesary();
    }

/*    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, MessageContentProvider.CONTENT_URI, new String[]{"_id, timestamp", "json"}, null, null, "timestamp DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> objectLoader, Cursor c) {
        if (c == null || c.getCount() == 0) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
        messageAdapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> objectLoader) {
        messageAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        messageAdapter.click(i);
    }

    private class BusEvents {
        public BusEvents() {
            bus.register(this);
        }

        @Subscribe
        public void onRegisterEvent(RegisterEvent registerEvent) {
            if (registerEvent.e != null) {
                // @todo : handle exception properly
                Toast.makeText(MainActivity.this, "Exception : " + registerEvent.e, Toast.LENGTH_LONG).show();
                registerEvent.e.printStackTrace();
            } else {
                Toast.makeText(MainActivity.this, "Synced with GCM", Toast.LENGTH_SHORT).show();
            }
        }

        @Subscribe
        public void onLogMessage(LogMessage logMessage) {
            textView.setText(textView.getText() + "\n" + logMessage.message);
            Log.i(TAG, logMessage.message);
        }
    }

}
