package com.mysdscanner.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DatabaseUtils;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mysdscanner.BR;
import com.mysdscanner.R;
import com.mysdscanner.adapters.FileRecyclerAdapter;
import com.mysdscanner.databinding.ActivityScannerBinding;
import com.mysdscanner.model.ScannerResult;

import java.util.Map;

/**
 * Created by hiteshgupta on 8/10/16.
 */

public class ScannerActivity extends AppCompatActivity implements ScannerTaskFragment.OnFragmentInteractionListener {

	private static final String TAG = ScannerActivity.class.getName();

	private static final int PERMISSION_REQUEST_READ_STORAGE = 1;
	private static final int NOTIFICATION_ID = 1;
	private static final int ACTION_ASK_PERMISSION = 2;
	private static final int ACTION_GOTO_SETTINGS = 3;

	private static final String STATE_SCAN = "StateScan";
	private static final String TAG_SCANNER_FRAGMENT = "SCANNER_FRAGMENT";

	private ProgressDialog progressDialog;

	private String biggestFiles, averageSize, frequentExtensions;
	private boolean isScanning;

	private ScannerResult scannerResult;

	private ShareActionProvider shareActionProvider;

	private RecyclerView bigFileView;

	private RecyclerView frequentFileView;

	private ActivityScannerBinding binding;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			isScanning = savedInstanceState.getBoolean(STATE_SCAN);
		}

		binding = DataBindingUtil.setContentView(this, R.layout.activity_scanner);
		binding.setHandlers(new MyHandlers());

		bigFileView = (RecyclerView) findViewById(R.id.file_big_recyclerview);
		frequentFileView = (RecyclerView) findViewById(R.id.file_frequent_recyclerview);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
	}

	@Override
	protected void onResume() {
		super.onResume();

		FragmentManager fragmentManager = getSupportFragmentManager();
		ScannerTaskFragment fragment = (ScannerTaskFragment) fragmentManager.findFragmentByTag(TAG_SCANNER_FRAGMENT);

		if (isScanning) {
			showProgress();
			return;
		}

		if (fragment != null && fragment.getScanResult() != null) {
			displayScanResult(fragment.getScanResult());
		}
	}

	private void checkPermissions() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
				showAlert(ACTION_ASK_PERMISSION, getString(R.string.permission_explanation));
			} else {
				// No explanation needed, we can request the permission.
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_STORAGE);
			}
		} else {
			performStorageScan();
		}
	}

	private void performStorageScan() {
		showProgress();
		showNotification();

		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment fragment = fragmentManager.findFragmentByTag(TAG_SCANNER_FRAGMENT);

		if (fragment == null) {
			ScannerTaskFragment scannerFragment = new ScannerTaskFragment();
			FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
			fragmentTransaction.add(scannerFragment, TAG_SCANNER_FRAGMENT);
			fragmentTransaction.commitNowAllowingStateLoss();
			fragmentManager.executePendingTransactions();
			return;
		}

		((ScannerTaskFragment) fragment).startScanTask();
	}

	private void displayScanResult(ScannerResult scanResult) {
		frequentFileView.setLayoutManager(new LinearLayoutManager(this));
		frequentFileView.addItemDecoration(new VerticalSpaceItemDecoration(5));

		bigFileView.setLayoutManager(new LinearLayoutManager(this));
		bigFileView.setItemAnimator(new DefaultItemAnimator());
		bigFileView.addItemDecoration(new VerticalSpaceItemDecoration(5));

		bigFileView.setAdapter(new FileRecyclerAdapter(R.layout.file_big_item, BR.file, scanResult.getLstFileInfo()));
		frequentFileView.setAdapter(new FileRecyclerAdapter(R.layout.file_frequent_item, BR.file, scanResult.getLstFileExtensionInfo()));

		findViewById(R.id.layout_welcome).setVisibility(View.GONE);
	}

	private void showAlert(final int action, final String message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setCanceledOnTouchOutside(false);
		alertDialog.setCancelable(false);
		alertDialog.setMessage(message);
		alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.alert_positive_button), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.cancel();
				switch (action) {
					case ACTION_ASK_PERMISSION:
						ActivityCompat.requestPermissions(ScannerActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_STORAGE);
						break;
					case ACTION_GOTO_SETTINGS:
						Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
						Uri uri = Uri.fromParts("package", getPackageName(), null);
						intent.setData(uri);
						startActivityForResult(intent, PERMISSION_REQUEST_READ_STORAGE);
						break;
				}

			}
		});
		alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.alert_negative_button), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.cancel();
				finish();
			}
		});
		alertDialog.show();
	}

	private void showProgress() {
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(getString(R.string.progress_message));
		progressDialog.setCancelable(false);
		progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancelScan();
				cancelProgress();
				cancelNotification();
			}
		});
		progressDialog.setIndeterminate(true);
		progressDialog.show();
	}

	private void cancelProgress() {
		if (progressDialog != null) {
			progressDialog.cancel();
			progressDialog = null;
		}
	}

	/*
		Cancel Scan.
	 */
	private void cancelScan() {
		Log.v(TAG, "Cancelling Scan");
		FragmentManager fragmentManager = getSupportFragmentManager();
		ScannerTaskFragment fragment = (ScannerTaskFragment) fragmentManager.findFragmentByTag(TAG_SCANNER_FRAGMENT);
		if (fragment != null) {
			fragment.cancelScanTask();
		}
	}

	/*
		Show notification
	 */
	private void showNotification() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getString(R.string.app_name));
		builder.setContentText(getString(R.string.progress_message));
		builder.setSmallIcon(android.R.drawable.ic_menu_search);
		Notification notification = builder.build();
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	/*
		Cancel notification
	 */
	private void cancelNotification() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case PERMISSION_REQUEST_READ_STORAGE: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// Permission Granted
					performStorageScan();
				} else {
					Log.v(TAG, permissions[0] + ": permission Denied");
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_scanner, menu);
		MenuItem menuItem = menu.findItem(R.id.action_share);
		if (scannerResult != null) {
			// Locate MenuItem with ShareActionProvider
			menuItem.setEnabled(true);

			// Fetch and store ShareActionProvider
			shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
			setShareIntent(createShareIntent());
		} else {
			menuItem.setEnabled(false);
		}

		// Return true to display menu
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_share) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onScanComplete(ScannerResult result) {
		findViewById(R.id.layout_welcome).setVisibility(View.GONE);
		isScanning = false;
		cancelProgress();
		scannerResult = result;
		displayScanResult(result);
		invalidateOptionsMenu();
		cancelNotification();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (progressDialog != null && progressDialog.isShowing()) {
			isScanning = true;
			cancelProgress();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(STATE_SCAN, isScanning);
		super.onSaveInstanceState(outState);
	}

	/*
		Set sharing intent using share action provider
	 */
	private void setShareIntent(Intent shareIntent) {
		if (shareActionProvider != null) {
			shareActionProvider.setShareIntent(shareIntent);
		}
	}

	/*
		Creating intent and data to be shared
	 */
	private Intent createShareIntent() {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		StringBuilder dataToShareBuilder = new StringBuilder();
		dataToShareBuilder.append(biggestFiles).append("\n\n");
		dataToShareBuilder.append(averageSize).append("\n\n");
		dataToShareBuilder.append(frequentExtensions);
		shareIntent.putExtra(Intent.EXTRA_TEXT, dataToShareBuilder.toString());
		return shareIntent;
	}

	public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

		private final int mVerticalSpaceHeight;

		public VerticalSpaceItemDecoration(int mVerticalSpaceHeight) {
			this.mVerticalSpaceHeight = mVerticalSpaceHeight;
		}

		@Override
		public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
		                           RecyclerView.State state) {
			outRect.bottom = mVerticalSpaceHeight;
		}
	}

	public class MyHandlers {
		public void onClickScan(View view) {
			checkPermissions();
		}
	}
}
