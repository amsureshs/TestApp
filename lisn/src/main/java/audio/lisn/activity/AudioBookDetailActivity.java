package audio.lisn.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import audio.lisn.R;
import audio.lisn.model.AudioBook;
import audio.lisn.util.CustomTypeFace;

public class AudioBookDetailActivity extends  AppCompatActivity {

    private static final String TRANSITION_NAME = "audio.lisn.AudioBookDetailActivity";
    private CollapsingToolbarLayout collapsingToolbarLayout;
    AudioBook audioBook;


    public static void navigate(AppCompatActivity activity, View transitionImage, AudioBook audioBook) {
        Intent intent = new Intent(activity, AudioBookDetailActivity.class);
        intent.putExtra("audioBook", audioBook);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionImage, TRANSITION_NAME);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

    @SuppressWarnings("ConstantConditions")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActivityTransitions();
        setContentView(R.layout.activity_audio_book_detail);
        ViewCompat.setTransitionName(findViewById(R.id.app_bar_layout), TRANSITION_NAME);

        audioBook = (AudioBook) getIntent().getSerializableExtra("audioBook");

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        supportStartPostponedEnterTransition();

        String itemTitle = audioBook.getEnglish_title();
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(itemTitle);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));


        updateData();


    }

    private void updateData() {

        ImageView bookBannerImage = (ImageView) findViewById(R.id.bookBannerImage);
        String bannerImageUrl="http://lorempixel.com/500/500/animals/8/";

        Picasso.with(this).load(bannerImageUrl).into(bookBannerImage);
        Picasso.with(this)
                .load(bannerImageUrl)
                .placeholder(R.drawable.drawer_background)
                .into(bookBannerImage);

        final ImageView bookCoverImage = (ImageView) findViewById(R.id.bookCoverImage);
        Picasso.with(this)
                .load(audioBook.getCover_image())
                .placeholder(R.drawable.audiobook_placeholder)
                .into(bookCoverImage);

        String narratorText="";
        String durationText="";
        //ExpandableTextView descriptionTextView = (ExpandableTextView)findViewById(R.id.description);
        TextView title = (TextView) findViewById(R.id.title);
        TextView description = (TextView) findViewById(R.id.description);
        TextView category = (TextView) findViewById(R.id.category);
        TextView price = (TextView) findViewById(R.id.price);
        TextView author = (TextView) findViewById(R.id.author);
        TextView narrator = (TextView) findViewById(R.id.narrator);
        TextView ratingValue = (TextView) findViewById(R.id.rating_value);
        RatingBar ratingBar = (RatingBar) findViewById(R.id.rating_bar);

        if(audioBook.getLanguageCode()== AudioBook.LanguageCode.LAN_SI){
            description.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            title.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            author.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            category.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            narrator.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
          //  duration.setTypeface(CustomTypeFace.getSinhalaTypeFace(getApplicationContext()));
            narratorText=getString(R.string.narrator_si);
            durationText=getString(R.string.duration_si);
        }else{
            description.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            title.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            author.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            category.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
            narrator.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));
          //  duration.setTypeface(CustomTypeFace.getEnglishTypeFace(getApplicationContext()));

            narratorText=getString(R.string.narrator_en);
            durationText=getString(R.string.duration_en);
        }
        title.setText(audioBook.getTitle());
        String priceText="Free";
        if( Float.parseFloat(audioBook.getPrice())>0 ){
            priceText="Rs: "+audioBook.getPrice();
        }

        if(Float.parseFloat(audioBook.getRate())>-1){
            ratingBar.setRating(Float.parseFloat(audioBook.getRate()));
            ratingValue.setText(String.format("%.1f", Float.parseFloat(audioBook.getRate())));


        }

        narratorText=narratorText+" - "+audioBook.getNarrator();
        durationText=durationText+" - "+audioBook.getDuration();
        author.setText(audioBook.getAuthor());
        category.setText(audioBook.getCategory());
        narrator.setText(narratorText);
       // duration.setText(durationText);
        price.setText(priceText);
        title.setText(audioBook.getTitle());
        description.setText(audioBook.getDescription());
        if(audioBook.isPurchase()){
           // btnDownload.setImageResource(R.drawable.btn_lisn_book_large);
        }else{
           // btnDownload.setImageResource(R.drawable.btn_buy_book_large);


        }

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
    private void SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
