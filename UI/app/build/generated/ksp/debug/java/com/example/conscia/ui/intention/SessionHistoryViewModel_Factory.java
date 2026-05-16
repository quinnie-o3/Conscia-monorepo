package com.example.conscia.ui.intention;

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
public final class SessionHistoryViewModel_Factory implements Factory<SessionHistoryViewModel> {
  private final Provider<ConsciaApiService> apiServiceProvider;

  private final Provider<TrackedAppsDataStore> dataStoreProvider;

  public SessionHistoryViewModel_Factory(Provider<ConsciaApiService> apiServiceProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    this.apiServiceProvider = apiServiceProvider;
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public SessionHistoryViewModel get() {
    return newInstance(apiServiceProvider.get(), dataStoreProvider.get());
  }

  public static SessionHistoryViewModel_Factory create(
      Provider<ConsciaApiService> apiServiceProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    return new SessionHistoryViewModel_Factory(apiServiceProvider, dataStoreProvider);
  }

  public static SessionHistoryViewModel newInstance(ConsciaApiService apiService,
      TrackedAppsDataStore dataStore) {
    return new SessionHistoryViewModel(apiService, dataStore);
  }
}
