package com.example.conscia.ui.onboarding;

import com.example.conscia.data.AppRepository;
import com.example.conscia.data.TrackedAppsDataStore;
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
public final class ChooseAppsViewModel_Factory implements Factory<ChooseAppsViewModel> {
  private final Provider<AppRepository> repositoryProvider;

  private final Provider<TrackedAppsDataStore> dataStoreProvider;

  public ChooseAppsViewModel_Factory(Provider<AppRepository> repositoryProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    this.repositoryProvider = repositoryProvider;
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public ChooseAppsViewModel get() {
    return newInstance(repositoryProvider.get(), dataStoreProvider.get());
  }

  public static ChooseAppsViewModel_Factory create(Provider<AppRepository> repositoryProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    return new ChooseAppsViewModel_Factory(repositoryProvider, dataStoreProvider);
  }

  public static ChooseAppsViewModel newInstance(AppRepository repository,
      TrackedAppsDataStore dataStore) {
    return new ChooseAppsViewModel(repository, dataStore);
  }
}
