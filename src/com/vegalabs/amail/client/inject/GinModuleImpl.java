
package com.vegalabs.amail.client.inject;

import com.vegalabs.general.client.request.GwtRequestServiceImpl;
import com.vegalabs.general.client.request.RequestService;
import com.vegalabs.general.client.utils.VegaUtils;
import com.vegalabs.general.client.utils.VegaUtilsImpl;
import com.vegalabs.amail.client.service.IService;
import com.vegalabs.amail.client.service.ServiceImpl;
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
		bind(RequestService.class).to(GwtRequestServiceImpl.class).in(Singleton.class);

		bind(VegaUtils.class).to(VegaUtilsImpl.class).in(Singleton.class);
		//    
		//    bind(WaveFeature.class).toProvider(WaveMonitory.WaveFeatureProvider.class).in(Singleton.class);
		//    bind(GoogleAnalyticsFeature.class).toProvider(WaveMonitory.AnalyticsFeatureProvider.class).in(Singleton.class);
		//    bind(MiniMessagesFeature.class).toProvider(WaveMonitory.MiniMessagesFeatureProvider.class).in(Singleton.class);
		//    bind(DynamicHeightFeature.class).toProvider(WaveMonitory.DynamicHeightFeatureProvider.class).in(Singleton.class);
		//    bind(ViewsFeature.class).toProvider(WaveMonitory.ViewsFeatureProvider.class).in(Singleton.class);
	}
}