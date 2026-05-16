package com.example.conscia.ui.auth;

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
public final class AuthViewModel_Factory implements Factory<AuthViewModel> {
  private final Provider<TrackedAppsDataStore> dataStoreProvider;

  private final Provider<ConsciaApiService> apiServiceProvider;

  private final Provider<RuleRepository> ruleRepositoryProvider;

  public AuthViewModel_Factory(Provider<TrackedAppsDataStore> dataStoreProvider,
      Provider<ConsciaApiService> apiServiceProvider,
      Provider<RuleRepository> ruleRepositoryProvider) {
    this.dataStoreProvider = dataStoreProvider;
    this.apiServiceProvider = apiServiceProvider;
    this.ruleRepositoryProvider = ruleRepositoryProvider;
  }

  @Override
  public AuthViewModel get() {
    return newInstance(dataStoreProvider.get(), apiServiceProvider.get(), ruleRepositoryProvider.get());
  }

  public static AuthViewModel_Factory create(Provider<TrackedAppsDataStore> dataStoreProvider,
      Provider<ConsciaApiService> apiServiceProvider,
      Provider<RuleRepository> ruleRepositoryProvider) {
    return new AuthViewModel_Factory(dataStoreProvider, apiServiceProvider, ruleRepositoryProvider);
  }

  public static AuthViewModel newInstance(TrackedAppsDataStore dataStore,
      ConsciaApiService apiService, RuleRepository ruleRepository) {
    return new AuthViewModel(dataStore, apiService, ruleRepository);
  }
}
