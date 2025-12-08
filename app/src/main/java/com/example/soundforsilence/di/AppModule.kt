package com.example.soundforsilence.di

import com.example.soundforsilence.data.AssessmentRepositoryImpl
import com.example.soundforsilence.data.AuthRepositoryImpl
import com.example.soundforsilence.data.ProfileRepositoryImpl
import com.example.soundforsilence.data.StreakRepositoryImpl
import com.example.soundforsilence.data.VideoProgressRepositoryImpl
import com.example.soundforsilence.data.VideoRepositoryImpl
import com.example.soundforsilence.domain.repository.AssessmentRepository
import com.example.soundforsilence.domain.repository.AuthRepository
import com.example.soundforsilence.domain.repository.ChildProfileRepository
import com.example.soundforsilence.domain.repository.ProfileRepository
import com.example.soundforsilence.domain.repository.StreakRepository
import com.example.soundforsilence.domain.repository.VideoProgressRepository
import com.example.soundforsilence.domain.repository.VideoRepository
import com.example.soundforsilence.domain.usecase.GetAssessmentsUseCase
import com.example.soundforsilence.domain.usecase.GetCategoriesUseCase
import com.example.soundforsilence.domain.usecase.GetVideosByCategoryUseCase
import com.example.soundforsilence.domain.usecase.LoginUseCase
import com.example.soundforsilence.domain.usecase.RegisterUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // -------------------------------------------------
    // 🔹 Firebase singletons
    // -------------------------------------------------
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideRealtimeDatabaseRef(): DatabaseReference =
        FirebaseDatabase.getInstance().reference

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage =
        FirebaseStorage.getInstance()

    // -------------------------------------------------
    // 🔹 Repositories (data layer implementations)
    // -------------------------------------------------

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        db: DatabaseReference
    ): AuthRepository = AuthRepositoryImpl(firebaseAuth, db)

    @Provides
    @Singleton
    fun provideAssessmentRepository(): AssessmentRepository = AssessmentRepositoryImpl()

    @Provides
    @Singleton
    fun provideProfileRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): ProfileRepository = ProfileRepositoryImpl(auth, firestore, storage)

    // If your ChildProfileRepository now uses Firestore instead of Realtime DB:
    // Adjust this to your actual implementation constructor.
    @Provides
    @Singleton
    fun provideChildProfileRepository(
        db: DatabaseReference
    ): ChildProfileRepository = ChildProfileRepository(db)

    // -------------------------------------------------
    // 🔹 Use cases
    // -------------------------------------------------

    @Provides
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase =
        LoginUseCase(authRepository)

    @Provides
    fun provideRegisterUseCase(authRepository: AuthRepository): RegisterUseCase =
        RegisterUseCase(authRepository)

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
    @Singleton
    fun provideVideoRepository(
        firestore: FirebaseFirestore
    ): VideoRepository = VideoRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideVideoProgressRepository(
        firestore: FirebaseFirestore
    ): VideoProgressRepository = VideoProgressRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideStreakRepository(
        firestore: FirebaseFirestore
    ): StreakRepository = StreakRepositoryImpl(firestore)

}

