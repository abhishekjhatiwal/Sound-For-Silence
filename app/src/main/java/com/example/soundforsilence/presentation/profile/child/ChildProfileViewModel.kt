package com.example.soundforsilence.presentation.profile.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundforsilence.domain.repository.ChildProfileData
import com.example.soundforsilence.domain.repository.ChildProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChildProfileState(
    val name: String = "",
    val age: String = "",
    val notes: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ChildProfileViewModel @Inject constructor(
    private val repository: ChildProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChildProfileState())
    val state: StateFlow<ChildProfileState> = _state

    fun loadChildProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null, successMessage = null)

            val result = repository.getChildProfile()
            _state.value = result.fold(
                onSuccess = { data: ChildProfileData ->
                    _state.value.copy(
                        name = data.name,
                        age = data.age,
                        notes = data.notes,
                        loading = false
                    )
                },
                onFailure = { e ->
                    _state.value.copy(
                        loading = false,
                        error = e.message ?: "Failed to load child details"
                    )
                }
            )
        }
    }

    fun onNameChanged(value: String) {
        _state.value = _state.value.copy(name = value)
    }

    fun onAgeChanged(value: String) {
        _state.value = _state.value.copy(age = value)
    }

    fun onNotesChanged(value: String) {
        _state.value = _state.value.copy(notes = value)
    }

    fun saveChildProfile() {
        viewModelScope.launch {
            val s = _state.value
            _state.value = s.copy(loading = true, error = null, successMessage = null)

            val result = repository.saveChildProfile(
                name = s.name,
                age = s.age,
                notes = s.notes
            )

            _state.value = result.fold(
                onSuccess = {
                    _state.value.copy(
                        loading = false,
                        successMessage = "Child details saved"
                    )
                },
                onFailure = { e ->
                    _state.value.copy(
                        loading = false,
                        error = e.message ?: "Failed to save child details"
                    )
                }
            )
        }
    }
}
