package de.bitdroid.flooding.ods;

import android.content.Context;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

import org.jvalue.ods.api.DataApi;

import javax.inject.Inject;
import javax.inject.Named;

import de.bitdroid.flooding.R;
import de.bitdroid.flooding.auth.AuthClient;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;


/**
 * Dependecy injection configuratoin for CEPS related classes.
 */
public final class OdsModule implements Module {

	public static final String NAME_ODS_REST_ADAPTER = "ODS_REST_ADAPTER";

	@Override
	public void configure(Binder binder) {
		// nothing to do for now
	}


	@Provides
	@Inject
	@Named(NAME_ODS_REST_ADAPTER)
	public RestAdapter provideOdsRestAdapter(Context context, AuthClient authClient) {
		return new RestAdapter.Builder()
				.setEndpoint(context.getString(R.string.ods_base_url))
				.setConverter(new JacksonConverter())
				.build();
	}


	@Provides
	@Inject
	public DataApi provideUserApi(@Named(NAME_ODS_REST_ADAPTER) RestAdapter restAdapter) {
		return restAdapter.create(DataApi.class);
	}

}
