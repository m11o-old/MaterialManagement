package com.example.macuser.havi3;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Map;


public class OrderList extends Fragment {
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_order_list, container, false);

        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH) + 1;

        TextView txtMonth = (TextView) view.findViewById(R.id.orderListMonthTextView);
        txtMonth.setText(String.valueOf(month));

        ListView listView = (ListView) view.findViewById(R.id.OrderListListView);
        myAdapter adapter = new myAdapter(getActivity().getApplicationContext(), R.layout.order_list_list_text1, Material.getOrder(getActivity().getApplicationContext()));
        listView.setAdapter(adapter);

        return view;
    }

    private class myAdapter extends ArrayAdapter<List<String>> {
        List<String> list;
        LayoutInflater layoutInflater;

        public myAdapter(Context context, int textViewResourceId, List<List<String>> list) {
            super(context, textViewResourceId, list);
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            this.list = getItem(position);

            View view;

            if (list.get(0).equals("1") || list.get(0).equals("2") || list.get(0).equals("3") || list.get(0).equals("4")) {
                view = layoutInflater.inflate(R.layout.order_list_list_text2, null);
                TextView textView = (TextView) view.findViewById(R.id.orderListListViewTextView);
                textView.setText(list.get(0));
            } else if (list.get(0).equals("A") || list.get(0).equals("B")) {
                view = layoutInflater.inflate(R.layout.order_list_list_text3, null);
                TextView textView = (TextView) view.findViewById(R.id.orderListListViewExpendableTextView);
                textView.setText(list.get(0));
            } else {
                view = layoutInflater.inflate(R.layout.order_list_list_text1, null);
                TextView txtName = (TextView) view.findViewById(R.id.orderListNameTextView);
                TextView txtNumber = (TextView) view.findViewById(R.id.orderListNumberTextView);
                TextView txtOrder = (TextView) view.findViewById(R.id.orderListOrderTextView);

                txtName.setText(list.get(0));
                txtNumber.setText(list.get(1));
                txtOrder.setText(list.get(2));
            }

            return view;
        }
    }
}
