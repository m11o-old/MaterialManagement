package com.example.macuser.havi3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Order extends Fragment {
    private Order.OrderFragmentListener listener;

    public interface OrderFragmentListener {
        void onOrderFragmentEvent(String name, String price);
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

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("注意")
                .setIcon(R.drawable.warm)
                .setMessage("このページは登録されていない資材の発注を入力するページです。\n\n資材一覧を確認済みですか？")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FragmentManager manager = getFragmentManager();
                        manager.beginTransaction()
                                .replace(R.id.container, Fragment.instantiate(getActivity().getApplicationContext(), "com.example.macuser.havi3.MaterialList"))
                                .commit();
                    }
                })
                .create().show();

        final EditText nameEditText = (EditText) view.findViewById(R.id.orderEditText1);
        final EditText priceEditText = (EditText) view.findViewById(R.id.orderEditText2);
        final EditText orderNumberEditText = (EditText) view.findViewById(R.id.orderEditText3);

        Button btnPreserve = (Button) view.findViewById(R.id.orderButton);

        btnPreserve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = nameEditText.getText().toString();
                final String price = priceEditText.getText().toString();
                String number = orderNumberEditText.getText().toString();

                if (name.equals("")) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                    builder1.setTitle("注意")
                            .setIcon(R.drawable.warm)
                            .setMessage("資材名が入力されていません")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
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

                if (!name.equals("") && !price.equals("") && !number.equals("")) {
                    Money money = new Money(getActivity().getApplicationContext(), true);
                    //保存されない
                    money.recordRemainingMoney(getActivity().getApplicationContext(), Integer.valueOf(price), Integer.valueOf(number));

                    Toast.makeText(getActivity().getApplicationContext(), "保存しました。", Toast.LENGTH_SHORT).show();

                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getContext());
                    builder2.setTitle("資材の登録")
                            .setIcon(android.R.drawable.ic_input_add)
                            .setMessage("この資材を一覧に登録しますか？")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (listener != null) {
                                        listener.onOrderFragmentEvent(name, price);
                                    }
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                }
            }
        });

        return view;
    }
}
