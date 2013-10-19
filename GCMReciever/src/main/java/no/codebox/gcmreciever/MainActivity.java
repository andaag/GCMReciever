package no.codebox.gcmreciever;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import no.codebox.gcmreciever.db.MessageContentProvider;
import no.codebox.gcmreciever.events.LogMessage;
import no.codebox.gcmreciever.events.RegisterEvent;
import no.codebox.gcmreciever.helpers.GCMRegister;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final Bus bus = new Bus();
    private GCMRegister gcmRegister;
    private TextView textView;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.output);
        ListView listView = (ListView) findViewById(android.R.id.list);


        new BusEvents();
        gcmRegister = new GCMRegister(this, bus);
        getLoaderManager().initLoader(0, null, this);
        messageAdapter = new MessageAdapter(this);
        listView.setAdapter(messageAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gcmRegister.registerIfNeccesary();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, MessageContentProvider.CONTENT_URI, new String[]{"_id, timestamp", "title", "message", "icon", "expires"}, null, null, "timestamp ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> objectLoader, Cursor c) {
        messageAdapter.swapCursor(c);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> objectLoader) {
        messageAdapter.swapCursor(null);
    }

    private class MessageAdapter extends CursorAdapter {
        private LayoutInflater inflater;

        public MessageAdapter(Context context) {
            super(context, null, false);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View view = inflater.inflate(R.layout.row_messages, viewGroup, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) view.findViewById(R.id.title);
            view.setTag(viewHolder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            int IDX_TITLE = cursor.getColumnIndex("title");
            viewHolder.title.setText(cursor.getString(IDX_TITLE));
        }

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

    private static class ViewHolder {
        public TextView title;
    }
}
