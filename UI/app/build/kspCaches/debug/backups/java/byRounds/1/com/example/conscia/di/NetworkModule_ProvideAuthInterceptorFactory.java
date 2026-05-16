package com.example.conscia.di;

import com.example.conscia.data.TrackedAppsDataStore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.Interceptor;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class NetworkModule_ProvideAuthInterceptorFactory implements Factory<Interceptor> {
  private final Provider<TrackedAppsDataStore> dataStoreProvider;

  public NetworkModule_ProvideAuthInterceptorFactory(
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public Interceptor get() {
    return provideAuthInterceptor(dataStoreProvider.get());
  }

  public static NetworkModule_ProvideAuthInterceptorFactory create(
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    return new NetworkModule_ProvideAuthInterceptorFactory(dataStoreProvider);
  }

  public static Interceptor provideAuthInterceptor(TrackedAppsDataStore dataStore) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideAuthInterceptor(dataStore));
  }
}
