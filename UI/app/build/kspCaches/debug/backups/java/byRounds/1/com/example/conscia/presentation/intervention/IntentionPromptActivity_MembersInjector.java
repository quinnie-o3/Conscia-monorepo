package com.example.conscia.presentation.intervention;

import com.example.conscia.data.rule.RuleRepository;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class IntentionPromptActivity_MembersInjector implements MembersInjector<IntentionPromptActivity> {
  private final Provider<RuleRepository> ruleRepositoryProvider;

  public IntentionPromptActivity_MembersInjector(Provider<RuleRepository> ruleRepositoryProvider) {
    this.ruleRepositoryProvider = ruleRepositoryProvider;
  }

  public static MembersInjector<IntentionPromptActivity> create(
      Provider<RuleRepository> ruleRepositoryProvider) {
    return new IntentionPromptActivity_MembersInjector(ruleRepositoryProvider);
  }

  @Override
  public void injectMembers(IntentionPromptActivity instance) {
    injectRuleRepository(instance, ruleRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.example.conscia.presentation.intervention.IntentionPromptActivity.ruleRepository")
  public static void injectRuleRepository(IntentionPromptActivity instance,
      RuleRepository ruleRepository) {
    instance.ruleRepository = ruleRepository;
  }
}
