package com.example.macuser.havi3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.GpsStatus;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.KeyListener;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MaterialListContainer extends AppCompatActivity {
    private int position;

    EditText Number;
    EditText Name;
    EditText price;
    Spinner expendable;
    Spinner place1;
    Spinner place2;
    EditText Case;
    EditText Bag;
    EditText Substance;
    EditText thisMonth;
    EditText lastMonth;
    EditText theMonthBeforeLast;
    EditText stock;

    Button btnChange;
    Button btnEnd;

    String[] expendableSpinner = {"A", "B"};
    String[] placeSpinner = {"", "イナバ", "倉庫", "シンク", "厨房", "カウンター", "クルールーム"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_list_container);

        Intent i = getIntent();
        position = i.getIntExtra("position", 0);

        Number = (EditText)this.findViewById(R.id.MaterialNumberEditText);
        Name = (EditText)this.findViewById(R.id.MaterialNameEditText);
        price = (EditText)this.findViewById(R.id.MaterialPriceEditText);
        expendable = (Spinner) this.findViewById(R.id.MaterialExpendableSpinner);
        place1 = (Spinner) this.findViewById(R.id.MaterialPlace1Spinner);
        place2 = (Spinner) this.findViewById(R.id.MaterialPlace2Spinner);
        Case = (EditText)this.findViewById(R.id.MaterialCaseEditText);
        Bag = (EditText)this.findViewById(R.id.MaterialBagEditText);
        Substance = (EditText)this.findViewById(R.id.MaterialSubstanceEditText);
        thisMonth = (EditText)this.findViewById(R.id.MaterialThisEditText);
        lastMonth = (EditText)this.findViewById(R.id.MaterialLastEditText);
        theMonthBeforeLast = (EditText)this.findViewById(R.id.MaterialTheMonthBeforeLastEditText);
        stock = (EditText)this.findViewById(R.id.MaterialStockEditText);

        setFalseFocusable();

        btnChange = (Button) findViewById(R.id.changeContainButton);
        btnEnd = (Button) findViewById(R.id.EndContainButton);

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnEnd.getText().toString().equals("終了")){
                    btnEnd.setText("保存");
                    setTrueFocusable();
                }
            }
        });

        ArrayAdapter<String> expendableAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, expendableSpinner);
        expendableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        expendable.setAdapter(expendableAdapter);

        ArrayAdapter<String> place1Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, placeSpinner);
        place1Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> place2Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, placeSpinner);
        place2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        place1.setAdapter(place1Adapter);
        place2.setAdapter(place2Adapter);

        /**
         * materialContent indexと内容
         * 0.商品番号
         * 1.商品名
         * 2.単価
         * 3.消耗品の種類
         * 4.場所1
         * 5.場所2
         * 6.内包量:ケース
         * 7.内包量:中包
         * 8.内包量:個数
         * 9.使用数:今月
         * 10.使用数:先月
         * 11.使用数:先々月
         * 12.在庫
         */

        String numberExtra;
        final String nameExtra;
        String priceExtra;
        final String numberExtra_2;

        switch(position) {

            //materialListから資材の詳細表示をするとき
            case 0:
                numberExtra = i.getStringExtra("number");
                nameExtra = i.getStringExtra("name");

                final Material material = new Material(numberExtra, nameExtra);
                List<String> materialContent = material.showContent(getApplicationContext());

                btnEnd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (btnEnd.getText().toString().equals("保存")) {
                            btnEnd.setText("終了");
                            material.updateMaterialContain(getApplicationContext(), getTextInEditText());
                            Toast.makeText(getApplicationContext(), "保存しました。", Toast.LENGTH_SHORT).show();
                            setFalseFocusable();
                        } else {
                            finish();
                        }
                    }
                });

                setTextInEditText(materialContent);

                break;
            //新規で注文したときに新しく資材として、登録する場合
            case 1:
                priceExtra = i.getStringExtra("price");
                numberExtra_2 = i.getStringExtra("number");
                btnEnd.setText("保存");

                price.setText(priceExtra);

                btnEnd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (btnEnd.getText().toString().equals("保存")) {
                            btnEnd.setText("終了");
                            if (judgementNull()) {
                                Material.recordMaterial(getApplicationContext(), getTextInEditText());
                                Material.updatePriceByName(getApplicationContext(), Name.getText().toString(), Integer.valueOf(numberExtra_2));
                                Toast.makeText(getApplicationContext(), "登録しました。", Toast.LENGTH_SHORT).show();
                                setFalseFocusable();
                            } else {
                                showOutOfOrderAlertDialog();
                            }
                        } else {
                            finish();
                        }
                    }
                });

                setTrueFocusable();

                break;
            //新規で資材を登録する場合
            case 2:
                setTrueFocusable();

                btnEnd.setText("保存");

                btnEnd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (btnEnd.getText().toString().equals("保存")) {
                            btnEnd.setText("終了");
                            if (judgementNull()) {
                                Material.recordMaterial(getApplicationContext(), getTextInEditText());
                                Toast.makeText(getApplicationContext(), "登録しました。", Toast.LENGTH_SHORT).show();
                                setFalseFocusable();
                            } else {
                                showOutOfOrderAlertDialog();
                            }
                        } else {
                            finish();
                        }
                    }
                });
                break;

        }
    }

    private void showOutOfOrderAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MaterialListContainer.this);
        builder.setTitle("注意")
                .setIcon(R.drawable.warm)
                .setMessage("入力が不足している部分があります。")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        btnEnd.setText("保存");
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private boolean judgementNull() {
        if (Number.getText().toString().equals("")) {
            return false;
        } else if (Name.getText().toString().equals("")) {
            return false;
        } else if (price.getText().toString().equals("")) {
            return false;
        } else if (expendable.getSelectedItem().toString().equals("")) {
            return false;
        } else if (place1.getSelectedItem().toString().equals("")) {
            return false;
        } else if (Case.getText().toString().equals("")) {
            return false;
        } else if (Bag.getText().toString().equals("")) {
            return false;
        } else if (Substance.getText().toString().equals("")) {
            return false;
        }

        return true;
    }

    private void setTextInEditText(List<String> materialContent) {
        Number.setText(materialContent.get(0));
        Name.setText(materialContent.get(1));
        price.setText(materialContent.get(2));
        expendable.setSelection(getExpendableSelectionPosition(materialContent.get(3)));
        place1.setSelection(getPlaceSelectionPosition(materialContent.get(4)));
        place2.setSelection(getPlaceSelectionPosition(materialContent.get(5)));
        Case.setText(materialContent.get(6));
        Bag.setText(materialContent.get(7));
        Substance.setText(materialContent.get(8));
        thisMonth.setText(materialContent.get(9));
        lastMonth.setText(materialContent.get(10));
        theMonthBeforeLast.setText(materialContent.get(11));
        stock.setText(materialContent.get(12));
    }

    private void setTrueFocusable() {
        Number.setEnabled(true);
        Number.setFocusable(true);
        Number.setFocusableInTouchMode(true);
        Name.setEnabled(true);
        Name.setFocusable(true);
        Name.setFocusableInTouchMode(true);
        price.setEnabled(true);
        price.setFocusable(true);
        price.setFocusableInTouchMode(true);
        expendable.setEnabled(true);
        expendable.setFocusable(true);
        expendable.setFocusableInTouchMode(true);
        place1.setEnabled(true);
        place1.setFocusable(true);
        place1.setFocusableInTouchMode(true);
        place2.setEnabled(true);
        place2.setFocusable(true);
        place2.setFocusableInTouchMode(true);
        Case.setEnabled(true);
        Case.setFocusable(true);
        Case.setFocusableInTouchMode(true);
        Bag.setEnabled(true);
        Bag.setFocusable(true);
        Bag.setFocusableInTouchMode(true);
        Substance.setEnabled(true);
        Substance.setFocusable(true);
        Substance.setFocusableInTouchMode(true);
    }

    private void setFalseFocusable() {
        Number.setEnabled(false);
        Number.setFocusable(false);
        Name.setEnabled(false);
        Name.setFocusable(false);
        price.setEnabled(false);
        price.setFocusable(false);
        expendable.setEnabled(false);
        expendable.setFocusable(false);
        place1.setEnabled(false);
        place1.setFocusable(false);
        place2.setEnabled(false);
        place2.setFocusable(false);
        Case.setEnabled(false);
        Case.setFocusable(false);
        Bag.setEnabled(false);
        Bag.setFocusable(false);
        Substance.setEnabled(false);
        Substance.setFocusable(false);
        thisMonth.setEnabled(false);
        thisMonth.setFocusable(false);
        lastMonth.setEnabled(false);
        lastMonth.setFocusable(false);
        theMonthBeforeLast.setEnabled(false);
        theMonthBeforeLast.setFocusable(false);
        stock.setEnabled(false);
        stock.setFocusable(false);
    }

    private List<String> getTextInEditText() {
        List<String> list = new ArrayList<>(13);
        list.add(Number.getText().toString());
        list.add(Name.getText().toString());
        list.add(price.getText().toString());
        list.add(expendable.getSelectedItem().toString());
        list.add(place1.getSelectedItem().toString());
        list.add(place2.getSelectedItem().toString());
        list.add(Case.getText().toString());
        list.add(Bag.getText().toString());
        list.add(Substance.getText().toString());
        if (position == 1 || position == 2) {
            list.add("0.0");
            list.add("0.0");
            list.add("0.0");
            list.add("0.0");
        }


        return list;
    }

    private int getExpendableSelectionPosition(String column) {
        for (int i = 0; i < expendableSpinner.length; i++) {
            if (expendableSpinner[i].equals(column)) {
                return i;
            }
        }

        return 0;
    }

    private int getPlaceSelectionPosition(String column) {
        for (int i = 0; i < placeSpinner.length; i++) {
            if (placeSpinner[i].equals(column)) {
                return i;
            }
        }
        return 0;
    }
}
