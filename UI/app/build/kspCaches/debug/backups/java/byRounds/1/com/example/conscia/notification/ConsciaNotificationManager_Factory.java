package com.example.conscia.notification;

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
public final class ConsciaNotificationManager_Factory implements Factory<ConsciaNotificationManager> {
  private final Provider<Context> contextProvider;

  public ConsciaNotificationManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public ConsciaNotificationManager get() {
    return newInstance(contextProvider.get());
  }

  public static ConsciaNotificationManager_Factory create(Provider<Context> contextProvider) {
    return new ConsciaNotificationManager_Factory(contextProvider);
  }

  public static ConsciaNotificationManager newInstance(Context context) {
    return new ConsciaNotificationManager(context);
  }
}
