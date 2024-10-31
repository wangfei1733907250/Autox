package org.autojs.autojs.ui.main.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.aiselp.autox.ui.material3.MainFloatingActionButton
import com.stardust.autojs.servicecomponents.EngineController
import org.autojs.autojs.ui.widget.fillMaxSize
import org.autojs.autoxjs.R

class TaskManagerFragmentKt : Fragment() {

    private val taskListRecyclerView by lazy {
        TaskListRecyclerView(
            requireContext()
        ).apply { fillMaxSize() }
    }
    private val swipeRefreshLayout by lazy {
        SwipeRefreshLayout(requireContext()).apply {
            fillMaxSize()
            addView(taskListRecyclerView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        init()
        return ComposeView(requireContext()).apply {
            setContent {
                this@TaskManagerFragmentKt.Content()
            }
        }
    }

    @Composable
    private fun Content() {
        val context = LocalContext.current
        Scaffold(
            floatingActionButton = {
                MainFloatingActionButton(
                    onClick = { EngineController.stopAllScript() }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(id = R.string.text_clear),
                    )
                }
            },
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        swipeRefreshLayout
                    },
                )

//                LaunchedEffect(key1 = Unit, block = {
//                    val scope = this
//                    taskListRecyclerView.adapter?.registerAdapterDataObserver(
//                        object : SimpleAdapterDataObserver() {
//                            override fun onSomethingChanged() {
//                                scope.launch {
//                                    val noRunningScript0 =
//                                        (taskListRecyclerView.adapter?.itemCount ?: 0)  <= 0
//                                    delay(150)
//                                    noRunningScript = noRunningScript0
//                                }
//                            }
//                        },
//                    )
//                })
//                if (noRunningScript) {
//                    Text(
//                        text = stringResource(id = R.string.notice_no_running_script),
//                        modifier = Modifier.align(Alignment.Center)
//                    )
//                }
            }
        }
    }

    fun refresh() = taskListRecyclerView.refresh()
    private fun init() {
        swipeRefreshLayout.setOnRefreshListener {
            taskListRecyclerView.refresh()
            taskListRecyclerView.postDelayed({
                swipeRefreshLayout.isRefreshing = false
            }, 800)
        }
    }

    init {
        arguments = Bundle()
    }
}