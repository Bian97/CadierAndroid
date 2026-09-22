package app.convencao.cadier.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import app.convencao.cadier.modelo.User;
import app.convencao.cadier.view.fragments.FragmentCalendar;
import app.convencao.cadier.view.fragments.FragmentConfigurations;
import app.convencao.cadier.view.fragments.FragmentContacts;
import app.convencao.cadier.view.fragments.FragmentHelp;
import app.convencao.cadier.view.fragments.FragmentMonthly;
import app.convencao.cadier.view.fragments.FragmentOrders;
import app.convencao.cadier.view.fragments.FragmentProfile;
import app.convencao.cadier.R;
import com.google.android.material.navigation.NavigationView;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    User user = new User();
    TextView txtNameMenu, txtChurchMenu;
    ImageView imageViewMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        if(intent != null){
            user = (User) intent.getSerializableExtra("usuario");
        }

        View view = navigationView.getHeaderView(0);
        imageViewMenu = view.findViewById(R.id.imageViewMenu);
        txtNameMenu = view.findViewById(R.id.txtNomeMenu);
        txtChurchMenu = view.findViewById(R.id.txtIgrejaMenu);

        txtNameMenu.setText(user.getName());
        txtChurchMenu.setText(user.getChurch());

        Bitmap aux = BitmapFactory.decodeFile(user.getPhoto());
        if(user.getPhoto() != null && aux != null) {
            imageViewMenu.setImageBitmap(aux);
        } else {
            imageViewMenu.setImageResource(R.drawable.foto);
        }

        startFragment(new FragmentProfile(), "Perfil");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
            drawer.openDrawer(GravityCompat.START);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // if/else em vez de switch: a partir do AGP 9 os ids de R.id deixaram de ser constantes de
        // compilação por padrão (android.nonFinalResIds), e switch/case exige constante.
        if (id == R.id.opcao_perfil) {
            startFragment(new FragmentProfile(), "Perfil");
        } else if (id == R.id.opcao_calendario) {
            startFragment(new FragmentCalendar(), "Calendário");
        } else if (id == R.id.opcao_pedidos) {
            startFragment(new FragmentOrders(), "Pedidos");
        } else if (id == R.id.opcao_mensalidades) {
            startFragment(new FragmentMonthly(), "Mensalidades");
        } else if (id == R.id.opcao_configuracao) {
            startFragment(new FragmentConfigurations(), "Configurações");
        } else if (id == R.id.opcao_contatos) {
            startFragment(new FragmentContacts(), "Contatos");
        } else if (id == R.id.opcao_ajuda) {
            startFragment(new FragmentHelp(), "Ajuda");
        } else if (id == R.id.opcao_logout) {
            user = null;
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void startFragment(Fragment fragment, String title) {

        if (fragment != null) {
            if (title.equalsIgnoreCase(title)) {
                getIntent().putExtra("usuario", user);
            }
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.frameLayout, fragment);
            fragmentTransaction.commit();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}