package com.example.data.ai

import android.util.Log
import com.example.data.model.DailyReport
import com.example.data.model.Guest
import com.example.data.model.Room
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class AiAssistantService {
    private val TAG = "AiAssistantService"

    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    suspend fun generateDailyExecutiveSummary(
        rooms: List<Room>,
        reports: List<DailyReport>,
        todayCollection: Double
    ): Result<String> {
        return try {
            val totalRooms = rooms.size
            val occupied = rooms.count { it.status == "occupied" }
            val partially = rooms.count { it.status == "partially_occupied" }
            val vacant = rooms.count { it.status == "vacant" }
            val maintenance = rooms.count { it.status == "maintenance" }
            val occupancyPercent = if (totalRooms > 0) ((occupied + partially).toDouble() / totalRooms * 100).toInt() else 0

            val prompt = """
                You are the AI Business Operations Advisor for 'EzyStay Living' hostel management.
                Provide a crisp, actionable 3-4 bullet point executive summary of today's hostel performance:
                - Total Rooms: $totalRooms (Occupied: $occupied, Partial: $partially, Vacant: $vacant, In Maintenance: $maintenance)
                - Occupancy Rate: $occupancyPercent%
                - Today's Rent & Fees Collected: Rs. $todayCollection
                Include 1 key proactive recommendation for the hostel warden/manager (e.g., expediting maintenance on room $maintenance, or advertising vacant beds).
                Keep the tone professional, concise, and operational.
            """.trimIndent()

            val response = withTimeout(40_000L) {
                model.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("AI returned an empty analysis."))
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(Exception("AI analysis request timed out. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error generating executive summary", e)
            Result.failure(e)
        }
    }

    suspend fun draftDueReminderSms(guest: Guest): Result<String> {
        return try {
            val prompt = """
                Generate a polite, professional, and clear SMS & Email payment due reminder for a hostel resident at EzyStay Living:
                - Resident Name: ${guest.fullName}
                - Contract Number: ${guest.contractNo}
                - Room Number: ${guest.roomNumber} (${guest.bedNumber})
                - Monthly Rent: Rs. ${guest.monthlyRent.toInt()}
                - Due Date: ${guest.rentDueDay}th of this month
                - Contact: 9873677108 / ezystayliving@gmail.com
                Keep it under 160 characters if possible, including Contract No and payment instruction.
            """.trimIndent()

            val response = withTimeout(35_000L) {
                model.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("AI could not draft due alert."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error generating reminder SMS", e)
            Result.failure(e)
        }
    }

    suspend fun draftCeoBroadcast(topic: String): Result<String> {
        return try {
            val prompt = """
                Draft an official CEO preinformed broadcast memo to hostel employees at EzyStay Living from Founder/CEO Mr. Kavi Raj Singh.
                Topic: $topic
                Include:
                - Direct instruction for hostel operations (discipline, guest safety, timely DPR submission before 8 PM cutoff, maintaining cleanliness).
                - Clear closing signoff from Mr. Kavi Raj Singh (CEO, EzyStay Living).
                Keep it concise (1-2 short paragraphs).
            """.trimIndent()

            val response = withTimeout(35_000L) {
                model.generateContent(prompt)
            }
            val text = response.text
            if (!text.isNullOrBlank()) {
                Result.success(text.trim())
            } else {
                Result.failure(Exception("Failed to generate CEO broadcast."))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error generating CEO broadcast", e)
            Result.failure(e)
        }
    }
}
