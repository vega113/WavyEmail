
package com.vegalabs.amail.client.inject;

import org.cobogw.gwt.waveapi.gadget.client.WaveFeature;

import com.vegalabs.features.client.feature.minimessages.MiniMessagesFeature;
import com.vegalabs.features.client.feature.views.ViewsFeature;
import com.vegalabs.features.client.request.GadgetRequestServiceImpl;
import com.vegalabs.features.client.utils.WaveVegaUtilsImpl;
import com.vegalabs.general.client.objects.AppDomainId;
import com.vegalabs.general.client.objects.GoogleAnalyticsId;
import com.vegalabs.general.client.request.RequestService;
import com.vegalabs.general.client.utils.VegaUtils;
import com.vegalabs.amail.client.WaveMailGadget;
import com.vegalabs.amail.client.service.IService;
import com.vegalabs.amail.client.service.ServiceImpl;
import com.google.gwt.gadgets.client.DynamicHeightFeature;
import com.google.gwt.gadgets.client.GoogleAnalyticsFeature;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 * This gin module binds an implementation for the
 * {@link com.google.gwt.inject.example.simple.client.SimpleService} used in
 * this example application. Note that we don't have to bind implementations
 * for {@link com.google.gwt.inject.DigestConstants.simple.client.SimpleConstants} and
 * {@link com.google.gwt.inject.DigestMessages.simple.client.SimpleMessages} - they
 * are constructed by Gin through GWT.create.
 */
public class GinModuleImpl extends AbstractGinModule {

	protected void configure() {

		bind(IService.class).to(ServiceImpl.class);
		bind(VegaUtils.class).to(WaveVegaUtilsImpl.class);
		bind(RequestService.class).to(GadgetRequestServiceImpl.class).in(Singleton.class);

		bind(WaveFeature.class).toProvider(WaveMailGadget.WaveFeatureProvider.class).in(Singleton.class);
		bind(GoogleAnalyticsFeature.class).toProvider(WaveMailGadget.AnalyticsFeatureProvider.class).in(Singleton.class);
		bind(MiniMessagesFeature.class).toProvider(WaveMailGadget.MiniMessagesFeatureProvider.class).in(Singleton.class);
		bind(DynamicHeightFeature.class).toProvider(WaveMailGadget.DynamicHeightFeatureProvider.class).in(Singleton.class);
		bind(ViewsFeature.class).toProvider(WaveMailGadget.ViewsFeatureProvider.class).in(Singleton.class);
		bind(GoogleAnalyticsId.class).toProvider(WaveMailGadget.AnalyticsIdFeatureProvider.class).in(Singleton.class);
		bind(AppDomainId.class).toProvider(WaveMailGadget.AppDomainIdFeatureProvider.class).in(Singleton.class);
	}
}
