package es.aviferdev.n3to.ui.fixedincome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.aviferdev.n3to.domain.model.FixedIncomeEvent
import es.aviferdev.n3to.domain.model.FixedIncomeEventType
import es.aviferdev.n3to.domain.model.FixedIncomePosition
import es.aviferdev.n3to.domain.model.FixedIncomeRow
import es.aviferdev.n3to.domain.portfolio.FixedIncomeCalculator
import es.aviferdev.n3to.domain.portfolio.MaturitySimulation
import es.aviferdev.n3to.domain.portfolio.ScheduledCoupon
import es.aviferdev.n3to.domain.usecase.fixedincome.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FixedIncomeDetailUiState(
    val row: FixedIncomeRow? = null,
    val events: List<FixedIncomeEvent> = emptyList(),
    val couponSchedule: List<ScheduledCoupon> = emptyList(),
    val maturitySimulation: MaturitySimulation? = null,
    val showRegisterCouponSheet: Boolean = false,
    val showCloseSheet: Boolean = false,
    val preselectedCloseType: es.aviferdev.n3to.domain.model.FixedIncomeCloseType? = null,
    val showEditSheet: Boolean = false,
    val showDeleteEventDialog: Boolean = false,
    val selectedEventForDelete: FixedIncomeEvent? = null,
    val isLoading: Boolean = false,
    val error: FixedIncomeDetailError? = null
)

sealed class FixedIncomeDetailError {
    data object PositionNotFound : FixedIncomeDetailError()
    data class Unknown(val message: String?) : FixedIncomeDetailError()
}

