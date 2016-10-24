package com.droidrank.checklist;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.droidrank.checklist.data.localdb.DatabaseHelper;
import com.droidrank.checklist.model.CheckListItem;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class AddNewItem extends AppCompatActivity {

    // To take the user input for the new item
    EditText itemName;
    // To add the new item to the list
    Button addItem;
    // To cancel this task
    Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_item);
        itemName = (EditText) findViewById(R.id.et_item_name);
        cancel = (Button) findViewById(R.id.bt_cancel);
        addItem = (Button) findViewById(R.id.bt_ok);
        addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 *Save the new item, if it does not exist in the list
                 *else display appropriate error message
                 */
                // finish the current activity
                if (itemName == null || itemName.getText() == null
                        || "".equals(itemName.getText().toString())) {
                    Toast.makeText(getApplicationContext(),
                            "Empty Item! Check the name!"
                            , Toast.LENGTH_SHORT).show();
                } else {
                    CheckListItem checkListItem = new CheckListItem();
                    checkListItem.setItemName(itemName.getText().toString());
                    DatabaseHelper.getHelper(getApplicationContext())
                            .insertItem(checkListItem)
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<String>() {
                                @Override
                                public void call(String s) {
                                    if (s == null) {
                                        Toast.makeText(getApplicationContext(),
                                                getApplicationContext()
                                                        .getResources()
                                                        .getString(R.string.item_already_exists_message)
                                                , Toast.LENGTH_SHORT).show();
                                    } else {
                                        finish();
                                    }
                                }
                            });
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 * Do nothing and close this activity
                 */
                finish();
            }
        });
    }
}
