package com.babasoft.vocabs;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import static com.babasoft.vocabs.WordDB.*;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.RecyclerViewHolder> {

    static class RecyclerViewHolder extends RecyclerView.ViewHolder {

        private TextView label;
        private CheckBox checkBox;
        private TextView listID;

        RecyclerViewHolder(View view) {
            super(view);
            label       = view.findViewById(R.id.listName);
            checkBox    = view.findViewById(R.id.listSelected);
            listID      = view.findViewById(R.id.listId);
        }

    }
    WordDB mDB;
    private ArrayList<WordList> mList;
    private Context mContext;
    private SparseBooleanArray mSelectedItemsIds;


    public WordListAdapter(Context context, WordDB db) {
        mDB = db;
        mList = (ArrayList<WordList>) mDB.getWordLists();
        mContext = context;
        mSelectedItemsIds = new SparseBooleanArray();
        for(int i=0; i < mList.size(); i++) {
            mSelectedItemsIds.put(i, mList.get(i).selection == 1 ? true : false);
        }
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.word_list_row, viewGroup, false);
        return new RecyclerViewHolder(v);
    }


    @Override
    public void onBindViewHolder(WordListAdapter.RecyclerViewHolder holder, int position) {
        WordList l = mList.get(position);
        String label = l.title + " " + "[" + l.lang1 + "-" + l.lang2 + "]" + " " + "[" + l.count + "]";
        holder.label.setText(label);
        holder.checkBox.setChecked(mSelectedItemsIds.get(position));
        holder.listID.setText(String.valueOf(l.id));
        holder.checkBox.setOnClickListener(v -> checkCheckBox(position, !mSelectedItemsIds.get(position)));
        holder.label.setOnClickListener(v -> checkCheckBox(position, !mSelectedItemsIds.get(position)));
    }

    @Override
    public int getItemCount() {return (null != mList ? mList.size() : 0);}

    /**
     * Remove all elements from list and populate with new content from database
     **/
    public void refreshList() {
        mList.clear();
        mList = (ArrayList<WordList>) mDB.getWordLists();
        mSelectedItemsIds = new SparseBooleanArray();
        for(int i=0; i < mList.size(); i++) {
            mSelectedItemsIds.put(i, mList.get(i).selection == 1 ? true : false);
        }
        notifyDataSetChanged();
    }

    /**
     * Remove all checkbox Selection
     **/
    public void removeSelection() {
        Log.d(getClass().getName(), "removeSelection called");
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

    /**
     * Check the Checkbox if not checked
     **/
    public void checkCheckBox(int position, boolean value) {
        Log.d(getClass().getName(), "checkCheckBox called");
        if (value)
            mSelectedItemsIds.put(position, true);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
        mDB.updateWordListSelection(mList.get(position).id, value);
    }

    /**
     * Return the selected Checkbox IDs
     **/
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }


}