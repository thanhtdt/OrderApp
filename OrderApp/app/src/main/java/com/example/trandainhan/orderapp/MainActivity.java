package com.example.trandainhan.orderapp;

import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.SubMenu;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.trandainhan.orderapp.api.Api;
import com.example.trandainhan.orderapp.fragments.ChoXuLyFragment;
import com.example.trandainhan.orderapp.fragments.DaXuLyFragment;
import com.example.trandainhan.orderapp.fragments.DangGiaoHangFragment;
import com.example.trandainhan.orderapp.fragments.FoodFragment;
import com.example.trandainhan.orderapp.fragments.QuanLyDanhMucFragment;
import com.example.trandainhan.orderapp.models.DanhMuc;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.trandainhan.orderapp.fragments.WelcomeFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    Menu menu;

    // List
    List<DanhMuc> danhMucs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState==null) {
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            WelcomeFragment welcomeFragment = WelcomeFragment.newInstance(this);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, welcomeFragment).commit();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);

            menu = navigationView.getMenu();

            reload();
        }
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
        getMenuInflater().inflate(R.menu.danh_muc, menu);
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

        if (id == R.id.menu_quan_ly_danh_muc) {
            QuanLyDanhMucFragment quanLyDanhMucFragment = QuanLyDanhMucFragment.newInstance(this);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, quanLyDanhMucFragment).commit();
        } else if (id == R.id.nav_cho_xu_ly) {
            ChoXuLyFragment choXuLyFragment = ChoXuLyFragment.newInstance(this);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, choXuLyFragment).commit();
        } else if (id == R.id.nav_dang_giao_hang) {
            DangGiaoHangFragment dangGiaoHangFragment = DangGiaoHangFragment.newInstance(this);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, dangGiaoHangFragment).commit();
        } else if (id == R.id.nav_da_xu_ly) {
            DaXuLyFragment daXuLyFragment = DaXuLyFragment.newInstance(this);
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, daXuLyFragment).commit();
        } else {
            int groupId = item.getGroupId();
            if (groupId == R.id.menu_danhmuc) {
                String tenDanhMuc = String.valueOf(item.getTitle());

                for (DanhMuc danhMuc : danhMucs) {
                    if (danhMuc.tenDanhMuc.equals(tenDanhMuc)) {
                        // navigate to
                        FoodFragment foodFragment = FoodFragment.newInstance(danhMuc.danhMucId, this);
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.content_frame, foodFragment).commit();
                        break;
                    }
                }
            }
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void reload() {
        new LoadDanhMucTask().execute();
    }

    class LoadDanhMucTask extends AsyncTask<Void, String, List<DanhMuc>> {

        @Override
        protected List<DanhMuc> doInBackground(Void... params) {

            List<DanhMuc> danhMucs = Api.GetDanhMuc();
            return danhMucs;
        }

        @Override
        protected void onPostExecute(List<DanhMuc> danhMucs) {
            super.onPostExecute(danhMucs);

            Collections.sort(danhMucs, new Comparator<DanhMuc>() {
                @Override
                public int compare(DanhMuc o1, DanhMuc o2) {
                    return o1.tenDanhMuc.compareTo(o2.tenDanhMuc);
                }
            });

            MainActivity.this.danhMucs = danhMucs;

            // start reload ui
            SubMenu subMenu = menu.findItem(R.id.menu_danhmuc).getSubMenu();
            subMenu.removeGroup(R.id.menu_danhmuc);
            for (DanhMuc danhMuc : danhMucs) {
                subMenu.add(R.id.menu_danhmuc, Menu.NONE, 0, danhMuc.tenDanhMuc);
            }
        }
    }
}
