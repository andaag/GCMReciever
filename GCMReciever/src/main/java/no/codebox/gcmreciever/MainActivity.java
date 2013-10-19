package no.codebox.gcmreciever;

import java.io.IOException;
import java.util.Map;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import no.codebox.gcmreciever.helpers.IntentCreator;
import no.codebox.gcmreciever.helpers.JsonParser;

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
            viewHolder.message = (TextView) view.findViewById(R.id.message);
            view.setTag(viewHolder);
            return view;
        }

        private boolean renderKey(TextView view, String key, Map data) {
            String msg = data.containsKey(key) ? data.get(key).toString() : null;
            if (msg != null) {
                view.setVisibility(View.VISIBLE);
                view.setText(msg);
                return true;
            } else {
                view.setVisibility(View.GONE);
                return false;
            }
        }

        @Override
        public boolean isEnabled(int position) {
            try {
                return hasIntent(getData(position));
            } catch (IOException e) {
                return false;
            }
        }

        private boolean hasIntent(Map data) {
            return (data != null && data.containsKey("intent"));
        }

        private Map getData(int position) throws IOException {
            Cursor cursor = getCursor();
            if (cursor == null) {
                return null;
            }
            cursor.moveToPosition(position);
            int IDX_DATA = cursor.getColumnIndex("json");
            return (Map) JsonParser.parseBlock(cursor.getString(IDX_DATA));
        }

        private void click(int position) {
            assert isEnabled(position);
            try {
                Map data = (Map) getData(position).get("intent");
                Intent intent = IntentCreator.createIntent(data);
                startActivity(intent);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            int IDX_DATA = cursor.getColumnIndex("json");
            ViewHolder viewHolder = (ViewHolder) view.getTag();

            try {
                Map data = getData(cursor.getPosition());
                boolean hasTitle = renderKey(viewHolder.title, "title", data);
                boolean hasMessage = renderKey(viewHolder.message, "message", data);
                if (!hasMessage && !hasTitle) {
                    viewHolder.message.setText("Missing title and message in:\n" + cursor.getString(IDX_DATA));
                    viewHolder.message.setVisibility(View.VISIBLE);
                }
            } catch (IOException e) {
                e.printStackTrace();
                viewHolder.title.setText("Failed to parse json block " + cursor.getString(IDX_DATA));
            }

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
        public TextView message;
    }
}
