package com.example.soundforsilence.presentation.profile.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.model.ChildProfile
import com.example.soundforsilence.domain.model.ChildProfileState
import com.example.soundforsilence.domain.repository.AuthRepository
import com.example.soundforsilence.domain.repository.ChildProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChildProfileViewModel @Inject constructor(
    private val repository: ChildProfileRepository,
    private val authRepository: AuthRepository    // ✅ inject auth
) : ViewModel() {

    private val _state = MutableStateFlow(ChildProfileState())
    val state: StateFlow<ChildProfileState> = _state.asStateFlow()

    fun loadChildProfile() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, successMessage = null) }

            // ✅ Check logged-in user
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _state.update {
                    it.copy(
                        loading = false,
                        error = "User not logged in"
                    )
                }
                return@launch
            }

            // ✅ Pass userId to repository
            val result = repository.getChildProfile(userId)

            result
                .onSuccess { profile: ChildProfile ->
                    _state.update {
                        it.copy(
                            name = profile.name,
                            age = profile.age,
                            notes = profile.notes,
                            loading = false
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = e.message ?: "Failed to load child profile"
                        )
                    }
                }
        }
    }

    fun onNameChanged(newName: String) {
        _state.update { it.copy(name = newName) }
    }

    fun onAgeChanged(newAge: String) {
        _state.update { it.copy(age = newAge) }
    }

    fun onNotesChanged(newNotes: String) {
        _state.update { it.copy(notes = newNotes) }
    }

    fun saveChildProfile() {
        viewModelScope.launch {
            val current = state.value

            _state.update { it.copy(loading = true, error = null, successMessage = null) }

            // ✅ Check logged-in user
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _state.update {
                    it.copy(
                        loading = false,
                        error = "User not logged in"
                    )
                }
                return@launch
            }

            val profile = ChildProfile(
                name = current.name,
                age = current.age,
                notes = current.notes
            )

            // ✅ Pass userId to repository
            val result = repository.saveChildProfile(userId, profile)

            result
                .onSuccess {
                    _state.update {
                        it.copy(
                            loading = false,
                            successMessage = "Child details saved successfully"
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = e.message ?: "Failed to save child profile"
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}







































/*
@HiltViewModel
class ChildProfileViewModel @Inject constructor(
    private val repository: ChildProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChildProfileState())
    val state: StateFlow<ChildProfileState> = _state.asStateFlow()

    fun loadChildProfile() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, successMessage = null) }

            val result = repository.getChildProfile()

            result
                .onSuccess { profile: ChildProfile ->
                    _state.update {
                        it.copy(
                            name = profile.name,
                            age = profile.age,
                            notes = profile.notes,
                            loading = false
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = e.message ?: "Failed to load child profile"
                        )
                    }
                }
        }
    }

    fun onNameChanged(newName: String) {
        _state.update { it.copy(name = newName) }
    }

    fun onAgeChanged(newAge: String) {
        _state.update { it.copy(age = newAge) }
    }

    fun onNotesChanged(newNotes: String) {
        _state.update { it.copy(notes = newNotes) }
    }

    fun saveChildProfile() {
        viewModelScope.launch {
            val current = state.value

            _state.update { it.copy(loading = true, error = null, successMessage = null) }

            val profile = ChildProfile(
                name = current.name,
                age = current.age,
                notes = current.notes
            )

            val result = repository.saveChildProfile(profile)

            result
                .onSuccess {
                    _state.update {
                        it.copy(
                            loading = false,
                            successMessage = "Child details saved successfully"
                        )
                    }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = e.message ?: "Failed to save child profile"
                        )
                    }
                }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(error = null, successMessage = null) }
    }
}


 */
