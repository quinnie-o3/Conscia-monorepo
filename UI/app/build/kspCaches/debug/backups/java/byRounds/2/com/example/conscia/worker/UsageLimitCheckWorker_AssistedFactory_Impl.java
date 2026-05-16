package com.example.conscia.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class UsageLimitCheckWorker_AssistedFactory_Impl implements UsageLimitCheckWorker_AssistedFactory {
  private final UsageLimitCheckWorker_Factory delegateFactory;

  UsageLimitCheckWorker_AssistedFactory_Impl(UsageLimitCheckWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public UsageLimitCheckWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<UsageLimitCheckWorker_AssistedFactory> create(
      UsageLimitCheckWorker_Factory delegateFactory) {
    return InstanceFactory.create(new UsageLimitCheckWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<UsageLimitCheckWorker_AssistedFactory> createFactoryProvider(
      UsageLimitCheckWorker_Factory delegateFactory) {
    return InstanceFactory.create(new UsageLimitCheckWorker_AssistedFactory_Impl(delegateFactory));
  }
}
