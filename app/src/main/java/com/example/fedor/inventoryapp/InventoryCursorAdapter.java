package com.example.fedor.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fedor.inventoryapp.data.InventoryContract.InventoryEntry;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of inventory data as its data source. This adapter knows
 * how to create list items for each row of inventory data in the {@link Cursor}.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    /** Tag for the log messages */
    public static final String LOG_TAG = InventoryCursorAdapter.class.getSimpleName();

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name_catalog);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_catalog);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_catalog);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        final int itemStockId = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));

        // Find the columns of item attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);

        // Read the item attributes from the Cursor for the current item
        String itemName = cursor.getString(nameColumnIndex);
        final int itemQuantity = cursor.getInt(quantityColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        itemPrice = itemPrice + "€";

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(itemName);
        quantityTextView.setText(String.valueOf(itemQuantity));
        priceTextView.setText(itemPrice);

        // set OnClickListener on Sale Button
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                purchaseItem(itemStockId, itemQuantity, context);
            }
        });
    }

    /**
     * Method to simulate a sale on item.
     * Decrease item Quantity by 1 and save in DB
     */
    private void purchaseItem(int itemStockId, int itemQuantity, Context context) {
        Uri itemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, itemStockId);
        if (itemQuantity > 0) {
            ContentValues values = new ContentValues();
            int newQuantity = itemQuantity - 1;
            values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, newQuantity);
            context.getContentResolver().update(itemUri, values, null, null);
        } else {
            Toast.makeText(context, "This item is out of stock.", Toast.LENGTH_SHORT).show();

        }
    }

}