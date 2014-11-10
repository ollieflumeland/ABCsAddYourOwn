package uk.co.flumeland.abcsaddyourown;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;

public class Splash extends Activity {
	
	private static final int COMPLETE = 100;
	MediaPlayer startMusic;
	public DatabaseHelper db;
	private static final int PROGRESS = 345;
	private ProgressBar dbProgress;
    private int dbProgressStatus = 0;
    private Handler dbHandler = new Handler();

	@Override
	/**
	 * A method to inflate and prepare splash layout and set up various variables
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);
		
		dbProgress = (ProgressBar) findViewById(R.id.pbSplV);
		
		startMusic = MediaPlayer.create(Splash.this, R.raw.abc_song);
		startMusic.start();
		db = new DatabaseHelper(getApplicationContext());
		
		loadDatabase();				
		timeDelay();
	

        // Start lengthy operation in a background thread
        new Thread(new Runnable() {
            public void run() {
                while (dbProgressStatus < COMPLETE) {
                	
                    // Update the progress bar
                    dbHandler.post(new Runnable() {
                        public void run() {
                            dbProgress.setProgress(dbProgressStatus);
                        }
                    });
                }
            }
        }).start();
    }


	/**
	 * A method to initiate database checking and loading if required
	 * @return 100 to indicate method completed
	 */
	private int loadDatabase() {
		// Check on database------------------
		try {
			MainPlay.totalItems = db.prepareDatabaseContent();
			Toast t = Toast.makeText(getBaseContext(), "Number of objects = " + MainPlay.totalItems, Toast.LENGTH_LONG);
			t.setGravity(Gravity.BOTTOM, 0, -2);
			t.show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			Toast t = Toast.makeText(getApplicationContext(), "Database Loaded", Toast.LENGTH_SHORT);
			t.setGravity(Gravity.TOP, 0, 50);
			t.show();
		}
		return 100;
	}

	/**
	 * A method to delay startup of main program while database loads
	 */
	private void timeDelay() {
		Thread timer = new Thread() {
			public void run(){
				try {
					for (int i=1; i<9; i++) {
					sleep(1100);
					dbProgressStatus = i*10;
					}
					dbProgressStatus = 100;
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					Intent openMainPlay = new Intent("uk.co.flumeland.ABCsAddYourOwn.MainPlay");
					startActivity(openMainPlay);
					finish();
				}
			}
		};
		timer.start();
		
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		startMusic.release();
		finish();
	}

	
}


