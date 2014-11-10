package uk.co.flumeland.abcsaddyourown;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


public class GoToSndAlertDialog extends DialogFragment {
	
	protected static final int RESULT_OK = -1;
	
	//constructor	
    public GoToSndAlertDialog() {}
	
	@Override
	/**
	 * method to create a dialog box
	 */
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.add_snd_to_pic).setPositiveButton(R.string.yes_add_snd, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				startActivity(new Intent("uk.co.flumeland.ABCsAddYourOwn.Sound"));
				getActivity().finish();
			}
		})
		.setNeutralButton(R.string.no_more_photo, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				//return to current screen
			}
		})
		.setNegativeButton(R.string.no_back, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				getActivity().finish();
				getActivity().finish();
				getActivity().setResult(RESULT_OK, null);
				getActivity().finish();
			}
		});

		// Create the AlertDialog object and return it
		return builder.create();
	}
}