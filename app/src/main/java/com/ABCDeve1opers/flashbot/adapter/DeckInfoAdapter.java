package com.ABCDeve1opers.flashbot.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ABCDeve1opers.flashbot.model.DeckCollection;
import com.ABCDeve1opers.flashbot.model.DeckInfo;
import com.ABCDeve1opers.flashbot.view.DeckListActivity;
import com.ABCDeve1opers.flashbot.view.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Adapter class to display deck info (name and statistics) in the list view of DeckListActivity
 */
public class DeckInfoAdapter extends ArrayAdapter<DeckInfo> {

    private List<DeckInfo> deckInfoList;
    private Context context;

    public DeckInfoAdapter(Context context, List<DeckInfo> deckInfoList){
        super(context, R.layout.item_deck_info, deckInfoList);
        this.context = context;
        this.deckInfoList = deckInfoList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeckInfo deckInfo = deckInfoList.get(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View deckInfoView = inflater.inflate(R.layout.item_deck_info, parent, false);

        TextView viewName = (TextView) deckInfoView.findViewById(R.id.view_name);
        viewName.setText(deckInfo.getName());
        TextView viewNumCards = (TextView) deckInfoView.findViewById(R.id.view_num_cards);
        viewNumCards.setText(deckInfo.getNumCards());
        TextView viewNumHotCards = (TextView) deckInfoView.findViewById(R.id.view_num_hot_cards);
        viewNumHotCards.setText(deckInfo.getNumHotCards());
        TextView viewNumKnownCards = (TextView) deckInfoView.findViewById(R.id.view_num_known_cards);
        viewNumKnownCards.setText(deckInfo.getNumKnownCards());

        return deckInfoView;
    }

    /**
     * Filter the search results of the list of decks
     *
     * @param charText the partial query
     */
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        DeckListActivity.getDeckList().clear();
        DeckCollection deckCollection = new DeckCollection();

        // Reinitialize collection
        try {
            deckCollection.reload(DeckCollection.flashBotDir);
        } catch (IOException e) {
            Log.e(this.getClass().toString(), "Error reloading deck for getting deckInfos");
            e.printStackTrace();
        }
        List<DeckInfo> deckInfos = deckCollection.getDeckInfos();

        // Either filter the decks or show all decks if there is no query
        if (charText.length() == 0) {
            DeckListActivity.getDeckList().addAll(deckInfos);
        } else {
            for (DeckInfo di : deckInfos) {
                if (di.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    DeckListActivity.getDeckList().add(di);
                }
            }
        }
        // reload list for view
        notifyDataSetChanged();
    }
}
