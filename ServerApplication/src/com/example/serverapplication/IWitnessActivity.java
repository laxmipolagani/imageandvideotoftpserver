package com.example.serverapplication;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class IWitnessActivity extends Activity {
	public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	// private static final String TAG = MainActivity.class.getSimpleName();
	public RadioButton radioButtonImage, radioButtonVideo;
	TextView attachment;
	Button butCamera, butChoose, sendPhotsOrVideos;
	public Uri fileUri;
	public String textimage;
	String textvideo;
	private static final int SELECT_PICTURE = 1;
	// private String selectedImagePath;
	ImageView image;
	String stinguri;
	/********* work only for Dedicated IP ***********/
	static final String FTP_HOST = "ftp.eminosoft.com";

	/********* FTP USERNAME ***********/
	static final String FTP_USER = "eminosoft";

	/********* FTP PASSWORD ***********/
	static final String FTP_PASS = "iOSTeam81";
	SharedPreferences sharedPrefs;
	SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.iwitness);
		radioButtonImage = (RadioButton) findViewById(R.id.radioButImage);
		radioButtonVideo = (RadioButton) findViewById(R.id.radioButVideo);
		attachment = (TextView) findViewById(R.id.textAttachment);
		radioButtonImage.setChecked(true);
		radioButtonVideo.setChecked(false);
		butCamera = (Button) findViewById(R.id.butcapthure);
		butChoose = (Button) findViewById(R.id.butchoose);
		sendPhotsOrVideos = (Button) findViewById(R.id.butSend);
		// sendPhotsOrVideos.setEnabled(false);
		image = (ImageView) findViewById(R.id.imageView1);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		editor = sharedPrefs.edit();
		if (savedInstanceState != null) {
			radioButtonImage.setChecked(savedInstanceState.getBoolean("image"));
			radioButtonVideo.setChecked(savedInstanceState.getBoolean("video"));
			fileUri = Uri.parse(savedInstanceState.getString("path"));

		}
		radioButtonImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				radioButtonImage.setChecked(true);
				radioButtonVideo.setChecked(false);
				attachment.setText("No item Attached");
				// sendPhotsOrVideos.setEnabled(false);
			}
		});
		radioButtonVideo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				radioButtonImage.setChecked(false);
				radioButtonVideo.setChecked(true);
				attachment.setText("No item Attached");
				// sendPhotsOrVideos.setEnabled(false);
			}
		});
		butCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				attachment.setText("No item Attached");
				// sendPhotsOrVideos.setEnabled(false);
				// TODO Auto-generated method stub
				try {
					camera();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

		butChoose.setOnClickListener(new OnClickListener() {

			@SuppressLint("DefaultLocale")
			@Override
			public void onClick(View v) {
				attachment.setText("No item Attached");
				// sendPhotsOrVideos.setEnabled(false);
				// TODO Auto-generated method stub
				textimage = radioButtonImage.getText().toString();
				textvideo = radioButtonVideo.getText().toString();
				String imagetext = "image";
				String videotext = "video";

				if (radioButtonImage.isChecked()) {
					if (imagetext.equalsIgnoreCase(textimage)) {
						Intent intent = new Intent();
						intent.setType(textimage.toLowerCase() + "/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(
								Intent.createChooser(intent, "Select Picture"),
								SELECT_PICTURE);
					}
				} else if (radioButtonVideo.isChecked()) {
					if (videotext.equalsIgnoreCase(textvideo)) {

						Intent intent = new Intent();
						intent.setType(textvideo.toLowerCase() + "/*");
						intent.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(
								Intent.createChooser(intent, "Select Picture"),
								SELECT_PICTURE);
					}
				}
			}
		});
		sendPhotsOrVideos.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (fileUri == null) {
					AlertDialog.Builder alert = new AlertDialog.Builder(
							IWitnessActivity.this);
					{
						alert.setMessage("please choose the camera or choose button");
						alert.setPositiveButton("ok",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.cancel();
									}
								});
						alert.show();
					}
				} else if (fileUri != null) {
					System.out.println(fileUri.getPath());
					File f = new File(fileUri.getPath());

					uploadFile(f);
				}
				// System.out.println(fileUri.getPath());
				// File f = new File(fileUri.getPath());
				//
				// // Upload sdcard file
				// uploadFile(f);

			}

		});
	}

	protected void camera() {

		if (radioButtonImage.isChecked()) {

			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		} else if (radioButtonVideo.isChecked()) {
			Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

			fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
			// intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
			//
			// intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

			startActivityForResult(intent, 200);
		}
	}

	public void uploadFile(File fileName) {

		FTPClient client = new FTPClient();

		try {

			client.connect(FTP_HOST, 21);
			client.login(FTP_USER, FTP_PASS);
			client.setType(FTPClient.TYPE_BINARY);

			client.upload(fileName, new MyTransferListener());

		} catch (Exception e) {
			e.printStackTrace();
			try {
				client.disconnect(true);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

	}

	public class MyTransferListener implements FTPDataTransferListener {

		public void started() {

			sendPhotsOrVideos.setVisibility(View.GONE);
			// Transfer started
			Toast.makeText(getBaseContext(), " Upload Started ...",
					Toast.LENGTH_SHORT).show();
			// System.out.println(" Upload Started ...");
		}

		public void transferred(int length) {

			// Yet other length bytes has been transferred since the last time
			// this
			// method was called
			Toast.makeText(getBaseContext(), " transferred ..." + length,
					Toast.LENGTH_SHORT).show();
			// System.out.println(" transferred ..." + length);
		}

		public void completed() {

			sendPhotsOrVideos.setVisibility(View.VISIBLE);
			// Transfer completed
			// sendPhotsOrVideos.setBackgroundResource(R.drawable.imageborder)
			Toast.makeText(getBaseContext(), " completed ...",
					Toast.LENGTH_SHORT).show();
			// System.out.println(" completed ..." );
		}

		public void aborted() {

			sendPhotsOrVideos.setVisibility(View.VISIBLE);
			// Transfer aborted
			Toast.makeText(getBaseContext(),
					" transfer aborted please try again...", Toast.LENGTH_SHORT)
					.show();
			// System.out.println(" aborted ..." );
		}

		public void failed() {

			sendPhotsOrVideos.setVisibility(View.VISIBLE);
			// Transfer failed
			System.out.println(" failed ...");
		}

	}

	protected Uri getOutputMediaFileUri(int type) {

		return Uri.fromFile(getOutputMediaFile(type));
	}

	@SuppressLint("SimpleDateFormat")
	private File getOutputMediaFile(int type) {
		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyCameraApp");

		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		try {
			outState.putString("path", fileUri.toString());
			outState.putBoolean("image", radioButtonImage.isChecked());
			outState.putBoolean("video", radioButtonVideo.isChecked());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1: {

			if (resultCode == RESULT_OK) {
				Uri photoUri = data.getData();
				if (photoUri != null) {
					try {
						String[] filePathColumn = { MediaStore.Images.Media.DATA };
						Cursor cursor = getContentResolver().query(photoUri,
								filePathColumn, null, null, null);
						cursor.moveToFirst();
						int columnIndex = cursor
								.getColumnIndex(filePathColumn[0]);
						String filePath = cursor.getString(columnIndex);
						cursor.close();
						fileUri = Uri.fromFile(new File(filePath));

						// Bitmap bMap = BitmapFactory.decodeFile(filePath);

						// image.setImageBitmap(bMap);
						if (fileUri.getPath().endsWith(".png")
								|| fileUri.getPath().endsWith(".jpg")) {
							attachment.setText("Attachmed Image");
							// sendPhotsOrVideos.setEnabled(true);
						} else if (fileUri.getPath().endsWith(".mp4")
								|| fileUri.getPath().endsWith(".3gp")) {
							attachment.setText("Attached Video");
							// sendPhotsOrVideos.setEnabled(true);
						}
					} catch (Exception e) {
					}
				}

			}

		}
		}
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				Toast.makeText(this, "image saved to:\n", Toast.LENGTH_LONG)
						.show();
				if (fileUri.getPath().endsWith(".png")
						|| fileUri.getPath().endsWith(".jpg")) {
					attachment.setText("Attachmed Image");
					// sendPhotsOrVideos.setEnabled(true);
					editor.putString("Attached image", "Attached image");
					editor.commit();
				}

			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the image capture
			} else {
				// Image capture failed, advise user
			}
		}

		if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Video captured and saved to fileUri specified in the Intent
				Toast.makeText(this, "Video saved to:\n", Toast.LENGTH_LONG)
						.show();
				Uri vid = data.getData();
				String videoPath = getRealPathFromURI(vid);
				writeToFile(videoPath, fileUri.getPath());
				// fileUri=Uri.fromFile(new File(videoPath));
				if (fileUri.getPath().endsWith(".mp4")
						|| fileUri.getPath().endsWith(".3gp")) {
					attachment.setText("Attached Video");
					// sendPhotsOrVideos.setEnabled(true);
					editor.putString("Attached video", "Attached video");
					editor.commit();
				}
			} else if (resultCode == RESULT_CANCELED) {
				// User cancelled the video capture
			} else {
				// Video capture failed, advise user
			}
		}
	}

	private void writeToFile(String videoPath, String path) {

		File source = new File(videoPath);
		File destination = new File(path);
		source.renameTo(destination);

	}

	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		// attachment.setText("no item is added");
		if (radioButtonImage.isChecked()) { 
			attachment.setText(sharedPrefs.getString("Attached image", null));
		} else if (radioButtonVideo.isChecked()) {
			attachment.setText(sharedPrefs.getString("Attached video", null));
		}
	}
}
