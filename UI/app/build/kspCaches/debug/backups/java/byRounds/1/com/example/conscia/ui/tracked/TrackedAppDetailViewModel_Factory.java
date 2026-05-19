package com.example.conscia.ui.tracked;

import com.example.conscia.data.rule.RuleRepository;
import com.example.conscia.domain.usecase.DeleteRuleUseCase;
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
public final class TrackedAppDetailViewModel_Factory implements Factory<TrackedAppDetailViewModel> {
  private final Provider<RuleRepository> ruleRepositoryProvider;

  private final Provider<DeleteRuleUseCase> deleteRuleUseCaseProvider;

  private final Provider<GetTodayUsageUseCase> getTodayUsageUseCaseProvider;

  public TrackedAppDetailViewModel_Factory(Provider<RuleRepository> ruleRepositoryProvider,
      Provider<DeleteRuleUseCase> deleteRuleUseCaseProvider,
      Provider<GetTodayUsageUseCase> getTodayUsageUseCaseProvider) {
    this.ruleRepositoryProvider = ruleRepositoryProvider;
    this.deleteRuleUseCaseProvider = deleteRuleUseCaseProvider;
    this.getTodayUsageUseCaseProvider = getTodayUsageUseCaseProvider;
  }

  @Override
  public TrackedAppDetailViewModel get() {
    return newInstance(ruleRepositoryProvider.get(), deleteRuleUseCaseProvider.get(), getTodayUsageUseCaseProvider.get());
  }

  public static TrackedAppDetailViewModel_Factory create(
      Provider<RuleRepository> ruleRepositoryProvider,
      Provider<DeleteRuleUseCase> deleteRuleUseCaseProvider,
      Provider<GetTodayUsageUseCase> getTodayUsageUseCaseProvider) {
    return new TrackedAppDetailViewModel_Factory(ruleRepositoryProvider, deleteRuleUseCaseProvider, getTodayUsageUseCaseProvider);
  }

  public static TrackedAppDetailViewModel newInstance(RuleRepository ruleRepository,
      DeleteRuleUseCase deleteRuleUseCase, GetTodayUsageUseCase getTodayUsageUseCase) {
    return new TrackedAppDetailViewModel(ruleRepository, deleteRuleUseCase, getTodayUsageUseCase);
  }
}
