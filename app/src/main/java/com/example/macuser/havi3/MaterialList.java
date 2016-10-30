package com.example.macuser.havi3;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ButtonBarLayout;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MaterialList extends Fragment {
    private String expendable = "";
    private String place = "";
    private myAdapter myAdapter;
    private View view;
    private MaterialListFragmentListener listener;

    public interface MaterialListFragmentListener {
        void onMaterialListFragmentEvent(String number, String name);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof MaterialListFragmentListener)) {
            throw new UnsupportedOperationException("Listener is not Implementation");
        } else {
            listener = (MaterialListFragmentListener) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_material_list, container, false);

        Money money = new Money(getActivity().getApplicationContext(), true);

        final Spinner spinnerExpendable = (Spinner) view.findViewById(R.id.materialListExpendableSpinner);
        final Spinner spinnerPlace = (Spinner) view.findViewById(R.id.materialListPlaceSpinner);

        final ListView listView = (ListView)view.findViewById(R.id.listView);

        mySpinnerAdapter<String> spinnerExpendableAdapter = new mySpinnerAdapter<>(view.getContext(), android.R.layout.simple_spinner_item);
        spinnerExpendableAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerExpendableAdapter.add("");
        spinnerExpendableAdapter.add("A");
        spinnerExpendableAdapter.add("B");

        spinnerExpendable.setAdapter(spinnerExpendableAdapter);
        spinnerExpendable.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                expendable = spinnerExpendable.getSelectedItem().toString();
                place = spinnerPlace.getSelectedItem().toString();

                myAdapter = new myAdapter(
                        getActivity().getApplicationContext(),
                        R.layout.row,
                        Material.showMaterialList(view.getContext(), expendable, place)
                );

                listView.setAdapter(myAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //何もしない
            }
        });

        mySpinnerAdapter<String> spinnerPlaceAdapter = new mySpinnerAdapter<>(view.getContext(), android.R.layout.simple_spinner_item);
        spinnerPlaceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinnerPlaceAdapter.add("");
        spinnerPlaceAdapter.add("イナバ");
        spinnerPlaceAdapter.add("倉庫");
        spinnerPlaceAdapter.add("シンク");
        spinnerPlaceAdapter.add("厨房");
        spinnerPlaceAdapter.add("カウンター");
        spinnerPlaceAdapter.add("クルールーム");

        spinnerPlace.setAdapter(spinnerPlaceAdapter);
        spinnerPlace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                expendable = spinnerExpendable.getSelectedItem().toString();
                place = spinnerPlace.getSelectedItem().toString();

                myAdapter = new myAdapter(
                        getActivity().getApplicationContext(),
                        R.layout.row,
                        Material.showMaterialList(view.getContext(), expendable, place)
                );

                listView.setAdapter(myAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //何もしない
            }
        });



        return view;
    }

    private class mySpinnerAdapter<T> extends ArrayAdapter<T> {
        public mySpinnerAdapter(Context context, int layoutResourceId) {
            super(context, layoutResourceId);
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            View view = super.getDropDownView(position, convertView, parent);
            ((TextView) view).setGravity(Gravity.CENTER);
            return view;
        }
    }

    private class myAdapter extends ArrayAdapter<List<String>> {
        private LayoutInflater layoutInflater;
        private List<String> list;

        private static final float BUTTON_WIDTH_DP = 140f;
        private int margin;

        private MyPagerAdapter pagerAdapter;

        public myAdapter(Context context, int textViewResourceId, List<List<String>> list) {
            super(context, textViewResourceId, list);
            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            float density = getContext().getResources().getDisplayMetrics().density;
            int buttonWidthPX = (int) (BUTTON_WIDTH_DP * density + 0.5f);

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display dp = wm.getDefaultDisplay();
            margin = dp.getWidth() - buttonWidthPX - 97;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            list = (List<String>) getItem(position);
            View view;

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.row, null);
            } else {
                view = convertView;
            }


            ViewPager viewPager = (ViewPager) view.findViewById(R.id.viewPager);
            viewPager.setPageMargin(-margin);
            pagerAdapter = new MyPagerAdapter(getActivity().getApplicationContext(), getItem(position).get(1), getItem(position).get(0));
            viewPager.setAdapter(pagerAdapter);

            return view;
        }
    }

    private class MyPagerAdapter extends PagerAdapter {
        private LayoutInflater inflater;
        private String name;
        private String number;
        private final int PAGE_NUM = 2;

        public MyPagerAdapter(Context context, String str1, String str2) {
            super();
            inflater = LayoutInflater.from(context);
            this.name = str1;
            this.number = str2;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LinearLayout layout = null;
            if (position == 0) {
                layout = (LinearLayout) inflater.inflate(R.layout.page1, null);
                TextView txt1 = (TextView) layout.findViewById(R.id.text1);
                txt1.setText(name);
                TextView txt2 = (TextView) layout.findViewById(R.id.text2);
                txt2.setText(number);
            } else {
                layout = (LinearLayout) inflater.inflate(R.layout.page2, null);

                Button btnStock = (Button) layout.findViewById(R.id.MaterialListStockPreserveButton);
                btnStock.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        final View layout = layoutInflater.inflate(R.layout.dialog_list_view, null);

                        final Material material = new Material(number, name);

                        final EditText Case = (EditText) layout.findViewById(R.id.editTextDialogCase);
                        final EditText Bag = (EditText) layout.findViewById(R.id.editTextDialogBag);
                        final EditText Substance = (EditText) layout.findViewById(R.id.editTextDialogSubstance);

                        List<Integer> inNumberList = material.getMaterial.getInNumber(layout.getContext());

                        Case.setHint(String.valueOf(inNumberList.get(0)));
                        Bag.setHint(String.valueOf(inNumberList.get(1)));
                        Substance.setHint(String.valueOf(inNumberList.get(2)));

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(name)
                                .setView(layout)
                                .setPositiveButton("登録", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {


                                        String caseNumber = Case.getText().toString();
                                        String bagNumber = Bag.getText().toString();
                                        String substanceNumber = Substance.getText().toString();

                                        if (caseNumber.equals("")) {
                                            caseNumber = "0";
                                        }
                                        if (bagNumber.equals("")) {
                                            bagNumber = "0";
                                        }
                                        if (substanceNumber.equals("")) {
                                            substanceNumber = "0";
                                        }

                                        material.setNumber(getActivity().getApplicationContext(), Integer.parseInt(caseNumber), Integer.parseInt(bagNumber), Double.parseDouble(substanceNumber));
                                    }
                                })
                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).create().show();
                    }
                });

                Button btnContainer = (Button) layout.findViewById(R.id.MaterialListPreserveButton);
                btnContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onMaterialListFragmentEvent(number, name);
                        }
                    }
                });
            }

            container.addView(layout);

            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeViewInLayout((View) object);
        }

        @Override
        public int getCount() {
            return PAGE_NUM;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view.equals(obj);
        }
    }


}
