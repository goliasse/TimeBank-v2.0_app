package me.mariotti.timebank;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import me.mariotti.timebank.RESTWorkers.ListingWorker;
import me.mariotti.timebank.classes.ConfirmDeleteListingDialog;
import me.mariotti.timebank.classes.Listing;
import me.mariotti.timebank.classes.RESTCaller;
import me.mariotti.timebank.classes.User;
import me.mariotti.timebank.profile.ProfileActivity;


public class ListingDetailActivity extends FragmentActivity implements ConfirmDeleteListingDialog.NoticeDialogListener {
    public static final String LISTING_OBJECT = "me.mariotti.timebank.listing_object";
    private static final String TAG = "ListingDetailActivity";
    private Button mRequestButton;
    private Menu mOptionsMenu;
    private Listing mListing;
    private CheckBox mCheckBox;
    private TextView mRequestedLabel;
    public ProgressDialog progress;
    private static boolean areListingsOutdated = false;
    private TextView mApplicantNameLabel,mApplicantName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listing_detail);
        Bundle data = getIntent().getExtras();
        mListing = data.getParcelable(LISTING_OBJECT);
        mRequestedLabel = (TextView) findViewById(R.id.listing_detail__requested_label);
        mRequestButton = (Button) findViewById(R.id.listing_detail__request_button);
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClickEvent();
            }
        });
        progress = new ProgressDialog(this);
        progress.setMessage("Loading listings");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
    }

    private void handleClickEvent() {
        if (mListing.imTheApplicant()) {
            new ListingWorker(this, ListingWorker.UNREQUEST_LISTING, String.valueOf(mListing.id)).execute();
        } else {
            new ListingWorker(this, ListingWorker.REQUEST_LISTING, String.valueOf(mListing.id)).execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (areListingsOutdated) {
            new ListingWorker(this, RESTCaller.GET_SINGLE_LISTING, String.valueOf(getListingId())).execute();
            areListingsOutdated = false;
        }
        updateUI();
    }

    public void setListing(Listing mListing) {
        this.mListing = mListing;
    }

    public int getListingId() {
        return mListing.id;
    }

    /**
     * Update all the information inside TextView, hide/show Menu Item and UI Views based on Ownership, Applicant name,
     * and requested state
     */
    public void updateUI() {
        Log.i(TAG, "UpdateUI requested");
        ((TextView) findViewById(R.id.listing_detail__descriptionText)).setText(mListing.description);
        ((TextView) findViewById(R.id.categoryText)).setText(mListing.categoryName);
        ((TextView) findViewById(R.id.listing_detail__date_text)).setText(Listing.dateFormatter.format(mListing.dateCreation));
        (mCheckBox = (CheckBox) findViewById(R.id.listing_detail__checkBox_requested)).setChecked(!mListing.requested);
        mApplicantNameLabel= (TextView) findViewById(R.id.listing_detail__applicant_name_label);
        mApplicantName = (TextView) findViewById(R.id.listing_detail__applicant_name);
        invalidateOptionsMenu();
        if (mOptionsMenu != null) {
            MenuItem logInOut = mOptionsMenu.findItem(R.id.menu__listing_detail__log_in_out);
            Intent intent = (new Intent(this, LoginActivity.class));
            if (User.isLogged) {
                logInOut.setTitle(R.string.menu__log_out);
                intent.putExtra(LoginActivity.ACTION, LoginActivity.LOGOUT);
            } else {
                logInOut.setTitle(R.string.menu__log_in);
                intent.putExtra(LoginActivity.ACTION, LoginActivity.LOGIN);
            }
            logInOut.setIntent(intent);
        }
        mRequestButton.setVisibility(!mListing.imOwner() && User.isLogged && (mListing.imTheApplicant() || !mListing.requested) ? View.VISIBLE : View.INVISIBLE);
        if (mListing.imOwner()) {
            mCheckBox.setVisibility(View.VISIBLE);
            mCheckBox.setChecked(mListing.requested);
            mRequestedLabel.setVisibility(View.VISIBLE);
            mApplicantNameLabel.setVisibility(mListing.requested ? View.VISIBLE : View.GONE);
            mApplicantName.setText(mListing.applicantName);
            mApplicantName.setVisibility(mListing.requested?View.VISIBLE:View.GONE);
        } else {
            mApplicantNameLabel.setVisibility(View.GONE);
            mApplicantName.setVisibility(View.GONE);
            mCheckBox.setVisibility(View.INVISIBLE);
            mRequestedLabel.setVisibility(View.INVISIBLE);
            if (mListing.imTheApplicant()) {
                mRequestButton.setText(getString(R.string.listing_detail__unrequest_text));
            } else {
                mRequestButton.setText(getString(R.string.listing_detail__request_text));
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem profile = mOptionsMenu.findItem(R.id.menu__listing_detail__profile);
        if (User.isLogged) {
            profile.setVisible(true);
        } else {
            profile.setVisible(false);
        }
        if (mListing.imOwner()) {
            mOptionsMenu.findItem(R.id.menu__listing_detail__edit).setVisible(true);
            mOptionsMenu.findItem(R.id.menu__listing_detail__delete).setVisible(true);
        } else {
            mOptionsMenu.findItem(R.id.menu__listing_detail__edit).setVisible(false);
            mOptionsMenu.findItem(R.id.menu__listing_detail__delete).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_listing_detail, menu);
        MenuItem LogInOut = mOptionsMenu.findItem(R.id.menu__listing_detail__log_in_out);
        Intent intent = (new Intent(this, LoginActivity.class));
        if (User.isLogged) {
            LogInOut.setTitle(R.string.menu__log_out);
            intent.putExtra(LoginActivity.ACTION, LoginActivity.LOGOUT);
        } else {
            LogInOut.setTitle(R.string.menu__log_in);
            intent.putExtra(LoginActivity.ACTION, LoginActivity.LOGIN);
        }
        LogInOut.setIntent(intent);
        mOptionsMenu.findItem(R.id.menu__listing_detail__profile).setIntent(new Intent(this, ProfileActivity.class));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu__listing_detail__edit) {
            Intent intent = new Intent(this, NewEditActivity.class);
            intent.putExtra(NewEditActivity.LISTING_OBJECT, mListing);
            intent.putExtra(NewEditActivity.ACTION, NewEditActivity.EDIT);
            startActivity(intent);
        }
        if (id == R.id.menu__listing_detail__delete) {
            showConfirmDelete();
        }
        return super.onOptionsItemSelected(item);
    }

    public static void markListingsAsOutdated() {
        areListingsOutdated = true;
    }

    public void showConfirmDelete() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new ConfirmDeleteListingDialog();
        dialog.show(getFragmentManager(), "ConfirmDeleteListingDialog");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the ConfirmDeleteListingDialog.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        new ListingWorker(this, RESTCaller.DELETE_LISTING, String.valueOf(getListingId())).execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        progress.dismiss();
    }

}
