package com.example.conscia.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AppRepository_Factory implements Factory<AppRepository> {
  private final Provider<Context> contextProvider;

  public AppRepository_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AppRepository get() {
    return newInstance(contextProvider.get());
  }

  public static AppRepository_Factory create(Provider<Context> contextProvider) {
    return new AppRepository_Factory(contextProvider);
  }

  public static AppRepository newInstance(Context context) {
    return new AppRepository(context);
  }
}
