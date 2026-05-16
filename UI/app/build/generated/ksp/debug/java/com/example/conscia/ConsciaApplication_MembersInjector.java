package com.example.conscia;

import androidx.hilt.work.HiltWorkerFactory;
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
public final class ConsciaApplication_MembersInjector implements MembersInjector<ConsciaApplication> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public ConsciaApplication_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<ConsciaApplication> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new ConsciaApplication_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(ConsciaApplication instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.example.conscia.ConsciaApplication.workerFactory")
  public static void injectWorkerFactory(ConsciaApplication instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
