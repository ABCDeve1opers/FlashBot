package com.ABCDeve1opers.flashbot.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ABCDeve1opers.flashbot.adapter.DeckInfoAdapter;
import com.ABCDeve1opers.flashbot.model.Card;
import com.ABCDeve1opers.flashbot.model.Deck;
import com.ABCDeve1opers.flashbot.model.DeckCollection;

import java.io.File;
import java.io.IOException;

/**
 * The DeckListActivity is the entry screen of StackSRS and shows all decks in a list view. Next to
 * the name of each deck, the total number of cards (blue), the number of remaining cards to learn
 * (red) and the number of cards already mastered (green) is displayed.
 */
public class DeckListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{

    private DeckInfoAdapter deckListAdapter;
    private DeckCollection deckCollection = new DeckCollection();
    private final String TAG = "DeckListActivity";

    private Button newButton;
    private Button downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navdrawer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ListView deckListView = (ListView) findViewById(R.id.deck_list);
        deckListAdapter = new DeckInfoAdapter(this, deckCollection.getDeckInfos());
        deckListView.setAdapter(deckListAdapter);

        // normal click: open deck
        deckListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // switch to download activity
                final String deckName = deckListAdapter.getItem(position).getName();
                Intent intent = new Intent(getApplicationContext(), ReviewActivity.class);
                intent.putExtra("deck name", deckName);
                startActivity(intent);
            }
        });

        // long click: delete deck
        deckListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String deckName = deckListAdapter.getItem(position).getName();
                AlertDialog.Builder dialog = new AlertDialog.Builder(DeckListActivity.this);
                dialog.setTitle(getString(R.string.delete_deck));
                dialog.setMessage(getString(R.string.really_delete_deck, deckName));
                dialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deckCollection.deleteDeckFile(deckName);
                        reloadDeckList();
                        dialog.dismiss();
                    }
                });
                dialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.create().show();
                return true;
            }
        });

//        newButton = (Button) findViewById(R.id.button_new);
//        downloadButton = (Button) findViewById(R.id.button_download);
//
//        newButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showNewDeckDialog();
//            }
//        });
//
//        downloadButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // switch to download activity
//                Intent intent = new Intent(getApplicationContext(), DeckDownloadActivity.class);
//                startActivity(intent);
//            }
//        });
//
        setVolumeControlStream(AudioManager.STREAM_MUSIC);



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                showNewDeckDialog();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadDeckList();
    }

    public void reloadDeckList() {
        try {
            File stackSRSDir = provideStackSRSDir();
            deckCollection.reload(stackSRSDir);
        } catch(IOException e){
            Toast.makeText(this, getString(R.string.collection_could_not_be_loaded),
                    Toast.LENGTH_SHORT).show();
        }
        deckListAdapter.notifyDataSetChanged();
    }

    private File provideStackSRSDir(){
        // if there is (possibly emulated) external storage available, we use it
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return getApplicationContext().getExternalFilesDir(null);
        } else { // otherwise we use an internal directory without access from the outside
            return getApplicationContext().getDir("StackSRS", MODE_PRIVATE);
        }
    }

    public void showNewDeckDialog(){
        final Dialog dialog = new Dialog(DeckListActivity.this);
        dialog.setContentView(R.layout.deck_dialog);
        dialog.setTitle(getString(R.string.new_deck));
        final EditText editDeckName = (EditText) dialog.findViewById(R.id.edit_deck_name);
        final EditText editLanguage = (EditText) dialog.findViewById(R.id.edit_language);
        final EditText editAccent = (EditText) dialog.findViewById(R.id.edit_accent);
        final CheckBox checkBoxTTS = (CheckBox) dialog.findViewById(R.id.checkbox_tts);
        Button cancelButton = (Button) dialog.findViewById(R.id.button_cancel);
        Button okButton = (Button) dialog.findViewById(R.id.button_ok);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deckName = editDeckName.getText().toString().trim();
                if (deckCollection.isIllegalDeckName(deckName)) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.illegal_deck_name), Toast.LENGTH_SHORT).show();
                } else if(deckCollection.deckWithNameExists(deckName)){
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.deck_already_exists, deckName), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Deck newDeck = new Deck(deckName, "");
                    newDeck.addNewCard(new Card("default", "default", 2));
                    newDeck.setLanguage(editLanguage.getText().toString().trim().toLowerCase());
                    newDeck.setAccent(editAccent.getText().toString().trim().toUpperCase());
                    if(checkBoxTTS.isChecked())
                        newDeck.activateTTS();
                    newDeck.saveDeck();
                    reloadDeckList();
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navdrawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            Log.v(TAG,"gallery tapped");

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
