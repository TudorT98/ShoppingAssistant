package com.example.mapboxtutorial;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class ShoppingList extends AppCompatActivity {

    //array list for data
    ArrayList<String> list = new ArrayList<>();
    ListView list_view;
    ArrayAdapter arrayAdapter;
    ArrayList<String> usedProductTypes = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);
        //find view by id
        list_view = findViewById(R.id.shoppingList);
        arrayAdapter = new ArrayAdapter(ShoppingList.this, android.R.layout.simple_list_item_1, list);
        list_view.setAdapter(arrayAdapter);

        //
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                PopupMenu popupMenu = new PopupMenu(ShoppingList.this, view);
                popupMenu.getMenuInflater().inflate(R.menu.pop_up_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {

                            case R.id.item_update:
                                //function for update
                                AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingList.this);
                                View v = LayoutInflater.from(ShoppingList.this).inflate(R.layout.item_dialog, null, false);
                                builder.setTitle("Update Item");
                                final EditText editText = v.findViewById(R.id.etItemName);
                                editText.setText(list.get(position));


                                builder.setView(v);

                                builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!editText.getText().toString().isEmpty()) {
                                            list.set(position, editText.getText().toString().trim());
                                            arrayAdapter.notifyDataSetChanged();
                                            Toast.makeText(ShoppingList.this, "Item Updated!", Toast.LENGTH_SHORT).show();

                                        } else {
                                            editText.setError("add item here !");
                                        }
                                    }
                                });

                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });

                                builder.show();

                                break;

                            case R.id.item_del:
                                //fucntion for del
                                Toast.makeText(ShoppingList.this, "Item Deleted", Toast.LENGTH_SHORT).show();
                                list.remove(position);
                                arrayAdapter.notifyDataSetChanged();

                                break;

                        }

                        return true;
                    }
                });

                //don't forgot this
                popupMenu.show();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.add_product_menu:
                _addItem();
                break;
            case R.id.map_menu:
                Intent intent = new Intent(ShoppingList.this,MainActivity.class);
                intent.putExtra("types",usedProductTypes);
                ShoppingList.this.startActivity(intent);
                break;
            case R.id.shop_cart_menu:
                Toast.makeText(ShoppingList.this, "View your shopping list", Toast.LENGTH_LONG).show();
        }

        return true;
    }

    private void _addItem() {

        AlertDialog.Builder builder = new AlertDialog.Builder(ShoppingList.this);
        builder.setTitle("Add New Item");

        View v = LayoutInflater.from(ShoppingList.this).inflate(R.layout.item_dialog, null, false);

        builder.setView(v);
        //set spinner values
        Spinner spinner = v.findViewById(R.id.productType);
        ArrayList<String> types = new ArrayList<>();
        types.add("Food");
        types.add("Medicine");
        types.add("Clothing");
        types.add("Gas");

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(spinnerAdapter);



       /* spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String productType = parent.getItemAtPosition(position).toString();
                Toast.makeText(parent.getContext(), "Type selected is " + productType, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onNothingSelected(AdapterView <?> parent) {
                Toast.makeText(parent.getContext(), "Please select product type", Toast.LENGTH_LONG).show();
            }
        });*/

        final EditText etItemName = v.findViewById(R.id.etItemName);
        final EditText etItemPrice = v.findViewById(R.id.etItemPrice);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!etItemName.getText().toString().isEmpty() && !etItemPrice.getText().toString().isEmpty() && !spinner.getSelectedItem().toString().isEmpty()) {
                    Product product = new Product(etItemName.getText().toString(),spinner.getSelectedItem().toString(),Float.parseFloat(etItemPrice.getText().toString()));
                    list.add(product.toString());
                    usedProductTypes.add(product.getType());
                    arrayAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(ShoppingList.this, "Please fill all fields", Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();




    }
}
