package io.github.sds100.keymapper

import io.github.sds100.keymapper.Selection.SelectableItem

/**
 * Created by sds100 on 25/11/2018.
 */

class KeymapAdapterModel(override val id: Long,
                         val isEnabled: Boolean,
                         val triggerList: List<Trigger>,
                         val actionDescription: ActionDescription) : SelectableItem() {

    constructor(keyMap: KeyMap, actionDescription: ActionDescription)
            : this(keyMap.id, keyMap.isEnabled, keyMap.triggerList, actionDescription)
}