package com.example.plainolnotes2;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static final int EDITOR_REQUEST_CODE = 1001; //1001-  integer value. This value can be anything you want. It's only there so you can identify the request when we come back to this activity.

    private CursorAdapter cursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cursorAdapter = new NotesCursorAdapter(this,null, 0);

        ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(cursorAdapter);

        //code you need to React when the User selects an item from the todo list
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri uri = Uri.parse(NotesProvider.CONTENT_URI+ "/" + id);  //the id argument. Here's how it's working in the background. The content provider, when used with an SQL-like database, with a primary key column, requires the primary key column to have a name of _ID. The content provider gets the value from that named column and passes it back to the ListActivity, so by the time we get this value back, we know exactly which item we want. We don't have to deal with the position of the list item in the list or any other information.
                intent.putExtra(NotesProvider.CONTENT_ITEM_TYPE, uri);
                startActivityForResult(intent, EDITOR_REQUEST_CODE); //start the Activity
            }
        });

        getLoaderManager().initLoader(0, null, this);

    }

    private void insertNote(String noteText) {
        ContentValues values = new ContentValues();
        values.put(DBOpenHelper.NOTE_TEXT, noteText);
        Uri noteUri = getContentResolver().insert(NotesProvider.CONTENT_URI,
                values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_create_sample: //note ":" not ";"   //If Create All Sample on hamburger menu (see menu_main.xml) is pressed, launch method "insertSampleData()"
                insertSampleData();
                break;
            case R.id.action_delete_all: //note ":" not ";"      //If Delete All Notes on hamburger menu (see menu_main.xml) is pressed, launch method "deleteAllNotes()"
                deleteAllNotes();
                break;

        }


        //noinspection SimplifiableIfStatement
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllNotes() {
        //going to start with a dialogue box that asks user to confirm if they want to delete all notes (since this is really long, its copied over from Gist:
        //git.io/jxbB.
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) { //Dialog Interface: "Are you sure you want to delete all notes" all the words are located in strings.xml
                        if (button == DialogInterface.BUTTON_POSITIVE) { // (BUTTON_POSITVE is the 'Okay' button)
                            //Insert Data management code here
                            getContentResolver().delete(
                                    NotesProvider.CONTENT_URI, null, null
                            );
                            restartLoader(); //delete everything, then restart loader then toast message to tell the user what happened.

                            Toast.makeText(MainActivity.this,
                                    getString(R.string.all_deleted),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.are_you_sure))
                .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
                .setNegativeButton(getString(android.R.string.no), dialogClickListener)
                .show();



    }

    private void insertSampleData() {  //insert sample data method created by Alt+Enter on "insertSampleData" in Switch Case above
        //Create multiple notes for Sample:
        insertNote("Simple note");
        insertNote("Multi-Line\nnote");
        insertNote("Very long note with a lot of text that exceeds the width of the screen");

        //Each time you change the data in the database you need to tell your loader object that it needs to restart and reread the data from the back-end database
        restartLoader(); //<--= this was created after typing "getLoaderManager().restartLoader(0, null, this);" and the Highlighting the phrase>RightClicking it>Refactor>Extract>Method> then naming it "restartLoader"
    }

    private void restartLoader() {      //<--=
        getLoaderManager().restartLoader(0, null, this); //<--=
    }   //<--=

    //Callback methods (you will never call these methods yourself)
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) { //the cursor loader is specifically designed to manage a cursor
        return new CursorLoader(this, NotesProvider.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursorAdapter.swapCursor(data); //pass cursor object "Cursor data" to cursorAdapater.
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { //this method is called whenever a cursor needs to be wiped out.
        cursorAdapter.swapCursor(null); //null to wipe whatever needs to be wiped out, in this case the Cursor
    }

    public void openEditorForNewNote(View view) {
        Intent intent = new Intent(this, EditorActivity.class);
        startActivityForResult(intent, EDITOR_REQUEST_CODE);
    }

    //When the user completes the operation in the editor activity they'll press the back button or the up button to return to the main activity.
    // And that'll trigger the method onActivityResult. I'll add in override of that method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDITOR_REQUEST_CODE && resultCode == RESULT_OK){ //I'll also examine the result code that's passed in as the second argument. And I'll say if resultCode has a value of RESULT_OK and if both of those are true,
        // then I'll call my method restartLoader.
            restartLoader(); // And that method restarts the loader from the loader manager and refills the data from the database and displays it in the list.
            // All the code is done and I'm ready to test.
        }
    }
}
