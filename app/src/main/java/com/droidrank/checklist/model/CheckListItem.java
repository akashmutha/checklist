package com.droidrank.checklist.model;

/**
 * Created by mutha on 27/10/16.
 */

public class CheckListItem {

    private String itemName;
    private boolean isChecked;

    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }
}
