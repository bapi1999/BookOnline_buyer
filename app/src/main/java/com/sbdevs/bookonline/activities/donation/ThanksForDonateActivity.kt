package com.sbdevs.bookonline.activities.donation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_ANY
import com.google.android.gms.ads.nativead.NativeAdView
import com.sbdevs.bookonline.R
import com.sbdevs.bookonline.databinding.ActivityThanksForDonateBinding
import com.sbdevs.bookonline.fragments.LoadingDialog

class ThanksForDonateActivity : AppCompatActivity() {
    private lateinit var binding:ActivityThanksForDonateBinding

    private val gone = View.GONE
    private val visible = View.VISIBLE
    private lateinit var nativeAdView:NativeAdView

    private val loadingDialog = LoadingDialog()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThanksForDonateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val newlyAddedPoint:Int = intent.getIntExtra("totalPoint",0)

        binding.newlyAddedPoint.text = "$newlyAddedPoint Points"

        loadingDialog.show(supportFragmentManager,"show")


        val videoOptions = VideoOptions.Builder()
//            .setStartMuted(true)
            .build()

        val adOptions = NativeAdOptions.Builder()
//            .setVideoOptions(videoOptions)
//            .setMediaAspectRatio(NATIVE_MEDIA_ASPECT_RATIO_ANY)
            .build()

        val adId = resources.getString(R.string.g_native_ad_norm)

        val adLoader = AdLoader.Builder(this, adId)
            .forNativeAd { nativeAd ->

                if (isDestroyed) {
                    nativeAd.destroy()
                    return@forNativeAd
                }

                displayNativeAd(nativeAd)
            }.withAdListener(object : AdListener() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    Log.d("Ads","clicked")
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    Log.e("Ads","Closed")
                }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("Ads Load Failed","${adError.message}")
                    loadingDialog.dismiss()
                    // Handle the failure by logging, altering the UI, and so on.
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    Log.e("Ads","On Impression")
                    loadingDialog.dismiss()
                }
            })
            .withNativeAdOptions(adOptions).build()

        adLoader.loadAd(AdRequest.Builder().build())

    }

    private fun displayNativeAd(ad: NativeAd) {

        // Inflate a layout and add it to the parent ViewGroup.
        nativeAdView = binding.layAds.nativeAdView
//        val inflater = LayoutInflater.from(this)
//        val adView = inflater.inflate(R.layout.ad_native_g_1,nativeAdView ,false) as NativeAdView


        val adLay = binding.layAds

        val icon = ad.icon
        val headline = ad.headline
        val starRating = ad.starRating
        val advertiser = ad.advertiser
        val body = ad.body
        val callToAction = ad.callToAction
        val media = ad.mediaContent
        val f = media.mainImage

//        val adHeadline:TextView = adView.findViewById<TextView>(R.id.ad_headline)
//        val adAppIcon:ImageView =  adView.findViewById<ImageView>(R.id.ad_app_icon)
//        val adAdvertiser:TextView =adView.findViewById<TextView>(R.id.ad_advertiser)
//        val adStars:RatingBar =adView.findViewById<RatingBar>(R.id.ad_stars)
//        val adBody:TextView =adView.findViewById<TextView>(R.id.ad_body)
//        val adMedia:MediaView =adView.findViewById<MediaView>(R.id.ad_media)
//        val cta:Button =adView.findViewById<Button>(R.id.cta)
//
//        adHeadline.text = headline.toString()
//        adView.headlineView = adHeadline
//        adMedia.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
//        adMedia.setMediaContent(media)
//        adView.mediaView = adMedia
//
//        Glide.with(this).load(icon.uri).into(adAppIcon)
//        adView.iconView = adAppIcon
//        adView.advertiserView = adAdvertiser
//        adView.bodyView = adBody
//        adView.starRatingView = adStars
//        adView.callToActionView = cta




        if (icon == null){
            adLay.adAppIcon.visibility = gone
        }else{
            adLay.adAppIcon.visibility = visible
            Glide.with(this).load(icon.uri).into(adLay.adAppIcon)
        }

        if (headline == null){
            adLay.adHeadline.visibility = gone
        }else{
            adLay.adHeadline.visibility = visible
            adLay.adHeadline.text = headline.toString()
        }

        if (advertiser == null){
            adLay.adAdvertiser.visibility = gone
        }else{
            adLay.adAdvertiser.visibility = visible
            adLay.adAdvertiser.text = headline.toString()
        }

        if (starRating == null){
            adLay.adStars.visibility = gone
        }else{
            adLay.adStars.visibility = visible
            adLay.adStars.rating = starRating.toFloat()
        }

        if (body == null){
            adLay.adBody.visibility = gone
        }else{
            adLay.adBody.visibility = visible
            adLay.adBody.text = body.toString()
        }

        if (media == null) {
            adLay.adMedia.visibility = gone
        }else{


            if (media.hasVideoContent()){
                adLay.adMedia.visibility = visible
                adLay.adMediaImage.visibility = gone
                adLay.adMedia.setMediaContent(media)
                nativeAdView.mediaView = adLay.adMedia
                Log.e("media", "Has video content")
            }else{
                adLay.adMedia.visibility = gone
                adLay.adMediaImage.visibility = visible
//                adLay.adMedia.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
//                adLay.adMedia.setMediaContent(media)
//                nativeAdView.mediaView = adLay.adMedia
                Glide.with(this).load(media.mainImage).into(adLay.adMediaImage)
                Log.e("media", "${media.mainImage}")
            }
        }

        if (callToAction == null){
            adLay.cta.visibility = gone
        }else{
            adLay.cta.visibility = visible
            adLay.cta.text = callToAction.toString()
            adLay.nativeAdView.callToActionView = adLay.cta
        }

        nativeAdView.setNativeAd(ad)

//        adView.setNativeAd(ad)
    }


}