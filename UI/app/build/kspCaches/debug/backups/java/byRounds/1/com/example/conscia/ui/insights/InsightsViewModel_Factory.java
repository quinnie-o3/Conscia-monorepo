package com.example.conscia.ui.insights;

import android.app.Application;
import com.example.conscia.data.remote.RemoteUsageSyncRepository;
import com.example.conscia.data.rule.RuleRepository;
import com.example.conscia.data.usage.UsageStatsRepository;
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
public final class InsightsViewModel_Factory implements Factory<InsightsViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<UsageStatsRepository> usageRepositoryProvider;

  private final Provider<RuleRepository> ruleRepositoryProvider;

  private final Provider<RemoteUsageSyncRepository> remoteUsageSyncRepositoryProvider;

  public InsightsViewModel_Factory(Provider<Application> applicationProvider,
      Provider<UsageStatsRepository> usageRepositoryProvider,
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<RemoteUsageSyncRepository> remoteUsageSyncRepositoryProvider) {
    this.applicationProvider = applicationProvider;
    this.usageRepositoryProvider = usageRepositoryProvider;
    this.ruleRepositoryProvider = ruleRepositoryProvider;
    this.remoteUsageSyncRepositoryProvider = remoteUsageSyncRepositoryProvider;
  }

  @Override
  public InsightsViewModel get() {
    return newInstance(applicationProvider.get(), usageRepositoryProvider.get(), ruleRepositoryProvider.get(), remoteUsageSyncRepositoryProvider.get());
  }

  public static InsightsViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<UsageStatsRepository> usageRepositoryProvider,
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<RemoteUsageSyncRepository> remoteUsageSyncRepositoryProvider) {
    return new InsightsViewModel_Factory(applicationProvider, usageRepositoryProvider, ruleRepositoryProvider, remoteUsageSyncRepositoryProvider);
  }

  public static InsightsViewModel newInstance(Application application,
      UsageStatsRepository usageRepository, RuleRepository ruleRepository,
      RemoteUsageSyncRepository remoteUsageSyncRepository) {
    return new InsightsViewModel(application, usageRepository, ruleRepository, remoteUsageSyncRepository);
  }
}
