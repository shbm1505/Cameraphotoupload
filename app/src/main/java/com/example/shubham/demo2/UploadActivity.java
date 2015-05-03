package com.example.shubham.demo2;

/**
 * Created by shubham on 5/1/2015.
 */

        import android.app.Activity;
        import android.app.AlertDialog;
        import android.app.Dialog;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Color;
        import android.graphics.drawable.ColorDrawable;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.os.Environment;
        import android.support.v7.app.ActionBarActivity;
        import android.util.Log;
        import android.view.View;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.ProgressBar;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.widget.VideoView;

        import java.io.DataOutputStream;
        import java.io.File;
        import java.io.FileInputStream;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;
        import java.security.KeyManagementException;
        import java.security.NoSuchAlgorithmException;
        import java.security.SecureRandom;
        import java.security.cert.X509Certificate;

        import javax.net.ssl.SSLContext;
        import javax.net.ssl.SSLSocketFactory;
        import javax.net.ssl.TrustManager;
        import javax.net.ssl.X509TrustManager;

        import it.sauronsoftware.ftp4j.FTPClient;

public class UploadActivity extends Activity {
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar progressBar;
    private String filePath = null;
    private TextView txtPercentage;
    private ImageView imgPreview;
    private VideoView vidPreview;
    private Button btnUpload;
    private String fileName;
    private static String url_pic_upload="http://eckovation.com/test/gaurav/upload_pic.php";


    /*********  work only for Dedicated IP ***********/
    static final String FTP_HOST= "199.79.62.11";
//    static final String FTP_HOST= "ftps://ftp.akshatgoel.com";

    /*********  FTP USERNAME ***********/
    static final String FTP_USER = "gauravagw@akshatgoel.com";

    /*********  FTP PASSWORD ***********/
    static final String FTP_PASS  ="gauravagw100";

    long totalSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
     //   vidPreview = (VideoView) findViewById(R.id.videoPreview);

        // Changing action bar background color
      //  getActionBar().setBackgroundDrawable(
        //       new ColorDrawable(Color.parseColor(getResources().getString(
          //             R.colors.action_bar))));

        // Receiving the data from previous activity
        Intent i = getIntent();

        // image or video path that is captured in previous activity
        filePath = i.getStringExtra("filePath");
        fileName = i.getStringExtra("fileName");

        // boolean flag to identify the media type, image or video
        boolean isImage = i.getBooleanExtra("isImage", true);

        if (filePath != null) {
            // Displaying the image or video on the screen
            previewMedia(isImage);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // uploading the file to server
                new UploadFileToServer().execute();
            }
        });

    }

    /**
     * Displaying captured image/video on the screen
     * */
    private void previewMedia(boolean isImage) {
        // Checking whether captured media is image or video
        if (isImage) {
            imgPreview.setVisibility(View.VISIBLE);
          //  vidPreview.setVisibility(View.GONE);
            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            imgPreview.setImageBitmap(bitmap);
        } else {
            imgPreview.setVisibility(View.GONE);
           // vidPreview.setVisibility(View.VISIBLE);
           // vidPreview.setVideoPath(filePath);
            // start playing
            //vidPreview.start();
        }
    }

    /**
     * Uploading the file to server
     * */


     private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        public String doInBackground(Void... params) {
            return uploadFileUsingHTTTP();
        }
       @SuppressWarnings("deprecation")
        public String uploadFile() {
            String responseString = null;



           TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {
               public X509Certificate[] getAcceptedIssuers() {
                   return null;
               }
               public void checkClientTrusted(X509Certificate[] certs, String authType) {
               }
               public void checkServerTrusted(X509Certificate[] certs, String authType) {
               }
           } };
           SSLContext sslContext = null;
           try {
               sslContext = SSLContext.getInstance("TLS");
               sslContext.init(null, trustManager, new SecureRandom());
           } catch (NoSuchAlgorithmException e) {
               e.printStackTrace();
           } catch (KeyManagementException e) {
               e.printStackTrace();
           }
           SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
           FTPClient client = new FTPClient();
           client.setSSLSocketFactory(sslSocketFactory);
           client.setSecurity(FTPClient.SECURITY_FTPS); // or client.setSecurity(FTPClient.SECURITY_FTPES);


           try {

               client.setSecurity(FTPClient.SECURITY_FTPES);
               client.connect(FTP_HOST,21);
               client.login(FTP_USER, FTP_PASS);
               client.setType(FTPClient.TYPE_BINARY);
             //  client.changeDirectory("/upload/");

               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       Toast.makeText(UploadActivity.this,"Uploading...Please Wait.",Toast.LENGTH_LONG).show();
                   }
               });
               File file=new File(filePath);
               client.upload(file);

           } catch (Exception e) {
               e.printStackTrace();
               try {
                   client.disconnect(true);
               } catch (Exception e2) {
                   e2.printStackTrace();
               }
           }
              //  int statusCode = response.getStatusLine().getStatusCode();
              //  if (statusCode == 200) {
                    // Server response
               //     responseString = EntityUtils.toString(r_entity);
               // } else {
                //    responseString = "Error occurred! Http Status Code: "
                 //           + statusCode;
               // }


//            return responseString;
            return "DoneBaby";
        }
        public String uploadFileUsingHTTTP() {

            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(filePath);
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(url_pic_upload);
                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName + ".jpg");

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);
                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }
                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                int serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {

                    runOnUiThread(new Runnable() {
                        public void run() {
                            // button_camera.setEnabled(false);
                            Toast.makeText(UploadActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                //   dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(UploadActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                //  dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(UploadActivity.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);
                return "something";
            }
        return "something else";

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(UploadActivity.this,"Uploaded Successfully.",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}
