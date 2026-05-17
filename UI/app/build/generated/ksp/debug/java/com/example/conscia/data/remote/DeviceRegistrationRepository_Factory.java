package com.example.conscia.data.remote;

import com.example.conscia.data.TrackedAppsDataStore;
import com.example.conscia.data.remote.api.ConsciaApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class DeviceRegistrationRepository_Factory implements Factory<DeviceRegistrationRepository> {
  private final Provider<ConsciaApiService> apiServiceProvider;

  private final Provider<TrackedAppsDataStore> dataStoreProvider;

  public DeviceRegistrationRepository_Factory(Provider<ConsciaApiService> apiServiceProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public DeviceRegistrationRepository get() {
    return newInstance(apiServiceProvider.get(), dataStoreProvider.get());
  }

  public static DeviceRegistrationRepository_Factory create(
      Provider<ConsciaApiService> apiServiceProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    return new DeviceRegistrationRepository_Factory(apiServiceProvider, dataStoreProvider);
  }

  public static DeviceRegistrationRepository newInstance(ConsciaApiService apiService,
      TrackedAppsDataStore dataStore) {
    return new DeviceRegistrationRepository(apiService, dataStore);
  }
}
