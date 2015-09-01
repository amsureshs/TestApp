package audio.lisn.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.List;

import audio.lisn.R;
import audio.lisn.adapter.CoverFlowAdapter;
import audio.lisn.model.AudioBook;
import it.moondroid.coverflow.components.ui.containers.FeatureCoverFlow;

public class PlayerControllerActivity extends AppCompatActivity {
    private static final String TRANSITION_NAME = "audio.lisn.PlayerControllerActivity";
    AudioBook audioBook;
    private FeatureCoverFlow mCoverFlow;
    private CoverFlowAdapter mAdapter;
    private TextSwitcher mTitle;
    private List<AudioBook> items =new ArrayList<>(0);





    public static void navigate(AppCompatActivity activity, View transitionView, AudioBook audioBook) {
        Intent intent = new Intent(activity, PlayerControllerActivity.class);
        intent.putExtra("audioBook", audioBook);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, TRANSITION_NAME);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_player_controller);
        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), TRANSITION_NAME);

        audioBook = (AudioBook) getIntent().getSerializableExtra("audioBook");

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        supportStartPostponedEnterTransition();
        getSupportActionBar().setTitle(R.string.app_name);

        setupData();
    }
    private void setupData(){


        mTitle = (TextSwitcher) findViewById(R.id.play_book_title);
        mTitle.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                LayoutInflater inflater = LayoutInflater.from(PlayerControllerActivity.this);
                TextView textView = (TextView) inflater.inflate(R.layout.play_book_title, null);
                return textView;
            }
        });
        Animation in = AnimationUtils.loadAnimation(this, R.anim.slide_in_top);
        Animation out = AnimationUtils.loadAnimation(this, R.anim.slide_out_bottom);
        mTitle.setInAnimation(in);
        mTitle.setOutAnimation(out);

        AudioBook book1 = new AudioBook();
        book1.imageResId=R.drawable.image_1;
        book1.setEnglish_title("God of War Ascension");

        AudioBook book2 = new AudioBook();
        book2.imageResId=R.drawable.image_2;
        book2.setEnglish_title("Assassin\\'s Creed III");


        AudioBook book3 = new AudioBook();
        book3.imageResId=R.drawable.image_3;
        book3.setEnglish_title("Hitman Absolution");


        AudioBook book4 = new AudioBook();
        book4.imageResId=R.drawable.image_4;
        book4.setEnglish_title("Dishonored");


        items.add(book1);
        items.add(book2);
        items.add(book3);
        items.add(book4);

        mAdapter = new CoverFlowAdapter(this);
        mAdapter.setData(items);
        mCoverFlow = (FeatureCoverFlow) findViewById(R.id.coverflow);
        mCoverFlow.setAdapter(mAdapter);
        mCoverFlow.setReflectionHeight(0.2f);

        mCoverFlow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCoverFlow.scrollToPosition(position);
//                Toast.makeText(PlayerControllerActivity.this,
//                        getResources().getString(items.get(position).titleResId),
//                        Toast.LENGTH_SHORT).show();
            }
        });

        mCoverFlow.setOnScrollPositionListener(new FeatureCoverFlow.OnScrollPositionListener() {
            @Override
            public void onScrolledToPosition(int position) {
                mTitle.setText(items.get(position).getEnglish_title());
            }

            @Override
            public void onScrolling() {

                mTitle.setText("");
            }
        });

    }




    private void initActivityTransitions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide transition = new Slide();
            transition.excludeTarget(android.R.id.statusBarBackground, true);
            getWindow().setEnterTransition(transition);
            getWindow().setReturnTransition(transition);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
              this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
