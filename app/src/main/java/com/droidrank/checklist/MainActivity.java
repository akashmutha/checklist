package com.droidrank.checklist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.droidrank.checklist.Adapter.CheckListAdapter;
import com.droidrank.checklist.Utils.Constants;
import com.droidrank.checklist.data.localdb.DatabaseHelper;
import com.droidrank.checklist.model.CheckListItem;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    // To display the items in the list
    ListView listView;
    // To add a new item
    Button addNew;
    private ArrayList<CheckListItem> checkListItems;
    private CheckListAdapter listItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = this.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE);

        listView = (ListView) findViewById(R.id.list_view);
        addNew = (Button) findViewById(R.id.bt_add_new_item);
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * Add a new item to the checklist
                 */
                Intent intent = new Intent(MainActivity.this, AddNewItem.class);
                startActivity(intent);
            }
        });

        // Only first time read from default array and then put it in db
        // then afterwards only handle from db
        if(!prefs.getBoolean(Constants.IS_FIRST_TIME_READ, false)) {
            putDefaultItemsToDB();
            prefs.edit().putBoolean(Constants.IS_FIRST_TIME_READ, true).commit();
        }
        listItemAdapter = new CheckListAdapter(checkListItems);
        listView.setAdapter(listItemAdapter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //Refresh the checklist
        refreshList();
    }

    private ArrayList<CheckListItem> putDefaultItemsToDB(){
        ArrayList<CheckListItem> checkListItems = new ArrayList<>();
        String [] defaultItems = getApplicationContext().getResources()
                .getStringArray(R.array.default_items);
        for(String itemName : defaultItems){
            CheckListItem listItem = new CheckListItem();
            listItem.setChecked(false);
            listItem.setItemName(itemName);
            checkListItems.add(listItem);
            DatabaseHelper.getHelper(getApplicationContext())
                    .insertItem(listItem)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<String>() {
                        @Override
                        public void call(String s) {
                            refreshList();
                        }
                    });
        }
        return checkListItems;
    }

    private void refreshList(){
        // get the list from db on background thread
        // and make the view modification on the main thread
        DatabaseHelper.getHelper(getApplicationContext())
                .getItemList()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<CheckListItem>>() {
                    // this runs on the main thread as observe on is called on main thread
                    @Override
                    public void call(ArrayList<CheckListItem> checkListItems1) {
                        listItemAdapter.setList(checkListItems1);
                        listItemAdapter.notifyDataSetChanged();
                    }
                });
    }
}