@OptIn(ExperimentalCoroutinesApi::class)
class FixedIncomeDetailViewModel(
    private val positionId: String,
    private val getPositionDetail: GetFixedIncomePositionDetailUseCase,
    private val getCouponSchedule: GetCouponScheduleUseCase,
    private val registerCoupon: RegisterCouponUseCase,
    private val closeFixedIncome: CloseFixedIncomeUseCase,
    private val deleteFixedIncomeEvent: DeleteFixedIncomeEventUseCase,
    private val updatePosition: UpdateFixedIncomePositionUseCase,
    private val archivePosition: ArchiveFixedIncomePositionUseCase
) : ViewModel() {

    private val _showRegisterCouponSheet = MutableStateFlow(false)
    private val _showCloseSheet = MutableStateFlow(false)
    private val _preselectedCloseType =
        MutableStateFlow<es.aviferdev.n3to.domain.model.FixedIncomeCloseType?>(null)
    private val _showEditSheet = MutableStateFlow(false)
    private val _showDeleteEventDialog = MutableStateFlow(false)
    private val _selectedEventForDelete = MutableStateFlow<FixedIncomeEvent?>(null)
    private val _error = MutableStateFlow<FixedIncomeDetailError?>(null)

    val uiState: StateFlow<FixedIncomeDetailUiState> = combine(
        getPositionDetail.getPosition(positionId),
        getPositionDetail.getEvents(positionId),
        _showRegisterCouponSheet,
        _showCloseSheet,
        _preselectedCloseType,
        _showEditSheet,
        _showDeleteEventDialog,
        _selectedEventForDelete,
        _error
    ) { values ->
        val row = values[0] as FixedIncomeRow?

        @Suppress("UNCHECKED_CAST")
        val events = values[1] as List<FixedIncomeEvent>
        val showCoupon = values[2] as Boolean
        val showClose = values[3] as Boolean
        val preselectedClose = values[4] as es.aviferdev.n3to.domain.model.FixedIncomeCloseType?
        val showEdit = values[5] as Boolean
        val showDelete = values[6] as Boolean
        val eventForDelete = values[7] as FixedIncomeEvent?
        val error = values[8] as FixedIncomeDetailError?

        val schedule = row?.let {
            getCouponSchedule(it.position)
        } ?: emptyList()

        val simulation = row?.let {
            FixedIncomeCalculator.simulateMaturity(it.position, events)
        }

        FixedIncomeDetailUiState(
            row = row,
            events = events,
            couponSchedule = schedule,
            maturitySimulation = simulation,
            showRegisterCouponSheet = showCoupon,
            showCloseSheet = showClose,
            preselectedCloseType = preselectedClose,
            showEditSheet = showEdit,
            showDeleteEventDialog = showDelete,
            selectedEventForDelete = eventForDelete,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FixedIncomeDetailUiState(isLoading = true)
    )

    fun showRegisterCouponSheet() {
        _showRegisterCouponSheet.value = true
    }

    fun hideRegisterCouponSheet() {
        _showRegisterCouponSheet.value = false
    }

    fun showCloseSheet() {
        _showCloseSheet.value = true
    }

    fun hideCloseSheet() {
        _showCloseSheet.value = false
        _preselectedCloseType.value = null
    }

    fun showCloseSheetWithType(closeType: es.aviferdev.n3to.domain.model.FixedIncomeCloseType) {
        _preselectedCloseType.value = closeType
        _showCloseSheet.value = true
    }

    fun showDeleteEventDialog(event: FixedIncomeEvent) {
        _selectedEventForDelete.value = event
        _showDeleteEventDialog.value = true
    }

    fun hideDeleteEventDialog() {
        _showDeleteEventDialog.value = false
        _selectedEventForDelete.value = null
    }

    fun registerCouponEvent(event: FixedIncomeEvent) {
        viewModelScope.launch {
            val position = uiState.value.row?.position
            if (position == null) {
                _error.value = FixedIncomeDetailError.PositionNotFound
                return@launch
            }
            registerCoupon(event, position.accountId, position.id)
                .onSuccess {
                    _showRegisterCouponSheet.value = false
                }
                .onFailure {
                    _error.value = FixedIncomeDetailError.Unknown(it.message)
                }
        }
    }

    fun closePosition(
        closeType: es.aviferdev.n3to.domain.model.FixedIncomeCloseType,
        closeDate: Long,
        settlementEvent: FixedIncomeEvent
    ) {
        viewModelScope.launch {
            val position = uiState.value.row?.position
            if (position == null) {
                _error.value = FixedIncomeDetailError.PositionNotFound
                return@launch
            }
            closeFixedIncome(positionId, closeType, closeDate, settlementEvent, position.accountId, position.id)
                .onSuccess { _showCloseSheet.value = false }
                .onFailure { _error.value = FixedIncomeDetailError.Unknown(it.message) }
        }
    }

    fun deleteEvent(event: FixedIncomeEvent) {
        viewModelScope.launch {
            val linkedTxId = "fi_${event.id}"
            deleteFixedIncomeEvent(event.id, linkedTxId)
                .onSuccess { hideDeleteEventDialog() }
                .onFailure { _error.value = FixedIncomeDetailError.Unknown(it.message) }
        }
    }

    fun archivePosition() {
        viewModelScope.launch {
            archivePosition(positionId)
                .onFailure { _error.value = FixedIncomeDetailError.Unknown(it.message) }
        }
    }

    fun showEditSheet() {
        _showEditSheet.value = true
    }

    fun hideEditSheet() {
        _showEditSheet.value = false
    }

    fun savePosition(updatedPosition: FixedIncomePosition) {
        viewModelScope.launch {
            updatePosition(updatedPosition)
                .onSuccess { hideEditSheet() }
                .onFailure { _error.value = FixedIncomeDetailError.Unknown(it.message) }
        }
    }

    fun updateRegionAndSector(region: String?, sector: String?) {
        viewModelScope.launch {
            val position = uiState.value.row?.position ?: return@launch
            val updatedPosition = position.copy(region = region, sector = sector)
            updatePosition(updatedPosition)
                .onFailure { _error.value = FixedIncomeDetailError.Unknown(it.message) }
        }
    }

    fun clearError() {
        _error.value = null
    }
}