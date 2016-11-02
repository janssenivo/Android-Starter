package com.kinvey.bookshelf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyPurgeCallback;
import com.kinvey.android.store.DataStore;
import com.kinvey.android.store.UserStore;
import com.kinvey.android.sync.KinveyPullCallback;
import com.kinvey.android.sync.KinveyPullResponse;
import com.kinvey.android.sync.KinveyPushCallback;
import com.kinvey.android.sync.KinveyPushResponse;
import com.kinvey.android.sync.KinveySyncCallback;
import com.kinvey.java.cache.KinveyCachedClientCallback;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.dto.User;
import com.kinvey.java.store.StoreType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShelfActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Client client;
    BooksAdapter adapter;
    DataStore<Book> bookStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelf);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        client =  ((App)getApplication()).getSharedClient();
        bookStore = DataStore.collection(Book.COLLECTION, Book.class, StoreType.CACHE, client);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            checkLogin();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sync(){
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("syncing");
        pd.show();

        bookStore.sync(new KinveySyncCallback<Book>() {
            @Override
            public void onSuccess(KinveyPushResponse kinveyPushResponse, KinveyPullResponse<Book> kinveyPullResponse) {
                pd.dismiss();
                Toast.makeText(ShelfActivity.this, "sync complete", Toast.LENGTH_LONG).show();
                getData();
            }

            @Override
            public void onPullStarted() {

            }

            @Override
            public void onPushStarted() {

            }

            @Override
            public void onPullSuccess(KinveyPullResponse<Book> kinveyPullResponse) {

            }

            @Override
            public void onPushSuccess(KinveyPushResponse kinveyPushResponse) {

            }

            @Override
            public void onFailure(Throwable t) {
                pd.dismiss();
                Toast.makeText(ShelfActivity.this, "sync failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void getData(){
        bookStore.find(new KinveyListCallback<Book>() {
            @Override
            public void onSuccess(List<Book> books) {
                updateBookAdapter(books);
            }

            @Override
            public void onFailure(Throwable error) {
            }
        }, new KinveyCachedClientCallback<List<Book>>() {
            @Override
            public void onSuccess(final List<Book> books) {
                Log.d("CachedClientCallback: ", "success");
                updateBookAdapter(books);
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.d("CachedClientCallback: ", "failure");
            }
        });
    }

    private void updateBookAdapter(List<Book> books) {
        if (books == null) {
            books = new ArrayList<Book>();
        }
        ListView list = (ListView) findViewById(R.id.shelf);
        list.setOnItemClickListener(ShelfActivity.this);
        adapter = new BooksAdapter(books, ShelfActivity.this);
        list.setAdapter(adapter);
    }

    private void checkLogin() throws IOException {

        final ProgressDialog pd = new ProgressDialog(this);


        if (!client.isUserLoggedIn()){
            pd.setIndeterminate(true);
            pd.setMessage("Logging in");
            pd.show();
            UserStore.login("test", "test", client, new KinveyClientCallback<User>() {
                @Override
                public void onSuccess(User result) {
                    //successfully logged in
                    pd.dismiss();
                    sync();
                }

                @Override
                public void onFailure(Throwable error) {
                    UserStore.signUp("test", "test", client, new KinveyClientCallback<User>() {
                        @Override
                        public void onSuccess(User o) {
                            getData();
                            pd.dismiss();
                        }

                        @Override
                        public void onFailure(Throwable error) {
                            pd.dismiss();
                            Toast.makeText(ShelfActivity.this, "can't login to app", Toast.LENGTH_LONG).show();

                        }
                    });
                }
            });
        } else {
            getData();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shelf, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        final ProgressDialog pd = new ProgressDialog(this);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new) {
            Intent i = new Intent(this, BookActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_sync){
            sync();
        } else if (id == R.id.action_pull){
            pd.setMessage("pulling");
            pd.show();
            bookStore.pull(null, new KinveyPullCallback<Book>() {
                @Override
                public void onSuccess(KinveyPullResponse kinveyPullResponse) {
                    pd.dismiss();
                    getData();
                    Toast.makeText(ShelfActivity.this, "pull success", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Throwable error) {
                    pd.dismiss();
                    Toast.makeText(ShelfActivity.this, "pull failed", Toast.LENGTH_LONG).show();
                }
            });
        } else if (id == R.id.action_push){
            pd.setMessage("pushing");
            pd.show();
            bookStore.push(new KinveyPushCallback() {
                @Override
                public void onSuccess(KinveyPushResponse kinveyPushResponse) {
                    pd.dismiss();
                    Toast.makeText(ShelfActivity.this, "push success", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Throwable error) {
                    pd.dismiss();
                    Toast.makeText(ShelfActivity.this, "push failed", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProgress(long current, long all) {

                }
            });
        } else if (id == R.id.action_purge){
            pd.setMessage("purging");
            pd.show();
            bookStore.purge(new KinveyPurgeCallback() {
                @Override
                public void onSuccess(Void result) {
                    pd.dismiss();
                    getData();
                    Toast.makeText(ShelfActivity.this, "purge success", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Throwable error) {
                    pd.dismiss();
                    Toast.makeText(ShelfActivity.this, "purge failed", Toast.LENGTH_LONG).show();
                }

            });
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Book book = adapter.getItem(position);
        Intent i = new Intent(this, BookActivity.class);
        i.putExtra("id", book.get("_id").toString());
        startActivity(i);
    }
}
