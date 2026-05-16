package com.example.conscia.ui.rules;

import com.example.conscia.domain.usecase.GetRulesUseCase;
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
public final class RulesViewModel_Factory implements Factory<RulesViewModel> {
  private final Provider<GetRulesUseCase> getRulesUseCaseProvider;

  public RulesViewModel_Factory(Provider<GetRulesUseCase> getRulesUseCaseProvider) {
    this.getRulesUseCaseProvider = getRulesUseCaseProvider;
  }

  @Override
  public RulesViewModel get() {
    return newInstance(getRulesUseCaseProvider.get());
  }

  public static RulesViewModel_Factory create(Provider<GetRulesUseCase> getRulesUseCaseProvider) {
    return new RulesViewModel_Factory(getRulesUseCaseProvider);
  }

  public static RulesViewModel newInstance(GetRulesUseCase getRulesUseCase) {
    return new RulesViewModel(getRulesUseCase);
  }
}
