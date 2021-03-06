package com.podevs.android.poAndroid.teambuilder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;

import com.podevs.android.poAndroid.R;
import com.podevs.android.poAndroid.poke.TeamPoke;
import com.podevs.android.poAndroid.pokeinfo.HiddenPowerInfo;

public class MoveChooserFragment extends Fragment {
	public interface MoveChooserListener {
		public void onMovesetChanged(boolean stats);
	}

	ListView moveList = null;
	MoveListAdapter moveAdapter = null;
	public MoveChooserListener listener = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.movelist, container, false);

		moveList = (ListView)v.findViewById(R.id.moves);
		moveAdapter = new MoveListAdapter();
		moveAdapter.setPoke(poke());

		moveList.setAdapter(moveAdapter);

		moveList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				int move = (Short) moveAdapter.getItem(arg2);
				if (!poke().hasMove(move) && poke().addMove(move)) {
					((CheckBox)arg1.findViewById(R.id.check)).setChecked(true);

					/* Hidden Power */
					if (move == 237) {
						buildHiddenPowerDialog();
					} else if (move == 216) { /* return */
						poke().happiness = (byte)255;
					}
					
					if (listener != null) {
						listener.onMovesetChanged(false);
					}
				} else if (poke().removeMove(move)) {
					((CheckBox)arg1.findViewById(R.id.check)).setChecked(false);

					/* Hidden Power */
					if (move == 237) {
						for (int i = 0; i < 6; i++) {
							poke().DVs[i] = 31;
						}
					} else if (move == 216) { /* return */
						poke().happiness = 0;
					}
					
					if (listener != null) {
						listener.onMovesetChanged(move == 237);
					}
				}
			}
		});

		moveList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				int move = (Short) moveAdapter.getItem(position);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setMessage(moveAdapter.moveInfo(move));
				builder.create().show();
				return false;
			}
		});

		return v;
	}

	protected void buildHiddenPowerDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.hiddenpower_choice)
			.setSingleChoiceItems(R.array.hp_array, poke().hiddenPowerType()-1, null)
			.setPositiveButton(R.string.ok, new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					ListView lw = ((AlertDialog)dialog).getListView();
					int type = lw.getCheckedItemPosition() + 1;
					
					byte[] config = HiddenPowerInfo.configurationForType(type, poke().gen);
					
					if (config != null) {
						for (int i = 0; i < 6; i++) {
							poke().DVs[i] = config[i];
						}
						
						if (listener != null) {
							listener.onMovesetChanged(true);
						}
					}
				}
			});
		builder.create().show();
	}

	public void updatePoke() {
		moveAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private TeamPoke poke() {
		return ((EditPokemonActivity) getActivity()).getPoke();
	}
}
