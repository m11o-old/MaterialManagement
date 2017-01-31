package com.example.macuser.havi3;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MaterialList.MaterialListFragmentListener, Order.OrderFragmentListener {

    final String[] fragments = {
            "com.example.macuser.havi3.Budget",
            "com.example.macuser.havi3.OrderList",
            "com.example.macuser.havi3.Order",
            "com.example.macuser.havi3.MaterialList"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment materialListFragment = new MaterialList();
        fragmentManager.beginTransaction()
                .replace(R.id.container, materialListFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (id == R.id.budget) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment.instantiate(MainActivity.this, fragments[0]))
                    .commit();
        } else if (id == R.id.orderList) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment.instantiate(MainActivity.this, fragments[1]))
                    .commit();
        } else if (id == R.id.order) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment.instantiate(MainActivity.this, fragments[2]))
                    .commit();
        } else if (id == R.id.materialList) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, Fragment.instantiate(MainActivity.this, fragments[3]))
                    .commit();
        } else if (id == R.id.newMaterial) {
            Intent intent = new Intent(MainActivity.this, MaterialListContainer.class);
            intent.putExtra("position", 2);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMaterialListFragmentEvent(String number, String name) {
        Intent intent = new Intent(MainActivity.this, MaterialListContainer.class);
        intent.putExtra("number", number);
        intent.putExtra("name", name);
        intent.putExtra("position", 0);
        startActivity(intent);
    }

    @Override
    public void onOrderFragmentEvent(String price, String number) {
        Intent intent = new Intent(MainActivity.this, MaterialListContainer.class);
        intent.putExtra("price", price);
        intent.putExtra("number", number);
        intent.putExtra("position", 1);
        startActivity(intent);
    }


}
