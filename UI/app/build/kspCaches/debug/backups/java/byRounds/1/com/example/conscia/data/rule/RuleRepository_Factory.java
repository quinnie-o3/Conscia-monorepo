package com.example.conscia.data.rule;

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
public final class RuleRepository_Factory implements Factory<RuleRepository> {
  private final Provider<RuleDao> ruleDaoProvider;

  private final Provider<ConsciaApiService> apiServiceProvider;

  private final Provider<TrackedAppsDataStore> dataStoreProvider;

  public RuleRepository_Factory(Provider<RuleDao> ruleDaoProvider,
      Provider<ConsciaApiService> apiServiceProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    this.ruleDaoProvider = ruleDaoProvider;
    this.apiServiceProvider = apiServiceProvider;
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public RuleRepository get() {
    return newInstance(ruleDaoProvider.get(), apiServiceProvider.get(), dataStoreProvider.get());
  }

  public static RuleRepository_Factory create(Provider<RuleDao> ruleDaoProvider,
      Provider<ConsciaApiService> apiServiceProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    return new RuleRepository_Factory(ruleDaoProvider, apiServiceProvider, dataStoreProvider);
  }

  public static RuleRepository newInstance(RuleDao ruleDao, ConsciaApiService apiService,
      TrackedAppsDataStore dataStore) {
    return new RuleRepository(ruleDao, apiService, dataStore);
  }
}
