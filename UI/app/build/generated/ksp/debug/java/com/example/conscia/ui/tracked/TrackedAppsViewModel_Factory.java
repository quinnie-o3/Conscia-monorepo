package com.example.conscia.ui.tracked;

import com.example.conscia.domain.usecase.EvaluateTrackedAppsUsageUseCase;
import com.example.conscia.domain.usecase.GetRulesUseCase;
import com.example.conscia.domain.usecase.GetTodayUsageUseCase;
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
public final class TrackedAppsViewModel_Factory implements Factory<TrackedAppsViewModel> {
  private final Provider<GetRulesUseCase> getRulesUseCaseProvider;

  private final Provider<GetTodayUsageUseCase> getTodayUsageUseCaseProvider;

  private final Provider<EvaluateTrackedAppsUsageUseCase> evaluateUseCaseProvider;

  public TrackedAppsViewModel_Factory(Provider<GetRulesUseCase> getRulesUseCaseProvider,
      Provider<GetTodayUsageUseCase> getTodayUsageUseCaseProvider,
      Provider<EvaluateTrackedAppsUsageUseCase> evaluateUseCaseProvider) {
    this.getRulesUseCaseProvider = getRulesUseCaseProvider;
    this.getTodayUsageUseCaseProvider = getTodayUsageUseCaseProvider;
    this.evaluateUseCaseProvider = evaluateUseCaseProvider;
  }

  @Override
  public TrackedAppsViewModel get() {
    return newInstance(getRulesUseCaseProvider.get(), getTodayUsageUseCaseProvider.get(), evaluateUseCaseProvider.get());
  }

  public static TrackedAppsViewModel_Factory create(
      Provider<GetRulesUseCase> getRulesUseCaseProvider,
      Provider<GetTodayUsageUseCase> getTodayUsageUseCaseProvider,
      Provider<EvaluateTrackedAppsUsageUseCase> evaluateUseCaseProvider) {
    return new TrackedAppsViewModel_Factory(getRulesUseCaseProvider, getTodayUsageUseCaseProvider, evaluateUseCaseProvider);
  }

  public static TrackedAppsViewModel newInstance(GetRulesUseCase getRulesUseCase,
      GetTodayUsageUseCase getTodayUsageUseCase, EvaluateTrackedAppsUsageUseCase evaluateUseCase) {
    return new TrackedAppsViewModel(getRulesUseCase, getTodayUsageUseCase, evaluateUseCase);
  }
}
