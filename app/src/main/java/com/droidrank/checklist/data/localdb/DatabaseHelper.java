package com.droidrank.checklist.data.localdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.droidrank.checklist.Utils.Constants;
import com.droidrank.checklist.model.CheckListItem;

import java.util.ArrayList;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by mutha on 23/10/16.
 * Database helper class for list
 * doing all the database queries on background thread
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String CREATE_TABLE_ITEMS = "create table items " + " (" +
            "item_name" + " TEXT PRIMARY KEY," +
            "ischecked" + " INT)";

    private static DatabaseHelper databaseHelper;
    private static final int DATABASE_VERSION = 1;

    // Have only one instance of database helper class
    public static synchronized DatabaseHelper getHelper(Context context){
        if(databaseHelper == null){
            databaseHelper = new DatabaseHelper(context);
        }
        return databaseHelper;
    }

    private DatabaseHelper(Context context) {
        super(context, Constants.ITEMS_DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public Observable<String> insertItem(final CheckListItem checkListItem) throws SQLiteException {
        return Observable.just(checkListItem).flatMap(new Func1<CheckListItem, Observable<? extends String>>() {
            @Override
            public Observable<? extends String> call(CheckListItem newCheckListItem) {
                SQLiteDatabase db = getWritableDatabase();
                ContentValues contentValues = getContentValues(checkListItem);
                CheckListItem tempCheckListItem = getItem(checkListItem.getItemName());
                //If the item doesn't exist, insert it into the database
                if (tempCheckListItem == null) {
                    db.insert("items", null, contentValues);
                } else {
                    return Observable.just(null);
                }
                return Observable.just(checkListItem.getItemName());
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
    }

    @NonNull
    private ContentValues getContentValues(CheckListItem checkListItem) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("item_name", checkListItem.getItemName());
        if(checkListItem.isChecked()) {
            contentValues.put("ischecked", 1);
        } else {
            contentValues.put("ischecked", 0);
        }
        return contentValues;
    }

    public Observable<ArrayList<CheckListItem>> getItemList() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + Constants.TABLE_NAME, null);
        ArrayList<CheckListItem> checkListItems = new ArrayList<>();
        CheckListItem checkListItem;
        if(cursor.moveToFirst()){
            checkListItem = getItemInfoFromCursor(cursor);
            checkListItems.add(checkListItem);
        }
        while(cursor.moveToNext()) {
            checkListItem = getItemInfoFromCursor(cursor);
            checkListItems.add(checkListItem);
        }
        closeCursor(cursor);
        return Observable.just(checkListItems);
    }

    @NonNull
    private void closeCursor(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @NonNull
    private CheckListItem getItemInfoFromCursor(Cursor cursor) {
        CheckListItem checkListItem = new CheckListItem();
        checkListItem.setItemName(cursor.getString(cursor.getColumnIndex("item_name")));

        if(cursor.getInt(cursor.getColumnIndex("ischecked")) == 0){
            checkListItem.setChecked(false);
        } else {
            checkListItem.setChecked(true);
        }
        return checkListItem;
    }

    public Observable<String> deleteItem(final CheckListItem checkListItem) throws SQLiteException {
        return Observable.just(checkListItem).flatMap(new Func1<CheckListItem,
                Observable<? extends String>>() {
            @Override
            public Observable<? extends String> call(CheckListItem newCheckListItem) {
                SQLiteDatabase db = getWritableDatabase();
                CheckListItem tempCheckListItem = getItem(checkListItem.getItemName());
                //If the item doesn't exist, insert it into the database
                if (tempCheckListItem != null) {
                    db.delete( Constants.TABLE_NAME, "item_name" + " = ?",
                            new String[] {checkListItem.getItemName()});
                } else {
                    return Observable.just(null);
                }
                return Observable.just(checkListItem.getItemName());
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
    }

    private CheckListItem getItem(String itemName) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + Constants.TABLE_NAME
                + " where " + "item_name" + " = ?", new String[]{itemName});
        if (cursor != null && cursor.moveToFirst()) {
            CheckListItem checkListItem = getItemInfoFromCursor(cursor);
            closeCursor(cursor);
            return checkListItem;
        }
        closeCursor(cursor);
        return null;
    }

    public Observable<String> updateItemStatus(final CheckListItem checkListItem) throws SQLiteException {
        return Observable.just(checkListItem).flatMap(new Func1<CheckListItem,
                Observable<? extends String>>() {
            @Override
            public Observable<? extends String> call(CheckListItem newCheckListItem) {
                SQLiteDatabase db = getWritableDatabase();
                ContentValues contentValues = getContentValues(checkListItem);
                CheckListItem tempCheckListItem = getItem(checkListItem.getItemName());
                //If the item doesn't exist, insert it into the database
                if (tempCheckListItem != null) {
                    db.update(Constants.TABLE_NAME, contentValues, "item_name" + " = ?",
                            new String[] {checkListItem.getItemName()});
                } else {
                    return Observable.just(null);
                }
                return Observable.just(checkListItem.getItemName());
            }
        }).subscribeOn(Schedulers.io()).observeOn(Schedulers.io());
    }
}
