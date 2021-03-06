package com.linkedin.eatin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.linkedin.eatin.model.Caterer;
import com.linkedin.eatin.model.Comment;
import com.linkedin.eatin.model.FoodItem;
import com.linkedin.eatin.model.Menu;
import com.linkedin.eatin.model.BaseData;
import com.linkedin.eatin.utility.Constants;

public class MenuActivity extends Activity {
	public class MenuListAdapter extends ArrayAdapter<FoodItem> {
		private List<FoodItem> objects;
		private Context context;

		public MenuListAdapter(Context context, List<FoodItem> objects) {
			super(context, R.layout.food_list_item, objects);

			this.objects = objects;
			this.context = context;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.food_list_item, null);

			final FoodItem item = objects.get(position);

			((TextView) view.findViewById(R.id.foodName)).setText(item.getName());
			((TextView) view.findViewById(R.id.numRatings)).setText(item.getNumRatings().toString());
			((View) view.findViewById(R.id.ratingIndicator)).setBackgroundColor(Color.parseColor(item.getRatingColor()));
			
			((ImageButton) view.findViewById(R.id.upvoteIcon)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view) {
					item.upvote();
					MenuListAdapter.this.notifyDataSetChanged();
					MenuActivity.this.updateLikes();
				}
			});
			
			((ImageButton) view.findViewById(R.id.downvoteIcon)).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View view) {
					item.downvote();
					MenuListAdapter.this.notifyDataSetChanged();
					MenuActivity.this.updateLikes();
				}
			});

			return view;
		}
	}

	public class CommentListAdapter extends ArrayAdapter<Comment> {
		private List<Comment> objects;
		private Context context;
		
		private DateFormat dateFormat;

		public CommentListAdapter(Context context, List<Comment> objects) {
			super(context, R.layout.food_list_item, objects);

			this.objects = objects;
			this.context = context;
			this.dateFormat = new SimpleDateFormat("HH:mm, MMMM dd, yyyy", Locale.US);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.comment_list_item, null);

			Comment item = objects.get(position);

			((TextView) view.findViewById(R.id.posterName)).setText(item.getPoster());
			((TextView) view.findViewById(R.id.message)).setText(item.getMessage());
			((TextView) view.findViewById(R.id.postDate)).setText(dateFormat.format(item.getPostDate()));

			return view;
		}
	}

	private ListView foodList;
	private ListView commentList;
	private Menu menu;
	private Caterer caterer;
	private ViewFlipper flipper;
	private EditText commentField;
	private TextView numAvgVotes;
	private View likesBar;

	private int day;
	private int catId;
	private boolean flipped = false;
	
	private static final String DATA_FLIPPED = "dflipped";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		Bundle bundle = getIntent().getExtras();

		catId = bundle.getInt(Constants.ARG_CAT_ID);
		day = bundle.getInt(Constants.ARG_DAY);

		menu = BaseData.getModel().getMenuList().get(day);
		caterer = menu.getCaterer(catId);

		likesBar = (View) findViewById(R.id.posRatingsBar);
		commentField = (EditText) findViewById(R.id.commentField);
		numAvgVotes = (TextView) findViewById(R.id.numAvgVotes);
		flipper = (ViewFlipper) findViewById(R.id.menuFlipper);
		foodList = (ListView) findViewById(R.id.foodList);
		commentList = (ListView) findViewById(R.id.commentList);
		
		foodList.setAdapter(new MenuListAdapter(this, menu.getFoodItems()[catId]));
		commentList.setAdapter(new CommentListAdapter(this, caterer.getCommentList()));

		ActionBar ab = getActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setTitle(Constants.CAT_NAMES[catId]);
		
		switch (catId) {
		case Constants.CAT_CATER:
			ab.setLogo(R.drawable.cater_icon);
			break;
		case Constants.CAT_INDIAN:
			ab.setLogo(R.drawable.indian_icon);
			break;
		case Constants.CAT_VEGGIE:
			ab.setLogo(R.drawable.vege_icon);
			break;
		}

		((TextView) findViewById(R.id.catererName)).setText(caterer.getName());
		((ImageButton) findViewById(R.id.commentsBtn)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View source) {
				flipped = !flipped;
				flipper.showNext();
			}
		});
		((Button) findViewById(R.id.submitBtn)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View source) {
				String text = commentField.getText().toString();
				commentField.setText("");
				caterer.addComment(text);
			}
		});

		updateLikes();
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		flipped = savedInstanceState.getBoolean(DATA_FLIPPED);
		if (flipped)
			flipper.showNext();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(DATA_FLIPPED, flipped);
	}
	
	protected void updateLikes() {
		numAvgVotes.setText(caterer.getNumRatings().toString());
		likesBar.getLayoutParams().width = (int)(getResources().getDimension(R.dimen.s160dp) * caterer.getLikeRatio());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
