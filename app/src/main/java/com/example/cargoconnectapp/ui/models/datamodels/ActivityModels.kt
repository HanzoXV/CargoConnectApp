package com.example.cargoconnectapp.ui.models.datamodels

import com.google.gson.annotations.SerializedName

data class ThreadResponse(
    @SerializedName("thread_id") val threadId: Int
)

data class ChatThreadDto(
    @SerializedName("thread_id") val threadId: Int,
    @SerializedName("other_user_id") val otherUserId: Int,
    @SerializedName("other_name") val otherName: String,
    @SerializedName("last_message") val lastMessage: String,
    @SerializedName("last_message_time") val lastMessageTime: String?,
    @SerializedName("unread_count") val unreadCount: Int
)

data class ChatMessageDto(
    @SerializedName("message_id") val messageId: Int,
    @SerializedName("thread_id") val threadId: Int,
    @SerializedName("sender_user_id") val senderUserId: Int,
    @SerializedName("is_mine") val isMine: Boolean,
    @SerializedName("message_text") val messageText: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("sent_at") val sentAt: String?
)

data class SendMessageResponse(
    @SerializedName("message_id") val messageId: Int,
    @SerializedName("thread_id") val threadId: Int,
    @SerializedName("sent_at") val sentAt: String?
)