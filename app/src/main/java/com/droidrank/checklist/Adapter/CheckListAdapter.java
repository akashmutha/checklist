package com.droidrank.checklist.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.droidrank.checklist.R;
import com.droidrank.checklist.data.localdb.DatabaseHelper;
import com.droidrank.checklist.model.CheckListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by mutha on 23/10/16.
 */

public class CheckListAdapter extends BaseAdapter {

    private ArrayList<CheckListItem> checkListItems;

    public CheckListAdapter(final ArrayList<CheckListItem> checkListItems){
        this.checkListItems = checkListItems;
    }

    @Override
    public int getCount() {
        return checkListItems != null ? checkListItems.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return checkListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolderItem viewHolderItem;
        final CheckListItem listItem = checkListItems.get(position);

        if(convertView == null){
            LayoutInflater inflater = ((Activity)parent.getContext()).getLayoutInflater();
            convertView = inflater.inflate(R.layout.check_list_item, parent, false);
            viewHolderItem = new ViewHolderItem();
            viewHolderItem.textView = (TextView) convertView.findViewById(R.id.tv_item_name);
            viewHolderItem.checkBox = (CheckBox) convertView.findViewById(R.id.cb_item_status);
            viewHolderItem.button = (Button) convertView.findViewById(R.id.bt_item_delete);
            viewHolderItem.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkListItems.remove(position);
                    DatabaseHelper.getHelper(parent.getContext())
                            .deleteItem(listItem)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String s) {
                                    notifyDataSetChanged();
                                }
                            });
                }
            });
            convertView.setTag(viewHolderItem);
        } else {
            viewHolderItem = (ViewHolderItem) convertView.getTag();
        }

        if(listItem != null) {
            viewHolderItem.textView.setText(listItem.getItemName());
            // If we don't remove the listener it will go in
            // infinite loop while someone clicks on checkbox
            // we will change the list and it will invoke the listener again
            viewHolderItem.checkBox.setOnCheckedChangeListener(null);
            viewHolderItem.checkBox.setChecked(listItem.isChecked());
        }

        viewHolderItem.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listItem.setChecked(!listItem.isChecked());
                DatabaseHelper.getHelper(parent.getContext())
                        .updateItemStatus(listItem)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String s) {
                                rearrangeList();
                                notifyDataSetChanged();
                            }
                        });
            }
        });
        return convertView;
    }

    public void setList(ArrayList<CheckListItem> checkListItems){
        this.checkListItems = checkListItems;
        rearrangeList();
    }

    private void rearrangeList(){
        ArrayList<CheckListItem> checkedListItems = new ArrayList<>();
        ArrayList<CheckListItem> unCheckedListItems = new ArrayList<>();
        for(CheckListItem checkListItem : checkListItems){
            if(checkListItem.isChecked()){
                checkedListItems.add(checkListItem);
            } else {
                unCheckedListItems.add(checkListItem);
            }
        }
        sortList(checkedListItems);
        sortList(unCheckedListItems);
        checkListItems.clear();
        checkListItems.addAll(unCheckedListItems);
        checkListItems.addAll(checkedListItems);
    }

    private void sortList(ArrayList<CheckListItem> checkListItems){
        Collections.sort(checkListItems, new Comparator<CheckListItem>() {
            @Override
            public int compare(CheckListItem lhs, CheckListItem rhs) {
                return lhs.getItemName().compareTo(rhs.getItemName());
            }
        });
    }

    static class ViewHolderItem {
        TextView textView;
        CheckBox checkBox;
        Button button;
    }
}
