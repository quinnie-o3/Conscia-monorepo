package com.example.conscia.domain.usecase;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class EvaluateTrackedAppsUsageUseCase_Factory implements Factory<EvaluateTrackedAppsUsageUseCase> {
  @Override
  public EvaluateTrackedAppsUsageUseCase get() {
    return newInstance();
  }

  public static EvaluateTrackedAppsUsageUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static EvaluateTrackedAppsUsageUseCase newInstance() {
    return new EvaluateTrackedAppsUsageUseCase();
  }

  private static final class InstanceHolder {
    private static final EvaluateTrackedAppsUsageUseCase_Factory INSTANCE = new EvaluateTrackedAppsUsageUseCase_Factory();
  }
}
