package com.example.conscia.data.weekly;

import com.example.conscia.data.usage.UsageStatsRepository;
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
public final class WeeklySummaryManager_Factory implements Factory<WeeklySummaryManager> {
  private final Provider<UsageStatsRepository> usageRepositoryProvider;

  private final Provider<WeeklySummaryStore> weeklySummaryStoreProvider;

  public WeeklySummaryManager_Factory(Provider<UsageStatsRepository> usageRepositoryProvider,
      Provider<WeeklySummaryStore> weeklySummaryStoreProvider) {
    this.usageRepositoryProvider = usageRepositoryProvider;
    this.weeklySummaryStoreProvider = weeklySummaryStoreProvider;
  }

  @Override
  public WeeklySummaryManager get() {
    return newInstance(usageRepositoryProvider.get(), weeklySummaryStoreProvider.get());
  }

  public static WeeklySummaryManager_Factory create(
      Provider<UsageStatsRepository> usageRepositoryProvider,
      Provider<WeeklySummaryStore> weeklySummaryStoreProvider) {
    return new WeeklySummaryManager_Factory(usageRepositoryProvider, weeklySummaryStoreProvider);
  }

  public static WeeklySummaryManager newInstance(UsageStatsRepository usageRepository,
      WeeklySummaryStore weeklySummaryStore) {
    return new WeeklySummaryManager(usageRepository, weeklySummaryStore);
  }
}
