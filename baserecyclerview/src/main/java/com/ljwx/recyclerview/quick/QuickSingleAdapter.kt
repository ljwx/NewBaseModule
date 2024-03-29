package com.ljwx.recyclerview.quick

import android.util.Log
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ljwx.recyclerview.BR
import com.ljwx.recyclerview.diff.ItemDiffCallback
import com.ljwx.recyclerview.holder.ItemHolder
import com.ljwx.recyclerview.itemtype.ItemBindClick
import com.ljwx.recyclerview.itemtype.ItemTypeBinding
import com.ljwx.recyclerview.itemtype.ItemTypeLayout

open class QuickSingleAdapter<Item : Any>@JvmOverloads constructor(
    itemClass: Class<Item>,
    @LayoutRes
    private val layoutResId: Int,
    brId: Int? = BR.item,
    diff: DiffUtil.ItemCallback<Item> = ItemDiffCallback(),
    itemClick: ((ItemHolder, Item) -> Unit)? = null,
) : ListAdapter<Item, ItemHolder>(AsyncDifferConfig.Builder(diff).build()),
    ItemBindClick<Item> {

    private val TAG = this.javaClass.simpleName + "[recyclerview]"

    private val mItemType: ItemTypeLayout<Item> =
        ItemTypeBinding(itemClass, layoutResId, brId = brId)

    init {
        itemClick?.let { setOnItemClick(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return mItemType.create(parent)
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder:$position")
        mItemType.bind(holder, getItem(position))
    }

    fun addList(list: List<Item>) {
        submitList(listOf<Item>() + currentList + list)
    }

    override fun setOnItemBind(binder: (ItemHolder, Item) -> Unit) {
        mItemType.setOnItemBind(binder)
    }

    override fun setOnItemClick(itemClick: ((ItemHolder, Item) -> Unit)) {
        mItemType.setOnItemClick(itemClick)
    }

    override fun setOnItemChildClick(vararg ids: Int, itemClick: ((ItemHolder, Item, Int) -> Unit)) {
        mItemType.setOnItemChildClick(*ids, itemClick = itemClick)
    }

}