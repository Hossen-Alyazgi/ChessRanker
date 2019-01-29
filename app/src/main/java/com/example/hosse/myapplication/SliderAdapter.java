package com.example.hosse.myapplication;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context){
        this.context = context;
    }

    public int[] slide_images ={
            R.drawable.tower__chess_rook,
            R.drawable.games_swordsfight,
            R.drawable.challengemountain
    };
    public String[] slide_headings = {
            "Welcome to Chess Club!",
            "Challenge",
            "Improve"
    };

    public String[] slide_descs = {
            "This app allows you to track your chess ranking, known as ELO, in relation to people " +
                    "you play chess with. You can use this anywhere, from a chess club to playing with friends",
            "This app simply tracks your ranking, you have to face people and challenge them using actual chess boards! " +
                    "All your opponent needs is the app so you can connect and track who wins or loses",
            "Through this, you can see how you improve throughout your chess journey, see how good you are " +
                    "compared to your friends, and maybe even brag a little"
    };
    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position){

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;
        View view = layoutInflater.inflate(R.layout.slides, container,false);

        ImageView slideImageView = view.findViewById(R.id.slide_image);
        TextView slideHeading = view.findViewById(R.id.slide_heading);
        TextView slideDescription = view.findViewById(R.id.slide_desc);

        slideImageView.setImageResource(slide_images[position]);
        slideHeading.setText(slide_headings[position]);
        slideDescription.setText(slide_descs[position]);

        container.addView(view);

        return view;

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((RelativeLayout)object);
    }
}

