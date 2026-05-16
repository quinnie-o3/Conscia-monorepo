package com.example.conscia.ui.rules;

import com.example.conscia.data.remote.api.ConsciaApiService;
import com.example.conscia.data.rule.RuleRepository;
import com.example.conscia.domain.usecase.DeleteRuleUseCase;
import com.example.conscia.domain.usecase.GetRuleByIdUseCase;
import com.example.conscia.domain.usecase.UpsertRuleUseCase;
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
public final class CreateEditRuleViewModel_Factory implements Factory<CreateEditRuleViewModel> {
  private final Provider<GetRuleByIdUseCase> getRuleByIdUseCaseProvider;

  private final Provider<UpsertRuleUseCase> upsertRuleUseCaseProvider;

  private final Provider<DeleteRuleUseCase> deleteRuleUseCaseProvider;

  private final Provider<RuleRepository> repositoryProvider;

  private final Provider<ConsciaApiService> apiServiceProvider;

  public CreateEditRuleViewModel_Factory(Provider<GetRuleByIdUseCase> getRuleByIdUseCaseProvider,
      Provider<UpsertRuleUseCase> upsertRuleUseCaseProvider,
      Provider<DeleteRuleUseCase> deleteRuleUseCaseProvider,
      Provider<RuleRepository> repositoryProvider, Provider<ConsciaApiService> apiServiceProvider) {
    this.getRuleByIdUseCaseProvider = getRuleByIdUseCaseProvider;
    this.upsertRuleUseCaseProvider = upsertRuleUseCaseProvider;
    this.deleteRuleUseCaseProvider = deleteRuleUseCaseProvider;
    this.repositoryProvider = repositoryProvider;
    this.apiServiceProvider = apiServiceProvider;
  }

  @Override
  public CreateEditRuleViewModel get() {
    return newInstance(getRuleByIdUseCaseProvider.get(), upsertRuleUseCaseProvider.get(), deleteRuleUseCaseProvider.get(), repositoryProvider.get(), apiServiceProvider.get());
  }

  public static CreateEditRuleViewModel_Factory create(
      Provider<GetRuleByIdUseCase> getRuleByIdUseCaseProvider,
      Provider<UpsertRuleUseCase> upsertRuleUseCaseProvider,
      Provider<DeleteRuleUseCase> deleteRuleUseCaseProvider,
      Provider<RuleRepository> repositoryProvider, Provider<ConsciaApiService> apiServiceProvider) {
    return new CreateEditRuleViewModel_Factory(getRuleByIdUseCaseProvider, upsertRuleUseCaseProvider, deleteRuleUseCaseProvider, repositoryProvider, apiServiceProvider);
  }

  public static CreateEditRuleViewModel newInstance(GetRuleByIdUseCase getRuleByIdUseCase,
      UpsertRuleUseCase upsertRuleUseCase, DeleteRuleUseCase deleteRuleUseCase,
      RuleRepository repository, ConsciaApiService apiService) {
    return new CreateEditRuleViewModel(getRuleByIdUseCase, upsertRuleUseCase, deleteRuleUseCase, repository, apiService);
  }
}
