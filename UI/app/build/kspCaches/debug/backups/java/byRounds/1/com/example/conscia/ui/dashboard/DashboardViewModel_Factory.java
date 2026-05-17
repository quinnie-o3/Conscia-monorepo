package com.example.conscia.ui.dashboard;

import android.app.Application;
import com.example.conscia.data.TrackedAppsDataStore;
import com.example.conscia.data.remote.api.ConsciaApiService;
import com.example.conscia.data.rule.RuleRepository;
import com.example.conscia.data.weekly.WeeklySummaryManager;
import com.example.conscia.domain.usecase.EvaluateTrackedAppsUsageUseCase;
import com.example.conscia.domain.usecase.GetRulesUseCase;
import com.example.conscia.domain.usecase.GetTodayUsageUseCase;
import com.example.conscia.domain.usecase.GetWeeklyUsageUseCase;
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
public final class DashboardViewModel_Factory implements Factory<DashboardViewModel> {
  private final Provider<Application> applicationProvider;

  private final Provider<GetTodayUsageUseCase> getTodayUsageUseCaseProvider;

  private final Provider<GetWeeklyUsageUseCase> getWeeklyUsageUseCaseProvider;

  private final Provider<GetRulesUseCase> getRulesUseCaseProvider;

  private final Provider<WeeklySummaryManager> weeklySummaryManagerProvider;

  private final Provider<EvaluateTrackedAppsUsageUseCase> evaluateUseCaseProvider;

  private final Provider<RuleRepository> ruleRepositoryProvider;

  private final Provider<ConsciaApiService> apiServiceProvider;

  private final Provider<TrackedAppsDataStore> dataStoreProvider;

  public DashboardViewModel_Factory(Provider<Application> applicationProvider,
      Provider<GetTodayUsageUseCase> getTodayUsageUseCaseProvider,
      Provider<GetWeeklyUsageUseCase> getWeeklyUsageUseCaseProvider,
      Provider<GetRulesUseCase> getRulesUseCaseProvider,
      Provider<WeeklySummaryManager> weeklySummaryManagerProvider,
      Provider<EvaluateTrackedAppsUsageUseCase> evaluateUseCaseProvider,
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<ConsciaApiService> apiServiceProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    this.applicationProvider = applicationProvider;
    this.getTodayUsageUseCaseProvider = getTodayUsageUseCaseProvider;
    this.getWeeklyUsageUseCaseProvider = getWeeklyUsageUseCaseProvider;
    this.getRulesUseCaseProvider = getRulesUseCaseProvider;
    this.weeklySummaryManagerProvider = weeklySummaryManagerProvider;
    this.evaluateUseCaseProvider = evaluateUseCaseProvider;
    this.ruleRepositoryProvider = ruleRepositoryProvider;
    this.apiServiceProvider = apiServiceProvider;
    this.dataStoreProvider = dataStoreProvider;
  }

  @Override
  public DashboardViewModel get() {
    return newInstance(applicationProvider.get(), getTodayUsageUseCaseProvider.get(), getWeeklyUsageUseCaseProvider.get(), getRulesUseCaseProvider.get(), weeklySummaryManagerProvider.get(), evaluateUseCaseProvider.get(), ruleRepositoryProvider.get(), apiServiceProvider.get(), dataStoreProvider.get());
  }

  public static DashboardViewModel_Factory create(Provider<Application> applicationProvider,
      Provider<GetTodayUsageUseCase> getTodayUsageUseCaseProvider,
      Provider<GetWeeklyUsageUseCase> getWeeklyUsageUseCaseProvider,
      Provider<GetRulesUseCase> getRulesUseCaseProvider,
      Provider<WeeklySummaryManager> weeklySummaryManagerProvider,
      Provider<EvaluateTrackedAppsUsageUseCase> evaluateUseCaseProvider,
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<ConsciaApiService> apiServiceProvider,
      Provider<TrackedAppsDataStore> dataStoreProvider) {
    return new DashboardViewModel_Factory(applicationProvider, getTodayUsageUseCaseProvider, getWeeklyUsageUseCaseProvider, getRulesUseCaseProvider, weeklySummaryManagerProvider, evaluateUseCaseProvider, ruleRepositoryProvider, apiServiceProvider, dataStoreProvider);
  }

  public static DashboardViewModel newInstance(Application application,
      GetTodayUsageUseCase getTodayUsageUseCase, GetWeeklyUsageUseCase getWeeklyUsageUseCase,
      GetRulesUseCase getRulesUseCase, WeeklySummaryManager weeklySummaryManager,
      EvaluateTrackedAppsUsageUseCase evaluateUseCase, RuleRepository ruleRepository,
      ConsciaApiService apiService, TrackedAppsDataStore dataStore) {
    return new DashboardViewModel(application, getTodayUsageUseCase, getWeeklyUsageUseCase, getRulesUseCase, weeklySummaryManager, evaluateUseCase, ruleRepository, apiService, dataStore);
  }
}
