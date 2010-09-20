package com.vegalabs.amail.client;


import com.allen_sauer.gwt.log.client.DivLogger;
import com.allen_sauer.gwt.log.client.Log;
import org.cobogw.gwt.waveapi.gadget.client.WaveFeature;
import org.cobogw.gwt.waveapi.gadget.client.WaveGadget;

import com.vegalabs.amail.client.inject.GinjectorImpl;
import com.vegalabs.amail.client.ui.MailForm;
import com.vegalabs.features.client.feature.minimessages.MiniMessagesFeature;
import com.vegalabs.features.client.feature.minimessages.NeedsMiniMessages;
import com.vegalabs.features.client.feature.views.NeedsViews;
import com.vegalabs.features.client.feature.views.ViewsFeature;
import com.vegalabs.general.client.objects.AppDomainId;
import com.vegalabs.general.client.objects.GoogleAnalyticsId;
import com.google.gwt.gadgets.client.DynamicHeightFeature;
import com.google.gwt.gadgets.client.GoogleAnalyticsFeature;
import com.google.gwt.gadgets.client.NeedsDynamicHeight;
import com.google.gwt.gadgets.client.NeedsGoogleAnalytics;
import com.google.gwt.gadgets.client.UserPreferences;
import com.google.gwt.gadgets.client.Gadget.ModulePrefs;
import com.google.gwt.core.client.GWT;
import com.google.gwt.gadgets.client.Gadget.AllowHtmlQuirksMode;
import com.google.gwt.gadgets.client.Gadget.UseLongManifestName;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.inject.Provider;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
@AllowHtmlQuirksMode
@UseLongManifestName
@ModulePrefs(title = "Wave Address",author="Yuri Zelikov",author_email="vega113+waveaddress@gmail.com", width=800, height=300)
public class WaveMailGadget	extends WaveGadget<UserPreferences> implements NeedsDynamicHeight, NeedsMiniMessages, NeedsGoogleAnalytics, NeedsViews{

	
	

	@Override
	protected void init(UserPreferences preferences) {
		try{
			waveFeature = getWave();
			GinjectorImpl ginjector = GWT.create(GinjectorImpl.class);
			MailForm widget = ginjector.getMailForm();
			dhFeature.getContentDiv().add(widget);
			initRemoteLogger(RootPanel.get());
		}catch(Exception e){
			initRemoteLogger(RootPanel.get());
			handleError(e);
		}
		Timer timer = new Timer() {
			
			@Override
			public void run() {
				dhFeature.adjustHeight();
			}
		};
		timer.scheduleRepeating(800);
	}

	
	public void initRemoteLogger(AbsolutePanel panel){
		Log.setUncaughtExceptionHandler();
		if (panel != null) {
			panel.add (Log.getLogger(DivLogger.class).getWidget());
			Log.info("Logger initialized: " + Log.class.getName());
		}
	}

	private void handleError(Throwable error) {
		Log.error("", error);
	}


static WaveFeature waveFeature;
	
	static DynamicHeightFeature dhFeature;
	@Override
	public void initializeFeature(DynamicHeightFeature feature) {
		WaveMailGadget.dhFeature = feature;
		
	}
	
	static private MiniMessagesFeature mmFeature;
	@Override
	public void initializeFeature(MiniMessagesFeature feature) {
		WaveMailGadget.mmFeature = feature;
	}
	
	static private ViewsFeature viewsFeature;
	@Override
	public void initializeFeature(ViewsFeature feature) {
		WaveMailGadget.viewsFeature = feature;
	}
	
	static private GoogleAnalyticsFeature analyticsFeature;
	@Override
	public void initializeFeature(GoogleAnalyticsFeature analyticsFeature) {
		WaveMailGadget.analyticsFeature = analyticsFeature;
	}
	
	public static class AnalyticsFeatureProvider implements Provider<GoogleAnalyticsFeature>{
		@Override
		public GoogleAnalyticsFeature get() {
			Log.info("Providing AnalyticsFeature");
			return WaveMailGadget.analyticsFeature;
		}
	}
	
	public static class MiniMessagesFeatureProvider implements Provider<MiniMessagesFeature>{
		@Override
		public MiniMessagesFeature get() {
			Log.info("Providing MiniMessagesFeature");
			return WaveMailGadget.mmFeature;
		}
	}
	
	public static class DynamicHeightFeatureProvider implements Provider<DynamicHeightFeature>{
		@Override
		public DynamicHeightFeature get() {
			Log.info("Providing DynamicHeightFeature");
			return WaveMailGadget.dhFeature;
		}
	}
	
	public static class WaveFeatureProvider implements Provider<WaveFeature>{
		@Override
		public WaveFeature get() {
			Log.info("Providing WaveFeature");
			return WaveMailGadget.waveFeature;
		}
	}
	
	public static class ViewsFeatureProvider implements Provider<ViewsFeature>{
		@Override
		public ViewsFeature get() {
			Log.info("Providing ViewsFeature");
			return WaveMailGadget.viewsFeature;
		}
	}
	
	public static class AnalyticsIdFeatureProvider implements Provider<GoogleAnalyticsId>{
		@Override
		public GoogleAnalyticsId get() {
			Log.info("Providing GoogleAnalyticsId");
			return new GoogleAnalyticsId("UA-13269470-3");
		}
	}
	
	public static class AppDomainIdFeatureProvider implements Provider<AppDomainId>{

		@Override
		public AppDomainId get() {
			AppDomainId ad = new AppDomainId();
			ad.setId("mailwavybeta");
			Log.info("The applicationId is: " + ad.getId());
			return ad;
		}

	}


	
	
}
