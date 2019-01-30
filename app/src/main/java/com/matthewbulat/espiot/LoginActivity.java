package com.matthewbulat.espiot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewbulat.espiot.Database.user.UserDB;
import com.matthewbulat.espiot.Database.user.UserTable;
import com.matthewbulat.espiot.Objects.ConstantValues;
import com.matthewbulat.espiot.Objects.User;
import com.matthewbulat.espiot.RetrofitDIR.ApiUtils;
import com.matthewbulat.espiot.RetrofitDIR.Interfaces.IoTAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, ConstantValues {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
//    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private UserDB userDB;
    private IoTAPI ioTAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ioTAPI = ApiUtils.getIoTService();
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = findViewById(R.id.email_login);
        populateAutoComplete();
        userDB = Room.databaseBuilder(getApplicationContext(), UserDB.class, "userdb").build();
        mPasswordView = findViewById(R.id.password_login);
        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin();
                return true;
            }
            return false;
        });

        Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(view -> attemptLogin());

        Button registerButton = findViewById(R.id.register_button_activity);
        registerButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            getApplicationContext().startActivity(intent);
        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_values, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection

        switch (item.getItemId()) {
            case R.id.applicationSettings:
                startActivity(new Intent(LoginActivity.this, Settings.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            User user = new User();
            user.setUserEmail(email);
            user.setPassword(password);
            systemLogin(user);
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */

    private void showProgress(final boolean show) {

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    public void systemLogin(User user) {
        compositeDisposable.add(ioTAPI.userLogin(user)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<User>() {
                                   @Override
                                   public void onComplete() {
                                       compositeDisposable.dispose();
                                   }

                                   @Override
                                   public void onError(Throwable e) {
                                       Log.e("Login", e.getMessage());
                                   }

                                   @Override
                                   public void onNext(User value) {
                                       loginResponse(value);
                                   }
                               }
                )
        );
    }

    public void insertUserToDatabase(User user) {
        Log.i("Login", "user login request successful");
        UserTable userTable = new UserTable();
        userTable.setUserName(user.getUserName());
        userTable.setUserToken(user.getUserToken());

        Completable.fromAction(() -> userDB.userDao().addUser(userTable))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onComplete() {
                                Log.i("Database", "Saved user to database");
                                showProgress(false);
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.e("Database", e.getMessage());
                            }
                        }
                );
    }


    public void loginResponse(User user) {
        switch (user.getAction()) {
            case "LoginSuccessful":
                insertUserToDatabase(user);
                break;
            case "IncorrectLoginCredentials":
                Log.i("Login", "incorrect credentials");
                mPasswordView.setError(getString(R.string.incorrect_credentials));
                mPasswordView.requestFocus();
                break;
            case "CommunicationError":
                Log.i("Login", "communication error");
                Toast.makeText(getApplicationContext(), "Something went wrong try again in few moments..."
                        , Toast.LENGTH_LONG).show();
                break;
            case "DataMissing":
                Log.i("Login", "data missing");
                Toast.makeText(getApplicationContext(), "Check your text fields and try again"
                        , Toast.LENGTH_LONG).show();
                break;
            case "UnknownError":
                Log.i("Login", "unknown error");
                Toast.makeText(getApplicationContext(), "Something went wrong try again in few moments..."
                        , Toast.LENGTH_LONG).show();
                break;
        }
    }
}

