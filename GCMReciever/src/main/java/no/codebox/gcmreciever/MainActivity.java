package no.codebox.gcmreciever;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import no.codebox.gcmreciever.events.LogMessage;
import no.codebox.gcmreciever.events.RegisterEvent;
import no.codebox.gcmreciever.helpers.GCMRegister;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final Bus bus = new Bus();
    private GCMRegister gcmRegister;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bus.register(this);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.output);

        new BusEvents();
        gcmRegister = new GCMRegister(this, bus);
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
