package com.example.conscia.ui.onboarding;

import com.example.conscia.data.AppRepository;
import com.example.conscia.data.TrackedAppsDataStore;
import com.example.conscia.data.remote.api.ConsciaApiService;
import com.example.conscia.data.rule.RuleRepository;
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
public final class StarterRulesViewModel_Factory implements Factory<StarterRulesViewModel> {
  private final Provider<AppRepository> appRepositoryProvider;

  private final Provider<TrackedAppsDataStore> dataStoreProvider;

  private final Provider<RuleRepository> ruleRepositoryProvider;

  private final Provider<ConsciaApiService> apiServiceProvider;

  public StarterRulesViewModel_Factory(Provider<AppRepository> appRepositoryProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider,
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<ConsciaApiService> apiServiceProvider) {
    this.appRepositoryProvider = appRepositoryProvider;
    this.dataStoreProvider = dataStoreProvider;
    this.ruleRepositoryProvider = ruleRepositoryProvider;
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public StarterRulesViewModel get() {
    return newInstance(appRepositoryProvider.get(), dataStoreProvider.get(), ruleRepositoryProvider.get(), apiServiceProvider.get());
  }

  public static StarterRulesViewModel_Factory create(Provider<AppRepository> appRepositoryProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider,
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<ConsciaApiService> apiServiceProvider) {
    return new StarterRulesViewModel_Factory(appRepositoryProvider, dataStoreProvider, ruleRepositoryProvider, apiServiceProvider);
  }

  public static StarterRulesViewModel newInstance(AppRepository appRepository,
      TrackedAppsDataStore dataStore, RuleRepository ruleRepository, ConsciaApiService apiService) {
    return new StarterRulesViewModel(appRepository, dataStore, ruleRepository, apiService);
  }
}
