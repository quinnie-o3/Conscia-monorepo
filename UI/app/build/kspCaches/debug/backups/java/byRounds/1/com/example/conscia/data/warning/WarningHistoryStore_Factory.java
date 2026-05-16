package com.example.conscia.data.warning;

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
public final class WarningHistoryStore_Factory implements Factory<WarningHistoryStore> {
  private final Provider<Context> contextProvider;

  public WarningHistoryStore_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public WarningHistoryStore get() {
    return newInstance(contextProvider.get());
  }

  public static WarningHistoryStore_Factory create(Provider<Context> contextProvider) {
    return new WarningHistoryStore_Factory(contextProvider);
  }

  public static WarningHistoryStore newInstance(Context context) {
    return new WarningHistoryStore(context);
  }
}
