package com.example.speechtotextnotes.data

import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

open class NoteList(
    @PrimaryKey
    var id: Int? = null,
    var notesList: RealmList<String>? = null
) : RealmObject() {

    companion object {
        @Ignore
        var cachedNextId: Int? = null
            get() {
                field = null
                val nextId =
                    if (field != null)
                    field?.plus(1)
                else Realm.getDefaultInstance()?.where(NoteList::class.java)?.max("id")?.toInt()
                    ?.plus(1) ?: 1
                cachedNextId = nextId
                return nextId
            }
    }
}