package com.example.conscia.domain.usecase;

import com.example.conscia.data.rule.RuleRepository;
import com.example.conscia.data.usage.UsageStatsRepository;
import com.example.conscia.data.warning.WarningHistoryStore;
import com.example.conscia.notification.ConsciaNotificationManager;
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
public final class CheckUsageLimitWarningsUseCase_Factory implements Factory<CheckUsageLimitWarningsUseCase> {
  private final Provider<RuleRepository> ruleRepositoryProvider;

  private final Provider<UsageStatsRepository> usageRepositoryProvider;

  private final Provider<WarningHistoryStore> warningHistoryStoreProvider;

  private final Provider<ConsciaNotificationManager> notificationManagerProvider;

  private final Provider<EvaluateTrackedAppsUsageUseCase> evaluateUseCaseProvider;

  public CheckUsageLimitWarningsUseCase_Factory(Provider<RuleRepository> ruleRepositoryProvider,
      Provider<UsageStatsRepository> usageRepositoryProvider,
      Provider<WarningHistoryStore> warningHistoryStoreProvider,
      Provider<ConsciaNotificationManager> notificationManagerProvider,
      Provider<EvaluateTrackedAppsUsageUseCase> evaluateUseCaseProvider) {
    this.ruleRepositoryProvider = ruleRepositoryProvider;
    this.usageRepositoryProvider = usageRepositoryProvider;
    this.warningHistoryStoreProvider = warningHistoryStoreProvider;
    this.notificationManagerProvider = notificationManagerProvider;
    this.evaluateUseCaseProvider = evaluateUseCaseProvider;
  }

  @Override
  public CheckUsageLimitWarningsUseCase get() {
    return newInstance(ruleRepositoryProvider.get(), usageRepositoryProvider.get(), warningHistoryStoreProvider.get(), notificationManagerProvider.get(), evaluateUseCaseProvider.get());
  }

  public static CheckUsageLimitWarningsUseCase_Factory create(
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<UsageStatsRepository> usageRepositoryProvider,
      Provider<WarningHistoryStore> warningHistoryStoreProvider,
      Provider<ConsciaNotificationManager> notificationManagerProvider,
      Provider<EvaluateTrackedAppsUsageUseCase> evaluateUseCaseProvider) {
    return new CheckUsageLimitWarningsUseCase_Factory(ruleRepositoryProvider, usageRepositoryProvider, warningHistoryStoreProvider, notificationManagerProvider, evaluateUseCaseProvider);
  }

  public static CheckUsageLimitWarningsUseCase newInstance(RuleRepository ruleRepository,
      UsageStatsRepository usageRepository, WarningHistoryStore warningHistoryStore,
      ConsciaNotificationManager notificationManager,
      EvaluateTrackedAppsUsageUseCase evaluateUseCase) {
    return new CheckUsageLimitWarningsUseCase(ruleRepository, usageRepository, warningHistoryStore, notificationManager, evaluateUseCase);
  }
}
