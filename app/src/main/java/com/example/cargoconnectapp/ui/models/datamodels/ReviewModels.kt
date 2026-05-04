package com.example.cargoconnectapp.ui.models.datamodels

import com.google.gson.annotations.SerializedName

data class ReviewDto(
    @SerializedName("review_id") val reviewId: Int,
    @SerializedName("trip_id") val tripId: Int,
    @SerializedName("reviewer_user_id") val reviewerUserId: Int,
    @SerializedName("target_user_id") val targetUserId: Int,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comments") val comments: String?,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("reviewer_name") val reviewerName: String?
)

data class RatingResponse(
    @SerializedName("averageRating") val averageRating: Double,
    @SerializedName("totalReviews") val totalReviews: Int
)

data class ReviewSentResponse(
    @SerializedName("message") val message: String,
    @SerializedName("review_id") val reviewId: Int
)

data class ReviewInfoDto(
    @SerializedName("booking_id") val bookingId: Int,
    @SerializedName("trip_id") val tripId: Int,
    @SerializedName("target_user_id") val targetUserId: Int,
    @SerializedName("target_name") val targetName: String,
    @SerializedName("already_reviewed") val alreadyReviewed: Boolean
)

data class CanReviewDto(
    @SerializedName("can_review") val canReview: Boolean,
    @SerializedName("target_user_id") val targetUserId: Int,
    @SerializedName("target_name") val targetName: String,
    @SerializedName("trip_id") val tripId: Int
)