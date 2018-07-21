package com.ABCDeve1opers.flashbot.view;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.ABCDeve1opers.flashbot.model.Card;
import com.ABCDeve1opers.flashbot.model.Deck;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The deck browser is a tool to search for specific cards and to modify/delete them easily. All
 * cards are presented in a list view. It is also possible to shuffle the deck and to reset the
 * levels of the cards.
 */
public class DeckBrowserActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SearchView.OnQueryTextListener, AbsListView.MultiChoiceModeListener {

    private static final int MAX_RESULTS = 200;
    private static final int CHANGE_DECK_NAME = 1;
    private static final String TAG = "DeckBrowserActivity";

    private String deckName;
    private Deck deck;
    private TextToSpeech tts;
    private ArrayAdapter<Card> cardAdapter;
    private List<Card> cards = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck_browser);
        Log.v(TAG,"oncreate deckbrowser activity");

        final ListView cardList = (ListView) findViewById(R.id.card_list);
        ContextWrapper themedContex = new ContextThemeWrapper(this, R.style.BlueTheme);
        cardAdapter = new ArrayAdapter<>(themedContex, android.R.layout.simple_list_item_activated_1, cards);
        cardList.setAdapter(cardAdapter);
        cardList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);


        Toolbar toolbar = (Toolbar) findViewById(R.id.deck_browser_toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        //setup multi choice list
        cardList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
//                Toast.makeText(getApplicationContext(),"context menu was clicked" , Toast.LENGTH_LONG).show();

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu,menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
        // normal click: edit
        cardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final Card card = cards.get(position);
                final Dialog dialog = new Dialog(DeckBrowserActivity.this);
                dialog.setContentView(R.layout.card_dialog);
                dialog.setTitle(getString(R.string.edit_card));
                final EditText frontEdit = (EditText) dialog.findViewById(R.id.edit_front);
                frontEdit.setText(card.getFront());
                final EditText backEdit = (EditText) dialog.findViewById(R.id.edit_back);
                backEdit.setText(card.getBack());
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
                        String front = frontEdit.getText().toString().trim();
                        String back = backEdit.getText().toString().trim();
                        if (front.length() == 0)
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.front_is_empty), Toast.LENGTH_SHORT).show();
                        else if(back.length() == 0)
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.back_is_empty), Toast.LENGTH_SHORT).show();
                        else {
                            card.edit(front, back);
                            deck.saveDeck();
                            cardAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    }
                });
                dialog.show();
            }
        });

        // long click: delete
        cardList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Card card = cardAdapter.getItem(position);


