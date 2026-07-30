package app.fridgedday.ui.addedit

import app.fridgedday.data.db.entity.ItemEntity
import app.fridgedday.data.db.entity.StorageLocation
import app.fridgedday.data.repo.AddEditItemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class AddEditViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun newItemRequiresExplicitDateBeforeSave() {
        val repository = FakeAddEditItemRepository()
        val viewModel = AddEditViewModel(repository, itemId = null)

        viewModel.updateName("우유")
        viewModel.saveItem()

        assertNull(viewModel.uiState.value.expiryDate)
        assertFalse(viewModel.uiState.value.isExpiryDateConfirmed)
        assertEquals("유통기한을 선택하고 확인해주세요", viewModel.uiState.value.errorMessage)
        assertTrue(repository.insertedItems.isEmpty())
    }

    @Test
    fun ocrProposalIsNotPersistedUntilUserConfirms() = runTest {
        val repository = FakeAddEditItemRepository()
        val viewModel = AddEditViewModel(repository, itemId = null)
        val ocrDate = LocalDate.of(2026, 8, 20)

        viewModel.updateName("요거트")
        viewModel.proposeOcrDate(ocrDate)
        viewModel.saveItem()

        assertEquals(ocrDate, viewModel.uiState.value.pendingOcrDate)
        assertNull(viewModel.uiState.value.expiryDate)
        assertTrue(repository.insertedItems.isEmpty())

        viewModel.confirmPendingOcrDate()
        viewModel.saveItem()
        advanceUntilIdle()

        assertEquals(ocrDate, repository.insertedItems.single().expiryDate)
        assertTrue(viewModel.uiState.value.isSaved)
    }

    @Test
    fun cancellingOcrProposalKeepsPreviouslyConfirmedDate() {
        val repository = FakeAddEditItemRepository()
        val viewModel = AddEditViewModel(repository, itemId = null)
        val manualDate = LocalDate.of(2026, 9, 1)

        viewModel.confirmManualExpiryDate(manualDate)
        viewModel.proposeOcrDate(LocalDate.of(2026, 8, 1))
        viewModel.cancelPendingOcrDate()

        assertEquals(manualDate, viewModel.uiState.value.expiryDate)
        assertTrue(viewModel.uiState.value.isExpiryDateConfirmed)
        assertNull(viewModel.uiState.value.pendingOcrDate)
    }

    @Test
    fun correctingOcrProposalPersistsCorrectedDate() = runTest {
        val repository = FakeAddEditItemRepository()
        val viewModel = AddEditViewModel(repository, itemId = null)
        val correctedDate = LocalDate.of(2026, 10, 15)

        viewModel.updateName("두부")
        viewModel.proposeOcrDate(LocalDate.of(2026, 10, 5))
        viewModel.confirmManualExpiryDate(correctedDate)
        viewModel.saveItem()
        advanceUntilIdle()

        assertEquals(correctedDate, repository.insertedItems.single().expiryDate)
        assertNull(viewModel.uiState.value.pendingOcrDate)
    }

    @Test
    fun editingItemPreservesExistingMetadata() = runTest {
        val createdDate = LocalDate.of(2025, 11, 25)
        val consumedDate = LocalDate.of(2026, 7, 1)
        val existingItem = ItemEntity(
            id = 7,
            name = "기존 항목",
            location = StorageLocation.FREEZER,
            expiryDate = LocalDate.of(2026, 8, 1),
            isArchived = true,
            consumedDate = consumedDate,
            createdDate = createdDate
        )
        val repository = FakeAddEditItemRepository(existingItem)
        val viewModel = AddEditViewModel(repository, itemId = existingItem.id)
        advanceUntilIdle()

        viewModel.updateName("수정 항목")
        viewModel.confirmManualExpiryDate(LocalDate.of(2026, 8, 15))
        viewModel.saveItem()
        advanceUntilIdle()

        val updated = repository.updatedItems.single()
        assertEquals(existingItem.id, updated.id)
        assertEquals(createdDate, updated.createdDate)
        assertEquals(consumedDate, updated.consumedDate)
        assertTrue(updated.isArchived)
        assertEquals("수정 항목", updated.name)
    }
}

private class FakeAddEditItemRepository(
    private val existingItem: ItemEntity? = null
) : AddEditItemRepository {

    val insertedItems = mutableListOf<ItemEntity>()
    val updatedItems = mutableListOf<ItemEntity>()

    override suspend fun getById(id: Long): ItemEntity? =
        existingItem?.takeIf { it.id == id }

    override suspend fun insert(item: ItemEntity): Long {
        insertedItems += item
        return insertedItems.size.toLong()
    }

    override suspend fun update(item: ItemEntity) {
        updatedItems += item
    }
}
