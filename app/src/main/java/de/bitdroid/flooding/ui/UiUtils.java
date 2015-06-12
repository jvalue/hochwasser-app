package de.bitdroid.flooding.ui;


import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import javax.inject.Inject;

import de.bitdroid.flooding.R;

public class UiUtils {

	private final Context context;

	@Inject
	UiUtils(Context context) {
		this.context = context;
	}


	public void showSpinner(View spinnerContainerView, final ImageView spinnerImageView) {
		final int[] images = { R.drawable.spinner_01, R.drawable.spinner_02, R.drawable.spinner_03 };

		final Animation startFlipAnimation = AnimationUtils.loadAnimation(context, R.anim.flip_to_middle);
		final Animation stopFlipAnimation = AnimationUtils.loadAnimation(context, R.anim.flip_from_middle);

		spinnerImageView.setImageResource(images[0]);

		startFlipAnimation.setAnimationListener(new Animation.AnimationListener() {

			private int currentImageIdx = 0;

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				currentImageIdx = (currentImageIdx + 1) % images.length;
				spinnerImageView.setImageResource(images[currentImageIdx]);
				spinnerImageView.startAnimation(stopFlipAnimation);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		stopFlipAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {  }

			@Override
			public void onAnimationEnd(Animation animation) {
				spinnerImageView.startAnimation(startFlipAnimation);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {  }
		});

		spinnerImageView.clearAnimation();
		spinnerImageView.startAnimation(startFlipAnimation);

		spinnerContainerView.setVisibility(View.VISIBLE);
	}


	public void hideSpinner(View spinnerContainerView, ImageView spinnerImageView) {
		spinnerImageView.clearAnimation();
		spinnerContainerView.setVisibility(View.GONE);
	}

}