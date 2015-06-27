package com.example.plainolnotes2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.w3c.dom.Text;


public class EditorActivity extends ActionBarActivity {

    // In this class, I'll declare two private fields.
    private String action;  // The first will be a string and I'll call it ACTION. // I'll be using this field to remember that I'm doing whether I'm inserting or updating a note and
    private EditText editor; //then, I'll also create a field to represent the edit text object, that's the editor control that the user's typing into.

    private String noteFilter; //The noteFilter will be a WHERE clause that I'll use in SQL statements
    private String oldText;    //oldText will contain the existing text of the selected note.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editor = (EditText) findViewById(R.id.editText); //

        Intent intent = getIntent();

        Uri uri = intent.getParcelableExtra(NotesProvider.CONTENT_ITEM_TYPE); //The URI class implements the parcelable inferface. That interface makes it possible to pass a complex object as an intent extra.
        //NotesProvider.CONTENT_ITEM_TYPE If that URI was passed in, it won't be null but if the user pressed the "Insert" button, it will be null
        // So, I'll use conditional code, an If statement, and my condition will be
        // If uri == null Then if it is, then I know that I'm supposed to be inserting a new note.
        if(uri == null) {
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note)); //?check for errror "new note"  6:10 updating an existing note

            //When I go the the EditorActivity, and I pass in a uri that means I want to edit an existing note.
            // So I'll add an else clause to this if statement.
            // The first part of the if statement is only executed if the uri is null, that is if it hasn't been passed in, but if it has been passed in, then I know that I want to edit a note.
        }else{
            action = Intent.ACTION_EDIT;
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();

            //retrieve the one row from the database
            Cursor cursor = getContentResolver().query(uri,
                    DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(DBOpenHelper.NOTE_TEXT));
            editor.setText(oldText);
            editor.requestFocus();
        }
    }

    //The Menu inside the note editor
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        //The Trash Can Icon delete on the action bar
        //I'm inflating that menu. But I only have one item in the menu, and I only want to show that item in a particular condition.
        // So I'll add an "if" clause, and I'll take a look at the "action" attribute.
        // I'll compare it to the value of "Intent.ACTION_EDIT".
        if (action.equals(Intent.ACTION_EDIT)){
            getMenuInflater().inflate(R.menu.menu_editor, menu); //And now, the trash can should only show up for an existing note.
        }
        return true;
    }

    @Override
    //The other condition when I want to call my finishEditing method is
    // when the user touches the up button or the back button in the toolbar.
    // That's registered in the onOptionsItemSelected method
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) { //And add a switch statement and I'll examine the value of item.getItemId When the user touches that back button, the ID is always the same.
            case android.R.id.home: //
                finishEditing();
                break;
            //deletes note from SQL database
            case R.id.action_delete:
                deleteNote();
                break;
        }
        return true;
    }

    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI, //deletes note from database
                noteFilter, null); //noteFilter String to make sure we are only deleting that ONE SELECTED note/row
        Toast.makeText(this, getString(R.string.note_deleted), Toast.LENGTH_SHORT).show();//toast message: "Note Deleted" to tell users note was deleted.
                                                                                            //English translation (passedmethodinfromline above, "Note deleted", length of toast time = short).show it
        setResult(RESULT_OK); //operation was completed
        finish();
    }


    //This method will be called when the user presses the device's back button or the up or back button on the toolbar.
    //This method is to save the note you created using the "+" button
    private void finishEditing(){
        String newText = editor.getText().toString().trim(); //find out what the user typed in. I'll create a string variable that I'll call newText and I'll get it's value from my editor object, that's the edit text object that I already set the reference to. //calling the .trim() method at the end makes sure it eliminates any trailing white spaces

        //if-then action to insert note
        switch (action) {
            case Intent.ACTION_INSERT:
                 if(newText.length() == 0) {
                 setResult(RESULT_CANCELED);//makes sure you are not inserting a blank note if you are inserting a new note
                 } else {   //if note editor is not blank: insert note*
                     insertNote(newText);
                 }
                break;
            //add code to the finished editing method to handle updates
            case Intent.ACTION_EDIT:
                if(newText.length() == 0) {
                   deleteNote(); //if no text in editor, delete note
                } else if(oldText.equals(newText)) { //if old text is the same and nothing is changed
                    setResult(RESULT_CANCELED);
                } else {
                   updateNote(newText); //or else: update the note in the database
                }

        }
        finish();
    }

    //code for updating note method in database
    private void updateNote(String noteText) {
        //first two lines of code same as insertNote
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null); //I'm reusing that noteFilter value to make sure I'm only updating the one selected row

        Toast.makeText(this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show(); //display a Toast message "Note Updated" after updating the database
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText) { //*insert note
        //first two lines of code same as updateNote
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);

        getContentResolver().insert(NotesProvider.CONTENT_URI, values); //insert data into database table
        setResult(RESULT_OK);
    }

    //Override onBackPress method
    @Override
    public void onBackPressed() {
        finishEditing();
    }


}
