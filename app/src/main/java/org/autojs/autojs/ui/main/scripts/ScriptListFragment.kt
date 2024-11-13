package org.autojs.autojs.ui.main.scripts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.stardust.app.GlobalAppContext.get
import com.stardust.util.IntentUtil
import org.autojs.autojs.Pref
import org.autojs.autojs.external.fileprovider.AppFileProvider
import org.autojs.autojs.model.explorer.ExplorerDirPage
import org.autojs.autojs.model.explorer.Explorers
import org.autojs.autojs.model.script.Scripts.edit
import org.autojs.autojs.ui.build.ProjectConfigActivity
import org.autojs.autojs.ui.common.ScriptOperations
import org.autojs.autojs.ui.explorer.ExplorerViewKt
import org.autojs.autojs.ui.viewmodel.ExplorerItemList.SortConfig
import org.autojs.autojs.ui.widget.fillMaxSize
import org.autojs.autoxjs.R

/**
 * Created by wilinz on 2022/7/15.
 */
class ScriptListFragment : Fragment() {

    val explorerView by lazy { ExplorerViewKt(this.requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        explorerView.setUpViews()
        return ComposeView(requireContext()).apply {
            setContent {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        FloatingButton()
                    },
                ) {
                    AndroidView(
                        modifier = Modifier.padding(it),
                        factory = { explorerView }
                    )
                }
            }
        }
    }

    @Composable
    private fun FloatingButton() {
        Column(
            modifier = Modifier.padding(end = 16.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.End
        ) {
            var expand by remember { mutableStateOf(false) }
            val rotate by animateFloatAsState(
                if (!expand) 0f else 360f,
                label = "FloatingActionButton"
            )
            AnimatedVisibility(expand) {
                Actions { expand = false }
                Spacer(modifier = Modifier.height(16.dp))
            }

            FloatingActionButton(
                onClick = { expand = !expand },
                modifier = Modifier.rotate(rotate)
            ) {
                if (expand) {
                    Icon(Icons.Default.Close, null)
                } else Icon(
                    Icons.Default.Add,
                    null,
                )
            }
        }
    }

    @Composable
    fun Actions(closeRequest: () -> Unit) {
        val context = LocalContext.current
        val spacerModifier = Modifier.height(12.dp)
        Column(horizontalAlignment = Alignment.End) {
            NewDirectory(context, closeRequest)
            Spacer(modifier = spacerModifier)
            NewFile(context, closeRequest)
            Spacer(modifier = spacerModifier)
            ImportFile(context, closeRequest)
            Spacer(modifier = spacerModifier)
            NewProject(context, closeRequest)
            Spacer(modifier = spacerModifier)
        }
    }

    @Composable
    private fun NewProject(context: Context, closeRequest: () -> Unit) {
        ExtendedFloatingActionButton(text = { Text(text = stringResource(id = R.string.text_project)) },
            icon = { Icon(painterResource(id = R.drawable.ic_project2), null) },
            onClick = {
                closeRequest()
                val explorerView = this@ScriptListFragment.explorerView
                ProjectConfigActivity.newProject(context, explorerView.currentPage!!.toScriptFile())
            })
    }


    @Composable
    private fun ImportFile(context: Context, closeRequest: () -> Unit) {
        ExtendedFloatingActionButton(text = { Text(text = stringResource(id = R.string.text_import)) },
            icon = { Icon(painterResource(id = R.drawable.ic_floating_action_menu_open), null) },
            onClick = {
                closeRequest()
                getScriptOperations(
                    context, this@ScriptListFragment
                ).importFile()
            })
    }

    @Composable
    private fun NewFile(context: Context, closeRequest: () -> Unit) {
        ExtendedFloatingActionButton(text = { Text(text = stringResource(id = R.string.text_file)) },
            icon = { Icon(painterResource(id = R.drawable.ic_floating_action_menu_file), null) },
            onClick = {
                closeRequest()
                getScriptOperations(
                    context, this@ScriptListFragment
                ).newFile()
            })
    }

    @Composable
    private fun NewDirectory(context: Context, closeRequest: () -> Unit) {
        ExtendedFloatingActionButton(text = { Text(text = stringResource(id = R.string.text_directory)) },
            icon = { Icon(painterResource(id = R.drawable.ic_floating_action_menu_dir), null) },
            onClick = {
                closeRequest()
                getScriptOperations(
                    context, this@ScriptListFragment
                ).newDirectory()
            })
    }

    fun ExplorerViewKt.setUpViews() {
        fillMaxSize()
        sortConfig = SortConfig.from(
            PreferenceManager.getDefaultSharedPreferences(
                requireContext()
            )
        )
        setExplorer(
            Explorers.workspace(),
            ExplorerDirPage.createRoot(Pref.getScriptDirPath())
        )
        setOnItemClickListener { _, item ->
            item?.let {
                if (item.isEditable) {
                    edit(requireContext(), item.toScriptFile());
                } else {
                    IntentUtil.viewFile(get(), item.path, AppFileProvider.AUTHORITY)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        explorerView.sortConfig?.saveInto(
            PreferenceManager.getDefaultSharedPreferences(
                requireContext()
            )
        )
    }

    private fun getScriptOperations(
        context: Context,
        scriptListFragment: ScriptListFragment
    ): ScriptOperations {
        val explorerView = scriptListFragment.explorerView
        return ScriptOperations(
            context,
            explorerView,
            explorerView.currentPage
        )
    }

    fun onBackPressed(): Boolean {
        if (explorerView.canGoBack()) {
            explorerView.goBack()
            return true
        }
        return false
    }

    companion object {
        private const val TAG = "MyScriptListFragment"
    }


}