//                AlertDialog.Builder dialog = new AlertDialog.Builder(DeckBrowserActivity.this);
//                dialog.setTitle(getString(R.string.delete_card));
//                dialog.setMessage(getString(R.string.really_delete_card));
//                dialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        if(deck.deleteCard(card)) {
//                            Log.v(TAG,"delete card called");
//                            cards.remove(position);
//                            cardAdapter.notifyDataSetChanged();
//                        } else {
//                            Toast.makeText(getApplicationContext(),
//                                    getString(R.string.cannot_delete_last_card),
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                        dialog.dismiss();
//                    }
//                });
//                dialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                dialog.create().show();
                return true;
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        deckName = getIntent().getStringExtra("deck name");
        setTitle(deckName);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ReviewActivity.class);
                intent.putExtra("deck name", deckName);
                startActivity(intent);
            }
        });

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
        try {
            deck = Deck.loadDeck(deckName);
        } catch(IOException e){
            Toast.makeText(getApplicationContext(), getString(R.string.deck_could_not_be_loaded),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        displayCardList("");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.edit_deck_name:
//                Toast.makeText(getApplicationContext(), "edit deck name clicked", Toast.LENGTH_SHORT).show();
                Intent changeDeckNameIntent = new Intent(getApplicationContext(),RenameDeckActivity.class);
                changeDeckNameIntent.putExtra("deck name", deckName);
                startActivityForResult(changeDeckNameIntent,CHANGE_DECK_NAME);
                return true;
            case R.id.action_add:
                final Dialog dialog = new Dialog(DeckBrowserActivity.this);
                dialog.setContentView(R.layout.card_dialog);
                dialog.setTitle(getString(R.string.add_new_card));
                final EditText frontEdit = (EditText) dialog.findViewById(R.id.edit_front);
                final EditText backEdit = (EditText) dialog.findViewById(R.id.edit_back);
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
                        String front = frontEdit.getText().toString().trim();
                        String back = backEdit.getText().toString().trim();
                        if (front.length() == 0)
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.front_is_empty), Toast.LENGTH_SHORT).show();
                        else if(back.length() == 0)
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.back_is_empty), Toast.LENGTH_SHORT).show();
                        else {
                            Card newCard = new Card(front,back);
                            deck.addNewCard(newCard);
                            cards.add(newCard);
                            deck.saveDeck();
                            cardAdapter.notifyDataSetChanged();
                            dialog.dismiss();


                        }
                    }
                });
                dialog.show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHANGE_DECK_NAME) {
            if(resultCode == RESULT_OK){
//                setTitle(data.getExtras().getString("newName"));
                String newName = data.getExtras().getString("newName");
                Toast.makeText(getApplicationContext(),newName,Toast.LENGTH_LONG);
                deck.changeName(newName);
                setTitle(newName);
                deckName = newName;
            }else if(resultCode == RESULT_CANCELED){
                String oldName = data.getExtras().getString("oldName");
                setTitle(oldName);
                getDeck(oldName);
            }

        }
    }
    private void getDeck(String name){
        try {
            deck = Deck.loadDeck(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_deck_actions, menu);
        return true;

    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchViewItem = menu.findItem(R.id.search_deck);
        searchViewItem.setActionView(R.layout.edit_text_field);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchViewItem);
        searchView.setIconifiedByDefault(false);
//        searchView.setMaxWidth(Integer.MAX_VALUE);
        ActionBar.LayoutParams params = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);
        searchView.setLayoutParams(params);
        searchView.setOnQueryTextListener(this);
        return super.onPrepareOptionsMenu(menu);
    }

    private void displayCardList(String searchTerm){
        cards.clear();
        cards.addAll(deck.searchCards(searchTerm, MAX_RESULTS));
        cardAdapter.notifyDataSetChanged();
    }
    private void askForTTSActivation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.activate_tts_new_deck));
        builder.setMessage(getString(R.string.want_activate_tts));
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deck.activateTTS();
                initTTS();
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initTTS(){
        final Locale locale = getLocaleForTTS();
        if(locale != null){
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener(){
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        tts.setLanguage(locale);
                    }
                }
            });
        }
    }
    private Locale getLocaleForTTS(){
        String lang = deck.getLanguage();
        if(lang == null || lang.equals(""))
            return null;
        String country = deck.getAccent();
        if(country == null || country.equals(""))
            return new Locale(lang);
        return new Locale(lang, country);
    }
    private void reloadDeck(){
        setTitle(deckName);
        try {
            deck = Deck.loadDeck(deckName);
            if(!deck.isUsingTTS() && !deck.getLanguage().equals("") && deck.isNew())
                askForTTSActivation();
            if(deck.isUsingTTS())
                initTTS();
//            showNextCard();
        } catch(IOException e){
            Toast.makeText(getApplicationContext(), getString(R.string.deck_could_not_be_loaded),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.nav_manage) {
            // Handle the camera action
        }
//        else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }




    @Override
    public boolean onQueryTextChange(String newText) {
        cards.clear();
        cards.addAll(deck.searchCards(newText, MAX_RESULTS));
        cardAdapter.notifyDataSetChanged();
        return true;
    }
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_menu,menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        //peform updates to CAB due to an invalidate() reuquest
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete_items:
                deleteSelectedItems();
                mode.finish();
                return true;

            default:
                return false;
        }

    }

    private void deleteSelectedItems() {
        Toast.makeText(getApplicationContext(),"context menu was clicked" , Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        //Make any updates to the activity when CAB is removed.
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        //here do somthin as update selecte/deselected item
        Toast.makeText(getApplicationContext(),"context menu was clicked" , Toast.LENGTH_LONG).show();

    }


}
