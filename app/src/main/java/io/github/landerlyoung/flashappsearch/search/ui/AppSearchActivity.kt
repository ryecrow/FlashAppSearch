package io.github.landerlyoung.flashappsearch.search.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProviders
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.landerlyoung.flashappsearch.R
import io.github.landerlyoung.flashappsearch.databinding.ActivityAppSearchBinding
import io.github.landerlyoung.flashappsearch.databinding.AppInfoBinding
import io.github.landerlyoung.flashappsearch.search.model.Input
import io.github.landerlyoung.flashappsearch.search.vm.AppSearchViewModel

class AppSearchActivity : AppCompatActivity() {

    lateinit var viewModel: AppSearchViewModel
    private lateinit var adapter: Adapter

    val searchInput: LiveData<CharSequence>
        get() = _searchInput

    private val _searchInput = MutableLiveData<CharSequence>()

    private val useAnimation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this)[AppSearchViewModel::class.java]

        DataBindingUtil.setContentView<ActivityAppSearchBinding>(
                this,
                R.layout.activity_app_search
        ).let {
            it.ui = this
            it.setLifecycleOwner(this)
            it.vm = viewModel
            initRecyclerView(it.appList)
        }

        viewModel.input.observe(this, Observer {
            _searchInput.value = makeInputs(it)
        })
        viewModel.resultApps.observe(this, Observer { it: List<Pair<String, CharSequence>>? ->
            adapter.setData(it ?: listOf())
        })
    }

    private fun makeInputs(inputs: List<Input>?): CharSequence? {
        if (inputs == null || inputs.isEmpty()) {
            return null
        }
        val ssb = SpannableStringBuilder()
        inputs.forEach {
            val start = ssb.length
            ssb.append('X')
            val drawable = key(it)
            val size = resources.getDimensionPixelSize(R.dimen.search_input_text_size)
            drawable.setBounds(0, 0, size * 1.5f.toInt(), size * 3)
            val span = ImageSpan(drawable)
            ssb.setSpan(span, start, start + 1, SpannableStringBuilder.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        return ssb
    }

    fun gotoSetting() {

    }

    fun key(key:Input) = KeyDrawable(key)

    fun clear(): Boolean {
        viewModel.clear()
        return true
    }

    private fun initRecyclerView(rv: RecyclerView) {
        adapter = Adapter()
        val lm = GridLayoutManager(this, 4)
        rv.layoutManager = lm
        rv.adapter = adapter
    }

    private inner class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
            DataBindingUtil.inflate<AppInfoBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.app_info, parent, false
            ).root
    ) {
        private val binding = DataBindingUtil.getBinding<AppInfoBinding>(itemView)!!
        private val packageName = MutableLiveData<String>()
        private val appInfo = Transformations.switchMap(packageName) {
            viewModel.queryAppInfo(it)
        }!!

        init {
            appInfo.observe(this@AppSearchActivity, Observer {
                binding.appIcon.setImageDrawable(it)
            })
            itemView.setOnClickListener {
                packageName.value?.let {
                    packageManager.getLaunchIntentForPackage(it)?.let {
                        startActivity(it)
                    }
                }
            }
        }

        fun setData(appInfo: Pair<String, CharSequence>) {
            this.packageName.value = appInfo.first
            binding.appLabel.text = appInfo.second
        }
    }

    private inner class Adapter : RecyclerView.Adapter<VH>() {
        private var data: List<Pair<String, CharSequence>> = listOf()

        fun setData(list: List<Pair<String, CharSequence>>) {
            val oldList = data
            val newList = list
            data = newList

            if (!useAnimation) {
                notifyDataSetChanged()
            } else {
                DiffUtil.calculateDiff(object : DiffUtil.Callback() {

                    override fun getOldListSize() = oldList.size

                    override fun getNewListSize() = newList.size

                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                            oldList[oldItemPosition].first == newList[newItemPosition].first

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                            oldList[oldItemPosition].first == newList[newItemPosition].first
                }).dispatchUpdatesTo(this)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(parent)
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.setData(data[position])
        }
    }
}
