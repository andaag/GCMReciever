package no.codebox.gcmreciever.adapter;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import no.codebox.gcmreciever.R;
import no.codebox.gcmreciever.helpers.IntentCreator;
import no.codebox.gcmreciever.model.GCMMsg;

public class MessageAdapter extends CursorAdapter {
    private LayoutInflater inflater;
    private Context context;

    public MessageAdapter(Context context) {
        super(context, null, false);
        this.context = context;
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

    @Override
    public boolean isEnabled(int position) {
        try {
            return getData(position).hasIntent();
        } catch (IOException e) {
            return false;
        }
    }

    private GCMMsg getData(int position) throws IOException {
        Cursor cursor = getCursor();
        if (cursor == null) {
            return null;
        }
        cursor.moveToPosition(position);
        int IDX_DATA = cursor.getColumnIndex("json");
        return new GCMMsg(cursor.getString(IDX_DATA));
    }

    public void click(int position) {
        assert isEnabled(position);
        try {
            GCMMsg msg = getData(position);
            Intent intent = IntentCreator.createIntent(msg);
            context.startActivity(intent);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int IDX_DATA = cursor.getColumnIndex("json");
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        try {
            GCMMsg msg = getData(cursor.getPosition());
            viewHolder.title.setText(msg.getTitle());
            viewHolder.message.setText(msg.getString("message", ""));
            if (viewHolder.title.getText().length() == 0 && viewHolder.message.getText().length() == 0) {
                viewHolder.message.setText("Missing title and message in:\n" + cursor.getString(IDX_DATA));
                viewHolder.message.setVisibility(View.VISIBLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            viewHolder.title.setText("Failed to parse json block " + cursor.getString(IDX_DATA));
        }

    }


    private static class ViewHolder {
        public TextView title;
        public TextView message;
    }
}