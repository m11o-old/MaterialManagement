package com.example.macuser.havi3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Calendar;
import java.util.List;


public class Budget extends Fragment {
    private TextView budgetText = null;
    private Money money;


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        money = new Money(getActivity().getApplicationContext(), true);

        ListView listView = (ListView) view.findViewById(R.id.BudgetListView);
        myBudgetAdapter adapter = new myBudgetAdapter(getActivity().getApplicationContext(), R.layout.budget_list_view_content, Material.getOrderList(getActivity().getApplicationContext()));
        listView.setAdapter(adapter);

        budgetText = (TextView) view.findViewById(R.id.BudgetShowTextView);
        budgetText.setText(String.valueOf(money.getBudget()));

        StringBuilder sb = new StringBuilder();
        int use = money.getUse(getActivity().getApplicationContext());
        if (use == 0) {
            sb.append(String.valueOf(use));
        } else {
            sb.append("-").append(String.valueOf(use));
        }

        final TextView useText = (TextView) view.findViewById(R.id.UseTextView);
        useText.setText(sb.toString());

        final TextView surplusText = (TextView)view.findViewById(R.id.SurplusShowTextView);
        surplusText.setText(String.valueOf(money.getSurplus(getActivity().getApplicationContext())));

        final Button btnBudgetSetting = (Button) view.findViewById(R.id.BudgetButton);
        btnBudgetSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (money.getBudget() != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("注意")
                            .setIcon(R.drawable.warm)
                            .setMessage("今月の予算はすでに登録されていますが、予算を編集しますか？")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    budgetSettingDialog();
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create().show();
                } else {
                    budgetSettingDialog();
                }

            }
        });

        return view;

    }

    private void budgetSettingDialog() {
        LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.dialog_fragment_budget_setting, null);

        final NumberPicker monthNumberPicker = (NumberPicker) layout.findViewById(R.id.monthNumberPicker);
        monthNumberPicker.setMaxValue(12);
        monthNumberPicker.setMinValue(1);

        Calendar calendar = Calendar.getInstance();
        final int monthCalendar = calendar.get(Calendar.MONTH);

        monthNumberPicker.setValue(monthCalendar + 1);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("予算の登録")
                .setIcon(android.R.drawable.ic_input_add)
                .setView(layout)
                .setPositiveButton("登録", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText budgetSetting = (EditText)layout.findViewById(R.id.budgetSettingEditText);

                        int month = monthNumberPicker.getValue();
                        int budget = Integer.valueOf(budgetSetting.getText().toString());
                        Money.budgetRecord(getActivity().getApplicationContext(), budget, month);

                        if (month == monthCalendar) {
                            money.allRecordRemainingMoney(getActivity().getApplicationContext());
                        }


                        Toast.makeText(getActivity().getApplicationContext(), "登録しました。\n更新してください。", Toast.LENGTH_SHORT).show();

                        dialog.dismiss();

                        FragmentManager fm = getFragmentManager();
                        fm.beginTransaction()
                                .replace(R.id.container, Fragment.instantiate(getActivity().getApplicationContext(), "com.example.macuser.havi3.Budget"))
                                .commit();
                    }
                })
                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private class myBudgetAdapter extends ArrayAdapter<List<String>> {
        List<String> list;
        LayoutInflater layoutInflater;

        myBudgetAdapter(Context context, int textViewResourceId, List<List<String>> list) {
            super(context, textViewResourceId, list);
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            list = (List<String>) getItem(position);
            View view;

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.budget_list_view_content, null);
            } else {
                view = convertView;
            }

            TextView txtBudget = (TextView) view.findViewById(R.id.BudgetListViewText1);
            TextView txtUse = (TextView) view.findViewById(R.id.BudgetListViewText2);
            TextView txtSurplus = (TextView) view.findViewById(R.id.BudgetListViewText3);

            txtBudget.setText(list.get(0));
            txtUse.setText(list.get(1));
            txtSurplus.setText(list.get(2));

            return view;
        }
    }
}
