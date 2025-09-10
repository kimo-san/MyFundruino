package com.qymoy.myfundruino

import android.content.Context
import com.qymoy.myfundruino.data.AndroidBluetoothController
import com.qymoy.myfundruino.domain.BluetoothController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltModule {

    @Singleton
    @Provides
    fun provideBleController(@ApplicationContext context: Context): BluetoothController =
        AndroidBluetoothController(context)
}