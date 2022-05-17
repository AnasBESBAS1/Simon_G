package fr.gime.projct.simon_g.backEnd

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class SimonBackendModule {
    @Binds
    abstract  fun bindSimonBackendImpl(simonBackend : BackEndImpl): SimonBackend
}