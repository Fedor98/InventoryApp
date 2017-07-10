/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.fedor.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fedor.inventoryapp.data.InventoryContract.InventoryEntry;

import static com.example.fedor.inventoryapp.R.id.price;

/**
 * Displays details of item that wes selected in the CatalogActivity.
 */
public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = DetailActivity.class.getSimpleName();

    /**
     * Identifier for the inventory data loader
     */
    private static final int EXISTING_INVENTORY_LOADER = 0;

    /**
     * Identifier for the permission to read external storage
     */
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int PICK_IMAGE_REQUEST = 0;

    private static final String STATE_URI = "STATE_URI";
    ImageView imageView;
    Uri pictureUri;
    /**
     * Content URI for the existing item (null if it's a new item)
     */
    private Uri mCurrentItemUri;
    /**
     * TextView to show item's name
     */
    private TextView mNameTextView;
    /**
     * TextView to show item's price
     */
    private TextView mPriceTextView;
    /**
     * TextView to show item's quantity
     */
    private TextView mQuantityTextView;
    /**
     * TextView to show item's supplier
     */
    private TextView mSupplierTextView;
    /**
     * Boolean flag that keeps track of whether the item has been edited (true) or not (false)
     */
    private boolean mItemHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Examine the intent that was used to launch this activity,
        // We're creating a new item.
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        // Change app bar to say "View Item details"
        setTitle(getString(R.string.detail_activity_title_detail_item));

        // Initialize a loader to read the item data from the database
        // and display the current values in the editor
        getLoaderManager().initLoader(EXISTING_INVENTORY_LOADER, null, this);

        // Find all relevant views that we will need to read info from
        mNameTextView = (TextView) findViewById(R.id.name);
        mPriceTextView = (TextView) findViewById(price);
        mQuantityTextView = (TextView) findViewById(R.id.quantity);
        mSupplierTextView = (TextView) findViewById(R.id.supplier);
        imageView = (ImageView) findViewById(R.id.image_view_holder);

        // Find the Button which will increase the item's quantity by 1
        Button increaseButton = (Button) findViewById(R.id.increase_button);

        // Setup the item click listener
        increaseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuantityTextView.getText().toString());
                quantity = quantity + 1;
                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);
                int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);
                // Show a toast message depending on whether or not the decreasement was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(DetailActivity.this, "Error with decreasing quantity",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(DetailActivity.this, "Quantity decreased by 1",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Find the Button which will decrease the item's quantity by 1
        Button decreaseButton = (Button) findViewById(R.id.decrease_button);

        // Setup the item click listener
        decreaseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int quantity = Integer.parseInt(mQuantityTextView.getText().toString());
                if (quantity > 0) {
                    quantity = quantity - 1;

                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);

                    int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

                    // Show a toast message depending on whether or not the decreasement was successful.
                    if (rowsAffected == 0) {
                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(DetailActivity.this, "Error with decreasing quantity",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the update was successful and we can display a toast.
                        Toast.makeText(DetailActivity.this, "Quantity decreased by 1",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DetailActivity.this,
                            "Cannot set quantity to a value lower than 0",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_detail file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_shop:
                int quantity = Integer.parseInt(mQuantityTextView.getText().toString());
                if (quantity > 0) {
                    quantity = quantity - 1;

                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);

                    int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

                    // Show a toast message depending on whether or not the purchase was successful.
                    if (rowsAffected == 0) {
                        // If no rows were affected, then there was an error with the purchase.
                        Toast.makeText(DetailActivity.this, "Error with purchasing product",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        String itemName = mNameTextView.getText().toString();
                        String itemPrice = mPriceTextView.getText().toString();
                        String itemSupplier = mSupplierTextView.getText().toString();

                        //Create the order summary
                        String priceMessage = getString(R.string.init_order_summary, itemName);
                        priceMessage += getString(R.string.price_order_summary, itemPrice);
                        priceMessage += getString(R.string.supplier_order_summary, itemSupplier);

                    /*
                     Use an intent to launch an email app.
                     Send the order summary in the email body.
                    */
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_summary_email_subject));
                        intent.putExtra(Intent.EXTRA_TEXT, priceMessage);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }

                } else {
                    Toast.makeText(DetailActivity.this,
                            "Item sold out",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the detail view shows all item attributes, define a projection that contains
        // all columns from the inventory table
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_SUPPLIER,
                InventoryEntry.COLUMN_ITEM_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,       // Parent activity context
                mCurrentItemUri,                // Query the content URI for the current item
                projection,                     // Columns to include in the resulting Cursor
                null,                           // No selection clause
                null,                           // No selection arguments
                null);                          // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_SUPPLIER);
            int imageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_ITEM_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String item_image = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameTextView.setText(name);
            mPriceTextView.setText(Integer.toString(price) + "€");
            mQuantityTextView.setText(Integer.toString(quantity));
            mSupplierTextView.setText(supplier);
            if (TextUtils.isEmpty(item_image)) {
                imageView.setImageURI(Uri.parse
                        ("android.resource://com.example.fedor.inventoryapp/res/drawable/empty_database"));
            } else {
                imageView.setImageURI(Uri.parse(item_image));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameTextView.setText("");
        mPriceTextView.setText("€");
        mQuantityTextView.setText("");
        mSupplierTextView.setText("");
        imageView.setImageDrawable(null);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}