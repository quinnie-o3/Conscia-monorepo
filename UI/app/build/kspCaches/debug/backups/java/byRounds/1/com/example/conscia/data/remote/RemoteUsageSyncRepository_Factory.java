package com.example.conscia.data.remote;

import android.content.Context;
import com.example.conscia.data.TrackedAppsDataStore;
import com.example.conscia.data.remote.api.ConsciaApiService;
import com.example.conscia.data.rule.RuleRepository;
import com.example.conscia.data.usage.UsageStatsRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class RemoteUsageSyncRepository_Factory implements Factory<RemoteUsageSyncRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<UsageStatsRepository> usageRepositoryProvider;

  private final Provider<RuleRepository> ruleRepositoryProvider;

  private final Provider<TrackedAppsDataStore> dataStoreProvider;

  private final Provider<ConsciaApiService> apiServiceProvider;

  public RemoteUsageSyncRepository_Factory(Provider<Context> contextProvider,
      Provider<UsageStatsRepository> usageRepositoryProvider,
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider,
      Provider<ConsciaApiService> apiServiceProvider) {
    this.contextProvider = contextProvider;
    this.usageRepositoryProvider = usageRepositoryProvider;
    this.ruleRepositoryProvider = ruleRepositoryProvider;
    this.dataStoreProvider = dataStoreProvider;
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public RemoteUsageSyncRepository get() {
    return newInstance(contextProvider.get(), usageRepositoryProvider.get(), ruleRepositoryProvider.get(), dataStoreProvider.get(), apiServiceProvider.get());
  }

  public static RemoteUsageSyncRepository_Factory create(Provider<Context> contextProvider,
      Provider<UsageStatsRepository> usageRepositoryProvider,
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider,
      Provider<ConsciaApiService> apiServiceProvider) {
    return new RemoteUsageSyncRepository_Factory(contextProvider, usageRepositoryProvider, ruleRepositoryProvider, dataStoreProvider, apiServiceProvider);
  }

  public static RemoteUsageSyncRepository newInstance(Context context,
      UsageStatsRepository usageRepository, RuleRepository ruleRepository,
      TrackedAppsDataStore dataStore, ConsciaApiService apiService) {
    return new RemoteUsageSyncRepository(context, usageRepository, ruleRepository, dataStore, apiService);
  }
}
