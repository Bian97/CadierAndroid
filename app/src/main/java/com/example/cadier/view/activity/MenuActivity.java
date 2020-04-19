package com.example.cadier.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cadier.R;
import com.example.cadier.modelo.Usuario;
import com.example.cadier.view.fragments.FragmentAjuda;
import com.example.cadier.view.fragments.FragmentCalendario;
import com.example.cadier.view.fragments.FragmentConfiguracoes;
import com.example.cadier.view.fragments.FragmentContatos;
import com.example.cadier.view.fragments.FragmentMensalidades;
import com.example.cadier.view.fragments.FragmentPedidos;
import com.example.cadier.view.fragments.FragmentPerfil;

public class MenuActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    Usuario usuario = new Usuario();
    TextView txtNomeMenu, txtIgrejaMenu;
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
            usuario = (Usuario) intent.getSerializableExtra("usuario");
        }

        View view = navigationView.getHeaderView(0);
        imageViewMenu = view.findViewById(R.id.imageViewMenu);
        txtNomeMenu = view.findViewById(R.id.txtNomeMenu);
        txtIgrejaMenu = view.findViewById(R.id.txtIgrejaMenu);

        txtNomeMenu.setText(usuario.getNome());
        txtIgrejaMenu.setText(usuario.getIgreja());

        Bitmap aux = BitmapFactory.decodeFile(usuario.getFoto());
        if(usuario.getFoto() != null && aux != null) {
            imageViewMenu.setImageBitmap(aux);
        } else {
            imageViewMenu.setImageResource(R.drawable.foto);
        }

        iniciarFragment(new FragmentPerfil(), "Perfil");
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

        switch (id){
            case R.id.opcao_perfil:
                iniciarFragment(new FragmentPerfil(), "Perfil");
                break;
            case R.id.opcao_calendario:
                Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://cadier.yolasite.com/calendario-reuni%C3%B5es.php"));
                startActivity(viewIntent);
                //iniciarFragment(new FragmentCalendario(), "Calendário");
                break;
            case R.id.opcao_pedidos:
                iniciarFragment(new FragmentPedidos(), "Pedidos");
                break;
            case R.id.opcao_mensalidades:
                iniciarFragment(new FragmentMensalidades(), "Mensalidades");
                break;
            case R.id.opcao_configuracao:
                iniciarFragment(new FragmentConfiguracoes(), "Configurações");
                break;
            case R.id.opcao_contatos:
                iniciarFragment(new FragmentContatos(), "Contatos");
                break;
            case R.id.opcao_ajuda:
                iniciarFragment(new FragmentAjuda(), "Ajuda");
                break;
            case R.id.opcao_logout:
                usuario = null;
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void iniciarFragment(Fragment fragment, String title) {

        if (fragment != null) {

            if (title.equalsIgnoreCase(title)) {
                getIntent().putExtra("usuario", usuario);
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