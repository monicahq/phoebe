package org.monicahq.phoebe.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?,
    @ColumnInfo(name = "nickname") val nickname: String?,
    @ColumnInfo(name = "gender") val gender: String?,
    @ColumnInfo(name = "is_partial") val isPartial: Boolean,
    @ColumnInfo(name = "is_dead") val isDead: Boolean
) : Serializable
