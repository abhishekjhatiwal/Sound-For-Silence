package com.example.soundforsilence.di

import com.example.soundforsilence.data.AssessmentRepositoryImpl
import com.example.soundforsilence.data.AuthRepositoryImpl
import com.example.soundforsilence.data.VideoRepositoryImpl
import com.example.soundforsilence.domain.repository.AssessmentRepository
import com.example.soundforsilence.domain.repository.AuthRepository
import com.example.soundforsilence.domain.repository.ProfileRepository
import com.example.soundforsilence.domain.repository.VideoRepository
import com.example.soundforsilence.domain.usecase.GetAssessmentsUseCase
import com.example.soundforsilence.domain.usecase.GetCategoriesUseCase
import com.example.soundforsilence.domain.usecase.GetVideosByCategoryUseCase
import com.example.soundforsilence.domain.usecase.LoginUseCase
import com.example.soundforsilence.domain.usecase.RegisterUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Repositories
    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = AuthRepositoryImpl()

    @Provides
    @Singleton
    fun provideVideoRepository(): VideoRepository = VideoRepositoryImpl()

    @Provides
    @Singleton
    fun provideAssessmentRepository(): AssessmentRepository = AssessmentRepositoryImpl()

    // Use Cases
    @Provides
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase =
        LoginUseCase(authRepository)

    @Provides
    fun provideGetCategoriesUseCase(videoRepository: VideoRepository): GetCategoriesUseCase =
        GetCategoriesUseCase(videoRepository)

    @Provides
    fun provideGetVideosByCategoryUseCase(
        videoRepository: VideoRepository
    ): GetVideosByCategoryUseCase = GetVideosByCategoryUseCase(videoRepository)

    @Provides
    fun provideGetAssessmentsUseCase(
        assessmentRepository: AssessmentRepository
    ): GetAssessmentsUseCase = GetAssessmentsUseCase(assessmentRepository)

    @Provides
    fun provideRegisterUseCase(authRepository: AuthRepository): RegisterUseCase =
        RegisterUseCase(authRepository)

    // e.g., AppModule.kt
    @Provides
    @Singleton
    fun provideProfileRepository(): ProfileRepository = ProfileRepository()

}
