package com.englishfriendai.app.domain.usecase

import com.englishfriendai.app.domain.repository.ConversationRepository
import javax.inject.Inject

/**
 * Stub for a future "export conversation transcript as PDF/TXT" feature.
 * TODO: implement once the export file format and share/save UX are decided; for now this
 * always resolves to a NotImplementedError so callers can surface a friendly "coming soon" state.
 */
class ExportTranscriptUseCase @Inject constructor(
    private val conversationRepository: ConversationRepository
) {
    suspend operator fun invoke(conversationId: String): Result<Nothing> =
        conversationRepository.exportTranscript(conversationId)
}
