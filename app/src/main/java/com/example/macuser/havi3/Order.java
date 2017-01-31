package com.example.macuser.havi3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class Order extends Fragment {
    private Order.OrderFragmentListener listener;

    public interface OrderFragmentListener {
        void onOrderFragmentEvent(String price, String number);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Order.OrderFragmentListener)) {
            throw new UnsupportedOperationException("Listener is not Implementation");
        } else {
            listener = (Order.OrderFragmentListener) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_order, container, false);

        final EditText priceEditText = (EditText) view.findViewById(R.id.orderEditText2);
        final EditText orderNumberEditText = (EditText) view.findViewById(R.id.orderEditText3);

        final Spinner orderSpinner = (Spinner) view.findViewById(R.id.orderSpinner);
        mySpinnerAdapter<String> mySpinnerAdapter = new mySpinnerAdapter<>(view.getContext(), android.R.layout.simple_spinner_item);
        mySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mySpinnerAdapter.add("その他");
        for (List<String> list : Material.showMaterialList(view.getContext(), "", "")) {
            mySpinnerAdapter.add(list.get(1));
        }

        orderSpinner.setAdapter(mySpinnerAdapter);
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String name = orderSpinner.getSelectedItem().toString();

                if (!name.equals("その他")) {
                    int price = Material.getPrice(view.getContext(), name);
                    priceEditText.setText(String.valueOf(price));
                    priceEditText.setFocusable(false);
                    priceEditText.setEnabled(false);
                } else {
                    priceEditText.setText("");
                    priceEditText.setEnabled(true);
                    priceEditText.setFocusable(true);
                    priceEditText.setFocusableInTouchMode(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //何もしない
            }
        });

        Button btnPreserve = (Button) view.findViewById(R.id.orderButton);

        btnPreserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String price = priceEditText.getText().toString();
                final String number = orderNumberEditText.getText().toString();

                if (price.equals("")) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                    builder1.setTitle("注意")
                            .setIcon(R.drawable.warm)
                            .setMessage("単価が入力されていません")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
                if (number.equals("")) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                    builder1.setTitle("注意")
                            .setIcon(R.drawable.warm)
                            .setMessage("発注数が入力されていません")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }

                if (!price.equals("") && !number.equals("")) {
                    final Money money = new Money(getActivity().getApplicationContext(), true);

                    Toast.makeText(getActivity().getApplicationContext(), "保存しました。", Toast.LENGTH_SHORT).show();

                    if (orderSpinner.getSelectedItem().equals("その他")) {
                        AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                        builder2.setTitle("資材の登録")
                                .setIcon(android.R.drawable.ic_input_add)
                                .setMessage("この資材を一覧に登録しますか？")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (listener != null) {
                                            money.recordRemainingMoney(getActivity().getApplicationContext(), Integer.valueOf(price), Integer.valueOf(number));
                                            listener.onOrderFragmentEvent(price, number);
                                        }
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
                    } else {
                        Material.updatePriceByName(getActivity().getApplicationContext(), orderSpinner.getSelectedItem().toString(), Integer.valueOf(number));
                    }

                }
            }
        });

        return view;
    }

    private class mySpinnerAdapter<T> extends ArrayAdapter<T> {
        public mySpinnerAdapter(Context context, int layoutResourceId) {
            super(context, layoutResourceId);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            ((TextView) view).setGravity(Gravity.CENTER);
            return view;
        }
    }
}
