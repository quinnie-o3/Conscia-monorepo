package com.example.conscia.ui.settings;

import com.example.conscia.data.TrackedAppsDataStore;
import com.example.conscia.data.remote.api.ConsciaApiService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ProfileViewModel_Factory implements Factory<ProfileViewModel> {
  private final Provider<ConsciaApiService> apiServiceProvider;

  private final Provider<TrackedAppsDataStore> dataStoreProvider;

  public ProfileViewModel_Factory(Provider<ConsciaApiService> apiServiceProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public ProfileViewModel get() {
    return newInstance(apiServiceProvider.get(), dataStoreProvider.get());
  }

  public static ProfileViewModel_Factory create(Provider<ConsciaApiService> apiServiceProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    return new ProfileViewModel_Factory(apiServiceProvider, dataStoreProvider);
  }

  public static ProfileViewModel newInstance(ConsciaApiService apiService,
      TrackedAppsDataStore dataStore) {
    return new ProfileViewModel(apiService, dataStore);
  }
}
