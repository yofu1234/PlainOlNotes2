package com.example.plainolnotes2;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class NotesCursorAdapter extends CursorAdapter {
    public NotesCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return LayoutInflater.from(context).inflate(  //creates layout inflater object .then calls inflate
                R.layout.note_list_item, parent, false //passes in two values: parent, false.
        );
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        /* Next, I need to bind the view.
        When you bind the view, you receive an instance of the cursor object, and it will already point
        to the particular row of your database that's supposed to be displayed.
        You receive three arguments, a view, a context, and the cursor. */

        String noteText = cursor.getString(
                cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));

        int pos = noteText.indexOf(10); // So now I have the text for the note that I want to display. Now remember, the reason for this whole exercise is to handle notes that have line feeds, so next I need to detect whether there's a line feed in the string. I'll create an integer variable that I'll name P-O-S, for position, and I'll get it from the method indexOf. I'll call noteText.indexOf, and I'll pass in a value of 10. 10 is the ASCII value of a line feed character
        if (pos != -1) {
            noteText = noteText.substring(0, pos) + " ...";
        }

        TextView tv = (TextView) view.findViewById(R.id.tvNote);
        tv.setText(noteText);

    }
}
