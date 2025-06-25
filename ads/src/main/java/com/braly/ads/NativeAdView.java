// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.braly.ads;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.braly.ads.data.AdPlacement;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;

import java.util.Arrays;

public class NativeAdView extends FrameLayout {
    private final int GRID_LAYOUT_ID_TOP = 0x1111111;
    private final int GRID_LAYOUT_ID_BOTTOM = 0x2222222;
    public int templateType;
    public TextView primaryView;
    public int maxNativeAdView;
    public TextView secondaryView;
    public TextView pricingView;
    public RatingBar ratingBar;
    public TextView tertiaryView;
    public ImageView iconView;
    public TextView callToActionView;
    public MediaView mediaView;
    public View background;
    public View closeButton;
    public View overlayButton;
    private AdPlacement adPlacement;

    public void setAdPlacement(AdPlacement adPlacement) {
        this.adPlacement = adPlacement;
    }

    public NativeAdView(Context context) {
        super(context);
        initView(context, null);
    }

    public NativeAdView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
    }

    public NativeAdView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    @SuppressLint("NewApi")
    public NativeAdView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs);
    }

    public boolean adHasOnlyStore(NativeAd nativeAd) {
        String store = nativeAd.getStore();
        String advertiser = nativeAd.getAdvertiser();
        return !TextUtils.isEmpty(store) && TextUtils.isEmpty(advertiser);
    }

    public void initView(Context context, AttributeSet attributeSet) {
        TypedArray attributes = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.NativeAdView, 0, 0);
        try {
            templateType = getTemplateType(attributes);
            maxNativeAdView = attributes.getResourceId(R.styleable.NativeAdView_max_ad_view_layout, R.layout.max_native_ad_view);
        } finally {
            attributes.recycle();
        }
        initLayout(context, templateType);
        initIds();
    }

    public void setAdmobTemplateType(int admobTemplateType) {
        templateType = admobTemplateType;
        removeAllViews();
        initLayout(getContext(), templateType);
        initIds();
    }

    private void initLayout(Context context, int layoutRes) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutRes, this);
    }

    protected int getTemplateType(TypedArray attributes) {
        return attributes.getResourceId(R.styleable.NativeAdView_ad_view_layout, R.layout.admob_native_ad_view);
    }

    private void initIds() {
        primaryView = (TextView) findViewById(R.id.primary);
        secondaryView = (TextView) findViewById(R.id.secondary);
        tertiaryView = (TextView) findViewById(R.id.body);
        ratingBar = (RatingBar) findViewById(R.id.rating_bar);
        if (ratingBar != null) ratingBar.setEnabled(false);
        callToActionView = (TextView) findViewById(R.id.cta);
        iconView = (ImageView) findViewById(R.id.icon);
        closeButton = (View) findViewById(R.id.iv_close);
        overlayButton = (View) findViewById(R.id.overlayButton);
        background = (View) findViewById(R.id.background);
        mediaView = (MediaView) findViewById(R.id.media_view);

       if (closeButton != null) {
           closeButton.setOnClickListener(new OnClickListener() {
               @Override
               public void onClick(View v) {
                   background.setVisibility(View.GONE);

               }
           });
           background.setVisibility(View.GONE);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }


    private void hideGridView() {
        for (Integer i : Arrays.asList(GRID_LAYOUT_ID_TOP, GRID_LAYOUT_ID_BOTTOM)) {
            View gridView = findViewById(i);
            if (gridView != null) gridView.setVisibility(View.GONE);
        }
    }

    public int getLayoutRes(String layoutName) {
        if (layoutName == null || layoutName.isEmpty()) return -1;
        try {
            return getContext().getResources().getIdentifier(layoutName, "layout", getContext().getPackageName());
        } catch (Exception exception) {
            return -1;
        }
    }
}
